package io.github.sps23.interview.preparation.collections

import java.time.LocalDate
import scala.collection.immutable.{List, Map, Set}

/** Demonstrates Scala 3 collection operations compared to Java's Stream API.
  *
  * Scala collections are immutable by default and provide rich functional operations. This class
  * shows how to:
  *   - Create immutable collections (List, Set, Map)
  *   - Filter, map, and group collections
  *   - Calculate statistics
  *   - Process transactions idiomatically
  *
  * Problem Statement: Process a list of transactions: filter by amount, group by category, and
  * calculate statistics.
  */
object TransactionProcessor:

  // ========================================================================
  // Collection Factory Methods
  // ========================================================================

  /** Creates a sample list of transactions.
    *
    * Scala's List is immutable by default. Use `List(...)` or `List.apply(...)` to create lists.
    *
    * @return
    *   immutable list of sample transactions
    */
  def createSampleTransactions: List[Transaction] =
    List(
      Transaction(1, "Food", 25.50, "Grocery shopping", LocalDate.of(2025, 1, 15)),
      Transaction(2, "Transport", 45.00, "Uber ride", LocalDate.of(2025, 1, 15)),
      Transaction(3, "Entertainment", 120.00, "Concert tickets", LocalDate.of(2025, 1, 16)),
      Transaction(4, "Food", 35.75, "Restaurant dinner", LocalDate.of(2025, 1, 17)),
      Transaction(5, "Transport", 30.00, "Train ticket", LocalDate.of(2025, 1, 18)),
      Transaction(6, "Food", 15.25, "Coffee shop", LocalDate.of(2025, 1, 18)),
      Transaction(7, "Entertainment", 60.00, "Movie night", LocalDate.of(2025, 1, 19)),
      Transaction(8, "Shopping", 250.00, "New shoes", LocalDate.of(2025, 1, 20)),
      Transaction(9, "Food", 42.50, "Takeout order", LocalDate.of(2025, 1, 20)),
      Transaction(10, "Transport", 55.00, "Taxi fare", LocalDate.of(2025, 1, 21))
    )

  /** Creates a set of valid categories.
    *
    * Scala's Set is immutable by default. Use `Set(...)` to create sets.
    *
    * @return
    *   immutable set of valid categories
    */
  def getValidCategories: Set[String] =
    Set("Food", "Transport", "Entertainment", "Shopping", "Utilities", "Healthcare")

  /** Creates a map of category descriptions.
    *
    * Scala's Map is immutable by default. Use `Map(k -> v, ...)` to create maps.
    *
    * @return
    *   immutable map of category descriptions
    */
  def getCategoryDescriptions: Map[String, String] =
    Map(
      "Food"          -> "Food and dining expenses",
      "Transport"     -> "Transportation and travel costs",
      "Entertainment" -> "Entertainment and leisure activities",
      "Shopping"      -> "Shopping and retail purchases",
      "Utilities"     -> "Utility bills and services"
    )

  /** Creates a map of category budgets.
    *
    * @return
    *   immutable map of category budgets
    */
  def getCategoryBudgets: Map[String, Double] =
    Map(
      "Food"          -> 500.0,
      "Transport"     -> 200.0,
      "Entertainment" -> 150.0,
      "Shopping"      -> 300.0,
      "Utilities"     -> 250.0,
      "Healthcare"    -> 100.0
    )

  // ========================================================================
  // Collection Operations (Comparable to Java Stream API)
  // ========================================================================

  /** Filters transactions by minimum amount.
    *
    * Equivalent to Java's `stream().filter(...).toList()`
    *
    * @param transactions
    *   list of transactions to filter
    * @param minAmount
    *   minimum amount threshold
    * @return
    *   filtered list of transactions
    */
  def filterByMinAmount(transactions: List[Transaction], minAmount: Double): List[Transaction] =
    transactions.filter(_.amount >= minAmount)

  /** Filters transactions by category.
    *
    * @param transactions
    *   list of transactions to filter
    * @param category
    *   category to filter by
    * @return
    *   filtered list of transactions
    */
  def filterByCategory(transactions: List[Transaction], category: String): List[Transaction] =
    transactions.filter(_.category == category)

  /** Groups transactions by category.
    *
    * Equivalent to Java's `Collectors.groupingBy()`
    *
    * @param transactions
    *   list of transactions to group
    * @return
    *   map of category to list of transactions
    */
  def groupByCategory(transactions: List[Transaction]): Map[String, List[Transaction]] =
    transactions.groupBy(_.category)

  /** Calculates total amount per category.
    *
    * Uses Scala's groupMapReduce for efficient single-pass computation.
    *
    * @param transactions
    *   list of transactions
    * @return
    *   map of category to total amount
    */
  def calculateTotalByCategory(transactions: List[Transaction]): Map[String, Double] =
    transactions.groupMapReduce(_.category)(_.amount)(_ + _)

  // ========================================================================
  // Statistics
  // ========================================================================

  /** Result case class for summary statistics.
    *
    * @param count
    *   number of transactions
    * @param total
    *   total amount
    * @param average
    *   average amount
    * @param min
    *   minimum amount
    * @param max
    *   maximum amount
    */
  case class Statistics(
      count: Long,
      total: Double,
      average: Double,
      min: Double,
      max: Double
  )

  /** Calculates statistics for all transactions.
    *
    * @param transactions
    *   list of transactions
    * @return
    *   Statistics with count, total, average, min, max
    */
  def calculateStatistics(transactions: List[Transaction]): Statistics =
    if transactions.isEmpty then Statistics(0, 0.0, 0.0, 0.0, 0.0)
    else
      val amounts = transactions.map(_.amount)
      Statistics(
        count   = transactions.size,
        total   = amounts.sum,
        average = amounts.sum / transactions.size,
        min     = amounts.min,
        max     = amounts.max
      )

  /** Calculates statistics per category.
    *
    * @param transactions
    *   list of transactions
    * @return
    *   map of category to statistics
    */
  def calculateStatisticsByCategory(transactions: List[Transaction]): Map[String, Statistics] =
    transactions.groupBy(_.category).map { case (category, txns) =>
      category -> calculateStatistics(txns)
    }

  // ========================================================================
  // Min/Max (Comparable to Java's Collectors.teeing())
  // ========================================================================

  /** Result case class for min/max transactions.
    *
    * @param min
    *   minimum transaction (or None if empty)
    * @param max
    *   maximum transaction (or None if empty)
    */
  case class MinMaxResult(min: Option[Transaction], max: Option[Transaction])

  /** Finds both min and max transactions.
    *
    * Unlike Java's Collectors.teeing(), Scala can easily compute this with pattern matching.
    *
    * @param transactions
    *   list of transactions
    * @return
    *   MinMaxResult with min and max transactions
    */
  def findMinAndMaxTransaction(transactions: List[Transaction]): MinMaxResult =
    MinMaxResult(
      min = transactions.minByOption(_.amount),
      max = transactions.maxByOption(_.amount)
    )

  /** Result case class for combined summary.
    *
    * @param total
    *   total amount
    * @param count
    *   number of transactions
    * @param average
    *   average amount
    */
  case class SummaryResult(total: Double, count: Long, average: Double)

  /** Calculates summary statistics.
    *
    * Scala's foldLeft can combine multiple computations in one pass.
    *
    * @param transactions
    *   list of transactions
    * @return
    *   SummaryResult with total, count, and average
    */
  def calculateSummary(transactions: List[Transaction]): SummaryResult =
    val (total, count) = transactions.foldLeft((0.0, 0L)) { case ((sum, cnt), t) =>
      (sum + t.amount, cnt + 1)
    }
    SummaryResult(total, count, if count > 0 then total / count else 0.0)

  // ========================================================================
  // Mutable vs Immutable Collections
  // ========================================================================

  /** Demonstrates the difference between mutable and immutable collections in Scala.
    *
    * By default, Scala collections are immutable. Mutable variants must be explicitly imported from
    * scala.collection.mutable.
    */
  def demonstrateImmutableVsMutable(): Unit =
    // Immutable list (default)
    val immutableList = List("Apple", "Banana", "Cherry")
    println(s"Immutable list: $immutableList")

    // To "modify", create a new list with added elements
    val newList = immutableList :+ "Date"
    println(s"New list with addition: $newList")
    println(s"Original unchanged: $immutableList")

    // Mutable list (explicit import required)
    import scala.collection.mutable.ListBuffer
    val mutableList = ListBuffer("Apple", "Banana", "Cherry")
    mutableList += "Date"
    println(s"Mutable ListBuffer after addition: $mutableList")

    // Immutable map (default)
    val immutableMap = Map("one" -> 1, "two" -> 2)
    println(s"Immutable map: $immutableMap")

    // To "modify", create a new map
    val newMap = immutableMap + ("three" -> 3)
    println(s"New map with addition: $newMap")

  // ========================================================================
  // Report Generation
  // ========================================================================

  /** Processes transactions and prints a comprehensive report.
    *
    * @param transactions
    *   list of transactions to process
    */
  def processAndPrintReport(transactions: List[Transaction]): Unit =
    println("=== Transaction Processing Report ===\n")

    // 1. Filter high-value transactions
    println("--- High-Value Transactions (>$50) ---")
    filterByMinAmount(transactions, 50.0).foreach(t => println(s"  ${t.toFormattedString}"))

    // 2. Group by category
    println("\n--- Transactions by Category ---")
    groupByCategory(transactions).foreach { case (category, txns) =>
      println(s"  $category:")
      txns.foreach(t => println(s"    - ${t.toFormattedString}"))
    }

    // 3. Calculate totals per category
    println("\n--- Total Spending by Category ---")
    calculateTotalByCategory(transactions).foreach { case (category, total) =>
      println(f"  $category: $$$total%.2f")
    }

    // 4. Overall statistics
    println("\n--- Overall Statistics ---")
    val stats = calculateStatistics(transactions)
    println(f"  Count: ${stats.count} transactions")
    println(f"  Total: $$${stats.total}%.2f")
    println(f"  Average: $$${stats.average}%.2f")
    println(f"  Min: $$${stats.min}%.2f")
    println(f"  Max: $$${stats.max}%.2f")

    // 5. Min/Max transactions
    println("\n--- Min/Max Transactions ---")
    val minMax = findMinAndMaxTransaction(transactions)
    minMax.min.foreach(t => println(s"  Minimum: ${t.toFormattedString}"))
    minMax.max.foreach(t => println(s"  Maximum: ${t.toFormattedString}"))

