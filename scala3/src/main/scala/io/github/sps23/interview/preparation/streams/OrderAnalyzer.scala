package io.github.sps23.interview.preparation.streams

import java.time.LocalDate
import scala.collection.immutable.{List, Map, Set}
import scala.math.Ordering.Double.TotalOrdering

/** Demonstrates advanced collection operations in Scala 3 compared to Java's Stream API.
  *
  * This object showcases:
  *   - takeWhile() and dropWhile() for conditional stream processing
  *   - groupBy() with various transformations (like Java's groupingBy + downstream)
  *   - partition() for binary partitioning (like Java's partitioningBy)
  *   - Custom aggregation functions (like Java's custom collectors)
  *   - Parallel collections and when to use them
  *
  * Problem Statement: Analyze a dataset of orders: calculate running totals, find top N by
  * category, and generate a summary report.
  */
object OrderAnalyzer:

  // ========================================================================
  // Sample Data Creation
  // ========================================================================

  /** Creates a sample list of orders for demonstration.
    *
    * @return
    *   immutable list of sample orders
    */
  def createSampleOrders: List[Order] =
    List(
      Order(1, "Electronics", 1299.99, "Alice", LocalDate.of(2025, 1, 15), List("Laptop", "Mouse")),
      Order(
        2,
        "Books",
        45.50,
        "Bob",
        LocalDate.of(2025, 1, 15),
        List("Java Programming", "Design Patterns")
      ),
      Order(3, "Electronics", 799.00, "Charlie", LocalDate.of(2025, 1, 16), List("Tablet")),
      Order(4, "Clothing", 125.00, "Alice", LocalDate.of(2025, 1, 17), List("Jacket", "Jeans")),
      Order(5, "Books", 32.99, "Diana", LocalDate.of(2025, 1, 18), List("Scala for Impatient")),
      Order(
        6,
        "Electronics",
        2499.99,
        "Eve",
        LocalDate.of(2025, 1, 18),
        List("Desktop Computer", "Monitor", "Keyboard")
      ),
      Order(7, "Clothing", 89.99, "Frank", LocalDate.of(2025, 1, 19), List("Shirt", "Tie")),
      Order(
        8,
        "Books",
        89.00,
        "Alice",
        LocalDate.of(2025, 1, 20),
        List("Kotlin in Action", "Effective Java", "Clean Code")
      ),
      Order(9, "Electronics", 349.99, "Bob", LocalDate.of(2025, 1, 20), List("Headphones")),
      Order(10, "Clothing", 250.00, "Charlie", LocalDate.of(2025, 1, 21), List("Suit")),
      Order(
        11,
        "Books",
        28.50,
        "Diana",
        LocalDate.of(2025, 1, 22),
        List("Functional Programming")
      ),
      Order(12, "Electronics", 599.00, "Eve", LocalDate.of(2025, 1, 22), List("Smartphone"))
    )

  // ========================================================================
  // takeWhile() and dropWhile()
  // ========================================================================

  /** Demonstrates takeWhile - takes elements while predicate is true.
    *
    * Scala's takeWhile is a direct collection operation (not tied to streams). It takes elements
    * from the collection as long as the predicate returns true.
    *
    * @param orders
    *   list of orders
    * @param maxAmount
    *   take orders while amount stays below this threshold
    * @return
    *   list of orders taken while condition is met
    */
  def takeOrdersWhileBelowBudget(orders: List[Order], maxAmount: Double): List[Order] =
    orders.sortBy(_.amount)(TotalOrdering).takeWhile(_.amount < maxAmount)

  /** Takes orders before a cutoff date from sorted list.
    *
    * @param orders
    *   list of orders
    * @param cutoffDate
    *   take orders before this date
    * @return
    *   orders before the cutoff date
    */
  def takeOrdersBeforeDate(orders: List[Order], cutoffDate: LocalDate): List[Order] =
    orders.sortBy(_.date)(using Ordering[LocalDate]).takeWhile(_.date.isBefore(cutoffDate))

  /** Demonstrates dropWhile - drops elements while predicate is true.
    *
    * dropWhile discards elements from the collection as long as the predicate returns true. Once
    * the predicate returns false, it returns all remaining elements.
    *
    * @param orders
    *   list of orders
    * @param threshold
    *   skip orders below this amount
    * @return
    *   orders starting from first order at or above threshold
    */
  def dropOrdersBelowThreshold(orders: List[Order], threshold: Double): List[Order] =
    orders.sortBy(_.amount)(TotalOrdering).dropWhile(_.amount < threshold)

  /** Combines takeWhile and dropWhile for range selection.
    *
    * @param orders
    *   list of orders
    * @param minAmount
    *   minimum amount
    * @param maxAmount
    *   maximum amount
    * @return
    *   orders in the specified amount range
    */
  def getOrdersInAmountRange(
      orders: List[Order],
      minAmount: Double,
      maxAmount: Double
  ): List[Order] =
    orders
      .sortBy(_.amount)(TotalOrdering)
      .dropWhile(_.amount < minAmount)
      .takeWhile(_.amount < maxAmount)

  // ========================================================================
  // groupBy with Various Transformations (like groupingBy + downstream)
  // ========================================================================

  /** Groups orders by category with count.
    *
    * Equivalent to Java's Collectors.groupingBy(Order::category, Collectors.counting())
    *
    * @param orders
    *   list of orders
    * @return
    *   map of category to order count
    */
  def countOrdersByCategory(orders: List[Order]): Map[String, Int] =
    orders.groupBy(_.category).view.mapValues(_.size).toMap

  /** Groups orders by category with total amount.
    *
    * Equivalent to Java's Collectors.groupingBy(Order::category,
    * Collectors.summingDouble(Order::amount))
    *
    * Scala's groupMapReduce provides an efficient single-pass operation.
    *
    * @param orders
    *   list of orders
    * @return
    *   map of category to total amount
    */
  def totalAmountByCategory(orders: List[Order]): Map[String, Double] =
    orders.groupMapReduce(_.category)(_.amount)(_ + _)

  /** Groups orders by category with statistics.
    *
    * @param orders
    *   list of orders
    * @return
    *   map of category to statistics
    */
  case class Statistics(
      count: Int,
      total: Double,
      average: Double,
      min: Double,
      max: Double
  )

  def statisticsByCategory(orders: List[Order]): Map[String, Statistics] =
    orders
      .groupBy(_.category)
      .view
      .mapValues { categoryOrders =>
        val amounts = categoryOrders.map(_.amount)
        Statistics(
          count = amounts.size,
          total = amounts.sum(using Numeric.DoubleIsFractional),
          average =
            if amounts.nonEmpty then amounts.sum(using Numeric.DoubleIsFractional) / amounts.size
            else 0.0,
          min = if amounts.nonEmpty then amounts.min(using TotalOrdering) else 0.0,
          max = if amounts.nonEmpty then amounts.max(using TotalOrdering) else 0.0
        )
      }
      .toMap

  /** Groups orders by category with list of customer names.
    *
    * Equivalent to Java's Collectors.groupingBy(Order::category,
    * Collectors.mapping(Order::customer, Collectors.toList()))
    *
    * @param orders
    *   list of orders
    * @return
    *   map of category to list of customer names
    */
  def customersByCategory(orders: List[Order]): Map[String, List[String]] =
    orders.groupBy(_.category).view.mapValues(_.map(_.customer)).toMap

  /** Groups orders by category with unique customers.
    *
    * @param orders
    *   list of orders
    * @return
    *   map of category to set of unique customer names
    */
  def uniqueCustomersByCategory(orders: List[Order]): Map[String, Set[String]] =
    orders.groupBy(_.category).view.mapValues(_.map(_.customer).toSet).toMap

  /** Nested grouping: by category then by customer.
    *
    * @param orders
    *   list of orders
    * @return
    *   nested map of category -> customer -> list of orders
    */
  def ordersByCategoryAndCustomer(orders: List[Order]): Map[String, Map[String, List[Order]]] =
    orders.groupBy(_.category).view.mapValues(_.groupBy(_.customer)).toMap

  /** Finds highest value order per category.
    *
    * @param orders
    *   list of orders
    * @return
    *   map of category to highest value order
    */
  def highestOrderByCategory(orders: List[Order]): Map[String, Option[Order]] =
    orders.groupBy(_.category).view.mapValues(_.maxByOption(_.amount)(TotalOrdering)).toMap

  /** Gets top N orders by amount for each category.
    *
    * @param orders
    *   list of orders
    * @param topN
    *   number of top orders per category
    * @return
    *   map of category to top N orders
    */
  def topNOrdersByCategory(orders: List[Order], topN: Int): Map[String, List[Order]] =
    orders
      .groupBy(_.category)
      .view
      .mapValues(categoryOrders => categoryOrders.sortBy(-_.amount)(TotalOrdering).take(topN))
      .toMap

  // ========================================================================
  // partition (like partitioningBy)
  // ========================================================================

  /** Partitions orders into high-value and regular orders.
    *
    * Scala's partition returns a tuple (matching, non-matching) unlike Java's Map<Boolean, List>.
    *
    * @param orders
    *   list of orders
    * @param threshold
    *   amount threshold
    * @return
    *   tuple of (high-value orders, regular orders)
    */
  def partitionByHighValue(orders: List[Order], threshold: Double): (List[Order], List[Order]) =
    orders.partition(_.amount >= threshold)

  /** Result case class for partition results (more type-safe than tuple). */
  case class PartitionResult(highValue: List[Order], regular: List[Order])

  /** Partitions orders with named result.
    *
    * @param orders
    *   list of orders
    * @param threshold
    *   amount threshold
    * @return
    *   PartitionResult with named fields
    */
  def partitionByHighValueNamed(orders: List[Order], threshold: Double): PartitionResult =
    val (high, regular) = orders.partition(_.amount >= threshold)
    PartitionResult(high, regular)

  /** Partitions orders by date.
    *
    * @param orders
    *   list of orders
    * @param cutoffDate
    *   date to partition around
    * @return
    *   tuple of (count on/after cutoff, count before cutoff)
    */
  def countByDatePartition(orders: List[Order], cutoffDate: LocalDate): (Int, Int) =
    val (onOrAfter, before) = orders.partition(!_.date.isBefore(cutoffDate))
    (onOrAfter.size, before.size)

  /** Partitions with totals.
    *
    * @param orders
    *   list of orders
    * @param threshold
    *   amount threshold
    * @return
    *   tuple of (high-value total, regular total)
    */
  def partitionWithTotals(orders: List[Order], threshold: Double): (Double, Double) =
    val (high, regular) = orders.partition(_.amount >= threshold)
    (
      high.map(_.amount).sum(using Numeric.DoubleIsFractional),
      regular.map(_.amount).sum(using Numeric.DoubleIsFractional)
    )

  // ========================================================================
  // Custom Aggregation Functions (like custom collectors)
  // ========================================================================

  /** Result case class for running total calculation.
    *
    * @param order
    *   the order
    * @param runningTotal
    *   cumulative total including this order
    */
  case class OrderWithRunningTotal(order: Order, runningTotal: Double)

  /** Calculates running totals using scanLeft.
    *
    * Scala's scanLeft is a powerful operation that produces all intermediate accumulation results.
    * This is more elegant than Java's custom collector approach.
    *
    * @param orders
    *   list of orders
    * @return
    *   list of orders with running totals
    */
  def calculateRunningTotals(orders: List[Order]): List[OrderWithRunningTotal] =
    val sorted = orders.sortBy(_.date)(using Ordering[LocalDate])
    sorted
      .scanLeft(0.0)((acc, order) => acc + order.amount)
      .tail // Remove initial 0.0
      .zip(sorted)
      .map { case (total, order) => OrderWithRunningTotal(order, total) }

  /** Category summary case class.
    *
    * @param category
    *   the category name
    * @param orderCount
    *   number of orders
    * @param totalAmount
    *   sum of all order amounts
    * @param averageAmount
    *   average order amount
    * @param minAmount
    *   minimum order amount
    * @param maxAmount
    *   maximum order amount
    * @param uniqueCustomers
    *   number of unique customers
    */
  case class CategorySummary(
      category: String,
      orderCount: Int,
      totalAmount: Double,
      averageAmount: Double,
      minAmount: Double,
      maxAmount: Double,
      uniqueCustomers: Int
  )

  /** Generates category summaries using foldLeft.
    *
    * Scala's foldLeft can aggregate multiple values in a single pass, similar to custom Java
    * collectors.
    *
    * @param orders
    *   list of orders
    * @return
    *   map of category to summary
    */
  def generateCategorySummaries(orders: List[Order]): Map[String, CategorySummary] =
    orders.groupBy(_.category).map { case (category, categoryOrders) =>
      val amounts   = categoryOrders.map(_.amount)
      val customers = categoryOrders.map(_.customer).distinct
      category -> CategorySummary(
        category    = category,
        orderCount  = categoryOrders.size,
        totalAmount = amounts.sum(using Numeric.DoubleIsFractional),
        averageAmount =
          if amounts.nonEmpty then amounts.sum(using Numeric.DoubleIsFractional) / amounts.size
          else 0.0,
        minAmount       = if amounts.nonEmpty then amounts.min(using TotalOrdering) else 0.0,
        maxAmount       = if amounts.nonEmpty then amounts.max(using TotalOrdering) else 0.0,
        uniqueCustomers = customers.size
      )
    }

  // ========================================================================
  // Sequential Processing (Note: Parallel collections require separate dependency)
  // ========================================================================

  /** Calculates total amount.
    *
    * Note: Scala 3 parallel collections require a separate dependency (scala-parallel-collections).
    * For simplicity, this example shows sequential processing.
    *
    * @param orders
    *   list of orders
    * @return
    *   total amount
    */
  def calculateTotal(orders: List[Order]): Double =
    orders.map(_.amount).sum(using Numeric.DoubleIsFractional)

  /** Groups orders by category.
    *
    * @param orders
    *   list of orders
    * @return
    *   map of category to orders
    */
  def groupByCategory(orders: List[Order]): Map[String, List[Order]] =
    orders.groupBy(_.category)

  /** Filters high value orders.
    *
    * @param orders
    *   list of orders
    * @param minAmount
    *   minimum amount threshold
    * @return
    *   filtered list
    */
  def filterHighValue(orders: List[Order], minAmount: Double): List[Order] =
    orders.filter(_.amount >= minAmount)

  /** Compares different collection processing approaches.
    *
    * @param orders
    *   list of orders
    * @return
    *   performance metrics as string
    */
  def compareProcessingApproaches(orders: List[Order]): String =
    // Warm up
    for _ <- 1 to 100 do
      orders.map(_.amount).sum(using Numeric.DoubleIsFractional)
      orders.view.map(_.amount).sum(using Numeric.DoubleIsFractional)

    // Direct collection timing
    val directStart = System.nanoTime()
    for _ <- 1 to 1000 do orders.map(_.amount).sum(using Numeric.DoubleIsFractional)
    val directTime = System.nanoTime() - directStart

    // View (lazy) timing
    val viewStart = System.nanoTime()
    for _ <- 1 to 1000 do orders.view.map(_.amount).sum(using Numeric.DoubleIsFractional)
    val viewTime = System.nanoTime() - viewStart

    f"Direct: ${directTime / 1_000_000.0}%.3f ms, View (lazy): ${viewTime / 1_000_000.0}%.3f ms (for ${orders.size} orders, 1000 iterations)"

  // ========================================================================
  // Report Generation
  // ========================================================================

  /** Full order report case class.
    *
    * @param totalOrders
    *   total number of orders
    * @param totalRevenue
    *   sum of all order amounts
    * @param categorySummaries
    *   summary for each category
    * @param topOrdersByCategory
    *   top 3 orders per category
    * @param highValueOrders
    *   orders above threshold
    * @param regularOrders
    *   orders below threshold
    */
  case class OrderReport(
      totalOrders: Int,
      totalRevenue: Double,
      categorySummaries: Map[String, CategorySummary],
      topOrdersByCategory: Map[String, List[Order]],
      highValueOrders: List[Order],
      regularOrders: List[Order]
  )

  /** Generates a comprehensive order analysis report.
    *
    * @param orders
    *   list of orders
    * @param highValueThreshold
    *   threshold for high-value classification
    * @return
    *   complete order report
    */
  def generateReport(orders: List[Order], highValueThreshold: Double): OrderReport =
    val (highValue, regular) = partitionByHighValue(orders, highValueThreshold)

    OrderReport(
      totalOrders         = orders.size,
      totalRevenue        = orders.map(_.amount).sum(using Numeric.DoubleIsFractional),
      categorySummaries   = generateCategorySummaries(orders),
      topOrdersByCategory = topNOrdersByCategory(orders, 3),
      highValueOrders     = highValue,
      regularOrders       = regular
    )

  /** Prints a formatted report to console.
    *
    * @param report
    *   the order report to print
    */
  def printReport(report: OrderReport): Unit =
    println("=== Order Analysis Report ===\n")
    println(s"Total Orders: ${report.totalOrders}")
    println(f"Total Revenue: $$${report.totalRevenue}%.2f\n")

    println("--- Category Summaries ---")
    report.categorySummaries.foreach { case (category, summary) =>
      println(s"\n$category:")
      println(s"  Orders: ${summary.orderCount}")
      println(f"  Total: $$${summary.totalAmount}%.2f")
      println(f"  Average: $$${summary.averageAmount}%.2f")
      println(f"  Min: $$${summary.minAmount}%.2f, Max: $$${summary.maxAmount}%.2f")
      println(s"  Unique Customers: ${summary.uniqueCustomers}")
    }

    println("\n--- Top 3 Orders per Category ---")
    report.topOrdersByCategory.foreach { case (category, topOrders) =>
      println(s"\n$category:")
      topOrders.foreach(order => println(s"  ${order.toFormattedString}"))
    }

    println("\n--- High Value Orders ---")
    report.highValueOrders.foreach(order => println(s"  ${order.toFormattedString}"))

    println("\n--- Regular Orders ---")
    report.regularOrders.foreach(order => println(s"  ${order.toFormattedString}"))

