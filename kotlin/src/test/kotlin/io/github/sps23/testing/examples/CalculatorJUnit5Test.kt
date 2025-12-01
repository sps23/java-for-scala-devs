package io.github.sps23.testing.examples

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

/**
 * JUnit 5 test examples for Calculator in Kotlin.
 * Demonstrates using JUnit 5 with Kotlin.
 */
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
        @DisplayName("Subtraction should return difference of two numbers")
        fun testSubtraction() {
            assertEquals(1, calculator.subtract(3, 2))
            assertEquals(-2, calculator.subtract(-1, 1))
            assertEquals(1, calculator.subtract(-2, -3))
        }

        @Test
        @DisplayName("Multiplication should return product of two numbers")
        fun testMultiplication() {
            assertEquals(6, calculator.multiply(2, 3))
            assertEquals(-2, calculator.multiply(-1, 2))
            assertEquals(6, calculator.multiply(-2, -3))
        }
    }

    @Nested
    @DisplayName("Division Operations")
    inner class DivisionOperations {

        @Test
        @DisplayName("Division should return quotient of two numbers")
        fun testDivision() {
            assertEquals(2.5, calculator.divide(5, 2), 0.001)
            assertEquals(-2.0, calculator.divide(-4, 2), 0.001)
        }

        @Test
        @DisplayName("Division by zero should throw ArithmeticException")
        fun testDivisionByZero() {
            assertThrows(ArithmeticException::class.java) { calculator.divide(5, 0) }
        }
    }

    @Nested
    @DisplayName("Prime Number Checks")
    inner class PrimeNumberChecks {

        @ParameterizedTest
        @DisplayName("Should identify prime numbers correctly")
        @ValueSource(ints = [2, 3, 5, 7, 11, 13, 17, 19, 23, 29])
        fun testPrimeNumbers(number: Int) {
            assertTrue(calculator.isPrime(number), "$number should be prime")
        }

        @ParameterizedTest
        @DisplayName("Should identify non-prime numbers correctly")
        @ValueSource(ints = [0, 1, 4, 6, 8, 9, 10, 12, 15, 16])
        fun testNonPrimeNumbers(number: Int) {
            assertFalse(calculator.isPrime(number), "$number should not be prime")
        }
    }
}
