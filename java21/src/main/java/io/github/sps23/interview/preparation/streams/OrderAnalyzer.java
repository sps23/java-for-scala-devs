package io.github.sps23.interview.preparation.streams;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Demonstrates advanced Stream API operations and collectors in Java 21.
 *
 * <p>
 * This class showcases:
 * <ul>
 * <li>{@code Stream.takeWhile()}, {@code Stream.dropWhile()} (Java 9+)</li>
 * <li>{@code Collectors.groupingBy()} with downstream collectors</li>
 * <li>{@code Collectors.partitioningBy()}</li>
 * <li>Custom collectors</li>
 * <li>Parallel streams and when to use them</li>
 * </ul>
 *
 * <h2>Problem Statement:</h2>
 * <p>
 * Analyze a dataset of orders: calculate running totals, find top N by
 * category, and generate a summary report.
 */
public final class OrderAnalyzer {

    private OrderAnalyzer() {
        // Utility class
    }

    // ========================================================================
    // Sample Data Creation
    // ========================================================================

    /**
     * Creates a sample list of orders for demonstration.
     *
     * @return immutable list of sample orders
     */
    public static List<Order> createSampleOrders() {
        return List.of(
                new Order(1, "Electronics", 1299.99, "Alice", LocalDate.of(2025, 1, 15),
                        List.of("Laptop", "Mouse")),
                new Order(2, "Books", 45.50, "Bob", LocalDate.of(2025, 1, 15),
                        List.of("Java Programming", "Design Patterns")),
                new Order(3, "Electronics", 799.00, "Charlie", LocalDate.of(2025, 1, 16),
                        List.of("Tablet")),
                new Order(4, "Clothing", 125.00, "Alice", LocalDate.of(2025, 1, 17),
                        List.of("Jacket", "Jeans")),
                new Order(5, "Books", 32.99, "Diana", LocalDate.of(2025, 1, 18),
                        List.of("Scala for Impatient")),
                new Order(6, "Electronics", 2499.99, "Eve", LocalDate.of(2025, 1, 18),
                        List.of("Desktop Computer", "Monitor", "Keyboard")),
                new Order(7, "Clothing", 89.99, "Frank", LocalDate.of(2025, 1, 19),
                        List.of("Shirt", "Tie")),
                new Order(8, "Books", 89.00, "Alice", LocalDate.of(2025, 1, 20),
                        List.of("Kotlin in Action", "Effective Java", "Clean Code")),
                new Order(9, "Electronics", 349.99, "Bob", LocalDate.of(2025, 1, 20),
                        List.of("Headphones")),
                new Order(10, "Clothing", 250.00, "Charlie", LocalDate.of(2025, 1, 21),
                        List.of("Suit")),
                new Order(11, "Books", 28.50, "Diana", LocalDate.of(2025, 1, 22),
                        List.of("Functional Programming")),
                new Order(12, "Electronics", 599.00, "Eve", LocalDate.of(2025, 1, 22),
                        List.of("Smartphone")));
    }

    // ========================================================================
    // takeWhile() and dropWhile() (Java 9+)
    // ========================================================================

    /**
     * Demonstrates Stream.takeWhile() - takes elements while predicate is true.
     *
     * <p>
     * {@code takeWhile()} is a short-circuiting intermediate operation that takes
     * elements from the stream as long as the predicate returns true. Once the
     * predicate returns false, the remaining elements are discarded.
     *
     * <p>
     * Note: Works best with ordered streams. For unordered streams, behavior is
     * non-deterministic.
     *
     * @param orders
     *            sorted list of orders (typically by date or amount)
     * @param maxAmount
     *            take orders while total amount stays below this threshold
     * @return list of orders taken while cumulative condition is met
     */
    public static List<Order> takeOrdersWhileBelowBudget(List<Order> orders, double maxAmount) {
        // Sort by amount ascending
        return orders.stream().sorted(Comparator.comparingDouble(Order::amount))
                .takeWhile(order -> order.amount() < maxAmount).toList();
    }

    /**
     * Demonstrates Stream.takeWhile() with sorted data - takes orders from the
     * beginning of month.
     *
     * @param orders
     *            list of orders sorted by date
     * @param cutoffDate
     *            take orders before this date
     * @return orders before the cutoff date
     */
    public static List<Order> takeOrdersBeforeDate(List<Order> orders, LocalDate cutoffDate) {
        return orders.stream().sorted(Comparator.comparing(Order::date))
                .takeWhile(order -> order.date().isBefore(cutoffDate)).toList();
    }

