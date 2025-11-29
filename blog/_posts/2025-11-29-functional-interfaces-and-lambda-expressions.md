---
layout: post
title: "Functional Interfaces and Lambda Expressions"
date: 2025-11-29 15:00:00 +0000
categories: functional-programming
tags: java scala kotlin lambdas functional-interfaces method-references
---

Functional programming is a core paradigm in modern software development. This post explores how Java, Scala, and Kotlin handle functional interfaces, lambda expressions, and function composition, with a practical example: building a configurable retry mechanism.

## The Problem

Implement a reusable retry utility that supports:
- Configurable retry policies (fixed delay, exponential backoff)
- Custom retry conditions (max attempts, exception types)
- Result transformation
- Retry event listeners

This problem showcases all major functional programming concepts across all three languages.

## Java: @FunctionalInterface and Lambdas

### Custom Functional Interfaces

Java requires explicit interface declarations with the `@FunctionalInterface` annotation:

```java
@FunctionalInterface
public interface RetryPolicy {
    Duration delayFor(int attempt, Throwable lastError);
    
    // Static factory methods
    static RetryPolicy fixed(Duration delay) {
        return (attempt, error) -> delay;
    }
    
    static RetryPolicy exponentialBackoff(Duration initial, Duration max) {
        return (attempt, error) -> {
            long delayMs = initial.toMillis() * (long) Math.pow(2, attempt - 1);
            return Duration.ofMillis(Math.min(delayMs, max.toMillis()));
        };
    }
    
    // Default method for composition
    default RetryPolicy maxWith(RetryPolicy other) {
        return (attempt, error) -> {
            Duration thisDelay = this.delayFor(attempt, error);
            Duration otherDelay = other.delayFor(attempt, error);
            return thisDelay.compareTo(otherDelay) > 0 ? thisDelay : otherDelay;
        };
    }
}
```

The `@FunctionalInterface` annotation:
- Ensures exactly one abstract method exists
- Allows default and static methods
- Enables lambda expression usage

### Built-in Functional Interfaces

Java provides standard functional interfaces in `java.util.function`:

| Interface | Method | Scala Equivalent |
|-----------|--------|------------------|
| `Supplier<T>` | `T get()` | `() => T` |
| `Consumer<T>` | `void accept(T)` | `T => Unit` |
| `Function<T,R>` | `R apply(T)` | `T => R` |
| `Predicate<T>` | `boolean test(T)` | `T => Boolean` |
| `BiFunction<T,U,R>` | `R apply(T,U)` | `(T, U) => R` |

### Method References

Java supports four types of method references:

```java
// 1. Static method reference: ClassName::staticMethod
Function<Long, Duration> staticRef = Duration::ofMillis;

// 2. Instance method on specific object: object::instanceMethod
String prefix = "Result: ";
Function<String, String> boundRef = prefix::concat;

// 3. Instance method on type: ClassName::instanceMethod
Function<String, String> unboundRef = String::trim;

// 4. Constructor reference: ClassName::new
Function<String, IOException> constructorRef = IOException::new;
```

### Exception Handling in Lambdas

Java's checked exceptions don't work well with lambdas. Create wrapper interfaces:

```java
@FunctionalInterface
public interface ThrowingSupplier<T, E extends Throwable> {
    T get() throws E;
    
    default Supplier<T> toSupplier() {
        return () -> {
            try {
                return get();
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }
}
```

### Complete Retry Executor

```java
public final class RetryExecutor<T> {
    private final RetryPolicy policy;
    private final RetryCondition condition;
    private final Consumer<RetryEvent> listener;
    private final Function<T, T> transformer;
    
    public <E extends Throwable> RetryResult<T> execute(
            ThrowingSupplier<T, E> operation) {
        int attempt = 0;
        RetryContext context = null;
        
        while (true) {
            attempt++;
            try {
                T result = operation.get();
                return RetryResult.success(transformer.apply(result), attempt);
            } catch (Throwable e) {
                context = context == null 
                    ? RetryContext.initial(e) 
                    : context.nextAttempt(e);
                    
                if (!condition.shouldRetry(context)) {
                    return RetryResult.failure(e, attempt);
                }
                
                Duration delay = policy.delayFor(attempt, e);
                listener.accept(new RetryEvent(context, delay));
                Thread.sleep(delay.toMillis());
            }
        }
    }
}
```

