---
layout: post
title: "Tricky Java Patterns That Everyone Uses But Few Understand"
description: "Explore 8 confusing Java code patterns that look simple but hide surprising complexity - from ArrayList.toArray() optimization to Virtual Thread pinning in Java 21."
date: 2025-12-03 11:00:00 +0000
categories: [best-practices]
tags: [java, java21, performance, best-practices, gotchas, virtual-threads, patterns, optimization]
---

Have you ever copy-pasted code from StackOverflow that "just works" but wondered why it's written that way? Java has several patterns that developers use daily without fully understanding their implications. Some are performance optimizations that seem counter-intuitive, others are subtle pitfalls that work in testing but fail in production.

This post explores **8 tricky Java patterns** covering both classic gotchas and modern Java 21 features. Each pattern includes runnable examples showing the problem, explanation of why it happens, and the correct approach.

## 1. ArrayList.toArray(): Empty Array vs. Sized Array

### The Pattern Everyone Copies

```java
List<String> fruits = List.of("Apple", "Banana", "Cherry");

// Which is better?
String[] array1 = fruits.toArray(new String[0]);           // Empty array
String[] array2 = fruits.toArray(new String[fruits.size()]); // Sized array
```

### The Confusion

Logic suggests passing a pre-sized array (`new String[fruits.size()]`) avoids reallocation. But **this is actually slower in modern JVMs!**

### The Truth

```java
// ✅ OPTIMAL: Empty array (since Java 6)
String[] array = fruits.toArray(new String[0]);

// Why it's faster:
// 1. JVM recognizes this pattern
// 2. Can allocate the exact size immediately  
// 3. May use stack allocation or escape analysis
// 4. The empty array is only used for type inference!

// ❌ SLOWER: Pre-sized array
String[] array = fruits.toArray(new String[fruits.size()]);

// Why it's slower:
// 1. Allocates array on heap
// 2. JVM can't optimize as aggressively
// 3. Wastes allocation if list size changed between calls
```

**Benchmark results:** Empty array is typically **15-20% faster** with less garbage collection pressure.

**Modern alternative (Java 11+):**
```java
String[] array = fruits.toArray(String[]::new);  // Cleanest and fastest
```

### Key Takeaway

Old StackOverflow answers from pre-Java-6 era recommend sized arrays. Modern JVMs optimize the empty array case better. Always use `toArray(new Type[0])` or `toArray(Type[]::new)`.

## 2. String Concatenation in Loops

### The Pattern That Looks Fine

```java
String result = "";
for (int i = 0; i < 1000; i++) {
    result += "Item-" + i + ", ";  // Looks simple!
}
```

### The Hidden Cost

Each `+=` creates a **new String object**. With 1000 iterations:
- Creates ~3000 intermediate String objects
- Time complexity: **O(n²)** due to copying
- Memory: Significant garbage collection pressure

### The Classic Solution

```java
StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    sb.append("Item-").append(i).append(", ");
}
String result = sb.toString();
```

**Performance:** 10-100x faster for large loops.

### Modern Java 9+ Optimization

```java
// Single statement concatenation is now optimized
int count = 42;
String name = "Alice";
String result = "User: " + name + ", Count: " + count;  // ✅ Fast in Java 9+
```

Since Java 9, the compiler uses `invokedynamic` and may generate `StringBuilder` code or use `StringConcatFactory`. Single-statement concatenation is nearly as fast as manual `StringBuilder`.

### When It Matters

<div class="table-wrapper" markdown="1">

| Scenario | Use StringBuilder? |
|----------|-------------------|
| Single statement | ❌ No - compiler optimizes |
| Loop concatenation | ✅ Yes - essential |
| Conditional building | ✅ Yes - clearer |
| < 10 strings | ⚠️ Optional - negligible difference |
| 100+ strings | ✅ Critical - 10-100x faster |

</div>

## 3. Integer Caching: The == Trap