    /**
     * Demonstrates Stream.dropWhile() - drops elements while predicate is true.
     *
     * <p>
     * {@code dropWhile()} is the opposite of {@code takeWhile()}. It discards
     * elements from the stream as long as the predicate returns true. Once the
     * predicate returns false, it passes all remaining elements.
     *
     * @param orders
     *            sorted list of orders (by amount ascending)
     * @param threshold
     *            skip orders below this amount
     * @return orders starting from first order at or above threshold
     */
    public static List<Order> dropOrdersBelowThreshold(List<Order> orders, double threshold) {
        return orders.stream().sorted(Comparator.comparingDouble(Order::amount))
                .dropWhile(order -> order.amount() < threshold).toList();
    }

    /**
     * Demonstrates combining takeWhile() and dropWhile() for range selection.
     *
     * <p>
     * This pattern selects orders within a specific amount range from a sorted
     * stream.
     *
     * @param orders
     *            list of orders
     * @param minAmount
     *            minimum amount (inclusive via drop)
     * @param maxAmount
     *            maximum amount (exclusive via take)
     * @return orders in the specified amount range
     */
    public static List<Order> getOrdersInAmountRange(List<Order> orders, double minAmount,
            double maxAmount) {
        return orders.stream().sorted(Comparator.comparingDouble(Order::amount))
                .dropWhile(order -> order.amount() < minAmount)
                .takeWhile(order -> order.amount() < maxAmount).toList();
    }

    // ========================================================================
    // Collectors.groupingBy() with Downstream Collectors
    // ========================================================================

