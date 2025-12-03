---
layout: post
title: "String Manipulation with Modern APIs"
description: "Learn modern Java String APIs (Java 11-17) including isBlank(), lines(), strip(), indent(), and text blocks - with comparisons to Scala 3 and Kotlin approaches."
date: 2025-11-28 22:00:00 +0000
categories: [interview]
tags: [java, java21, scala, kotlin, strings, interview-preparation]
---

This is Part 2 of our Java 21 Interview Preparation series. We'll explore modern String API enhancements introduced in Java 11-17, comparing them with Scala 3 and Kotlin approaches.

## The Problem: Processing Multi-line Text

A common programming task involves processing multi-line text: stripping indentation, filtering blank lines, and formatting output. Let's see how this task evolved from Java 8 to modern Java 21, and compare with idiomatic Scala 3 and Kotlin solutions.

## Java 8 Style - Verbose and Manual

Before Java 11, string processing required manual operations:

```java
public static String processJava8Style(String text) {
    if (text == null || text.trim().isEmpty()) {
        return "";
    }

    String[] lines = text.split("\n");
    StringBuilder result = new StringBuilder();

    for (String line : lines) {
        String trimmed = line.trim();
        if (!trimmed.isEmpty()) {
            result.append(trimmed).append("\n");
        }
    }

    // Remove trailing newline if present
    if (!result.isEmpty() && result.charAt(result.length() - 1) == '\n') {
        result.deleteCharAt(result.length() - 1);
    }

    return result.toString();
}
```

This approach requires:
- Manual splitting by newline character
- Manual trimming of each line
- Manual filtering of empty lines
- StringBuilder for output construction

## Modern Java 11+ Style - Fluent and Expressive

Java 11+ introduced several String API enhancements that enable a fluent, functional approach:

```java
public static String processModernStyle(String text) {
    if (text == null || text.isBlank()) {
        return "";
    }

    return text.lines()
            .map(String::strip)
            .filter(line -> !line.isBlank())
            .collect(Collectors.joining("\n"));
}
```

**Key APIs used:**
- `lines()` - Splits into a Stream of lines (Java 11)
- `strip()` - Removes leading/trailing whitespace, Unicode-aware (Java 11)
- `isBlank()` - Checks for empty or whitespace-only strings (Java 11)

## Key String API Features (Java 11-17+)

### 1. String.isBlank() vs isEmpty() (Java 11)

```java
String empty = "";
String whitespace = "   \t\n   ";
String text = "hello";

// isEmpty() - only checks length == 0
empty.isEmpty();      // true
whitespace.isEmpty(); // false
text.isEmpty();       // false

// isBlank() - checks empty OR only whitespace
empty.isBlank();      // true
whitespace.isBlank(); // true
text.isBlank();       // false
```

### 2. String.lines() (Java 11)

```java
String multiline = """
    First line
    Second line
    Third line
    """;

multiline.lines()
    .forEach(System.out::println);
// Output:
// First line
// Second line
// Third line
```

### 3. String.strip(), stripLeading(), stripTrailing() (Java 11)

Unlike `trim()` which only removes characters ≤ U+0020, `strip()` methods are Unicode-aware:

```java
String text = "\u00A0 hello \u00A0"; // Non-breaking spaces

text.trim();   // "  hello  " (doesn't remove Unicode whitespace)
text.strip();  // "hello" (removes all Unicode whitespace)
```

### 4. String.indent() (Java 12)

```java
String code = """
    public void hello() {
        System.out.println("Hello");
    }
    """;

// Add 4 spaces to each line
code.indent(4);

// Remove up to 2 spaces from each line
code.indent(-2);
```

### 5. String.transform() (Java 12)

Enables fluent chaining of arbitrary string operations:

```java
String result = "  hello world  "
    .transform(String::strip)
    .transform(String::toUpperCase)
    .transform(s -> "[" + s + "]");
// Result: "[HELLO WORLD]"
```

### 6. Text Blocks (Java 15)

Multi-line string literals with natural formatting:

```java
String json = """
    {
        "name": "%s",
        "email": "%s",
        "active": %b
    }
    """;

System.out.println(json.formatted("Alice", "alice@example.com", true));
```

### 7. String.formatted() (Java 15)

Instance method alternative to `String.format()`:

```java
// Traditional
String.format("Name: %s, Age: %d", name, age);

// Modern (Java 15+)
"Name: %s, Age: %d".formatted(name, age);
```

## Comparison: Java 21 vs Scala 3 vs Kotlin

### Processing Multi-line Text

