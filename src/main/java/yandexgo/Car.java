package yandexgo;

import java.util.Objects;

public final class Car {
    private final String model;
    private final String plate;
    private final Tariff tariff;
    private boolean available = true;

    public Car(String model, String plate, Tariff tariff) {
        this.model = Objects.requireNonNull(model);
        this.plate = Objects.requireNonNull(plate);
        this.tariff = Objects.requireNonNull(tariff);
    }

    public Tariff getTariff() { return tariff; }
    public boolean isAvailable() { return available; }
    void setAvailable(boolean available) { this.available = available; }
    @Override public String toString() { return model + " (" + plate + ")"; }
}
