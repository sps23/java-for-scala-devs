package io.github.sps23.interview.preparation.atomicity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Atomic operations Java 21 tests")
class AtomicOperationsExamplesTest {

    @Test
    @DisplayName("Should show that volatile counter increments can still lose updates")
    void shouldShowThatVolatileCounterIncrementsCanStillLoseUpdates() {
        var counter = new AtomicOperationsExamples.VolatileCounter();

        var finalValue = counter.loseOneIncrementDeterministically();

        assertEquals(1, finalValue);
        assertEquals(1, counter.currentValue());
    }

    @Test
    @DisplayName("Should allow only one buyer to claim the last ticket")
    void shouldAllowOnlyOneBuyerToClaimTheLastTicket() {
        var office = new AtomicTicketOffice(1, 4_500L);
        var start = new CountDownLatch(1);
        var finished = new CountDownLatch(2);
        var alexClaimed = new AtomicBoolean();
        var samClaimed = new AtomicBoolean();

        var first = new Thread(() -> {
            await(start);
            alexClaimed.set(office.claimTicket("Alex"));
            finished.countDown();
        }, "alex-claimer");
        var second = new Thread(() -> {
            await(start);
            samClaimed.set(office.claimTicket("Sam"));
            finished.countDown();
        }, "sam-claimer");

        first.start();
        second.start();
        start.countDown();
        await(finished);

        assertEquals(1, countSuccessfulClaims(alexClaimed.get(), samClaimed.get()));
        assertEquals(0, office.snapshot().ticketsRemaining());
        assertTrue(office.soldOutFlag());
        assertEquals(0, office.displayedQueueSize());
        assertEquals(4_500L, office.totalRevenueInCents());
        assertEquals(2L, office.claimAttempts());
        assertTrue(Set.of("Alex", "Sam").contains(office.snapshot().lastBuyer()));
    }

    @Test
    @DisplayName("Should atomically replace the complete ticket snapshot")
    void shouldAtomicallyReplaceTheCompleteTicketSnapshot() {
        var office = new AtomicReferenceTicketOffice(1);
        var start = new CountDownLatch(1);
        var finished = new CountDownLatch(2);
        var alexClaimed = new AtomicBoolean();
        var samClaimed = new AtomicBoolean();

        var first = new Thread(() -> {
            await(start);
            alexClaimed.set(office.claimTicket("Alex"));
            finished.countDown();
        }, "atomic-reference-alex-claimer");
        var second = new Thread(() -> {
            await(start);
            samClaimed.set(office.claimTicket("Sam"));
            finished.countDown();
        }, "atomic-reference-sam-claimer");

        first.start();
        second.start();
        start.countDown();
        await(finished);

        assertEquals(1, countSuccessfulClaims(alexClaimed.get(), samClaimed.get()));
        var snapshot = office.snapshot();
        assertEquals(0, snapshot.ticketsRemaining());
        assertFalse(snapshot.sellingOpen());
        assertTrue(Set.of("Alex", "Sam").contains(snapshot.lastBuyer()));
    }

    @Test
    @DisplayName("Should show that separate atomics do not make a full sequence atomic")
    void shouldShowThatSeparateAtomicsDoNotMakeAFullSequenceAtomic() {
        var office = new SplitAtomicTicketOffice(1);
        var seenRemaining = new AtomicInteger(-1);
        var sawSoldOutFlag = new AtomicBoolean(true);

        var claimed = office.claimLastTicket(() -> {
            seenRemaining.set(office.remainingTickets());
            sawSoldOutFlag.set(office.soldOutFlag());
        });

        assertTrue(claimed);
        assertEquals(0, seenRemaining.get());
        assertFalse(sawSoldOutFlag.get());
        assertTrue(office.soldOutFlag());
    }

    private static int countSuccessfulClaims(boolean... claims) {
        var successfulClaims = 0;
        for (var claim : claims) {
            if (claim) {
                successfulClaims++;
            }
        }
        return successfulClaims;
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while running the test", exception);
        }
    }
}
