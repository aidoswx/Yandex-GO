package yandexgo;

import java.util.List;
import java.util.Objects;

/** Worker ағындары іздейді; ортақ LOCK жүргізушіні екі рет тағайындаудан қорғайды. */
public final class RideService implements AutoCloseable {
    private static final Object LOCK = new Object();
    private final java.util.concurrent.ExecutorService workers = java.util.concurrent.Executors.newFixedThreadPool(2);
    private final PriceCalculator calculator = new PriceCalculator();

    public boolean assignMatchingDriver(Ride ride, List<Driver> candidates) {
        Objects.requireNonNull(candidates);
        synchronized (LOCK) {
        ride.beginSearch();
        for (Driver driver : candidates) {
            if (driver == null || !driver.isAvailable()) continue;
            Car car = driver.getCar();
            if (car == null || !car.isAvailable() || car.getTariff() != ride.getRequestedTariff()) continue;
            ride.assign(driver, calculator.calculate(ride.getRequestedTariff(), ride.getDistanceKm()));
            return true;
        }
        ride.noMatch();
        return false;
        }
    }

    public void startRide(Ride ride, Driver driver) {
        synchronized (LOCK) { requireAssignedDriver(ride, driver);
        ride.start(); }
    }

    public void completeRide(Ride ride, Driver driver) {
        synchronized (LOCK) { requireAssignedDriver(ride, driver);
        ride.complete(); }
    }

    private void requireAssignedDriver(Ride ride, Driver driver) {
        if (driver == null || ride.getDriver() != driver) {
            throw new IllegalArgumentException("Управлять поездкой может только назначенный водитель.");
        }
    }
    public java.util.concurrent.CompletableFuture<Boolean> assignAsync(Ride ride, List<Driver> candidates) {
        List<Driver> snapshot = List.copyOf(candidates);
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> assignMatchingDriver(ride, snapshot), workers);
    }
    @Override public void close() {
        workers.shutdown();
        try {
            if (!workers.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) workers.shutdownNow();
        } catch (InterruptedException ex) { workers.shutdownNow(); Thread.currentThread().interrupt(); }
    }}
