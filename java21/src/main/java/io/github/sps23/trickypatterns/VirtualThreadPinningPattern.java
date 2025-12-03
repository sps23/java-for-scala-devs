package io.github.sps23.trickypatterns;

import java.time.Duration;
import java.util.concurrent.Executors;

/**
 * Demonstrates Virtual Thread pinning scenarios that harm performance.
 * 
 * The tricky part: Virtual threads (Java 21) are cheap and scalable, BUT
 * certain operations "pin" them to platform threads, losing benefits.
 * This is extremely non-intuitive and poorly documented.
 */
public class VirtualThreadPinningPattern {

    public static void main(String[] args) throws InterruptedException {
        demonstratePinningProblem();
        demonstrateSynchronizedBlock();
        demonstrateNativeMethodPinning();
        demonstrateCorrectAlternatives();
    }

    /**
     * Shows what pinning means and why it matters
     */
    private static void demonstratePinningProblem() {
        System.out.println("=== VIRTUAL THREAD PINNING PROBLEM ===");
        System.out.println();
        System.out.println("Virtual threads are designed to be cheap:");
        System.out.println("  - Can create millions");
        System.out.println("  - Automatically unmount when blocking");
        System.out.println("  - Remount when ready to continue");
        System.out.println();
        System.out.println("BUT pinning prevents unmounting:");
        System.out.println("  - Virtual thread stays on platform thread");
        System.out.println("  - Platform thread pool can be exhausted");
        System.out.println("  - Defeats the purpose of virtual threads!");
        System.out.println();
    }

    /**
     * Synchronized blocks cause pinning (Java 21)
     */
    private static void demonstrateSynchronizedBlock() throws InterruptedException {
        System.out.println("=== PINNING CAUSE #1: synchronized ===");
        System.out.println();
        
        Object lock = new Object();
        
        // ❌ This pins the virtual thread!
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                synchronized (lock) {  // ❌ PINNING!
                    System.out.println("Inside synchronized block");
                    try {
                        Thread.sleep(1000);  // Virtual thread is pinned!
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                return null;
            });
        }
        
