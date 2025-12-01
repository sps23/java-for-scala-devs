---
layout: post
title: "Comparing JVM Test Frameworks: JUnit 5, ScalaTest, and Kotest"
description: "A comprehensive comparison of the most popular test frameworks across Java, Scala, and Kotlin - explore JUnit 5, ScalaTest, and Kotest with practical examples and cross-language usage."
date: 2025-12-01 23:00:00 +0000
categories: testing
tags: java scala kotlin junit scalatest kotest testing interview-preparation
---

Testing is a critical part of software development on the JVM. This comprehensive guide compares the three most popular test frameworks across Java, Scala, and Kotlin: **JUnit 5**, **ScalaTest**, and **Kotest**. We'll explore their features, syntax styles, and cross-language compatibility with practical examples.

## Overview of JVM Test Frameworks

### Top Test Frameworks by Language

**Java:**
1. **JUnit 5 (Jupiter)** - Modern, annotation-based, most widely adopted
2. **TestNG** - Flexible, data-driven, popular in enterprise
3. **Mockito** - Mocking framework (often used with JUnit)

**Scala:**
1. **ScalaTest** - Feature-rich, multiple testing styles, most popular
2. **Specs2** - BDD-focused, concurrent execution support
3. **MUnit** - Lightweight, fast, growing adoption

**Kotlin:**
1. **Kotest** - Kotlin-idiomatic, multiple spec styles, powerful matchers
2. **JUnit 5** - Fully compatible, widely used
3. **Spek** - BDD-style (less actively maintained)

**Cross-Language Framework:**
- **JUnit 5** works seamlessly across Java, Scala, and Kotlin
- All three languages can interoperate via JVM bytecode

## JUnit 5: The Universal JVM Test Framework

JUnit 5 (Jupiter) is the most widely adopted test framework on the JVM. It works natively with Java, Scala, and Kotlin.

### Key Features

✅ **Annotation-based** - Clean, declarative test definitions  
✅ **Nested tests** - Organize related tests hierarchically  
✅ **Parameterized tests** - Data-driven testing support  
✅ **Display names** - Human-readable test descriptions  
✅ **Extension model** - Powerful plugin system  
✅ **Parallel execution** - Concurrent test execution  
✅ **Cross-language** - Works with Java, Scala, Kotlin, Groovy

### JUnit 5 in Java

<div class="code-tabs" data-tabs-id="junit-java">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code>package io.github.sps23.testing.examples;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Calculator Tests (JUnit 5)")
class CalculatorJUnit5Test {

    private Calculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Calculator();
    }

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperations {

        @Test
        @DisplayName("Addition should return sum of two numbers")
        void testAddition() {
            assertEquals(5, calculator.add(2, 3));
            assertEquals(0, calculator.add(-1, 1));
            assertEquals(-5, calculator.add(-2, -3));
        }

        @Test
        @DisplayName("Division by zero should throw ArithmeticException")
        void testDivisionByZero() {
            assertThrows(ArithmeticException.class, 
                () -> calculator.divide(5, 0));
        }
    }

    @Nested
    @DisplayName("Prime Number Checks")
    class PrimeNumberChecks {

        @ParameterizedTest
        @DisplayName("Should identify prime numbers correctly")
        @ValueSource(ints = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29})
        void testPrimeNumbers(int number) {
            assertTrue(calculator.isPrime(number), 
                number + " should be prime");
        }
    }
}
</code></pre></div></div>
</div>
</div>

**Strengths:**
- Excellent IDE support (IntelliJ IDEA, Eclipse, VS Code)
- Rich assertion library with clear error messages
- Parameterized tests for data-driven testing
- Nested test classes for better organization
- Display names for documentation-like test output

**Weaknesses:**
- Verbose compared to Kotlin/Scala DSLs
- Requires explicit imports for assertions
- Lambda syntax for exceptions can be awkward
- No built-in BDD-style syntax

### JUnit 5 in Scala 3

