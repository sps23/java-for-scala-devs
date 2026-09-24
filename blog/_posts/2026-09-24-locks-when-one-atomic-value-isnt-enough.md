---
layout: post
title: "Locks: When One Atomic Value Isn't Enough"
description: "Learn when Java 21 locks beat atomics, how to protect cross-field invariants safely, and how synchronized vs ReentrantLock trade-offs change under virtual-thread pinning."
date: 2026-09-24 20:00:00 +0000
updated: 2026-09-24 21:00:00 +0000
categories: [concurrency]
tags: [java, java21, scala, scala3, kotlin, locks, synchronized, reentrantlock, deadlock, virtual-threads, pinning, atomicity]
---

Imagine you run a tiny pizza shop app where one thread updates "slices left" and another updates "orders accepted." If both numbers drift apart, your dashboard confidently announces: "0 slices left, 4 fresh orders accepted" — which is a great way to start a customer riot and a long apology email.

## The Problem / Context

Atomics are fantastic when one value changes independently. But many real workflows require **several fields to move together**.

For example, selling a pizza slice might need all of these updates as one business action:

- decrement `slicesLeft`
- increment `acceptedOrders`
- close sales when `slicesLeft == 0`

If those happen in separate atomic variables without one shared critical section, another thread can observe a half-updated state that violates your invariant.

## Key Concepts

<div class="table-wrapper" markdown="1">

| Concept | What it guarantees | What it does **not** guarantee |
|---------|--------------------|---------------------------------|
| `AtomicInteger` / `AtomicReference` | One variable update is atomic | Multi-field business transaction across separate variables |
| `synchronized` | Mutual exclusion + visibility for a critical section | Timeout, interruptible lock acquisition, fairness policy control |
| `ReentrantLock` | Explicit lock/unlock, timeout, interruptible acquisition, optional fairness | Automatic release if you forget `finally` |

</div>

For Scala developers: this is the same core idea as protecting a shared mutable region in JVM code. Immutable data avoids many of these problems, but once you keep mutable shared state, you need explicit coordination.

## The Solution / Implementation

A compact Java 21 example using one lock for a cross-field invariant:

```java
import java.util.concurrent.locks.ReentrantLock;

public final class PizzaCounter {
    private int slicesLeft = 8;
    private int acceptedOrders = 0;
    private boolean open = true;
    private final ReentrantLock lock = new ReentrantLock();

    public boolean acceptOrder() {
        lock.lock();
        try {
            if (!open || slicesLeft == 0) {
                open = false;
                return false;
            }

            slicesLeft--;
            acceptedOrders++;
            if (slicesLeft == 0) {
                open = false;
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    public String snapshot() {
        lock.lock();
        try {
            return "slices=" + slicesLeft + ", orders=" + acceptedOrders + ", open=" + open;
        } finally {
            lock.unlock();
        }
    }
}
```

The key point is not "ReentrantLock is always better." The key point is: one critical section protects the invariant that these fields must agree with each other.

### Closing the exact gap left by atomics

The [atomic operations post]({{ site.baseurl }}{% link _posts/2026-09-23-atomic-operations-defuse-the-race-condition.md %}) showed `SplitAtomicTicketOffice`: a separate `AtomicInteger` for `remainingTickets` and a separate `AtomicBoolean` for `soldOut`. Each field update is individually atomic, yet another thread can still observe `remainingTickets == 0` while `soldOut` is still `false`, because the two updates happen in two different atomic operations.

`LockedTicketOffice` fixes that by moving both fields under **one** `ReentrantLock`, so nobody ever sees them disagree:

```java
public final class LockedTicketOffice {
    private final ReentrantLock lock = new ReentrantLock();
    private int remainingTickets;
    private boolean soldOut;

    public LockedTicketOffice(int initialTickets) {
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

    public String snapshot() {
        lock.lock();
        try {
            return "remaining=" + remainingTickets + ", soldOut=" + soldOut;
        } finally {
            lock.unlock();
        }
    }
}
```

