package io.github.sps23.fibres

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import zio.*

/** Tests for FibreSupervision demonstrating typed error handling. */
class FibreSupervisionSpec extends AnyFlatSpec with Matchers:

  private val runtime = Runtime.default

  private def run[A](effect: UIO[A]): A =
    Unsafe.unsafe(implicit unsafe => runtime.unsafe.run(effect).getOrThrow())

  "FibreSupervision.withFallback" should "return a fallback string on NetworkError" in:
    run(FibreSupervision.withFallback) should startWith("fallback (network:")

  it should "not throw an exception (error is handled in the type)" in:
    noException should be thrownBy run(FibreSupervision.withFallback)
