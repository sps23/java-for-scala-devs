package io.github.sps23.trickypatterns;

/**
 * Demonstrates the confusing Integer caching behavior.
 * 
 * The tricky part: Integer.valueOf() caches values from -128 to 127,
 * leading to unexpected == behavior that seems to work, then breaks!
 */
public class IntegerCachingPattern {

    public static void main(String[] args) {
        demonstrateCachingBehavior();
        demonstrateCommonMistake();
        demonstrateCorrectWay();
        explainCachingRules();
    }

    /**
     * Shows the caching behavior that confuses developers
     */
    private static void demonstrateCachingBehavior() {
        System.out.println("=== INTEGER CACHING BEHAVIOR ===");
        
        // ✅ These work as expected due to caching
        Integer a = 100;
        Integer b = 100;
        System.out.println("a == b (100): " + (a == b));  // true - cached!
        System.out.println("a.equals(b): " + a.equals(b));  // true
        System.out.println();
        
        // ❌ These FAIL even though they look identical
        Integer c = 200;
        Integer d = 200;
        System.out.println("c == d (200): " + (c == d));  // false - NOT cached!
        System.out.println("c.equals(d): " + c.equals(d));  // true
        System.out.println();
        
        System.out.println("Why? JVM caches -128 to 127 by default");
        System.out.println("The == comparison checks object identity, not value!\n");
    }

    /**
     * Common mistake that leads to subtle bugs
     */
    private static void demonstrateCommonMistake() {
        System.out.println("=== COMMON MISTAKE: Using == ===");
        
        // This works in testing...
        Integer userId1 = getUserId(1);
        Integer userId2 = getUserId(1);
        if (userId1 == userId2) {  // ❌ Dangerous!
            System.out.println("Test users match (low IDs work)");
        }
        
        // ...but fails in production with larger IDs!
        Integer realId1 = getUserId(1000);
        Integer realId2 = getUserId(1000);
        if (realId1 == realId2) {  // This is false!
            System.out.println("Production users match");
        } else {
            System.out.println("Production users DON'T match (but they should!)");
        }
        System.out.println();
    }

    /**
     * The correct way to compare Integer objects
     */
    private static void demonstrateCorrectWay() {
        System.out.println("=== CORRECT WAY: Use .equals() ===");
        
        Integer a = 1000;
        Integer b = 1000;
        
        // ✅ Always use equals() for object comparison
        if (a.equals(b)) {
            System.out.println("Values are equal (correct!)");
        }
        
        // ✅ Or use intValue() to get primitive
        if (a.intValue() == b.intValue()) {
            System.out.println("Primitive comparison also works");
        }
        
        // ✅ Or auto-unbox to primitive (Java 5+)
        int aPrim = a;
        int bPrim = b;
        if (aPrim == bPrim) {
            System.out.println("Auto-unboxing works too");
        }
        System.out.println();
    }

    /**
     * Explains the caching rules for all wrapper types
     */
    private static void explainCachingRules() {
        System.out.println("=== WRAPPER CLASS CACHING RULES ===");
        System.out.println();
        System.out.println("Byte: ALL values (-128 to 127) - fully cached");
        System.out.println("Short: -128 to 127 - cached");
        System.out.println("Integer: -128 to 127 - cached (configurable with -XX:AutoBoxCacheMax)");
        System.out.println("Long: -128 to 127 - cached");
        System.out.println("Character: 0 to 127 - cached");
        System.out.println("Float: NEVER cached");
        System.out.println("Double: NEVER cached");
        System.out.println("Boolean: TRUE and FALSE - always cached (only 2 objects)");
        System.out.println();
        System.out.println("Rule of thumb: ALWAYS use .equals() for wrapper types!");
    }

    /**
     * Demonstrates autoboxing pitfalls
     */
    public static void demonstrateAutoboxingPitfalls() {
        System.out.println("=== AUTOBOXING PITFALLS ===");
        
        // Pitfall 1: Null pointer exception
        Integer value = null;
        try {
            int primitive = value;  // NullPointerException on auto-unbox!
        } catch (NullPointerException e) {
            System.out.println("NPE on auto-unboxing null Integer");
        }
        
        // Pitfall 2: Performance in loops
        long start = System.nanoTime();
        Integer sum = 0;  // ❌ Creates object for each addition!
        for (int i = 0; i < 10000; i++) {
            sum += i;  // Unbox, add, box - inefficient!
        }
        long boxedTime = System.nanoTime() - start;
        
        start = System.nanoTime();
        int primSum = 0;  // ✅ Primitive - efficient
        for (int i = 0; i < 10000; i++) {
            primSum += i;
        }
        long primTime = System.nanoTime() - start;
        
        System.out.println("Boxed loop: " + boxedTime / 1000 + "μs");
        System.out.println("Primitive loop: " + primTime / 1000 + "μs");
        System.out.println("Speedup: " + (boxedTime / primTime) + "x");
    }

    private static Integer getUserId(int id) {
        return Integer.valueOf(id);
    }
}