// Main entry point
@main def runOrderAnalyzer(): Unit =
  import OrderAnalyzer.*

  val orders = createSampleOrders

  println("=== Advanced Collection Operations Demo (Scala 3) ===\n")

  // Demo 1: takeWhile and dropWhile
  println("--- takeWhile() and dropWhile() ---\n")

  println("Orders with amount < $500 (takeWhile on sorted list):")
  takeOrdersWhileBelowBudget(orders, 500.0).foreach(o => println(s"  ${o.toFormattedString}"))

  println("\nOrders >= $500 (dropWhile on sorted list):")
  dropOrdersBelowThreshold(orders, 500.0).foreach(o => println(s"  ${o.toFormattedString}"))

  println("\nOrders in range $100-$1000:")
  getOrdersInAmountRange(orders, 100.0, 1000.0).foreach(o => println(s"  ${o.toFormattedString}"))

  // Demo 2: groupBy with transformations
  println("\n--- groupBy() with Transformations ---\n")

  println("Order count by category:")
  countOrdersByCategory(orders).foreach { case (category, count) =>
    println(s"  $category: $count orders")
  }

  println("\nTotal amount by category (using groupMapReduce):")
  totalAmountByCategory(orders).foreach { case (category, total) =>
    println(f"  $category: $$$total%.2f")
  }

  println("\nStatistics by category:")
  statisticsByCategory(orders).foreach { case (category, stats) =>
    println(
      f"  $category: count=${stats.count}, sum=$$${stats.total}%.2f, avg=$$${stats.average}%.2f, min=$$${stats.min}%.2f, max=$$${stats.max}%.2f"
    )
  }

  println("\nUnique customers by category:")
  uniqueCustomersByCategory(orders).foreach { case (category, customers) =>
    println(s"  $category: $customers")
  }

  println("\nHighest order by category:")
  highestOrderByCategory(orders).foreach { case (category, orderOpt) =>
    println(s"  $category: ${orderOpt.map(_.toFormattedString).getOrElse("N/A")}")
  }

  // Demo 3: partition
  println("\n--- partition() ---\n")

  val (highValue, regular) = partitionByHighValue(orders, 500.0)
  println("High-value orders (>= $500):")
  highValue.foreach(o => println(s"  ${o.toFormattedString}"))
  println("\nRegular orders (< $500):")
  regular.foreach(o => println(s"  ${o.toFormattedString}"))

  val (highTotal, regularTotal) = partitionWithTotals(orders, 500.0)
  println(f"\nHigh-value total: $$$highTotal%.2f")
  println(f"Regular total: $$$regularTotal%.2f")

  // Demo 4: Custom aggregations - Running totals
  println("\n--- Custom Aggregations (using scanLeft) ---\n")

  println("Running totals (sorted by date):")
  calculateRunningTotals(orders).foreach { owrt =>
    println(f"  ${owrt.order.toFormattedString} -> Running Total: $$${owrt.runningTotal}%.2f")
  }

  // Demo 5: Processing approaches
  println("\n--- Processing Approaches ---\n")

  println(s"Total: $$${calculateTotal(orders)}")
  println("\nPerformance comparison:")
  println(compareProcessingApproaches(orders))

  // Demo 6: Full report
  println("\n")
  val report = generateReport(orders, 500.0)
  printReport(report)
