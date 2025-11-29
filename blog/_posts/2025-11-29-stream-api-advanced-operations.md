---
layout: post
title: "Stream API Advanced Operations"
date: 2025-11-29 16:00:00 +0000
categories: interview
tags: java java21 scala kotlin streams collectors parallel interview-preparation
---

This is Part 4 of our Java 21 Interview Preparation series. We'll explore advanced Stream operations and collectors, comparing Java 21, Scala 3, and Kotlin approaches.

## The Problem: Analyzing Orders

A common programming task involves analyzing datasets: calculating running totals, finding top N items by category, and generating summary reports. Let's see how this is handled across our three JVM languages.

**Problem Statement:** Analyze a dataset of orders: calculate running totals, find top N by category, and generate a summary report.

## Order Data Model

Let's start with our Order model in all three languages:

### Java 21

```java
public record Order(
    long id,
    String category,
    double amount,
    String customer,
    LocalDate date,
    List<String> items
) {
    public Order {
        if (id <= 0) throw new IllegalArgumentException(
            "Order ID must be positive");
        Objects.requireNonNull(category);
        if (amount < 0) throw new IllegalArgumentException(
            "Amount cannot be negative");
        // Defensive copy for immutability
        items = List.copyOf(items);
    }
}
```

### Scala 3

```scala
case class Order(
    id: Long,
    category: String,
    amount: Double,
    customer: String,
    date: LocalDate,
    items: List[String]
):
  require(id > 0, s"Order ID must be positive")
  require(category.nonEmpty, "Category required")
  require(amount >= 0, "Amount cannot be negative")
```

### Kotlin

```kotlin
data class Order(
    val id: Long,
    val category: String,
    val amount: Double,
    val customer: String,
    val date: LocalDate,
    val items: List<String>
) {
    init {
        require(id > 0) { "Order ID must be positive" }
        require(category.isNotBlank()) { "Category required" }
        require(amount >= 0) { "Amount cannot be negative" }
    }
}
```

## Stream.takeWhile() and dropWhile() (Java 9+)

Java 9 introduced `takeWhile()` and `dropWhile()` for conditional stream processing. These operations work best with **ordered streams**.

### takeWhile() - Take While Condition is True

#### Java 21

```java
// Take orders while amount stays below threshold
public static List<Order> takeOrdersWhileBelowBudget(
        List<Order> orders, double maxAmount) {
    return orders.stream()
        .sorted(Comparator.comparingDouble(Order::amount))
        .takeWhile(order -> order.amount() < maxAmount)
        .toList();
}
```

#### Scala 3

```scala
// Scala's takeWhile is a direct collection operation
def takeOrdersWhileBelowBudget(
    orders: List[Order], 
    maxAmount: Double
): List[Order] =
  orders.sortBy(_.amount).takeWhile(_.amount < maxAmount)
```

#### Kotlin

```kotlin
// Kotlin's takeWhile is also a direct collection operation
fun takeOrdersWhileBelowBudget(
    orders: List<Order>,
    maxAmount: Double
): List<Order> = 
    orders.sortedBy { it.amount }
        .takeWhile { it.amount < maxAmount }
```

### dropWhile() - Skip While Condition is True

#### Java 21

```java
// Skip orders below threshold
public static List<Order> dropOrdersBelowThreshold(
        List<Order> orders, double threshold) {
    return orders.stream()
        .sorted(Comparator.comparingDouble(Order::amount))
        .dropWhile(order -> order.amount() < threshold)
        .toList();
}
```

#### Scala 3

```scala
def dropOrdersBelowThreshold(
    orders: List[Order], 
    threshold: Double
): List[Order] =
  orders.sortBy(_.amount).dropWhile(_.amount < threshold)
```

#### Kotlin

```kotlin
fun dropOrdersBelowThreshold(
    orders: List<Order>,
    threshold: Double
): List<Order> = 
    orders.sortedBy { it.amount }
        .dropWhile { it.amount < threshold }
```

### Combining takeWhile and dropWhile for Range Selection

```java
// Java: Select orders in amount range [$100, $1000)
public static List<Order> getOrdersInAmountRange(
        List<Order> orders, double minAmount, double maxAmount) {
    return orders.stream()
        .sorted(Comparator.comparingDouble(Order::amount))
        .dropWhile(order -> order.amount() < minAmount)
        .takeWhile(order -> order.amount() < maxAmount)
        .toList();
}
```

