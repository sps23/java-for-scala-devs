package io.github.sps23.spring.scopes;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayDeque;
import java.util.Deque;
import org.springframework.stereotype.Component;

/**
 * A real Spring-managed bean demonstrating the annotation-based lifecycle
 * callbacks {@code @PostConstruct} and {@code @PreDestroy}.
 *
 * <p>
 * This is the recommended, framework-agnostic way to hook into a bean's
 * lifecycle in modern Spring: both annotations come from
 * {@code jakarta.annotation} (JSR-250), not from a Spring-specific package, so
 * the class stays portable to any compliant container.
 *
 * <p>
 * {@code @PostConstruct} runs once, after all dependencies have been injected
 * and immediately before the bean is put into service. {@code @PreDestroy} runs
 * once, when the (singleton-scoped, by default) container starts its shutdown
 * sequence — for example when {@code ApplicationContext.close()} is called.
 */
@Component
public class ManagedConnectionPool {

    private static final int POOL_SIZE = 5;

    private final Deque<String> connections = new ArrayDeque<>();
    private boolean open;

    @PostConstruct
    void openPool() {
        for (int i = 0; i < POOL_SIZE; i++) {
            connections.push("connection-" + i);
        }
        open = true;
    }

    @PreDestroy
    void closePool() {
        connections.clear();
        open = false;
    }

    /**
     * Borrows a connection from the pool.
     *
     * @return a pooled connection identifier
     * @throws IllegalStateException
     *             if the pool has not been opened (or has already been closed)
     */
    public String borrowConnection() {
        if (!open) {
            throw new IllegalStateException("Connection pool is not open");
        }
        var connection = connections.poll();
        if (connection == null) {
            throw new IllegalStateException("No connections available");
        }
        return connection;
    }

    public boolean isOpen() {
        return open;
    }

    public int availableConnections() {
        return connections.size();
    }
}
