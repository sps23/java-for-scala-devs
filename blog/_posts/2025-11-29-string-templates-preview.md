---
layout: post
title: "String Templates (Preview) - Safe String Interpolation"
description: "Java 21 String Templates preview - build safe SQL queries with STR and FMT processors, create custom template processors, and compare with Scala and Kotlin interpolation."
date: 2025-11-29 21:00:00 +0000
categories: interview
tags: java java21 scala kotlin string-templates interview-preparation preview
---

This is Part 8 of our Java 21 Interview Preparation series. We'll explore Java 21's String Templates preview feature, build a SQL query builder that safely interpolates parameters, and compare with Scala 3 and Kotlin approaches.

## The Problem: Safe String Interpolation

Building dynamic strings is a common task, but it can be dangerous when user input is involved. Consider building a SQL query:

```java
// DANGEROUS: SQL Injection vulnerability!
String query = "SELECT * FROM users WHERE name = '" + userInput + "'";
```

If `userInput` is `"'; DROP TABLE users; --"`, this becomes a SQL injection attack. We need safer ways to build strings with embedded values.

## Java 21 String Templates (Preview)

Java 21 introduces String Templates as a preview feature, providing safer and more expressive string interpolation. Note: This requires the `--enable-preview` flag.

### STR Template Processor

The `STR` processor performs simple string interpolation:

```java
import static java.lang.StringTemplate.STR;

String name = "Alice";
int age = 30;
String greeting = STR."Hello, \{name}! You are \{age} years old.";
// Result: "Hello, Alice! You are 30 years old."
```

### Expression Interpolation

You can include any expression in the template:

```java
int x = 5, y = 3;
String math = STR."Sum: \{x} + \{y} = \{x + y}, Product: \{x * y}";
// Result: "Sum: 5 + 3 = 8, Product: 15"
```

### Multi-line Templates

String templates work seamlessly with text blocks:

```java
String json = STR."""
    {
        "name": "\{name}",
        "email": "\{email}",
        "active": \{active}
    }
    """;
```

## Building a Safe SQL Query Builder

Let's create a query builder that prevents SQL injection by using parameterized queries:

### Java 21 Implementation

```java
public static final class SafeQueryBuilder {
    private final StringBuilder query;
    private final List<Object> parameters;
    private boolean hasWhereClause;

    public SafeQueryBuilder() {
        this.query = new StringBuilder();
        this.parameters = new ArrayList<>();
        this.hasWhereClause = false;
    }

    public SafeQueryBuilder select(String... columns) {
        query.append("SELECT ");
        query.append(String.join(", ", columns));
        return this;
    }

    public SafeQueryBuilder from(String table) {
        query.append(" FROM ").append(table);
        return this;
    }

    public SafeQueryBuilder where(String column, String operator, Object value) {
        if (!hasWhereClause) {
            query.append(" WHERE ");
            hasWhereClause = true;
        } else {
            query.append(" AND ");
        }
        query.append(column).append(" ").append(operator).append(" ?");
        parameters.add(value);
        return this;
    }

    public String getQuery() {
        return query.toString();
    }

    public List<Object> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public String toDebugString() {
        return STR."""
            Query: \{query}
            Parameters: \{parameters}
            """;
    }
}
```

Usage:

```java
SafeQueryBuilder query = new SafeQueryBuilder()
    .select("id", "name", "email", "age")
    .from("users")
    .where("age", ">=", 18)
    .where("status", "=", "active")
    .orderBy("name", "ASC")
    .limit(100);

System.out.println(query.toDebugString());
// Query: SELECT id, name, email, age FROM users WHERE age >= ? AND status = ? ORDER BY name ASC LIMIT ?
// Parameters: [18, active, 100]
```

### Unsafe vs Safe Comparison

```java
// UNSAFE - vulnerable to SQL injection
public static String unsafeQuery(String name) {
    return "SELECT * FROM users WHERE name = '" + name + "'";
}

// SAFE - parameterized query
public static SafeQueryBuilder safeQuery(String name) {
    return new SafeQueryBuilder()
        .select("*")
        .from("users")
        .where("name", "=", name);
}

// With malicious input: "'; DROP TABLE users; --"
String unsafe = unsafeQuery(maliciousInput);
// Result: SELECT * FROM users WHERE name = ''; DROP TABLE users; --'

SafeQueryBuilder safe = safeQuery(maliciousInput);
// Query: SELECT * FROM users WHERE name = ?
// Parameters: ["'; DROP TABLE users; --"]  <- Safely parameterized!
```

## Comparison: Java 21 vs Scala 3 vs Kotlin

### Basic String Interpolation

