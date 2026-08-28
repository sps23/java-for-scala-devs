---
layout: post
title: "Singleton Pattern: When There Can Be Only One"
description: "Master the singleton design pattern across Java 21, Kotlin, Scala 2, and Scala 3. Compare eager vs lazy initialization, language-specific implementations, and when to use (or avoid) this creational pattern."
date: 2026-07-26 20:00:00 +0000
categories: [interview, best-practices]
tags: [java, java21, scala, scala2, scala3, kotlin, design-patterns, creational-patterns]
---

Imagine you're building a logging system for your application. You need exactly one logger instance—not two, not three, one. You want every part of your code to write to the same file, maintain consistent formatting, and never accidentally create duplicate loggers that step on each other's toes.

That's a singleton. It's a class that ensures only one instance exists throughout your application's lifetime, and provides a global point of access to that instance.

In this post, we'll explore how to implement singletons correctly in Java, Kotlin, Scala 2, and Scala 3. We'll see that some languages make it embarrassingly simple, while others require careful attention to thread safety and serialization.

## The Problem: One Logger to Rule Them All

Let's say you need a database connection manager that:
- Must have exactly one instance (connections are expensive)
- Is accessed from multiple threads simultaneously
- Should be initialized lazily (only when first needed)
- Survives serialization and deserialization correctly

This is precisely what singletons solve.

## Key Concepts

### Thread Safety

When multiple threads might access the singleton simultaneously, we need guarantees that only one instance is ever created:

<div class="table-wrapper" markdown="1">

| Approach                        | Thread-Safe  | Lazy | Complexity  |
|---------------------------------|:------------:|:----:|:-----------:|
| Eager static initialization     |      ✓      |  ✗  |     Low     |
| Double-checked locking          |      ✓      |  ✓  |    High     |
| Holder pattern                  |      ✓      |  ✓  |   Medium    |
| Language keyword (Kotlin/Scala) |      ✓      |  ✓  |     Low     |

</div>

---

### Initialization Strategies

- **Eager:** Instance created when the class loads (simple, but wastes resources if never used)
- **Lazy:** Instance created only when first requested (saves resources, but needs synchronization)
- **Language-level:** Built into the language (like Kotlin's `object` or Scala's `object`)

## The Solution: Singleton Implementations Across Languages

### Java 21: Eager Initialization

The simplest and most recommended approach in Java. The instance is created when the class is loaded—the class loader guarantees thread safety, so no synchronization is needed.

<div class="code-tabs" data-tabs-id="tabs-1">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">class</span> <span class="nc">DatabaseConnection</span> <span class="o">{</span>
    <span class="kd">private</span> <span class="kd">static</span> <span class="kd">final</span> <span class="nc">DatabaseConnection</span> <span class="n">instance</span> <span class="o">=</span>
        <span class="k">new</span> <span class="nc">DatabaseConnection</span><span class="o">();</span>

    <span class="c1">// Private constructor prevents external instantiation</span>
    <span class="kd">private</span> <span class="nc">DatabaseConnection</span><span class="o">()</span> <span class="o">{</span>
        <span class="nc">System</span><span class="o">.</span><span class="na">out</span><span class="o">.</span><span class="na">println</span><span class="o">(</span><span class="s">"DatabaseConnection instance created"</span><span class="o">);</span>
    <span class="o">}</span>

    <span class="kd">public</span> <span class="kd">static</span> <span class="nc">DatabaseConnection</span> <span class="nf">getInstance</span><span class="o">()</span> <span class="o">{</span>
        <span class="k">return</span> <span class="n">instance</span><span class="o">;</span>
    <span class="o">}</span>

    <span class="kd">public</span> <span class="kt">void</span> <span class="nf">connect</span><span class="o">()</span> <span class="o">{</span>
        <span class="nc">System</span><span class="o">.</span><span class="na">out</span><span class="o">.</span><span class="na">println</span><span class="o">(</span><span class="s">"Connected to database"</span><span class="o">);</span>
    <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/designpatterns/singleton/DatabaseConnection.java">View in repository</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">object</span> <span class="nc">DatabaseConnection</span> <span class="p">{</span>
    <span class="k">init</span> <span class="p">{</span>
        <span class="nf">println</span><span class="p">(</span><span class="s">"DatabaseConnection instance created"</span><span class="p">)</span>
    <span class="p">}</span>

    <span class="k">fun</span> <span class="nf">connect</span><span class="p">()</span> <span class="p">{</span>
        <span class="nf">println</span><span class="p">(</span><span class="s">"Connected to database"</span><span class="p">)</span>
    <span class="p">}</span>
<span class="p">}</span>

<span class="c1">// Usage: DatabaseConnection.connect()</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/designpatterns/singleton/DatabaseConnection.kt">View in repository</a></p>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">object</span> <span class="nc">DatabaseConnection</span> <span class="o">{</span>
  <span class="n">println</span><span class="o">(</span><span class="s">"DatabaseConnection instance created"</span><span class="o">)</span>

  <span class="k">def</span> <span class="n">connect</span><span class="o">():</span> <span class="kt">Unit</span> <span class="o">=</span> <span class="n">println</span><span class="o">(</span><span class="s">"Connected to database"</span><span class="o">)</span>
<span class="o">}</span>

