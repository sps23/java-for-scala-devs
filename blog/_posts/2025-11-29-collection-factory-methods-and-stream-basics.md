---
layout: post
title: "Collection Factory Methods and Stream Basics"
description: "Master Java 9+ collection factories (List.of, Set.of, Map.of) and Stream API fundamentals - compare with Scala 3 collections and Kotlin stdlib approaches."
date: 2025-11-29 14:00:00 +0000
categories: interview
tags: java java21 scala kotlin collections streams interview-preparation
---

This is Part 3 of our Java 21 Interview Preparation series. We'll explore modern collection factory methods (Java 9+) and Stream API fundamentals, comparing them with Scala 3 and Kotlin approaches.

## The Problem: Processing Transactions

A common programming task involves processing collections of data: filtering by criteria, grouping by category, and calculating statistics. Let's see how this task is handled in modern Java 21, comparing with idiomatic Scala 3 and Kotlin solutions.

**Problem Statement:** Process a list of transactions: filter by amount, group by category, and calculate statistics.

## Collection Factory Methods (Java 9+)

Before Java 9, creating immutable collections was verbose:

```java
// Java 8 style - verbose and error-prone
List<String> list = Collections.unmodifiableList(Arrays.asList("a", "b", "c"));
Set<String> set = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("a", "b", "c")));
Map<String, Integer> map = Collections.unmodifiableMap(new HashMap<>() {{
    put("one", 1);
    put("two", 2);
}});
```

Java 9+ introduced elegant factory methods:

### List.of(), Set.of(), Map.of()

#### Java

```java
// Immutable list
List<String> list = List.of("a", "b", "c");

// Immutable set
Set<String> set = Set.of("a", "b", "c");

// Immutable map (up to 10 entries)
Map<String, Integer> map = Map.of(
    "one", 1,
    "two", 2,
    "three", 3
);

// Immutable map (any number of entries)
Map<String, Integer> largeMap = Map.ofEntries(
    Map.entry("one", 1),
    Map.entry("two", 2),
    // ... more entries
);
```

#### Scala 3

```scala
// Immutable by default
val list = List("a", "b", "c")

// Immutable set
val set = Set("a", "b", "c")

// Immutable map
val map = Map(
  "one" -> 1,
  "two" -> 2,
  "three" -> 3
)
```

#### Kotlin

```kotlin
// Read-only list
val list = listOf("a", "b", "c")

// Read-only set
val set = setOf("a", "b", "c")

// Read-only map
val map = mapOf(
    "one" to 1,
    "two" to 2,
    "three" to 3
)
```

### Key Characteristics

| Feature | Java 9+ | Scala 3 | Kotlin |
|---------|---------|---------|--------|
| Default mutability | Immutable with factory methods | Immutable | Read-only (immutable view) |
| Null elements | Not allowed | Allowed | Allowed |
| Modification | UnsupportedOperationException | New collection created | UnsupportedOperationException |
| Duplicate keys (Map) | IllegalArgumentException | Last value wins | Last value wins |

## Immutable vs Mutable Collections

Understanding the difference between immutable and mutable collections is crucial for writing thread-safe, predictable code.

### Java 21 vs Scala 3 vs Kotlin

#### Java 21

```java
// Mutable (traditional)
List<String> mutable = new ArrayList<>();
mutable.add("Apple");
mutable.add("Banana");

// Immutable (Java 9+)
List<String> immutable = List.of("Apple", "Banana");
// immutable.add("Cherry"); // UnsupportedOperationException!

// Convert immutable to mutable when needed
List<String> copy = new ArrayList<>(immutable);
copy.add("Cherry"); // OK
```

#### Scala 3

```scala
// Immutable (default)
val immutable = List("Apple", "Banana")

// To "modify", create new collection
val newList = immutable :+ "Cherry"
// Original unchanged!

// Mutable (explicit import)
import scala.collection.mutable.ListBuffer
val mutable = ListBuffer("Apple", "Banana")
mutable += "Cherry" // Modifies in place
```

#### Kotlin

```kotlin
// Read-only (default)
val readOnly = listOf("Apple", "Banana")

// To "modify", create new collection
val newList = readOnly + "Cherry"

// Mutable (explicit)
val mutable = mutableListOf("Apple", "Banana")
mutable.add("Cherry") // Modifies in place
```

## Stream API: Filter, Group, Statistics

### Filtering Transactions

#### Java 21

```java
// Java 8 style - mutable result
List<Transaction> filtered = transactions.stream()
    .filter(t -> t.amount() >= 50.0)
    .collect(Collectors.toList());

// Java 16+ style - immutable result
List<Transaction> filtered = transactions.stream()
    .filter(t -> t.amount() >= 50.0)
    .toList();
```

