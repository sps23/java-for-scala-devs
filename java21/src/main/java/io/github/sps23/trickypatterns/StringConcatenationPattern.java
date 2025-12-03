package io.github.sps23.trickypatterns;

/**
 * Demonstrates the string concatenation performance trap.
 *
 * The tricky part: String concatenation LOOKS simple but has hidden performance
 * costs. Since Java 9, the situation changed with invokedynamic, making some
 * patterns faster.
 */
public class StringConcatenationPattern {

    public static void main(String[] args) {
        demonstrateNaiveLoop();
        demonstrateStringBuilder();
        demonstrateModernOptimization();
        explainWhenItMatters();
    }

    /**
     * NAIVE PATTERN: String concatenation in loop This creates N intermediate
     * String objects!
     */
    private static void demonstrateNaiveLoop() {
        System.out.println("=== NAIVE CONCATENATION IN LOOP ===");

        // ❌ Creates tons of intermediate String objects
        String result = "";
        for (int i = 0; i < 5; i++) {
            result += "Item-" + i + ", "; // Each += creates new String!
        }

        System.out.println("Result: " + result);
        System.out.println("Hidden cost: Creates ~15 intermediate String objects");
        System.out.println("Time complexity: O(n²) due to copying\n");
    }

    /**
     * CLASSIC SOLUTION: Use StringBuilder This is the traditional "correct" answer
     */
    private static void demonstrateStringBuilder() {
        System.out.println("=== STRINGBUILDER PATTERN ===");

        // ✅ Efficient: Single mutable buffer
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append("Item-").append(i).append(", ");
        }
        String result = sb.toString();

        System.out.println("Result: " + result);
        System.out.println("Benefit: O(n) complexity, minimal allocations");
        System.out.println("When to use: Loops, complex string building\n");
    }

    /**
     * MODERN JAVA (9+): Compiler optimizations Since Java 9, simple concatenations
     * use invokedynamic
     */
    private static void demonstrateModernOptimization() {
        System.out.println("=== MODERN JAVA 9+ OPTIMIZATION ===");

        int count = 42;
        String name = "Alice";

        // This LOOKS bad but Java 9+ compiles it efficiently
        String result = "User: " + name + ", Count: " + count;

        System.out.println("Result: " + result);
        System.out.println("Behind the scenes: Uses invokedynamic");
        System.out.println("Compiler may use StringBuilder or even StringConcatFactory");
        System.out.println("Performance: Nearly as fast as manual StringBuilder\n");
    }

    /**
     * Explains when the optimization matters
     */
    private static void explainWhenItMatters() {
        System.out.println("=== WHEN DOES IT MATTER? ===");
        System.out.println();
        System.out.println("DON'T worry about:");
        System.out.println("  String s = a + b + c;  // Single statement - optimized");
        System.out.println("  String s = 'Hello ' + name;  // Java 9+ handles this");
        System.out.println();
        System.out.println("DO use StringBuilder for:");
        System.out.println("  Loops with string building");
        System.out.println("  Conditional concatenation");
        System.out.println("  Building strings across multiple methods");
        System.out.println();
        System.out.println("Performance difference:");
        System.out.println("  Small strings (< 10 items): Negligible");
        System.out.println("  Medium strings (10-100): 2-3x faster with StringBuilder");
        System.out.println("  Large strings (1000+): 10-100x faster with StringBuilder");
    }

    /**
     * Benchmark comparison
     */
    public static void benchmark() {
        int iterations = 1000;

        // Naive approach
        long start = System.nanoTime();
        String s1 = "";
        for (int i = 0; i < iterations; i++) {
            s1 += "x";
        }
        long naiveTime = System.nanoTime() - start;

        // StringBuilder approach
        start = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < iterations; i++) {
            sb.append("x");
        }
        String s2 = sb.toString();
        long builderTime = System.nanoTime() - start;

        System.out.println("Naive: " + naiveTime / 1_000_000 + "ms");
        System.out.println("StringBuilder: " + builderTime / 1_000_000 + "ms");
        System.out.println("Speedup: " + (naiveTime / builderTime) + "x");
    }
}