### The Code That Works... Until It Doesn't

```java
Integer a = 100;
Integer b = 100;
System.out.println(a == b);  // true ✅

Integer c = 200;
Integer d = 200;
System.out.println(c == d);  // false ❌ Surprise!
```

### Why This Happens

Java caches `Integer` objects for values **-128 to 127** by default. The `==` operator checks **object identity**, not value equality.

```java
// Both point to same cached object
Integer a = 100;
Integer b = 100;
System.out.println(a == b);        // true (same object)
System.out.println(a.equals(b));   // true

// Each is a different object
Integer c = 200;
Integer d = 200;
System.out.println(c == d);        // false (different objects!)
System.out.println(c.equals(d));   // true
```

### The Production Bug

This causes subtle bugs because tests often use small numbers:

```java
// Works in testing with low IDs
Integer userId1 = getUserId(1);
Integer userId2 = getUserId(1);
if (userId1 == userId2) {  // ❌ Dangerous!
    System.out.println("Match");  // Works with low IDs
}

// Fails in production with larger IDs
Integer realId1 = getUserId(1000);
Integer realId2 = getUserId(1000);
if (realId1 == realId2) {  // Always false!
    System.out.println("This never prints!");
}
```

### Wrapper Caching Rules

<div class="table-wrapper" markdown="1">

| Type | Cached Range | Notes |
|------|--------------|-------|
| `Byte` | All values (-128 to 127) | Fully cached |
| `Short` | -128 to 127 | |
| `Integer` | -128 to 127 | Configurable with `-XX:AutoBoxCacheMax` |
| `Long` | -128 to 127 | |
| `Character` | 0 to 127 | ASCII range |
| `Float` | Never cached | |
| `Double` | Never cached | |
| `Boolean` | `TRUE` and `FALSE` | Always cached (only 2 objects) |

</div>

### The Correct Way

```java
// ✅ Always use equals() for wrapper types
if (userId1.equals(userId2)) {
    System.out.println("Match");
}

// ✅ Or auto-unbox to primitive
int id1 = userId1;
int id2 = userId2;
if (id1 == id2) {
    System.out.println("Match");
}
```

### Performance Note

Autoboxing in loops is expensive:

```java
// ❌ Slow: Box/unbox in every iteration
Integer sum = 0;
for (int i = 0; i < 10000; i++) {
    sum += i;  // Unbox sum, add, box result
}

// ✅ Fast: Use primitives
int sum = 0;
for (int i = 0; i < 10000; i++) {
    sum += i;
}
```

## 4. Stream.of() vs Arrays.stream() for Primitive Arrays

### The Trap

```java
int[] numbers = {1, 2, 3, 4, 5};

// ❌ WRONG: Treats array as single element
Stream<int[]> wrongStream = Stream.of(numbers);
System.out.println(wrongStream.count());  // Prints: 1 (the array itself!)

// ✅ CORRECT: Creates stream of elements
IntStream correctStream = Arrays.stream(numbers);
System.out.println(correctStream.count());  // Prints: 5
```

### Why This Happens

```java
// Stream.of() signature:
static <T> Stream<T> of(T... values)

// For int[], T becomes int[] (array is one value!)
Stream<int[]> stream = Stream.of(numbers);  // Stream of arrays

// Arrays.stream() has specialized overloads:
static IntStream stream(int[] array)
static <T> Stream<T> stream(T[] array)
```

### Object Arrays Work Differently

```java
String[] words = {"Java", "Scala", "Kotlin"};

// Both work correctly for object arrays
Stream<String> stream1 = Stream.of(words);       // ✅ Works
Stream<String> stream2 = Arrays.stream(words);   // ✅ Also works
```

### The Correct Patterns

