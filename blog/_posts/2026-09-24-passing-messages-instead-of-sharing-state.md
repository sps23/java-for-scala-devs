---
layout: post
title: "Passing Messages Instead of Sharing State"
description: "Learn when queue-based message passing beats shared mutable state in Java 21, and compare producer-consumer handoff and ownership boundaries with Scala 3 and Kotlin mental models."
date: 2026-09-24 20:00:00 +0000
categories: [concurrency]
tags: [java, java21, scala, scala3, kotlin, message-passing, producer-consumer, blockingqueue, concurrentlinkedqueue, ownership]
---

Your payment team gets two requirements in the same sprint: process top-ups from many API requests at once, and stop race-condition incidents caused by several threads mutating the same balance object. You can keep adding locks around shared state, or you can change the game and pass messages to a single owner thread.

## The Problem / Context

Shared mutable state is where concurrency bugs become expensive. Once several threads can update the same object directly, you are forced to reason about lock order, critical section scope, and edge cases where one thread sees half-finished state.

Message passing shifts the model:

- Producers submit work as messages.
- A clear owner processes those messages.
- Mutable state stays local to the owner.

That structure often feels natural to Scala developers, because it mirrors the same isolation instinct behind immutable values and explicit effect boundaries.

## Key Concepts

<div class="table-wrapper" markdown="1">

| Concept | Queue-based message passing | Lock-heavy shared state |
|---------|------------------------------|-------------------------|
| Ownership | One component owns mutation | Many threads touch the same object |
| Coordination | Messages + handoff protocol | Locking protocol |
| Failure mode | Backpressure, full queues, timeouts | Deadlocks, lock contention, stale reads |
| Reasoning style | "Who owns this state?" | "Which lock protects this field?" |

</div>

## The Solution / Implementation

The new examples in this repository use three practical message-passing patterns:

1. **Blocking handoff** with `LinkedBlockingQueue` for producer-consumer pipelines.
2. **Non-blocking drain** with `ConcurrentLinkedQueue` for opportunistic polling.
3. **Single-owner processing** where many producers enqueue updates and one owner applies them.

```java
public static int processWalletTopUpsWithSingleOwner(
        int producerCount,
        int topUpsPerProducer,
        int centsPerTopUp
) {
    var topUpMessages = new LinkedBlockingQueue<Integer>();
    var finalBalanceInCents = new AtomicInteger(0);

    var walletOwner = new Thread(() -> {
        var localBalance = 0;
        while (true) {
            var message = take(topUpMessages);
            if (message == Integer.MIN_VALUE) {
                finalBalanceInCents.set(localBalance);
                return;
            }
            localBalance += message;
        }
    });

    // Producers only enqueue messages; they never mutate localBalance directly.
    // ...
    return finalBalanceInCents.get();
}
```

This is the ownership boundary in one sentence: **only the owner thread mutates the balance**.

## Blocking vs Non-Blocking Handoff

<div class="table-wrapper" markdown="1">

| Handoff style | Java 21 building block | Best when |
|---------------|------------------------|-----------|
| Blocking | `LinkedBlockingQueue.take/poll(timeout)` | Consumers should wait for work instead of spin |
| Non-blocking | `ConcurrentLinkedQueue.poll()` | Work is optional and you want low-overhead checks |
| Bounded blocking | `ArrayBlockingQueue` | You need backpressure and explicit capacity |

</div>

In interview terms: blocking handoff is usually easier when each message must be processed; non-blocking handoff is useful for lightweight drains where "nothing available right now" is expected.

## What the Tests Prove

The mirrored tests in Java, Scala, and Kotlin verify:

1. Blocking queue handoff keeps message order for order processing.
2. Non-blocking queue drain exits cleanly when no messages remain.
3. Single-owner queue processing applies all concurrent top-ups without lost updates.

## Best Practices

- Start by identifying ownership boundaries before choosing low-level synchronization tools.
- Prefer message passing when one component can naturally own mutable state.
- Use blocking queues for must-process workflows and non-blocking queues for opportunistic polling.
- Define explicit shutdown messages or protocols (`poison pill`, close signal, or completion marker).
- Validate queue capacity and timeout behavior for production load, not only happy-path unit tests.

## Message Passing in Practice

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>When is message passing simpler than shared-state coordination?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Message passing is simpler when one logical owner can process updates in sequence. Instead of proving every read/write pair is protected by the right lock, you route all updates through one queue and one consumer. For example, a wallet service can enqueue top-up requests and let one owner thread apply them, which avoids lost updates without lock choreography.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>What is the difference between a queue-based design and a lock-heavy design?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Queue-based design coordinates by ownership and handoff, while lock-heavy design coordinates by mutual exclusion around shared objects. With queues, producers focus on sending work and the owner enforces consistency. With locks, every caller must follow the same lock discipline, and one mistake can introduce deadlocks or stale reads.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>How do ownership boundaries reduce race conditions?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Ownership boundaries reduce races by limiting who can mutate a value. If only one thread can change balance state, two threads cannot accidentally overwrite each other because there is no concurrent writer path. The queue becomes the synchronization point, and state mutation stays local and predictable.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>What Java 21 building blocks help with message-passing architectures?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      The practical core is in <code>java.util.concurrent</code>: blocking queues for producer-consumer handoff, concurrent queues for non-blocking mailboxes, and latches/scopes for lifecycle coordination. Pair these with immutable message payloads and clear owner responsibilities. This gives you a design that maps well to Scala's isolation-first mental model while staying fully idiomatic in Java 21.
    </div>
  </details>
</div>

## Conclusion

For Scala developers learning Java 21, message passing is usually the cleanest way to avoid shared-state traps: isolate mutation, hand off work explicitly, and let ownership boundaries carry the thread-safety burden.

## Code Samples

All examples in this post are runnable. Find them in the repository:
- [Java 21 message passing examples](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/interview/preparation/messagepassing)
- [Scala 3 message passing examples](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/interview/preparation/messagepassing)
- [Kotlin message passing examples](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/messagepassing)
- [Java 21 tests](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/test/java/io/github/sps23/interview/preparation/messagepassing)
- [Scala 3 tests](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/test/scala/io/github/sps23/interview/preparation/messagepassing)
- [Kotlin tests](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/test/kotlin/io/github/sps23/interview/preparation/messagepassing)

---

*This is part of our [Immutability and Concurrency Preparation Guide]({{ site.baseurl }}{% link _posts/2026-09-24-java21-immutability-concurrency-preparation-guide.md %}). Next related posts: [Atomic Operations: Defuse the Race Condition]({{ site.baseurl }}{% link _posts/2026-09-23-atomic-operations-defuse-the-race-condition.md %}) and [Concurrent Collections: One Pot, Many Spoons.]({{ site.baseurl }}{% link _posts/2026-09-23-concurrent-collections-one-pot-many-spoons.md %}).*
