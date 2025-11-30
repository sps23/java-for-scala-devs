package io.github.sps23.interview.preparation.streams

import java.time.LocalDate

/**
 * Demonstrates advanced collection operations in Kotlin compared to Java's Stream API.
 *
 * This object showcases:
 * - takeWhile() and dropWhile() for conditional stream processing
 * - groupBy() with various transformations (like Java's groupingBy + downstream)
 * - partition() for binary partitioning (like Java's partitioningBy)
 * - Custom aggregation functions (like Java's custom collectors)
 * - Parallel processing considerations
 *
 * Problem Statement: Analyze a dataset of orders: calculate running totals,
 * find top N by category, and generate a summary report.
 */
object OrderAnalyzer {
    // ========================================================================
    // Sample Data Creation
    // ========================================================================

    /**
     * Creates a sample list of orders for demonstration.
     *
     * @return immutable list of sample orders
     */
    fun createSampleOrders(): List<Order> =
        listOf(
            Order(1, "Electronics", 1299.99, "Alice", LocalDate.of(2025, 1, 15), listOf("Laptop", "Mouse")),
            Order(2, "Books", 45.50, "Bob", LocalDate.of(2025, 1, 15), listOf("Java Programming", "Design Patterns")),
            Order(3, "Electronics", 799.00, "Charlie", LocalDate.of(2025, 1, 16), listOf("Tablet")),
            Order(4, "Clothing", 125.00, "Alice", LocalDate.of(2025, 1, 17), listOf("Jacket", "Jeans")),
            Order(5, "Books", 32.99, "Diana", LocalDate.of(2025, 1, 18), listOf("Scala for Impatient")),
            Order(
                6,
                "Electronics",
                2499.99,
                "Eve",
                LocalDate.of(2025, 1, 18),
                listOf("Desktop Computer", "Monitor", "Keyboard"),
            ),
            Order(7, "Clothing", 89.99, "Frank", LocalDate.of(2025, 1, 19), listOf("Shirt", "Tie")),
            Order(
                8,
                "Books",
                89.00,
                "Alice",
                LocalDate.of(2025, 1, 20),
                listOf("Kotlin in Action", "Effective Java", "Clean Code"),
            ),
            Order(9, "Electronics", 349.99, "Bob", LocalDate.of(2025, 1, 20), listOf("Headphones")),
            Order(10, "Clothing", 250.00, "Charlie", LocalDate.of(2025, 1, 21), listOf("Suit")),
            Order(11, "Books", 28.50, "Diana", LocalDate.of(2025, 1, 22), listOf("Functional Programming")),
            Order(12, "Electronics", 599.00, "Eve", LocalDate.of(2025, 1, 22), listOf("Smartphone")),
        )

    // ========================================================================
    // takeWhile() and dropWhile()
    // ========================================================================

    /**
     * Demonstrates takeWhile - takes elements while predicate is true.
     *
     * Kotlin's takeWhile is a direct collection operation. It takes elements
     * from the collection as long as the predicate returns true.
     *
     * @param orders list of orders
     * @param maxAmount take orders while amount stays below this threshold
     * @return list of orders taken while condition is met
     */
    fun takeOrdersWhileBelowBudget(
        orders: List<Order>,
        maxAmount: Double,
    ): List<Order> = orders.sortedBy { it.amount }.takeWhile { it.amount < maxAmount }

    /**
     * Takes orders before a cutoff date from sorted list.
     *
     * @param orders list of orders
     * @param cutoffDate take orders before this date
     * @return orders before the cutoff date
     */
    fun takeOrdersBeforeDate(
        orders: List<Order>,
        cutoffDate: LocalDate,
    ): List<Order> = orders.sortedBy { it.date }.takeWhile { it.date.isBefore(cutoffDate) }

    /**
     * Demonstrates dropWhile - drops elements while predicate is true.
     *
     * dropWhile discards elements from the collection as long as the predicate
     * returns true. Once the predicate returns false, it returns all remaining elements.
     *
     * @param orders list of orders
     * @param threshold skip orders below this amount
     * @return orders starting from first order at or above threshold
     */
    fun dropOrdersBelowThreshold(
        orders: List<Order>,
        threshold: Double,
    ): List<Order> = orders.sortedBy { it.amount }.dropWhile { it.amount < threshold }

