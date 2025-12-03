package io.github.sps23.trickypatterns;

/**
 * Demonstrates Record constructor subtleties that confuse developers.
 * 
 * The tricky part: Records have canonical, compact, and custom constructors
 * with non-obvious interaction rules and validation patterns.
 */
public class RecordConstructorPattern {

    // Basic record - simple and clear
    record Point(int x, int y) {}

    // Compact constructor - validation without parameter list
    record ValidatedPoint(int x, int y) {
        // ✅ Compact constructor - validates and normalizes
        public ValidatedPoint {
            if (x < 0 || y < 0) {
                throw new IllegalArgumentException("Coordinates must be positive");
            }
            // No need to assign fields - happens automatically!
        }
    }

    // Common mistake: Trying to modify in compact constructor
    record NormalizedPoint(int x, int y) {
        public NormalizedPoint {
            // ❌ This doesn't work - can't reassign parameters!
            // x = Math.max(0, x);  // Compilation error!
            
            // ✅ Can only validate, not transform
            if (x < 0) {
                throw new IllegalArgumentException("x must be positive");
            }
        }
    }

    // Correct way to normalize: Use canonical constructor
    record CorrectNormalizedPoint(int x, int y) {
        // ✅ Canonical constructor - can transform
        public CorrectNormalizedPoint(int x, int y) {
            this.x = Math.max(0, x);  // Normalize negative to 0
            this.y = Math.max(0, y);
        }
    }

    // Multiple constructors confusion
    record ComplexPoint(int x, int y) {
        // Compact constructor
        public ComplexPoint {
            System.out.println("Compact called");
        }
        
        // Custom constructor - must delegate to canonical
        public ComplexPoint(int value) {
            this(value, value);  // ✅ Must call canonical constructor
            // ❌ Cannot have both compact and this custom pattern!
        }
    }

    public static void main(String[] args) {
        demonstrateCompactConstructor();
        demonstrateCanonicalConstructor();
        demonstrateConstructorDelegation();
        demonstrateCommonMistakes();
    }

    /**
     * Shows compact constructor behavior
     */
    private static void demonstrateCompactConstructor() {
        System.out.println("=== COMPACT CONSTRUCTOR ===");
        System.out.println();
        
        // Compact constructor syntax is confusing
        System.out.println("Syntax comparison:");
        System.out.println("❌ Regular constructor: public Point(int x, int y) { this.x = x; this.y = y; }");
        System.out.println("✅ Compact constructor: public Point { /* validation */ }");
        System.out.println();
        
        try {
            ValidatedPoint p = new ValidatedPoint(10, 20);
            System.out.println("Valid point: " + p);
        } catch (IllegalArgumentException e) {
            System.out.println("Validation failed: " + e.getMessage());
        }
        
        try {
            ValidatedPoint invalid = new ValidatedPoint(-5, 10);
        } catch (IllegalArgumentException e) {
            System.out.println("Caught invalid point: " + e.getMessage());
        }
        
        System.out.println();
        System.out.println("Key points:");
        System.out.println("  - No parameter list needed");
        System.out.println("  - Runs BEFORE field assignment");
        System.out.println("  - Cannot modify parameters (they're final!)");
        System.out.println("  - Fields assigned automatically after compact constructor");
        System.out.println();
    }

    /**
     * Shows canonical constructor for transformation
     */
    private static void demonstrateCanonicalConstructor() {
        System.out.println("=== CANONICAL CONSTRUCTOR ===");
        System.out.println();
        
        CorrectNormalizedPoint p1 = new CorrectNormalizedPoint(-10, 20);
        System.out.println("Normalized point: " + p1);  // x becomes 0
        System.out.println("  Input: (-10, 20)");
        System.out.println("  Output: " + p1);
        System.out.println();
        
        System.out.println("When to use canonical constructor:");
        System.out.println("  ✅ Need to transform/normalize input");
        System.out.println("  ✅ Need to compute derived values");
        System.out.println("  ✅ Need to assign fields explicitly");
        System.out.println();
        System.out.println("When to use compact constructor:");
        System.out.println("  ✅ Only need validation");
        System.out.println("  ✅ Fields should be assigned as-is");
        System.out.println("  ✅ Want less boilerplate");
        System.out.println();
    }