<span class="c1">// Usage: DatabaseConnection.connect()</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/main/scala/io/github/sps23/designpatterns/singleton/DatabaseConnection.scala">View in repository</a></p>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">object</span> <span class="nc">DatabaseConnection</span><span class="o">:</span>
  <span class="n">println</span><span class="o">(</span><span class="s">"DatabaseConnection instance created"</span><span class="o">)</span>

  <span class="k">def</span> <span class="n">connect</span><span class="o">():</span> <span class="kt">Unit</span> <span class="o">=</span> <span class="n">println</span><span class="o">(</span><span class="s">"Connected to database"</span><span class="o">)</span>

<span class="c1">// Usage: DatabaseConnection.connect()</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/designpatterns/singleton/DatabaseConnection.scala">View in repository</a></p>
</div>
</div>

**Key differences:**

- **Java**: Class loading guarantees thread safety. No synchronization needed, but instance is created immediately.
- **Kotlin**: The `object` keyword handles everything. Lazy initialization and thread safety are automatic.
- **Scala 2/3**: Like Kotlin, `object` is the idiomatic way. Scala handles all the synchronization and lazy initialization.

---

## When Lazy Initialization Matters

Sometimes you don't want to create the singleton immediately. Imagine a configuration loader that's never used in your tests—why create it?

### Java 21: Holder Pattern (Recommended)

The **holder pattern** combines lazy initialization with the simplicity of eager initialization. The inner class is only loaded when you call `getInstance()`.

```java
public class HolderDatabaseConnection {
    private HolderDatabaseConnection() { }

    private static class ConnectionHolder {
        static final HolderDatabaseConnection instance =
            new HolderDatabaseConnection();
    }

    public static HolderDatabaseConnection getInstance() {
        return ConnectionHolder.instance;
    }
}
```

This is the **modern Java best practice**: thread-safe, lazy, and simple.

### Kotlin/Scala: Automatic

Kotlin's `object` and Scala's `object` both provide lazy initialization by default. No special pattern needed.

```kotlin
// Kotlin - lazy by default
object LazyLogger {
    init { println("Logger initialized") }
    fun log(msg: String) = println(msg)
}

// Only creates instance when LazyLogger.log() is first called
```

---

## Comparison: Language Implementations

<div class="table-wrapper" markdown="1">

| Language      | Syntax                               | Boilerplate  |   Thread-Safe   | Lazy  | Comments                           |
|---------------|--------------------------------------|:------------:|:---------------:|:-----:|------------------------------------|
| **Java 21**   | `static final + private constructor` |     High     | ✓ (by default) |  ✗   | Use holder pattern for lazy        |
| **Kotlin**    | `object` keyword                     |   Minimal    |       ✓        |  ✓   | Idiomatic, automatic serialization |
| **Scala 2**   | `object` keyword                     |   Minimal    |       ✓        |  ✓   | Idiomatic, similar to Kotlin       |
| **Scala 3**   | `object` keyword                     |   Minimal    |       ✓        |  ✓   | Same as Scala 2, cleaner syntax    |

