package io.github.sps23.interview.preparation.atomicity

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.LongAdder

import scala.annotation.tailrec

object AtomicOperationsExamples:
  final class VolatileCounter:
    @volatile private var counter = 0

    def loseOneIncrementDeterministically(): Int =
      counter = 0

      val start      = new CountDownLatch(1)
      val bothRead   = new CountDownLatch(2)
      val allowWrite = new CountDownLatch(1)

      val first = Thread(() => stagedIncrement(start, bothRead, allowWrite), "counter-reader-1")
      val second = Thread(
        () => stagedIncrement(start, bothRead, allowWrite),
        "counter-reader-2"
      )

      first.start()
      second.start()

      start.countDown()
      await(bothRead)
      allowWrite.countDown()
      join(first)
      join(second)
      counter

    def currentValue: Int = counter

    private def stagedIncrement(
        start: CountDownLatch,
        bothRead: CountDownLatch,
        allowWrite: CountDownLatch
    ): Unit =
      await(start)
      val observed = counter
      bothRead.countDown()
      await(allowWrite)
      counter = observed + 1

  final case class TicketSnapshot(
      ticketsRemaining: Int,
      sellingOpen: Boolean,
      lastBuyer: Option[String]
  ):
    def sellTo(buyer: String): TicketSnapshot =
      val updatedRemaining = ticketsRemaining - 1
      TicketSnapshot(updatedRemaining, updatedRemaining > 0, Some(buyer))

  final class AtomicTicketOffice(initialTickets: Int, ticketPriceInCents: Long):
    require(initialTickets >= 0, "initialTickets cannot be negative")
    require(ticketPriceInCents >= 0, "ticketPriceInCents cannot be negative")

    private val ticketState = new AtomicReference[TicketSnapshot](
      TicketSnapshot(initialTickets, initialTickets > 0, None)
    )
    private val displayedQueueSize = new AtomicInteger(initialTickets)
    private val totalRevenueInCents = new AtomicLong(0L)
    private val soldOut             = new AtomicBoolean(initialTickets == 0)
    private val claimAttempts       = new LongAdder()

    def claimTicket(buyer: String): Boolean =
      val normalizedBuyer = normalizeBuyer(buyer)
      claimAttempts.increment()

      @tailrec
      def attempt(): Boolean =
        val observed = ticketState.get()
        if !observed.sellingOpen || observed.ticketsRemaining == 0 then
          soldOut.set(observed.ticketsRemaining == 0)
          false
        else
          val updated = observed.sellTo(normalizedBuyer)
          if ticketState.compareAndSet(observed, updated) then
            totalRevenueInCents.addAndGet(ticketPriceInCents)
            displayedQueueSize.set(updated.ticketsRemaining)
            soldOut.set(updated.ticketsRemaining == 0)
            true
          else attempt()

      attempt()

    def snapshot: TicketSnapshot = ticketState.get()

    def displayedQueueSizeValue: Int = displayedQueueSize.get()

    def totalRevenueInCentsValue: Long = totalRevenueInCents.get()

    def claimAttemptsValue: Long = claimAttempts.sum()

    def soldOutFlag: Boolean = soldOut.get()

    private def normalizeBuyer(buyer: String): String =
      val normalizedBuyer = Option(buyer).map(_.trim).getOrElse("")
      require(normalizedBuyer.nonEmpty, "buyer cannot be blank")
      normalizedBuyer

  final class SplitAtomicTicketOffice(initialTickets: Int):
    require(initialTickets >= 0, "initialTickets cannot be negative")

    private val remainingTickets = new AtomicInteger(initialTickets)
    private val soldOut          = new AtomicBoolean(initialTickets == 0)

    @tailrec
    final def claimLastTicket(beforeSoldOutFlagUpdate: => Unit): Boolean =
      val observed = remainingTickets.get()
      if observed == 0 then
        soldOut.set(true)
        false
      else if remainingTickets.compareAndSet(observed, observed - 1) then
        beforeSoldOutFlagUpdate
        if observed - 1 == 0 then
          soldOut.set(true)
        true
      else claimLastTicket(beforeSoldOutFlagUpdate)

    def remainingTicketsValue: Int = remainingTickets.get()

    def soldOutFlag: Boolean = soldOut.get()

  private def await(latch: CountDownLatch): Unit =
    try latch.await()
    catch
      case exception: InterruptedException =>
        Thread.currentThread().interrupt()
        throw IllegalStateException("Interrupted while coordinating the demo", exception)

  private def join(thread: Thread): Unit =
    try thread.join()
    catch
      case exception: InterruptedException =>
        Thread.currentThread().interrupt()
        throw IllegalStateException("Interrupted while waiting for the demo threads", exception)
