---
layout: post
title: "Immutable Data with Java Records"
description: "Master Java Records for immutable data classes - compare with Scala case classes and Kotlin data classes, learn validation patterns, and see before/after code examples."
date: 2025-11-26 21:00:00 +0000
updated: 2026-08-28 15:00:00 +0000
updated: 2026-08-29 14:00:00 +0000
categories: [interview]
tags: [java, java21, records, immutability, interview-preparation]
---

This is the first post in our Java 21 Interview Preparation series. We'll explore Java Records, one of the most significant additions for Scala developers coming to Java.

## The Problem: Immutable Data Classes in Java 8

Before Java 16, creating an immutable data class required significant boilerplate. Here's what it looked like:

```java
public final class EmployeeTraditional {
    private final long id;
    private final String name;
    private final String email;
    private final String department;
    private final double salary;

    public EmployeeTraditional(long id, String name, String email, 
            String department, double salary) {
        // Validation logic
        if (id <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        Objects.requireNonNull(name, "Employee name cannot be null");
        // ... more validation ...

        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.salary = salary;
    }

    // Getter methods
    public long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getDepartment() { return department; }
    public double getSalary() { return salary; }

    // Must manually implement equals()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeTraditional that = (EmployeeTraditional) o;
        return id == that.id 
            && Double.compare(salary, that.salary) == 0
            && Objects.equals(name, that.name) 
            && Objects.equals(email, that.email)
            && Objects.equals(department, that.department);
    }

    // Must manually implement hashCode()
    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, department, salary);
    }

    // Must manually implement toString()
    @Override
    public String toString() {
        return "EmployeeTraditional[id=" + id + ", name=" + name + 
               ", email=" + email + ", department=" + department + 
               ", salary=" + salary + "]";
    }
}
```

That's **over 60 lines** of code just to hold 5 fields! This verbosity led many developers to use libraries like Lombok or switch to Kotlin/Scala.

## The Solution: Java Records (Java 16+)

Java Records provide a concise way to declare immutable data classes. Here's the same class as a record:

```java
public record Employee(
    long id, 
    String name, 
    String email, 
    String department, 
    double salary
) {
    // Compact constructor for validation
    public Employee {
        if (id <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive");
        }
        Objects.requireNonNull(name, "Employee name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Employee name cannot be blank");
        }
        Objects.requireNonNull(email, "Employee email cannot be null");
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        Objects.requireNonNull(department, "Department cannot be null");
        if (salary < 0) {
            throw new IllegalArgumentException("Salary cannot be negative");
        }
    }
}
```

**Just ~25 lines** with full validation! The compiler automatically generates:
- A constructor with all parameters
- Accessor methods: `id()`, `name()`, `email()`, `department()`, `salary()`
- `equals()`, `hashCode()`, and `toString()` methods

## Key Concepts

### 1. Record Syntax and Components

The record declaration `record Employee(long id, String name, ...)` defines:
- **Components**: The fields (`id`, `name`, etc.)
- **Canonical constructor**: Takes all components as parameters
- **Accessor methods**: Named after the components (not `getId()`, just `id()`)

```java
var employee = new Employee(1L, "Alice", "alice@example.com", "Engineering", 75000.0);
System.out.println(employee.name());       // Alice
System.out.println(employee.department()); // Engineering
```

### 2. Compact Constructors for Validation

The compact constructor is unique to records. Notice there's no parameter list:

```java
public Employee {  // No parentheses with parameters!
    // Validation logic here
    if (id <= 0) {
        throw new IllegalArgumentException("Employee ID must be positive");
    }
    // Parameters are automatically assigned to fields at the end
}
```

This is cleaner than the traditional canonical constructor:

```java
// You can still use the canonical constructor if needed
public Employee(long id, String name, String email, String department, double salary) {
    // Manual validation and assignment
    this.id = id;
    this.name = name;
    // ...
}
```

### 3. Auto-generated Methods

Records automatically generate `equals()`, `hashCode()`, and `toString()`:

```java
var emp1 = new Employee(1L, "Alice", "alice@example.com", "Engineering", 75000.0);
var emp2 = new Employee(1L, "Alice", "alice@example.com", "Engineering", 75000.0);

System.out.println(emp1.equals(emp2));  // true
System.out.println(emp1.hashCode() == emp2.hashCode());  // true
System.out.println(emp1);  // Employee[id=1, name=Alice, email=alice@example.com, ...]
```

