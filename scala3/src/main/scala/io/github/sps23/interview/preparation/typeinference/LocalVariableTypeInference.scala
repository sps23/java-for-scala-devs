package io.github.sps23.interview.preparation.typeinference

import java.time.LocalDate
import scala.collection.mutable

/** Demonstrates local variable type inference in Scala 3.
  *
  * Scala has supported type inference since its inception, making it a natural comparison point for
  * Java's var keyword (Java 10+).
  *
  * ==Key Points==
  *   - Scala uses `val` for immutable and `var` for mutable variables
  *   - Type inference works for local variables, fields, and return types
  *   - Scala's inference is more powerful than Java's var
  *   - Best practice: use explicit types for public API
  */
object LocalVariableTypeInference:

  // ========================================================================
  // Data model for examples
  // ========================================================================

  /** Represents a sales transaction for processing.
    *
    * Note: Scala case classes provide immutability by default, similar to Java records.
    */
  case class Transaction(
      id: Long,
      product: String,
      category: String,
      amount: Double,
      quantity: Int,
      date: LocalDate,
      region: String
  ):
    require(product.nonEmpty, "Product cannot be empty")
    require(category.nonEmpty, "Category cannot be empty")
    require(amount >= 0, "Amount cannot be negative")
    require(quantity > 0, "Quantity must be positive")

    /** Calculates the total value of this transaction. */
    def totalValue: Double = amount * quantity

  /** Summary report for a category. */
  case class CategorySummary(
      category: String,
      transactionCount: Long,
      totalAmount: Double,
      averageAmount: Double
  )

  // ========================================================================
  // Sample data
  // ========================================================================

  /** Creates sample transaction data for demonstrations. */
  def sampleTransactions: List[Transaction] = List(
    Transaction(1, "Laptop", "Electronics", 1200.00, 2, LocalDate.of(2024, 1, 15), "North"),
    Transaction(2, "Mouse", "Electronics", 25.00, 10, LocalDate.of(2024, 1, 16), "South"),
    Transaction(3, "Desk Chair", "Furniture", 350.00, 3, LocalDate.of(2024, 1, 17), "North"),
    Transaction(4, "Monitor", "Electronics", 400.00, 5, LocalDate.of(2024, 1, 18), "East"),
    Transaction(5, "Bookshelf", "Furniture", 180.00, 2, LocalDate.of(2024, 1, 19), "West"),
    Transaction(6, "Keyboard", "Electronics", 75.00, 8, LocalDate.of(2024, 1, 20), "North"),
    Transaction(7, "Coffee Table", "Furniture", 220.00, 1, LocalDate.of(2024, 1, 21), "South"),
    Transaction(8, "Webcam", "Electronics", 90.00, 6, LocalDate.of(2024, 1, 22), "East")
  )

  // ========================================================================
  // GOOD: When type inference improves readability
  // ========================================================================

  /** Demonstrates good use of type inference - when the type is obvious from context.
    *
    * Unlike Java's var, Scala's val/var can infer types from:
    *   - Constructor calls
    *   - Literals
    *   - Factory methods
    *   - Method calls with clear return types
    */
  def goodTypeInferenceObviousTypes(): Unit =
    // Type is obvious from constructor - inference is cleaner
    val transactions   = List.empty[Transaction]
    val categoryTotals = mutable.HashMap.empty[String, Double]

    // Type is obvious from literal
    val count   = 0
    val total   = 0.0
    val message = "Processing transactions"

    // Type is obvious from factory method
    val today     = LocalDate.now()
    val emptyList = List.empty[String]

    println(s"Count: $count")
    println(s"Total: $total")
    println(s"Message: $message")
    println(s"Today: $today")
    println(s"Empty list size: ${emptyList.size}")
    println(s"Transactions list: ${transactions.getClass.getSimpleName}")
    println(s"Category totals map: ${categoryTotals.getClass.getSimpleName}")

  /** Demonstrates type inference with complex generic types.
    *
    * Scala's type inference handles complex nested generics seamlessly, making code much cleaner
    * than explicitly typed equivalents.
    */
  def goodTypeInferenceWithGenerics(): Unit =
    val transactions = sampleTransactions

    // Type inferred as Map[String, List[Transaction]]
    val byCategory = transactions.groupBy(_.category)

    // Nested generics - type inference handles complex structures
    val categoryToRegionMap = transactions
      .groupBy(_.category)
      .view
      .mapValues(_.groupBy(_.region))
      .toMap

    println(s"Categories: ${byCategory.keys.mkString(", ")}")
    println(s"Nested map structure: ${categoryToRegionMap.keys.mkString(", ")}")

  /** Demonstrates type inference in loops and iterations.
    *
    * Scala's for-comprehensions work seamlessly with type inference.
    */
  def goodTypeInferenceInLoops(): Unit =
    val transactions = sampleTransactions

    // Type inferred in for-comprehension
    println("Transactions by product:")
    for transaction <- transactions do
      println(f"  ${transaction.product}: $$${transaction.amount}%.2f")

    // Type inferred in indexed iteration
    println("\nTransaction IDs:")
    for (transaction, i) <- transactions.zipWithIndex do
      println(f"  Index $i: ID ${transaction.id}")

  /** Demonstrates type inference with pattern matching.
    *
    * Scala's pattern matching works with inferred types, enabling concise destructuring.
    */
  def goodTypeInferenceWithPatternMatching(): Unit =
    val transactions = sampleTransactions

    // Type inference with pattern matching in map
    val productAmounts = transactions.map { case Transaction(_, product, _, amount, _, _, _) =>
      (product, amount)
    }

    println("Product amounts:")
    productAmounts.foreach { case (product, amount) =>
      println(f"  $product: $$$amount%.2f")
    }

  // ========================================================================
  // val vs var: Immutability preference
  // ========================================================================

  /** Demonstrates Scala's preference for immutability with val.
    *
    * Unlike Java where var is for type inference and final is for immutability, Scala separates:
    *   - `val` = immutable reference (preferred)
    *   - `var` = mutable reference (use sparingly)
    *
    * Both support type inference.
    */
  def valVsVarDemonstration(): Unit =
    // val: immutable reference (preferred)
    val immutableList = List(1, 2, 3)
    // immutableList = List(4, 5, 6) // Compilation error!

    // var: mutable reference (use sparingly)
    var counter = 0
    counter += 1 // OK

    // Idiomatic Scala: prefer val with transformation
    val doubled = immutableList.map(_ * 2)

    // Less idiomatic: using var for accumulation
    var sum = 0
    for n <- immutableList do sum += n

    // More idiomatic: using fold
    val sumFunctional = immutableList.foldLeft(0)(_ + _)

    println(s"Doubled: $doubled")
    println(s"Sum (imperative): $sum")
    println(s"Sum (functional): $sumFunctional")

  // ========================================================================
  // AVOID: When explicit types are better
  // ========================================================================

  /** Demonstrates cases where explicit types improve readability.
    *
    * Best practice in Scala:
    *   - Use explicit types for public API (def, val in traits/classes)
    *   - Use inference for local variables
    *   - Use explicit types when the inferred type might be surprising
    */
  def whenExplicitTypesAreBetter(): Unit =
    val transactions = sampleTransactions

    // AVOID: Type not obvious from context
    // val result = processData(transactions) // What type is result?

    // BETTER: Explicit type documents intent
    val summaries: List[CategorySummary] = generateCategorySummaries(transactions)
    println(s"Summaries generated: ${summaries.size}")

    // AVOID: Numeric literal ambiguity (less of an issue in Scala)
    // In Scala, 100 is always Int, 100L is Long

    // BETTER: Explicit type for API clarity
    val transactionId: Long = 100L
    val amount: Double      = 100.0
    println(s"ID: $transactionId, Amount: $amount")

    // For public methods, always use explicit return types
    // def processTransactions(ts: List[Transaction]): Map[String, CategorySummary] = ...

  // ========================================================================
  // Scala's advanced type inference capabilities
  // ========================================================================

  /** Demonstrates Scala's more powerful type inference vs Java.
    *
    * Scala can infer:
    *   - Method return types (though explicit is recommended for public API)
    *   - Generic type parameters
    *   - Types in pattern matching
    *   - Types for lambda parameters
    */
  def advancedTypeInference(): Unit =
    val transactions = sampleTransactions

    // Lambda parameter types inferred
    val electronics = transactions.filter(_.category == "Electronics")

    // Chained operations with full inference
    val topByAmount = transactions
      .sortBy(-_.amount)
      .take(3)
      .map(t => s"${t.product}: ${t.amount}")

    // Type inference with partial functions
    val amounts: List[Double] = transactions.collect {
      case t if t.amount > 100 =>
        t.amount
    }

    println(s"Electronics: ${electronics.map(_.product).mkString(", ")}")
    println(s"Top 3: ${topByAmount.mkString(", ")}")
    println(s"High amounts: ${amounts.mkString(", ")}")

  // ========================================================================
  // Data processing pipeline example
  // ========================================================================

  /** Demonstrates a complete data processing pipeline using type inference.
    *
    * Shows how Scala's inference makes functional pipelines concise and readable.
    */
  def processTransactionPipeline(
      transactions: List[Transaction]
  ): Map[String, CategorySummary] =
    // Type inference throughout the pipeline
    val filteredTransactions = transactions.filter(_.amount >= 50.0)

    val byCategory = filteredTransactions.groupBy(_.category)

    val summaries = byCategory.map { case (category, categoryTransactions) =>
      // Local vals with inferred types
      val count   = categoryTransactions.size.toLong
      val total   = categoryTransactions.map(_.amount).sum
      val average = if count > 0 then total / count else 0.0

      category -> CategorySummary(category, count, total, average)
    }

    summaries

  /** Generates category summaries from transactions. */
  def generateCategorySummaries(transactions: List[Transaction]): List[CategorySummary] =
    processTransactionPipeline(transactions).values.toList

  /** Demonstrates the full pipeline with output. */
  def demonstratePipeline(): Unit =
    val transactions = sampleTransactions
    val summaries    = processTransactionPipeline(transactions)

    println("Transaction Pipeline Results:")
    println("=============================")

    for (category, summary) <- summaries do
      println(s"$category:")
      println(s"  Transactions: ${summary.transactionCount}")
      println(f"  Total: $$${summary.totalAmount}%.2f")
      println(f"  Average: $$${summary.averageAmount}%.2f")

  // ========================================================================
  // Comparison table summary
  // ========================================================================

  /** Prints a comparison of Scala vs Java type inference. */
  def printComparisonSummary(): Unit =
    val comparison = """
      |Scala vs Java Type Inference Comparison:
      |=========================================
      |
      |Feature                  | Scala         | Java (var)
      |-------------------------|---------------|------------------
      |Local variables          | val/var       | var
      |Fields                   | val/var       | NOT ALLOWED
      |Method parameters        | NO            | NOT ALLOWED
      |Return types             | YES (inferred)| NOT ALLOWED
      |Immutability keyword     | val           | final (separate)
      |Mutability keyword       | var           | var
      |Lambda params            | YES           | NOT ALLOWED
      |Pattern match bindings   | YES           | YES (Java 21+)
      |With null initializer    | YES           | NOT ALLOWED
      |""".stripMargin

    println(comparison)

// ========================================================================
// Main demonstration
// ========================================================================

@main def runLocalVariableTypeInference(): Unit =
  import LocalVariableTypeInference.*

  println("=== Scala 3 Type Inference Demo ===\n")

  println("--- Good type inference: Obvious types ---\n")
  goodTypeInferenceObviousTypes()

  println("\n--- Good type inference: With generics ---\n")
  goodTypeInferenceWithGenerics()

  println("\n--- Good type inference: In loops ---\n")
  goodTypeInferenceInLoops()

  println("\n--- Good type inference: Pattern matching ---\n")
  goodTypeInferenceWithPatternMatching()

  println("\n--- val vs var: Immutability ---\n")
  valVsVarDemonstration()

  println("\n--- When explicit types are better ---\n")
  whenExplicitTypesAreBetter()

  println("\n--- Advanced type inference ---\n")
  advancedTypeInference()

  println("\n--- Data processing pipeline ---\n")
  demonstratePipeline()

  println("\n--- Comparison Summary ---\n")
  printComparisonSummary()