## Scala: First-Class Functions

Scala treats functions as first-class citizens with simple type syntax:

### Function Types Replace Interfaces

```scala
// Java requires @FunctionalInterface
@FunctionalInterface
interface RetryPolicy {
    Duration delayFor(int attempt, Throwable error);
}

// Scala uses simple type aliases
type RetryPolicy = (Int, Throwable) => Duration
type RetryCondition = RetryContext => Boolean
type RetryListener = (RetryContext, Duration) => Unit
```

### Extension Methods for Composition

```scala
object RetryPolicy:
  def fixed(delay: Duration): RetryPolicy = (_, _) => delay
  
  def exponentialBackoff(initial: Duration, max: Duration): RetryPolicy =
    (attempt, _) =>
      val delayMs = initial.toMillis * Math.pow(2, attempt - 1).toLong
      Duration.ofMillis(Math.min(delayMs, max.toMillis))

  // Extension methods replace Java's default methods
  extension (self: RetryPolicy)
    def maxWith(other: RetryPolicy): RetryPolicy =
      (attempt, error) =>
        val selfDelay = self(attempt, error)
        val otherDelay = other(attempt, error)
        if selfDelay.compareTo(otherDelay) > 0 then selfDelay else otherDelay
```

### Pattern Matching on Conditions

```scala
object RetryCondition:
  def maxAttempts(max: Int): RetryCondition = ctx => ctx.attempt < max
  
  // Reified generics for type checking
  def forExceptions[E <: Throwable: reflect.ClassTag]: RetryCondition =
    ctx => reflect.classTag[E].runtimeClass.isInstance(ctx.lastError)

  // Symbolic operators for DSL
  extension (self: RetryCondition)
    def &&(other: RetryCondition): RetryCondition = 
      ctx => self(ctx) && other(ctx)
    def ||(other: RetryCondition): RetryCondition = 
      ctx => self(ctx) || other(ctx)
```

### By-Name Parameters

Scala's by-name parameters eliminate the need for `Supplier`:

```scala
// Java requires explicit Supplier
executor.execute(() -> fetchData());

// Scala uses by-name parameter
executor.execute(fetchData()) // More natural syntax

class RetryExecutor[T](/* ... */):
  def execute(operation: => T): RetryResult[T] = // => T is by-name
    Try(operation) match
      case scala.util.Success(result) => RetryResult.Success(transformer(result), attempt)
      case scala.util.Failure(e) => // handle failure
```

### Function References

```scala
// Static method reference equivalent
val staticRef: Long => Duration = Duration.ofMillis

// Bound instance method
val prefix = "Result: "
val boundRef: String => String = prefix.concat

// Lambda (more common in Scala)
val unboundRef: String => String = _.trim

// Constructor reference
val constructorRef: String => IOException = new IOException(_)

// Partial application (Scala-specific)
def add(a: Int, b: Int): Int = a + b
val addFive: Int => Int = add(5, _)

// Function composition
val composed = trim andThen toLowerCase
```

## Kotlin: Pragmatic Functional Programming

Kotlin combines Java interoperability with Scala-like syntax:

### Type Aliases for Functions

```kotlin
typealias RetryPolicy = (Int, Throwable) -> Duration
typealias RetryCondition = (RetryContext) -> Boolean
typealias RetryListener = (RetryContext, Duration) -> Unit
```

### Extension Functions

