package io.github.sps23.designpatterns.singleton;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("DatabaseConnection Eager Singleton Tests")
class DatabaseConnectionTest {

    @Test
    @DisplayName("Should return the same instance on multiple calls")
    void testSingletonInstances() {
        DatabaseConnection conn1 = DatabaseConnection.getInstance();
        DatabaseConnection conn2 = DatabaseConnection.getInstance();

        assertSame(conn1, conn2, "Instances should be identical");
    }

    @Test
    @DisplayName("Should handle concurrent access safely")
    void testConcurrentAccess() throws InterruptedException {
        int numThreads = 100;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numThreads);
        Set<DatabaseConnection> instances = new HashSet<>();

        /*
         * Try-with-resources automatically closes resources that implement
         * AutoCloseable, even if an exception occurs: ✅ executor.shutdown() called
         * automatically, guaranteed
         */
        try (ExecutorService executor = Executors.newFixedThreadPool(10)) {
            for (int i = 0; i < numThreads; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();
                        instances.add(DatabaseConnection.getInstance());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        endLatch.countDown();
                    }
                });
            }
            startLatch.countDown();
            endLatch.await();
            assertEquals(1, instances.size(),
                    "Only one instance should exist despite concurrent access");
        }
    }

    @Test
    @DisplayName("Should successfully connect to database")
    void testConnectionEstablished() {
        DatabaseConnection conn = DatabaseConnection.getInstance();
        assertDoesNotThrow(conn::connect, "Should be able to connect without throwing exception");
    }

    @Test
    @DisplayName("Should successfully disconnect from database")
    void testDisconnection() {
        DatabaseConnection conn = DatabaseConnection.getInstance();
        assertDoesNotThrow(conn::disconnect,
                "Should be able to disconnect without throwing exception");
    }
}
