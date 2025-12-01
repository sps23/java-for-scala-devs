package io.github.sps23.testing.examples

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

/**
 * Kotest StringSpec examples for Calculator.
 * Demonstrates the most concise Kotest testing style.
 */
class CalculatorKotestStringSpec : StringSpec({
    val calculator = Calculator()

    "addition should return sum of two numbers" {
        calculator.add(2, 3) shouldBe 5
        calculator.add(-1, 1) shouldBe 0
        calculator.add(-2, -3) shouldBe -5
    }

    "subtraction should return difference of two numbers" {
        calculator.subtract(3, 2) shouldBe 1
        calculator.subtract(-1, 1) shouldBe -2
        calculator.subtract(-2, -3) shouldBe 1
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

    "should handle negative numbers as non-prime" {
        calculator.isPrime(-5) shouldBe false
        calculator.isPrime(-1) shouldBe false
    }
})
