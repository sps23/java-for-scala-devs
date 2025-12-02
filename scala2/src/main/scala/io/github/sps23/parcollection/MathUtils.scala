package io.github.sps23.parcollection

import scala.annotation.tailrec

/** Utility functions used in examples and tests for collection processing.
  *
  * This object contains small, well-commented utility methods that demonstrate numeric
  * type-parameterization, parallel and sequential aggregation, and common algorithms (factorial,
  * gcd, distance) used as educational examples for Scala developers learning about Java-like APIs.
  */
object MathUtils {

  /** Square each element of a sequence using the provided Numeric evidence.
    *
    * @tparam T
    *   numeric element type (e.g. Int, Double)
    * @param input
    *   sequence of elements to square
    * @return
    *   a sequence containing the square of each input element
    */
  def square[T: Numeric](input: Seq[T]): Seq[T] = {
    val num = implicitly[Numeric[T]]
    ParCollection.applyF(input, x => num.times(x, x))
  }

  /** Square each element of a sequence in parallel using a fork/join style helper.
    *
    * This is a demonstration of running an element-wise transformation in parallel with a
    * configurable level of parallelism. It requires a Numeric instance for the element type.
    *
    * @tparam T
    *   numeric element type
    * @param input
    *   sequence of elements to square
    * @param parallelism
    *   number of parallel workers to use (default: 4)
    * @return
    *   a sequence containing the square of each input element
    */
  def squarePar[T: Numeric](input: Seq[T], parallelism: Int = 4): Seq[T] = {
    val num = implicitly[Numeric[T]]
    ParCollection.applyParForkF(input, x => num.times(x, x), parallelism)
  }

  /** Compute the sum of squares of the input sequence.
    *
    * Useful as a building block for variance and similar statistics. Runs the operation in parallel
    * using the provided ParCollection helper.
    *
    * @tparam T
    *   numeric element type
    * @param input
    *   sequence of elements to process
    * @return
    *   the sum of squares of the input elements
    */
  def sumOfSquares[T: Numeric](input: Seq[T]): T = {
    val num = implicitly[Numeric[T]]
    ParCollection.aggregateParForkF(
      input       = input,
      f           = x => num.times(x, x),         // Transform: square each number
      aggregate   = (acc, x) => num.plus(acc, x), // Combine: sum them up
      zero        = num.zero,
      parallelism = 4
    )
  }

  /** Compute the arithmetic mean (average) of the input sequence.
    *
    * Note: returns Double to handle fractional results for integral input types.
    *
    * @tparam T
    *   numeric element type
    * @param input
    *   non-empty sequence of numbers
    * @return
    *   the mean as Double
    */
  def mean[T: Numeric](input: Seq[T]): Double = {
    val num = implicitly[Numeric[T]]
    // val sum = input.foldLeft(num.zero)((acc, x) => num.plus(acc, x))
    val sum = ParCollection.aggregateParForkF(
      input       = input,
      f           = identity, // Identity function
      aggregate   = num.plus, // (acc, x) => num.plus(acc, x), // Combine: sum them up
      zero        = num.zero,
      parallelism = 4
    )
    num.toDouble(sum) / input.size
  }

  /** Compute the population variance of the input sequence.
    *
    * Uses the identity: variance = Σ(x²)/n - mean². Returns Double.
    *
    * @tparam T
    *   numeric element type
    * @param input
    *   non-empty sequence of numbers
    * @return
    *   the population variance as Double
    */
  def variance[T: Numeric](input: Seq[T]): Double = {
    val num               = implicitly[Numeric[T]]
    val meanValue         = mean(input)
    val sumOfSquaresValue = sumOfSquares(input)
    (num.toDouble(sumOfSquaresValue) / input.size) - (meanValue * meanValue)
  }

  /** Simple 2D point used for distance examples.
    *
    * @param x
    *   x-coordinate
    * @param y
    *   y-coordinate
    */
  case class Point(x: Double, y: Double)

  /** Euclidean distance between two 2D points.
    *
    * @param p1
    *   first point
    * @param p2
    *   second point
    * @return
    *   the Euclidean distance between p1 and p2
    */
  def euclideanDistance(p1: Point, p2: Point): Double = {
    val deltaX = p1.x - p2.x
    val deltaY = p1.y - p2.y
    math.sqrt(deltaX * deltaX + deltaY * deltaY)
  }

  /** Tail-recursive factorial implementation with accumulator.
    *
    * @param n
    *   non-negative integer
    * @param acc
    *   accumulator (used in recursion, default 1)
    * @return
    *   n! as Long
    */
  @tailrec
  def factorial(n: Int, acc: Long = 1L): Long =
    if (n <= 1) acc
    else factorial(n - 1, acc * n)

  /** Compute the product of factorials for the given integers.
    *
    * Example: for [1,2,3] returns 1!*2!*3! = 1*2*6 = 12.
    *
    * @param input
    *   sequence of integers
    * @return
    *   product of factorials as Long
    */
  def productOfFactorials(input: Seq[Int]): Long = ParCollection.aggregateF(
    input,
    f         = factorial(_),
    aggregate = (acc: Long, x: Long) => acc * x,
    zero      = 1L
  )

  /** Compute the greatest common divisor (GCD) of two integers using Euclid's algorithm.
    *
    * @param a
    *   first integer
    * @param b
    *   second integer
    * @return
    *   gcd(a, b)
    */
  @tailrec
  def gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

  /** Compute the GCD of all numbers in a sequence.
    *
    * Note: Uses the sequence head as the initial value for aggregation.
    *
    * @param input
    *   non-empty sequence of integers
    * @return
    *   gcd of all elements
    */
  def gcd(input: Seq[Int]): Int = ParCollection.aggregateF(
    input,
    f         = identity, // Identity function
    aggregate = gcd,
    zero      = input.head
  )
}
