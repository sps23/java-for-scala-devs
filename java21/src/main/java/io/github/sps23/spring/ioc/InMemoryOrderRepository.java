package io.github.sps23.spring.ioc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of {@link OrderRepository} for testing and demos.
 *
 * <p>
 * No database, no Spring context, no infrastructure — just a
 * {@link ConcurrentHashMap}. This is all you need to run fast, isolated unit
 * tests for any service that depends on {@link OrderRepository}.
 */
public class InMemoryOrderRepository implements OrderRepository {

    private final ConcurrentHashMap<String, Order> store = new ConcurrentHashMap<>();

    @Override
    public void save(Order order) {
        store.put(order.id(), order);
    }

    @Override
    public Optional<Order> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    /**
     * Returns all saved orders — useful for verifying test outcomes.
     *
     * @return unmodifiable list of all saved orders
     */
    public List<Order> findAll() {
        return Collections.unmodifiableList(new ArrayList<>(store.values()));
    }

    /**
     * Clears all stored orders — handy in {@code @AfterEach} setup.
     */
    public void clear() {
        store.clear();
    }
}