<div class="code-tabs" data-tabs-id="junit-scala">
<div class="tab-buttons">
<button class="tab-button active" data-tab="scala" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code>package io.github.sps23.testing.examples

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@DisplayName("Calculator Tests (JUnit 5 in Scala)")
class CalculatorJUnit5Test:

  private var calculator: Calculator = _

  @BeforeEach
  def setUp(): Unit =
    calculator = new Calculator

  @Nested
  @DisplayName("Basic Operations")
  class BasicOperations:

    @Test
    @DisplayName("Addition should return sum of two numbers")
    def testAddition(): Unit =
      assertEquals(5, calculator.add(2, 3))
      assertEquals(0, calculator.add(-1, 1))
      assertEquals(-5, calculator.add(-2, -3))

    @Test
    @DisplayName("Division by zero should throw ArithmeticException")
    def testDivisionByZero(): Unit =
      assertThrows(classOf[ArithmeticException], 
        () => calculator.divide(5, 0))

  @Nested
  @DisplayName("Prime Number Checks")
  class PrimeNumberChecks:

    @ParameterizedTest
    @DisplayName("Should identify prime numbers correctly")
    @ValueSource(ints = Array(2, 3, 5, 7, 11, 13, 17, 19, 23, 29))
    def testPrimeNumbers(number: Int): Unit =
      assertTrue(calculator.isPrime(number), 
        s"$number should be prime")
</code></pre></div></div>
</div>
</div>

**Scala 3 with JUnit 5:**
- Works seamlessly with Scala 3 syntax
- String interpolation in assertions (`s"$number should be prime"`)
- Uses `classOf[ExceptionType]` for exception testing
- Requires `Unit` return type for test methods
- Can leverage Scala collections in tests

### JUnit 5 in Kotlin

<div class="code-tabs" data-tabs-id="junit-kotlin">
<div class="tab-buttons">
<button class="tab-button active" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code>package io.github.sps23.testing.examples

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

@DisplayName("Calculator Tests (JUnit 5 in Kotlin)")
class CalculatorJUnit5Test {

    private lateinit var calculator: Calculator

    @BeforeEach
    fun setUp() {
        calculator = Calculator()
    }

    @Nested
    @DisplayName("Basic Operations")
    inner class BasicOperations {

        @Test
        @DisplayName("Addition should return sum of two numbers")
        fun testAddition() {
            assertEquals(5, calculator.add(2, 3))
            assertEquals(0, calculator.add(-1, 1))
            assertEquals(-5, calculator.add(-2, -3))
        }

        @Test
        @DisplayName("Division by zero should throw ArithmeticException")
        fun testDivisionByZero() {
            assertThrows(ArithmeticException::class.java) { 
                calculator.divide(5, 0) 
            }
        }
    }

    @Nested
    @DisplayName("Prime Number Checks")
    inner class PrimeNumberChecks {

        @ParameterizedTest
        @DisplayName("Should identify prime numbers correctly")
        @ValueSource(ints = [2, 3, 5, 7, 11, 13, 17, 19, 23, 29])
        fun testPrimeNumbers(number: Int) {
            assertTrue(calculator.isPrime(number), 
                "$number should be prime")
        }
    }
}
</code></pre></div></div>
</div>
</div>

**Kotlin with JUnit 5:**
- Uses `inner class` for nested tests
- `lateinit var` for test instance initialization
- String templates in assertions (`"$number should be prime"`)
- Exception class reference: `ArithmeticException::class.java`
- Kotlin array literals `[...]` for `@ValueSource`

## ScalaTest: Feature-Rich Scala Testing

ScalaTest is the most popular test framework for Scala, offering multiple testing styles to match different preferences.

### Key Features

✅ **Multiple testing styles** - FunSuite, WordSpec, FlatSpec, FeatureSpec, etc.  
✅ **Rich matchers** - Expressive assertions with clear failure messages  
✅ **BDD support** - Behavior-driven development syntax  
✅ **Async testing** - Built-in support for asynchronous tests  
✅ **Property-based testing** - Integration with ScalaCheck  
✅ **Scala-idiomatic** - Leverages Scala language features

### ScalaTest FunSuite Style

