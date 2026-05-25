package io.github.sps23.fibres

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.*

/** Tests for BasicFibres using ScalaTest with ZIO's runtime.
  *
  * We run ZIO effects synchronously using `Runtime.default.unsafe.run(...)` so they fit naturally
  * into the ScalaTest lifecycle.
  */
class BasicFibresSpec extends AnyFlatSpec with Matchers:

  private val runtime = Runtime.default

  private def run[A](effect: UIO[A]): A =
    Unsafe.unsafe(implicit unsafe => runtime.unsafe.run(effect).getOrThrow())

  "BasicFibres.program" should "return the combined user and order count" in:
    run(BasicFibres.program) shouldBe "Alice has 2 orders"

  it should "run both fetches concurrently (not sequentially)" in:
    val start = java.lang.System.currentTimeMillis()
    run(BasicFibres.program)
    val elapsed = java.lang.System.currentTimeMillis() - start
    // Sequential would be 100 + 80 = 180ms; parallel finishes in ~100ms
    elapsed should be < 170L