<div class="code-tabs" data-tabs-id="tabs-1">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">static</span> <span class="nc">String</span> <span class="nf">processText</span><span class="o">(</span><span class="nc">String</span> <span class="n">text</span><span class="o">)</span> <span class="o">{</span>
    <span class="k">if</span> <span class="o">(</span><span class="n">text</span> <span class="o">==</span> <span class="kc">null</span> <span class="o">||</span> <span class="n">text</span><span class="o">.</span><span class="na">isBlank</span><span class="o">())</span> <span class="o">{</span>
        <span class="k">return</span> <span class="s">""</span><span class="o">;</span>
    <span class="o">}</span>
    <span class="k">return</span> <span class="n">text</span><span class="o">.</span><span class="na">lines</span><span class="o">()</span>
        <span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="nc">String</span><span class="o">::</span><span class="n">strip</span><span class="o">)</span>
        <span class="o">.</span><span class="na">filter</span><span class="o">(</span><span class="n">line</span> <span class="o">-&gt;</span> <span class="o">!</span><span class="n">line</span><span class="o">.</span><span class="na">isBlank</span><span class="o">())</span>
        <span class="o">.</span><span class="na">collect</span><span class="o">(</span><span class="nc">Collectors</span><span class="o">.</span><span class="na">joining</span><span class="o">(</span><span class="s">"\n"</span><span class="o">));</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">def</span> <span class="nf">processText</span><span class="o">(</span><span class="n">text</span><span class="k">:</span> <span class="kt">String</span><span class="o">)</span><span class="k">:</span> <span class="kt">String</span> <span class="k">=</span>
  <span class="nc">Option</span><span class="o">(</span><span class="n">text</span><span class="o">)</span>
    <span class="o">.</span><span class="py">filter</span><span class="o">(</span><span class="k">_</span><span class="o">.</span><span class="py">trim</span><span class="o">.</span><span class="py">nonEmpty</span><span class="o">)</span>
    <span class="o">.</span><span class="py">map</span><span class="o">(</span><span class="k">_</span><span class="o">.</span><span class="py">linesIterator</span>
      <span class="o">.</span><span class="py">map</span><span class="o">(</span><span class="k">_</span><span class="o">.</span><span class="py">strip</span><span class="o">)</span>
      <span class="o">.</span><span class="py">filter</span><span class="o">(</span><span class="k">_</span><span class="o">.</span><span class="py">nonEmpty</span><span class="o">)</span>
      <span class="o">.</span><span class="py">mkString</span><span class="o">(</span><span class="s">"\n"</span><span class="o">))</span>
    <span class="o">.</span><span class="py">getOrElse</span><span class="o">(</span><span class="s">""</span><span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">fun</span> <span class="nf">processText</span><span class="p">(</span><span class="n">text</span><span class="p">:</span> <span class="nc">String</span><span class="p">?)</span><span class="p">:</span> <span class="nc">String</span> <span class="p">{</span>
    <span class="k">if</span> <span class="p">(</span><span class="n">text</span><span class="p">.</span><span class="nf">isNullOrBlank</span><span class="p">())</span> <span class="k">return</span> <span class="s">""</span>
    <span class="k">return</span> <span class="n">text</span><span class="p">.</span><span class="nf">lines</span><span class="p">()</span>
        <span class="p">.</span><span class="nf">map</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="nf">trim</span><span class="p">()</span> <span class="p">}</span>
        <span class="p">.</span><span class="nf">filter</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="nf">isNotBlank</span><span class="p">()</span> <span class="p">}</span>
        <span class="p">.</span><span class="nf">joinToString</span><span class="p">(</span><span class="s">"\n"</span><span class="p">)</span>
<span class="p">}</span>
</code></pre></div></div>
</div>
</div>

### Multi-line Strings

<div class="table-wrapper" markdown="1">

| Feature | Java 21 | Scala 3 | Kotlin |
|---------|---------|---------|--------|
| Multi-line literal | Text blocks (`"""..."""`) | Triple quotes with `stripMargin` | Triple quotes with `trimIndent` |
| String interpolation | `formatted()` method | `s"..."`, `f"..."` | `"$variable"`, `"${expr}"` |
| Margin handling | Automatic | `\|` with `stripMargin` | Auto with `trimIndent` |

</div>

### String Checking Methods

<div class="table-wrapper" markdown="1">

| Check | Java 21 | Scala 3 | Kotlin |
|-------|---------|---------|--------|
| Empty | `isEmpty()` | `isEmpty` | `isEmpty()` |
| Blank | `isBlank()` | `isBlank` | `isBlank()` |
| Null-safe blank | Manual | `Option(s).exists(_.nonEmpty)` | `isNullOrBlank()` |

</div>

### Creating Multi-line Strings

<div class="code-tabs" data-tabs-id="tabs-2">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nc">String</span> <span class="n">json</span> <span class="o">=</span> <span class="s">"""
    {
        "name": "%s",
        "email": "%s"
    }
    """</span><span class="o">.</span><span class="na">formatted</span><span class="o">(</span><span class="n">name</span><span class="o">,</span> <span class="n">email</span><span class="o">);</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">val</span> <span class="nv">json</span> <span class="k">=</span> <span class="nv">s</span><span class="s">"""{

<div class="table-wrapper" markdown="1">

  |  "name": "$name",
  |  "email": "$email"
  |}"""</span><span class="o">.</span><span class="py">stripMargin</span>

</div>

</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">val</span> <span class="py">json</span> <span class="p">=</span> <span class="s">"""
    {
      "name": "$name",
      "email": "$email"
    }