<div class="code-tabs" data-tabs-id="scalatest-funsuite">
<div class="tab-buttons">
<button class="tab-button active" data-tab="scala" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code>package io.github.sps23.testing.examples

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class CalculatorScalaTestFunSuite extends AnyFunSuite with Matchers:

  val calculator = new Calculator

  test("addition should return sum of two numbers"):
    calculator.add(2, 3) shouldBe 5
    calculator.add(-1, 1) shouldBe 0
    calculator.add(-2, -3) shouldBe -5

  test("division should return quotient of two numbers"):
    calculator.divide(5, 2) shouldBe 2.5 +- 0.001
    calculator.divide(-4, 2) shouldBe -2.0 +- 0.001

  test("division by zero should throw ArithmeticException"):
    an[ArithmeticException] should be thrownBy calculator.divide(5, 0)

  test("should identify prime numbers correctly"):
    val primes = Seq(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)
    primes.foreach { n =>
      calculator.isPrime(n) shouldBe true
    }

  test("should identify non-prime numbers correctly"):
    val nonPrimes = Seq(0, 1, 4, 6, 8, 9, 10, 12, 15, 16)
    nonPrimes.foreach { n =>
      calculator.isPrime(n) shouldBe false
    }
</code></pre></div></div>
</div>
</div>

**FunSuite Strengths:**
- Concise, xUnit-style tests
- Simple `test("description") { ... }` syntax
- Natural reading flow
- Good for straightforward unit tests

### ScalaTest WordSpec Style (BDD)

<div class="code-tabs" data-tabs-id="scalatest-wordspec">
<div class="tab-buttons">
<button class="tab-button active" data-tab="scala" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code>package io.github.sps23.testing.examples

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

class CalculatorScalaTestWordSpec extends AnyWordSpec with Matchers:

  val calculator = new Calculator

  "A Calculator" when {
    "performing basic operations" should {
      "add two numbers correctly" in {
        calculator.add(2, 3) shouldBe 5
        calculator.add(-1, 1) shouldBe 0
        calculator.add(-2, -3) shouldBe -5
      }

      "multiply two numbers correctly" in {
        calculator.multiply(2, 3) shouldBe 6
        calculator.multiply(-1, 2) shouldBe -2
        calculator.multiply(-2, -3) shouldBe 6
      }
    }

    "performing division" should {
      "divide two numbers correctly" in {
        calculator.divide(5, 2) shouldBe 2.5 +- 0.001
        calculator.divide(-4, 2) shouldBe -2.0 +- 0.001
      }

      "throw ArithmeticException when dividing by zero" in {
        an[ArithmeticException] should be thrownBy calculator.divide(5, 0)
      }
    }

    "checking for prime numbers" should {
      "identify prime numbers correctly" in {
        val primes = Seq(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)
        all(primes.map(calculator.isPrime)) shouldBe true
      }

      "identify non-prime numbers correctly" in {
        val nonPrimes = Seq(0, 1, 4, 6, 8, 9, 10, 12, 15, 16)
        all(nonPrimes.map(calculator.isPrime)) shouldBe false
      }
    }
  }
</code></pre></div></div>
</div>
</div>

**WordSpec Strengths:**
- BDD-style nested descriptions
- Reads like natural language specifications
- Excellent for acceptance tests
- Great test report readability

**ScalaTest Overall Strengths:**
- Flexible style selection (10+ testing styles)
- Powerful matchers: `shouldBe`, `shouldEqual`, `should be`
- Tolerance for floating-point comparisons: `2.5 +- 0.001`
- Exception testing: `an[ExceptionType] should be thrownBy`
- Collection assertions: `all(...)`, `atLeast(...)`, `atMost(...)`

**ScalaTest Weaknesses:**
- Steeper learning curve due to many options
- Can be slower than lightweight alternatives
- Some DSL features can be confusing for beginners
- Less familiar to developers from other JVM languages

## Kotest: Modern Kotlin Testing

Kotest is the most Kotlin-idiomatic test framework, offering multiple spec styles and powerful assertions.

### Key Features

✅ **Multiple spec styles** - FunSpec, StringSpec, DescribeSpec, BehaviorSpec, etc.  
✅ **Kotlin DSL** - Leverages Kotlin's syntax features  
✅ **Data-driven testing** - Built-in support for property-based testing  
✅ **Coroutine support** - First-class async/await testing  
✅ **Powerful matchers** - Extensive assertion library  
✅ **IDE integration** - Excellent IntelliJ IDEA support

### Kotest FunSpec Style

