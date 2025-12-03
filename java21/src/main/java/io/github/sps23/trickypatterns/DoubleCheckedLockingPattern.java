package io.github.sps23.trickypatterns;

/**
 * Demonstrates the double-checked locking pattern and its evolution.
 * 
 * The tricky part: The "obvious" implementation is broken due to memory model.
 * Pre-Java 5: Completely broken
 * Java 5+: Requires volatile keyword
 * Modern Java: Use lazy initialization holders instead
 */
public class DoubleCheckedLockingPattern {

    // Shared instance for singleton pattern
    private static DoubleCheckedLockingPattern instance;

    private DoubleCheckedLockingPattern() {
        // Private constructor
    }

    public static void main(String[] args) {
        demonstrateBrokenPattern();
        demonstrateCorrectPattern();
        demonstrateModernWay();
        explainMemoryModel();
    }

    /**
     * BROKEN PATTERN (Pre-Java 5 and even after without volatile)
     * This looks correct but has subtle race conditions
     */
    @SuppressWarnings("all")
    private static void demonstrateBrokenPattern() {
        System.out.println("=== BROKEN PATTERN ===");
        System.out.println();
        System.out.println("private static BrokenSingleton instance;");
        System.out.println();
        System.out.println("public static BrokenSingleton getInstance() {");
        System.out.println("    if (instance == null) {  // Check 1: No lock");
        System.out.println("        synchronized (BrokenSingleton.class) {");
        System.out.println("            if (instance == null) {  // Check 2: With lock");
        System.out.println("                instance = new BrokenSingleton();  // ❌ BROKEN!");
        System.out.println("            }");
        System.out.println("        }");
        System.out.println("    }");
        System.out.println("    return instance;");
        System.out.println("}");
        System.out.println();
        System.out.println("Why it's broken:");
        System.out.println("  1. 'new' is not atomic - has 3 steps:");
        System.out.println("     a) Allocate memory");
        System.out.println("     b) Initialize object");
        System.out.println("     c) Assign reference");
        System.out.println("  2. CPU can reorder b and c!");
        System.out.println("  3. Thread 1 might see partially constructed object");
        System.out.println("  4. Thread 2 sees non-null but uninitialized object");
        System.out.println();
    }

    /**
     * CORRECT PATTERN (Java 5+)
     * Requires volatile keyword to prevent reordering
     */
    private static void demonstrateCorrectPattern() {
        System.out.println("=== CORRECT PATTERN (Java 5+) ===");
        System.out.println();
        System.out.println("private static volatile CorrectSingleton instance;  // volatile!");
        System.out.println();
        System.out.println("public static CorrectSingleton getInstance() {");
        System.out.println("    if (instance == null) {");
        System.out.println("        synchronized (CorrectSingleton.class) {");
        System.out.println("            if (instance == null) {");
        System.out.println("                instance = new CorrectSingleton();  // ✅ Safe with volatile");
        System.out.println("            }");
        System.out.println("        }");
        System.out.println("    }");
        System.out.println("    return instance;");
        System.out.println("}");
        System.out.println();
        System.out.println("How volatile fixes it:");
        System.out.println("  1. Prevents instruction reordering");
        System.out.println("  2. Establishes happens-before relationship");
        System.out.println("  3. Ensures full construction before assignment visible");
        System.out.println();
        System.out.println("Performance:");
        System.out.println("  - First call: Synchronized (slow)");
        System.out.println("  - Subsequent calls: Just volatile read (fast)");
        System.out.println();
    }

    /**
     * MODERN PATTERN
     * Use initialization-on-demand holder idiom
     */
    private static void demonstrateModernWay() {
        System.out.println("=== MODERN WAY: Initialization-on-Demand Holder ===");
        System.out.println();
        System.out.println("public class ModernSingleton {");
        System.out.println("    private ModernSingleton() {}");
        System.out.println();
        System.out.println("    private static class Holder {");
        System.out.println("        static final ModernSingleton INSTANCE = new ModernSingleton();");
        System.out.println("    }");
        System.out.println();
        System.out.println("    public static ModernSingleton getInstance() {");
        System.out.println("        return Holder.INSTANCE;  // ✅ Thread-safe, lazy, fast!");
        System.out.println("    }");
        System.out.println("}");
        System.out.println();
        System.out.println("Why it's better:");
        System.out.println("  1. JVM guarantees thread-safe class initialization");
        System.out.println("  2. Lazy - Holder class loaded only when getInstance() called");
        System.out.println("  3. No synchronization overhead");
        System.out.println("  4. No volatile needed");
        System.out.println("  5. Simpler code");
        System.out.println();
        System.out.println("When to use:");
        System.out.println("  ✅ Singleton pattern");
        System.out.println("  ✅ Any lazy-initialized expensive resource");
        System.out.println("  ❌ When you need parameters for construction");
        System.out.println();
    }

    /**
     * Explains the Java Memory Model aspects
     */
    private static void explainMemoryModel() {
        System.out.println("=== JAVA MEMORY MODEL EXPLANATION ===");
        System.out.println();
        System.out.println("Key concepts:");
        System.out.println();
        System.out.println("1. Happens-Before Relationship:");
        System.out.println("   - volatile write happens-before volatile read");
        System.out.println("   - Synchronized block release happens-before acquire");
        System.out.println("   - Thread start happens-before thread actions");
        System.out.println();
        System.out.println("2. Why volatile is needed:");
        System.out.println("   - Without it: Compiler/CPU can reorder");
        System.out.println("   - With it: All writes before assignment are visible");
        System.out.println();
        System.out.println("3. Performance impact:");
        System.out.println("   - volatile read: ~5ns");
        System.out.println("   - volatile write: ~5ns");
        System.out.println("   - synchronized: ~50ns");
        System.out.println("   - Double-checked locking: Amortizes sync cost");
        System.out.println();
        System.out.println("Historical note:");
        System.out.println("  - Pre-Java 5: volatile was weaker, DCL was broken");
        System.out.println("  - Java 5 (JSR-133): Fixed volatile, DCL now safe");
        System.out.println("  - Modern Java: Use holder pattern instead");
    }

    /**
     * Practical example: Database connection pool
     */
    static class ConnectionPool {
        private static volatile ConnectionPool instance;
        private int maxConnections = 10;

        private ConnectionPool() {
            // Expensive initialization
            System.out.println("Initializing connection pool...");
            try {
                Thread.sleep(100);  // Simulate slow init
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Double-checked locking (correct with volatile)
        public static ConnectionPool getInstance() {
            if (instance == null) {  // Fast path: no lock
                synchronized (ConnectionPool.class) {  // Slow path: lock
                    if (instance == null) {
                        instance = new ConnectionPool();
                    }
                }
            }
            return instance;
        }
    }

    /**
     * Modern alternative using holder
     */
    static class ModernConnectionPool {
        private int maxConnections = 10;

        private ModernConnectionPool() {
            System.out.println("Initializing modern connection pool...");
        }

        private static class Holder {
            static final ModernConnectionPool INSTANCE = new ModernConnectionPool();
        }

        public static ModernConnectionPool getInstance() {
            return Holder.INSTANCE;  // Simple and correct
        }
    }
}