**Key Insight:** These operations are efficient because they stop processing once the condition changes. However, they rely on the stream being ordered correctly.

## Collectors.groupingBy() with Downstream Collectors

Java's `groupingBy()` collector becomes powerful when combined with downstream collectors.

### Basic Grouping with Count

#### Java 21

```java
// Count orders by category
Map<String, Long> countByCategory = orders.stream()
    .collect(Collectors.groupingBy(
        Order::category,
        Collectors.counting()
    ));
```

#### Scala 3

```scala
// Using groupBy with size
val countByCategory = orders
  .groupBy(_.category)
  .view.mapValues(_.size).toMap
```

#### Kotlin

```kotlin
val countByCategory = orders
    .groupBy { it.category }
    .mapValues { it.value.size }
```

### Grouping with Sum

#### Java 21

```java
// Total amount by category
Map<String, Double> totalByCategory = orders.stream()
    .collect(Collectors.groupingBy(
        Order::category,
        Collectors.summingDouble(Order::amount)
    ));
```

#### Scala 3

```scala
// Efficient single-pass with groupMapReduce
val totalByCategory = orders
  .groupMapReduce(_.category)(_.amount)(_ + _)
```

#### Kotlin

```kotlin
val totalByCategory = orders
    .groupBy { it.category }
    .mapValues { (_, orders) -> 
        orders.sumOf { it.amount } 
    }
```

### Grouping with Statistics

#### Java 21

```java
// Full statistics by category
Map<String, DoubleSummaryStatistics> stats = orders.stream()
    .collect(Collectors.groupingBy(
        Order::category,
        Collectors.summarizingDouble(Order::amount)
    ));

stats.forEach((category, s) -> 
    System.out.printf("%s: count=%d, sum=%.2f, avg=%.2f%n",
        category, s.getCount(), s.getSum(), s.getAverage()));
```

### Nested Grouping

#### Java 21

```java
// Group by category, then by customer
Map<String, Map<String, List<Order>>> nested = orders.stream()
    .collect(Collectors.groupingBy(
        Order::category,
        Collectors.groupingBy(Order::customer)
    ));
```

#### Scala 3

```scala
val nested = orders
  .groupBy(_.category)
  .view.mapValues(_.groupBy(_.customer)).toMap
```

#### Kotlin

```kotlin
val nested = orders
    .groupBy { it.category }
    .mapValues { (_, orders) -> 
        orders.groupBy { it.customer } 
    }
```

### Top N per Group

This is a common interview question: "Find the top 3 orders by amount in each category."

#### Java 21

```java
public static Map<String, List<Order>> topNOrdersByCategory(
        List<Order> orders, int topN) {
    return orders.stream()
        .collect(Collectors.groupingBy(
            Order::category,
            Collectors.collectingAndThen(
                Collectors.toList(),
                list -> list.stream()
                    .sorted(Comparator.comparingDouble(
                        Order::amount).reversed())
                    .limit(topN)
                    .toList()
            )
        ));
}
```

#### Scala 3

```scala
def topNOrdersByCategory(
    orders: List[Order], 
    topN: Int
): Map[String, List[Order]] =
  orders.groupBy(_.category).view.mapValues { categoryOrders =>
    categoryOrders.sortBy(-_.amount).take(topN)
  }.toMap
```

#### Kotlin

```kotlin
fun topNOrdersByCategory(
    orders: List<Order>,
    topN: Int
): Map<String, List<Order>> =
    orders.groupBy { it.category }
        .mapValues { (_, orders) ->
            orders.sortedByDescending { it.amount }.take(topN)
        }
```

## Collectors.partitioningBy()

`partitioningBy()` is a special case of grouping that creates exactly two groups based on a predicate.

### Basic Partitioning

#### Java 21

```java
// Partition into high-value (>=500) and regular orders
Map<Boolean, List<Order>> partitioned = orders.stream()
    .collect(Collectors.partitioningBy(
        order -> order.amount() >= 500.0
    ));

List<Order> highValue = partitioned.get(true);
List<Order> regular = partitioned.get(false);
```

#### Scala 3

