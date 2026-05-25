package io.github.sps23.spring.ioc;

import java.util.Optional;

/**
 * Abstraction over order persistence.
 *
 * <p>
 * The interface separates the business logic in {@link OrderService} from the
 * choice of persistence technology. Swap in a JPA repository for production or
 * an {@link InMemoryOrderRepository} for fast, isolated tests — the service
 * code doesn't change.
 */
public interface OrderRepository {

    /**
     * Persists the order.
     *
     * @param order
     *            the order to save
     */
    void save(Order order);

    /**
     * Looks up an order by its identifier.
     *
     * @param id
     *            the order identifier
     * @return the order wrapped in an Optional, or empty if not found
     */
    Optional<Order> findById(String id);
}
