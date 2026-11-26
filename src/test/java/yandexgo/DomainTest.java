package yandexgo;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DomainTest {
    @Test void rolesArePolymorphic() {
        List<User> users = List.of(new Passenger("A"), new Driver("B", null));
        assertEquals(List.of("Пассажир: A", "Водитель: B"), users.stream().map(User::describe).toList());
    }
    @Test void invalidDistanceRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Ride(new Passenger("A"), Tariff.ECONOMY, "A", "B", BigDecimal.ZERO));
    }
    @Test void emptyAddressRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Ride(new Passenger("A"), Tariff.ECONOMY, " ", "B", BigDecimal.ONE));
    }
    @Test void pricesDependOnTariff() {
        PriceCalculator c = new PriceCalculator();
        assertEquals(new BigDecimal("700.00"), c.calculate(Tariff.ECONOMY, new BigDecimal("2")));
        assertEquals(new BigDecimal("1100.00"), c.calculate(Tariff.COMFORT, new BigDecimal("2")));
        assertEquals(new BigDecimal("2000.00"), c.calculate(Tariff.BUSINESS, new BigDecimal("2")));
    }
    @Test void ratingOnlyOnceAfterCompletionByOwner() {
        Passenger p = new Passenger("A");
        Ride r = new Ride(p, Tariff.ECONOMY, "A", "B", BigDecimal.ONE);
        Driver d = new Driver("D", new Car("Car", "1", Tariff.ECONOMY));
        RatingService ratings = new RatingService();
        try (RideService s = new RideService()) {
            s.assignMatchingDriver(r, List.of(d));
            assertThrows(IllegalStateException.class, () -> ratings.rateRide(r, p, 5));
            s.startRide(r, d); s.completeRide(r, d);
            assertThrows(IllegalArgumentException.class, () -> ratings.rateRide(r, p, 6));
            assertThrows(IllegalArgumentException.class, () -> ratings.rateRide(r, new Passenger("A"), 5));
            ratings.rateRide(r, p, 4);
            assertEquals(4, d.getAverageRating());
            assertThrows(IllegalStateException.class, () -> ratings.rateRide(r, p, 5));
        }
    }
}
