---
layout: post
title: "Java 21 Interview Preparation Plan"
description: "Comprehensive Java 21 interview preparation plan for senior developers - 20 problems covering Records, Virtual Threads, Pattern Matching, and more modern Java features."
date: 2025-11-25 20:00:00 +0000
categories: [interview]
tags: [java, java21, interview, preparation, senior-developer]
---

This post outlines a comprehensive plan for preparing for a Java Senior Developer interview, especially for developers who haven't worked with Java since JDK 1.8. The problems are divided into three groups: Basic, Medium, and Advanced, with a focus on JDK 21 features.

## Basic Problems (5)

These problems cover fundamental Java concepts that every senior developer should know, updated for modern Java.

### 1. Immutable Data with Records
**Topic:** Java Records (introduced in Java 16)

**Problem:** Design an immutable `Employee` record with validation in the compact constructor.

**Key Concepts:**
- Record syntax and components
- Compact constructors for validation
- Auto-generated `equals()`, `hashCode()`, and `toString()`
- Comparison with traditional immutable classes and Scala case classes

**Blog Material:** Show before/after comparison of verbose Java 8 immutable class vs concise Java record.

---

### 2. String Manipulation with Modern APIs
**Topic:** String API enhancements (Java 11-17)

**Problem:** Process a multi-line text file: strip indentation, filter blank lines, and format output.

**Key Concepts:**
- `String.isBlank()`, `String.lines()`, `String.strip()`
- `String.indent()`, `String.transform()`
- Text blocks (Java 15+)
- `String.formatted()` method

**Blog Material:** Compare Java 8 string processing with modern fluent API approach.

---

### 3. Null-Safe Programming with Optional
**Topic:** Optional API and best practices

**Problem:** Implement a service that fetches user preferences with fallback defaults, avoiding null checks.

**Key Concepts:**
- `Optional.ofNullable()`, `orElse()`, `orElseGet()`, `orElseThrow()`
- `Optional.map()`, `flatMap()`, `filter()`
- `Optional.ifPresentOrElse()` (Java 9+)
- `Optional.or()` (Java 9+)
- Anti-patterns to avoid

**Blog Material:** Show how to refactor nested null checks into fluent Optional chains.

---

### 4. Collection Factory Methods and Stream Basics
**Topic:** Collection factories (Java 9+) and Stream API fundamentals

**Problem:** Process a list of transactions: filter by amount, group by category, and calculate statistics.

**Key Concepts:**
- `List.of()`, `Set.of()`, `Map.of()`, `Map.ofEntries()`
- `Stream.toList()` (Java 16+)
- `Collectors.teeing()` (Java 12+)
- Immutable vs mutable collections

**Blog Material:** Demonstrate the evolution from Java 8 Collectors to modern collection factories.

---

### 5. Local Variable Type Inference
**Topic:** `var` keyword (Java 10+)

**Problem:** Refactor a data processing pipeline using appropriate `var` declarations while maintaining readability.

**Key Concepts:**
- When to use `var` (local variables with initializers)
- Where `var` cannot be used (fields, method parameters, return types)
- Best practices for readability
- `var` with diamond operator and generics

**Blog Material:** Guidelines on when `var` improves code vs when explicit types are clearer.

---

## Medium Problems (5)

These problems explore more advanced concepts and newer Java features that distinguish experienced developers.

### 1. Sealed Classes and Exhaustive Pattern Matching
**Topic:** Sealed classes (Java 17) and pattern matching

**Problem:** Model a payment system with different payment types (CreditCard, BankTransfer, DigitalWallet) using sealed classes, then implement fee calculation with exhaustive pattern matching.

**Key Concepts:**
- `sealed`, `permits`, `non-sealed` keywords
- Exhaustive switch expressions (no default needed)
- Record patterns for destructuring
- Comparison with Scala sealed traits and ADTs

**Blog Material:** Show how Java's sealed classes enable type-safe domain modeling similar to Scala.

---

### 2. Functional Interfaces and Lambda Expressions
**Topic:** Custom functional interfaces and advanced lambda usage

