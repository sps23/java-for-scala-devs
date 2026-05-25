package io.github.sps23.fibres

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.*

/** Tests for FibreConcurrency showing race and parallel collection patterns. */
class FibreConcurrencySpec extends AnyFlatSpec with Matchers:

  private val runtime = Runtime.default

  private def run[A](effect: UIO[A]): A =
    Unsafe.unsafe(implicit unsafe => runtime.unsafe.run(effect).getOrThrow())

  "FibreConcurrency.fastest" should "return the cached result (wins the race)" in:
    run(FibreConcurrency.fastest) shouldBe "cached"

  "FibreConcurrency.allResults" should "return content for all three URLs" in:
    val results = run(FibreConcurrency.allResults)
    results should have length 3
    results should contain("content of url1")
    results should contain("content of url2")
    results should contain("content of url3")

  "FibreConcurrency.combined" should "run both fetches and return a tuple" in:
    val (user, orders) = run(FibreConcurrency.combined)
    user shouldBe "Alice"
    orders shouldBe List("order-1", "order-2")