    /**
     * Groups orders by category with count as downstream collector.
     *
     * @param orders
     *            list of orders
     * @return map of category to order count
     */
    public static Map<String, Long> countOrdersByCategory(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(Order::category, Collectors.counting()));
    }

    /**
     * Groups orders by category with total amount as downstream collector.
     *
     * @param orders
     *            list of orders
     * @return map of category to total amount
     */
    public static Map<String, Double> totalAmountByCategory(List<Order> orders) {
        return orders.stream().collect(
                Collectors.groupingBy(Order::category, Collectors.summingDouble(Order::amount)));
    }

    /**
     * Groups orders by category with statistics as downstream collector.
     *
     * @param orders
     *            list of orders
     * @return map of category to summary statistics
     */
    public static Map<String, DoubleSummaryStatistics> statisticsByCategory(List<Order> orders) {
        return orders.stream().collect(Collectors.groupingBy(Order::category,
                Collectors.summarizingDouble(Order::amount)));
    }

    /**
     * Groups orders by category with list of customer names as downstream
     * collector.
     *
     * @param orders
     *            list of orders
     * @return map of category to list of unique customer names
     */
    public static Map<String, List<String>> customersByCategory(List<Order> orders) {
        return orders.stream().collect(Collectors.groupingBy(Order::category,
                Collectors.mapping(Order::customer, Collectors.toList())));
    }

    /**
     * Groups orders by category with unique customers (using Set) as downstream
     * collector.
     *
     * @param orders
     *            list of orders
     * @return map of category to set of unique customer names
     */
    public static Map<String, Set<String>> uniqueCustomersByCategory(List<Order> orders) {
        return orders.stream().collect(Collectors.groupingBy(Order::category,
                Collectors.mapping(Order::customer, Collectors.toSet())));
    }

    /**
     * Nested grouping: first by category, then by customer.
     *
     * @param orders
     *            list of orders
     * @return nested map of category -> customer -> list of orders
     */
    public static Map<String, Map<String, List<Order>>> ordersByCategoryAndCustomer(
            List<Order> orders) {
        return orders.stream().collect(
                Collectors.groupingBy(Order::category, Collectors.groupingBy(Order::customer)));
    }

    /**
     * Finds the highest value order per category using downstream collector.
     *
     * @param orders
     *            list of orders
     * @return map of category to highest value order
     */
    public static Map<String, Order> highestOrderByCategory(List<Order> orders) {
        return orders.stream()
                .collect(Collectors.groupingBy(Order::category,
                        Collectors.collectingAndThen(
                                Collectors.maxBy(Comparator.comparingDouble(Order::amount)),
                                opt -> opt.orElse(null))));
    }

    /**
     * Gets top N orders by amount for each category.
     *
     * @param orders
     *            list of orders
     * @param topN
     *            number of top orders to retrieve per category
     * @return map of category to top N orders sorted by amount descending
     */
    public static Map<String, List<Order>> topNOrdersByCategory(List<Order> orders, int topN) {
        return orders.stream()
                .collect(Collectors.groupingBy(Order::category,
                        Collectors.collectingAndThen(Collectors.toList(), list -> list.stream()
                                .sorted(Comparator.comparingDouble(Order::amount).reversed())
                                .limit(topN).toList())));
    }

    // ========================================================================
    // Collectors.partitioningBy()
    // ========================================================================

    /**
     * Partitions orders into high-value (>= threshold) and regular orders.
     *
     * <p>
     * {@code partitioningBy()} is a special case of grouping that creates exactly
     * two groups: one for elements matching the predicate (key=true), and one for
     * elements not matching (key=false).
     *
     * @param orders
     *            list of orders
     * @param threshold
     *            amount threshold for high-value classification
     * @return map with true -> high-value orders, false -> regular orders
     */
    public static Map<Boolean, List<Order>> partitionByHighValue(List<Order> orders,
            double threshold) {
        return orders.stream()
                .collect(Collectors.partitioningBy(order -> order.amount() >= threshold));
    }

    /**
     * Partitions orders by date (before/after cutoff) with count downstream.
     *
     * @param orders
     *            list of orders
     * @param cutoffDate
     *            date to partition around
     * @return map with true -> count of orders on/after cutoff, false -> count
     *         before
     */
    public static Map<Boolean, Long> countByDatePartition(List<Order> orders,
            LocalDate cutoffDate) {
        return orders.stream().collect(Collectors.partitioningBy(
                order -> !order.date().isBefore(cutoffDate), Collectors.counting()));
    }

    /**
     * Partitions orders with total amount as downstream collector.
     *
     * @param orders
     *            list of orders
     * @param threshold
     *            amount threshold
     * @return map with true -> total of high-value orders, false -> total of
     *         regular
     */
    public static Map<Boolean, Double> partitionWithTotals(List<Order> orders, double threshold) {
        return orders.stream().collect(Collectors.partitioningBy(
                order -> order.amount() >= threshold, Collectors.summingDouble(Order::amount)));
    }

    // ========================================================================
    // Custom Collectors
    // ========================================================================

    /**
     * Result record for running total calculation.
     *
     * @param order
     *            the order
     * @param runningTotal
     *            cumulative total including this order
     */
    public record OrderWithRunningTotal(Order order, double runningTotal) {
    }

    /**
     * Custom collector to calculate running totals.
     *
     * <p>
     * This demonstrates creating a custom collector using Collector.of(). The
     * collector maintains state (running total) as it processes elements.
     *
     * @return a collector that produces orders with running totals
     */
    public static Collector<Order, ?, List<OrderWithRunningTotal>> runningTotalCollector() {
        return Collector.of(
                // Supplier: creates the accumulator (list + running total holder)
                () -> {
                    List<OrderWithRunningTotal> list = new ArrayList<>();
                    return new Object[]{list, new double[]{0.0}};
                },
                // Accumulator: adds each order with running total
                (acc, order) -> {
                    @SuppressWarnings("unchecked")
                    List<OrderWithRunningTotal> list = (List<OrderWithRunningTotal>) acc[0];
                    double[] total = (double[]) acc[1];
                    total[0] += order.amount();
                    list.add(new OrderWithRunningTotal(order, total[0]));
                },
                // Combiner: merges two accumulators (for parallel execution)
                (acc1, acc2) -> {
                    @SuppressWarnings("unchecked")
                    List<OrderWithRunningTotal> list1 = (List<OrderWithRunningTotal>) acc1[0];
                    @SuppressWarnings("unchecked")
                    List<OrderWithRunningTotal> list2 = (List<OrderWithRunningTotal>) acc2[0];
                    double[] total1 = (double[]) acc1[1];
                    double[] total2 = (double[]) acc2[1];

                    // Adjust running totals in list2 and add to list1
                    double offset = total1[0];
                    for (OrderWithRunningTotal owrt : list2) {
                        list1.add(new OrderWithRunningTotal(owrt.order(),
                                owrt.runningTotal() + offset));
                    }
                    total1[0] += total2[0];
                    return acc1;
                },
                // Finisher: extracts the result list
                acc -> {
                    @SuppressWarnings("unchecked")
                    List<OrderWithRunningTotal> list = (List<OrderWithRunningTotal>) acc[0];
                    return list;
                });
    }

    /**
     * Calculates running totals for orders sorted by date.
     *
     * @param orders
     *            list of orders
     * @return list of orders with running totals
     */
    public static List<OrderWithRunningTotal> calculateRunningTotals(List<Order> orders) {
        return orders.stream().sorted(Comparator.comparing(Order::date))
                .collect(runningTotalCollector());
    }

    /**
     * Summary report record.
     *
     * @param category
     *            the category name
     * @param orderCount
     *            number of orders
     * @param totalAmount
     *            sum of all order amounts
     * @param averageAmount
     *            average order amount
     * @param minAmount
     *            minimum order amount
     * @param maxAmount
     *            maximum order amount
     * @param uniqueCustomers
     *            number of unique customers
     */
    public record CategorySummary(String category, long orderCount, double totalAmount,
            double averageAmount, double minAmount, double maxAmount, long uniqueCustomers) {
    }

    /**
     * Custom collector implementation using Collector interface.
     *
     * <p>
     * This collector aggregates order data into a CategorySummary for each
     * category.
     */
    public static class CategorySummaryCollector
            implements
                Collector<Order, CategorySummaryCollector.Accumulator, CategorySummary> {

        private final String category;

        public CategorySummaryCollector(String category) {
            this.category = category;
        }

        static class Accumulator {

            long count = 0;
            double total = 0;
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            Set<String> customers = ConcurrentHashMap.newKeySet();

            void add(Order order) {
                count++;
                total += order.amount();
                min = Math.min(min, order.amount());
                max = Math.max(max, order.amount());
                customers.add(order.customer());
            }

            Accumulator combine(Accumulator other) {
                count += other.count;
                total += other.total;
                min = Math.min(min, other.min);
                max = Math.max(max, other.max);
                customers.addAll(other.customers);
                return this;
            }
        }

        @Override
        public Supplier<Accumulator> supplier() {
            return Accumulator::new;
        }

        @Override
        public BiConsumer<Accumulator, Order> accumulator() {
            return Accumulator::add;
        }

        @Override
        public BinaryOperator<Accumulator> combiner() {
            return Accumulator::combine;
        }

        @Override
        public Function<Accumulator, CategorySummary> finisher() {
            return acc -> new CategorySummary(category, acc.count, acc.total,
                    acc.count > 0 ? acc.total / acc.count : 0.0, acc.count > 0 ? acc.min : 0.0,
                    acc.count > 0 ? acc.max : 0.0, acc.customers.size());
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of(Characteristics.UNORDERED);
        }
    }

    /**
     * Generates category summaries using custom collector.
     *
     * @param orders
     *            list of orders
     * @return map of category to summary
     */
    public static Map<String, CategorySummary> generateCategorySummaries(List<Order> orders) {
        return orders.stream().collect(Collectors.groupingBy(Order::category)).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream()
                        .collect(new CategorySummaryCollector(entry.getKey()))));
    }

    // ========================================================================
    // Parallel Streams
    // ========================================================================

    /**
     * Demonstrates parallel stream processing.
     *
     * <p>
     * Parallel streams split the workload across multiple threads. Use parallel
     * streams when:
     * <ul>
     * <li>Data set is large (thousands of elements)</li>
     * <li>Operations are CPU-intensive</li>
     * <li>Order doesn't matter</li>
     * <li>No shared mutable state</li>
     * </ul>
     *
     * <p>
     * Avoid parallel streams when:
     * <ul>
     * <li>Data set is small (overhead exceeds benefit)</li>
     * <li>Operations involve I/O (thread blocking)</li>
     * <li>Order must be preserved</li>
     * <li>Stream source is not easily splittable (e.g., LinkedList, I/O)</li>
     * </ul>
     *
     * @param orders
     *            list of orders
     * @return total amount using parallel processing
     */
    public static double calculateTotalParallel(List<Order> orders) {
        return orders.parallelStream().mapToDouble(Order::amount).sum();
    }

    /**
     * Groups orders by category using parallel stream.
     *
     * <p>
     * For grouping operations with large datasets, parallel streams can provide
     * significant performance improvements.
     *
     * @param orders
     *            list of orders
     * @return map of category to orders (concurrent map for thread safety)
     */
    public static Map<String, List<Order>> groupByCategoryParallel(List<Order> orders) {
        return orders.parallelStream().collect(Collectors.groupingByConcurrent(Order::category));
    }

    /**
     * Filters orders in parallel with stateless predicate.
     *
     * <p>
     * Best practice: Always use stateless, non-interfering predicates with parallel
     * streams.
     *
     * @param orders
     *            list of orders
     * @param minAmount
     *            minimum amount threshold
     * @return filtered list
     */
    public static List<Order> filterHighValueParallel(List<Order> orders, double minAmount) {
        return orders.parallelStream().filter(order -> order.amount() >= minAmount).toList();
    }

    /**
     * Demonstrates performance comparison between sequential and parallel streams.
     *
     * @param orders
     *            list of orders
     * @return performance metrics as a formatted string
     */
    public static String compareSequentialVsParallel(List<Order> orders) {
        // Warm up JIT
        for (int i = 0; i < 100; i++) {
            orders.stream().mapToDouble(Order::amount).sum();
            orders.parallelStream().mapToDouble(Order::amount).sum();
        }

        // Sequential timing
        long seqStart = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            orders.stream().mapToDouble(Order::amount).sum();
        }
        long seqTime = System.nanoTime() - seqStart;

        // Parallel timing
        long parStart = System.nanoTime();
        for (int i = 0; i < 1000; i++) {
            orders.parallelStream().mapToDouble(Order::amount).sum();
        }
        long parTime = System.nanoTime() - parStart;

        return String.format(
                "Sequential: %.3f ms, Parallel: %.3f ms (for %d orders, 1000 iterations)",
                seqTime / 1_000_000.0, parTime / 1_000_000.0, orders.size());
    }

    // ========================================================================
    // Report Generation
    // ========================================================================

    /**
     * Full order summary report record.
     *
     * @param totalOrders
     *            total number of orders
     * @param totalRevenue
     *            sum of all order amounts
     * @param categorySummaries
     *            summary for each category
     * @param topOrdersByCategory
     *            top 3 orders per category
     * @param highValueOrders
     *            orders above threshold
     * @param regularOrders
     *            orders below threshold
     */
    public record OrderReport(long totalOrders, double totalRevenue,
            Map<String, CategorySummary> categorySummaries,
            Map<String, List<Order>> topOrdersByCategory, List<Order> highValueOrders,
            List<Order> regularOrders) {
    }

    /**
     * Generates a comprehensive order analysis report.
     *
     * @param orders
     *            list of orders
     * @param highValueThreshold
     *            threshold for high-value classification
     * @return complete order report
     */
    public static OrderReport generateReport(List<Order> orders, double highValueThreshold) {
        Map<Boolean, List<Order>> partitioned = partitionByHighValue(orders, highValueThreshold);

        return new OrderReport(orders.size(), orders.stream().mapToDouble(Order::amount).sum(),
                generateCategorySummaries(orders), topNOrdersByCategory(orders, 3),
                partitioned.get(true), partitioned.get(false));
    }

    /**
     * Prints a formatted report to console.
     *
     * @param report
     *            the order report to print
     */
    public static void printReport(OrderReport report) {
        System.out.println("=== Order Analysis Report ===\n");

        System.out.printf("Total Orders: %d%n", report.totalOrders());
        System.out.printf("Total Revenue: $%.2f%n%n", report.totalRevenue());

        System.out.println("--- Category Summaries ---");
        report.categorySummaries().forEach((category, summary) -> {
            System.out.printf("%n%s:%n", category);
            System.out.printf("  Orders: %d%n", summary.orderCount());
            System.out.printf("  Total: $%.2f%n", summary.totalAmount());
            System.out.printf("  Average: $%.2f%n", summary.averageAmount());
            System.out.printf("  Min: $%.2f, Max: $%.2f%n", summary.minAmount(),
                    summary.maxAmount());
            System.out.printf("  Unique Customers: %d%n", summary.uniqueCustomers());
        });

        System.out.println("\n--- Top 3 Orders per Category ---");
        report.topOrdersByCategory().forEach((category, topOrders) -> {
            System.out.printf("%n%s:%n", category);
            topOrders.forEach(order -> System.out.printf("  %s%n", order.toFormattedString()));
        });

        System.out.println("\n--- High Value Orders ---");
        report.highValueOrders()
                .forEach(order -> System.out.printf("  %s%n", order.toFormattedString()));

        System.out.println("\n--- Regular Orders ---");
        report.regularOrders()
                .forEach(order -> System.out.printf("  %s%n", order.toFormattedString()));
    }

    // ========================================================================
    // Main Method - Demo
    // ========================================================================

    /**
     * Main method demonstrating all advanced stream operations.
     *
     * @param args
     *            command-line arguments (not used)
     */
    public static void main(String[] args) {
        List<Order> orders = createSampleOrders();

        System.out.println("=== Advanced Stream Operations Demo ===\n");

        // Demo 1: takeWhile and dropWhile
        System.out.println("--- takeWhile() and dropWhile() (Java 9+) ---\n");

        System.out.println("Orders with amount < $500 (takeWhile on sorted stream):");
        takeOrdersWhileBelowBudget(orders, 500.0)
                .forEach(o -> System.out.println("  " + o.toFormattedString()));

        System.out.println("\nOrders >= $500 (dropWhile on sorted stream):");
        dropOrdersBelowThreshold(orders, 500.0)
                .forEach(o -> System.out.println("  " + o.toFormattedString()));

        System.out.println("\nOrders in range $100-$1000:");
        getOrdersInAmountRange(orders, 100.0, 1000.0)
                .forEach(o -> System.out.println("  " + o.toFormattedString()));

        // Demo 2: groupingBy with downstream collectors
        System.out.println("\n--- groupingBy() with Downstream Collectors ---\n");

        System.out.println("Order count by category:");
        countOrdersByCategory(orders).forEach(
                (category, count) -> System.out.printf("  %s: %d orders%n", category, count));

        System.out.println("\nTotal amount by category:");
        totalAmountByCategory(orders)
                .forEach((category, total) -> System.out.printf("  %s: $%.2f%n", category, total));

        System.out.println("\nStatistics by category:");
        statisticsByCategory(orders).forEach((category, stats) -> System.out.printf(
                "  %s: count=%d, sum=$%.2f, avg=$%.2f, min=$%.2f, max=$%.2f%n", category,
                stats.getCount(), stats.getSum(), stats.getAverage(), stats.getMin(),
                stats.getMax()));

        System.out.println("\nUnique customers by category:");
        uniqueCustomersByCategory(orders).forEach(
                (category, customers) -> System.out.printf("  %s: %s%n", category, customers));

        System.out.println("\nHighest order by category:");
        highestOrderByCategory(orders).forEach((category, order) -> System.out.printf("  %s: %s%n",
                category, order != null ? order.toFormattedString() : "N/A"));

        // Demo 3: partitioningBy
        System.out.println("\n--- partitioningBy() ---\n");

        Map<Boolean, List<Order>> partitioned = partitionByHighValue(orders, 500.0);
        System.out.println("High-value orders (>= $500):");
        partitioned.get(true).forEach(o -> System.out.println("  " + o.toFormattedString()));
        System.out.println("\nRegular orders (< $500):");
        partitioned.get(false).forEach(o -> System.out.println("  " + o.toFormattedString()));

        Map<Boolean, Double> totals = partitionWithTotals(orders, 500.0);
        System.out.printf("%nHigh-value total: $%.2f%n", totals.get(true));
        System.out.printf("Regular total: $%.2f%n", totals.get(false));

        // Demo 4: Custom collectors - Running totals
        System.out.println("\n--- Custom Collectors ---\n");

        System.out.println("Running totals (sorted by date):");
        calculateRunningTotals(orders)
                .forEach(owrt -> System.out.printf("  %s -> Running Total: $%.2f%n",
                        owrt.order().toFormattedString(), owrt.runningTotal()));

        // Demo 5: Parallel streams
        System.out.println("\n--- Parallel Streams ---\n");

        System.out.println("Total (parallel): $" + calculateTotalParallel(orders));
        System.out.println("\nPerformance comparison (small dataset - parallel may be slower):");
        System.out.println(compareSequentialVsParallel(orders));

        // Demo 6: Full report
        System.out.println("\n");
        OrderReport report = generateReport(orders, 500.0);
        printReport(report);
    }
}
