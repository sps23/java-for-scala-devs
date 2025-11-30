package io.github.sps23.interview.preparation.typeinference;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Demonstrates local variable type inference using the {@code var} keyword
 * (Java 10+).
 *
 * <p>
 * This class showcases when to use {@code var}, where it cannot be used, and
 * best practices for maintaining code readability.
 *
 * <h2>Key Points:</h2>
 * <ul>
 * <li>{@code var} is for local variables with initializers only</li>
 * <li>Cannot be used for fields, method parameters, or return types</li>
 * <li>Type is inferred at compile time (still statically typed)</li>
 * <li>Works with generics and the diamond operator</li>
 * </ul>
 */
public final class LocalVariableTypeInference {

    private LocalVariableTypeInference() {
        // Utility class
    }

    // ========================================================================
    // Data model for examples
    // ========================================================================

    /**
     * Represents a sales transaction for processing.
     */
    public record Transaction(long id, String product, String category, double amount, int quantity,
            LocalDate date, String region) {

        public Transaction {
            Objects.requireNonNull(product, "Product cannot be null");
            Objects.requireNonNull(category, "Category cannot be null");
            Objects.requireNonNull(date, "Date cannot be null");
            Objects.requireNonNull(region, "Region cannot be null");
            if (amount < 0) {
                throw new IllegalArgumentException("Amount cannot be negative");
            }
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
        }

        /**
         * Calculates the total value of this transaction.
         */
        public double totalValue() {
            return amount * quantity;
        }
    }

    /**
     * Summary report for a category.
     */
    public record CategorySummary(String category, long transactionCount, double totalAmount,
            double averageAmount) {
    }

    // ========================================================================
    // Sample data
    // ========================================================================

    /**
     * Creates sample transaction data for demonstrations.
     */
    public static List<Transaction> sampleTransactions() {
        return List.of(
                new Transaction(1, "Laptop", "Electronics", 1200.00, 2, LocalDate.of(2024, 1, 15),
                        "North"),
                new Transaction(2, "Mouse", "Electronics", 25.00, 10, LocalDate.of(2024, 1, 16),
                        "South"),
                new Transaction(3, "Desk Chair", "Furniture", 350.00, 3, LocalDate.of(2024, 1, 17),
                        "North"),
                new Transaction(4, "Monitor", "Electronics", 400.00, 5, LocalDate.of(2024, 1, 18),
                        "East"),
                new Transaction(5, "Bookshelf", "Furniture", 180.00, 2, LocalDate.of(2024, 1, 19),
                        "West"),
                new Transaction(6, "Keyboard", "Electronics", 75.00, 8, LocalDate.of(2024, 1, 20),
                        "North"),
                new Transaction(7, "Coffee Table", "Furniture", 220.00, 1,
                        LocalDate.of(2024, 1, 21), "South"),
                new Transaction(8, "Webcam", "Electronics", 90.00, 6, LocalDate.of(2024, 1, 22),
                        "East"));
    }

    // ========================================================================
    // GOOD: When var improves readability
    // ========================================================================

    /**
     * Demonstrates good use of var - when the type is obvious from the right side.
     *
     * <p>
     * var improves readability when:
     * <ul>
     * <li>The type is obvious from the constructor call</li>
     * <li>Using literals where the type is clear</li>
     * <li>Using factory methods with descriptive names</li>
     * </ul>
     */
    public static void goodVarUsageObviousTypes() {
        // Type is obvious from constructor - var is cleaner
        var transactions = new ArrayList<Transaction>();
        var categoryTotals = new HashMap<String, Double>();

        // Type is obvious from literal
        var count = 0;
        var total = 0.0;
        var message = "Processing transactions";

        // Type is obvious from factory method name
        var today = LocalDate.now();
        var emptyList = List.of();

        System.out.println("Count: " + count);
        System.out.println("Total: " + total);
        System.out.println("Message: " + message);
        System.out.println("Today: " + today);
        System.out.println("Empty list size: " + emptyList.size());
        System.out.println("Transactions list: " + transactions.getClass().getSimpleName());
        System.out.println("Category totals map: " + categoryTotals.getClass().getSimpleName());
    }

