package io.github.sps23.interview.preparation.typeinference

import java.time.LocalDate

/**
 * Demonstrates local variable type inference in Kotlin.
 *
 * Kotlin, like Scala, has supported type inference from its first release,
 * making it a natural comparison point for Java's var keyword (Java 10+).
 *
 * ## Key Points
 * - Kotlin uses `val` for immutable and `var` for mutable variables
 * - Type inference works for local variables, properties, and sometimes return types
 * - Kotlin's inference is more powerful than Java's var
 * - Best practice: use explicit types for public API
 */
object LocalVariableTypeInference {
    // ========================================================================
    // Data model for examples
    // ========================================================================

    /**
     * Represents a sales transaction for processing.
     *
     * Note: Kotlin data classes provide similar functionality to Java records.
     */
    data class Transaction(
        val id: Long,
        val product: String,
        val category: String,
        val amount: Double,
        val quantity: Int,
        val date: LocalDate,
        val region: String,
    ) {
        init {
            require(product.isNotBlank()) { "Product cannot be blank" }
            require(category.isNotBlank()) { "Category cannot be blank" }
            require(amount >= 0) { "Amount cannot be negative" }
            require(quantity > 0) { "Quantity must be positive" }
        }

        /** Calculates the total value of this transaction. */
        fun totalValue(): Double = amount * quantity
    }

    /**
     * Summary report for a category.
     */
    data class CategorySummary(
        val category: String,
        val transactionCount: Long,
        val totalAmount: Double,
        val averageAmount: Double,
    )

    // ========================================================================
    // Sample data
    // ========================================================================

    /**
     * Creates sample transaction data for demonstrations.
     */
    fun sampleTransactions(): List<Transaction> =
        listOf(
            Transaction(1, "Laptop", "Electronics", 1200.00, 2, LocalDate.of(2024, 1, 15), "North"),
            Transaction(2, "Mouse", "Electronics", 25.00, 10, LocalDate.of(2024, 1, 16), "South"),
            Transaction(3, "Desk Chair", "Furniture", 350.00, 3, LocalDate.of(2024, 1, 17), "North"),
            Transaction(4, "Monitor", "Electronics", 400.00, 5, LocalDate.of(2024, 1, 18), "East"),
            Transaction(5, "Bookshelf", "Furniture", 180.00, 2, LocalDate.of(2024, 1, 19), "West"),
            Transaction(6, "Keyboard", "Electronics", 75.00, 8, LocalDate.of(2024, 1, 20), "North"),
            Transaction(7, "Coffee Table", "Furniture", 220.00, 1, LocalDate.of(2024, 1, 21), "South"),
            Transaction(8, "Webcam", "Electronics", 90.00, 6, LocalDate.of(2024, 1, 22), "East"),
        )

    // ========================================================================
    // GOOD: When type inference improves readability
    // ========================================================================

    /**
     * Demonstrates good use of type inference - when the type is obvious from context.
     *
     * Unlike Java's var, Kotlin's val/var can infer types from:
     * - Constructor calls
     * - Literals
     * - Factory methods
     * - Method calls with clear return types
     */
    fun goodTypeInferenceObviousTypes() {
        // Type is obvious from constructor - inference is cleaner
        val transactions = mutableListOf<Transaction>()
        val categoryTotals = mutableMapOf<String, Double>()

        // Type is obvious from literal
        val count = 0
        val total = 0.0
        val message = "Processing transactions"

        // Type is obvious from factory method
        val today = LocalDate.now()
        val emptyList = emptyList<String>()

        println("Count: $count")
        println("Total: $total")
        println("Message: $message")
        println("Today: $today")
        println("Empty list size: ${emptyList.size}")
        println("Transactions list: ${transactions::class.simpleName}")
        println("Category totals map: ${categoryTotals::class.simpleName}")
    }

    /**
     * Demonstrates type inference with complex generic types.
     *
     * Kotlin's type inference handles complex nested generics seamlessly.
     */
    fun goodTypeInferenceWithGenerics() {
        val transactions = sampleTransactions()

        // Type inferred as Map<String, List<Transaction>>
        val byCategory = transactions.groupBy { it.category }

        // Nested generics - type inference handles complex structures
        val categoryToRegionMap =
            transactions.groupBy { it.category }
                .mapValues { (_, categoryTransactions) ->
                    categoryTransactions.groupBy { it.region }
                }

        println("Categories: ${byCategory.keys.joinToString(", ")}")
        println("Nested map structure: ${categoryToRegionMap.keys.joinToString(", ")}")
    }

