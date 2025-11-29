package io.github.sps23.interview.preparation.virtualthreads;

/**
 * Guide: When to use Virtual Threads vs Platform Threads.
 *
 * <p>
 * This class provides a comprehensive comparison and decision guide for
 * choosing between virtual threads and platform threads in Java 21.
 *
 * <h2>Virtual Threads - Best For:</h2>
 * <ul>
 * <li>I/O-bound operations (HTTP calls, database queries, file I/O)</li>
 * <li>High-concurrency servers handling many simultaneous connections</li>
 * <li>Microservices making many outbound API calls</li>
 * <li>Batch processing with parallel I/O operations</li>
 * <li>Replacing CompletableFuture chains with simpler blocking code</li>
 * </ul>
 *
 * <h2>Platform Threads - Best For:</h2>
 * <ul>
 * <li>CPU-bound computations (number crunching, cryptography)</li>
 * <li>Native code integration (JNI calls)</li>
 * <li>Operations requiring thread affinity</li>
 * <li>Code using synchronized blocks extensively</li>
 * <li>ThreadLocal variables that need mutation</li>
 * </ul>
 *
 * <h2>Migration Considerations:</h2>
 * <ul>
 * <li>Virtual threads work with existing blocking APIs</li>
 * <li>No code changes needed for most I/O operations</li>
 * <li>ThreadLocal should be replaced with ScopedValue</li>
 * <li>synchronized can cause "pinning" - prefer ReentrantLock</li>
 * </ul>
 *
 * <p>
 * For Scala developers: Virtual threads provide similar benefits to effect
 * systems (ZIO, Cats Effect) for I/O-bound work, but at the JVM level. For
 * CPU-bound work, you still want to limit parallelism like you would with a
 * compute pool in Scala.
 */
public class VirtualVsPlatformThreadsGuide {

    /**
     * Demonstrates the memory difference between thread types.
     *
     * <p>
     * Platform threads typically use ~1MB of stack space each. Virtual threads use
     * only a few KB initially.
     */
    public static void demonstrateMemoryDifference() {
        System.out.println("=== Memory Comparison ===");

        // Platform thread default stack size
        long platformStackSize = 1024 * 1024; // ~1MB default
        System.out.println("Platform thread stack: ~" + (platformStackSize / 1024) + " KB");

        // Virtual threads are much smaller
        System.out.println("Virtual thread initial: ~few KB (grows as needed)");
        System.out.println();

        // Calculate capacity difference
        long availableMemory = 4L * 1024 * 1024 * 1024; // 4GB
        long platformThreads = availableMemory / platformStackSize;
        long virtualThreads = 1_000_000; // Can easily have millions

        System.out.println("With 4GB memory:");
        System.out.println("  Platform threads possible: ~" + platformThreads);
        System.out.println("  Virtual threads possible: millions");
    }

    /**
     * Demonstrates when virtual threads excel - I/O-bound work.
     */
    public static void demonstrateIOBound() {
        System.out.println("=== I/O-Bound Work: Virtual Threads Excel ===");
        System.out.println();
        System.out.println("Scenario: Web server handling HTTP requests");
        System.out.println();

        System.out.println("Traditional (Platform Threads):");
        System.out.println("  - 200 threads in pool");
        System.out.println("  - Each thread: 1MB stack memory");
        System.out.println("  - Max concurrent requests: 200");
        System.out.println("  - Request #201 must wait in queue");
        System.out.println();

        System.out.println("Virtual Threads:");
        System.out.println("  - One virtual thread per request");
        System.out.println("  - Each thread: few KB");
        System.out.println("  - Max concurrent requests: millions");
        System.out.println("  - All requests processed immediately");
        System.out.println();

        System.out.println("Key insight: When a virtual thread blocks on I/O,");
        System.out.println("the carrier thread runs other virtual threads.");
    }