@main def runTransactionProcessor(): Unit =
  import TransactionProcessor.*

  println("=== Scala 3 Collection Operations Demo ===\n")

  // Demo 1: Collection factory methods
  println("--- Collection Factory Methods ---\n")

  println("List() - Sample transactions:")
  val transactions = createSampleTransactions
  transactions.foreach(t => println(s"  ${t.toFormattedString}"))

  println("\nSet() - Valid categories:")
  val categories = getValidCategories
  println(s"  $categories")

  println("\nMap() - Category descriptions:")
  getCategoryDescriptions.foreach { case (k, v) =>
    println(s"  $k: $v")
  }

  println("\nMap() - Category budgets:")
  getCategoryBudgets.foreach { case (k, v) =>
    println(f"  $k: $$$v%.2f")
  }

  // Demo 2: Mutable vs Immutable
  println("\n--- Mutable vs Immutable Collections ---\n")
  demonstrateImmutableVsMutable()

  // Demo 3: Process transactions
  println("\n")
  processAndPrintReport(transactions)

  // Demo 4: Summary
  println("\n--- Summary (comparable to Collectors.teeing) ---\n")
  val summary = calculateSummary(transactions)
  println(f"Total: $$${summary.total}%.2f")
  println(f"Count: ${summary.count}")
  println(f"Average: $$${summary.average}%.2f")
