---
layout: post
title: "Local Variable Type Inference with var"
description: "Use Java 10+ var keyword effectively - learn best practices for type inference, readability guidelines, and comparisons with Scala 3 and Kotlin type inference."
date: 2025-11-29 21:00:00 +0000
categories: [interview]
tags: [java, java21, scala, kotlin, type-inference, var, interview-preparation]
---

This is part of our Java 21 Interview Preparation series. We'll explore local variable type inference using Java's `var` keyword (Java 10+), comparing it with Scala 3's and Kotlin's approach to type inference.

## The Problem: Reducing Boilerplate While Maintaining Readability

Java has traditionally required explicit type declarations for all variables. Java 10 introduced the `var` keyword for local variable type inference, reducing verbosity while maintaining static typing. Let's explore when to use `var` and best practices for readable code.

**Problem Statement:** Refactor a data processing pipeline using appropriate `var` declarations while maintaining readability.

## Data Model

Let's start with our Transaction model in all three languages:

### Java 21

```java
public record Transaction(
    long id,
    String product,
    String category,
    double amount,
    int quantity,
    LocalDate date,
    String region) {

    public Transaction {
        Objects.requireNonNull(product, "Product cannot be null");
        Objects.requireNonNull(category, "Category cannot be null");
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }

    public double totalValue() {
        return amount * quantity;
    }
}
```

### Scala 3

```scala
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
  require(amount >= 0, "Amount cannot be negative")
  
  def totalValue: Double = amount * quantity
```

### Kotlin

```kotlin
data class Transaction(
    val id: Long,
    val product: String,
    val category: String,
    val amount: Double,
    val quantity: Int,
    val date: LocalDate,
    val region: String
) {
    init {
        require(product.isNotBlank()) { "Product cannot be blank" }
        require(amount >= 0) { "Amount cannot be negative" }
    }

    fun totalValue(): Double = amount * quantity
}
```

## When var Improves Readability

### 1. Constructor Calls with Obvious Types

When the type is obvious from the constructor, `var` reduces redundancy.

#### Java 21

```java
// Without var: redundant type declaration
ArrayList<Transaction> transactions = new ArrayList<Transaction>();
HashMap<String, Double> categoryTotals = new HashMap<String, Double>();

// With var: cleaner, type is obvious from constructor
var transactions = new ArrayList<Transaction>();
var categoryTotals = new HashMap<String, Double>();
```

#### Scala 3

```scala
// Type inference is the default - no explicit type needed
val transactions = List.empty[Transaction]
val categoryTotals = mutable.HashMap.empty[String, Double]
```

#### Kotlin

```kotlin
// Type inference is the default
val transactions = mutableListOf<Transaction>()
val categoryTotals = mutableMapOf<String, Double>()
```

### 2. Complex Generic Types

`var` significantly reduces noise with nested generics.

#### Java 21

```java
// Without var: verbose and hard to read
Map<String, List<Transaction>> byCategory = transactions.stream()
    .collect(Collectors.groupingBy(Transaction::category));

// With var: cleaner, type is clear from stream operation
var byCategory = transactions.stream()
    .collect(Collectors.groupingBy(Transaction::category));

// Nested generics - var is even more helpful
var categoryToRegionMap = transactions.stream()
    .collect(Collectors.groupingBy(
        Transaction::category,
        Collectors.groupingBy(Transaction::region)));
```

#### Scala 3

```scala
// Type inferred as Map[String, List[Transaction]]
val byCategory = transactions.groupBy(_.category)

// Nested generics handled seamlessly
val categoryToRegionMap = transactions.groupBy(_.category)
  .view.mapValues(_.groupBy(_.region)).toMap
```

#### Kotlin

```kotlin
// Type inferred as Map<String, List<Transaction>>
val byCategory = transactions.groupBy { it.category }

// Nested generics handled seamlessly
val categoryToRegionMap = transactions.groupBy { it.category }
    .mapValues { (_, txns) -> txns.groupBy { it.region } }
```

### 3. Loops and Iterations

#### Java 21

```java
// var works well in for-each loops
for (var transaction : transactions) {
    System.out.printf("%s: $%.2f%n", transaction.product(), transaction.amount());
}

// var in traditional for loops
for (var i = 0; i < transactions.size(); i++) {
    System.out.printf("Index %d: ID %d%n", i, transactions.get(i).id());
}
```

#### Scala 3