```scala
// Returns a tuple (matching, non-matching)
val (highValue, regular) = orders.partition(_.amount >= 500.0)
```

#### Kotlin

```kotlin
// Returns a Pair (matching, non-matching)
val (highValue, regular) = orders.partition { it.amount >= 500.0 }
```

**Key Difference:** Java returns `Map<Boolean, List>`, while Scala and Kotlin return tuples/pairs which are more type-safe and explicit.

### Partitioning with Downstream Collector

```java
// Partition and sum each group
Map<Boolean, Double> totals = orders.stream()
    .collect(Collectors.partitioningBy(
        order -> order.amount() >= 500.0,
        Collectors.summingDouble(Order::amount)
    ));

System.out.println("High-value total: $" + totals.get(true));
System.out.println("Regular total: $" + totals.get(false));
```

## Custom Collectors

Custom collectors allow you to define exactly how stream elements are accumulated. This is useful for complex aggregations like running totals.

### Running Totals with Custom Collector

#### Java 21

```java
public record OrderWithRunningTotal(
    Order order, 
    double runningTotal
) {}

public static Collector<Order, ?, List<OrderWithRunningTotal>> 
        runningTotalCollector() {
    return Collector.of(
        // Supplier: create accumulator
        () -> {
            List<OrderWithRunningTotal> list = new ArrayList<>();
            return new Object[] { list, new double[] { 0.0 } };
        },
        // Accumulator: add each element
        (acc, order) -> {
            List<OrderWithRunningTotal> list = 
                (List<OrderWithRunningTotal>) acc[0];
            double[] total = (double[]) acc[1];
            total[0] += order.amount();
            list.add(new OrderWithRunningTotal(order, total[0]));
        },
        // Combiner: merge accumulators (for parallel)
        (acc1, acc2) -> {
            // Adjust second list's totals and merge
            // ... implementation details
            return acc1;
        },
        // Finisher: extract result
        acc -> (List<OrderWithRunningTotal>) acc[0]
    );
}

// Usage
List<OrderWithRunningTotal> withTotals = orders.stream()
    .sorted(Comparator.comparing(Order::date))
    .collect(runningTotalCollector());
```

#### Scala 3

```scala
// Scala's scanLeft is more elegant for running totals
case class OrderWithRunningTotal(order: Order, runningTotal: Double)

def calculateRunningTotals(
    orders: List[Order]
): List[OrderWithRunningTotal] =
  val sorted = orders.sortBy(_.date)
  sorted
    .scanLeft(0.0)((acc, order) => acc + order.amount)
    .tail  // Remove initial 0.0
    .zip(sorted)
    .map { case (total, order) => 
      OrderWithRunningTotal(order, total) 
    }
```

#### Kotlin

```kotlin
// Kotlin's scan is similar to Scala's scanLeft
data class OrderWithRunningTotal(
    val order: Order,
    val runningTotal: Double
)

fun calculateRunningTotals(
    orders: List<Order>
): List<OrderWithRunningTotal> {
    val sorted = orders.sortedBy { it.date }
    val runningTotals = sorted
        .scan(0.0) { acc, order -> acc + order.amount }
        .drop(1)
    return sorted.zip(runningTotals) { order, total ->
        OrderWithRunningTotal(order, total)
    }
}
```

**Key Insight:** Java requires verbose custom collectors, while Scala and Kotlin have built-in `scan`/`scanLeft` operations that elegantly handle running calculations.

## Parallel Streams and When to Use Them

### When to Use Parallel Streams

✅ **Good candidates:**
- Large datasets (thousands of elements)
- CPU-intensive operations (complex calculations)
- Independent, stateless operations
- Data in easily splittable structures (ArrayList, arrays)

❌ **Avoid when:**
- Small datasets (overhead exceeds benefit)
- I/O operations (thread blocking)
- Order matters (forEachOrdered adds overhead)
- Shared mutable state exists
- Source is not easily splittable (LinkedList, I/O streams)

### Java Parallel Stream Example

```java
// Simple parallel sum
double total = orders.parallelStream()
    .mapToDouble(Order::amount)
    .sum();

// Parallel grouping with concurrent map
Map<String, List<Order>> byCategory = orders.parallelStream()
    .collect(Collectors.groupingByConcurrent(Order::category));
```