```java
int[] numbers = {1, 2, 3, 4, 5};

// ✅ Method 1: Use Arrays.stream()
IntStream stream1 = Arrays.stream(numbers);

// ✅ Method 2: Use IntStream.of()
IntStream stream2 = IntStream.of(numbers);

// Calculate average
double avg = Arrays.stream(numbers).average().orElse(0.0);
```

### All Primitive Streams

<div class="table-wrapper" markdown="1">

| Array Type | Stream Type | Method |
|------------|-------------|--------|
| `int[]` | `IntStream` | `Arrays.stream()` or `IntStream.of()` |
| `long[]` | `LongStream` | `Arrays.stream()` or `LongStream.of()` |
| `double[]` | `DoubleStream` | `Arrays.stream()` or `DoubleStream.of()` |
| `byte[]` | No specialized stream | Convert to `IntStream` |
| `Object[]` | `Stream<T>` | `Stream.of()` or `Arrays.stream()` |

</div>

**Rule of thumb:**
- **Primitive arrays** → Use `Arrays.stream()` or `IntStream.of()`
- **Object arrays** → Either `Stream.of()` or `Arrays.stream()`

## 5. Double-Checked Locking Evolution

### The Broken Pattern (Pre-Java 5)

```java
public class Singleton {
    private static Singleton instance;

    public static Singleton getInstance() {
        if (instance == null) {           // Check 1: No lock
            synchronized (Singleton.class) {
                if (instance == null) {    // Check 2: With lock
                    instance = new Singleton();  // ❌ BROKEN!
                }
            }
        }
        return instance;
    }
}
```

### Why It's Broken

The `new` operator is **not atomic**. It has three steps:
1. Allocate memory
2. Initialize object
3. Assign reference

**CPU can reorder steps 2 and 3!**

Thread 1 might set `instance` before initialization completes. Thread 2 sees non-null but **uninitialized object**.

### The Fix (Java 5+)

```java
public class Singleton {
    private static volatile Singleton instance;  // volatile!

    public static Singleton getInstance() {
        if (instance == null) {
            synchronized (Singleton.class) {
                if (instance == null) {
                    instance = new Singleton();  // ✅ Safe with volatile
                }
            }
        }
        return instance;
    }
}
```

The `volatile` keyword:
- Prevents instruction reordering
- Establishes happens-before relationship
- Ensures full construction before assignment visible

### The Modern Way

```java
public class ModernSingleton {
    private ModernSingleton() {}

    private static class Holder {
        static final ModernSingleton INSTANCE = new ModernSingleton();
    }

    public static ModernSingleton getInstance() {
        return Holder.INSTANCE;  // ✅ Thread-safe, lazy, fast!
    }
}
```

**Why it's better:**
- JVM guarantees thread-safe class initialization
- Lazy - `Holder` class loaded only when `getInstance()` called
- No synchronization overhead
- No `volatile` needed
- Simpler code

### Performance Comparison

<div class="table-wrapper" markdown="1">

| Operation | Time |
|-----------|------|
| volatile read | ~5ns |
| volatile write | ~5ns |
| synchronized | ~50ns |
| Holder pattern | ~1ns (after first call) |

</div>

**Use when:** Singleton pattern or lazy-initialized expensive resources

## 6. Switch Expressions vs Statements

### The Confusing Differences (Java 14+)

```java
Day day = Day.TUESDAY;

// OLD: Switch statement with fall-through
switch (day) {
    case MONDAY:
    case TUESDAY:    // Fall-through
    case WEDNESDAY:
        System.out.println("Early week");
        break;      // Must remember break!
    default:
        System.out.println("Other");
}

// NEW: Switch expression with arrow
String result = switch (day) {
    case MONDAY, TUESDAY, WEDNESDAY -> "Early week";  // No fall-through!
    case THURSDAY, FRIDAY -> "Late week";
    case SATURDAY, SUNDAY -> "Weekend";
    // Must be exhaustive or have default
};
```

### Key Differences

<div class="table-wrapper" markdown="1">

