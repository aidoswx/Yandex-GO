package yandexgo;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public final class Ride implements Identifiable {
    private final String id = UUID.randomUUID().toString();
    private final Passenger passenger;
    private final Tariff requestedTariff;
    private final String origin;
    private final String destination;
    private final BigDecimal distanceKm;
    private RideStatus status = RideStatus.CREATED;
    private Driver driver;
    private Car car;
    private BigDecimal price;
    private Integer rating;

    public Ride(Passenger passenger, Tariff requestedTariff, String origin,
                String destination, BigDecimal distanceKm) {
        this.passenger = Objects.requireNonNull(passenger);
        this.requestedTariff = Objects.requireNonNull(requestedTariff);
        this.origin = Objects.requireNonNull(origin).trim();
        this.destination = Objects.requireNonNull(destination).trim();
        if (this.origin.isEmpty() || this.destination.isEmpty()) {
            throw new IllegalArgumentException("Укажите адрес отправления и назначения.");
        }
        if (distanceKm == null || distanceKm.signum() <= 0) {
            throw new IllegalArgumentException("Расстояние должно быть больше нуля.");
        }
        this.distanceKm = distanceKm;
    }

    void requireStatus(RideStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Ожидается " + expected + ", текущий статус: " + status);
        }
    }

    void beginSearch() { requireStatus(RideStatus.CREATED); status = RideStatus.SEARCHING; }
    void noMatch() { requireStatus(RideStatus.SEARCHING); status = RideStatus.NO_MATCH; }

    void assign(Driver candidate, BigDecimal calculatedPrice) {
        requireStatus(RideStatus.SEARCHING);
        Car candidateCar = candidate.getCar();
        if (!candidate.isAvailable() || candidateCar == null || !candidateCar.isAvailable()
                || candidateCar.getTariff() != requestedTariff) {
            throw new IllegalArgumentException("Водитель или автомобиль не подходит.");
        }
        driver = candidate;
        car = candidateCar;
        price = Objects.requireNonNull(calculatedPrice);
        driver.setAvailable(false);
        car.setAvailable(false);
        status = RideStatus.ASSIGNED;
    }

    void start() { requireStatus(RideStatus.ASSIGNED); status = RideStatus.IN_PROGRESS; }
    void complete() {
        requireStatus(RideStatus.IN_PROGRESS);
        status = RideStatus.COMPLETED;
        driver.setAvailable(true);
        car.setAvailable(true);
    }

    void rate(int stars) {
        requireStatus(RideStatus.COMPLETED);
        if (rating != null) throw new IllegalStateException("Поездка уже оценена.");
        if (stars < 1 || stars > 5) throw new IllegalArgumentException("Оценка: от 1 до 5.");
        rating = stars;
        driver.addRating(stars);
    }

    public String getId() { return id; }
    public String getOrigin() { return origin; }
    public String getDestination() { return destination; }
    public Passenger getPassenger() { return passenger; }
    public Tariff getRequestedTariff() { return requestedTariff; }
    public BigDecimal getDistanceKm() { return distanceKm; }
    public RideStatus getStatus() { return status; }
    public Driver getDriver() { return driver; }
    public Car getCar() { return car; }
    public BigDecimal getPrice() { return price; }
    public Integer getRating() { return rating; }

    @Override public String toString() {
        return id.substring(0, 8) + " | " + origin + " -> " + destination + " | "
                + requestedTariff + " | " + status
                + (driver == null ? "" : " | " + driver.getName() + ", " + car + " | " + price + " тг")
                + (rating == null ? "" : " | оценка: " + rating);
    }
}
