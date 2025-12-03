package io.github.sps23.trickypatterns;

/**
 * Demonstrates switch expression vs statement subtle differences.
 * 
 * The tricky part: Switch expressions (Java 14+) look like statements but have
 * different fall-through behavior, exhaustiveness requirements, and return semantics.
 */
public class SwitchExpressionPattern {

    enum Day { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY }

    public static void main(String[] args) {
        demonstrateFallThroughConfusion();
        demonstrateExhaustivenessRules();
        demonstrateReturnBehavior();
        demonstrateArrowVsColon();
    }

    /**
     * Shows the confusing fall-through behavior difference
     */
    private static void demonstrateFallThroughConfusion() {
        System.out.println("=== FALL-THROUGH BEHAVIOR ===");
        System.out.println();
        
        Day day = Day.TUESDAY;
        
        // OLD STYLE: Switch statement with fall-through
        System.out.println("Traditional switch STATEMENT:");
        switch (day) {
            case MONDAY:
            case TUESDAY:
            case WEDNESDAY:
                System.out.println("  Early week (fall-through works)");
                break;  // ❌ Forgot break = bug!
            case THURSDAY:
            case FRIDAY:
                System.out.println("  Late week");
                break;
            default:
                System.out.println("  Weekend");
        }
        System.out.println();
        
        // NEW STYLE: Switch expression with arrow
        System.out.println("Modern switch EXPRESSION (arrow):");
        String result = switch (day) {
            case MONDAY, TUESDAY, WEDNESDAY -> "Early week";  // ✅ No fall-through!
            case THURSDAY, FRIDAY -> "Late week";
            case SATURDAY, SUNDAY -> "Weekend";
        };
        System.out.println("  " + result);
        System.out.println();
        
        System.out.println("Key difference:");
        System.out.println("  Statement: Fall-through by default (needs break)");
        System.out.println("  Expression with ->: NO fall-through (safer!)");
        System.out.println();
    }

    /**
     * Shows exhaustiveness requirements that confuse developers
     */
    private static void demonstrateExhaustivenessRules() {
        System.out.println("=== EXHAUSTIVENESS RULES ===");
        System.out.println();
        
        Day day = Day.FRIDAY;
        
        // Switch STATEMENT: Not required to be exhaustive
        System.out.println("Switch statement (no exhaustiveness required):");
        switch (day) {
            case MONDAY -> System.out.println("  Monday");
            case FRIDAY -> System.out.println("  Friday");
            // Missing cases OK for statement
        }
        System.out.println();
        
        // Switch EXPRESSION: MUST be exhaustive
        System.out.println("Switch expression (MUST be exhaustive):");
        String type = switch (day) {
            case MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY -> "Weekday";
            case SATURDAY, SUNDAY -> "Weekend";
            // ✅ All cases covered - compiles
        };
        System.out.println("  " + type);
        System.out.println();
        
        // This would NOT compile (missing case):
        // String broken = switch (day) {
        //     case MONDAY -> "Start";
        //     case FRIDAY -> "End";
        //     // ❌ Compilation error: not exhaustive!
        // };
        
        System.out.println("Why this matters:");
        System.out.println("  Expressions must return a value for ALL cases");
        System.out.println("  Compiler enforces safety");
        System.out.println("  Prevents runtime 'no matching case' errors");
        System.out.println();
    }