    /**
     * Combines takeWhile and dropWhile for range selection.
     *
     * @param orders list of orders
     * @param minAmount minimum amount
     * @param maxAmount maximum amount
     * @return orders in the specified amount range
     */
    fun getOrdersInAmountRange(
        orders: List<Order>,
        minAmount: Double,
        maxAmount: Double,
    ): List<Order> =
        orders.sortedBy { it.amount }
            .dropWhile { it.amount < minAmount }
            .takeWhile { it.amount < maxAmount }

    // ========================================================================
    // groupBy with Various Transformations (like groupingBy + downstream)
    // ========================================================================

    /**
     * Groups orders by category with count.
     *
     * Equivalent to Java's Collectors.groupingBy(Order::category, Collectors.counting())
     *
     * @param orders list of orders
     * @return map of category to order count
     */
    fun countOrdersByCategory(orders: List<Order>): Map<String, Int> = orders.groupBy { it.category }.mapValues { it.value.size }

    /**
     * Groups orders by category with total amount.
     *
     * Equivalent to Java's Collectors.groupingBy(Order::category, Collectors.summingDouble(Order::amount))
     *
     * @param orders list of orders
     * @return map of category to total amount
     */
    fun totalAmountByCategory(orders: List<Order>): Map<String, Double> =
        orders.groupBy { it.category }
            .mapValues { (_, categoryOrders) -> categoryOrders.sumOf { it.amount } }

    /**
     * Groups orders by category with statistics.
     *
     * @param orders list of orders
     * @return map of category to statistics
     */
    data class Statistics(
        val count: Int,
        val total: Double,
        val average: Double,
        val min: Double,
        val max: Double,
    )

    fun statisticsByCategory(orders: List<Order>): Map<String, Statistics> =
        orders.groupBy { it.category }
            .mapValues { (_, categoryOrders) ->
                val amounts = categoryOrders.map { it.amount }
                Statistics(
                    count = amounts.size,
                    total = amounts.sum(),
                    average = if (amounts.isNotEmpty()) amounts.average() else 0.0,
                    min = amounts.minOrNull() ?: 0.0,
                    max = amounts.maxOrNull() ?: 0.0,
                )
            }

    /**
     * Groups orders by category with list of customer names.
     *
     * Equivalent to Java's Collectors.groupingBy(Order::category,
     * Collectors.mapping(Order::customer, Collectors.toList()))
     *
     * @param orders list of orders
     * @return map of category to list of customer names
     */
    fun customersByCategory(orders: List<Order>): Map<String, List<String>> =
        orders.groupBy { it.category }
            .mapValues { (_, categoryOrders) -> categoryOrders.map { it.customer } }

    /**
     * Groups orders by category with unique customers.
     *
     * @param orders list of orders
     * @return map of category to set of unique customer names
     */
    fun uniqueCustomersByCategory(orders: List<Order>): Map<String, Set<String>> =
        orders.groupBy { it.category }
            .mapValues { (_, categoryOrders) -> categoryOrders.map { it.customer }.toSet() }

    /**
     * Nested grouping: by category then by customer.
     *
     * @param orders list of orders
     * @return nested map of category -> customer -> list of orders
     */
    fun ordersByCategoryAndCustomer(orders: List<Order>): Map<String, Map<String, List<Order>>> =
        orders.groupBy { it.category }
            .mapValues { (_, categoryOrders) -> categoryOrders.groupBy { it.customer } }

    /**
     * Finds highest value order per category.
     *
     * @param orders list of orders
     * @return map of category to highest value order
     */
    fun highestOrderByCategory(orders: List<Order>): Map<String, Order?> =
        orders.groupBy { it.category }
            .mapValues { (_, categoryOrders) -> categoryOrders.maxByOrNull { it.amount } }

    /**
     * Gets top N orders by amount for each category.
     *
     * @param orders list of orders
     * @param topN number of top orders per category
     * @return map of category to top N orders
     */
    fun topNOrdersByCategory(
        orders: List<Order>,
        topN: Int,
    ): Map<String, List<Order>> =
        orders.groupBy { it.category }
            .mapValues { (_, categoryOrders) ->
                categoryOrders.sortedByDescending { it.amount }.take(topN)
            }

    // ========================================================================
    // partition (like partitioningBy)
    // ========================================================================