```scala
// Type inferred in for-comprehension
for transaction <- transactions do
  println(f"${transaction.product}: $$${transaction.amount}%.2f")

// With index
for (transaction, i) <- transactions.zipWithIndex do
  println(f"Index $i: ID ${transaction.id}")
```

#### Kotlin

```kotlin
// Type inferred in for loop
for (transaction in transactions) {
    println("${transaction.product}: $${transaction.amount}")
}

// With index
for ((index, transaction) in transactions.withIndex()) {
    println("Index $index: ID ${transaction.id}")
}
```

### 4. Try-with-Resources (Java 10+)

```java
try (var reader = new BufferedReader(new FileReader("data.txt"))) {
    var line = reader.readLine();
    while (line != null) {
        processLine(line);
        line = reader.readLine();
    }
}
```

## When Explicit Types Are Better

### 1. Non-Obvious Return Types

When the return type isn't clear from the method name, explicit types improve readability.

```java
// AVOID: What type is result?
var result = processor.process(transactions);

// BETTER: Explicit type documents intent
List<CategorySummary> summaries = processor.process(transactions);
```

### 2. Numeric Literals with Potential Ambiguity

```java
// Could be confusing - is this int, long, double?
var value = 100;

// BETTER: Be explicit for clarity
long transactionId = 100L;
double amount = 100.0;
```

### 3. Chained Method Calls Where Type Isn't Clear

```java
// AVOID: Hard to know the intermediate and final types
var x = someObject.getProcessor().process().getResult();

// BETTER: Break into steps or use explicit type
ProcessResult result = someObject.getProcessor()
    .process()
    .getResult();
```

## Where var Cannot Be Used (Java)

Java's `var` has specific limitations that don't exist in Scala or Kotlin:

```java
// ❌ Fields - NOT ALLOWED
private var fieldValue = 10;  // Compilation error!

// ❌ Method parameters - NOT ALLOWED
public void process(var data) { }  // Compilation error!

// ❌ Return types - NOT ALLOWED
public var getData() { return List.of(); }  // Compilation error!

// ❌ Without initializer - NOT ALLOWED
var uninitializedValue;  // Compilation error!

// ❌ With null initializer - NOT ALLOWED
var nullValue = null;  // Compilation error!

// ❌ Lambda expressions - NOT ALLOWED
var comparator = (a, b) -> a - b;  // Compilation error!

// ❌ Array initializers - NOT ALLOWED
var numbers = {1, 2, 3};  // Compilation error!
```

## var with Diamond Operator

When using `var` with the diamond operator, be careful about type inference:

```java
// ✅ Type argument specified - infers ArrayList<Transaction>
var transactions = new ArrayList<Transaction>();

// ⚠️ Diamond with no context - infers ArrayList<Object>
var list = new ArrayList<>();  // Becomes ArrayList<Object>!

// ✅ Better: specify the type parameter
var explicitList = new ArrayList<String>();
```

## val vs var: Immutability (Scala/Kotlin)

A key difference between Java and Scala/Kotlin is how they handle mutability:

| Aspect | Java | Scala | Kotlin |
|--------|------|-------|--------|
| Type inference | `var` | Automatic with `val`/`var` | Automatic with `val`/`var` |
| Immutable reference | `final var` | `val` | `val` |
| Mutable reference | `var` | `var` | `var` |

### Scala 3

```scala
// val: immutable reference (preferred)
val immutableList = List(1, 2, 3)
// immutableList = List(4, 5, 6)  // Compilation error!

// var: mutable reference (use sparingly)
var counter = 0
counter += 1  // OK

// Idiomatic: prefer val with transformation
val doubled = immutableList.map(_ * 2)
```

### Kotlin

```kotlin
// val: immutable reference (preferred)
val immutableList = listOf(1, 2, 3)
// immutableList = listOf(4, 5, 6)  // Compilation error!

// var: mutable reference (use sparingly)
var counter = 0
counter += 1  // OK

// Idiomatic: prefer val with transformation
val doubled = immutableList.map { it * 2 }
```

## Data Processing Pipeline Example

Here's a complete example showing appropriate `var` usage:

### Java 21

