package io.github.sps23.designpatterns.singleton;

import java.io.Serial;
import java.io.Serializable;

/**
 * Lazy initialization singleton pattern using double-checked locking.
 *
 * The instance is created only when first requested. This saves resources if
 * the singleton is never used, but adds synchronization complexity.
 *
 * Trade-offs: - ✓ Instance created only if needed - ✗ Requires synchronization
 * (performance overhead) - ✗ More complex than eager initialization - ✗
 * Vulnerable to double-checked locking bugs if not done carefully
 */
public class LazyDatabaseConnection implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static volatile LazyDatabaseConnection instance;

    // Private constructor prevents external instantiation
    private LazyDatabaseConnection() {
        System.out.println("LazyDatabaseConnection instance created");
    }

    // Global access point with lazy initialization
    public static LazyDatabaseConnection getInstance() {
        if (instance == null) {
            synchronized (LazyDatabaseConnection.class) {
                if (instance == null) {
                    instance = new LazyDatabaseConnection();
                }
            }
        }
        return instance;
    }

    public void connect() {
        System.out.println("Connected to database (lazy)");
    }

    public void disconnect() {
        System.out.println("Disconnected from database (lazy)");
    }

    // Prevents serialization from creating a new instance
    @Serial
    protected Object readResolve() {
        return instance;
    }
}
