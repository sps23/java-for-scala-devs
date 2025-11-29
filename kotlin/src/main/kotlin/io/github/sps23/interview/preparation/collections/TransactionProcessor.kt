package io.github.sps23.interview.preparation.collections

import java.time.LocalDate

/**
 * Demonstrates Kotlin collection operations compared to Java's Stream API.
 *
 * Kotlin collections provide rich functional operations and clear distinction
 * between mutable and immutable types. This class shows how to:
 * - Create immutable collections (listOf, setOf, mapOf)
 * - Create mutable collections (mutableListOf, mutableSetOf, mutableMapOf)
 * - Filter, map, and group collections
 * - Calculate statistics
 * - Process transactions idiomatically
 *
 * Problem Statement: Process a list of transactions: filter by amount,
 * group by category, and calculate statistics.
 */
object TransactionProcessor {
    // ========================================================================
    // Collection Factory Methods
    // ========================================================================

    /**
     * Creates a sample list of transactions.
     *
     * Kotlin's `listOf()` creates an immutable (read-only) list.
     * Use `mutableListOf()` for mutable lists.
     *
     * @return immutable list of sample transactions
     */
    fun createSampleTransactions(): List<Transaction> =
        listOf(
            Transaction(1, "Food", 25.50, "Grocery shopping", LocalDate.of(2025, 1, 15)),
            Transaction(2, "Transport", 45.00, "Uber ride", LocalDate.of(2025, 1, 15)),
            Transaction(3, "Entertainment", 120.00, "Concert tickets", LocalDate.of(2025, 1, 16)),
            Transaction(4, "Food", 35.75, "Restaurant dinner", LocalDate.of(2025, 1, 17)),
            Transaction(5, "Transport", 30.00, "Train ticket", LocalDate.of(2025, 1, 18)),
            Transaction(6, "Food", 15.25, "Coffee shop", LocalDate.of(2025, 1, 18)),
            Transaction(7, "Entertainment", 60.00, "Movie night", LocalDate.of(2025, 1, 19)),
            Transaction(8, "Shopping", 250.00, "New shoes", LocalDate.of(2025, 1, 20)),
            Transaction(9, "Food", 42.50, "Takeout order", LocalDate.of(2025, 1, 20)),
            Transaction(10, "Transport", 55.00, "Taxi fare", LocalDate.of(2025, 1, 21)),
        )

    /**
     * Creates a set of valid categories.
     *
     * Kotlin's `setOf()` creates an immutable (read-only) set.
     *
     * @return immutable set of valid categories
     */
    fun getValidCategories(): Set<String> = setOf("Food", "Transport", "Entertainment", "Shopping", "Utilities", "Healthcare")

    /**
     * Creates a map of category descriptions.
     *
     * Kotlin's `mapOf()` creates an immutable (read-only) map.
     *
     * @return immutable map of category descriptions
     */
    fun getCategoryDescriptions(): Map<String, String> =
        mapOf(
            "Food" to "Food and dining expenses",
            "Transport" to "Transportation and travel costs",
            "Entertainment" to "Entertainment and leisure activities",
            "Shopping" to "Shopping and retail purchases",
            "Utilities" to "Utility bills and services",
        )

    /**
     * Creates a map of category budgets.
     *
     * @return immutable map of category budgets
     */
    fun getCategoryBudgets(): Map<String, Double> =
        mapOf(
            "Food" to 500.0,
            "Transport" to 200.0,
            "Entertainment" to 150.0,
            "Shopping" to 300.0,
            "Utilities" to 250.0,
            "Healthcare" to 100.0,
        )

    // ========================================================================
    // Collection Operations (Comparable to Java Stream API)
    // ========================================================================

    /**
     * Filters transactions by minimum amount.
     *
     * Equivalent to Java's `stream().filter(...).toList()`
     *
     * @param transactions list of transactions to filter
     * @param minAmount minimum amount threshold
     * @return filtered list of transactions
     */
    fun filterByMinAmount(
        transactions: List<Transaction>,
        minAmount: Double,
    ): List<Transaction> = transactions.filter { it.amount >= minAmount }

