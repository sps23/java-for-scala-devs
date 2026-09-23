package io.github.sps23.interview.preparation.visibility

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class VisibilityExamplesTest extends AnyFunSuite with Matchers:

  test("Should let a worker observe a volatile stop flag update") {
    val flag    = VisibilityExamples.VolatileRunningFlag()
    val started = new CountDownLatch(1)
    val stopped = new CountDownLatch(1)

    val worker = flag.startWorker(started, stopped)

    started.await(1, TimeUnit.SECONDS) shouldBe true
    flag.stop()

    stopped.await(1, TimeUnit.SECONDS) shouldBe true
    worker.join(1000L)
    worker.isAlive shouldBe false
  }

  test("Should show that volatile counter increments can still lose updates") {
    val counter = VisibilityExamples.VolatileCounter()

    val finalValue = counter.loseOneIncrementDeterministically()

    finalValue shouldBe 1
    counter.currentValue shouldBe 1
  }
