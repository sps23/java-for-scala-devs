package io.github.sps23.interview.preparation.messagepassing;

/**
 * The "old way" of protecting shared mutable state: every caller must remember
 * to synchronize on the same lock. Kept around only as a contrast to the
 * message-passing examples in {@link MessagePassingExamples}, where a single
 * owner thread removes the need for locking entirely.
 */
public final class LockedWallet {
    private final Object lock = new Object();
    private int balanceInCents = 0;

    public void topUp(int cents) {
        synchronized (lock) {
            // Every single caller - app, till, kiosk - must remember
            // to go through this method. Miss one spot and you have
            // a silent, hard-to-reproduce race condition.
            balanceInCents += cents;
        }
    }

    public int balance() {
        synchronized (lock) {
            return balanceInCents;
        }
    }
}
