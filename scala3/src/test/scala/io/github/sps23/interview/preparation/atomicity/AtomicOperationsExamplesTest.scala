package io.github.sps23.interview.preparation.atomicity

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class AtomicOperationsExamplesTest extends AnyFunSuite with Matchers:

  test("Should show that volatile counter increments can still lose updates") {
    val counter = AtomicOperationsExamples.VolatileCounter()

    val finalValue = counter.loseOneIncrementDeterministically()

    finalValue shouldBe 1
    counter.currentValue shouldBe 1
  }

  test("Should allow only one buyer to claim the last ticket") {
    val office      = AtomicOperationsExamples.AtomicTicketOffice(1, 4500L)
    val start       = new CountDownLatch(1)
    val finished    = new CountDownLatch(2)
    val alexClaimed = new AtomicBoolean(false)
    val samClaimed  = new AtomicBoolean(false)

    val first = new Thread(
      () =>
        await(start)
        alexClaimed.set(office.claimTicket("Alex"))
        finished.countDown(),
      "alex-claimer"
    )
    val second = new Thread(
      () =>
        await(start)
        samClaimed.set(office.claimTicket("Sam"))
        finished.countDown(),
      "sam-claimer"
    )

    first.start()
    second.start()
    start.countDown()
    await(finished)

    List(alexClaimed.get(), samClaimed.get()).count(identity) shouldBe 1
    office.snapshot.ticketsRemaining shouldBe 0
    office.soldOutFlag shouldBe true
    office.displayedQueueSizeValue shouldBe 0
    office.totalRevenueInCentsValue shouldBe 4500L
    office.claimAttemptsValue shouldBe 2L
    Set("Alex", "Sam") should contain(office.snapshot.lastBuyer.get)
  }

  test("Should show that separate atomics do not make a full sequence atomic") {
    val office          = AtomicOperationsExamples.SplitAtomicTicketOffice(1)
    val seenRemaining   = new AtomicInteger(-1)
    val sawSoldOutFlag  = new AtomicBoolean(true)

    val claimed = office.claimLastTicket {
      seenRemaining.set(office.remainingTicketsValue)
      sawSoldOutFlag.set(office.soldOutFlag)
    }

    claimed shouldBe true
    seenRemaining.get() shouldBe 0
    sawSoldOutFlag.get() shouldBe false
    office.soldOutFlag shouldBe true
  }

  private def await(latch: CountDownLatch): Unit =
    try latch.await()
    catch
      case exception: InterruptedException =>
        Thread.currentThread().interrupt()
        throw IllegalStateException("Interrupted while running the test", exception)