    /**
     * Partitions orders into high-value and regular orders.
     *
     * Kotlin's partition returns a Pair (matching, non-matching) unlike Java's Map<Boolean, List>.
     *
     * @param orders list of orders
     * @param threshold amount threshold
     * @return pair of (high-value orders, regular orders)
     */
    fun partitionByHighValue(
        orders: List<Order>,
        threshold: Double,
    ): Pair<List<Order>, List<Order>> = orders.partition { it.amount >= threshold }

    /**
     * Result data class for partition results (more type-safe than pair).
     */
    data class PartitionResult(
        val highValue: List<Order>,
        val regular: List<Order>,
    )

    /**
     * Partitions orders with named result.
     *
     * @param orders list of orders
     * @param threshold amount threshold
     * @return PartitionResult with named fields
     */
    fun partitionByHighValueNamed(
        orders: List<Order>,
        threshold: Double,
    ): PartitionResult {
        val (high, regular) = orders.partition { it.amount >= threshold }
        return PartitionResult(high, regular)
    }

    /**
     * Partitions orders by date.
     *
     * @param orders list of orders
     * @param cutoffDate date to partition around
     * @return pair of (count on/after cutoff, count before cutoff)
     */
    fun countByDatePartition(
        orders: List<Order>,
        cutoffDate: LocalDate,
    ): Pair<Int, Int> {
        val (onOrAfter, before) = orders.partition { !it.date.isBefore(cutoffDate) }
        return onOrAfter.size to before.size
    }

    /**
     * Partitions with totals.
     *
     * @param orders list of orders
     * @param threshold amount threshold
     * @return pair of (high-value total, regular total)
     */
    fun partitionWithTotals(
        orders: List<Order>,
        threshold: Double,
    ): Pair<Double, Double> {
        val (high, regular) = orders.partition { it.amount >= threshold }
        return high.sumOf { it.amount } to regular.sumOf { it.amount }
    }

    // ========================================================================
    // Custom Aggregation Functions (like custom collectors)
    // ========================================================================

    /**
     * Result data class for running total calculation.
     *
     * @property order the order
     * @property runningTotal cumulative total including this order
     */
    data class OrderWithRunningTotal(
        val order: Order,
        val runningTotal: Double,
    )

    /**
     * Calculates running totals using scan.
     *
     * Kotlin's scan is similar to Scala's scanLeft. It produces all intermediate
     * accumulation results.
     *
     * @param orders list of orders
     * @return list of orders with running totals
     */
    fun calculateRunningTotals(orders: List<Order>): List<OrderWithRunningTotal> {
        val sorted = orders.sortedBy { it.date }
        val runningTotals =
            sorted.scan(0.0) { acc, order -> acc + order.amount }
                .drop(1) // Remove initial 0.0
        return sorted.zip(runningTotals) { order, total ->
            OrderWithRunningTotal(order, total)
        }
    }

    /**
     * Category summary data class.
     *
     * @property category the category name
     * @property orderCount number of orders
     * @property totalAmount sum of all order amounts
     * @property averageAmount average order amount
     * @property minAmount minimum order amount
     * @property maxAmount maximum order amount
     * @property uniqueCustomers number of unique customers
     */
    data class CategorySummary(
        val category: String,
        val orderCount: Int,
        val totalAmount: Double,
        val averageAmount: Double,
        val minAmount: Double,
        val maxAmount: Double,
        val uniqueCustomers: Int,
    )

    /**
     * Generates category summaries using fold.
     *
     * Kotlin's fold can aggregate multiple values in a single pass.
     *
     * @param orders list of orders
     * @return map of category to summary
     */
    fun generateCategorySummaries(orders: List<Order>): Map<String, CategorySummary> =
        orders.groupBy { it.category }
            .mapValues { (category, categoryOrders) ->
                val amounts = categoryOrders.map { it.amount }
                val customers = categoryOrders.map { it.customer }.distinct()
                CategorySummary(
                    category = category,
                    orderCount = categoryOrders.size,
                    totalAmount = amounts.sum(),
                    averageAmount = if (amounts.isNotEmpty()) amounts.average() else 0.0,
                    minAmount = amounts.minOrNull() ?: 0.0,
                    maxAmount = amounts.maxOrNull() ?: 0.0,
                    uniqueCustomers = customers.size,
                )
            }

