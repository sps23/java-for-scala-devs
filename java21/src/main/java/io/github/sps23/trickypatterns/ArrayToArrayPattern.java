package io.github.sps23.trickypatterns;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates the confusing ArrayList.toArray() pattern.
 * 
 * The tricky part: Should you pass an empty array or a sized array?
 * Common on StackOverflow: list.toArray(new String[0]) vs list.toArray(new String[list.size()])
 */
public class ArrayToArrayPattern {

    public static void main(String[] args) {
        demonstrateOldWay();
        demonstrateOptimalWay();
        explainPerformance();
    }

    /**
     * OLD PATTERN (Pre-Java 6): Passing sized array
     * People still copy this from old StackOverflow answers
     */
    private static void demonstrateOldWay() {
        System.out.println("=== OLD WAY (Sized Array) ===");
        List<String> fruits = List.of("Apple", "Banana", "Cherry");
        
        // ❌ Looks logical but is SLOWER in modern JVMs
        String[] array = fruits.toArray(new String[fruits.size()]);
        
        System.out.println("Result: " + java.util.Arrays.toString(array));
        System.out.println("Why people use it: Seems to avoid reallocation");
        System.out.println("Reality: JVM optimizes the empty array case better!\n");
    }

    /**
     * OPTIMAL PATTERN (Since Java 6+): Passing empty array
     * This is actually FASTER due to JVM optimizations
     */
    private static void demonstrateOptimalWay() {
        System.out.println("=== OPTIMAL WAY (Empty Array) ===");
        List<String> fruits = List.of("Apple", "Banana", "Cherry");
        
        // ✅ Counter-intuitive but FASTER - JVM internally optimizes this
        String[] array = fruits.toArray(new String[0]);
        
        System.out.println("Result: " + java.util.Arrays.toString(array));
        System.out.println("Why it's better: JVM can optimize away array allocation");
        System.out.println("The empty array is just used for type inference!\n");
    }

    /**
     * Explains the performance characteristics
     */
    private static void explainPerformance() {
        System.out.println("=== PERFORMANCE EXPLANATION ===");
        System.out.println("With new String[0]:");
        System.out.println("  - JVM recognizes the pattern");
        System.out.println("  - Can allocate the right size immediately");
        System.out.println("  - May even use stack allocation");
        System.out.println();
        System.out.println("With new String[list.size()]:");
        System.out.println("  - Creates array on heap");
        System.out.println("  - JVM can't optimize as aggressively");
        System.out.println("  - Extra allocation and copy if list size changed");
        System.out.println();
        System.out.println("Benchmark results (typical):");
        System.out.println("  Empty array: ~15% faster");
        System.out.println("  Less garbage collection pressure");
    }

    /**
     * Modern alternative: Use collection directly or streams
     */
    public static void demonstrateModernAlternatives() {
        List<String> fruits = List.of("Apple", "Banana", "Cherry");
        
        // Modern approach 1: Use toArray() without argument (Java 11+)
        // Returns Object[] - need cast if specific type needed
        Object[] objectArray = fruits.toArray();
        
        // Modern approach 2: Use toArray(IntFunction) (Java 11+)
        String[] typedArray = fruits.toArray(String[]::new);
        
        // This is the cleanest and most performant way in Java 11+
        System.out.println("Modern Java 11+: fruits.toArray(String[]::new)");
    }
}
