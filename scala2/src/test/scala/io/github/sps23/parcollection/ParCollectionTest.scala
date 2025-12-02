package io.github.sps23.parcollection

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

/** Real-world mathematical examples demonstrating apply and aggregate operations.
  *
  * Mathematical operations covered:
  * - Summation (Σ): Adding elements together
  * - Sum of squares: Used in variance/standard deviation calculations
  * - Euclidean distance: Geometry calculations
  * - Factorial products: Combinatorics
  * - GCD: Number theory
  * - Dot product: Linear algebra
  * - Prime factorization: Cryptography
  */
class ParCollectionTest extends AnyFunSuite with Matchers {

  // ============================================================================
  // Test 1: Apply - Computing Squares (Simple Transformation)
  // ============================================================================

  test("applyF should compute squares of numbers sequentially") {
    // Given: A sequence of integers from 1 to 10
    val numbers = 1 to 10

    // When: Computing squares using applyF
    val squares = ParCollection.applyF(numbers, (n: Int) => n * n)

    // Then: Should produce [1, 4, 9, 16, 25, 36, 49, 64, 81, 100]
    assert(squares == Seq(1, 4, 9, 16, 25, 36, 49, 64, 81, 100))
  }

  test("applyParF should compute squares in parallel") {
    val numbers = 1 to 100
    val sequential = ParCollection.applyF(numbers, (n: Int) => n * n)
    val parallel   = ParCollection.applyParF(numbers, (n: Int) => n * n)

    // Results should be identical (order may differ in collection, but values same)
    assert(sequential.sorted == parallel.sorted)
    assert(parallel.sum == 338350) // Sum of squares formula: n(n+1)(2n+1)/6
  }

  // ============================================================================
  // Test 2: Aggregate - Sum of Squares (Statistics/Variance Calculation)
  // ============================================================================

  test("aggregateF should compute sum of squares for variance calculation") {
    // Real-world: Computing sum of squares is used in variance calculation
    // Variance = Σ(x - mean)² / n, or simplified: Σ(x²)/n - mean²
    val dataPoints = Seq(2, 4, 6, 8, 10)

    // Computing Σ(x²) = 2² + 4² + 6² + 8² + 10² = 4 + 16 + 36 + 64 + 100 = 220
    val sumOfSquares = ParCollection.aggregateF(
      dataPoints,
      f = (x: Int) => x * x,                      // Transform: square each number
      aggregate = (acc: Int, x: Int) => acc + x, // Combine: sum them up
      zero = 0
    )

    assert(sumOfSquares == 220)

    // Can now compute variance: mean = 6, variance = 220/5 - 6² = 44 - 36 = 8
    val mean     = dataPoints.sum.toDouble / dataPoints.size
    val variance = (sumOfSquares.toDouble / dataPoints.size) - (mean * mean)
    assert(math.abs(variance - 8.0) < 1e-9)
  }

  test("aggregateParF should compute sum of squares in parallel") {
    val dataPoints = 1 to 100
    val sequential = ParCollection.aggregateF(
      dataPoints,
      f = (x: Int) => x * x,
      aggregate = (acc: Int, x: Int) => acc + x,
      zero = 0
    )
    val parallel = ParCollection.aggregateParF(
      dataPoints,
      f = (x: Int) => x * x,
      aggregate = (acc: Int, x: Int) => acc + x,
      zero = 0
    )

    assert(sequential == parallel)
    assert(parallel == 338350) // Formula: n(n+1)(2n+1)/6 = 100*101*201/6
  }

  // ============================================================================
  // Test 3: Aggregate - Computing Euclidean Distance (Geometry)
  // ============================================================================

  test("aggregateF should compute Euclidean distance between points") {
    // Real-world: Distance between two points in n-dimensional space
    // Distance = √(Σ(xᵢ - yᵢ)²)

    case class Point(x: Double, y: Double)
    val point1 = Point(1.0, 2.0)
    val point2 = Point(4.0, 6.0)

    // Create a sequence of coordinate pairs
    val coordinates = Seq((point1.x, point2.x), (point1.y, point2.y))

    // Compute sum of squared differences
    val sumOfSquaredDiffs = ParCollection.aggregateF(
      coordinates,
      f = (pair: (Double, Double)) => {
        val diff = pair._1 - pair._2
        diff * diff
      },
      aggregate = (acc: Double, x: Double) => acc + x,
      zero = 0.0
    )

    val distance = math.sqrt(sumOfSquaredDiffs)

    // Distance from (1,2) to (4,6) = √((1-4)² + (2-6)²) = √(9 + 16) = √25 = 5
    assert(math.abs(distance - 5.0) < 1e-9)
  }

  // ============================================================================
  // Test 4: Aggregate - Product of Factorials (Number Theory)
  // ============================================================================

