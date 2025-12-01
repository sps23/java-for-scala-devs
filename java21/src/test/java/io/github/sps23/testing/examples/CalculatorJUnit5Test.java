package io.github.sps23.testing.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * JUnit 5 test examples for Calculator.
 * Demonstrates modern Java testing with Jupiter API.
 */
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
        @DisplayName("Subtraction should return difference of two numbers")
        void testSubtraction() {
            assertEquals(1, calculator.subtract(3, 2));
            assertEquals(-2, calculator.subtract(-1, 1));
            assertEquals(1, calculator.subtract(-2, -3));
        }

        @Test
        @DisplayName("Multiplication should return product of two numbers")
        void testMultiplication() {
            assertEquals(6, calculator.multiply(2, 3));
            assertEquals(-2, calculator.multiply(-1, 2));
            assertEquals(6, calculator.multiply(-2, -3));
        }
    }

    @Nested
    @DisplayName("Division Operations")
    class DivisionOperations {

        @Test
        @DisplayName("Division should return quotient of two numbers")
        void testDivision() {
            assertEquals(2.5, calculator.divide(5, 2), 0.001);
            assertEquals(-2.0, calculator.divide(-4, 2), 0.001);
        }

        @Test
        @DisplayName("Division by zero should throw ArithmeticException")
        void testDivisionByZero() {
            assertThrows(ArithmeticException.class, () -> calculator.divide(5, 0));
        }
    }

    @Nested
    @DisplayName("Prime Number Checks")
    class PrimeNumberChecks {

        @ParameterizedTest
        @DisplayName("Should identify prime numbers correctly")
        @ValueSource(ints = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29})
        void testPrimeNumbers(int number) {
            assertTrue(calculator.isPrime(number), number + " should be prime");
        }

        @ParameterizedTest
        @DisplayName("Should identify non-prime numbers correctly")
        @ValueSource(ints = {0, 1, 4, 6, 8, 9, 10, 12, 15, 16})
        void testNonPrimeNumbers(int number) {
            assertFalse(calculator.isPrime(number), number + " should not be prime");
        }

        @ParameterizedTest
        @DisplayName("Should handle edge cases")
        @CsvSource({"-5, false", "-1, false", "0, false", "1, false"})
        void testEdgeCases(int number, boolean expected) {
            assertEquals(expected, calculator.isPrime(number));
        }
    }
}
