package io.github.sps23.interview.preparation.locks;

import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A tiny mutable account guarded by its own lock, used to demonstrate
 * cross-lock deadlocks.
 */
public final class BankAccount {
    private final String id;
    private final ReentrantLock lock = new ReentrantLock();
    private long balanceInCents;

    public BankAccount(String id, long initialBalanceInCents) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.balanceInCents = initialBalanceInCents;
    }

    public String id() {
        return id;
    }

    ReentrantLock lock() {
        return lock;
    }

    long balanceInCents() {
        return balanceInCents;
    }

    void withdraw(long amountInCents) {
        balanceInCents -= amountInCents;
    }

    void deposit(long amountInCents) {
        balanceInCents += amountInCents;
    }

    public long balanceSnapshot() {
        lock.lock();
        try {
            return balanceInCents;
        } finally {
            lock.unlock();
        }
    }
}