<div class="code-tabs" data-tabs-id="kotest-funspec">
<div class="tab-buttons">
<button class="tab-button active" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code>package io.github.sps23.testing.examples

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class CalculatorKotestFunSpec : FunSpec({
    val calculator = Calculator()

    context("Basic Operations") {
        test("addition should return sum of two numbers") {
            calculator.add(2, 3) shouldBe 5
            calculator.add(-1, 1) shouldBe 0
            calculator.add(-2, -3) shouldBe -5
        }

        test("multiplication should return product of two numbers") {
            calculator.multiply(2, 3) shouldBe 6
            calculator.multiply(-1, 2) shouldBe -2
            calculator.multiply(-2, -3) shouldBe 6
        }
    }

    context("Division Operations") {
        test("division should return quotient of two numbers") {
            calculator.divide(5, 2) shouldBe (2.5.plusOrMinus(0.001))
            calculator.divide(-4, 2) shouldBe ((-2.0).plusOrMinus(0.001))
        }

        test("division by zero should throw ArithmeticException") {
            shouldThrow<ArithmeticException> {
                calculator.divide(5, 0)
            }
        }
    }

    context("Prime Number Checks") {
        test("should identify prime numbers correctly") {
            val primes = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)
            primes.forEach { n ->
                calculator.isPrime(n) shouldBe true
            }
        }

        test("should identify non-prime numbers correctly") {
            val nonPrimes = listOf(0, 1, 4, 6, 8, 9, 10, 12, 15, 16)
            nonPrimes.forEach { n ->
                calculator.isPrime(n) shouldBe false
            }
        }
    }
})
</code></pre></div></div>
</div>
</div>

**FunSpec Features:**
- `context { }` blocks for grouping related tests
- `test("description") { }` for individual tests
- Clean, nested structure
- Similar to ScalaTest FunSuite but with Kotlin idioms

### Kotest StringSpec Style (Most Concise)

<div class="code-tabs" data-tabs-id="kotest-stringspec">
<div class="tab-buttons">
<button class="tab-button active" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code>package io.github.sps23.testing.examples

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

class CalculatorKotestStringSpec : StringSpec({
    val calculator = Calculator()

    "addition should return sum of two numbers" {
        calculator.add(2, 3) shouldBe 5
        calculator.add(-1, 1) shouldBe 0
        calculator.add(-2, -3) shouldBe -5
    }

    "multiplication should return product of two numbers" {
        calculator.multiply(2, 3) shouldBe 6
        calculator.multiply(-1, 2) shouldBe -2
        calculator.multiply(-2, -3) shouldBe 6
    }

    "division should return quotient of two numbers" {
        calculator.divide(5, 2) shouldBe (2.5.plusOrMinus(0.001))
        calculator.divide(-4, 2) shouldBe ((-2.0).plusOrMinus(0.001))
    }

    "division by zero should throw ArithmeticException" {
        shouldThrow<ArithmeticException> {
            calculator.divide(5, 0)
        }
    }

    "should identify prime numbers correctly" {
        val primes = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)
        primes.forEach { n ->
            calculator.isPrime(n) shouldBe true
        }
    }

    "should identify non-prime numbers correctly" {
        val nonPrimes = listOf(0, 1, 4, 6, 8, 9, 10, 12, 15, 16)
        nonPrimes.forEach { n ->
            calculator.isPrime(n) shouldBe false
        }
    }
})
</code></pre></div></div>
</div>
</div>

**StringSpec Features:**
- Most concise Kotest style
- Each test is a simple string key-value pair
- No nesting or context blocks
- Perfect for straightforward unit tests

**Kotest Overall Strengths:**
- Extremely Kotlin-idiomatic syntax
- Powerful infix matchers: `a shouldBe b`
- Exception testing: `shouldThrow<ExceptionType> { }`
- Floating-point comparisons: `value.plusOrMinus(delta)`
- Extensive matcher library (strings, collections, exceptions, etc.)
- Great coroutine and async support

**Kotest Weaknesses:**
- Kotlin-specific (not usable from Java/Scala)
- Smaller community than JUnit 5
- Some IDE features lag behind JUnit
- Learning curve for choosing appropriate spec style

## Feature Comparison