    /**
     * Shows constructor delegation rules
     */
    private static void demonstrateConstructorDelegation() {
        System.out.println("=== CONSTRUCTOR DELEGATION ===");
        System.out.println();
        
        // Record with multiple constructors
        record User(String name, int age, String email) {
            // Compact constructor for validation
            public User {
                if (age < 0) throw new IllegalArgumentException("Age cannot be negative");
            }
            
            // Convenience constructor - MUST delegate
            public User(String name, int age) {
                this(name, age, name.toLowerCase() + "@example.com");
            }
            
            // Another convenience constructor
            public User(String name) {
                this(name, 0);  // Delegates to previous constructor
            }
        }
        
        User u1 = new User("Alice", 30, "alice@email.com");
        User u2 = new User("Bob", 25);  // Uses computed email
        User u3 = new User("Charlie");   // Uses defaults
        
        System.out.println("Full constructor: " + u1);
        System.out.println("Computed email: " + u2);
        System.out.println("All defaults: " + u3);
        System.out.println();
        
        System.out.println("Delegation rules:");
        System.out.println("  - All constructors must eventually call canonical constructor");
        System.out.println("  - Compact constructor IS the canonical constructor");
        System.out.println("  - Custom constructors use this(...) to delegate");
        System.out.println();
    }

    /**
     * Shows common mistakes
     */
    private static void demonstrateCommonMistakes() {
        System.out.println("=== COMMON MISTAKES ===");
        System.out.println();
        
        System.out.println("Mistake 1: Trying to modify parameters in compact constructor");
        System.out.println("❌ public Point { x = Math.max(0, x); }  // Won't compile!");
        System.out.println("   Parameters are implicitly final");
        System.out.println();
        
        System.out.println("Mistake 2: Forgetting field assignments in canonical constructor");
        System.out.println("❌ public Point(int x, int y) { /* forgot this.x = x */ }");
        System.out.println("   Fields will be null/0, not the parameters!");
        System.out.println();
        
        System.out.println("Mistake 3: Mixing compact and canonical constructors");
        System.out.println("❌ Cannot have both compact constructor and canonical constructor");
        System.out.println("   Choose one or the other");
        System.out.println();
        
        System.out.println("Mistake 4: Not delegating in custom constructors");
        System.out.println("❌ public Point(int value) { this.x = value; this.y = value; }");
        System.out.println("   Custom constructors MUST delegate to canonical");
        System.out.println();
    }

    /**
     * Advanced pattern: Builder-like record
     */
    public static void demonstrateBuilderPattern() {
        record Config(
            String host,
            int port,
            boolean ssl,
            int timeout
        ) {
            // Canonical constructor with defaults
            public Config(String host, int port, boolean ssl, int timeout) {
                this.host = host != null ? host : "localhost";
                this.port = port > 0 ? port : 8080;
                this.ssl = ssl;
                this.timeout = timeout > 0 ? timeout : 5000;
            }
            
            // Convenience constructors for common patterns
            public Config(String host, int port) {
                this(host, port, false, 5000);
            }
            
            public Config(String host) {
                this(host, 8080);
            }
            
            public Config() {
                this("localhost");
            }
        }
        
        Config c1 = new Config();
        Config c2 = new Config("example.com");
        Config c3 = new Config("secure.com", 443, true, 10000);
        
        System.out.println("Default config: " + c1);
        System.out.println("Host only: " + c2);
        System.out.println("Full config: " + c3);
    }

    /**
     * Shows null handling patterns
     */
    public static void demonstrateNullHandling() {
        System.out.println("=== NULL HANDLING ===");
        
        record Person(String name, Integer age) {
            // Compact constructor with null checks
            public Person {
                if (name == null || name.isBlank()) {
                    throw new IllegalArgumentException("Name required");
                }
                // age can be null (using Integer not int)
            }
        }
        
        try {
            Person p1 = new Person(null, 25);
        } catch (IllegalArgumentException e) {
            System.out.println("Null name rejected: " + e.getMessage());
        }
        
        Person p2 = new Person("Alice", null);  // age can be null
        System.out.println("Person with null age: " + p2);
        
        System.out.println();
        System.out.println("Best practices:");
        System.out.println("  - Use compact constructor for null checks");
        System.out.println("  - Be explicit about nullable fields (use wrapper types)");
        System.out.println("  - Provide clear error messages");
    }
}
