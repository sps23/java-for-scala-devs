package io.github.sps23.interview.preparation.atomicity

import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Atomic operations Kotlin tests")
class AtomicOperationsExamplesTest {
    @Test
    @DisplayName("Should show that volatile counter increments can still lose updates")
    fun shouldShowThatVolatileCounterIncrementsCanStillLoseUpdates() {
        val counter = AtomicOperationsExamples.VolatileCounter()

        val finalValue = counter.loseOneIncrementDeterministically()

        assertEquals(1, finalValue)
        assertEquals(1, counter.currentValue())
    }

    @Test
    @DisplayName("Should allow only one buyer to claim the last ticket")
    fun shouldAllowOnlyOneBuyerToClaimTheLastTicket() {
        val office = AtomicOperationsExamples.AtomicTicketOffice(1, 4_500L)
        val start = CountDownLatch(1)
        val finished = CountDownLatch(2)
        val alexClaimed = AtomicBoolean(false)
        val samClaimed = AtomicBoolean(false)

        val first =
            Thread(
                {
                    await(start)
                    alexClaimed.set(office.claimTicket("Alex"))
                    finished.countDown()
                },
                "alex-claimer",
            )
        val second =
            Thread(
                {
                    await(start)
                    samClaimed.set(office.claimTicket("Sam"))
                    finished.countDown()
                },
                "sam-claimer",
            )

        first.start()
        second.start()
        start.countDown()
        await(finished)

        assertEquals(1, listOf(alexClaimed.get(), samClaimed.get()).count { it })
        assertEquals(0, office.snapshot().ticketsRemaining)
        assertTrue(office.soldOutFlag())
        assertEquals(0, office.displayedQueueSize())
        assertEquals(4_500L, office.totalRevenueInCents())
        assertEquals(2L, office.claimAttempts())
        assertTrue(setOf("Alex", "Sam").contains(office.snapshot().lastBuyer))
    }

    @Test
    @DisplayName("Should show that separate atomics do not make a full sequence atomic")
    fun shouldShowThatSeparateAtomicsDoNotMakeAFullSequenceAtomic() {
        val office = AtomicOperationsExamples.SplitAtomicTicketOffice(1)
        val seenRemaining = AtomicInteger(-1)
        val sawSoldOutFlag = AtomicBoolean(true)

        val claimed =
            office.claimLastTicket {
                seenRemaining.set(office.remainingTickets())
                sawSoldOutFlag.set(office.soldOutFlag())
            }

        assertTrue(claimed)
        assertEquals(0, seenRemaining.get())
        assertFalse(sawSoldOutFlag.get())
        assertTrue(office.soldOutFlag())
    }

    private fun await(latch: CountDownLatch) {
        try {
            latch.await()
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("Interrupted while running the test", exception)
        }
    }
}