### Performance Comparison

```java
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
        "Sequential: %.3f ms, Parallel: %.3f ms",
        seqTime / 1_000_000.0, parTime / 1_000_000.0);
}
```

**Warning:** For small datasets, parallel streams are often **slower** due to thread management overhead!

## Summary Report Generation

Putting it all together, here's a complete report generator:

### Java 21

```java
public record CategorySummary(
    String category,
    long orderCount,
    double totalAmount,
    double averageAmount,
    double minAmount,
    double maxAmount,
    long uniqueCustomers
) {}

public record OrderReport(
    long totalOrders,
    double totalRevenue,
    Map<String, CategorySummary> categorySummaries,
    Map<String, List<Order>> topOrdersByCategory,
    List<Order> highValueOrders,
    List<Order> regularOrders
) {}

public static OrderReport generateReport(
        List<Order> orders, double highValueThreshold) {
    Map<Boolean, List<Order>> partitioned = 
        partitionByHighValue(orders, highValueThreshold);

    return new OrderReport(
        orders.size(),
        orders.stream().mapToDouble(Order::amount).sum(),
        generateCategorySummaries(orders),
        topNOrdersByCategory(orders, 3),
        partitioned.get(true),
        partitioned.get(false)
    );
}
```

## Feature Comparison Table

| Feature | Java 21 | Scala 3 | Kotlin |
|---------|---------|---------|--------|
| takeWhile/dropWhile | `stream().takeWhile()` | `list.takeWhile()` | `list.takeWhile {}` |
| Group by | `groupingBy()` | `groupBy()` | `groupBy {}` |
| Partition | `partitioningBy()` → Map<Boolean> | `partition()` → Tuple | `partition {}` → Pair |
| Downstream collectors | Extensive library | mapValues/transform | mapValues/transform |
| Custom collectors | `Collector.of()` | foldLeft/scanLeft | fold/scan |
| Running totals | Custom collector | `scanLeft` | `scan` |
| Parallel | `.parallelStream()` | `.par` (separate lib) | Coroutines preferred |

## Best Practices

1. **Use takeWhile/dropWhile with sorted streams** - They rely on order for predictable results
2. **Combine groupingBy with downstream collectors** - More efficient than separate operations
3. **Prefer partitioningBy for binary splits** - Clearer intent than filter + filter-not
4. **Consider scan operations for running calculations** - Scala/Kotlin's scan is cleaner than custom Java collectors
5. **Benchmark before parallelizing** - Parallel streams have overhead; measure first
6. **Use thread-safe collectors with parallel streams** - `groupingByConcurrent()` instead of `groupingBy()`

## Code Samples

See the complete implementations in our repository:

- [Java 21 Order.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/streams/Order.java)
- [Java 21 OrderAnalyzer.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/streams/OrderAnalyzer.java)
- [Scala 3 Order.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/streams/Order.scala)
- [Scala 3 OrderAnalyzer.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/streams/OrderAnalyzer.scala)
- [Kotlin Order.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/streams/Order.kt)
- [Kotlin OrderAnalyzer.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/streams/OrderAnalyzer.kt)

## Conclusion

Java's Stream API has evolved to include powerful operations like `takeWhile()`, `dropWhile()`, and sophisticated collectors. While Scala and Kotlin often provide more concise syntax (especially for operations like `scan` and `partition`), Java 21 offers comprehensive functionality through its collector framework.

For complex data processing pipelines:
- **Java** excels with its extensive collector library and clear parallel stream support
- **Scala** shines with its expressive operations like `groupMapReduce` and `scanLeft`
- **Kotlin** offers clean syntax with features like `scan` while integrating well with Java's Stream API when needed

Choose based on your team's expertise and the specific requirements of your data processing pipeline.

---

*This is Part 4 of our Java 21 Interview Preparation series. Check out [Part 1: Immutable Data with Java Records](/interview/2025/11/26/immutable-data-with-java-records.html), [Part 2: String Manipulation with Modern APIs](/interview/2025/11/28/string-manipulation-with-modern-apis.html), [Part 3: Collection Factory Methods and Stream Basics](/interview/2025/11/29/collection-factory-methods-and-stream-basics.html), and the [full preparation plan](/interview/2025/11/25/java21-interview-preparation-plan.html).*