  test("aggregateF should compute product of factorials") {
    // Real-world: Used in combinatorics and probability calculations
    // Computing: (1! * 2! * 3! * 4!) = 1 * 2 * 6 * 24 = 288

    def factorial(n: Int): Long = {
      if (n <= 1) 1L else n * factorial(n - 1)
    }

    val numbers             = Seq(1, 2, 3, 4)
    val productOfFactorials = ParCollection.aggregateF(
      numbers,
      f = (n: Int) => factorial(n),
      aggregate = (acc: Long, x: Long) => acc * x,
      zero = 1L
    )

    assert(productOfFactorials == 288L) // 1! * 2! * 3! * 4! = 1 * 2 * 6 * 24
  }

  // ============================================================================
  // Test 5: Aggregate - Computing GCD (Greatest Common Divisor)
  // ============================================================================

  test("aggregateF should find GCD of multiple numbers") {
    // Real-world: Finding the greatest common divisor of a set of numbers
    // Used in fraction simplification, scheduling problems, etc.

    def gcd(a: Int, b: Int): Int = {
      if (b == 0) a else gcd(b, a % b)
    }

    val numbers = Seq(48, 64, 80, 96)

    // GCD of all numbers in sequence
    val overallGcd = ParCollection.aggregateF(
      numbers,
      f = (n: Int) => n,                         // Identity function
      aggregate = (acc: Int, x: Int) => gcd(acc, x),
      zero = numbers.head
    )

    assert(overallGcd == 16) // GCD(48, 64, 80, 96) = 16
  }

  // ============================================================================
  // Test 6: Aggregate - Dot Product (Linear Algebra)
  // ============================================================================

  test("aggregateF should compute dot product of vectors") {
    // Real-world: Vector dot product used in physics, ML, graphics
    // Dot product: v₁·v₂ = Σ(v₁ᵢ * v₂ᵢ)

    val vector1 = Seq(1, 2, 3, 4)
    val vector2 = Seq(5, 6, 7, 8)

    // Zip vectors to create coordinate pairs
    val vectorPairs = vector1.zip(vector2)

    val dotProduct = ParCollection.aggregateF(
      vectorPairs,
      f = (pair: (Int, Int)) => pair._1 * pair._2, // Multiply components
      aggregate = (acc: Int, x: Int) => acc + x,    // Sum products
      zero = 0
    )

    // (1*5) + (2*6) + (3*7) + (4*8) = 5 + 12 + 21 + 32 = 70
    assert(dotProduct == 70)
  }

  // ============================================================================
  // Test 7: Aggregate with Fixed Thread Pool - Prime Factorization Count
  // ============================================================================

  test("aggregateParFixedF should count total prime factors in parallel") {
    // Real-world: Analyzing cryptographic strength of numbers

    def countPrimeFactors(n: Int): Int = {
      var count   = 0
      var num     = n
      var divisor = 2

      while (divisor * divisor <= num) {
        while (num % divisor == 0) {
          count += 1
          num = num / divisor
        }
        divisor += 1
      }

      if (num > 1) count += 1
      count
    }

    val numbers = Seq(12, 18, 24, 30, 36) // Small numbers for testing

    val totalPrimeFactors = ParCollection.aggregateParFixedF(
      numbers,
      f = (n: Int) => countPrimeFactors(n),
      aggregate = (acc: Int, x: Int) => acc + x,
      zero = 0,
      fixedNumberOfThreads = 2
    )

    // 12=2²·3 (3 factors), 18=2·3² (3), 24=2³·3 (4), 30=2·3·5 (3), 36=2²·3² (4)
    assert(totalPrimeFactors == 17)
  }

  // ============================================================================
  // Test 8: Performance Comparison - Large Dataset
  // ============================================================================

  test("parallel aggregation should handle large computations") {
    val largeDataset = 1 to 10000

    // Expensive computation: sum of cube roots
    def expensiveComputation(n: Int): Double = {
      math.pow(n, 1.0 / 3.0) // Cube root
    }

    val start1 = System.nanoTime()
    val sequential = ParCollection.aggregateF(
      largeDataset,
      f = expensiveComputation,
      aggregate = (acc: Double, x: Double) => acc + x,
      zero = 0.0
    )
    val time1 = System.nanoTime() - start1

    val start2 = System.nanoTime()
    val parallel = ParCollection.aggregateParForkF(
      largeDataset,
      f = expensiveComputation,
      aggregate = (acc: Double, x: Double) => acc + x,
      zero = 0.0,
      parallelism = 4
    )
    val time2 = System.nanoTime() - start2

    // Results should be approximately equal (floating point precision)
    assert(math.abs(sequential - parallel) < 0.001)

    // Note: Parallel might not always be faster for small datasets due to overhead
    // This test mainly verifies correctness
    println(s"Sequential: ${time1 / 1000000}ms, Parallel: ${time2 / 1000000}ms")
  }
}