<div class="code-tabs" data-tabs-id="tabs-1">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kn">import static</span> <span class="nn">java.lang.StringTemplate.STR</span><span class="o">;</span>

<span class="nc">String</span> <span class="n">greeting</span> <span class="o">=</span> <span class="no">STR</span><span class="o">.</span><span class="s">"Hello, \{name}! You are \{age} years old."</span><span class="o">;</span>
<span class="nc">String</span> <span class="n">math</span> <span class="o">=</span> <span class="no">STR</span><span class="o">.</span><span class="s">"Sum: \{x + y}, Product: \{x * y}"</span><span class="o">;</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">val</span> <span class="nv">greeting</span> <span class="k">=</span> <span class="nv">s</span><span class="s">"Hello, $name! You are $age years old."</span>
<span class="k">val</span> <span class="nv">math</span> <span class="k">=</span> <span class="nv">s</span><span class="s">"Sum: ${x + y}, Product: ${x * y}"</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">val</span> <span class="py">greeting</span> <span class="p">=</span> <span class="s">"Hello, $name! You are $age years old."</span>
<span class="kd">val</span> <span class="py">math</span> <span class="p">=</span> <span class="s">"Sum: ${x + y}, Product: ${x * y}"</span>
</code></pre></div></div>
</div>
</div>

### Printf-style Formatting

