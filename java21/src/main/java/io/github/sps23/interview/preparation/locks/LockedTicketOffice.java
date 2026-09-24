package io.github.sps23.interview.preparation.locks;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Fixes the exact gap shown by {@code SplitAtomicTicketOffice} in the atomicity
 * post: a separate atomic counter and a separate atomic flag can be
 * individually correct while the pair, observed together, is momentarily
 * inconsistent. Here both fields live inside one critical section guarded by a
 * single {@link ReentrantLock}, so "tickets left" and "sold out" always agree.
 */
public final class LockedTicketOffice {
    private final ReentrantLock lock = new ReentrantLock();
    private int remainingTickets;
    private boolean soldOut;

    public LockedTicketOffice(int initialTickets) {
        if (initialTickets < 0) {
            throw new IllegalArgumentException("initialTickets cannot be negative");
        }

        this.remainingTickets = initialTickets;
        this.soldOut = initialTickets == 0;
    }

    public boolean claimLastTicket() {
        lock.lock();
        try {
            if (remainingTickets == 0) {
                soldOut = true;
                return false;
            }

            remainingTickets--;
            if (remainingTickets == 0) {
                soldOut = true;
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Demonstrates reentrancy: this method is itself lock-guarded and calls
     * {@link #snapshot()}, which acquires the very same lock again on the same
     * thread. A plain mutex (for example a binary {@code Semaphore}) would deadlock
     * here; {@link ReentrantLock} tracks the owning thread and hold count, so
     * re-entering is safe.
     */
    public String describeAfterClaimAttempt() {
        lock.lock();
        try {
            var claimed = claimLastTicket();
            return (claimed ? "claimed, " : "rejected, ") + snapshot();
        } finally {
            lock.unlock();
        }
    }

    public String snapshot() {
        lock.lock();
        try {
            return "remaining=" + remainingTickets + ", soldOut=" + soldOut;
        } finally {
            lock.unlock();
        }
    }

    public int remainingTickets() {
        lock.lock();
        try {
            return remainingTickets;
        } finally {
            lock.unlock();
        }
    }

    public boolean soldOutFlag() {
        lock.lock();
        try {
            return soldOut;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Exposes the current hold count so tests can prove reentrant acquisition
     * happened.
     */
    int holdCountForCurrentThread() {
        return lock.getHoldCount();
    }
}
