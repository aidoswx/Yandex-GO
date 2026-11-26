package yandexgo;

import java.util.Objects;

public final class Passenger extends User {

    public Passenger(String name) {
        super(name);
    }

    @Override public String getRole() { return "Пассажир"; }
}