</div>

---

## The Pitfalls: How to Break (and Fix) a Singleton

### Java Pitfall 1: Reflection Attacks

Even a private constructor can be bypassed with reflection:

```java
// This BREAKS your singleton!
Constructor<DatabaseConnection> constructor =
    DatabaseConnection.class.getDeclaredConstructor();
constructor.setAccessible(true);
DatabaseConnection fake = constructor.newInstance(); // ❌ New instance!
```

**Fix:** Add a check in the constructor:

```java
private DatabaseConnection() {
    if (instance != null) {
        throw new IllegalStateException("Singleton already instantiated");
    }
}
```

### Java Pitfall 2: Serialization Creates Copies

Deserializing a singleton creates a new instance:

```java
DatabaseConnection original = DatabaseConnection.getInstance();
byte[] serialized = serialize(original);
DatabaseConnection deserialized = deserialize(serialized);

// ❌ deserialized != original (new instance!)
```

**Fix:** Implement `readResolve()`:

```java
protected Object readResolve() {
    return getInstance();
}
```

### Kotlin/Scala: No Pitfalls (Built-In Safety)

Because `object` is a language keyword, the compiler and runtime handle these issues. You simply can't accidentally create two instances.

---

## When NOT to Use Singletons

Singletons are often misused as "convenient global state." Before reaching for a singleton, ask:

