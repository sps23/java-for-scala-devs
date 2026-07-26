package io.github.sps23.designpatterns.singleton

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@DisplayName("DatabaseConnection Kotlin Singleton Tests")
class DatabaseConnectionTest {
    @Test
    @DisplayName("Should handle concurrent access safely")
    fun testConcurrentAccess() {
        val numThreads = 100
        val startLatch = CountDownLatch(1)
        val endLatch = CountDownLatch(numThreads)
        val executionCount = java.util.concurrent.atomic.AtomicInteger(0)

        /*
         * How It Works
         * • use() is Kotlin's idiomatic equivalent to Java's try-with-resources
         * • It guarantees the resource is closed (executor shutdown) even if an exception occurs
         * • The resource is available inside the lambda via the executor parameter
         * • Much more readable than nested try-finally blocks
         */
        Executors.newFixedThreadPool(10).use { executor ->
            repeat(numThreads) {
                executor.submit {
                    try {
                        startLatch.await()
                        DatabaseConnection.connect()
                        executionCount.incrementAndGet()
                    } finally {
                        endLatch.countDown()
                    }
                }
            }

            startLatch.countDown()
            endLatch.await()
        }

        assertEquals(
            numThreads,
            executionCount.get(),
            "All threads should execute without errors",
        )
    }

    @Test
    @DisplayName("Should successfully connect to database")
    fun testConnectionEstablished() {
        assertDoesNotThrow {
            DatabaseConnection.connect()
        }
    }

    @Test
    @DisplayName("Should successfully disconnect from database")
    fun testDisconnection() {
        assertDoesNotThrow {
            DatabaseConnection.disconnect()
        }
    }

    @Test
    @DisplayName("Stateful companion object should be singleton")
    fun testStatefulSingletonInstance() {
        val conn1 = StatefulDatabaseConnection.getInstance()
        val conn2 = StatefulDatabaseConnection.getInstance()

        assertSame(conn1, conn2, "Instances should be identical")
    }

    @Test
    @DisplayName("Stateful singleton should maintain connection state")
    fun testStatefulConnectionState() {
        val conn = StatefulDatabaseConnection.getInstance()
        assertDoesNotThrow {
            conn.connect()
        }
        assertTrue(conn.isConnected(), "Connection should be established")
    }
}