#### Scala 3

```scala
// Direct collection operations
val filtered = transactions
  .filter(_.amount >= 50.0)
// Result is immutable by default
```

#### Kotlin

```kotlin
// Direct collection operations
val filtered = transactions
    .filter { it.amount >= 50.0 }
// Result is read-only by default
```


### Grouping by Category

#### Java 21

```java
// Group transactions by category
Map<String, List<Transaction>> byCategory = 
    transactions.stream()
        .collect(Collectors.groupingBy(
            Transaction::category
        ));

// Calculate total per category
Map<String, Double> totals = 
    transactions.stream()
        .collect(Collectors.groupingBy(
            Transaction::category,
            Collectors.summingDouble(
                Transaction::amount
            )
        ));
```

#### Scala 3

```scala
// Group transactions by category
val byCategory = transactions
  .groupBy(_.category)

// Calculate total per category
// groupMapReduce: single-pass operation
val totals = transactions
  .groupMapReduce(_.category)(_.amount)(_ + _)
```

#### Kotlin

```kotlin
// Group transactions by category
val byCategory = transactions
    .groupBy { it.category }

// Calculate total per category
val totals = transactions
    .groupBy { it.category }
    .mapValues { (_, txns) -> 
        txns.sumOf { it.amount } 
    }
```

### Calculating Statistics

#### Java 21

```java
// DoubleSummaryStatistics provides:
// count, sum, min, max, average
DoubleSummaryStatistics stats = 
    transactions.stream()
        .mapToDouble(Transaction::amount)
        .summaryStatistics();

System.out.println("Count: " + stats.getCount());
System.out.println("Sum: " + stats.getSum());
System.out.println("Average: " + stats.getAverage());
System.out.println("Min: " + stats.getMin());
System.out.println("Max: " + stats.getMax());
```

#### Scala 3

```scala
// Calculate statistics manually
val amounts = transactions.map(_.amount)
val stats = Statistics(
  count = transactions.size,
  total = amounts.sum,
  average = amounts.sum / transactions.size,
  min = amounts.min,
  max = amounts.max
)
```

#### Kotlin

```kotlin
// Calculate statistics using stdlib
val amounts = transactions.map { it.amount }
val stats = Statistics(
    count = transactions.size,
    total = amounts.sum(),
    average = amounts.average(),
    min = amounts.min(),
    max = amounts.max()
)
```

## Collectors.teeing() (Java 12+)

`Collectors.teeing()` combines two collectors and merges their results in a single pass. This is particularly useful when you need multiple aggregate values.

### Finding Min and Max in One Pass

#### Java 21

```java
record MinMaxResult(
    Transaction min, 
    Transaction max
) {}

MinMaxResult result = transactions.stream()
    .collect(Collectors.teeing(
        Collectors.minBy(
            Comparator.comparingDouble(
                Transaction::amount
            )
        ),
        Collectors.maxBy(
            Comparator.comparingDouble(
                Transaction::amount
            )
        ),
        (minOpt, maxOpt) -> new MinMaxResult(
            minOpt.orElse(null),
            maxOpt.orElse(null)
        )
    ));
```

#### Scala 3

```scala
case class MinMaxResult(
  min: Option[Transaction],
  max: Option[Transaction]
)

// Simpler in Scala - no need for teeing
val result = MinMaxResult(
  min = transactions.minByOption(_.amount),
  max = transactions.maxByOption(_.amount)
)
```

#### Kotlin

```kotlin
data class MinMaxResult(
    val min: Transaction?,
    val max: Transaction?
)

// Simpler in Kotlin - no need for teeing
val result = MinMaxResult(
    min = transactions.minByOrNull { it.amount },
    max = transactions.maxByOrNull { it.amount }
)
```

### Combined Summary Statistics

```java
record SummaryResult(double total, long count, double average) {}

SummaryResult summary = transactions.stream()
    .collect(Collectors.teeing(
        Collectors.summingDouble(Transaction::amount),
        Collectors.counting(),
        (sum, count) -> new SummaryResult(
            sum,
            count,
            count > 0 ? sum / count : 0.0
        )
    ));
```

## Stream.toList() vs Collectors.toList()

Java 16 introduced `Stream.toList()` as a more concise alternative to `Collectors.toList()`:

| Method | Return Type | Mutability | Java Version |
|--------|-------------|------------|--------------|
| `Collectors.toList()` | ArrayList | Mutable | Java 8+ |
| `Stream.toList()` | Unmodifiable List | Immutable | Java 16+ |
| `Collectors.toUnmodifiableList()` | Unmodifiable List | Immutable | Java 10+ |