    /**
     * Filters transactions by category.
     *
     * @param transactions list of transactions to filter
     * @param category category to filter by
     * @return filtered list of transactions
     */
    fun filterByCategory(
        transactions: List<Transaction>,
        category: String,
    ): List<Transaction> = transactions.filter { it.category == category }

    /**
     * Groups transactions by category.
     *
     * Equivalent to Java's `Collectors.groupingBy()`
     *
     * @param transactions list of transactions to group
     * @return map of category to list of transactions
     */
    fun groupByCategory(transactions: List<Transaction>): Map<String, List<Transaction>> = transactions.groupBy { it.category }

    /**
     * Calculates total amount per category.
     *
     * Uses Kotlin's groupBy and mapValues for efficient computation.
     *
     * @param transactions list of transactions
     * @return map of category to total amount
     */
    fun calculateTotalByCategory(transactions: List<Transaction>): Map<String, Double> =
        transactions.groupBy { it.category }
            .mapValues { (_, txns) -> txns.sumOf { it.amount } }

    // ========================================================================
    // Statistics
    // ========================================================================

    /**
     * Result data class for summary statistics.
     *
     * @property count number of transactions
     * @property total total amount
     * @property average average amount
     * @property min minimum amount
     * @property max maximum amount
     */
    data class Statistics(
        val count: Int,
        val total: Double,
        val average: Double,
        val min: Double,
        val max: Double,
    )

    /**
     * Calculates statistics for all transactions.
     *
     * @param transactions list of transactions
     * @return Statistics with count, total, average, min, max
     */
    fun calculateStatistics(transactions: List<Transaction>): Statistics {
        if (transactions.isEmpty()) {
            return Statistics(0, 0.0, 0.0, 0.0, 0.0)
        }
        val amounts = transactions.map { it.amount }
        return Statistics(
            count = transactions.size,
            total = amounts.sum(),
            average = amounts.average(),
            min = amounts.min(),
            max = amounts.max(),
        )
    }

    /**
     * Calculates statistics per category.
     *
     * @param transactions list of transactions
     * @return map of category to statistics
     */
    fun calculateStatisticsByCategory(transactions: List<Transaction>): Map<String, Statistics> =
        transactions.groupBy { it.category }
            .mapValues { (_, txns) -> calculateStatistics(txns) }

    // ========================================================================
    // Min/Max (Comparable to Java's Collectors.teeing())
    // ========================================================================

    /**
     * Result data class for min/max transactions.
     *
     * @property min minimum transaction (or null if empty)
     * @property max maximum transaction (or null if empty)
     */
    data class MinMaxResult(
        val min: Transaction?,
        val max: Transaction?,
    )

    /**
     * Finds both min and max transactions.
     *
     * Kotlin provides minByOrNull and maxByOrNull for safe operations.
     *
     * @param transactions list of transactions
     * @return MinMaxResult with min and max transactions
     */
    fun findMinAndMaxTransaction(transactions: List<Transaction>): MinMaxResult =
        MinMaxResult(
            min = transactions.minByOrNull { it.amount },
            max = transactions.maxByOrNull { it.amount },
        )

    /**
     * Result data class for combined summary.
     *
     * @property total total amount
     * @property count number of transactions
     * @property average average amount
     */
    data class SummaryResult(
        val total: Double,
        val count: Int,
        val average: Double,
    )

    /**
     * Calculates summary statistics.
     *
     * Kotlin's fold can combine multiple computations in one pass.
     *
     * @param transactions list of transactions
     * @return SummaryResult with total, count, and average
     */
    fun calculateSummary(transactions: List<Transaction>): SummaryResult {
        val (total, count) =
            transactions.fold(0.0 to 0) { (sum, cnt), t ->
                (sum + t.amount) to (cnt + 1)
            }
        return SummaryResult(total, count, if (count > 0) total / count else 0.0)
    }

    // ========================================================================
    // Mutable vs Immutable Collections
    // ========================================================================