    /**
     * Demonstrates type inference in loops and iterations.
     *
     * Kotlin's for loops work seamlessly with type inference.
     */
    fun goodTypeInferenceInLoops() {
        val transactions = sampleTransactions()

        // Type inferred in for loop
        println("Transactions by product:")
        for (transaction in transactions) {
            println("  ${transaction.product}: $${String.format("%.2f", transaction.amount)}")
        }

        // Type inferred with index
        println("\nTransaction IDs:")
        for ((index, transaction) in transactions.withIndex()) {
            println("  Index $index: ID ${transaction.id}")
        }
    }

    /**
     * Demonstrates type inference with destructuring.
     *
     * Kotlin's destructuring declarations work with inferred types.
     */
    fun goodTypeInferenceWithDestructuring() {
        val transactions = sampleTransactions()

        // Destructuring in map operation
        val productAmounts =
            transactions.map { (_, product, _, amount) ->
                product to amount
            }

        println("Product amounts:")
        productAmounts.forEach { (product, amount) ->
            println("  $product: $${String.format("%.2f", amount)}")
        }
    }

    // ========================================================================
    // val vs var: Immutability preference
    // ========================================================================

    /**
     * Demonstrates Kotlin's preference for immutability with val.
     *
     * Unlike Java where var is for type inference and final is for immutability,
     * Kotlin separates:
     * - `val` = immutable reference (preferred)
     * - `var` = mutable reference (use sparingly)
     *
     * Both support type inference.
     */
    fun valVsVarDemonstration() {
        // val: immutable reference (preferred)
        val immutableList = listOf(1, 2, 3)
        // immutableList = listOf(4, 5, 6) // Compilation error!

        // var: mutable reference (use sparingly)
        var counter = 0
        counter += 1 // OK

        // Idiomatic Kotlin: prefer val with transformation
        val doubled = immutableList.map { it * 2 }

        // Less idiomatic: using var for accumulation
        var sum = 0
        for (n in immutableList) sum += n

        // More idiomatic: using fold or sum
        val sumFunctional = immutableList.fold(0) { acc, n -> acc + n }
        val sumSimple = immutableList.sum()

        println("Doubled: $doubled")
        println("Sum (imperative): $sum")
        println("Sum (functional): $sumFunctional")
        println("Sum (simple): $sumSimple")
    }

    // ========================================================================
    // AVOID: When explicit types are better
    // ========================================================================

    /**
     * Demonstrates cases where explicit types improve readability.
     *
     * Best practice in Kotlin:
     * - Use explicit types for public API (function parameters and return types)
     * - Use inference for local variables
     * - Use explicit types when the inferred type might be surprising
     */
    fun whenExplicitTypesAreBetter() {
        val transactions = sampleTransactions()

        // AVOID: Type not obvious from context
        // val result = processData(transactions) // What type is result?

        // BETTER: Explicit type documents intent
        val summaries: List<CategorySummary> = generateCategorySummaries(transactions)
        println("Summaries generated: ${summaries.size}")

        // Kotlin has less numeric ambiguity than Java:
        // 100 is always Int, 100L is Long, 100.0 is Double

        // BETTER: Explicit type for API clarity
        val transactionId: Long = 100L
        val amount: Double = 100.0
        println("ID: $transactionId, Amount: $amount")

        // For public functions, always use explicit return types
        // fun processTransactions(ts: List<Transaction>): Map<String, CategorySummary>
    }

    // ========================================================================
    // Kotlin's advanced type inference capabilities
    // ========================================================================

    /**
     * Demonstrates Kotlin's type inference features.
     *
     * Kotlin can infer:
     * - Function return types (for single-expression functions)
     * - Generic type parameters
     * - Types in when expressions
     * - Types for lambda parameters
     */
    fun advancedTypeInference() {
        val transactions = sampleTransactions()

        // Lambda parameter types inferred
        val electronics = transactions.filter { it.category == "Electronics" }

        // Chained operations with full inference
        val topByAmount =
            transactions
                .sortedByDescending { it.amount }
                .take(3)
                .map { "${it.product}: ${it.amount}" }

        // Type inference with let, run, apply, also
        val formatted =
            transactions.firstOrNull()?.let {
                "${it.product}: $${it.amount}"
            } ?: "No transactions"

        // Type inference in when expression
        val highestAmount = transactions.maxByOrNull { it.amount }
        val tier =
            when {
                highestAmount == null -> "None"
                highestAmount.amount >= 1000 -> "Premium"
                highestAmount.amount >= 500 -> "Standard"
                else -> "Basic"
            }

        println("Electronics: ${electronics.map { it.product }.joinToString(", ")}")
        println("Top 3: ${topByAmount.joinToString(", ")}")
        println("First formatted: $formatted")
        println("Tier: $tier")
    }