### 4. Adding Custom Methods

Records can have additional methods, static fields, and static methods:

```java
public record Employee(long id, String name, String email, String department, double salary) {
    
    // Custom instance method
    public String toFormattedString() {
        return String.format("Employee #%d: %s (%s) - %s - $%.2f", 
            id, name, email, department, salary);
    }
    
    // Static factory method
    public static Employee of(long id, String name, String email, 
            String department, double salary) {
        return new Employee(id, name, email, department, salary);
    }
}
```

## Comparison: Scala Case Class vs Java Record vs Kotlin Data Class

The table below summarizes the similarities and differences between Scala case classes, Java records, and Kotlin data classes for modeling immutable data:

<div class="table-wrapper" markdown="1">

| Feature                | Scala Case Class                | Java Record                        | Kotlin Data Class                  |
|------------------------|---------------------------------|------------------------------------|------------------------------------|
| Declaration            | `case class Employee(...)`      | `record Employee(...) {}`          | `data class Employee(...)`         |
| Immutable              | Yes                             | Yes                                | Yes (with `val` properties)        |
| Pattern matching       | Yes                             | Yes (Java 21+)                     | Yes (with `when`)                  |
| Auto `equals`/`hashCode` | Yes                           | Yes                                | Yes                                |
| Auto `toString`        | Yes                             | Yes                                | Yes                                |
| Copy method            | Built-in                        | Manual implementation needed        | Built-in (`copy()`)                |
| Validation             | `require(...)` in body          | Compact constructor                | `require(...)` in `init` block     |
| Accessor naming        | `employee.name`                 | `employee.name()`                  | `employee.name`                    |

</div>

### Side-by-Side Code Example

Below are equivalent immutable Employee data classes in all three languages, each with validation:

<div class="code-tabs" data-tabs-id="employee-records-side-by-side">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">record</span> <span class="nc">Employee</span><span class="o">(</span>
    <span class="kt">long</span> <span class="n">id</span><span class="o">,</span>
    <span class="nc">String</span> <span class="n">name</span><span class="o">,</span>
    <span class="nc">String</span> <span class="n">email</span><span class="o">,</span>
    <span class="nc">String</span> <span class="n">department</span><span class="o">,</span>
    <span class="kt">double</span> <span class="n">salary</span>
