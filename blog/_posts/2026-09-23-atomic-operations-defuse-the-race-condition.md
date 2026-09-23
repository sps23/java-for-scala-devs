---
layout: post
title: "Atomic Operations: Defuse the Race Condition"
description: "Learn how Java 21 atomics prevent lost updates, when to use compare-and-set or LongAdder, and how the same JVM atomic patterns map to Scala 3 and Kotlin."
date: 2026-09-23 13:00:00 +0000
categories: [concurrency]
tags: [java, java21, scala, scala3, kotlin, atomicity, atomics, concurrency, compare-and-set, longadder]
---

Two customers press **Buy** at almost the same moment, and there is only **one ticket left**. If both requests read “1 remaining” before either one writes back “0 remaining,” you have a race condition: two happy customers, one unhappy support team, and a database row that now tells a lie.

## The Problem / Context

For Scala developers, this is the classic shared-mutable-state problem in JVM clothing. In Java, Kotlin, and Scala, the dangerous part is not reading a value or writing a value by itself. The dangerous part is doing a **read, then change, then write** sequence while another thread is doing the same thing.

A tiny example is `counter++`:

```java
volatile int counter = 0;
counter++;
```

That looks like one action, but the JVM treats it as three steps:

1. Read `counter`
2. Add `1`
3. Write the new value back

If two threads both read `0`, both compute `1`, and both write `1`, one update is lost.

<div class="table-wrapper" markdown="1">

| Thread A | Thread B | Shared value |
|----------|----------|--------------|
| reads `0` | reads `0` | `0` |
| computes `1` | computes `1` | `0` |
| writes `1` | writes `1` | `1` |

</div>

`volatile` helps with **visibility**: one thread sees another thread's latest write. It does **not** make a compound action like `counter++` atomic. That is why the tests for this post include a deterministic demo that still loses one increment even with a volatile field.

## Basic Atomic Tools

The core JVM atomics solve slightly different problems. In the ticket example, each one plays a different role.

<div class="table-wrapper" markdown="1">

| Type | Good for | Ticket example use |
|------|----------|--------------------|
| `AtomicInteger` | Small mutable numeric state | A displayed queue size or tickets remaining counter |
| `AtomicLong` | Exact numeric totals | Total revenue in cents |
| `AtomicBoolean` | On/off state | A `soldOut` flag |
| `AtomicReference<T>` | Replacing an immutable snapshot safely | Publishing a new `TicketSnapshot` |
| `LongAdder` | Hot counters with heavy contention | Claim-attempt metrics |

</div>

The key mindset is simple: atomics protect **one value at a time**. If your whole business change can be represented as “replace the old snapshot with this new snapshot only if nobody changed it first,” `AtomicReference` becomes very powerful.

## The Solution / Implementation

The repository examples use one immutable `TicketSnapshot` value and publish updates with `compareAndSet`. That keeps the running example the same in Java 21, Scala 3, and Kotlin.

