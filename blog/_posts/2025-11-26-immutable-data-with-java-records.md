---
layout: post
title: "Immutable Data with Java Records"
description: "Master Java Records for immutable data classes - compare with Scala case classes and Kotlin data classes, learn validation patterns, and see before/after code examples."
date: 2025-11-26 21:00:00 +0000
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

### Side-by-Side Code Example

Below are equivalent immutable Employee data classes in all three languages, each with validation:

#### **Scala 3**

```scala
case class Employee(
  id: Long,
  name: String,
  email: String,
  department: String,
  salary: Double
) {
  require(id > 0, "Employee ID must be positive")
  require(name.nonEmpty, "Employee name cannot be empty")
  require(email.contains("@"), "Invalid email format")
  require(department.nonEmpty, "Department cannot be empty")
  require(salary >= 0, "Salary cannot be negative")
}
```

[View full Scala 3 example →](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/Employee.scala)

---

#### **Java 21**

```java
public record Employee(
    long id,
    String name,
    String email,
    String department,
    double salary
) {
    public Employee {
        if (id <= 0) throw new IllegalArgumentException("Employee ID must be positive");
        Objects.requireNonNull(name, "Employee name cannot be null");
        if (name.isBlank()) throw new IllegalArgumentException("Employee name cannot be blank");
        Objects.requireNonNull(email, "Employee email cannot be null");
        if (!email.contains("@")) throw new IllegalArgumentException("Invalid email format");
        Objects.requireNonNull(department, "Department cannot be null");
        if (salary < 0) throw new IllegalArgumentException("Salary cannot be negative");
    }
}
```

[View full Java example →](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/Employee.java)

---

#### **Kotlin**

```kotlin
data class EmployeeDataClass(
    val id: Long,
    val name: String,
    val email: String,
    val department: String,
    val salary: Double
) {
    init {
        require(id > 0) { "Employee ID must be positive" }
        require(name.isNotBlank()) { "Employee name cannot be blank" }
        require(email.contains("@")) { "Invalid email format: $email" }
        require(department.isNotBlank()) { "Department cannot be blank" }
        require(salary >= 0) { "Salary cannot be negative" }
    }
}
```

[View full Kotlin example →](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/EmployeeDataClass.kt)

---

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

## Code Sample

See the complete implementation in our repository:
- [Employee.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/Employee.java) - The modern Java Record
- [EmployeeTraditional.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/EmployeeTraditional.java) - The verbose Java 8 approach
- [Employee.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/Employee.scala) - Scala 3 case class
- [EmployeeDataClass.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/EmployeeDataClass.kt) - Kotlin data class

## Summary

Java Records are a game-changer for Java developers, especially those coming from Scala:

| Aspect | Before (Java 8) | After (Java 16+) |
|--------|----------------|------------------|
| Lines of code | 60+ | ~25 |
| Boilerplate | High | Minimal |
| Error-prone | Yes (manual equals/hashCode) | No (auto-generated) |
| Readability | Low | High |
| IDE support | Required for generation | Not needed |

Records bring Java much closer to Scala's case classes, making the transition between languages smoother. In our next post, we'll explore String Manipulation with Modern APIs.

---

*This is Part 1 of our Java 21 Interview Preparation series. Check out the [full preparation plan](/interview/2025/11/28/java21-interview-preparation-plan.html) for more topics.*