<div class="code-tabs" data-tabs-id="tabs-2">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nc">String</span> <span class="n">formatted</span> <span class="o">=</span> <span class="no">STR</span><span class="o">.</span><span class="s">"Item: \{item}, Price: $\{String.format(\"%.2f\", price)}"</span><span class="o">;</span>
<span class="c1">// Or with text blocks</span>
<span class="nc">String</span> <span class="n">row</span> <span class="o">=</span> <span class="no">STR</span><span class="o">.</span><span class="s">"| \{String.format(\"%5d\", id)} | \{String.format(\"%-20s\", name)} |"</span><span class="o">;</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// f-interpolator provides printf-style formatting</span>
<span class="k">val</span> <span class="nv">formatted</span> <span class="k">=</span> <span class="nv">f</span><span class="s">"Item: $item, Price: $$$price%.2f"</span>
<span class="k">val</span> <span class="nv">row</span> <span class="k">=</span> <span class="nv">f</span><span class="s">"| $id%5d | $name%-20s |"</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">val</span> <span class="py">formatted</span> <span class="p">=</span> <span class="s">"Item: $item, Price: $%.2f"</span><span class="p">.</span><span class="nf">format</span><span class="p">(</span><span class="n">price</span><span class="p">)</span>
<span class="kd">val</span> <span class="py">row</span> <span class="p">=</span> <span class="s">"| %5d | %-20s |"</span><span class="p">.</span><span class="nf">format</span><span class="p">(</span><span class="n">id</span><span class="p">,</span> <span class="n">name</span><span class="p">)</span>
</code></pre></div></div>
</div>
</div>

### Multi-line Strings

<div class="code-tabs" data-tabs-id="tabs-3">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nc">String</span> <span class="n">json</span> <span class="o">=</span> <span class="no">STR</span><span class="o">.</span><span class="s">"""
    {
        "name": "\{name}",
        "email": "\{email}"
    }
    """</span><span class="o">;</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">val</span> <span class="nv">json</span> <span class="k">=</span> <span class="nv">s</span><span class="s">"""{
   |    "name": "$name",
   |    "email": "$email"
   |}"""</span><span class="o">.</span><span class="py">stripMargin</span>
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

### Safe Query Builder (Immutable Pattern)

<div class="code-tabs" data-tabs-id="tabs-4">
<div class="tab-buttons">
<button class="tab-button active" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">final</span> <span class="k">case</span> <span class="k">class</span> <span class="nc">SafeQueryBuilder</span><span class="o">(</span>
    <span class="n">query</span><span class="k">:</span> <span class="kt">String</span> <span class="o">=</span> <span class="s">""</span><span class="o">,</span>
    <span class="n">parameters</span><span class="k">:</span> <span class="kt">List</span><span class="o">[</span><span class="kt">Any</span><span class="o">]</span> <span class="k">=</span> <span class="nv">List</span><span class="o">.</span><span class="py">empty</span><span class="o">,</span>
    <span class="n">hasWhereClause</span><span class="k">:</span> <span class="kt">Boolean</span> <span class="o">=</span> <span class="kc">false</span>
<span class="o">):</span>
  <span class="k">def</span> <span class="nf">select</span><span class="o">(</span><span class="n">columns</span><span class="k">:</span> <span class="kt">String*</span><span class="o">)</span><span class="k">:</span> <span class="kt">SafeQueryBuilder</span> <span class="o">=</span>
    <span class="nf">copy</span><span class="o">(</span><span class="n">query</span> <span class="k">=</span> <span class="nv">s</span><span class="s">"SELECT ${columns.mkString(\", \")}"</span><span class="o">)</span>

  <span class="k">def</span> <span class="nf">from</span><span class="o">(</span><span class="n">table</span><span class="k">:</span> <span class="kt">String</span><span class="o">)</span><span class="k">:</span> <span class="kt">SafeQueryBuilder</span> <span class="o">=</span>
    <span class="nf">copy</span><span class="o">(</span><span class="n">query</span> <span class="k">=</span> <span class="nv">s</span><span class="s">"$query FROM $table"</span><span class="o">)</span>

  <span class="k">def</span> <span class="nf">where</span><span class="o">(</span><span class="n">column</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">operator</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">value</span><span class="k">:</span> <span class="kt">Any</span><span class="o">)</span><span class="k">:</span> <span class="kt">SafeQueryBuilder</span> <span class="o">=</span>
    <span class="nf">if</span> <span class="n">hasWhereClause</span> <span class="k">then</span>
      <span class="nf">copy</span><span class="o">(</span>
        <span class="n">query</span> <span class="k">=</span> <span class="nv">s</span><span class="s">"$query AND $column $operator ?"</span><span class="o">,</span>
        <span class="n">parameters</span> <span class="k">=</span> <span class="n">parameters</span> <span class="o">:+</span> <span class="n">value</span>
      <span class="o">)</span>
    <span class="k">else</span>
      <span class="nf">copy</span><span class="o">(</span>
        <span class="n">query</span> <span class="k">=</span> <span class="nv">s</span><span class="s">"$query WHERE $column $operator ?"</span><span class="o">,</span>
        <span class="n">parameters</span> <span class="k">=</span> <span class="n">parameters</span> <span class="o">:+</span> <span class="n">value</span><span class="o">,</span>
        <span class="n">hasWhereClause</span> <span class="k">=</span> <span class="kc">true</span>
      <span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">data</span> <span class="kd">class</span> <span class="nc">SafeQueryBuilder</span><span class="p">(</span>
    <span class="kd">val</span> <span class="py">query</span><span class="p">:</span> <span class="nc">String</span> <span class="p">=</span> <span class="s">""</span><span class="p">,</span>
    <span class="kd">val</span> <span class="py">parameters</span><span class="p">:</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">Any</span><span class="p">&gt;</span> <span class="p">=</span> <span class="nf">emptyList</span><span class="p">(),</span>
    <span class="kd">val</span> <span class="py">hasWhereClause</span><span class="p">:</span> <span class="nc">Boolean</span> <span class="p">=</span> <span class="k">false</span>
<span class="p">)</span> <span class="p">{</span>
    <span class="k">fun</span> <span class="nf">select</span><span class="p">(</span><span class="k">vararg</span> <span class="n">columns</span><span class="p">:</span> <span class="nc">String</span><span class="p">):</span> <span class="nc">SafeQueryBuilder</span> <span class="p">=</span>
        <span class="nf">copy</span><span class="p">(</span><span class="n">query</span> <span class="p">=</span> <span class="s">"SELECT ${columns.joinToString(\", \")}"</span><span class="p">)</span>

    <span class="k">fun</span> <span class="nf">from</span><span class="p">(</span><span class="n">table</span><span class="p">:</span> <span class="nc">String</span><span class="p">):</span> <span class="nc">SafeQueryBuilder</span> <span class="p">=</span>
        <span class="nf">copy</span><span class="p">(</span><span class="n">query</span> <span class="p">=</span> <span class="s">"$query FROM $table"</span><span class="p">)</span>

    <span class="k">fun</span> <span class="nf">where</span><span class="p">(</span><span class="n">column</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span> <span class="n">operator</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span> <span class="n">value</span><span class="p">:</span> <span class="nc">Any</span><span class="p">):</span> <span class="nc">SafeQueryBuilder</span> <span class="p">=</span>
        <span class="k">if</span> <span class="p">(</span><span class="n">hasWhereClause</span><span class="p">)</span> <span class="p">{</span>
            <span class="nf">copy</span><span class="p">(</span>
                <span class="n">query</span> <span class="p">=</span> <span class="s">"$query AND $column $operator ?"</span><span class="p">,</span>
                <span class="n">parameters</span> <span class="p">=</span> <span class="n">parameters</span> <span class="p">+</span> <span class="n">value</span>
            <span class="p">)</span>
        <span class="p">}</span> <span class="k">else</span> <span class="p">{</span>
            <span class="nf">copy</span><span class="p">(</span>
                <span class="n">query</span> <span class="p">=</span> <span class="s">"$query WHERE $column $operator ?"</span><span class="p">,</span>
                <span class="n">parameters</span> <span class="p">=</span> <span class="n">parameters</span> <span class="p">+</span> <span class="n">value</span><span class="p">,</span>
                <span class="n">hasWhereClause</span> <span class="p">=</span> <span class="k">true</span>
            <span class="p">)</span>
        <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
</div>
</div>

### Custom String Interpolator (Scala 3)

Scala allows creating custom string interpolators:

```scala
extension (sc: StringContext)
  def sql(args: Any*): SafeQueryBuilder =
    val parts = sc.parts.iterator
    val builder = new StringBuilder(parts.next())

    args.foreach { arg =>
      builder.append("?")
      if parts.hasNext then builder.append(parts.next())
    }

    SafeQueryBuilder(builder.toString(), args.toList)

// Usage
val tableName = "products"
val minPrice = 10.0
val category = "electronics"
val query = sql"SELECT * FROM $tableName WHERE price > $minPrice AND category = $category"
// Query: SELECT * FROM products WHERE price > ? AND category = ?
// Parameters: [10.0, electronics]
```

## HTML Template with XSS Protection

Building HTML safely requires escaping user input:

```java
public static String htmlTemplate(String title, String heading, String content) {
    return STR."""
        <!DOCTYPE html>
        <html>
        <head>
            <title>\{escapeHtml(title)}</title>
        </head>
        <body>
            <h1>\{escapeHtml(heading)}</h1>
            <div class="content">
                \{escapeHtml(content)}
            </div>
        </body>
        </html>
        """;
}

public static String escapeHtml(String input) {
    if (input == null) return "";
    return input
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
}
```

## Feature Comparison Table

| Feature | Java 21 | Scala 3 | Kotlin |
|---------|---------|---------|--------|
| Basic interpolation | `STR."\{var}"` | `s"$var"` | `"$var"` |
| Expression interpolation | `STR."\{expr}"` | `s"${expr}"` | `"${expr}"` |
| Printf formatting | Manual with `String.format()` | `f"$var%.2f"` | `"%.2f".format(var)` |
| Multi-line strings | Text blocks + STR | Triple quotes + stripMargin | Triple quotes + trimIndent |
| Custom interpolators | Template processors | Extension methods on StringContext | Not built-in |
| Preview/Stable | Preview (Java 21) | Stable (since Scala 2.10) | Stable (since Kotlin 1.0) |

## Key Concepts

### 1. STR Template Processor

The standard processor for simple string interpolation. Embeds values directly into the string.

### 2. FMT Template Processor

Combines interpolation with printf-style formatting (experimental).

### 3. Custom Template Processors

Create domain-specific string handling for safety and validation.

### 4. Safety Benefits

- Parameterized queries prevent SQL injection
- HTML escaping prevents XSS attacks
- Type-safe interpolation catches errors at compile time

## Best Practices

1. **Never concatenate user input** directly into SQL queries
2. **Use parameterized queries** with placeholders and separate parameter lists
3. **Escape output** when embedding in HTML, JSON, or other formats
4. **Validate input** before using in templates
5. **Use immutable builders** (like Scala/Kotlin data classes) for safer query construction
6. **Prefer built-in interpolation** over string concatenation for readability

## Log Message Example

String templates work great for structured logging:

```java
public static String logMessage(String level, String component, String message) {
    return STR."[\{java.time.LocalDateTime.now()}] [\{level}] [\{component}] \{message}";
}

System.out.println(logMessage("INFO", "UserService", "User logged in"));
// [2025-11-29T21:00:00.000] [INFO] [UserService] User logged in
```

## Code Samples

See the complete implementations in our repository:

- [Java 21 StringTemplates.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/stringtemplates/StringTemplates.java)
- [Scala 3 StringTemplates.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/stringtemplates/StringTemplates.scala)
- [Kotlin StringTemplates.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/stringtemplates/StringTemplates.kt)

## Conclusion

Java 21's String Templates bring modern string interpolation to Java, closing the gap with Scala and Kotlin. Key takeaways:

- **Expressive syntax** with `STR."\{expression}"` for clean string building
- **Multi-line support** with text blocks
- **Safety-first design** enables custom processors for domain-specific validation
- **Preview feature** - syntax may evolve in future Java versions

For Scala and Kotlin developers, the concepts are familiar but the syntax differs. All three languages now provide excellent support for safe, readable string interpolation.

---

*This is Part 8 of our Java 21 Interview Preparation series. Check out [Part 7: Virtual Threads and Structured Concurrency](/interview/2025/11/29/virtual-threads-and-structured-concurrency.html) and the [full preparation plan](/interview/2025/11/28/java21-interview-preparation-plan.html).*
