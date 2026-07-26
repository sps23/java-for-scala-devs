package io.github.sps23.spring.scopes;

import org.springframework.stereotype.Service;

/**
 * A real Spring-managed {@code @Service} bean used to demonstrate the default
 * <strong>singleton</strong> scope.
 *
 * <p>
 * No {@code @Scope} annotation is needed — {@code singleton} is the default.
 * Spring creates exactly <strong>one</strong> instance of this class per
 * {@code ApplicationContext} and hands out that same instance to every caller.
 *
 * <p>
 * <strong>The golden rule:</strong> singleton beans must be stateless, or use
 * thread-safe state. Because every caller shares the same instance, the plain
 * {@code int} field below is a genuine race condition under concurrent load —
 * it is kept here deliberately to demonstrate the danger, not as a best
 * practice.
 */
@Service
public class OrderVolumeTracker {

    // ⚠️ Shared mutable state across every caller of this singleton bean.
    // In a real service, replace with a java.util.concurrent.atomic.AtomicInteger
    // or push counting into a proper metrics registry.
    private int ordersProcessed;

    /**
     * Records that one more order has been processed.
     *
     * @return the total number of orders processed by this singleton instance so
     *         far
     */
    public synchronized int recordOrderProcessed() {
        ordersProcessed++;
        return ordersProcessed;
    }

    /**
     * Returns the current count without modifying it.
     *
     * @return orders processed so far
     */
    public synchronized int ordersProcessed() {
        return ordersProcessed;
    }
}