A concurrency test hammers this with several threads claiming tickets at once and asserts that `remainingTickets == 0` and `soldOut` **never** disagree — something `SplitAtomicTicketOffice` cannot guarantee, no matter how many atomics you add.

### Reentrancy: the feature `synchronized` and `ReentrantLock` both give you for free

A plain mutex only lets one thread hold it at a time — including the thread that already holds it. `ReentrantLock` (like `synchronized`) tracks *which thread* owns the lock and *how many times* it has been acquired, so the owning thread can re-enter without deadlocking itself:

```java
public String describeAfterClaimAttempt() {
    lock.lock();
    try {
        var claimed = claimLastTicket(); // re-acquires the same lock, same thread
        return (claimed ? "claimed, " : "rejected, ") + snapshot(); // re-acquires again
    } finally {
        lock.unlock();
    }
}
```

`ReentrantLock#getHoldCount()` even lets you assert this behavior directly in a test: the count climbs to 3 while nested, then drops back to 0 once every `unlock()` has matched a `lock()`. Try the same trick with a binary `Semaphore(1)` used as a mutex and the second `acquire()` call from the same thread blocks forever — semaphores are **not** reentrant.

## `synchronized` vs `ReentrantLock`

<div class="table-wrapper" markdown="1">

| Situation | Better default | Why |
|-----------|----------------|-----|
| Simple, short critical section | `synchronized` | Readable, safe, and easy to maintain |
| Need timed lock attempts (`tryLock`) | `ReentrantLock` | Avoid waiting forever |
| Need interruptible waiting (`lockInterruptibly`) | `ReentrantLock` | Better cancellation behavior |
| Need strict lock ordering policy in complex flows | `ReentrantLock` | More explicit control surface |

</div>

In interviews, a strong answer is usually: start with `synchronized` for straightforward code; choose `ReentrantLock` when explicit lock-control features are required.

## Lock Scope, Deadlock Avoidance, and Virtual-Thread Pinning

1. **Keep lock scope tiny.** Guard shared state updates, not slow I/O calls.
2. **Use a consistent lock order** when multiple locks are unavoidable.
3. **Prefer one lock per invariant** over many tiny locks that are impossible to reason about.
4. **Virtual-thread pinning matters in Java 21.** Long `synchronized` sections that block can pin carrier threads; this is one reason to keep synchronized regions short and evaluate explicit lock strategies when contention/blocking is non-trivial.

Think of locks like bathroom keys in a restaurant: one clear key cabinet is annoying but predictable; hiding random spare keys in five drawers is how you create chaos and manager meetings.

## A Good Use Case: Transferring Money Between Two Accounts

Locking one shared object is straightforward. The classic *hard* case is when a single operation must lock **two** shared objects at once — for example, transferring money between two bank accounts, each guarded by its own lock.

```java
/** Deliberately deadlock-prone: locks "from" first, then "to", with no ordering. */
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
```

This looks fine in isolation. It deadlocks the moment two threads transfer in opposite directions at the same time: Thread 1 locks account A and waits for account B; Thread 2 has already locked account B and waits for account A. Neither can proceed, and neither will ever time out on its own. A test in the repository proves this deterministically: it forces both threads to hold their first lock before reaching for the second, then confirms both threads are still blocked half a second later.

The fix is a **global, consistent lock order** — always lock accounts in the same sequence, regardless of transfer direction:

```java
/** Deadlock-free: both accounts are always locked in the same, id-derived order. */
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
```

Now every thread agrees on lock order, so a circular wait can never form — a test hammers this with concurrent transfers in both directions and confirms the total balance stays conserved and every transfer completes within a few seconds.

As a second line of defense, `ReentrantLock.tryLock(timeout, unit)` lets a transfer **fail fast** instead of hanging forever, even if ordering were somehow violated elsewhere:

```java
public void transferWithTimeout(BankAccount from, BankAccount to, long amountInCents, long timeoutMillis)
        throws TimeoutException {
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
}
```

This is the exact `tryLock` capability `synchronized` cannot give you — it is a strong reason to reach for `ReentrantLock` whenever an operation must acquire more than one lock.

## Best Practices

- Use atomics for single-value updates; use locks for cross-field invariants.
- Keep critical sections short and boring.
- Never call blocking I/O while holding a lock unless you absolutely must.
- Document which fields each lock protects.
- Validate lock strategy under realistic concurrency tests, not just "it worked once on my laptop."

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>When is a lock better than an atomic variable?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      A lock is better when several related fields must change together as one consistent business action. One atomic variable only protects that single variable, so cross-field invariants can still break. If your rule sounds like "all of these values must agree at the same moment," a lock is usually the safer tool.
    </div>
  </details>
  <details class="faq-item" open>
    <summary>
      <span>What trade-offs exist between <code>synchronized</code> and <code>ReentrantLock</code>?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      <code>synchronized</code> is simpler and harder to misuse, so it is a great default for small critical sections. <code>ReentrantLock</code> gives advanced controls like timeout and interruptible acquisition, which help in complex workflows. The trade-off is that you must remember explicit unlock discipline with <code>finally</code>, or you can deadlock your system.
    </div>
  </details>
  <details class="faq-item" open>
    <summary>
      <span>How do you protect several related fields consistently?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Define the invariant first, then protect every field participating in that invariant with the same lock. Update them in one critical section so other threads cannot see half-finished state. This is effectively making one tiny in-memory transaction for that shared object.
    </div>
  </details>
  <details class="faq-item" open>
    <summary>
      <span>What is the relationship between locks and virtual-thread pinning?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      In Java 21, long blocking work inside <code>synchronized</code> regions can pin carrier threads, which reduces virtual-thread scalability. The practical fix is to keep monitor-held sections very short and move slow work outside them. In some scenarios, explicit lock strategies and careful structure make this easier to enforce.
    </div>
  </details>
</div>

## Conclusion

Locks are not "old-fashioned" tools; they are what you use when your state updates must move in formation instead of solo dancing. For Scala developers learning Java 21, keep the mental model simple: immutable data first, atomics for one value, and locks for coordinated multi-field invariants — especially when virtual-thread behavior enters the conversation.

## Code Samples

All examples in this post are runnable and backed by concurrency tests proving the invariants hold under contention:

- [Java 21 LockedTicketOffice](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/locks/LockedTicketOffice.java) — fixes the exact race from `SplitAtomicTicketOffice`
- [Java 21 BankAccount](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/locks/BankAccount.java)
- [Java 21 BankAccountTransfer](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/locks/BankAccountTransfer.java) — naive, ordered, and timeout-guarded transfers
- [Java 21 tests](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/interview/preparation/locks/LockedTicketOfficeAndTransferTest.java) — proves the consistency fix, reentrancy, the deadlock, and its two fixes
- [Java 21 pinning and lock patterns](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/trickypatterns)
- [Java 21 virtual threads interview examples](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/interview/preparation/virtualthreads)
- [Java 21 Spring scope synchronization examples](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/spring/scopes)

---

*This is part of our Java 21 Immutability and Concurrency series. Start with [Java 21 Immutability and Concurrency Preparation Guide: From Shared Mutable State to Confidence]({{ site.baseurl }}{% link _posts/2026-09-24-java21-immutability-concurrency-preparation-guide.md %}).
Nearby related posts: [ZIO Fibres vs Java Virtual Threads vs Kotlin Coroutines]({{ site.baseurl }}{% link _posts/2026-05-25-zio-fibres-vs-virtual-threads-vs-coroutines.md %}) and [Virtual Threads and Structured Concurrency in Java 21]({{ site.baseurl }}{% link _posts/2025-11-29-virtual-threads-and-structured-concurrency.md %}).*