    /**
     * Demonstrates when platform threads are better - CPU-bound work.
     */
    public static void demonstrateCPUBound() {
        System.out.println("=== CPU-Bound Work: Platform Threads Preferred ===");
        System.out.println();
        System.out.println("Scenario: Computing cryptographic hashes");
        System.out.println();

        System.out.println("Why platform threads are better for CPU work:");
        System.out.println("  1. Virtual threads don't add concurrency for CPU work");
        System.out.println("     (limited by available CPU cores regardless)");
        System.out.println("  2. More threads than cores = more context switching");
        System.out.println("  3. Virtual thread scheduling adds slight overhead");
        System.out.println();

        int cores = Runtime.getRuntime().availableProcessors();
        System.out.println("Your system has " + cores + " CPU cores.");
        System.out.println("For CPU-bound work, use a thread pool of size ~" + cores);
    }

    /**
     * Demonstrates the "pinning" issue with synchronized blocks.
     */
    public static void demonstratePinning() {
        System.out.println("=== The Pinning Problem ===");
        System.out.println();
        System.out.println("'Pinning' occurs when a virtual thread cannot unmount from");
        System.out.println("its carrier thread, blocking the carrier.");
        System.out.println();

        System.out.println("Causes of pinning:");
        System.out.println("  1. synchronized blocks/methods");
        System.out.println("  2. Native/JNI code execution");
        System.out.println();

        System.out.println("Example - AVOID:");
        System.out.println("  synchronized (lock) {");
        System.out.println("      httpClient.send(request);  // Pins the carrier!");
        System.out.println("  }");
        System.out.println();

        System.out.println("Example - PREFERRED:");
        System.out.println("  ReentrantLock lock = new ReentrantLock();");
        System.out.println("  lock.lock();");
        System.out.println("  try {");
        System.out.println("      httpClient.send(request);  // Does not pin!");
        System.out.println("  } finally {");
        System.out.println("      lock.unlock();");
        System.out.println("  }");
    }

    /**
     * Migration guide from platform threads to virtual threads.
     */
    public static void printMigrationGuide() {
        System.out.println("=== Migration Guide ===");
        System.out.println();

        System.out.println("Step 1: Identify candidates");
        System.out.println("  - Look for ExecutorService/thread pools handling I/O");
        System.out.println("  - Web servers, HTTP clients, database access");
        System.out.println();

        System.out.println("Step 2: Simple replacement");
        System.out.println("  BEFORE: Executors.newFixedThreadPool(200)");
        System.out.println("  AFTER:  Executors.newVirtualThreadPerTaskExecutor()");
        System.out.println();

        System.out.println("Step 3: Replace ThreadLocal with ScopedValue");
        System.out.println("  BEFORE: ThreadLocal<User> currentUser = new ThreadLocal<>();");
        System.out.println("  AFTER:  ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();");
        System.out.println();

        System.out.println("Step 4: Replace synchronized with ReentrantLock");
        System.out.println("  BEFORE: synchronized (this) { ... }");
        System.out.println("  AFTER:  lock.lock(); try { ... } finally { lock.unlock(); }");
        System.out.println();

        System.out.println("Step 5: Consider Structured Concurrency");
        System.out.println("  Use StructuredTaskScope for related concurrent tasks");
        System.out.println("  Ensures cleanup and cancellation propagation");
    }

    /**
     * Quick reference decision tree.
     */
    public static void printDecisionTree() {
        System.out.println("=== Quick Decision Tree ===");
        System.out.println();
        System.out.println("Is the work I/O-bound (network, database, file)?");
        System.out.println("├── YES: Use Virtual Threads");
        System.out.println("│   └── Do you need concurrent sub-tasks?");
        System.out.println("│       ├── YES: Use StructuredTaskScope");
        System.out.println("│       └── NO:  Use newVirtualThreadPerTaskExecutor()");
        System.out.println("│");
        System.out.println("└── NO (CPU-bound):");
        System.out.println("    └── Use Platform Thread Pool");
        System.out.println("        └── Size: availableProcessors() or slightly more");
    }

    /**
     * Main method demonstrating all guides.
     */
    public static void main(String[] args) {
        demonstrateMemoryDifference();
        System.out.println();
        System.out.println("=".repeat(50));
        System.out.println();

        demonstrateIOBound();
        System.out.println("=".repeat(50));
        System.out.println();

        demonstrateCPUBound();
        System.out.println("=".repeat(50));
        System.out.println();

        demonstratePinning();
        System.out.println("=".repeat(50));
        System.out.println();

        printMigrationGuide();
        System.out.println("=".repeat(50));
        System.out.println();

        printDecisionTree();
    }
}
