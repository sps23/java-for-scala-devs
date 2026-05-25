package io.github.sps23.spring.scopes;

/**
 * Demonstrates Spring bean lifecycle callbacks.
 *
 * <p>
 * In a Spring application this class would carry {@code @Component} (or
 * {@code @Service}), and the two lifecycle methods would be annotated:
 *
 * <pre>
 * {@code @PostConstruct}  // runs after all dependencies are injected
 * public void open() { ... }
 *
 * {@code @PreDestroy}     // runs before the bean is removed from the context
 * public void close() { ... }
 * </pre>
 *
 * <p>
 * This plain-Java version lets us test the same behaviour without a Spring
 * runtime. The test simply calls {@code open()} and {@code close()} directly,
 * mirroring what Spring would do automatically.
 */
public class ConnectionPool {

    private final String url;
    private final int maxConnections;

    private boolean open;
    private int activeConnections;

    public ConnectionPool(String url, int maxConnections) {
        this.url = url;
        this.maxConnections = maxConnections;
    }

    /**
     * Initialises the pool. Called by Spring immediately after all dependencies
     * are injected (equivalent of {@code @PostConstruct}).
     */
    public void open() {
        this.open = true;
        this.activeConnections = 0;
        System.out.println("Connection pool opened — " + maxConnections + " slots ready at " + url);
    }

    /**
     * Releases all connections and shuts down the pool. Called by Spring just
     * before the bean is destroyed (equivalent of {@code @PreDestroy}).
     */
    public void close() {
        this.open = false;
        System.out.println(
                "Connection pool closed — " + activeConnections + " active connections released");
        this.activeConnections = 0;
    }

    /**
     * Borrows a connection slot from the pool.
     *
     * @return {@code true} if a slot was available; {@code false} if the pool is
     *         full
     * @throws IllegalStateException if the pool has not been opened yet
     */
    public boolean borrow() {
        if (!open) {
            throw new IllegalStateException("Pool is not open — call open() first");
        }
        if (activeConnections >= maxConnections) {
            return false;
        }
        activeConnections++;
        return true;
    }

    /**
     * Returns a previously borrowed connection slot to the pool.
     */
    public void release() {
        if (activeConnections > 0) {
            activeConnections--;
        }
    }

    public boolean isOpen() {
        return open;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public int getActiveConnections() {
        return activeConnections;
    }

    public String getUrl() {
        return url;
    }
}