| Feature | Statement | Expression |
|---------|-----------|------------|
| Fall-through | Default (needs `break`) | Never (with `->`) |
| Exhaustiveness | Not required | Required |
| Returns value | No | Yes |
| `break` | Exits switch | Not allowed |
| `yield` | Not allowed | Returns value from block |

</div>

### Return vs Yield Confusion

```java
// Arrow without block: implicit return
String result = switch (day) {
    case MONDAY -> "Start";     // Implicit return
    case FRIDAY -> "End";
    default -> "Middle";
};

// Arrow with block: must use yield
String result = switch (day) {
    case MONDAY -> {
        System.out.println("Complex logic");
        yield "Start";  // ✅ Use yield, not return!
    }
    default -> "Other";
};

// ❌ Can't use return in switch expression
// return would exit the enclosing method!
```

### Pattern Matching (Java 21)

```java
Object obj = "Hello";

String result = switch (obj) {
    case String s when s.length() > 10 -> "Long: " + s;
    case String s -> "Short: " + s;      // Unguarded must come after guarded
    case Integer i when i > 0 -> "Positive: " + i;
    case Integer i -> "Non-positive: " + i;
    case null -> "Null";                  // Explicit null handling
    default -> "Unknown";
};
```

**Gotcha:** Order matters! More specific patterns must come first.

### Null Handling (Java 21)

```java
Day day = null;

// Old statement: NullPointerException
switch (day) {
    case MONDAY -> System.out.println("Monday");
    default -> System.out.println("Other");
}  // ❌ NPE!

// New expression: Can handle null explicitly
String result = switch (day) {
    case null -> "Null day";              // ✅ Explicit null case
    case MONDAY -> "Monday";
    default -> "Other";
};
```

## 7. Try-With-Resources Gotchas

### Reverse Close Order

```java
try (
    Resource first = new Resource("First");
    Resource second = new Resource("Second");
    Resource third = new Resource("Third")
) {
    System.out.println("Using resources");
}

// Output:
// Opening: First
// Opening: Second
// Opening: Third
// Using resources
// Closing: Third   ← Reversed!
// Closing: Second
// Closing: First
```

**Resources close in REVERSE order** (like a stack, LIFO). This ensures dependent resources close correctly.

### Suppressed Exceptions

```java
try (FailingResource resource = new FailingResource()) {
    throw new RuntimeException("Body exception");
} catch (Exception e) {
    System.out.println("Primary: " + e.getMessage());
    // Body exception is primary
    
    for (Throwable suppressed : e.getSuppressed()) {
        System.out.println("Suppressed: " + suppressed.getMessage());
        // Close exceptions added as suppressed
    }
}
```

**Without try-with-resources:** Close exception would HIDE body exception—major bug source!

### Effectively Final (Java 9+)

```java
Resource resource = new Resource("External");

try (resource) {  // ✅ Java 9+: Can use existing variable
    System.out.println("Using");
}
// resource is closed here

// Variable must be effectively final (no reassignment)
```

### Common Mistakes

```java
// ❌ Mistake 1: Returning closed resource
Resource getResource() {
    try (Resource r = new Resource()) {
        return r;  // r is closed after return!
    }
}

// ❌ Mistake 2: Lock is NOT AutoCloseable
Lock lock = new ReentrantLock();
// try (lock.lock()) { }  // Won't compile!

// ✅ Must use traditional try-finally
lock.lock();
try {
    // critical section
} finally {
    lock.unlock();
}
```

## 8. Virtual Thread Pinning (Java 21)

### The Non-Obvious Performance Killer

Virtual threads (Java 21) are designed to be cheap—you can create millions. They automatically **unmount** from platform threads when blocking, allowing other virtual threads to run.

**BUT:** Certain operations **pin** virtual threads to platform threads, losing all benefits!

### Pinning Cause #1: synchronized

