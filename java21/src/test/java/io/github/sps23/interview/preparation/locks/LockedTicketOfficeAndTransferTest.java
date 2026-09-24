package io.github.sps23.interview.preparation.locks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Lock-based ticket office and bank transfer tests")
class LockedTicketOfficeAndTransferTest {

    @Test
    @DisplayName("Should allow only one buyer to claim the last ticket under a single lock")
    void shouldAllowOnlyOneBuyerToClaimTheLastTicket() throws InterruptedException {
        var office = new LockedTicketOffice(1);
        var start = new CountDownLatch(1);
        var finished = new CountDownLatch(2);
        var alexClaimed = new AtomicBoolean();
        var samClaimed = new AtomicBoolean();

        var first = new Thread(() -> {
            await(start);
            alexClaimed.set(office.claimLastTicket());
            finished.countDown();
        }, "locked-alex-claimer");
        var second = new Thread(() -> {
            await(start);
            samClaimed.set(office.claimLastTicket());
            finished.countDown();
        }, "locked-sam-claimer");

        first.start();
        second.start();
        start.countDown();
        finished.await();

        assertEquals(1, countSuccessfulClaims(alexClaimed.get(), samClaimed.get()));
        assertEquals(0, office.remainingTickets());
        assertTrue(office.soldOutFlag());
        assertEquals("remaining=0, soldOut=true", office.snapshot());
    }

    @Test
    @DisplayName("Should never let remaining tickets and the sold-out flag disagree")
    void shouldNeverLetRemainingTicketsAndSoldOutFlagDisagree() throws InterruptedException {
        var office = new LockedTicketOffice(50);
        var threadCount = 8;
        var start = new CountDownLatch(1);
        var finished = new CountDownLatch(threadCount);
        var inconsistentSnapshotSeen = new AtomicBoolean(false);

        for (var i = 0; i < threadCount; i++) {
            new Thread(() -> {
                await(start);
                for (var attempt = 0; attempt < 20; attempt++) {
                    office.claimLastTicket();
                    var remaining = office.remainingTickets();
                    var soldOut = office.soldOutFlag();
                    if ((remaining == 0) != soldOut) {
                        inconsistentSnapshotSeen.set(true);
                    }
                }
                finished.countDown();
            }, "ticket-hammer-" + i).start();
        }

        start.countDown();
        finished.await();

        assertFalse(inconsistentSnapshotSeen.get());
        assertTrue(office.remainingTickets() >= 0);
        assertEquals(office.remainingTickets() == 0, office.soldOutFlag());
    }

    @Test
    @DisplayName("Should allow the same thread to reenter the lock without deadlocking itself")
    void shouldAllowSameThreadToReenterLockWithoutDeadlockingItself() {
        var office = new LockedTicketOffice(1);

        var description = office.describeAfterClaimAttempt();

        assertTrue(description.startsWith("claimed,"));
        assertEquals(0, office.holdCountForCurrentThread());
    }

    @Test
    @DisplayName("Should deadlock when two threads lock two accounts in opposite order")
    void shouldDeadlockWhenTwoThreadsLockTwoAccountsInOppositeOrder() throws InterruptedException {
        var accountA = new BankAccount("A", 10_000);
        var accountB = new BankAccount("B", 10_000);
        var firstHoldsA = new CountDownLatch(1);
        var secondHoldsB = new CountDownLatch(1);

        var first = new Thread(() -> {
            accountA.lock().lock();
            try {
                firstHoldsA.countDown();
                await(secondHoldsB);
                accountB.lock().lock();
                accountB.lock().unlock();
            } finally {
                accountA.lock().unlock();
            }
        }, "deadlock-a-then-b");
        first.setDaemon(true);

        var second = new Thread(() -> {
            accountB.lock().lock();
            try {
                secondHoldsB.countDown();
                await(firstHoldsA);
                accountA.lock().lock();
                accountA.lock().unlock();
            } finally {
                accountB.lock().unlock();
            }
        }, "deadlock-b-then-a");
        second.setDaemon(true);

        first.start();
        second.start();

        first.join(500);
        second.join(500);

        assertTrue(first.isAlive(), "First thread should still be blocked waiting on account B's lock");
        assertTrue(second.isAlive(), "Second thread should still be blocked waiting on account A's lock");
    }

    @Test
    @DisplayName("Should never deadlock when transfers always lock accounts in the same order")
    void shouldNeverDeadlockWhenTransfersLockAccountsInTheSameOrder() throws InterruptedException {
        var accountA = new BankAccount("A", 10_000);
        var accountB = new BankAccount("B", 10_000);
        var transferService = new BankAccountTransfer();
        var start = new CountDownLatch(1);
        var finished = new CountDownLatch(2);

        var aToB = new Thread(() -> {
            await(start);
            for (var i = 0; i < 200; i++) {
                transferService.transferOrdered(accountA, accountB, 1);
            }
            finished.countDown();
        }, "ordered-a-to-b");
        var bToA = new Thread(() -> {
            await(start);
            for (var i = 0; i < 200; i++) {
                transferService.transferOrdered(accountB, accountA, 1);
            }
            finished.countDown();
        }, "ordered-b-to-a");

        aToB.start();
        bToA.start();
        start.countDown();

        var completed = finished.await(5, TimeUnit.SECONDS);

        assertTrue(completed, "Ordered transfers must complete without deadlocking");
        assertEquals(20_000L, accountA.balanceSnapshot() + accountB.balanceSnapshot());
    }

    @Test
    @DisplayName("Should fail fast with a timeout instead of blocking forever on a busy account")
    void shouldFailFastWithTimeoutInsteadOfBlockingForeverOnABusyAccount() throws InterruptedException {
        var accountA = new BankAccount("A", 10_000);
        var accountB = new BankAccount("B", 10_000);
        var transferService = new BankAccountTransfer();
        var accountALocked = new CountDownLatch(1);
        var releaseAccountA = new CountDownLatch(1);

        var busyHolder = new Thread(() -> {
            accountA.lock().lock();
            try {
                accountALocked.countDown();
                await(releaseAccountA);
            } finally {
                accountA.lock().unlock();
            }
        }, "account-a-busy-holder");
        busyHolder.start();
        accountALocked.await();

        try {
            assertThrows(TimeoutException.class,
                    () -> transferService.transferWithTimeout(accountA, accountB, 100, 50));
        } finally {
            releaseAccountA.countDown();
            busyHolder.join();
        }
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
