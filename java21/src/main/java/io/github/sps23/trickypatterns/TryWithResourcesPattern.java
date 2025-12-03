package io.github.sps23.trickypatterns;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Demonstrates try-with-resources and its subtle gotchas.
 *
 * The tricky part: Resources are closed in reverse order, suppressed
 * exceptions, and the confusion between try-with-resources and traditional
 * try-finally.
 */
public class TryWithResourcesPattern {

    public static void main(String[] args) {
        demonstrateCloseOrder();
        demonstrateSuppressedExceptions();
        demonstrateEffectivelyFinal();
        demonstrateCommonMistakes();
    }

    /**
     * Shows the non-intuitive close order
     */
    private static void demonstrateCloseOrder() {
        System.out.println("=== CLOSE ORDER (Reverse!) ===");
        System.out.println();

        try (DemoResource first = new DemoResource("First");
                DemoResource second = new DemoResource("Second");
                DemoResource third = new DemoResource("Third")) {
            System.out.println("Using resources...");
        }

        System.out.println();
        System.out.println("Key point: Resources closed in REVERSE order");
        System.out.println("  Opened: First → Second → Third");
        System.out.println("  Closed: Third → Second → First");
        System.out.println("  Like a stack (LIFO)");
        System.out.println();
    }

    /**
     * Shows suppressed exceptions behavior
     */
    private static void demonstrateSuppressedExceptions() {
        System.out.println("=== SUPPRESSED EXCEPTIONS ===");
        System.out.println();

        // Exception in both body and close
        try {
            try (FailingResource resource = new FailingResource()) {
                System.out.println("About to throw from body");
                throw new RuntimeException("Exception from body");
            }
        } catch (Exception e) {
            System.out.println("Caught: " + e.getMessage());
            System.out.println("Suppressed exceptions: " + e.getSuppressed().length);
            for (Throwable suppressed : e.getSuppressed()) {
                System.out.println("  Suppressed: " + suppressed.getMessage());
            }
        }

        System.out.println();
        System.out.println("Behavior:");
        System.out.println("  1. Body exception is the primary exception");
        System.out.println("  2. Close exceptions are added as suppressed");
        System.out.println("  3. Access via exception.getSuppressed()");
        System.out.println();
        System.out.println("Without try-with-resources:");
        System.out.println("  Close exception would HIDE body exception!");
        System.out.println("  Major bug source in manual try-finally");
        System.out.println();
    }

    /**
     * Shows effectively final requirement (Java 9+)
     */
    private static void demonstrateEffectivelyFinal() {
        System.out.println("=== EFFECTIVELY FINAL (Java 9+) ===");
        System.out.println();

        // Java 9+ allows effectively final variables
        DemoResource resource = new DemoResource("External");

        try (resource) { // ✅ Java 9+: Can use existing variable
            System.out.println("Using external resource");
        }

        // Note: resource is already closed here!
        System.out.println("After try-with-resources, resource is closed");
        System.out.println();

        // This would NOT work (not effectively final):
        // DemoResource mutable = new DemoResource("Mutable");
        // mutable = new DemoResource("Changed"); // Reassignment
        // try (mutable) { // ❌ Compilation error!
        // }

        System.out.println("Java 9+ feature:");
        System.out.println("  Can use existing variables (if effectively final)");
        System.out.println("  No need to declare in try() clause");
        System.out.println("  But variable must not be reassigned");
        System.out.println();
    }

    /**
     * Shows common mistakes
     */
    private static void demonstrateCommonMistakes() {
        System.out.println("=== COMMON MISTAKES ===");
        System.out.println();

        // Mistake 1: Returning resource from try-with-resources
        System.out.println("Mistake 1: Returning closed resource");
        try {
            DemoResource resource = getClosedResource();
            // resource is already closed!
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
        System.out.println();

        // Mistake 2: Forgetting implements AutoCloseable
        System.out.println("Mistake 2: Not implementing AutoCloseable");
        System.out.println("  Cannot use with try-with-resources");
        System.out.println("  Must implement AutoCloseable or Closeable");
        System.out.println();

        // Mistake 3: Multiple try blocks for dependent resources
        System.out.println("Mistake 3: Nested try for dependent resources");
        System.out.println("  ❌ try (A a = ...) { try (B b = new B(a)) { } }");
        System.out.println("  ✅ try (A a = ...; B b = new B(a)) { }");
        System.out.println();
    }

    /**
     * Returns a closed resource (DON'T DO THIS!)
     */
    private static DemoResource getClosedResource() {
        try (DemoResource resource = new DemoResource("Returned")) {
            return resource; // ❌ Resource is closed after return!
        }
    }

    /**
     * Demonstrates Lock pattern confusion
     */
    public static void demonstrateLockPattern() {
        System.out.println("=== LOCK PATTERN GOTCHA ===");
        System.out.println();

        Lock lock = new ReentrantLock();

        // ❌ WRONG: Lock is not AutoCloseable
        // try (lock.lock()) { // Compilation error!
        // // critical section
        // }

        // ✅ Correct traditional way
        lock.lock();
        try {
            System.out.println("Critical section");
        } finally {
            lock.unlock();
        }

        // ✅ Or use a helper (Java 10+)
        try (var locked = new LockResource(lock)) {
            System.out.println("Critical section with helper");
        }

        System.out.println();
        System.out.println("Key point:");
        System.out.println("  Lock is NOT AutoCloseable");
        System.out.println("  Must use traditional try-finally");
        System.out.println("  Or wrap in AutoCloseable helper");
    }

    // Helper classes for demonstration

    static class DemoResource implements AutoCloseable {
        private final String name;
        private boolean closed = false;

        public DemoResource(String name) {
            this.name = name;
            System.out.println("  Opening: " + name);
        }

        @Override
        public void close() {
            if (closed) {
                throw new IllegalStateException("Already closed: " + name);
            }
            closed = true;
            System.out.println("  Closing: " + name);
        }
    }

    static class FailingResource implements AutoCloseable {
        @Override
        public void close() throws Exception {
            throw new Exception("Exception from close()");
        }
    }

    static class LockResource implements AutoCloseable {
        private final Lock lock;

        public LockResource(Lock lock) {
            this.lock = lock;
            lock.lock();
        }

        @Override
        public void close() {
            lock.unlock();
        }
    }

    /**
     * Shows ordering matters for exceptions
     */
    public static void demonstrateExceptionOrdering() {
        System.out.println("=== EXCEPTION ORDERING ===");

        // If first resource fails to close, second still closes
        try (DemoResource first = new DemoResource("First");
                FailingResource failing = new FailingResource();
                DemoResource third = new DemoResource("Third")) {
            System.out.println("Body executes");
        } catch (Exception e) {
            System.out.println("Caught: " + e.getClass().getSimpleName());
            System.out.println("All resources attempted close despite exception");
        }

        System.out.println();
        System.out.println("Guarantee: All resources close even if some fail");
        System.out.println("Exceptions are collected as suppressed");
    }
}
