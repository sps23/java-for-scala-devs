package io.github.sps23.designpatterns.singleton;

import java.io.Serial;
import java.io.Serializable;

/**
 * Eager initialization singleton pattern in Java.
 *
 * The instance is created when the class is loaded, guaranteeing thread safety
 * without synchronization overhead. This is the simplest and most recommended
 * approach for most use cases.
 *
 * Trade-offs: - ✓ Thread-safe by default (class loading guarantees atomicity) -
 * ✓ Simple and efficient - ✗ Instance created even if never used
 */
public class DatabaseConnection implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private static final DatabaseConnection instance = new DatabaseConnection();

    // Private constructor prevents external instantiation
    private DatabaseConnection() {
        System.out.println("DatabaseConnection instance created");
    }

    // Global access point
    public static DatabaseConnection getInstance() {
        return instance;
    }

    public void connect() {
        System.out.println("Connected to database");
    }

    public void disconnect() {
        System.out.println("Disconnected from database");
    }

    // Prevents serialization from creating a new instance
    @Serial
    protected Object readResolve() {
        return instance;
    }
}
