package io.github.sps23.interview.preparation.atomicity

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.LongAdder

object AtomicOperationsExamples {
    class VolatileCounter {
        @Volatile
        private var counter: Int = 0

        fun loseOneIncrementDeterministically(): Int {
            counter = 0

            val start = CountDownLatch(1)
            val bothRead = CountDownLatch(2)
            val allowWrite = CountDownLatch(1)

            val first = Thread({ stagedIncrement(start, bothRead, allowWrite) }, "counter-reader-1")
            val second =
                Thread({ stagedIncrement(start, bothRead, allowWrite) }, "counter-reader-2")

            first.start()
            second.start()

            start.countDown()
            await(bothRead)
            allowWrite.countDown()
            join(first)
            join(second)
            return counter
        }

        fun currentValue(): Int = counter

        private fun stagedIncrement(
            start: CountDownLatch,
            bothRead: CountDownLatch,
            allowWrite: CountDownLatch,
        ) {
            await(start)
            val observed = counter
            bothRead.countDown()
            await(allowWrite)
            counter = observed + 1
        }
    }

    data class TicketSnapshot(
        val ticketsRemaining: Int,
        val sellingOpen: Boolean,
        val lastBuyer: String?,
    ) {
        fun sellTo(buyer: String): TicketSnapshot {
            val updatedRemaining = ticketsRemaining - 1
            return TicketSnapshot(updatedRemaining, updatedRemaining > 0, buyer)
        }
    }

    class AtomicTicketOffice(initialTickets: Int, private val ticketPriceInCents: Long) {
        private val ticketState =
            AtomicReference(TicketSnapshot(initialTickets, initialTickets > 0, null))
        private val displayedQueueSize = AtomicInteger(initialTickets)
        private val totalRevenueInCents = AtomicLong(0L)
        private val soldOut = AtomicBoolean(initialTickets == 0)
        private val claimAttempts = LongAdder()

        init {
            require(initialTickets >= 0) { "initialTickets cannot be negative" }
            require(ticketPriceInCents >= 0) { "ticketPriceInCents cannot be negative" }
        }

        fun claimTicket(buyer: String): Boolean {
            val normalizedBuyer = normalizeBuyer(buyer)
            claimAttempts.increment()

            while (true) {
                val observed = ticketState.get()
                if (!observed.sellingOpen || observed.ticketsRemaining == 0) {
                    soldOut.set(observed.ticketsRemaining == 0)
                    return false
                }

                val updated = observed.sellTo(normalizedBuyer)
                if (ticketState.compareAndSet(observed, updated)) {
                    totalRevenueInCents.addAndGet(ticketPriceInCents)
                    displayedQueueSize.set(updated.ticketsRemaining)
                    soldOut.set(updated.ticketsRemaining == 0)
                    return true
                }
            }
        }

        fun snapshot(): TicketSnapshot = ticketState.get()

        fun displayedQueueSize(): Int = displayedQueueSize.get()

        fun totalRevenueInCents(): Long = totalRevenueInCents.get()

        fun claimAttempts(): Long = claimAttempts.sum()

        fun soldOutFlag(): Boolean = soldOut.get()

        private fun normalizeBuyer(buyer: String): String {
            val normalizedBuyer = buyer.trim()
            require(normalizedBuyer.isNotBlank()) { "buyer cannot be blank" }
            return normalizedBuyer
        }
    }

    class SplitAtomicTicketOffice(initialTickets: Int) {
        private val remainingTickets = AtomicInteger(initialTickets)
        private val soldOut = AtomicBoolean(initialTickets == 0)

        init {
            require(initialTickets >= 0) { "initialTickets cannot be negative" }
        }

        fun claimLastTicket(beforeSoldOutFlagUpdate: (() -> Unit)? = null): Boolean {
            while (true) {
                val observed = remainingTickets.get()
                if (observed == 0) {
                    soldOut.set(true)
                    return false
                }

                if (remainingTickets.compareAndSet(observed, observed - 1)) {
                    beforeSoldOutFlagUpdate?.invoke()
                    if (observed - 1 == 0) {
                        soldOut.set(true)
                    }
                    return true
                }
            }
        }

        fun remainingTickets(): Int = remainingTickets.get()

        fun soldOutFlag(): Boolean = soldOut.get()
    }

    private fun await(latch: CountDownLatch) {
        try {
            latch.await()
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("Interrupted while coordinating the demo", exception)
        }
    }

    private fun join(thread: Thread) {
        try {
            thread.join()
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException(
                "Interrupted while waiting for the demo threads",
                exception,
            )
        }
    }
}