**Problem:** Implement a configurable retry mechanism using functional interfaces for retry policy, exception handling, and result transformation.

**Key Concepts:**
- `@FunctionalInterface` annotation
- `Function`, `Predicate`, `Consumer`, `Supplier` composition
- Method references (static, instance, constructor)
- Exception handling in lambdas
- Comparison with Scala function types

**Blog Material:** Build a reusable retry utility showcasing functional programming in Java.

---

### 3. CompletableFuture and Asynchronous Programming
**Topic:** Asynchronous programming with CompletableFuture

**Problem:** Implement a service that aggregates data from multiple APIs concurrently with timeout handling and fallbacks.

**Key Concepts:**
- `CompletableFuture.supplyAsync()`, `thenApply()`, `thenCompose()`
- `allOf()`, `anyOf()` for combining futures
- `completeOnTimeout()`, `orTimeout()` (Java 9+)
- Exception handling with `exceptionally()`, `handle()`
- Comparison with Scala Futures

**Blog Material:** Demonstrate concurrent API calls with proper error handling and timeouts.

---

### 4. Enhanced Switch Expressions and Pattern Matching
**Topic:** Switch expressions (Java 14+) and type patterns (Java 16+)

**Problem:** Implement a JSON-like value parser that handles different types (String, Number, Boolean, Array, Object) using pattern matching.

**Key Concepts:**
- Switch expressions with arrow syntax
- Type patterns (`case String s ->`)
- Guarded patterns (`case String s when s.length() > 10 ->`)
- Pattern matching for `instanceof`
- Yielding values from switch

**Blog Material:** Show evolution from traditional switch statements to modern pattern matching.

---

### 5. Stream API Advanced Operations
**Topic:** Advanced Stream operations and collectors

**Problem:** Analyze a dataset of orders: calculate running totals, find top N by category, and generate a summary report.

**Key Concepts:**
- `Stream.takeWhile()`, `dropWhile()` (Java 9+)
- `Collectors.groupingBy()` with downstream collectors
- `Collectors.partitioningBy()`
- Custom collectors
- Parallel streams and when to use them

**Blog Material:** Complex data processing pipeline with performance considerations.

---

## Advanced Problems (10)

These problems focus on JDK 21 features and complex scenarios expected at the senior level.

### 1. Virtual Threads and Structured Concurrency
**Topic:** Project Loom - Virtual Threads (Java 21)

**Problem:** Migrate a thread-pool-based web scraper to virtual threads, handling thousands of concurrent HTTP requests efficiently.

**Key Concepts:**
- `Thread.startVirtualThread()` and `Thread.ofVirtual()`
- `Executors.newVirtualThreadPerTaskExecutor()`
- Structured concurrency with `StructuredTaskScope` (preview)
- When to use virtual threads vs platform threads
- Thread-local variables and scoped values

**Blog Material:** Before/after comparison showing dramatic simplification with virtual threads.

---

### 2. Record Patterns and Nested Destructuring
**Topic:** Record Patterns (Java 21)

**Problem:** Implement a geometric shape processor that calculates properties using nested record patterns for complex shapes (e.g., shapes containing other shapes).

**Key Concepts:**
- Nested record patterns (`case Rectangle(Point(int x1, int y1), Point(int x2, int y2))`)
- Record patterns in switch and instanceof
- Combining record patterns with type patterns
- Unnamed patterns and variables (`_`) (Java 21 preview)

**Blog Material:** Elegant data extraction from complex nested structures.

---

### 3. Sequenced Collections
**Topic:** Sequenced Collections API (Java 21)

**Problem:** Implement a navigation history system (browser-like) using the new sequenced collection interfaces.

**Key Concepts:**
- `SequencedCollection`, `SequencedSet`, `SequencedMap` interfaces
- `getFirst()`, `getLast()`, `addFirst()`, `addLast()`
- `reversed()` view
- Retrofitted implementations (ArrayList, LinkedHashSet, etc.)

**Blog Material:** Show how sequenced collections unify access patterns across collection types.

---

### 4. Pattern Matching for Switch - Complete Coverage
**Topic:** Exhaustive pattern matching with sealed hierarchies (Java 21)