    /**
     * Shows return/yield behavior confusion
     */
    private static void demonstrateReturnBehavior() {
        System.out.println("=== RETURN vs YIELD ===");
        System.out.println();
        
        Day day = Day.WEDNESDAY;
        
        // Arrow syntax: implicit return
        String result1 = switch (day) {
            case MONDAY -> "Start";  // Implicit return
            case FRIDAY -> "End";
            default -> "Middle";
        };
        System.out.println("Arrow syntax result: " + result1);
        System.out.println();
        
        // Block with yield (for complex logic)
        String result2 = switch (day) {
            case MONDAY -> {
                System.out.println("  Complex Monday logic");
                yield "Start of week";  // ✅ Use yield, not return!
            }
            case FRIDAY -> {
                System.out.println("  Complex Friday logic");
                yield "End of week";
            }
            default -> {
                System.out.println("  Middle logic");
                yield "Midweek";
            }
        };
        System.out.println("Block syntax result: " + result2);
        System.out.println();
        
        System.out.println("Key points:");
        System.out.println("  - Arrow without block: implicit return");
        System.out.println("  - Arrow with block: must use 'yield'");
        System.out.println("  - Cannot use 'return' in switch expression");
        System.out.println("  - 'return' would exit the enclosing method!");
        System.out.println();
    }

    /**
     * Shows mixing arrow and colon syntax (confusing!)
     */
    private static void demonstrateArrowVsColon() {
        System.out.println("=== ARROW (->) vs COLON (:) SYNTAX ===");
        System.out.println();
        
        Day day = Day.SATURDAY;
        
        // Can use colon syntax in expression (but why?)
        String result = switch (day) {
            case MONDAY:
            case FRIDAY:
                yield "Important day";  // Must use yield with colon
            case SATURDAY:
            case SUNDAY:
                yield "Weekend";
            default:
                yield "Regular day";
        };
        System.out.println("Colon syntax result: " + result);
        System.out.println();
        
        // Cleaner with arrow
        String better = switch (day) {
            case MONDAY, FRIDAY -> "Important day";
            case SATURDAY, SUNDAY -> "Weekend";
            default -> "Regular day";
        };
        System.out.println("Arrow syntax result: " + better);
        System.out.println();
        
        System.out.println("Recommendations:");
        System.out.println("  ✅ Use arrow (->) for expressions (cleaner)");
        System.out.println("  ✅ Use colon (:) only for legacy statements");
        System.out.println("  ❌ Don't mix arrow and colon in same switch");
        System.out.println();
    }

    /**
     * Pattern matching in switch (Java 21)
     */
    public static void demonstratePatternMatching() {
        System.out.println("=== PATTERN MATCHING (Java 21) ===");
        
        Object obj = "Hello";
        
        // Modern switch with patterns
        String result = switch (obj) {
            case String s when s.length() > 10 -> "Long string: " + s;
            case String s -> "Short string: " + s;
            case Integer i when i > 0 -> "Positive: " + i;
            case Integer i -> "Non-positive: " + i;
            case null -> "Null value";
            default -> "Unknown type";
        };
        
        System.out.println(result);
        
        // Tricky: Order matters!
        // This won't compile (unreachable):
        // switch (obj) {
        //     case String s -> "Any string";
        //     case String s when s.length() > 10 -> "Long string";  // ❌ Unreachable!
        // }
        
        System.out.println();
        System.out.println("Pattern matching gotchas:");
        System.out.println("  1. Order matters - more specific patterns first");
        System.out.println("  2. Guarded patterns (when) must come before unguarded");
        System.out.println("  3. null case is explicit (not in default)");
    }

    /**
     * Null handling differences
     */
    public static void demonstrateNullHandling() {
        System.out.println("=== NULL HANDLING ===");
        
        Day day = null;
        
        // Old statement: NullPointerException
        try {
            switch (day) {
                case MONDAY -> System.out.println("Monday");
                default -> System.out.println("Other");
            }
        } catch (NullPointerException e) {
            System.out.println("Statement: NPE on null");
        }
        
        // New expression: Can handle null explicitly (Java 21)
        String result = switch (day) {
            case null -> "Null day";
            case MONDAY -> "Monday";
            default -> "Other";
        };
        System.out.println("Expression result: " + result);
        
        System.out.println();
        System.out.println("Java 21 enhancement:");
        System.out.println("  Can now match null explicitly in switch");
        System.out.println("  Prevents NPE, makes intent clear");
    }
}