```kotlin
object RetryPolicies {
    fun fixed(delay: Duration): RetryPolicy = { _, _ -> delay }
    
    fun exponentialBackoff(initial: Duration, max: Duration): RetryPolicy =
        { attempt, _ ->
            val delayMs = initial.toMillis() * Math.pow(2.0, (attempt - 1).toDouble()).toLong()
            Duration.ofMillis(minOf(delayMs, max.toMillis()))
        }
}

// Extension function for composition
fun RetryPolicy.maxWith(other: RetryPolicy): RetryPolicy =
    { attempt, error ->
        val selfDelay = this(attempt, error)
        val otherDelay = other(attempt, error)
        if (selfDelay > otherDelay) selfDelay else otherDelay
    }
```

### Infix Functions for DSL

```kotlin
// Infix functions allow readable composition
infix fun RetryCondition.and(other: RetryCondition): RetryCondition = 
    { ctx -> this(ctx) && other(ctx) }

// Usage
val condition = RetryConditions.maxAttempts(3) and 
    RetryConditions.forException<IOException>()
```

### Reified Generics

```kotlin
// No Class parameter needed thanks to reified
inline fun <reified E : Throwable> forException(): RetryCondition = 
    { ctx -> ctx.lastError is E }

// Usage
val condition = RetryConditions.forException<IOException>()
```

### Trailing Lambda Syntax

```kotlin
// Clean syntax for operations
executor.execute {
    fetchData()
}

// Result handling with fold
val result = runCatching { operation() }
    .fold(
        onSuccess = { "Success: $it" },
        onFailure = { "Error: ${it.message}" }
    )
```

## Comparison Table

| Feature | Java | Scala | Kotlin |
|---------|------|-------|--------|
| Function type syntax | `Function<T,R>` | `T => R` | `(T) -> R` |
| Interface annotation | `@FunctionalInterface` | Not needed | Not needed |
| Composition | `andThen`, `compose` | `andThen`, `compose` | Manual or extension |
| Method reference | `Class::method` | `_.method` or `method _` | `Class::method` |
| Constructor ref | `Class::new` | `new Class(_)` | `::Class` |
| Exception handling | Wrapper interfaces | `Try`, `Either` | `runCatching`, `Result` |
| Default methods | `default` keyword | Extension methods | Extension functions |
| DSL support | Limited | Symbolic methods, infix | Infix functions |
| Partial application | Not supported | `f(a, _)` | Not directly |

## Key Insights for Scala Developers

1. **Type verbosity**: Java requires explicit interface declarations; Scala/Kotlin use function types directly.

2. **Composition patterns**: 
   - Java: Default methods in interfaces
   - Scala: Extension methods with `extension`
   - Kotlin: Extension functions

3. **DSL creation**:
   - Scala: Symbolic operators (`&&`, `||`)
   - Kotlin: Infix functions (`and`, `or`)
   - Java: Method chaining

4. **Exception handling**:
   - Java: Checked exceptions require wrapper interfaces
   - Scala: `Try`, `Either`, no checked exceptions
   - Kotlin: `runCatching`, `Result`, no checked exceptions

5. **Method references**:
   - All three support similar patterns
   - Scala often prefers lambdas with `_`
   - Kotlin's `::` works like Java

## Full Working Examples

Check out the complete implementation in our repository:
- [Java Retry Executor](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/interview/preparation/retry)
- [Scala 3 Retry Executor](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/interview/preparation/retry)
- [Kotlin Retry Executor](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/retry)

## Conclusion

Functional programming concepts translate well across Java, Scala, and Kotlin:

- **Java** requires more boilerplate but has excellent IDE support and type safety
- **Scala** offers the most concise and powerful functional features
- **Kotlin** provides a pragmatic middle ground with clean syntax and Java interoperability

For Scala developers working with Java:
- Think of `@FunctionalInterface` as defining function types
- Use `Function`, `Predicate`, `Consumer`, `Supplier` like Scala function types
- Method references work similarly to Scala's `_` placeholder syntax
- Default methods provide composition like extension methods

Happy functional programming! 🚀