**Problem:** Build an expression evaluator for a simple arithmetic language using sealed interfaces and exhaustive pattern matching.

**Key Concepts:**
- Sealed interfaces with record implementations
- Exhaustive switch without default
- Guarded patterns with `when` clause
- Dominance and coverage rules
- Comparison with Scala match expressions

**Blog Material:** Type-safe interpreter implementation showcasing Java's pattern matching maturity.

---

### 5. String Templates (Preview)
**Topic:** String Templates (Java 21 Preview)

**Problem:** Build a SQL query builder that safely interpolates parameters while preventing SQL injection.

**Key Concepts:**
- `STR` template processor for string interpolation
- `FMT` template processor for formatting
- Custom template processors
- Safety benefits over string concatenation
- Comparison with Scala string interpolation

**Blog Material:** Safe string interpolation with custom processors for domain-specific needs.

---

### 6. Foreign Function and Memory API
**Topic:** FFM API (Java 21)

**Problem:** Integrate with a native C library (e.g., compression or crypto) using the Foreign Function and Memory API.

**Key Concepts:**
- `Arena` for memory lifecycle management
- `MemorySegment` for native memory access
- `Linker` and `FunctionDescriptor` for native calls
- `SymbolLookup` for finding native functions
- Safety improvements over JNI

**Blog Material:** Modern native integration without the complexity of JNI.

---

### 7. Scoped Values (Preview)
**Topic:** Scoped Values (Java 21 Preview)

**Problem:** Implement request-scoped context propagation in a web application, replacing ThreadLocal with ScopedValue.

**Key Concepts:**
- `ScopedValue` declaration and binding
- `ScopedValue.where().run()` pattern
- Advantages over ThreadLocal (especially with virtual threads)
- Inheritance in child threads
- Performance characteristics

**Blog Material:** Context propagation in modern concurrent applications.

---

### 8. Unnamed Classes and Instance Main Methods (Preview)
**Topic:** Simplified program entry (Java 21 Preview)

**Problem:** Create educational examples demonstrating simple Java programs without class declarations.

**Key Concepts:**
- Instance main methods (`void main()`)
- Unnamed classes for simple programs
- Progression from simple to full Java programs
- Use cases for scripting and education

**Blog Material:** Onboarding new developers with simplified Java syntax.

---

### 9. Generational ZGC and Performance Tuning
**Topic:** Generational ZGC (Java 21)

**Problem:** Analyze and tune a memory-intensive application using Generational ZGC, comparing with other GC options.

**Key Concepts:**
- Generational ZGC characteristics and benefits
- JVM flags: `-XX:+UseZGC -XX:+ZGenerational`
- When to choose ZGC vs G1 vs other collectors
- Monitoring and tuning techniques
- Sub-millisecond pause times

**Blog Material:** Production GC tuning guide with benchmarks and recommendations.

---

### 10. Building a Modern Java 21 Application
**Topic:** Integration of all Java 21 features

**Problem:** Design and implement a complete microservice component that combines: records for DTOs, sealed classes for domain modeling, virtual threads for I/O, pattern matching for business logic, and modern collection APIs.

**Key Concepts:**
- Architectural decisions with modern Java
- Best practices for combining new features
- Migration path from Java 8/11
- Testing strategies for new features
- Performance considerations

**Blog Material:** Capstone project demonstrating real-world Java 21 application design.

---

## Recommended Study Order

1. **Week 1-2:** Basic Problems (foundation refresh)
2. **Week 3-4:** Medium Problems (intermediate features)
3. **Week 5-8:** Advanced Problems (JDK 21 mastery)

## Additional Resources

- [JEP Index for Java 21](https://openjdk.org/projects/jdk/21/)
- [Java Language Updates](https://docs.oracle.com/en/java/javase/21/language/)
- [Inside Java Blog](https://inside.java/)
- [This Repository's Code Examples](https://github.com/sps23/java-for-scala-devs)

---

*This plan will be followed by individual blog posts with detailed code examples for each problem. Stay tuned!*
