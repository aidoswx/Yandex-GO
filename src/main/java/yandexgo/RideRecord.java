package yandexgo;

import java.math.BigDecimal;

/** Immutable DTO: есептер өткен сессиялардың деректерін де пайдаланады. */
public record RideRecord(String id, String passenger, String driver, String origin,
                         String destination, Tariff tariff, RideStatus status,
                         BigDecimal distance, BigDecimal price, Integer rating) implements Identifiable {
    @Override public String getId() { return id; }
    public static RideRecord from(Ride r) {
        return new RideRecord(r.getId(), r.getPassenger().getName(),
                r.getDriver() == null ? null : r.getDriver().getName(), r.getOrigin(),
                r.getDestination(), r.getRequestedTariff(), r.getStatus(), r.getDistanceKm(),
                r.getPrice(), r.getRating());
    }
}
