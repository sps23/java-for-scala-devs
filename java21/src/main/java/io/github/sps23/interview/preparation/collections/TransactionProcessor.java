package io.github.sps23.interview.preparation.collections;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Demonstrates modern Java collection factory methods and Stream API
 * fundamentals.
 *
 * <p>
 * This class showcases:
 * <ul>
 * <li>{@code List.of()}, {@code Set.of()}, {@code Map.of()},
 * {@code Map.ofEntries()} - Collection factory methods (Java 9+)</li>
 * <li>{@code Stream.toList()} - Immutable list collector (Java 16+)</li>
 * <li>{@code Collectors.teeing()} - Combined collectors (Java 12+)</li>
 * <li>Stream operations: filter, map, groupBy, statistics</li>
 * <li>Immutable vs mutable collections comparison</li>
 * </ul>
 *
 * <h2>Problem Statement:</h2>
 * <p>
 * Process a list of transactions: filter by amount, group by category, and
 * calculate statistics.
 */
public final class TransactionProcessor {

    private TransactionProcessor() {
        // Utility class
    }

    // ========================================================================
    // Collection Factory Methods (Java 9+)
    // ========================================================================

    /**
     * Demonstrates List.of() - Creates an immutable list.
     *
     * <p>
     * Key characteristics:
     * <ul>
     * <li>Returns an unmodifiable list</li>
     * <li>Does not allow null elements</li>
     * <li>Throws UnsupportedOperationException on modification attempts</li>
     * </ul>
     *
     * @return immutable list of sample transactions
     */
    public static List<Transaction> createSampleTransactionsWithListOf() {
        return List.of(
                new Transaction(1, "Food", 25.50, "Grocery shopping", LocalDate.of(2025, 1, 15)),
                new Transaction(2, "Transport", 45.00, "Uber ride", LocalDate.of(2025, 1, 15)),
                new Transaction(3, "Entertainment", 120.00, "Concert tickets",
                        LocalDate.of(2025, 1, 16)),
                new Transaction(4, "Food", 35.75, "Restaurant dinner", LocalDate.of(2025, 1, 17)),
                new Transaction(5, "Transport", 30.00, "Train ticket", LocalDate.of(2025, 1, 18)),
                new Transaction(6, "Food", 15.25, "Coffee shop", LocalDate.of(2025, 1, 18)),
                new Transaction(7, "Entertainment", 60.00, "Movie night",
                        LocalDate.of(2025, 1, 19)),
                new Transaction(8, "Shopping", 250.00, "New shoes", LocalDate.of(2025, 1, 20)),
                new Transaction(9, "Food", 42.50, "Takeout order", LocalDate.of(2025, 1, 20)),
                new Transaction(10, "Transport", 55.00, "Taxi fare", LocalDate.of(2025, 1, 21)));
    }

    /**
     * Demonstrates Set.of() - Creates an immutable set.
     *
     * <p>
     * Key characteristics:
     * <ul>
     * <li>Returns an unmodifiable set</li>
     * <li>Does not allow null elements</li>
     * <li>Does not allow duplicate elements (throws IllegalArgumentException)</li>
     * </ul>
     *
     * @return immutable set of valid categories
     */
    public static Set<String> getValidCategoriesWithSetOf() {
        return Set.of("Food", "Transport", "Entertainment", "Shopping", "Utilities", "Healthcare");
    }

    /**
     * Demonstrates Map.of() - Creates an immutable map (up to 10 entries).
     *
     * <p>
     * Key characteristics:
     * <ul>
     * <li>Returns an unmodifiable map</li>
     * <li>Does not allow null keys or values</li>
     * <li>Does not allow duplicate keys</li>
     * <li>Convenient for small maps (up to 10 key-value pairs)</li>
     * </ul>
     *
     * @return immutable map of category descriptions
     */
    public static Map<String, String> getCategoryDescriptionsWithMapOf() {
        return Map.of("Food", "Food and dining expenses", "Transport",
                "Transportation and travel costs", "Entertainment",
                "Entertainment and leisure activities", "Shopping", "Shopping and retail purchases",
                "Utilities", "Utility bills and services");
    }