    /**
     * Demonstrates the difference between mutable and immutable collections in Kotlin.
     *
     * Kotlin distinguishes between read-only (List, Set, Map) and mutable
     * (MutableList, MutableSet, MutableMap) collection interfaces.
     */
    fun demonstrateImmutableVsMutable() {
        // Immutable (read-only) list
        val immutableList = listOf("Apple", "Banana", "Cherry")
        println("Immutable list: $immutableList")

        // To "modify", create a new list with added elements
        val newList = immutableList + "Date"
        println("New list with addition: $newList")
        println("Original unchanged: $immutableList")

        // Mutable list
        val mutableList = mutableListOf("Apple", "Banana", "Cherry")
        mutableList.add("Date")
        println("Mutable list after addition: $mutableList")

        // Immutable (read-only) map
        val immutableMap = mapOf("one" to 1, "two" to 2)
        println("Immutable map: $immutableMap")

        // To "modify", create a new map
        val newMap = immutableMap + ("three" to 3)
        println("New map with addition: $newMap")

        // Mutable map
        val mutableMap = mutableMapOf("one" to 1, "two" to 2)
        mutableMap["three"] = 3
        println("Mutable map after addition: $mutableMap")
    }

    // ========================================================================
    // Report Generation
    // ========================================================================

    /**
     * Processes transactions and prints a comprehensive report.
     *
     * @param transactions list of transactions to process
     */
    fun processAndPrintReport(transactions: List<Transaction>) {
        println("=== Transaction Processing Report ===\n")

        // 1. Filter high-value transactions
        println("--- High-Value Transactions (>$50) ---")
        filterByMinAmount(transactions, 50.0).forEach { t ->
            println("  ${t.toFormattedString()}")
        }

        // 2. Group by category
        println("\n--- Transactions by Category ---")
        groupByCategory(transactions).forEach { (category, txns) ->
            println("  $category:")
            txns.forEach { t -> println("    - ${t.toFormattedString()}") }
        }

        // 3. Calculate totals per category
        println("\n--- Total Spending by Category ---")
        calculateTotalByCategory(transactions).forEach { (category, total) ->
            println("  $category: ${"$%.2f".format(total)}")
        }

        // 4. Overall statistics
        println("\n--- Overall Statistics ---")
        val stats = calculateStatistics(transactions)
        println("  Count: ${stats.count} transactions")
        println("  Total: ${"$%.2f".format(stats.total)}")
        println("  Average: ${"$%.2f".format(stats.average)}")
        println("  Min: ${"$%.2f".format(stats.min)}")
        println("  Max: ${"$%.2f".format(stats.max)}")

        // 5. Min/Max transactions
        println("\n--- Min/Max Transactions ---")
        val minMax = findMinAndMaxTransaction(transactions)
        minMax.min?.let { println("  Minimum: ${it.toFormattedString()}") }
        minMax.max?.let { println("  Maximum: ${it.toFormattedString()}") }
    }
}

/**
 * Main function demonstrating all collection operations.
 */
fun main() {
    println("=== Kotlin Collection Operations Demo ===\n")

    // Demo 1: Collection factory methods
    println("--- Collection Factory Methods ---\n")

    println("listOf() - Sample transactions:")
    val transactions = TransactionProcessor.createSampleTransactions()
    transactions.forEach { t -> println("  ${t.toFormattedString()}") }

    println("\nsetOf() - Valid categories:")
    val categories = TransactionProcessor.getValidCategories()
    println("  $categories")

    println("\nmapOf() - Category descriptions:")
    TransactionProcessor.getCategoryDescriptions().forEach { (k, v) ->
        println("  $k: $v")
    }

    println("\nmapOf() - Category budgets:")
    TransactionProcessor.getCategoryBudgets().forEach { (k, v) ->
        println("  $k: ${"$%.2f".format(v)}")
    }

    // Demo 2: Mutable vs Immutable
    println("\n--- Mutable vs Immutable Collections ---\n")
    TransactionProcessor.demonstrateImmutableVsMutable()

    // Demo 3: Process transactions
    println("\n")
    TransactionProcessor.processAndPrintReport(transactions)

    // Demo 4: Summary
    println("\n--- Summary (comparable to Collectors.teeing) ---\n")
    val summary = TransactionProcessor.calculateSummary(transactions)
    println("Total: ${"$%.2f".format(summary.total)}")
    println("Count: ${summary.count}")
    println("Average: ${"$%.2f".format(summary.average)}")
}