<span class="o">)</span> <span class="o">{</span>
    <span class="kd">public</span> <span class="nc">Employee</span> <span class="o">{</span>
        <span class="k">if</span> <span class="o">(</span><span class="n">id</span> <span class="o">&lt;=</span> <span class="mi">0</span><span class="o">)</span> <span class="k">throw</span> <span class="k">new</span> <span class="nc">IllegalArgumentException</span><span class="o">(</span><span class="s">"Employee ID must be positive"</span><span class="o">);</span>
        <span class="nc">Objects</span><span class="o">.</span><span class="na">requireNonNull</span><span class="o">(</span><span class="n">name</span><span class="o">,</span> <span class="s">"Employee name cannot be null"</span><span class="o">);</span>
        <span class="k">if</span> <span class="o">(</span><span class="n">name</span><span class="o">.</span><span class="na">isBlank</span><span class="o">())</span> <span class="k">throw</span> <span class="k">new</span> <span class="nc">IllegalArgumentException</span><span class="o">(</span><span class="s">"Employee name cannot be blank"</span><span class="o">);</span>
        <span class="nc">Objects</span><span class="o">.</span><span class="na">requireNonNull</span><span class="o">(</span><span class="n">email</span><span class="o">,</span> <span class="s">"Employee email cannot be null"</span><span class="o">);</span>
        <span class="k">if</span> <span class="o">(!</span><span class="n">email</span><span class="o">.</span><span class="na">contains</span><span class="o">(</span><span class="s">"@"</span><span class="o">))</span> <span class="k">throw</span> <span class="k">new</span> <span class="nc">IllegalArgumentException</span><span class="o">(</span><span class="s">"Invalid email format"</span><span class="o">);</span>
        <span class="nc">Objects</span><span class="o">.</span><span class="na">requireNonNull</span><span class="o">(</span><span class="n">department</span><span class="o">,</span> <span class="s">"Department cannot be null"</span><span class="o">);</span>
        <span class="k">if</span> <span class="o">(</span><span class="n">salary</span> <span class="o">&lt;</span> <span class="mi">0</span><span class="o">)</span> <span class="k">throw</span> <span class="k">new</span> <span class="nc">IllegalArgumentException</span><span class="o">(</span><span class="s">"Salary cannot be negative"</span><span class="o">);</span>
    <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">case</span> <span class="k">class</span> <span class="nc">Employee</span><span class="o">(</span>
  <span class="n">id</span><span class="o">:</span> <span class="kt">Long</span><span class="o">,</span>
  <span class="n">name</span><span class="o">:</span> <span class="kt">String</span><span class="o">,</span>
  <span class="n">email</span><span class="o">:</span> <span class="kt">String</span><span class="o">,</span>
  <span class="n">department</span><span class="o">:</span> <span class="kt">String</span><span class="o">,</span>
  <span class="n">salary</span><span class="o">:</span> <span class="kt">Double</span>
<span class="o">)</span> <span class="o">{</span>
  <span class="n">require</span><span class="o">(</span><span class="n">id</span> <span class="o">&gt;</span> <span class="mi">0</span><span class="o">,</span> <span class="s">"Employee ID must be positive"</span><span class="o">)</span>
  <span class="n">require</span><span class="o">(</span><span class="n">name</span><span class="o">.</span><span class="n">nonEmpty</span><span class="o">,</span> <span class="s">"Employee name cannot be empty"</span><span class="o">)</span>
  <span class="n">require</span><span class="o">(</span><span class="n">email</span><span class="o">.</span><span class="n">contains</span><span class="o">(</span><span class="s">"@"</span><span class="o">),</span> <span class="s">"Invalid email format"</span><span class="o">)</span>
  <span class="n">require</span><span class="o">(</span><span class="n">department</span><span class="o">.</span><span class="n">nonEmpty</span><span class="o">,</span> <span class="s">"Department cannot be empty"</span><span class="o">)</span>
  <span class="n">require</span><span class="o">(</span><span class="n">salary</span> <span class="o">&gt;=</span> <span class="mi">0</span><span class="o">,</span> <span class="s">"Salary cannot be negative"</span><span class="o">)</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">data</span> <span class="k">class</span> <span class="nc">EmployeeDataClass</span><span class="p">(</span>
    <span class="k">val</span> <span class="py">id</span><span class="p">:</span> <span class="nc">Long</span><span class="p">,</span>
    <span class="k">val</span> <span class="py">name</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span>
    <span class="k">val</span> <span class="py">email</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span>
    <span class="k">val</span> <span class="py">department</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span>
    <span class="k">val</span> <span class="py">salary</span><span class="p">:</span> <span class="nc">Double</span>
<span class="p">)</span> <span class="p">{</span>
    <span class="k">init</span> <span class="p">{</span>
        <span class="n">require</span><span class="p">(</span><span class="n">id</span> <span class="o">&gt;</span> <span class="mi">0</span><span class="p">)</span> <span class="p">{</span> <span class="s">"Employee ID must be positive"</span> <span class="p">}</span>
        <span class="n">require</span><span class="p">(</span><span class="n">name</span><span class="o">.</span><span class="n">isNotBlank</span><span class="p">())</span> <span class="p">{</span> <span class="s">"Employee name cannot be blank"</span> <span class="p">}</span>
        <span class="n">require</span><span class="p">(</span><span class="n">email</span><span class="o">.</span><span class="n">contains</span><span class="p">(</span><span class="s">"@"</span><span class="p">))</span> <span class="p">{</span> <span class="s">"Invalid email format: </span><span class="si">$email</span><span class="s">"</span> <span class="p">}</span>
        <span class="n">require</span><span class="p">(</span><span class="n">department</span><span class="o">.</span><span class="n">isNotBlank</span><span class="p">())</span> <span class="p">{</span> <span class="s">"Department cannot be blank"</span> <span class="p">}</span>
        <span class="n">require</span><span class="p">(</span><span class="n">salary</span> <span class="o">&gt;=</span> <span class="mi">0</span><span class="p">)</span> <span class="p">{</span> <span class="s">"Salary cannot be negative"</span> <span class="p">}</span>
    <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
</div>
</div>

[View full Java example →](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/Employee.java)  
[View full Scala 3 example →](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/Employee.scala)  
[View full Kotlin example →](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/EmployeeDataClass.kt)

## Pattern Matching with Records (Java 21)

Java 21 brings powerful pattern matching with records:

```java
// Destructuring in switch expressions
String describe(Employee employee) {
    return switch (employee) {
        case Employee(var id, var name, var email, var dept, var salary) 
            when salary > 100000 -> name + " is a high earner in " + dept;
        case Employee(var id, var name, var email, var dept, var salary) 
            when dept.equals("Engineering") -> name + " is an engineer";
        case Employee(var id, var name, _, _, _) -> name + " (ID: " + id + ")";
    };
}

// Destructuring with instanceof
void process(Object obj) {
    if (obj instanceof Employee(var id, var name, var email, var dept, var salary)) {
        System.out.println("Processing employee: " + name);
    }
}
```

## Best Practices

1. **Use records for immutable data transfer objects (DTOs)** - They're perfect for API responses, database entities, and configuration objects.

2. **Prefer compact constructors for validation** - They're cleaner and the assignment happens automatically.

3. **Don't override accessor methods to return different values** - This violates the principle of least surprise.

4. **Use static factory methods for complex construction** - Name them `of()`, `from()`, or `create()`.

5. **Remember records are final** - They cannot be extended, but can implement interfaces.

    ## Interview Q&A: Records in Practice

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>How do you create an immutable data class in modern Java?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      You usually write a record. A record gives you a compact way to describe a value object: the data fields, the constructor, and the usual methods like <code>equals()</code>, <code>hashCode()</code>, and <code>toString()</code> are generated for you. That means you spend less time on boilerplate and more time on the business meaning of the object. For a Scala developer, the mental model is very close to a case class.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>What's the difference between a record and a normal class with private final fields?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      A normal class needs you to write the fields, constructor, getters, and comparison methods by hand. A record does most of that for you. The record is also designed for immutable data: once created, its values are fixed. That makes records easier to read, easier to test, and safer when you want value-based equality instead of object identity.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Can records have validation and custom methods?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Yes. You can add a compact constructor to validate the incoming values before the record is created. You can also add custom instance methods and static factory methods. This is one of the strongest parts of records: they stay concise, but they are not just a dumb container. They can still enforce rules and offer helper behavior that matches the domain you are modeling.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>How do records compare to Scala case classes or Kotlin data classes?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      They are very similar in intent. All three are meant to model data values rather than mutable state. Scala case classes and Kotlin data classes are often a little more flexible and have richer features out of the box, but Java records give Java developers a clean, modern way to do the same core idea without lots of repetitive code. In a Java interview, the best answer is: “records are Java's straightforward value object, much like a case class in Scala.”
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>When would you not use a record?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      You would avoid a record when the object is not just a value. If it needs mutable state, deep lifecycle management, inheritance, or a lot of logic that changes over time, a normal class is often a better fit. Records are best for data carrier objects: requests, responses, domain values, and configuration objects. They are not a replacement for every class in the system.
    </div>
  </details>
</div>

## Code Sample

See the complete implementation in our repository:
- [Employee.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/Employee.java) - The modern Java Record
- [EmployeeTraditional.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/EmployeeTraditional.java) - The verbose Java 8 approach
- [Employee.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/Employee.scala) - Scala 3 case class
- [EmployeeDataClass.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/EmployeeDataClass.kt) - Kotlin data class

## Summary

Java Records are a game-changer for Java developers, especially those coming from Scala:

<div class="table-wrapper" markdown="1">

| Aspect | Before (Java 8) | After (Java 16+) |
|--------|----------------|------------------|
| Lines of code | 60+ | ~25 |
| Boilerplate | High | Minimal |
| Error-prone | Yes (manual equals/hashCode) | No (auto-generated) |
| Readability | Low | High |
| IDE support | Required for generation | Not needed |

</div>

Records bring Java much closer to Scala's case classes, making the transition between languages smoother. In our next post, we'll explore String Manipulation with Modern APIs.

---

*This is Part 1 of our [Java 21 Interview Preparation Guide - Your Roadmap to Success]({{ site.baseurl }}{% link _posts/2025-11-25-java21-interview-preparation-plan.md %}). Next related posts: [String Manipulation with Modern APIs]({{ site.baseurl }}{% link _posts/2025-11-28-string-manipulation-with-modern-apis.md %}) and [Null-Safe Programming with Optional]({{ site.baseurl }}{% link _posts/2025-11-29-null-safe-programming-with-optional.md %}).*
