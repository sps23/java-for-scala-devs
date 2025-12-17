package io.github.sps23.testing.unittesting.antipatterns;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * ANTI-PATTERN EXAMPLES: Common mistakes in unit testing that impact
 * performance and reliability.
 *
 * <p>
 * These examples demonstrate what NOT to do when writing tests.
 *
 * <p>
 * NOTE: These tests are disabled because they are intentionally bad examples
 * that would fail in a real test suite.
 */
@DisplayName("Anti-Pattern Test Examples - DO NOT COPY THESE!")
@Disabled("These are intentionally bad examples for documentation purposes")
class BadTestExamples {

    // ANTI-PATTERN #1: Shared mutable state between tests
    // This causes test interdependence and race conditions in parallel execution
    private static int sharedCounter = 0; // NEVER DO THIS!
    private static StringBuilder sharedBuilder = new StringBuilder(); // NEVER DO THIS!

    @Test
    @DisplayName("ANTI-PATTERN: Test that modifies shared state")
    void testWithSharedState1() {
        sharedCounter++; // Modifying shared state
        assertEquals(1, sharedCounter); // This will fail if tests run in parallel or wrong order
    }

    @Test
    @DisplayName("ANTI-PATTERN: Another test modifying same shared state")
    void testWithSharedState2() {
        sharedCounter++; // Modifying same shared state
        assertEquals(1, sharedCounter); // Will fail depending on test execution order - FLAKY TEST!
    }

    // ANTI-PATTERN #2: Tests that depend on execution order
    @Test
    @DisplayName("ANTI-PATTERN: Test expecting specific execution order")
    void testThatRunsFirst() {
        sharedBuilder.append("first");
    }

    @Test
    @DisplayName("ANTI-PATTERN: Test depending on previous test")
    void testThatRunsSecond() {
        // Assumes testThatRunsFirst() ran before this - FRAGILE!
        assertEquals("first", sharedBuilder.toString());
    }

    // ANTI-PATTERN #3: Tests with real external dependencies
    @Test
    @DisplayName("ANTI-PATTERN: Test requiring real database")
    void testWithRealDatabase() {
        // BadUserService would connect to real PostgreSQL database
        // This makes the test:
        // - Slow (network I/O)
        // - Fragile (depends on database being available)
        // - Non-isolated (shares database with other tests)
        // - Difficult to run in CI/CD
        // var service = new BadUserService(); // Don't do this!
        // service.createUser("1", "John", "john@example.com");
    }

    // ANTI-PATTERN #4: Tests with Thread.sleep() to "ensure" timing
    @Test
    @DisplayName("ANTI-PATTERN: Test with Thread.sleep for timing")
    void testWithSleep() throws InterruptedException {
        // Start some async operation...

        // ANTI-PATTERN: Using sleep to wait for completion
        Thread.sleep(1000); // This is unreliable and slows down tests!

        // Better: Use proper synchronization, mocks, or test doubles
    }

    // ANTI-PATTERN #5: Test without proper setup/teardown causing resource leaks
    @Test
    @DisplayName("ANTI-PATTERN: Test without cleanup")
    void testWithoutCleanup() {
        // Opens a file but never closes it
        // var file = new FileWriter("/tmp/test.txt"); // Resource leak!
        // ... use file ...
        // Missing: file.close() or try-with-resources
    }

    // ANTI-PATTERN #6: Catching exceptions and passing silently
    @Test
    @DisplayName("ANTI-PATTERN: Swallowing exceptions in tests")
    void testSwallowingExceptions() {
        try {
            // Code that should throw exception
            throw new IllegalArgumentException("This should fail the test!");
        } catch (Exception e) {
            // ANTI-PATTERN: Silently catching exception - test passes incorrectly!
            // Should use assertThrows() instead
        }
    }

    // ANTI-PATTERN #7: Testing multiple unrelated things in one test
    @Test
    @DisplayName("ANTI-PATTERN: Giant test method testing everything")
    void testEverythingInOneGiantMethod() {
        // Tests user creation
        // Tests user retrieval
        // Tests user update
        // Tests user deletion
        // Tests email validation
        // Tests database connection
        // ... 200 more lines ...

        // PROBLEMS:
        // - Hard to understand what's being tested
        // - When it fails, unclear which part failed
        // - Difficult to maintain
        // - Violates Single Responsibility Principle
    }

    // ANTI-PATTERN #8: No setup method, duplicating initialization everywhere
    @Test
    @DisplayName("ANTI-PATTERN: Duplicating setup in every test")
    void testWithDuplicatedSetup1() {
        // Creating mocks and objects here
        Object repository = null; // ... setup code ...
        Object emailService = null; // ... setup code ...
        Object service = null; // ... setup code ...
        // ... 10 lines of setup duplicated in every test
    }

    @Test
    @DisplayName("ANTI-PATTERN: More duplicated setup")
    void testWithDuplicatedSetup2() {
        // Same setup code duplicated again - violation of DRY principle
        Object repository = null;
        Object emailService = null;
        Object service = null;
    }

    // ANTI-PATTERN #9: Test that requires specific system state
    @Test
    @DisplayName("ANTI-PATTERN: Test requiring specific environment")
    void testRequiringSpecificEnvironment() {
        // Assumes specific environment variable exists
        String dbUrl = System.getenv("DATABASE_URL");
        assertNotNull(dbUrl); // Fails on machines without this env var - NOT PORTABLE!

        // Assumes specific file exists
        // File config = new File("/etc/myapp/config.json");
        // assertTrue(config.exists()); // Fails on Windows, different systems, etc.
    }

    // ANTI-PATTERN #10: Test with System.out assertions
    @Test
    @DisplayName("ANTI-PATTERN: Testing console output directly")
    void testConsoleOutput() {
        // Code that prints to console
        System.out.println("Some output");

        // No proper way to assert on System.out without complex setup
        // Better: Use logging framework with testable appenders
    }
}