    /**
     * Demonstrates Map.ofEntries() - Creates an immutable map with any number of
     * entries.
     *
     * <p>
     * Use this when you need more than 10 entries or prefer explicit entry
     * creation.
     *
     * @return immutable map of category budgets
     */
    public static Map<String, Double> getCategoryBudgetsWithMapOfEntries() {
        return Map.ofEntries(Map.entry("Food", 500.0), Map.entry("Transport", 200.0),
                Map.entry("Entertainment", 150.0), Map.entry("Shopping", 300.0),
                Map.entry("Utilities", 250.0), Map.entry("Healthcare", 100.0));
    }

    // ========================================================================
    // Mutable vs Immutable Collections Comparison
    // ========================================================================

    /**
     * Demonstrates the difference between mutable and immutable collections.
     *
     * <p>
     * Java 8 style (mutable):
     *
     * <pre>
     * List<String> list = new ArrayList<>();
     * list.add("item"); // OK
     * </pre>
     *
     * <p>
     * Java 9+ style (immutable):
     *
     * <pre>
     * List<String> list = List.of("item1", "item2");
     * list.add("item3"); // Throws UnsupportedOperationException
     * </pre>
     */
    public static void demonstrateImmutableVsMutable() {
        // Mutable list - Java 8 style
        List<String> mutableList = new ArrayList<>();
        mutableList.add("Apple");
        mutableList.add("Banana");
        mutableList.add("Cherry");
        System.out.println("Mutable list: " + mutableList);

        // Immutable list - Java 9+ style
        List<String> immutableList = List.of("Apple", "Banana", "Cherry");
        System.out.println("Immutable list: " + immutableList);

        // Convert immutable to mutable when needed
        List<String> mutableCopy = new ArrayList<>(immutableList);
        mutableCopy.add("Date");
        System.out.println("Mutable copy with addition: " + mutableCopy);

        // Mutable map - Java 8 style
        Map<String, Integer> mutableMap = new HashMap<>();
        mutableMap.put("one", 1);
        mutableMap.put("two", 2);
        System.out.println("Mutable map: " + mutableMap);

        // Immutable map - Java 9+ style
        Map<String, Integer> immutableMap = Map.of("one", 1, "two", 2);
        System.out.println("Immutable map: " + immutableMap);
    }

    // ========================================================================
    // Stream API Operations
    // ========================================================================

    /**
     * Filters transactions by minimum amount using Stream API.
     *
     * <p>
     * Uses {@code Stream.toList()} (Java 16+) which returns an unmodifiable list.
     *
     * @param transactions
     *            list of transactions to filter
     * @param minAmount
     *            minimum amount threshold
     * @return filtered list of transactions
     */
    public static List<Transaction> filterByMinAmount(List<Transaction> transactions,
            double minAmount) {
        return transactions.stream().filter(t -> t.amount() >= minAmount).toList(); // Java 16+
    }

    /**
     * Filters transactions by category.
     *
     * @param transactions
     *            list of transactions to filter
     * @param category
     *            category to filter by
     * @return filtered list of transactions
     */
    public static List<Transaction> filterByCategory(List<Transaction> transactions,
            String category) {
        return transactions.stream().filter(t -> t.category().equals(category)).toList();
    }

    /**
     * Groups transactions by category.
     *
     * @param transactions
     *            list of transactions to group
     * @return map of category to list of transactions
     */
    public static Map<String, List<Transaction>> groupByCategory(List<Transaction> transactions) {
        return transactions.stream().collect(Collectors.groupingBy(Transaction::category));
    }

    /**
     * Calculates total amount per category.
     *
     * @param transactions
     *            list of transactions
     * @return map of category to total amount
     */
    public static Map<String, Double> calculateTotalByCategory(List<Transaction> transactions) {
        return transactions.stream().collect(Collectors.groupingBy(Transaction::category,
                Collectors.summingDouble(Transaction::amount)));
    }

    /**
     * Calculates statistics for all transactions.
     *
     * <p>
     * DoubleSummaryStatistics provides: count, sum, min, max, average
     *
     * @param transactions
     *            list of transactions
     * @return summary statistics for transaction amounts
     */
    public static DoubleSummaryStatistics calculateStatistics(List<Transaction> transactions) {
        return transactions.stream().mapToDouble(Transaction::amount).summaryStatistics();
    }