    // ========================================================================
    // Data processing pipeline example
    // ========================================================================

    /**
     * Demonstrates a complete data processing pipeline using type inference.
     *
     * Shows how Kotlin's inference makes functional pipelines concise and readable.
     */
    fun processTransactionPipeline(transactions: List<Transaction>): Map<String, CategorySummary> {
        // Type inference throughout the pipeline
        val filteredTransactions = transactions.filter { it.amount >= 50.0 }

        val byCategory = filteredTransactions.groupBy { it.category }

        val summaries =
            byCategory.map { (category, categoryTransactions) ->
                // Local vals with inferred types
                val count = categoryTransactions.size.toLong()
                val total = categoryTransactions.sumOf { it.amount }
                val average = if (count > 0) total / count else 0.0

                category to CategorySummary(category, count, total, average)
            }.toMap()

        return summaries
    }

    /**
     * Generates category summaries from transactions.
     */
    fun generateCategorySummaries(transactions: List<Transaction>): List<CategorySummary> =
        processTransactionPipeline(transactions).values.toList()

    /**
     * Demonstrates the full pipeline with output.
     */
    fun demonstratePipeline() {
        val transactions = sampleTransactions()
        val summaries = processTransactionPipeline(transactions)

        println("Transaction Pipeline Results:")
        println("=============================")

        for ((category, summary) in summaries) {
            println("$category:")
            println("  Transactions: ${summary.transactionCount}")
            println("  Total: $${String.format("%.2f", summary.totalAmount)}")
            println("  Average: $${String.format("%.2f", summary.averageAmount)}")
        }
    }

    // ========================================================================
    // Comparison table summary
    // ========================================================================

    /**
     * Prints a comparison of Kotlin vs Java type inference.
     */
    fun printComparisonSummary() {
        val comparison =
            """
            Kotlin vs Java Type Inference Comparison:
            =========================================

            Feature                  | Kotlin        | Java (var)
            -------------------------|---------------|------------------
            Local variables          | val/var       | var
            Properties (fields)      | val/var       | NOT ALLOWED
            Function parameters      | NO            | NOT ALLOWED
            Return types             | YES (inferred)| NOT ALLOWED
            Immutability keyword     | val           | final (separate)
            Mutability keyword       | var           | var
            Lambda params            | YES           | NOT ALLOWED
            Destructuring            | YES           | YES (Java 21+)
            With null initializer    | YES           | NOT ALLOWED
            Smart casts              | YES           | NO
            """.trimIndent()

        println(comparison)
    }
}

// ========================================================================
// Main demonstration
// ========================================================================

fun main() {
    println("=== Kotlin Type Inference Demo ===\n")

    println("--- Good type inference: Obvious types ---\n")
    LocalVariableTypeInference.goodTypeInferenceObviousTypes()

    println("\n--- Good type inference: With generics ---\n")
    LocalVariableTypeInference.goodTypeInferenceWithGenerics()

    println("\n--- Good type inference: In loops ---\n")
    LocalVariableTypeInference.goodTypeInferenceInLoops()

    println("\n--- Good type inference: Destructuring ---\n")
    LocalVariableTypeInference.goodTypeInferenceWithDestructuring()

    println("\n--- val vs var: Immutability ---\n")
    LocalVariableTypeInference.valVsVarDemonstration()

    println("\n--- When explicit types are better ---\n")
    LocalVariableTypeInference.whenExplicitTypesAreBetter()

    println("\n--- Advanced type inference ---\n")
    LocalVariableTypeInference.advancedTypeInference()

    println("\n--- Data processing pipeline ---\n")
    LocalVariableTypeInference.demonstratePipeline()

    println("\n--- Comparison Summary ---\n")
    LocalVariableTypeInference.printComparisonSummary()
}