        System.out.println();
        System.out.println("Why it pins:");
        System.out.println("  - synchronized uses monitor (JVM intrinsic)");
        System.out.println("  - JVM cannot transfer monitor to another thread");
        System.out.println("  - Virtual thread stays pinned for entire block");
        System.out.println();
        System.out.println("Impact:");
        System.out.println("  With 1000s of pinned threads:");
        System.out.println("    - Platform thread pool exhausted");
        System.out.println("    - New virtual threads cannot run");
        System.out.println("    - Throughput collapses");
        System.out.println();
    }

    /**
     * Native methods also cause pinning
     */
    private static void demonstrateNativeMethodPinning() {
        System.out.println("=== PINNING CAUSE #2: Native Methods ===");
        System.out.println();
        
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                // Many I/O operations use native methods
                // File I/O, network I/O, etc.
                System.out.println("Some native methods pin");
                return null;
            });
        }
        
        System.out.println();
        System.out.println("Native methods that pin:");
        System.out.println("  - FileInputStream/FileOutputStream (some operations)");
        System.out.println("  - Object.wait() in synchronized");
        System.out.println("  - Various JNI calls");
        System.out.println();
        System.out.println("Native methods that DON'T pin:");
        System.out.println("  - Socket operations (special handling)");
        System.out.println("  - Most NIO operations");
        System.out.println("  - Thread.sleep()");
        System.out.println();
    }

    /**
     * Shows correct alternatives to avoid pinning
     */
    private static void demonstrateCorrectAlternatives() throws InterruptedException {
        System.out.println("=== CORRECT ALTERNATIVES ===");
        System.out.println();
        
        // ✅ Alternative 1: Use ReentrantLock instead of synchronized
        System.out.println("1. Replace synchronized with ReentrantLock:");
        demonstrateReentrantLock();
        System.out.println();
        
        // ✅ Alternative 2: Keep synchronized blocks short
        System.out.println("2. Keep synchronized blocks SHORT:");
        System.out.println("   Move blocking I/O OUTSIDE synchronized");
        demonstrateShortSynchronized();
        System.out.println();
        
        // ✅ Alternative 3: Use Semaphore for concurrency control
        System.out.println("3. Use Semaphore for counting:");
        System.out.println("   Better than synchronized for rate limiting");
        System.out.println();
    }

    private static void demonstrateReentrantLock() throws InterruptedException {
        var lock = new java.util.concurrent.locks.ReentrantLock();
        
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                lock.lock();  // ✅ No pinning with ReentrantLock!
                try {
                    System.out.println("   Inside ReentrantLock");
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    lock.unlock();
                }
                return null;
            });
        }
        
        System.out.println("   Virtual thread can unmount while waiting");
    }

    private static void demonstrateShortSynchronized() throws InterruptedException {
        Object lock = new Object();
        
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                // ❌ Bad: I/O inside synchronized
                // synchronized (lock) {
                //     performExpensiveIO();  // Pins for entire duration
                // }
                
                // ✅ Good: I/O outside synchronized
                String data = performExpensiveIO();
                synchronized (lock) {  // Only pin for memory update
                    updateSharedState(data);
                }
                return null;
            });
        }
        
        System.out.println("   Pinning duration minimized");
    }

    private static String performExpensiveIO() {
        // Simulate I/O
        return "data";
    }

    private static void updateSharedState(String data) {
        // Quick memory update
    }

    /**
     * Practical example: Web server
     */
    public static void demonstrateWebServerScenario() {
        System.out.println("=== REAL WORLD: Web Server ===");
        System.out.println();
        
        System.out.println("❌ BAD: Each request in synchronized block");
        System.out.println("```java");
        System.out.println("void handleRequest(Request req) {");
        System.out.println("    synchronized (this) {  // PINS!");
        System.out.println("        String data = database.query(...);  // Blocking I/O");
        System.out.println("        return process(data);");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("```");
        System.out.println("Impact: Can only handle N concurrent requests (N = platform threads)");
        System.out.println();
        
        System.out.println("✅ GOOD: Minimal synchronization");
        System.out.println("```java");
        System.out.println("void handleRequest(Request req) {");
        System.out.println("    String data = database.query(...);  // No sync, can unmount");
        System.out.println("    String result = process(data);");
        System.out.println("    synchronized (this) {  // Only sync for update");
        System.out.println("        cache.put(key, result);");
        System.out.println("    }");
        System.out.println("}");
        System.out.println("```");
        System.out.println("Impact: Can handle millions of concurrent requests");
        System.out.println();
    }

    /**
     * How to detect pinning
     */
    public static void demonstrateDetectingPinning() {
        System.out.println("=== DETECTING PINNING ===");
        System.out.println();
        System.out.println("1. JVM Flag:");
        System.out.println("   -Djdk.tracePinnedThreads=full");
        System.out.println("   Prints stack trace when pinning detected");
        System.out.println();
        System.out.println("2. JFR Event:");
        System.out.println("   jdk.VirtualThreadPinned");
        System.out.println("   Use JDK Flight Recorder to analyze");
        System.out.println();
        System.out.println("3. Look for patterns:");
        System.out.println("   - synchronized blocks with I/O");
        System.out.println("   - synchronized methods");
        System.out.println("   - Object.wait() usage");
        System.out.println();
    }

    /**
     * Summary of pinning rules
     */
    public static void summarizePinningRules() {
        System.out.println("=== PINNING SUMMARY ===");
        System.out.println();
        System.out.println("CAUSES PINNING:");
        System.out.println("  ❌ synchronized blocks/methods");
        System.out.println("  ❌ Object.wait()");
        System.out.println("  ❌ Some native methods");
        System.out.println();
        System.out.println("SAFE (No pinning):");
        System.out.println("  ✅ ReentrantLock");
        System.out.println("  ✅ Semaphore");
        System.out.println("  ✅ Most java.util.concurrent");
        System.out.println("  ✅ Thread.sleep()");
        System.out.println("  ✅ Socket I/O (special handling)");
        System.out.println();
        System.out.println("BEST PRACTICES:");
        System.out.println("  1. Prefer ReentrantLock over synchronized");
        System.out.println("  2. Keep synchronized blocks minimal");
        System.out.println("  3. Move I/O outside synchronization");
        System.out.println("  4. Use -Djdk.tracePinnedThreads to detect");
        System.out.println("  5. Test with realistic concurrency levels");
    }
}