| Feature | JUnit 5 | ScalaTest | Kotest |
|---------|---------|-----------|--------|
| **Multi-language support** | ✅ Java, Scala, Kotlin | ⚠️ Scala (Java/Kotlin via JVM) | ❌ Kotlin only |
| **Testing styles** | Annotation-based | 10+ styles (FunSuite, WordSpec, etc.) | 10+ styles (StringSpec, FunSpec, etc.) |
| **BDD support** | ❌ (extension needed) | ✅ Built-in (WordSpec, FeatureSpec) | ✅ Built-in (BehaviorSpec, DescribeSpec) |
| **Parameterized tests** | ✅ `@ParameterizedTest` | ✅ Property-based testing | ✅ Data-driven tests |
| **Nested tests** | ✅ `@Nested` classes | ✅ Context blocks | ✅ Context blocks |
| **Assertion library** | Basic (assertions, hamcrest) | Rich Scala matchers | Rich Kotlin matchers |
| **Async testing** | ⚠️ Manual setup | ✅ Built-in | ✅ Coroutine support |
| **Parallel execution** | ✅ Configurable | ✅ Configurable | ✅ Configurable |
| **IDE support** | ⭐⭐⭐⭐⭐ Excellent | ⭐⭐⭐⭐ Very good | ⭐⭐⭐⭐ Very good |
| **Community size** | ⭐⭐⭐⭐⭐ Largest | ⭐⭐⭐⭐ Large (Scala) | ⭐⭐⭐ Growing |
| **Learning curve** | ⭐⭐ Easy | ⭐⭐⭐ Moderate | ⭐⭐⭐ Moderate |

## Syntax Comparison: Same Test, Three Frameworks

Let's compare how the same prime number test looks across all three frameworks:

<div class="code-tabs" data-tabs-id="syntax-comparison">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java (JUnit 5)">Java (JUnit 5)</button>
<button class="tab-button" data-tab="scala" data-lang="Scala (ScalaTest)">Scala (ScalaTest)</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin (Kotest)">Kotlin (Kotest)</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code>@ParameterizedTest
@DisplayName("Should identify prime numbers correctly")
@ValueSource(ints = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29})
void testPrimeNumbers(int number) {
    assertTrue(calculator.isPrime(number), 
        number + " should be prime");
}
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code>test("should identify prime numbers correctly"):
  val primes = Seq(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)
  primes.foreach { n =>
    calculator.isPrime(n) shouldBe true
  }
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code>test("should identify prime numbers correctly") {
    val primes = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)
    primes.forEach { n ->
        calculator.isPrime(n) shouldBe true
    }
}
</code></pre></div></div>
</div>
</div>

**Key Differences:**

1. **JUnit 5** uses `@ParameterizedTest` for data-driven testing
2. **ScalaTest** uses functional iteration with `foreach`
3. **Kotest** uses Kotlin's `forEach` lambda syntax
4. **Assertions**: `assertTrue` (JUnit), `shouldBe` (ScalaTest/Kotest)

## When to Use Each Framework

### Use JUnit 5 When:

✅ You need **cross-language compatibility** across Java, Scala, and Kotlin  
✅ Your team is familiar with JUnit from Java projects  
✅ You want **maximum IDE and tooling support**  
✅ You're working on a **polyglot JVM project**  
✅ You need **enterprise-level stability and support**

**Best for:** Enterprise projects, cross-language codebases, teams transitioning from Java

### Use ScalaTest When:

✅ You're writing **pure Scala** code  
✅ You want **flexible testing styles** (BDD, TDD, acceptance tests)  
✅ You need **powerful matchers** for complex assertions  
✅ You value **Scala-idiomatic** test code  
✅ You want **property-based testing** integration

**Best for:** Scala projects, teams that value flexibility, BDD-style acceptance tests

### Use Kotest When:

✅ You're writing **pure Kotlin** code  
✅ You want the **most concise test syntax**  
✅ You need **coroutine and async testing** support  
✅ You prefer **Kotlin-idiomatic DSLs**  
✅ You want **modern, fluent assertions**

**Best for:** Kotlin projects, Android development, teams that embrace Kotlin idioms

## Migration Path

### From JUnit 4 to JUnit 5

```java
// JUnit 4
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class OldTest {
    @Before
    public void setup() { }
    
    @Test
    public void testSomething() {
        assertEquals(5, calculator.add(2, 3));
    }
}
```

```java
// JUnit 5
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NewTest {
    @BeforeEach
    void setup() { }
    
    @Test
    void testSomething() {
        assertEquals(5, calculator.add(2, 3));
    }
}
```

**Key Changes:**
- Package changed: `org.junit` → `org.junit.jupiter.api`
- `@Before` → `@BeforeEach`, `@After` → `@AfterEach`
- No need for `public` on test classes/methods
- More descriptive assertion methods

