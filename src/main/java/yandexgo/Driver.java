package yandexgo;

import java.util.Objects;

public final class Driver extends User {
    private final Car car;
    private boolean available = true;
    private int ratingTotal;
    private int ratingCount;

    public Driver(String name, Car car) {
        super(name);
        this.car = car; // По UML водитель может не иметь автомобиля.
    }

    @Override public String getRole() { return "Водитель"; }
    public Car getCar() { return car; }
    public boolean isAvailable() { return available; }
    void setAvailable(boolean available) { this.available = available; }
    void addRating(int stars) { ratingTotal += stars; ratingCount++; }
    public int getRatingCount() { return ratingCount; }
    public double getAverageRating() {
        return ratingCount == 0 ? 0 : (double) ratingTotal / ratingCount;
    }
}