<div class="code-tabs" data-tabs-id="atomicity-ticket-claim-tabs">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kt">boolean</span> <span class="nf">claimTicket</span><span class="o">(</span><span class="nc">String</span> <span class="n">buyer</span><span class="o">)</span> <span class="o">{</span>
    <span class="kd">var</span> <span class="n">normalizedBuyer</span> <span class="o">=</span> <span class="n">normalizeBuyer</span><span class="o">(</span><span class="n">buyer</span><span class="o">);</span>
    <span class="n">claimAttempts</span><span class="o">.</span><span class="na">increment</span><span class="o">();</span>
    <span class="k">while</span> <span class="o">(</span><span class="kc">true</span><span class="o">)</span> <span class="o">{</span>
        <span class="kd">var</span> <span class="n">observed</span> <span class="o">=</span> <span class="n">ticketState</span><span class="o">.</span><span class="na">get</span><span class="o">();</span>
        <span class="k">if</span> <span class="o">(!</span><span class="n">observed</span><span class="o">.</span><span class="na">sellingOpen</span><span class="o">()</span> <span class="o">||</span> <span class="n">observed</span><span class="o">.</span><span class="na">ticketsRemaining</span><span class="o">()</span> <span class="o">==</span> <span class="mi">0</span><span class="o">)</span> <span class="o">{</span>
            <span class="n">soldOut</span><span class="o">.</span><span class="na">set</span><span class="o">(</span><span class="n">observed</span><span class="o">.</span><span class="na">ticketsRemaining</span><span class="o">()</span> <span class="o">==</span> <span class="mi">0</span><span class="o">);</span>
            <span class="k">return</span> <span class="kc">false</span><span class="o">;</span>
        <span class="o">}</span>
        <span class="kd">var</span> <span class="n">updated</span> <span class="o">=</span> <span class="n">observed</span><span class="o">.</span><span class="na">sellTo</span><span class="o">(</span><span class="n">normalizedBuyer</span><span class="o">);</span>
        <span class="k">if</span> <span class="o">(</span><span class="n">ticketState</span><span class="o">.</span><span class="na">compareAndSet</span><span class="o">(</span><span class="n">observed</span><span class="o">,</span> <span class="n">updated</span><span class="o">))</span> <span class="o">{</span>
            <span class="n">totalRevenueInCents</span><span class="o">.</span><span class="na">addAndGet</span><span class="o">(</span><span class="n">ticketPriceInCents</span><span class="o">);</span>
            <span class="n">displayedQueueSize</span><span class="o">.</span><span class="na">set</span><span class="o">(</span><span class="n">updated</span><span class="o">.</span><span class="na">ticketsRemaining</span><span class="o">());</span>
            <span class="n">soldOut</span><span class="o">.</span><span class="na">set</span><span class="o">(</span><span class="n">updated</span><span class="o">.</span><span class="na">ticketsRemaining</span><span class="o">()</span> <span class="o">==</span> <span class="mi">0</span><span class="o">);</span>
            <span class="k">return</span> <span class="kc">true</span><span class="o">;</span>
        <span class="o">}</span>
    <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/atomicity/AtomicOperationsExamples.java">View full Java example</a></p>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">def</span> <span class="n">claimTicket</span><span class="o">(</span><span class="n">buyer</span><span class="o">:</span> <span class="kt">String</span><span class="o">):</span> <span class="kt">Boolean</span> <span class="o">=</span>
  <span class="k">val</span> <span class="n">normalizedBuyer</span> <span class="o">=</span> <span class="n">normalizeBuyer</span><span class="o">(</span><span class="n">buyer</span><span class="o">)</span>
  <span class="n">claimAttempts</span><span class="o">.</span><span class="n">increment</span><span class="o">()</span>
  <span class="nd">@tailrec</span>
  <span class="k">def</span> <span class="n">attempt</span><span class="o">():</span> <span class="kt">Boolean</span> <span class="o">=</span>
    <span class="k">val</span> <span class="n">observed</span> <span class="o">=</span> <span class="n">ticketState</span><span class="o">.</span><span class="n">get</span><span class="o">()</span>
    <span class="k">if</span> <span class="o">!</span><span class="n">observed</span><span class="o">.</span><span class="n">sellingOpen</span> <span class="o">||</span> <span class="n">observed</span><span class="o">.</span><span class="n">ticketsRemaining</span> <span class="o">==</span> <span class="mi">0</span> <span class="k">then</span>
      <span class="n">soldOut</span><span class="o">.</span><span class="n">set</span><span class="o">(</span><span class="n">observed</span><span class="o">.</span><span class="n">ticketsRemaining</span> <span class="o">==</span> <span class="mi">0</span><span class="o">)</span>
      <span class="kc">false</span>
    <span class="k">else</span>
      <span class="k">val</span> <span class="n">updated</span> <span class="o">=</span> <span class="n">observed</span><span class="o">.</span><span class="n">sellTo</span><span class="o">(</span><span class="n">normalizedBuyer</span><span class="o">)</span>
      <span class="k">if</span> <span class="n">ticketState</span><span class="o">.</span><span class="n">compareAndSet</span><span class="o">(</span><span class="n">observed</span><span class="o">,</span> <span class="n">updated</span><span class="o">)</span> <span class="k">then</span>
        <span class="n">totalRevenueInCents</span><span class="o">.</span><span class="n">addAndGet</span><span class="o">(</span><span class="n">ticketPriceInCents</span><span class="o">)</span>
        <span class="n">displayedQueueSize</span><span class="o">.</span><span class="n">set</span><span class="o">(</span><span class="n">updated</span><span class="o">.</span><span class="n">ticketsRemaining</span><span class="o">)</span>
        <span class="n">soldOut</span><span class="o">.</span><span class="n">set</span><span class="o">(</span><span class="n">updated</span><span class="o">.</span><span class="n">ticketsRemaining</span> <span class="o">==</span> <span class="mi">0</span><span class="o">)</span>
        <span class="kc">true</span>
      <span class="k">else</span> <span class="n">attempt</span><span class="o">()</span>
  <span class="n">attempt</span><span class="o">()</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/atomicity/AtomicOperationsExamples.scala">View full Scala example</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">fun</span> <span class="nf">claimTicket</span><span class="p">(</span><span class="n">buyer</span><span class="p">:</span> <span class="nc">String</span><span class="p">):</span> <span class="nc">Boolean</span> <span class="p">{</span>
    <span class="k">val</span> <span class="py">normalizedBuyer</span> <span class="p">=</span> <span class="nf">normalizeBuyer</span><span class="p">(</span><span class="n">buyer</span><span class="p">)</span>
    <span class="n">claimAttempts</span><span class="p">.</span><span class="nf">increment</span><span class="p">()</span>
    <span class="k">while</span> <span class="p">(</span><span class="kc">true</span><span class="p">)</span> <span class="p">{</span>
        <span class="k">val</span> <span class="py">observed</span> <span class="p">=</span> <span class="n">ticketState</span><span class="p">.</span><span class="nf">get</span><span class="p">()</span>
        <span class="k">if</span> <span class="p">(!</span><span class="n">observed</span><span class="p">.</span><span class="n">sellingOpen</span> <span class="o">||</span> <span class="n">observed</span><span class="p">.</span><span class="n">ticketsRemaining</span> <span class="o">==</span> <span class="mi">0</span><span class="p">)</span> <span class="p">{</span>
            <span class="n">soldOut</span><span class="p">.</span><span class="nf">set</span><span class="p">(</span><span class="n">observed</span><span class="p">.</span><span class="n">ticketsRemaining</span> <span class="o">==</span> <span class="mi">0</span><span class="p">)</span>
            <span class="k">return</span> <span class="kc">false</span>
        <span class="p">}</span>
        <span class="k">val</span> <span class="py">updated</span> <span class="p">=</span> <span class="n">observed</span><span class="p">.</span><span class="nf">sellTo</span><span class="p">(</span><span class="n">normalizedBuyer</span><span class="p">)</span>
        <span class="k">if</span> <span class="p">(</span><span class="n">ticketState</span><span class="p">.</span><span class="nf">compareAndSet</span><span class="p">(</span><span class="n">observed</span><span class="p">,</span> <span class="n">updated</span><span class="p">))</span> <span class="p">{</span>
            <span class="n">totalRevenueInCents</span><span class="p">.</span><span class="nf">addAndGet</span><span class="p">(</span><span class="n">ticketPriceInCents</span><span class="p">)</span>
            <span class="n">displayedQueueSize</span><span class="p">.</span><span class="nf">set</span><span class="p">(</span><span class="n">updated</span><span class="p">.</span><span class="n">ticketsRemaining</span><span class="p">)</span>
            <span class="n">soldOut</span><span class="p">.</span><span class="nf">set</span><span class="p">(</span><span class="n">updated</span><span class="p">.</span><span class="n">ticketsRemaining</span> <span class="o">==</span> <span class="mi">0</span><span class="p">)</span>
            <span class="k">return</span> <span class="kc">true</span>
        <span class="p">}</span>
    <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/atomicity/AtomicOperationsExamples.kt">View full Kotlin example</a></p>
</div>
</div>

## Compare-and-Set: "Only If Nothing Changed"

`compareAndSet` is the crucial idea here:

1. Read the current snapshot.
2. Build the new snapshot you want.
3. Replace it **only if** the old snapshot is still the same one you saw.
4. If another thread got there first, retry with the newer state.

That retry loop is why two buyers cannot both win the last ticket in the repository tests. One thread swaps the snapshot from `1 remaining` to `0 remaining`; the other thread re-reads the newer snapshot and immediately sees that the sale is already over.

## The Boundary of Atomicity

One atomic variable does **not** make a whole workflow atomic.

In the example code, `SplitAtomicTicketOffice` uses one atomic counter and one atomic flag. That still leaves a gap where another thread can observe:

- `remainingTickets == 0`
- `soldOut == false`

That sounds impossible in business terms, but it is perfectly possible in code if you update those two atomics in separate steps. This is the boundary of atomicity: **one protected variable is not the same thing as one protected transaction**.

If several fields must change together, model them as one immutable value and swap that value atomically with `AtomicReference`, or move the whole update into a stronger coordination mechanism.

## Choosing a Counter: AtomicLong or LongAdder?

Both work, but they are optimized for slightly different goals.

<div class="table-wrapper" markdown="1">

| Counter type | Best when | Trade-off |
|--------------|-----------|-----------|
| `AtomicLong` | You need an exact running total after every update | Contended updates all hit the same memory location |
| `LongAdder` | Many threads hammer a metric such as attempts, retries, or requests | `sum()` is not a single compare-and-set style value update |

</div>

In the ticket example, revenue is a good `AtomicLong`: it is a business total. Claim attempts are a good `LongAdder`: they are a hot metric, not the source of truth for ticket ownership.

## Scala and Kotlin Mental Model

Scala and Kotlin are not magically free from JVM atomicity rules. They both use the same underlying memory model, so immutable values still need a safe publication mechanism when multiple threads replace shared state.

<div class="table-wrapper" markdown="1">

| Language | Typical style | What still matters |
|----------|---------------|--------------------|
| Java 21 | `AtomicReference`, `AtomicLong`, `LongAdder` | Be explicit about safe publication and contention |
| Scala 3 | Immutable case classes plus Java atomics | Immutability helps reasoning, but shared updates still need coordination |
| Kotlin | Data classes plus Java atomics from `java.util.concurrent.atomic` | Read-only data does not prevent races on shared references |

</div>

So the cross-language lesson is the same: **immutable snapshots make state easier to reason about, and atomics make replacing those snapshots safe**.

## What the Tests Prove

The runnable tests in all three modules check three practical claims:

1. A volatile counter can still lose one increment.
2. Exactly one buyer can claim the last ticket when we use compare-and-set.
3. Two separate atomic fields do not turn a whole sequence into one atomic action.

That gives you a compact interview-ready story with real code instead of vague concurrency folklore.

## Interview Q&A: Atomicity in Practice

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>Why doesn't <code>volatile</code> fix <code>counter++</code>?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      <code>volatile</code> makes writes visible to other threads, but it does not glue a read and a write into one indivisible step. With <code>counter++</code>, two threads can still read the same old value before either one writes back the incremented value. You solve that with an atomic update primitive, not with visibility alone.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>When should I reach for <code>AtomicReference</code> instead of several smaller atomics?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Use <code>AtomicReference</code> when several fields together form one business fact. In the ticket example, the remaining count and last buyer make more sense as one snapshot than as scattered mutable pieces. That way a reader never sees a half-updated version of the state.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>How does compare-and-set actually prevent double selling?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Each buyer tries to replace the same old snapshot. Only one thread succeeds, because the atomic variable checks that the old value is still exactly the one that thread observed. The losing thread retries, sees the new snapshot with zero tickets left, and backs out cleanly.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>When is <code>LongAdder</code> better than <code>AtomicLong</code>?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      <code>LongAdder</code> is better for noisy metrics that many threads update all the time, like attempt counts or request totals. It spreads contention across internal cells, which usually scales better under heavy write pressure. For exact business totals that you conceptually treat as one shared value, <code>AtomicLong</code> is often the better fit.
    </div>
  </details>
</div>

## Conclusion

For Scala developers learning Java 21, atomicity is the reminder that visibility, immutability, and thread safety are related but not identical ideas. Once you see shared updates as “replace this snapshot only if nobody changed it first,” atomics become much easier to reason about in Java, Scala, and Kotlin.

## Code Samples

All examples in this post are runnable. Find them in the repository:
- [Java 21 AtomicOperationsExamples](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/atomicity/AtomicOperationsExamples.java)
- [Scala 3 AtomicOperationsExamples](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/atomicity/AtomicOperationsExamples.scala)
- [Kotlin AtomicOperationsExamples](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/atomicity/AtomicOperationsExamples.kt)
- [Java 21 tests](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/interview/preparation/atomicity/AtomicOperationsExamplesTest.java)
- [Scala 3 tests](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/interview/preparation/atomicity/AtomicOperationsExamplesTest.scala)
- [Kotlin tests](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/interview/preparation/atomicity/AtomicOperationsExamplesTest.kt)


---

*This is part of our [Java 21 Interview Preparation Guide - Your Roadmap to Success]({{ site.baseurl }}{% link _posts/2025-11-25-java21-interview-preparation-plan.md %}). Next related posts: [Stream API Advanced Operations]({{ site.baseurl }}{% link _posts/2025-11-29-stream-api-advanced-operations.md %}).*