"""</span><span class="p">.</span><span class="nf">trimIndent</span><span class="p">()</span>
</code></pre></div></div>
</div>
</div>


## Complete Example: Text Processing Pipeline

Here's a complete example combining multiple modern String APIs:

### Java 21

```java
public static String processCompletePipeline(String text) {
    if (text == null || text.isBlank()) {
        return "";
    }

    return text.lines()
        .map(String::strip)
        .filter(line -> !line.isBlank())
        .map(line -> "• %s".formatted(line))
        .collect(Collectors.joining("\n"))
        .transform(result -> "Processed Content:\n" + result)
        .transform(result -> result.indent(2).stripTrailing());
}
```

### Scala 3

```scala
def processCompletePipeline(text: String): String =
  Option(text)
    .filter(_.trim.nonEmpty)
    .map { t =>
      val processed = t.linesIterator
        .map(_.strip)
        .filter(_.nonEmpty)
        .map(line => s"• $line")
        .mkString("\n")
      
      s"Processed Content:\n$processed"
        .linesIterator
        .map(line => s"  $line")
        .mkString("\n")
    }
    .getOrElse("")
```

### Kotlin

```kotlin
fun processCompletePipeline(text: String?): String {
    if (text.isNullOrBlank()) return ""

    val processed = text.lines()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .joinToString("\n") { "• $it" }

    return "Processed Content:\n$processed"
        .lines()
        .joinToString("\n") { "  $it" }
}
```

## Extension Methods: Scala and Kotlin Advantage

Both Scala and Kotlin allow extending String with custom methods:

### Scala 3 Extension Methods

```scala
extension (s: String)
  def isNullOrBlank: Boolean =
    s == null || s.isBlank

  def truncate(maxLength: Int): String =
    if s.length <= maxLength then s
    else s.take(maxLength - 3) + "..."

  def wrapWith(delimiter: String): String =
    s"$delimiter$s$delimiter"

// Usage
"Hello, World!".truncate(8)  // "Hello..."
"Hello".wrapWith("***")       // "***Hello***"
```

### Kotlin Extension Functions

```kotlin
fun String.truncate(maxLength: Int): String =
    if (length <= maxLength) this
    else take(maxLength - 3) + "..."

fun String.wrapWith(delimiter: String): String =
    "$delimiter$this$delimiter"

// Usage
"Hello, World!".truncate(8)  // "Hello..."
"Hello".wrapWith("***")       // "***Hello***"
```

## Summary: Feature Comparison

<div class="table-wrapper" markdown="1">

| Feature | Java 8 | Java 21 | Scala 3 | Kotlin |
|---------|--------|---------|---------|--------|
| Check blank | `text.trim().isEmpty()` | `text.isBlank()` | `text.isBlank` | `text.isBlank()` |
| Split lines | `text.split("\n")` | `text.lines()` | `text.linesIterator` | `text.lines()` |
| Strip whitespace | `text.trim()` | `text.strip()` | `text.strip` | `text.trim()` |
| Multi-line strings | Concatenation | Text blocks | Triple quotes | Triple quotes |
| String formatting | `String.format()` | `"...".formatted()` | `s"..."`, `f"..."` | `"$var"` |
| Indentation | Manual | `text.indent(n)` | Manual | `trimIndent()` |
| Functional transform | Manual | `text.transform(f)` | `Option(text).map(f)` | `text.let { f(it) }` |

</div>

## Best Practices

1. **Prefer `isBlank()` over `isEmpty()`** when checking for meaningful content
2. **Use `strip()` instead of `trim()`** for proper Unicode whitespace handling
3. **Use `lines()` for stream processing** multi-line text
4. **Use text blocks** for multi-line string literals (JSON, SQL, etc.)
5. **Chain operations with `transform()`** for readable pipelines
6. **Consider null safety** - Kotlin's `isNullOrBlank()` is convenient

## Code Samples

See the complete implementations in our repository:

- [Java 21 StringManipulation.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/strings/StringManipulation.java)
- [Scala 3 StringManipulation.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/strings/StringManipulation.scala)
- [Kotlin StringManipulation.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/strings/StringManipulation.kt)

## Conclusion

Java's String API has evolved significantly from Java 8 to Java 21. The modern APIs provide:

- **Cleaner code** with fluent, functional operations
- **Better Unicode support** with `strip()` and `isBlank()`
- **Natural multi-line strings** with text blocks
- **Fluent transformations** with `transform()` and `formatted()`

For Scala and Kotlin developers, the modern Java APIs feel more familiar and idiomatic. While Scala and Kotlin still offer advantages like extension methods and powerful string interpolation, Java 21 has closed much of the gap in string manipulation ergonomics.

---

*This is Part 2 of our Java 21 Interview Preparation series. Check out [Part 1: Immutable Data with Java Records](/interview/2025/11/28/immutable-data-with-java-records.html) and the [full preparation plan](/interview/2025/11/28/java21-interview-preparation-plan.html).*