```java
// Java 8 style - returns mutable ArrayList
List<Transaction> mutableList = transactions.stream()
    .filter(t -> t.amount() > 50)
    .collect(Collectors.toList());
mutableList.add(newTransaction); // OK

// Java 16+ style - returns unmodifiable list
List<Transaction> immutableList = transactions.stream()
    .filter(t -> t.amount() > 50)
    .toList();
// immutableList.add(newTransaction); // UnsupportedOperationException!
```

**Recommendation:** Prefer `Stream.toList()` for new code when you don't need mutability.

## Transaction Data Model

Here's our Transaction record used in the examples:

### Java 21

```java
public record Transaction(
    long id,
    String category,
    double amount,
    String description,
    LocalDate date
) {
    public Transaction {
        if (id <= 0) throw new IllegalArgumentException(
            "ID must be positive");
        Objects.requireNonNull(category);
        if (amount <= 0) throw new IllegalArgumentException(
            "Amount must be positive");
    }
}
```

### Scala 3

```scala
case class Transaction(
    id: Long,
    category: String,
    amount: Double,
    description: String,
    date: LocalDate
):
  require(id > 0, "ID must be positive")
  require(category.nonEmpty, "Category required")
  require(amount > 0, "Amount must be positive")
```

### Kotlin

```kotlin
data class Transaction(
    val id: Long,
    val category: String,
    val amount: Double,
    val description: String,
    val date: LocalDate
) {
    init {
        require(id > 0) { "ID must be positive" }
        require(category.isNotBlank()) { "Category required" }
        require(amount > 0) { "Amount must be positive" }
    }
}
```

## Summary: Feature Comparison

| Feature | Java 8 | Java 21 | Scala 3 | Kotlin |
|---------|--------|---------|---------|--------|
| Immutable List | `Collections.unmodifiableList()` | `List.of()` | `List()` | `listOf()` |
| Immutable Set | `Collections.unmodifiableSet()` | `Set.of()` | `Set()` | `setOf()` |
| Immutable Map | `Collections.unmodifiableMap()` | `Map.of()` | `Map()` | `mapOf()` |
| Stream to List | `collect(toList())` | `toList()` | N/A (direct) | N/A (direct) |
| Group By | `groupingBy()` | `groupingBy()` | `groupBy()` | `groupBy()` |
| Sum By Group | `groupingBy(..., summingDouble())` | Same | `groupMapReduce()` | `groupBy().mapValues()` |
| Combined Collectors | N/A | `teeing()` | N/A (not needed) | N/A (not needed) |
| Statistics | `summaryStatistics()` | `summaryStatistics()` | Manual | Manual |

## Best Practices

1. **Prefer immutable collections** - Use `List.of()`, `Set.of()`, `Map.of()` for data that shouldn't change
2. **Use `Stream.toList()`** (Java 16+) - More concise and returns immutable list
3. **Consider `Collectors.teeing()`** (Java 12+) - When you need multiple aggregations in one pass
4. **Understand null handling** - Java factory methods don't allow nulls; Scala and Kotlin do
5. **Know mutability semantics** - Java's `List.of()` throws on modification; Kotlin's `listOf()` is a read-only view

## Code Samples

See the complete implementations in our repository:

- [Java 21 Transaction.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/collections/Transaction.java)
- [Java 21 TransactionProcessor.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/collections/TransactionProcessor.java)
- [Scala 3 Transaction.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/collections/Transaction.scala)
- [Scala 3 TransactionProcessor.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/collections/TransactionProcessor.scala)
- [Kotlin Transaction.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/collections/Transaction.kt)
- [Kotlin TransactionProcessor.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/collections/TransactionProcessor.kt)

## Conclusion

Java's collection APIs have evolved significantly from Java 8 to Java 21. The modern factory methods and Stream enhancements provide:

- **Cleaner code** with concise factory methods
- **Better immutability** with `List.of()`, `Set.of()`, `Map.of()`
- **Improved Stream API** with `toList()` and `teeing()`
- **More functional style** approaching Scala and Kotlin idioms

For Scala and Kotlin developers, modern Java feels more familiar. While Scala and Kotlin still offer advantages like default immutability and more expressive collection operations, Java 21 has significantly closed the gap.

---

*This is Part 3 of our Java 21 Interview Preparation series. Check out [Part 1: Immutable Data with Java Records](/interview/2025/11/28/immutable-data-with-java-records.html), [Part 2: String Manipulation with Modern APIs](/interview/2025/11/28/string-manipulation-with-modern-apis.html), and the [full preparation plan](/interview/2025/11/28/java21-interview-preparation-plan.html).*