    /**
     * Calculates statistics per category.
     *
     * @param transactions
     *            list of transactions
     * @return map of category to statistics
     */
    public static Map<String, DoubleSummaryStatistics> calculateStatisticsByCategory(
            List<Transaction> transactions) {
        return transactions.stream().collect(Collectors.groupingBy(Transaction::category,
                Collectors.summarizingDouble(Transaction::amount)));
    }

    // ========================================================================
    // Collectors.teeing() - Combined Collectors (Java 12+)
    // ========================================================================

    /**
     * Result record for combined min/max statistics.
     *
     * @param min
     *            minimum transaction
     * @param max
     *            maximum transaction
     */
    public record MinMaxResult(Transaction min, Transaction max) {
    }

    /**
     * Demonstrates Collectors.teeing() to find both min and max in a single pass.
     *
     * <p>
     * {@code Collectors.teeing()} combines two collectors and merges their results.
     * This is more efficient than running two separate stream operations.
     *
     * @param transactions
     *            list of transactions
     * @return MinMaxResult containing both min and max transactions
     */
    public static MinMaxResult findMinAndMaxTransaction(List<Transaction> transactions) {
        return transactions.stream().collect(Collectors.teeing(
                Collectors.minBy((t1, t2) -> Double.compare(t1.amount(), t2.amount())),
                Collectors.maxBy((t1, t2) -> Double.compare(t1.amount(), t2.amount())),
                (minOpt, maxOpt) -> new MinMaxResult(minOpt.orElse(null), maxOpt.orElse(null))));
    }

    /**
     * Result record for combined statistics with filtered data.
     *
     * @param total
     *            total amount sum
     * @param count
     *            number of transactions
     * @param average
     *            average amount
     */
    public record SummaryResult(double total, long count, double average) {
    }

    /**
     * Demonstrates Collectors.teeing() for custom summary statistics.
     *
     * @param transactions
     *            list of transactions
     * @return SummaryResult with total, count, and average
     */
    public static SummaryResult calculateSummary(List<Transaction> transactions) {
        return transactions.stream().collect(Collectors.teeing(
                Collectors.summingDouble(Transaction::amount), Collectors.counting(),
                (sum, count) -> new SummaryResult(sum, count, count > 0 ? sum / count : 0.0)));
    }

    // ========================================================================
    // Evolution from Java 8 to Modern Java
    // ========================================================================

    /**
     * Java 8 style: Using Collectors.toList() which returns a mutable ArrayList.
     *
     * @param transactions
     *            list of transactions
     * @param minAmount
     *            minimum amount threshold
     * @return mutable list of filtered transactions
     */
    public static List<Transaction> filterJava8Style(List<Transaction> transactions,
            double minAmount) {
        return transactions.stream().filter(t -> t.amount() >= minAmount)
                .collect(Collectors.toList()); // Returns mutable ArrayList
    }

    /**
     * Modern Java 16+ style: Using Stream.toList() which returns an unmodifiable
     * list.
     *
     * @param transactions
     *            list of transactions
     * @param minAmount
     *            minimum amount threshold
     * @return immutable list of filtered transactions
     */
    public static List<Transaction> filterModernStyle(List<Transaction> transactions,
            double minAmount) {
        return transactions.stream().filter(t -> t.amount() >= minAmount).toList(); // Returns
                                                                                    // unmodifiable
                                                                                    // list
    }

    /**
     * Java 8 style: Create a map using Collectors.toMap().
     *
     * @param transactions
     *            list of transactions
     * @return map of transaction ID to transaction
     */
    public static Map<Long, Transaction> createTransactionMapJava8(List<Transaction> transactions) {
        return transactions.stream().collect(Collectors.toMap(Transaction::id, t -> t));
    }

