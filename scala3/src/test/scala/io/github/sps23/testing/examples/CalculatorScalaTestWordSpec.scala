package io.github.sps23.testing.examples

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers

/** ScalaTest WordSpec examples for Calculator. Demonstrates BDD-style testing with ScalaTest. */
class CalculatorScalaTestWordSpec extends AnyWordSpec with Matchers:

  val calculator = new Calculator

  "A Calculator" when {
    "performing basic operations" should {
      "add two numbers correctly" in {
        calculator.add(2, 3) shouldBe 5
        calculator.add(-1, 1) shouldBe 0
        calculator.add(-2, -3) shouldBe -5
      }

      "subtract two numbers correctly" in {
        calculator.subtract(3, 2) shouldBe 1
        calculator.subtract(-1, 1) shouldBe -2
        calculator.subtract(-2, -3) shouldBe 1
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

      "handle negative numbers as non-prime" in {
        calculator.isPrime(-5) shouldBe false
        calculator.isPrime(-1) shouldBe false
      }
    }
  }