### From ScalaTest 2.x to ScalaTest 3.x

Main changes involve package structure and trait composition. Most test code remains compatible with minor syntax updates.

### From Spek to Kotest

Kotest's DescribeSpec is similar to Spek, making migration straightforward:

```kotlin
// Spek (deprecated)
object CalculatorSpec : Spek({
    describe("calculator") {
        it("should add numbers") {
            calculator.add(2, 3) shouldEqual 5
        }
    }
})
```

```kotlin
// Kotest
class CalculatorSpec : DescribeSpec({
    describe("calculator") {
        it("should add numbers") {
            calculator.add(2, 3) shouldBe 5
        }
    }
})
```

## Best Practices Across All Frameworks

### 1. Use Descriptive Test Names

```java
// ❌ Poor
@Test void test1() { }

// ✅ Good
@Test 
@DisplayName("Addition should return sum of two positive numbers")
void testAdditionWithPositiveNumbers() { }
```

### 2. Follow the AAA Pattern

**Arrange-Act-Assert** makes tests readable:

```kotlin
test("division should handle edge cases") {
    // Arrange
    val calculator = Calculator()
    val dividend = 10
    val divisor = 0
    
    // Act & Assert
    shouldThrow<ArithmeticException> {
        calculator.divide(dividend, divisor)
    }
}
```

### 3. Keep Tests Independent

Each test should be able to run in isolation:

```scala
class CalculatorTest extends AnyFunSuite with BeforeEach:
  var calculator: Calculator = _
  
  override def beforeEach(): Unit =
    calculator = new Calculator // Fresh instance per test
```

### 4. Use Parameterized Tests for Data Variations

```java
@ParameterizedTest
@CsvSource({
    "2, 3, 5",
    "-1, 1, 0",
    "-2, -3, -5"
})
void testAddition(int a, int b, int expected) {
    assertEquals(expected, calculator.add(a, b));
}
```

### 5. Test One Thing Per Test

```kotlin
// ❌ Poor - tests multiple things
test("calculator works") {
    calculator.add(2, 3) shouldBe 5
    calculator.subtract(5, 3) shouldBe 2
    calculator.multiply(2, 3) shouldBe 6
}

// ✅ Good - focused tests
test("addition should return correct sum") {
    calculator.add(2, 3) shouldBe 5
}

test("subtraction should return correct difference") {
    calculator.subtract(5, 3) shouldBe 2
}
```

## Full Working Examples

Check out the complete implementation in our repository:
- [Java JUnit 5 Examples](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/test/java/io/github/sps23/testing/examples)
- [Scala ScalaTest Examples](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/test/scala/io/github/sps23/testing/examples)
- [Kotlin Kotest Examples](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/test/kotlin/io/github/sps23/testing/examples)

## Key Takeaways

1. **JUnit 5** is the most versatile choice for polyglot JVM projects, working seamlessly across Java, Scala, and Kotlin with excellent tooling support.

2. **ScalaTest** excels in pure Scala projects with its flexible testing styles, powerful matchers, and Scala-idiomatic syntax. The WordSpec style is particularly good for BDD.

3. **Kotest** provides the most concise and Kotlin-idiomatic testing experience with first-class coroutine support, but is limited to Kotlin projects.

4. All three frameworks support modern testing practices: parameterized tests, nested tests, async testing, and parallel execution.

5. **Cross-language compatibility** is JUnit 5's killer feature—if your project uses multiple JVM languages, standardize on JUnit 5.

6. **Test readability matters**—choose a framework and style that makes your tests serve as documentation for your code.

## Conclusion

The choice of test framework depends on your project context:

- **Multi-language JVM projects** → **JUnit 5** for universal compatibility
- **Scala projects with complex testing needs** → **ScalaTest** for flexibility and power
- **Kotlin projects prioritizing conciseness** → **Kotest** for idiomatic Kotlin testing

All three frameworks are mature, well-maintained, and production-ready. JUnit 5 stands out as the common denominator that works everywhere, making it ideal for teams working across multiple JVM languages. ScalaTest and Kotest provide more language-specific features and ergonomics for their respective languages.

For Scala developers learning Java: JUnit 5 is straightforward to adopt, and its annotation-based model translates well to Scala's syntax. The concepts remain the same—only the syntax changes.

Happy testing! 🧪
