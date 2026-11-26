package yandexgo;

import java.util.*;

/** Өз generic класы. Map идентификатор бойынша іздеуді, List реттілікті береді. */
public final class Repository<T extends Identifiable> {
    private final Map<String, T> items = new LinkedHashMap<>();
    public synchronized void save(T item) { items.put(item.getId(), item); }
    public synchronized Optional<T> findById(String id) { return Optional.ofNullable(items.get(id)); }
    public synchronized List<T> findAll() { return List.copyOf(items.values()); }
}
