package io.github.sps23.testing.examples

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe

/**
 * Kotest FunSpec examples for Calculator.
 * Demonstrates Kotlin idiomatic testing with Kotest.
 */
class CalculatorKotestFunSpec : FunSpec({
    val calculator = Calculator()

    context("Basic Operations") {
        test("addition should return sum of two numbers") {
            calculator.add(2, 3) shouldBe 5
            calculator.add(-1, 1) shouldBe 0
            calculator.add(-2, -3) shouldBe -5
        }

        test("subtraction should return difference of two numbers") {
            calculator.subtract(3, 2) shouldBe 1
            calculator.subtract(-1, 1) shouldBe -2
            calculator.subtract(-2, -3) shouldBe 1
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

        test("should handle negative numbers as non-prime") {
            calculator.isPrime(-5) shouldBe false
            calculator.isPrime(-1) shouldBe false
        }
    }
})
