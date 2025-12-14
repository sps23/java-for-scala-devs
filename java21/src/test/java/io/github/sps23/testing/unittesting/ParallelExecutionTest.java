package io.github.sps23.testing.unittesting;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

/**
 * Demonstrates parallel test execution in JUnit 5. Shows how to: 1. Enable
 * parallel execution 2. Use thread-safe data structures 3. Avoid shared mutable
 * state 4. Measure performance improvement
 *
 * <p>
 * To enable parallel execution globally, add to
 * src/test/resources/junit-platform.properties:
 * junit.jupiter.execution.parallel.enabled=true
 * junit.jupiter.execution.parallel.mode.default=concurrent
 * junit.jupiter.execution.parallel.config.strategy=dynamic
 */
@DisplayName("Parallel Test Execution Examples")
@Execution(ExecutionMode.CONCURRENT) // Enable parallel execution for this class
class ParallelExecutionTest {

    // Thread-safe counter for demonstration
    private static final AtomicInteger executionCounter = new AtomicInteger(0);

    @BeforeAll
    static void setupAll() {
        executionCounter.set(0);
    }

    @Nested
    @DisplayName("Tests that can run in parallel")
    @Execution(ExecutionMode.CONCURRENT)
    class IndependentTests {

        @RepeatedTest(5)
        @DisplayName("Independent test 1 - no shared state")
        void independentTest1() {
            // Each test has its own local variables
            int localValue = executionCounter.incrementAndGet();
            assertTrue(localValue > 0);

            // Simulate some work
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @RepeatedTest(5)
        @DisplayName("Independent test 2 - no shared state")
        void independentTest2() {
            int localValue = executionCounter.incrementAndGet();
            assertTrue(localValue > 0);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        @RepeatedTest(5)
        @DisplayName("Independent test 3 - no shared state")
        void independentTest3() {
            int localValue = executionCounter.incrementAndGet();
            assertTrue(localValue > 0);

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Nested
    @DisplayName("Tests that must run sequentially")
    @Execution(ExecutionMode.SAME_THREAD) // Force sequential execution for tests that need it
                                          // (rarely)
    class SequentialTests {

        private int sequentialCounter = 0;

        @Test
        @DisplayName("Sequential test 1")
        void sequentialTest1() {
            sequentialCounter = 1;
            assertEquals(1, sequentialCounter);
        }

        @Test
        @DisplayName("Sequential test 2")
        void sequentialTest2() {
            sequentialCounter = 2;
            assertEquals(2, sequentialCounter);
        }
    }

    @Nested
    @DisplayName("Performance comparison")
    class PerformanceTest {

        @Test
        @DisplayName("Measure parallel execution benefit")
        void measureParallelBenefit() {
            // This test demonstrates the concept - actual timing depends on system
            // With parallel execution enabled, multiple tests run simultaneously

            Instant start = Instant.now();

            // Simulate multiple independent operations
            for (int i = 0; i < 3; i++) {
                // In parallel tests, these would run concurrently
                simulateWork(50);
            }

            Instant end = Instant.now();
            Duration elapsed = Duration.between(start, end);

            // Sequential: ~150ms (3 * 50ms)
            // Parallel (with 3+ threads): ~50ms
            assertTrue(elapsed.toMillis() >= 150);
        }

        private void simulateWork(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Nested
    @DisplayName("Best practices for parallel tests")
    class BestPractices {

        @Test
        @DisplayName("Use local variables instead of instance fields")
        void useLocalVariables() {
            // GOOD: Local variable - thread-safe
            String localData = "test-data-" + System.currentTimeMillis();
            assertNotNull(localData);

            // AVOID: Instance or static fields that are mutable
        }

        @Test
        @DisplayName("Create fresh mocks for each test")
        void useFreshMocks() {
            // GOOD: Create mocks in @BeforeEach
            // Each test gets its own mock instance
            var repository = new FakeRepository();
            assertNotNull(repository);
        }

        @Test
        @DisplayName("Use thread-safe collections when needed")
        void useThreadSafeCollections() {
            // If you must share data (rare), use thread-safe structures
            // java.util.concurrent.ConcurrentHashMap
            // java.util.concurrent.CopyOnWriteArrayList
            // java.util.concurrent.atomic.*
            assertTrue(true);
        }
    }

    // Simple fake repository for demonstration
    static class FakeRepository {
        // Implementation details...
    }
}
