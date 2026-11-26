package yandexgo;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class DispatchTest {
    Ride ride() { return new Ride(new Passenger("P"), Tariff.COMFORT, "A", "B", BigDecimal.ONE); }
    Driver driver(Tariff tariff) { return new Driver("D", new Car("C", "1", tariff)); }
    @Test void skipsWrongTariffAndMissingCar() {
        try (RideService s = new RideService()) {
            Ride r = ride(); Driver d = driver(Tariff.COMFORT);
            assertTrue(s.assignMatchingDriver(r, List.of(driver(Tariff.ECONOMY), new Driver("N", null), d)));
            assertSame(d, r.getDriver()); assertSame(d.getCar(), r.getCar());
        }
    }
    @Test void noMatchIsTerminal() {
        try (RideService s = new RideService()) {
            Ride r = ride(); assertFalse(s.assignMatchingDriver(r, List.of()));
            assertEquals(RideStatus.NO_MATCH, r.getStatus());
            assertThrows(IllegalStateException.class, () -> s.assignMatchingDriver(r, List.of()));
        }
    }
    @Test void invalidTransitionAndWrongDriverRejected() {
        try (RideService s = new RideService()) {
            Ride r = ride(); Driver d = driver(Tariff.COMFORT); s.assignMatchingDriver(r, List.of(d));
            assertThrows(IllegalStateException.class, () -> s.completeRide(r, d));
            assertThrows(IllegalArgumentException.class, () -> s.startRide(r, driver(Tariff.COMFORT)));
        }
    }
    @Test void completionReleasesDriverAndCar() {
        try (RideService s = new RideService()) {
            Ride r = ride(); Driver d = driver(Tariff.COMFORT); s.assignMatchingDriver(r, List.of(d));
            s.startRide(r, d); s.completeRide(r, d);
            assertTrue(d.isAvailable()); assertTrue(d.getCar().isAvailable());
            assertTrue(s.assignMatchingDriver(ride(), List.of(d)));
        }
    }
    @Test void concurrentRequestsCannotDoubleBook() throws Exception {
        try (RideService s = new RideService()) {
            Driver d = driver(Tariff.COMFORT);
            List<CompletableFuture<Boolean>> jobs = new ArrayList<>();
            for (int i = 0; i < 30; i++) jobs.add(s.assignAsync(ride(), List.of(d)));
            CompletableFuture.allOf(jobs.toArray(new CompletableFuture<?>[0])).get(5, TimeUnit.SECONDS);
            assertEquals(1, jobs.stream().filter(CompletableFuture::join).count());
        }
    }
    @Test void sharedCarCannotBeBookedTwiceAcrossServices() throws Exception {
        try (RideService a = new RideService(); RideService b = new RideService()) {
            Car car = new Car("C", "1", Tariff.COMFORT);
            var first = a.assignAsync(ride(), List.of(new Driver("A", car)));
            var second = b.assignAsync(ride(), List.of(new Driver("B", car)));
            assertNotEquals(first.get(5, TimeUnit.SECONDS), second.get(5, TimeUnit.SECONDS));
        }
    }
}