```java
public static Map<String, CategorySummary> processTransactionPipeline(
        List<Transaction> transactions) {

    // var is good - type obvious from stream operation
    var filteredTransactions = transactions.stream()
        .filter(t -> t.amount() >= 50.0)
        .toList();

    // var good - Map type clear from groupingBy
    var byCategory = filteredTransactions.stream()
        .collect(Collectors.groupingBy(Transaction::category));

    // var good - type clear from stream().map() with toMap
    var summaries = byCategory.entrySet().stream()
        .map(entry -> {
            var category = entry.getKey();
            var categoryTransactions = entry.getValue();

            // Explicit types for calculated values
            long count = categoryTransactions.size();
            double total = categoryTransactions.stream()
                .mapToDouble(Transaction::amount)
                .sum();
            double average = count > 0 ? total / count : 0.0;

            return new CategorySummary(category, count, total, average);
        })
        .collect(Collectors.toMap(CategorySummary::category, s -> s));

    return summaries;
}
```

### Scala 3

```scala
def processTransactionPipeline(
    transactions: List[Transaction]
): Map[String, CategorySummary] =
  val filteredTransactions = transactions.filter(_.amount >= 50.0)

  val byCategory = filteredTransactions.groupBy(_.category)

  val summaries = byCategory.map { case (category, categoryTransactions) =>
    val count = categoryTransactions.size.toLong
    val total = categoryTransactions.map(_.amount).sum
    val average = if count > 0 then total / count else 0.0

    category -> CategorySummary(category, count, total, average)
  }

  summaries
```

### Kotlin

```kotlin
fun processTransactionPipeline(
    transactions: List<Transaction>
): Map<String, CategorySummary> {
    val filteredTransactions = transactions.filter { it.amount >= 50.0 }

    val byCategory = filteredTransactions.groupBy { it.category }

    val summaries = byCategory.map { (category, categoryTransactions) ->
        val count = categoryTransactions.size.toLong()
        val total = categoryTransactions.sumOf { it.amount }
        val average = if (count > 0) total / count else 0.0

        category to CategorySummary(category, count, total, average)
    }.toMap()

    return summaries
}
```

## Feature Comparison Table

| Feature | Java 21 (var) | Scala 3 (val/var) | Kotlin (val/var) |
|---------|---------------|-------------------|------------------|
| Local variables | ✅ | ✅ | ✅ |
| Fields/Properties | ❌ | ✅ | ✅ |
| Method parameters | ❌ | ❌ | ❌ |
| Return types | ❌ | ✅ (inferred) | ✅ (single-expression) |
| Lambda parameters | ❌ | ✅ | ✅ |
| Null initializer | ❌ | ✅ | ✅ |
| Immutable by keyword | ❌ (needs `final`) | ✅ (`val`) | ✅ (`val`) |
| Pattern matching | ✅ (Java 21) | ✅ | ✅ (destructuring) |

## Best Practices Summary

### When to Use var (Java)

✅ **DO use var when:**
- Type is obvious from constructor: `var list = new ArrayList<String>()`
- Type is obvious from literal: `var count = 0`
- Type is obvious from factory method: `var today = LocalDate.now()`
- Complex generics add noise without value
- In for-each loops where element type is clear
- In try-with-resources statements

❌ **DON'T use var when:**
- Type is not obvious from context
- Using methods with non-descriptive names
- Numeric literals could be ambiguous
- It would reduce code readability
- For fields, parameters, or return types (not allowed)

### General Guidelines

1. **Readability trumps brevity** - If `var` makes code harder to understand, use explicit types
2. **Be consistent** - Follow your team's conventions
3. **Consider IDE support** - IDEs show inferred types on hover, which helps when reading code
4. **Use meaningful variable names** - Good names reduce the need to see the type

## Code Samples

See the complete implementations in our repository:

- [Java 21 LocalVariableTypeInference.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/typeinference/LocalVariableTypeInference.java)
- [Scala 3 LocalVariableTypeInference.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/typeinference/LocalVariableTypeInference.scala)
- [Kotlin LocalVariableTypeInference.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/typeinference/LocalVariableTypeInference.kt)

## Conclusion

Java's `var` keyword brings local variable type inference to Java, reducing boilerplate while maintaining static typing. While more limited than Scala's and Kotlin's inference capabilities (which extend to fields and return types), `var` is a valuable tool for writing cleaner Java code.

Key takeaways:
- **Java's var** is limited to local variables with initializers
- **Scala and Kotlin** have more powerful inference that includes fields and return types
- **All three languages** maintain static typing - the compiler still knows the exact type
- **Readability is paramount** - use type inference when it helps, not just because you can

For Scala developers learning Java, `var` will feel natural but remember its limitations. The key is finding the right balance between conciseness and clarity.

---

*This is part of our Java 21 Interview Preparation series. Check out the [full preparation plan](/interview/2025/11/25/java21-interview-preparation-plan.html) for more topics.*