    // ========================================================================
    // Parallel Processing (using Coroutines would be idiomatic, but for comparison
    // we show Java-like parallel stream approach using asSequence)
    // ========================================================================

    /**
     * Calculates total amount.
     *
     * Note: Kotlin doesn't have built-in parallel collections like Scala.
     * For parallel processing, use Kotlin Coroutines (Flow) or Java parallel streams.
     * Here we show the sequential approach for fair comparison.
     *
     * @param orders list of orders
     * @return total amount
     */
    fun calculateTotal(orders: List<Order>): Double = orders.sumOf { it.amount }

    /**
     * Groups orders by category.
     *
     * @param orders list of orders
     * @return map of category to orders
     */
    fun groupByCategory(orders: List<Order>): Map<String, List<Order>> = orders.groupBy { it.category }

    /**
     * Filters high value orders.
     *
     * @param orders list of orders
     * @param minAmount minimum amount threshold
     * @return filtered list
     */
    fun filterHighValue(
        orders: List<Order>,
        minAmount: Double,
    ): List<Order> = orders.filter { it.amount >= minAmount }

    /**
     * Compares different processing approaches.
     *
     * @param orders list of orders
     * @return performance metrics as string
     */
    fun compareProcessingApproaches(orders: List<Order>): String {
        // Warm up
        repeat(100) {
            orders.sumOf { it.amount }
            orders.asSequence().sumOf { it.amount }
        }

        // Direct collection timing
        val directStart = System.nanoTime()
        repeat(1000) { orders.sumOf { it.amount } }
        val directTime = System.nanoTime() - directStart

        // Sequence timing (lazy evaluation)
        val seqStart = System.nanoTime()
        repeat(1000) { orders.asSequence().sumOf { it.amount } }
        val seqTime = System.nanoTime() - seqStart

        return "Direct: %.3f ms, Sequence: %.3f ms (for %d orders, 1000 iterations)".format(
            directTime / 1_000_000.0,
            seqTime / 1_000_000.0,
            orders.size,
        )
    }

    // ========================================================================
    // Report Generation
    // ========================================================================

    /**
     * Full order report data class.
     *
     * @property totalOrders total number of orders
     * @property totalRevenue sum of all order amounts
     * @property categorySummaries summary for each category
     * @property topOrdersByCategory top 3 orders per category
     * @property highValueOrders orders above threshold
     * @property regularOrders orders below threshold
     */
    data class OrderReport(
        val totalOrders: Int,
        val totalRevenue: Double,
        val categorySummaries: Map<String, CategorySummary>,
        val topOrdersByCategory: Map<String, List<Order>>,
        val highValueOrders: List<Order>,
        val regularOrders: List<Order>,
    )

    /**
     * Generates a comprehensive order analysis report.
     *
     * @param orders list of orders
     * @param highValueThreshold threshold for high-value classification
     * @return complete order report
     */
    fun generateReport(
        orders: List<Order>,
        highValueThreshold: Double,
    ): OrderReport {
        val (highValue, regular) = partitionByHighValue(orders, highValueThreshold)
        return OrderReport(
            totalOrders = orders.size,
            totalRevenue = orders.sumOf { it.amount },
            categorySummaries = generateCategorySummaries(orders),
            topOrdersByCategory = topNOrdersByCategory(orders, 3),
            highValueOrders = highValue,
            regularOrders = regular,
        )
    }

    /**
     * Prints a formatted report to console.
     *
     * @param report the order report to print
     */
    fun printReport(report: OrderReport) {
        println("=== Order Analysis Report ===\n")
        println("Total Orders: ${report.totalOrders}")
        println("Total Revenue: ${"$%.2f".format(report.totalRevenue)}\n")

        println("--- Category Summaries ---")
        report.categorySummaries.forEach { (category, summary) ->
            println("\n$category:")
            println("  Orders: ${summary.orderCount}")
            println("  Total: ${"$%.2f".format(summary.totalAmount)}")
            println("  Average: ${"$%.2f".format(summary.averageAmount)}")
            println("  Min: ${"$%.2f".format(summary.minAmount)}, Max: ${"$%.2f".format(summary.maxAmount)}")
            println("  Unique Customers: ${summary.uniqueCustomers}")
        }

        println("\n--- Top 3 Orders per Category ---")
        report.topOrdersByCategory.forEach { (category, topOrders) ->
            println("\n$category:")
            topOrders.forEach { order -> println("  ${order.toFormattedString()}") }
        }

        println("\n--- High Value Orders ---")
        report.highValueOrders.forEach { order -> println("  ${order.toFormattedString()}") }

        println("\n--- Regular Orders ---")
        report.regularOrders.forEach { order -> println("  ${order.toFormattedString()}") }
    }
}