1. **Do I really need exactly one instance?** (Or just want to avoid passing parameters?)
2. **Is this testable?** (Singletons make mocking difficult)
3. **Can I use dependency injection instead?** (Usually yes, and it's better)

Common anti-pattern:

```java
// ❌ DON'T DO THIS
public class BadConfig {
    public static final BadConfig instance = new BadConfig();
    public String apiKey; // Mutable global state!
}

// Someone changes it unexpectedly:
BadConfig.instance.apiKey = "wrong-key"; // 😱
```

Better approach:

```java
// ✓ Use dependency injection
public class AppService {
    private final Config config;
    
    public AppService(Config config) { // Injected, testable
        this.config = config;
    }
}
```

---

## Real-World Use Cases (Where Singletons Actually Make Sense)

### Logger

```java
Logger.getInstance().log("User logged in");
```

There should genuinely be one logger writing to one file.

### Database Connection Pool

```java
ConnectionPool pool = ConnectionPool.getInstance();
Connection conn = pool.getConnection();
```

A pool manages a fixed set of connections—exactly what a singleton should manage.

### Configuration (Immutable)

```java
AppConfig config = AppConfig.getInstance();
String apiKey = config.getApiKey();
```

If the configuration is immutable and read-only, a singleton is reasonable.

---

## Comparison Table: Quick Reference

<div class="table-wrapper" markdown="1">

| Question | Java 21 | Kotlin | Scala |
|----------|---------|--------|-------|
| How do I write it? | Static field + private constructor | `object` keyword | `object` keyword |
| How many lines of code? | ~10–20 | ~2–5 | ~2–5 |
| Is it thread-safe? | Yes | Yes | Yes |
| Is it lazy-initialized? | No (but use holder pattern) | Yes | Yes |
| Can I test it? | Difficult | Difficult | Difficult |
| Can I serialize it? | Yes (with `readResolve()`) | Yes | Yes |

</div>

---

## Testing Singletons: Proving It Works

To verify that our singleton implementations are truly creating only one instance across concurrent access, here are test examples for each language. These tests demonstrate thread safety using `CountDownLatch` to coordinate 100 concurrent threads:

<div class="code-tabs" data-tabs-id="tabs-test-1">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="nd">@DisplayName</span><span class="o">(</span><span class="s">"Should return the same instance"</span><span class="o">)</span>
<span class="kt">void</span> <span class="nf">testSingletonInstances</span><span class="o">()</span> <span class="o">{</span>
    <span class="nc">DatabaseConnection</span> <span class="n">conn1</span> <span class="o">=</span>
        <span class="nc">DatabaseConnection</span><span class="o">.</span><span class="na">getInstance</span><span class="o">();</span>
    <span class="nc">DatabaseConnection</span> <span class="n">conn2</span> <span class="o">=</span>
        <span class="nc">DatabaseConnection</span><span class="o">.</span><span class="na">getInstance</span><span class="o">();</span>

    <span class="n">assertSame</span><span class="o">(</span><span class="n">conn1</span><span class="o">,</span> <span class="n">conn2</span><span class="o">);</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/designpatterns/singleton/DatabaseConnectionTest.java">View full test file</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="nd">@DisplayName</span><span class="p">(</span><span class="s">"Should handle concurrent access"</span><span class="p">)</span>
<span class="k">fun</span> <span class="nf">testConcurrentAccess</span><span class="p">()</span> <span class="p">{</span>
    <span class="k">val</span> <span class="py">numThreads</span> <span class="p">=</span> <span class="m">100</span>
    <span class="k">val</span> <span class="py">executor</span> <span class="p">=</span>
        <span class="nc">Executors</span><span class="p">.</span><span class="nf">newFixedThreadPool</span><span class="p">(</span><span class="m">10</span><span class="p">)</span>

    <span class="nf">repeat</span><span class="p">(</span><span class="n">numThreads</span><span class="p">)</span> <span class="p">{</span>
        <span class="n">executor</span><span class="p">.</span><span class="nf">submit</span> <span class="p">{</span>
            <span class="nc">DatabaseConnection</span><span class="p">.</span><span class="nf">connect</span><span class="p">()</span>
        <span class="p">}</span>
    <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/designpatterns/singleton/DatabaseConnectionTest.kt">View full test file</a></p>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="nd">@DisplayName</span><span class="o">(</span><span class="s">"Should handle concurrent access"</span><span class="o">)</span>
<span class="k">def</span> <span class="nf">testConcurrentAccess</span><span class="o">():</span> <span class="kt">Unit</span> <span class="o">=</span> <span class="o">{</span>
  <span class="k">val</span> <span class="n">numThreads</span> <span class="o">=</span> <span class="mi">100</span>
  <span class="k">val</span> <span class="n">executor</span> <span class="o">=</span>
    <span class="nc">Executors</span><span class="o">.</span><span class="n">newFixedThreadPool</span><span class="o">(</span><span class="mi">10</span><span class="o">)</span>

  <span class="k">for</span> <span class="o">(</span><span class="n">_</span> <span class="o"><-</span> <span class="mi">1</span> <span class="n">to</span> <span class="n">numThreads</span><span class="o">)</span> <span class="o">{</span>
    <span class="n">executor</span><span class="o">.</span><span class="n">submit</span><span class="o">(...)</span>
  <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/test/scala/io/github/sps23/designpatterns/singleton/DatabaseConnectionTest.scala">View full test file</a></p>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="nd">@DisplayName</span><span class="o">(</span><span class="s">"Should handle concurrent access"</span><span class="o">)</span>
<span class="k">def</span> <span class="nf">testConcurrentAccess</span><span class="o">():</span> <span class="kt">Unit</span> <span class="o">=</span>
  <span class="k">val</span> <span class="n">numThreads</span> <span class="o">=</span> <span class="mi">100</span>
  <span class="k">val</span> <span class="n">executor</span> <span class="o">=</span>
    <span class="nc">Executors</span><span class="o">.</span><span class="nf">newFixedThreadPool</span><span class="o">(</span><span class="mi">10</span><span class="o">)</span>

  <span class="k">for</span> <span class="n">_</span> <span class="o"><-</span> <span class="mi">1</span> <span class="n">to</span> <span class="n">numThreads</span> <span class="k">do</span>
    <span class="n">executor</span><span class="o">.</span><span class="nf">submit</span><span class="o">(...)</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/designpatterns/singleton/DatabaseConnectionTest.scala">View full test file</a></p>
</div>
</div>

All tests verify:
- **Single instance**: Multiple calls to `getInstance()` or direct access returns the same object (using `assertSame`)
- **Thread safety**: 100 concurrent threads all access the singleton without creating duplicates
- **No exceptions**: Connect/disconnect operations complete without errors under concurrent load
- **Lazy initialization**: Tests run without blocking even with 100 threads

---

## Interview Q&A: Singleton Pattern in Practice

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>What is the purpose of the Singleton pattern?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      A singleton makes sure one class has only one shared instance in the application. This is useful for things like a configuration manager, a connection pool, or a process-wide logger, where having two copies would cause confusion or duplicate work. The goal is not to make everything global by default; it is to limit a real resource to one well-defined instance.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Why not just use a static field instead of a Singleton?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      A static field can give you one instance, but it often gives you less structure and more hidden state. A singleton is a class with a clear lifecycle and a single entry point, which makes the design easier to understand and test. Static state is also easier to misuse because it can be mutated from many places. With a singleton, the class still owns the behavior and lifecycle, which is usually a cleaner idea.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Can a singleton be broken?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Yes, if you are careless. In Java, reflection can sometimes create a second instance, and serialization can also create a new object during deserialization. That is why singletons need careful implementation details, such as a private constructor guard and a `readResolve()` method when needed. In Kotlin and Scala, the language-level object syntax helps a lot because the runtime enforces the single-instance model much more naturally.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>When is a singleton a good idea?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      It is a good idea when you truly have one shared resource or one source of truth. Examples include a configuration holder, a logger, or a connection manager that is supposed to be shared by the whole application. It is a bad idea when people use it as a shortcut for global mutable state. If the state changes often and many parts of the app depend on it, you usually want dependency injection or a narrower service instead.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Why do Scala and Kotlin make singletons easier?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      In Kotlin and Scala, the language gives you a built-in singleton object, so the compiler handles the design for you. That means less boilerplate and fewer easy-to-miss mistakes. In Java, you need to be more careful with the constructor, serialization, and reflection edge cases. The idea is the same, but the modern JVM languages make the safe version simpler to write and easier to read.
    </div>
  </details>
</div>

## Code Samples

All examples in this post are available in the repository:

**Implementation files:**
- **Java 21:** [DatabaseConnection.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/designpatterns/singleton/DatabaseConnection.java), [LazyDatabaseConnection.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/designpatterns/singleton/LazyDatabaseConnection.java), [HolderDatabaseConnection.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/designpatterns/singleton/HolderDatabaseConnection.java)
- **Kotlin:** [DatabaseConnection.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/designpatterns/singleton/DatabaseConnection.kt)
- **Scala 2:** [DatabaseConnection.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/main/scala/io/github/sps23/designpatterns/singleton/DatabaseConnection.scala)
- **Scala 3:** [DatabaseConnection.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/designpatterns/singleton/DatabaseConnection.scala)

**Test files:**
- **Java 21:** [DatabaseConnectionTest.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/designpatterns/singleton/DatabaseConnectionTest.java)
- **Kotlin:** [DatabaseConnectionTest.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/designpatterns/singleton/DatabaseConnectionTest.kt)
- **Scala 2:** [DatabaseConnectionTest.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/test/scala/io/github/sps23/designpatterns/singleton/DatabaseConnectionTest.scala)
- **Scala 3:** [DatabaseConnectionTest.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/designpatterns/singleton/DatabaseConnectionTest.scala)

---

## Key Takeaways

1. **In Java 21**: Use the **holder pattern** for lazy initialization, or eager initialization if resources aren't a concern. Remember `readResolve()` for serialization.

2. **In Kotlin/Scala**: Use the `object` keyword. It's idiomatic, thread-safe by default, and handles lazy initialization automatically.

3. **Singletons are often overused**: Consider dependency injection first—it's usually more testable and flexible.

4. **When a singleton makes sense**: Loggers, connection pools, and immutable configuration objects are legitimate use cases.

5. **Thread safety and serialization are non-negotiable**: If you implement a singleton, handle both correctly.

---

*This is part of our Design Patterns in JVM Languages series. Check out the [full design patterns guide]({{ site.baseurl }}/interview/2026/07/26/design-patterns-guide-jvm) for more patterns and interview preparation.*