    /**
     * Demonstrates var reducing verbosity with generics.
     *
     * <p>
     * var is especially helpful with complex generic types where repeating the type
     * adds noise without value.
     */
    public static void goodVarUsageWithGenerics() {
        var transactions = sampleTransactions();

        // Without var: verbose repetition
        // Map<String, List<Transaction>> byCategory = transactions.stream()
        // .collect(Collectors.groupingBy(Transaction::category));

        // With var: cleaner, type is clear from context
        var byCategory = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::category));

        // Nested generics - var significantly reduces noise
        var categoryToRegionMap = transactions.stream().collect(Collectors
                .groupingBy(Transaction::category, Collectors.groupingBy(Transaction::region)));

        System.out.println("Categories: " + byCategory.keySet());
        System.out.println("Nested map structure: " + categoryToRegionMap.keySet());
    }

    /**
     * Demonstrates var in loops and iterations.
     *
     * <p>
     * var works well in for-each loops and traditional for loops where the type is
     * contextually clear.
     */
    public static void goodVarUsageInLoops() {
        var transactions = sampleTransactions();

        // var in enhanced for loop - type clear from collection
        System.out.println("Transactions by product:");
        for (var transaction : transactions) {
            System.out.printf("  %s: $%.2f%n", transaction.product(), transaction.amount());
        }

        // var in traditional for loop
        System.out.println("\nTransaction IDs:");
        for (var i = 0; i < transactions.size(); i++) {
            System.out.printf("  Index %d: ID %d%n", i, transactions.get(i).id());
        }
    }

    /**
     * Demonstrates var with try-with-resources (Java 10+).
     *
     * <p>
     * var can be used in try-with-resources when the resource type is clear from
     * the constructor.
     */
    public static void goodVarUsageWithTryWithResources() {
        var data = "id,product,amount\n1,Laptop,1200\n2,Mouse,25";

        try (var reader = new java.io.BufferedReader(new java.io.StringReader(data))) {
            var line = reader.readLine(); // Header
            System.out.println("Header: " + line);
            while ((line = reader.readLine()) != null) {
                System.out.println("Data: " + line);
            }
        } catch (java.io.IOException e) {
            System.err.println("Error reading data: " + e.getMessage());
        }
    }

    // ========================================================================
    // AVOID: When explicit types are better
    // ========================================================================

    /**
     * Demonstrates cases where explicit types improve readability.
     *
     * <p>
     * Avoid var when:
     * <ul>
     * <li>The type is not obvious from context</li>
     * <li>The method name doesn't indicate the return type</li>
     * <li>Using numeric literals that could be ambiguous</li>
     * </ul>
     */
    public static void whenExplicitTypesAreBetter() {
        var transactions = sampleTransactions();

        // AVOID: Type not obvious from method call
        // var result = processData(transactions); // What type is result?

        // BETTER: Explicit type documents intent
        List<CategorySummary> summaries = generateCategorySummaries(transactions);
        System.out.println("Summaries generated: " + summaries.size());

        // AVOID: Numeric literal ambiguity
        // var value = 100; // Is this int, long, or something else?

        // BETTER: Explicit type for clarity
        long transactionId = 100L;
        double amount = 100.0;
        System.out.println("ID: " + transactionId + ", Amount: " + amount);

        // AVOID: Chained method calls where type isn't clear
        // var x = someObject.getProcessor().process().getResult();

        // BETTER: Break into steps or use explicit type
        double total = transactions.stream().mapToDouble(Transaction::amount).sum();
        System.out.println("Total amount: $" + total);
    }

    /**
     * Demonstrates var with the diamond operator.
     *
     * <p>
     * When using var with diamond operator, the compiler infers the most specific
     * type possible.
     */
    public static void varWithDiamondOperator() {
        // With diamond operator - infers ArrayList<Transaction>
        var transactions = new ArrayList<Transaction>();
        transactions.add(sampleTransactions().get(0));

        // With diamond operator on Map
        var totals = new HashMap<String, Double>();
        totals.put("Electronics", 1500.0);

        // Note: var with diamond and no context infers Object
        // var list = new ArrayList<>(); // Infers ArrayList<Object> - avoid this!

        // Better: Be explicit when using diamond with var
        var explicitList = new ArrayList<String>();
        explicitList.add("item1");

        System.out.println("Transactions: " + transactions);
        System.out.println("Totals: " + totals);
        System.out.println("Explicit list: " + explicitList);
    }

    // ========================================================================
    // WHERE VAR CANNOT BE USED
    // ========================================================================

    // ILLEGAL: var cannot be used for fields
    // private var fieldValue = 10; // Compilation error!

    // ILLEGAL: var cannot be used for method parameters
    // public void process(var data) { } // Compilation error!

    // ILLEGAL: var cannot be used for return types
    // public var getData() { return List.of(); } // Compilation error!

    // ILLEGAL: var cannot be used without initializer
    // var uninitializedValue; // Compilation error!

    // ILLEGAL: var cannot be used with null initializer
    // var nullValue = null; // Compilation error!

    // ILLEGAL: var cannot be used with lambda expressions
    // var comparator = (a, b) -> a - b; // Compilation error!

    // ILLEGAL: var cannot be used with method references without context
    // var processor = this::process; // Compilation error!

    // ========================================================================
    // Data processing pipeline example
    // ========================================================================

    /**
     * Demonstrates a complete data processing pipeline using var appropriately.
     *
     * <p>
     * This example shows a realistic use case where var improves code readability
     * by reducing boilerplate while maintaining type safety.
     */
    public static Map<String, CategorySummary> processTransactionPipeline(
            List<Transaction> transactions) {

        // var is good here - type obvious from stream operation
        var filteredTransactions = transactions.stream().filter(t -> t.amount() >= 50.0).toList();

        // var good - Map type clear from Collectors.groupingBy context
        var byCategory = filteredTransactions.stream()
                .collect(Collectors.groupingBy(Transaction::category));

        // var good - type clear from stream().map() with toMap
        var summaries = byCategory.entrySet().stream().map(entry -> {
            var category = entry.getKey();
            var categoryTransactions = entry.getValue();

            // Explicit types for calculated values improve clarity
            long count = categoryTransactions.size();
            double total = categoryTransactions.stream().mapToDouble(Transaction::amount).sum();
            double average = count > 0 ? total / count : 0.0;

            return new CategorySummary(category, count, total, average);
        }).collect(Collectors.toMap(CategorySummary::category, s -> s));

        return summaries;
    }

    /**
     * Generates category summaries from transactions.
     */
    public static List<CategorySummary> generateCategorySummaries(List<Transaction> transactions) {
        return processTransactionPipeline(transactions).values().stream().toList();
    }

    /**
     * Demonstrates the full pipeline with output.
     */
    public static void demonstratePipeline() {
        var transactions = sampleTransactions();
        var summaries = processTransactionPipeline(transactions);

        System.out.println("Transaction Pipeline Results:");
        System.out.println("=============================");

        for (var entry : summaries.entrySet()) {
            var category = entry.getKey();
            var summary = entry.getValue();

            System.out.printf("%s:%n", category);
            System.out.printf("  Transactions: %d%n", summary.transactionCount());
            System.out.printf("  Total: $%.2f%n", summary.totalAmount());
            System.out.printf("  Average: $%.2f%n", summary.averageAmount());
        }
    }

    // ========================================================================
    // Main demonstration
    // ========================================================================

    /**
     * Main method demonstrating all var keyword features.
     *
     * @param args
     *            command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=== Java 10+ var Keyword Demo ===\n");

        System.out.println("--- Good var usage: Obvious types ---\n");
        goodVarUsageObviousTypes();

        System.out.println("\n--- Good var usage: With generics ---\n");
        goodVarUsageWithGenerics();

        System.out.println("\n--- Good var usage: In loops ---\n");
        goodVarUsageInLoops();

        System.out.println("\n--- Good var usage: Try-with-resources ---\n");
        goodVarUsageWithTryWithResources();

        System.out.println("\n--- When explicit types are better ---\n");
        whenExplicitTypesAreBetter();

        System.out.println("\n--- var with diamond operator ---\n");
        varWithDiamondOperator();

        System.out.println("\n--- Data processing pipeline ---\n");
        demonstratePipeline();
    }
}
