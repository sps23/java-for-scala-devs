package io.github.sps23.interview.preparation.locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Classic "transfer money between two accounts" example used to show two
 * intrinsic lock hazards and their fixes:
 *
 * <ul>
 * <li>{@link #transferNaive} locks {@code from} then {@code to}. Two concurrent
 * transfers in opposite directions (A to B, and B to A) can each grab one lock
 * and wait forever for the other - a classic deadlock.
 * <li>{@link #transferOrdered} always locks accounts in a fixed, global order
 * (by {@code id}), so every thread agrees on the order and a circular wait can
 * never form.
 * <li>{@link #transferWithTimeout} adds a {@code tryLock} timeout as a second
 * line of defense: even if ordering were violated elsewhere, the transfer fails
 * fast instead of hanging.
 * </ul>
 */
public final class BankAccountTransfer {

    /**
     * Deliberately deadlock-prone: locks {@code from} first, then {@code to}, with
     * no ordering.
     */
    public void transferNaive(BankAccount from, BankAccount to, long amountInCents) {
        from.lock().lock();
        try {
            to.lock().lock();
            try {
                move(from, to, amountInCents);
            } finally {
                to.lock().unlock();
            }
        } finally {
            from.lock().unlock();
        }
    }

    /**
     * Deadlock-free: both accounts are always locked in the same, id-derived order.
     */
    public void transferOrdered(BankAccount from, BankAccount to, long amountInCents) {
        var first = from.id().compareTo(to.id()) <= 0 ? from : to;
        var second = first == from ? to : from;

        first.lock().lock();
        try {
            second.lock().lock();
            try {
                move(from, to, amountInCents);
            } finally {
                second.lock().unlock();
            }
        } finally {
            first.lock().unlock();
        }
    }

    /**
     * Belt-and-braces version: uses {@code tryLock} with a timeout instead of
     * blocking forever. Even a bug that violates lock ordering elsewhere degrades
     * into a failed transfer instead of a frozen application thread.
     */
    public void transferWithTimeout(BankAccount from, BankAccount to, long amountInCents, long timeoutMillis)
            throws TimeoutException {
        try {
            if (!from.lock().tryLock(timeoutMillis, TimeUnit.MILLISECONDS)) {
                throw new TimeoutException("Could not lock source account " + from.id());
            }
            try {
                if (!to.lock().tryLock(timeoutMillis, TimeUnit.MILLISECONDS)) {
                    throw new TimeoutException("Could not lock destination account " + to.id());
                }
                try {
                    move(from, to, amountInCents);
                } finally {
                    to.lock().unlock();
                }
            } finally {
                from.lock().unlock();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while transferring funds", exception);
        }
    }

    private void move(BankAccount from, BankAccount to, long amountInCents) {
        if (amountInCents <= 0) {
            throw new IllegalArgumentException("amountInCents must be positive");
        }
        if (from.balanceInCents() < amountInCents) {
            throw new IllegalStateException("Insufficient funds in account " + from.id());
        }

        from.withdraw(amountInCents);
        to.deposit(amountInCents);
    }
}
