package io.github.sps23.testing.examples

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/** JUnit 5 test examples for Calculator in Scala. Demonstrates using JUnit 5 with Scala 3. */
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
    @DisplayName("Subtraction should return difference of two numbers")
    def testSubtraction(): Unit =
      assertEquals(1, calculator.subtract(3, 2))
      assertEquals(-2, calculator.subtract(-1, 1))
      assertEquals(1, calculator.subtract(-2, -3))

    @Test
    @DisplayName("Multiplication should return product of two numbers")
    def testMultiplication(): Unit =
      assertEquals(6, calculator.multiply(2, 3))
      assertEquals(-2, calculator.multiply(-1, 2))
      assertEquals(6, calculator.multiply(-2, -3))

  @Nested
  @DisplayName("Division Operations")
  class DivisionOperations:

    @Test
    @DisplayName("Division should return quotient of two numbers")
    def testDivision(): Unit =
      assertEquals(2.5, calculator.divide(5, 2), 0.001)
      assertEquals(-2.0, calculator.divide(-4, 2), 0.001)

    @Test
    @DisplayName("Division by zero should throw ArithmeticException")
    def testDivisionByZero(): Unit =
      assertThrows(classOf[ArithmeticException], () => calculator.divide(5, 0))

  @Nested
  @DisplayName("Prime Number Checks")
  class PrimeNumberChecks:

    @ParameterizedTest
    @DisplayName("Should identify prime numbers correctly")
    @ValueSource(ints = Array(2, 3, 5, 7, 11, 13, 17, 19, 23, 29))
    def testPrimeNumbers(number: Int): Unit =
      assertTrue(calculator.isPrime(number), s"$number should be prime")

    @ParameterizedTest
    @DisplayName("Should identify non-prime numbers correctly")
    @ValueSource(ints = Array(0, 1, 4, 6, 8, 9, 10, 12, 15, 16))
    def testNonPrimeNumbers(number: Int): Unit =
      assertFalse(calculator.isPrime(number), s"$number should not be prime")