    /**
     * Processes transactions and prints a comprehensive report.
     *
     * @param transactions
     *            list of transactions to process
     */
    public static void processAndPrintReport(List<Transaction> transactions) {
        System.out.println("=== Transaction Processing Report ===\n");

        // 1. Filter high-value transactions
        System.out.println("--- High-Value Transactions (>$50) ---");
        filterByMinAmount(transactions, 50.0)
                .forEach(t -> System.out.println("  " + t.toFormattedString()));

        // 2. Group by category
        System.out.println("\n--- Transactions by Category ---");
        Map<String, List<Transaction>> byCategory = groupByCategory(transactions);
        byCategory.forEach((category, txns) -> {
            System.out.println("  " + category + ":");
            txns.forEach(t -> System.out.println("    - " + t.toFormattedString()));
        });

        // 3. Calculate totals per category
        System.out.println("\n--- Total Spending by Category ---");
        calculateTotalByCategory(transactions)
                .forEach((category, total) -> System.out.printf("  %s: $%.2f%n", category, total));

        // 4. Overall statistics
        System.out.println("\n--- Overall Statistics ---");
        DoubleSummaryStatistics stats = calculateStatistics(transactions);
        System.out.printf("  Count: %d transactions%n", stats.getCount());
        System.out.printf("  Total: $%.2f%n", stats.getSum());
        System.out.printf("  Average: $%.2f%n", stats.getAverage());
        System.out.printf("  Min: $%.2f%n", stats.getMin());
        System.out.printf("  Max: $%.2f%n", stats.getMax());

        // 5. Min/Max transactions using teeing
        System.out.println("\n--- Min/Max Transactions (using Collectors.teeing) ---");
        MinMaxResult minMax = findMinAndMaxTransaction(transactions);
        if (minMax.min() != null) {
            System.out.println("  Minimum: " + minMax.min().toFormattedString());
        }
        if (minMax.max() != null) {
            System.out.println("  Maximum: " + minMax.max().toFormattedString());
        }
    }

    /**
     * Main method demonstrating all collection factory methods and stream
     * operations.
     *
     * @param args
     *            command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=== Collection Factory Methods and Stream API Demo ===\n");

        // Demo 1: Collection factory methods
        System.out.println("--- Collection Factory Methods (Java 9+) ---\n");

        System.out.println("List.of() - Sample transactions:");
        List<Transaction> transactions = createSampleTransactionsWithListOf();
        transactions.forEach(t -> System.out.println("  " + t.toFormattedString()));

        System.out.println("\nSet.of() - Valid categories:");
        Set<String> categories = getValidCategoriesWithSetOf();
        System.out.println("  " + categories);

        System.out.println("\nMap.of() - Category descriptions:");
        Map<String, String> descriptions = getCategoryDescriptionsWithMapOf();
        descriptions.forEach((k, v) -> System.out.printf("  %s: %s%n", k, v));

        System.out.println("\nMap.ofEntries() - Category budgets:");
        Map<String, Double> budgets = getCategoryBudgetsWithMapOfEntries();
        budgets.forEach((k, v) -> System.out.printf("  %s: $%.2f%n", k, v));

        // Demo 2: Mutable vs Immutable
        System.out.println("\n--- Mutable vs Immutable Collections ---\n");
        demonstrateImmutableVsMutable();

        // Demo 3: Process transactions
        System.out.println("\n");
        processAndPrintReport(transactions);

        // Demo 4: Stream.toList() vs Collectors.toList()
        System.out.println("\n--- Stream.toList() vs Collectors.toList() ---\n");
        List<Transaction> java8Result = filterJava8Style(transactions, 50.0);
        List<Transaction> modernResult = filterModernStyle(transactions, 50.0);
        System.out.printf("Java 8 style (Collectors.toList()): %d transactions%n",
                java8Result.size());
        System.out.printf("Modern style (Stream.toList()): %d transactions%n", modernResult.size());
        System.out.println("Note: Stream.toList() returns an unmodifiable list!");

        // Demo 5: Collectors.teeing() example
        System.out.println("\n--- Collectors.teeing() Summary ---\n");
        SummaryResult summary = calculateSummary(transactions);
        System.out.printf("Total: $%.2f%n", summary.total());
        System.out.printf("Count: %d%n", summary.count());
        System.out.printf("Average: $%.2f%n", summary.average());
    }
}