/**
 * Main function demonstrating all collection operations.
 */
fun main() {
    val orders = OrderAnalyzer.createSampleOrders()

    println("=== Advanced Collection Operations Demo (Kotlin) ===\n")

    // Demo 1: takeWhile and dropWhile
    println("--- takeWhile() and dropWhile() ---\n")

    println("Orders with amount < \$500 (takeWhile on sorted list):")
    OrderAnalyzer.takeOrdersWhileBelowBudget(orders, 500.0).forEach { o ->
        println("  ${o.toFormattedString()}")
    }

    println("\nOrders >= \$500 (dropWhile on sorted list):")
    OrderAnalyzer.dropOrdersBelowThreshold(orders, 500.0).forEach { o ->
        println("  ${o.toFormattedString()}")
    }

    println("\nOrders in range \$100-\$1000:")
    OrderAnalyzer.getOrdersInAmountRange(orders, 100.0, 1000.0).forEach { o ->
        println("  ${o.toFormattedString()}")
    }

    // Demo 2: groupBy with transformations
    println("\n--- groupBy() with Transformations ---\n")

    println("Order count by category:")
    OrderAnalyzer.countOrdersByCategory(orders).forEach { (category, count) ->
        println("  $category: $count orders")
    }

    println("\nTotal amount by category:")
    OrderAnalyzer.totalAmountByCategory(orders).forEach { (category, total) ->
        println("  $category: ${"$%.2f".format(total)}")
    }

    println("\nStatistics by category:")
    OrderAnalyzer.statisticsByCategory(orders).forEach { (category, stats) ->
        println(
            "  $category: count=${stats.count}, sum=${"$%.2f".format(stats.total)}, " +
                "avg=${"$%.2f".format(stats.average)}, min=${"$%.2f".format(stats.min)}, " +
                "max=${"$%.2f".format(stats.max)}",
        )
    }

    println("\nUnique customers by category:")
    OrderAnalyzer.uniqueCustomersByCategory(orders).forEach { (category, customers) ->
        println("  $category: $customers")
    }

    println("\nHighest order by category:")
    OrderAnalyzer.highestOrderByCategory(orders).forEach { (category, order) ->
        println("  $category: ${order?.toFormattedString() ?: "N/A"}")
    }

    // Demo 3: partition
    println("\n--- partition() ---\n")

    val (highValue, regular) = OrderAnalyzer.partitionByHighValue(orders, 500.0)
    println("High-value orders (>= \$500):")
    highValue.forEach { o -> println("  ${o.toFormattedString()}") }
    println("\nRegular orders (< \$500):")
    regular.forEach { o -> println("  ${o.toFormattedString()}") }

    val (highTotal, regularTotal) = OrderAnalyzer.partitionWithTotals(orders, 500.0)
    println("\nHigh-value total: ${"$%.2f".format(highTotal)}")
    println("Regular total: ${"$%.2f".format(regularTotal)}")

    // Demo 4: Custom aggregations - Running totals
    println("\n--- Custom Aggregations (using scan) ---\n")

    println("Running totals (sorted by date):")
    OrderAnalyzer.calculateRunningTotals(orders).forEach { owrt ->
        println("  ${owrt.order.toFormattedString()} -> Running Total: ${"$%.2f".format(owrt.runningTotal)}")
    }

    // Demo 5: Processing approaches
    println("\n--- Processing Approaches ---\n")

    println("Total: ${"$%.2f".format(OrderAnalyzer.calculateTotal(orders))}")
    println("\nPerformance comparison:")
    println(OrderAnalyzer.compareProcessingApproaches(orders))

    // Demo 6: Full report
    println("\n")
    val report = OrderAnalyzer.generateReport(orders, 500.0)
    OrderAnalyzer.printReport(report)
}
