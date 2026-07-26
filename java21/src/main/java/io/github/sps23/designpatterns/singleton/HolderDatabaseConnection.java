package io.github.sps23.designpatterns.singleton;

import java.io.Serial;
import java.io.Serializable;

/**
 * Holder pattern singleton (recommended in Java). Combines lazy initialization
 * with the simplicity of eager initialization by using a static inner class.
 * The inner class is only loaded when getInstance() is called, providing lazy
 * initialization without explicit synchronization. Trade-offs: - ✓ Thread-safe
 * by default (class loading guarantees atomicity) - ✓ Lazy initialization
 * without synchronization - ✓ Simple and efficient - This is the modern best
 * practice in Java
 */
public class HolderDatabaseConnection implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    // Private constructor prevents external instantiation
    private HolderDatabaseConnection() {
        System.out.println("HolderDatabaseConnection instance created");
    }

    // Static holder inner class
    private static class ConnectionHolder {
        static final HolderDatabaseConnection instance = new HolderDatabaseConnection();
    }

    // Global access point
    public static HolderDatabaseConnection getInstance() {
        return ConnectionHolder.instance;
    }

    public void connect() {
        System.out.println("Connected to database (holder pattern)");
    }

    public void disconnect() {
        System.out.println("Disconnected from database (holder pattern)");
    }

    // Prevents serialization from creating a new instance
    @Serial
    protected Object readResolve() {
        return getInstance();
    }
}
