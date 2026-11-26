package yandexgo;

import java.util.Objects;
import java.util.UUID;

/** Абстракт базалық класс: Passenger және Driver ортақ деректерін сақтайды. */
public abstract class User implements Identifiable {
    private final String id = UUID.randomUUID().toString();
    private final String name;
    protected User(String name) {
        this.name = Objects.requireNonNull(name).trim();
        if (this.name.isEmpty()) throw new IllegalArgumentException("Имя не должно быть пустым.");
    }
    public final String getId() { return id; }
    public final String getName() { return name; }
    public abstract String getRole();
    public String describe() { return getRole() + ": " + name; }
}