```java
Object lock = new Object();

try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> {
        synchronized (lock) {  // ❌ PINNING!
            Thread.sleep(1000);  // Virtual thread stays pinned
        }
    });
}
```

**Why:** `synchronized` uses JVM monitor. JVM cannot transfer monitor ownership to another thread.

**Impact:** With 1000s of threads, platform thread pool exhausts, throughput collapses.

### The Fix: Use ReentrantLock

```java
var lock = new ReentrantLock();

try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> {
        lock.lock();  // ✅ No pinning!
        try {
            Thread.sleep(1000);  // Virtual thread can unmount
        } finally {
            lock.unlock();
        }
    });
}
```

### Pinning Causes

<div class="table-wrapper" markdown="1">

| Causes Pinning | Safe (No Pinning) |
|----------------|-------------------|
| ❌ `synchronized` blocks/methods | ✅ `ReentrantLock` |
| ❌ `Object.wait()` | ✅ `Semaphore` |
| ❌ Some native methods | ✅ `Thread.sleep()` |
| | ✅ Most `java.util.concurrent` |
| | ✅ Socket I/O (special handling) |

</div>

### Detecting Pinning

```bash
# JVM flag to detect pinning
java -Djdk.tracePinnedThreads=full MyApp

# Prints stack trace when pinning detected
```

### Real-World Pattern: Web Server

```java
// ❌ BAD: Synchronized with blocking I/O
void handleRequest(Request req) {
    synchronized (this) {  // Pins for entire block!
        String data = database.query(...);  // Blocking I/O
        return process(data);
    }
}
// Can only handle N requests (N = platform threads)

// ✅ GOOD: Minimal synchronization
void handleRequest(Request req) {
    String data = database.query(...);  // No sync, can unmount
    String result = process(data);
    
    synchronized (this) {  // Only sync for quick update
        cache.put(key, result);
    }
}
// Can handle millions of concurrent requests
```

### Best Practices

1. **Prefer `ReentrantLock` over `synchronized`** with virtual threads
2. **Keep `synchronized` blocks minimal**—move I/O outside
3. **Use `-Djdk.tracePinnedThreads`** to detect issues
4. **Test with realistic concurrency** levels
5. **Avoid `synchronized` methods** entirely

## Summary: When Each Pattern Matters

<div class="table-wrapper" markdown="1">

| Pattern | When It Matters | Quick Fix |
|---------|----------------|-----------|
| `toArray()` | Frequent conversions | Use `new Type[0]` or `Type[]::new` |
| String concat | Loops with 100+ iterations | Use `StringBuilder` |
| Integer `==` | Any wrapper comparison | Always use `.equals()` |
| `Stream.of()` primitives | Working with primitive arrays | Use `Arrays.stream()` |
| Double-checked locking | Thread-safe singletons | Use holder pattern |
| Switch expressions | Pattern matching, exhaustiveness | Prefer arrows (`->`) |
| Try-with-resources | Resource management | Remember reverse close order |
| Virtual thread pinning | High-concurrency apps | Replace `synchronized` with `ReentrantLock` |

</div>

## Conclusion

These patterns demonstrate that Java has **hidden complexity beneath simple syntax**. Code that looks correct can have subtle performance problems or bugs that only appear in production.

Key lessons:

1. **Don't blindly copy StackOverflow answers**—check the date and Java version
2. **Modern JVM optimizations** can make "obvious" optimizations wrong
3. **Understand the memory model**—concurrency issues are subtle
4. **Java 21 features** have non-obvious behavior (pinning, pattern matching order)
5. **Measure, don't guess**—use profilers and benchmarks

The examples in this post are runnable and available in the [GitHub repository](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/trickypatterns).

For Scala developers learning Java: These gotchas show why Scala made different design choices (immutability by default, no primitives, no null). But understanding Java's quirks makes you a better JVM developer.

Happy coding, and may your code be explicit about its complexity! 🚀
