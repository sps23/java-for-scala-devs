package io.github.sps23.parcollection

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class MathUtilsTest extends AnyFunSuite with Matchers {

  test("square should compute squares of integers sequentially") {
    val input    = Seq(1, 2, 3, 4)
    val expected = Seq(1, 4, 9, 16)

    MathUtils.square(input) shouldBe expected
  }

  test("squarePar should compute squares of integers in parallel (values match)") {
    val input      = 1 to 100
    val sequential = MathUtils.square(input)
    val parallel   = MathUtils.squarePar(input)

    // Values must match (order may vary depending on parallel implementation)
    sequential.sorted shouldBe parallel.sorted
    sequential.sum shouldBe parallel.sum
  }

  test("sumOfSquares should return correct sum of squares") {
    val input = Seq(2, 4, 6)
    // 2^2 + 4^2 + 6^2 = 4 + 16 + 36 = 56
    MathUtils.sumOfSquares(input) shouldBe 56
  }

  test("mean should compute the arithmetic mean as Double") {
    val input = Seq(1, 2, 3, 4)
    MathUtils.mean(input) shouldBe 2.5
  }

  test("variance should compute the population variance") {
    val input = Seq(1, 2, 3, 4, 5)
    // sum of squares = 55, mean = 3, variance = 55/5 - 3^2 = 11 - 9 = 2
    Math.abs(MathUtils.variance(input) - 2.0) should be < 1e-9
  }

  test("euclideanDistance should compute distance between two points") {
    val p1 = MathUtils.Point(1.0, 2.0)
    val p2 = MathUtils.Point(4.0, 6.0)

    Math.abs(MathUtils.euclideanDistance(p1, p2) - 5.0) should be < 1e-9
  }

  test("factorial should compute factorial using tail-recursive implementation") {
    MathUtils.factorial(0) shouldBe 1L
    MathUtils.factorial(1) shouldBe 1L
    MathUtils.factorial(5) shouldBe 120L
  }

  test("productOfFactorials should combine factorials correctly") {
    val numbers = Seq(1, 2, 3, 4)
    // 1! * 2! * 3! * 4! = 1 * 2 * 6 * 24 = 288
    MathUtils.productOfFactorials(numbers) shouldBe 288L
  }

  test("gcd of a sequence should return the greatest common divisor") {
    val nums = Seq(48, 64, 80, 96)
    MathUtils.gcd(nums) shouldBe 16
  }
}
