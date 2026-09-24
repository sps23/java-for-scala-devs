---
layout: post
title: "Passing Messages Instead of Sharing State"
description: "Learn when queue-based message passing beats shared mutable state and lock juggling in Java 21, with a coffee-shop story and compared Java, Scala 3, and Kotlin implementations."
date: 2026-09-24 20:00:00 +0000
updated: 2026-09-24 21:00:00 +0000
categories: [concurrency]
tags: [java, java21, scala, scala3, kotlin, message-passing, producer-consumer, blockingqueue, concurrentlinkedqueue, ownership]
---

Welcome to **The Bean Counter Café**, the busiest coffee shop in town and, unfortunately, also the buggiest piece of concurrent software you will read about today. Three baristas take orders at once. The mobile app, the till, and the loyalty kiosk all try to top up the same customer's rewards wallet at the same time. And somewhere in the back, one very tired `synchronized` block is holding the whole shop together with duct tape.

This post is the sequel to locks and atomics. You already know how to protect a single variable with a lock or an atomic reference. Today we ask a more interesting question: what if, instead of protecting the shared thing, we simply stopped sharing it?

## The Problem / Context

Here is the mistake almost everyone makes on day one of learning concurrency: they see "three threads need to update the same counter" and immediately reach for `synchronized`, a `ReentrantLock`, or an atomic. That is not wrong, exactly - it is just the first tool in the box, not the only one.

Locks answer the question "how do I let many threads touch the same object safely?" Message passing answers a different, often better question: "what if only *one* thread ever touches the object at all?"

Picture the café's rewards wallet. The app, the register, and the kiosk are all **producers** - they generate top-up events. If all three reach directly into the same `balance` field, you need:

- A lock (or several) around every read-modify-write.
- Careful thought about lock ordering so two operations never deadlock each other.
- A prayer that nobody adds a new update path later and forgets to acquire the lock.

Message passing throws that whole checklist away. The producers do not touch `balance` at all. They write a message - "add 500 cents" - onto a queue. One dedicated **owner** thread reads that queue and is the *only* code in the universe allowed to mutate `balance`. No lock needed, because there is no contention: only one thread was ever going to write to it.

<div class="table-wrapper" markdown="1">

| Concept | Queue-based message passing | Lock-heavy shared state |
|---------|------------------------------|-------------------------|
| Who mutates state | One owner thread, always | Any thread holding the lock |
| Coordination mechanism | Messages + a queue | Mutexes, monitors, or atomics |
| Mental model | "Who owns this value?" | "Which lock protects this field?" |
| Typical failure mode | Full/blocked queues, timeouts | Deadlocks, forgotten locks, stale reads |
| Debugging story | Replay the message log | Reconstruct interleavings from stack dumps |

</div>

If this smells familiar to Scala developers, that is no accident. It is the same instinct behind actors, `ZIO` queues, and "don't share mutable state, pass immutable values instead." Java 21 does not need a fancy actor framework to get there - `java.util.concurrent` queues plus a bit of discipline about ownership will do the job.

## Locks: The Old Way (and Why It Gets Ugly Fast)

Let's be honest about what the lock-based version of the café's wallet looks like, because message passing only makes sense once you've felt this pain:

```java
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
```

This *works*. It also does not scale in the way that matters most: **scale of understanding**. Every new feature that touches `balanceInCents` has to remember the lock. Every reviewer has to check that the lock is held everywhere it needs to be. Add a second field that must stay consistent with the first (say, a `lastTopUpTimestamp`) and now you are reasoning about lock granularity, too. It is not that locks are bad - it is that they ask *everyone, forever* to follow the rules correctly.

The [runnable `LockedWallet` example](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/messagepassing/LockedWallet.java) and its [concurrent top-up test](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/interview/preparation/messagepassing/LockedWalletTest.java) live in the repository alongside the message-passing examples, so you can run both approaches side by side.

Message passing flips the responsibility: only the owner thread needs to follow any rules, because it is the only thread doing the mutating.

## Building Block 1: Blocking Handoff for "Must-Process" Work

When every message absolutely must be processed - like a coffee order, which a customer is standing there waiting for - use a blocking queue. Producers `offer` orders, and one kitchen worker thread blocks on `take`/`poll` until there is something to make.

<div class="code-tabs" data-tabs-id="messaging-blocking-handoff-tabs">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">static</span> <span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="nf">handOffOrdersWithBlockingQueue</span><span class="o">(</span><span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">incomingOrders</span><span class="o">)</span> <span class="o">{</span>
    <span class="kd">var</span> <span class="n">orders</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">LinkedBlockingQueue</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;();</span>
    <span class="kd">var</span> <span class="n">prepared</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">ArrayList</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;();</span>
    <span class="kd">var</span> <span class="n">kitchenWorker</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">Thread</span><span class="o">(()</span> <span class="o">-&gt;</span> <span class="o">{</span>
        <span class="k">for</span> <span class="o">(</span><span class="kd">var</span> <span class="n">processed</span> <span class="o">=</span> <span class="mi">0</span><span class="o">;</span> <span class="n">processed</span> <span class="o">&lt;</span> <span class="n">incomingOrders</span><span class="o">.</span><span class="na">size</span><span class="o">();</span> <span class="n">processed</span><span class="o">++)</span> <span class="o">{</span>
            <span class="kd">var</span> <span class="n">order</span> <span class="o">=</span> <span class="nf">takeStringMessage</span><span class="o">(</span><span class="n">orders</span><span class="o">);</span>
            <span class="n">prepared</span><span class="o">.</span><span class="na">add</span><span class="o">(</span><span class="s">"prepared:"</span> <span class="o">+</span> <span class="n">order</span><span class="o">);</span>
        <span class="o">}</span>
    <span class="o">},</span> <span class="s">"kitchen-worker"</span><span class="o">);</span>
    <span class="n">kitchenWorker</span><span class="o">.</span><span class="na">start</span><span class="o">();</span>
    <span class="n">incomingOrders</span><span class="o">.</span><span class="na">forEach</span><span class="o">(</span><span class="n">orders</span><span class="o">::</span><span class="n">offer</span><span class="o">);</span>
    <span class="nf">join</span><span class="o">(</span><span class="n">kitchenWorker</span><span class="o">);</span>
    <span class="k">return</span> <span class="nc">List</span><span class="o">.</span><span class="na">copyOf</span><span class="o">(</span><span class="n">prepared</span><span class="o">);</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/messagepassing/MessagePassingExamples.java">View full Java example</a></p>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">def</span> <span class="n">handOffOrdersWithBlockingQueue</span><span class="o">(</span><span class="n">incomingOrders</span><span class="o">:</span> <span class="kt">List</span><span class="o">[</span><span class="kt">String</span><span class="o">]):</span> <span class="kt">List</span><span class="o">[</span><span class="kt">String</span><span class="o">]</span> <span class="o">=</span>
  <span class="k">val</span> <span class="n">orders</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">LinkedBlockingQueue</span><span class="o">[</span><span class="kt">String</span><span class="o">]()</span>
  <span class="k">val</span> <span class="n">prepared</span> <span class="o">=</span> <span class="n">scala</span><span class="o">.</span><span class="n">collection</span><span class="o">.</span><span class="n">mutable</span><span class="o">.</span><span class="n">ListBuffer</span><span class="o">.</span><span class="n">empty</span><span class="o">[</span><span class="kt">String</span><span class="o">]</span>
  <span class="k">val</span> <span class="n">kitchenWorker</span> <span class="o">=</span> <span class="nc">Thread</span><span class="o">(</span>
    <span class="o">()</span> <span class="o">=&gt;</span>
      <span class="k">for</span> <span class="n">_</span> <span class="o">&lt;-</span> <span class="n">incomingOrders</span><span class="o">.</span><span class="n">indices</span> <span class="k">do</span>
        <span class="k">val</span> <span class="n">order</span> <span class="o">=</span> <span class="n">takeStringMessage</span><span class="o">(</span><span class="n">orders</span><span class="o">)</span>
        <span class="n">prepared</span> <span class="o">+=</span> <span class="s">s"prepared:$order"</span>
    <span class="o">,</span>
    <span class="s">"kitchen-worker"</span>
  <span class="o">)</span>
  <span class="n">kitchenWorker</span><span class="o">.</span><span class="n">start</span><span class="o">()</span>
  <span class="n">incomingOrders</span><span class="o">.</span><span class="n">foreach</span><span class="o">(</span><span class="n">orders</span><span class="o">.</span><span class="n">offer</span><span class="o">)</span>
  <span class="n">join</span><span class="o">(</span><span class="n">kitchenWorker</span><span class="o">)</span>
  <span class="n">prepared</span><span class="o">.</span><span class="n">toList</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/messagepassing/MessagePassingExamples.scala">View full Scala example</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">fun</span> <span class="nf">handOffOrdersWithBlockingQueue</span><span class="p">(</span><span class="n">incomingOrders</span><span class="p">:</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">&gt;):</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">&gt;</span> <span class="p">{</span>
    <span class="k">val</span> <span class="py">orders</span> <span class="p">=</span> <span class="nf">LinkedBlockingQueue</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">&gt;()</span>
    <span class="k">val</span> <span class="py">prepared</span> <span class="p">=</span> <span class="nf">mutableListOf</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">&gt;()</span>
    <span class="k">val</span> <span class="py">kitchenWorker</span> <span class="p">=</span>
        <span class="nc">Thread</span><span class="p">(</span>
            <span class="p">{</span>
                <span class="nf">repeat</span><span class="p">(</span><span class="n">incomingOrders</span><span class="p">.</span><span class="n">size</span><span class="p">)</span> <span class="p">{</span>
                    <span class="k">val</span> <span class="py">order</span> <span class="p">=</span> <span class="nf">takeStringMessage</span><span class="p">(</span><span class="n">orders</span><span class="p">)</span>
                    <span class="n">prepared</span><span class="p">.</span><span class="nf">add</span><span class="p">(</span><span class="s">"prepared:$order"</span><span class="p">)</span>
                <span class="p">}</span>
            <span class="p">},</span>
            <span class="s">"kitchen-worker"</span><span class="p">,</span>
        <span class="p">)</span>
    <span class="n">kitchenWorker</span><span class="p">.</span><span class="nf">start</span><span class="p">()</span>
    <span class="n">incomingOrders</span><span class="p">.</span><span class="nf">forEach</span><span class="p">(</span><span class="n">orders</span><span class="p">::</span><span class="n">offer</span><span class="p">)</span>
    <span class="nf">join</span><span class="p">(</span><span class="n">kitchenWorker</span><span class="p">)</span>
    <span class="k">return</span> <span class="n">prepared</span><span class="p">.</span><span class="nf">toList</span><span class="p">()</span>
<span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/messagepassing/MessagePassingExamples.kt">View full Kotlin example</a></p>
</div>
</div>

Notice what is missing: no `synchronized`, no `Lock`, no `compareAndSet` retry loop. The queue itself is the synchronization point. `LinkedBlockingQueue.take()`/`poll(timeout)` already handles "wait safely until something shows up," so the kitchen worker never busy-spins and never races anyone for the `prepared` list, because it is the only thread touching it.

## Building Block 2: Non-Blocking Drain for "Best Effort" Work

Not everything is as urgent as a hot espresso. Marketing emails ("Come back, we miss you!") can wait. For that kind of work, `ConcurrentLinkedQueue.poll()` is a better fit: it returns `null` immediately instead of blocking, which is perfect for "check the mailbox, send what's there, and move on."

<div class="code-tabs" data-tabs-id="messaging-nonblocking-drain-tabs">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">static</span> <span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="nf">handOffEmailsWithNonBlockingQueue</span><span class="o">(</span><span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">outgoingEmails</span><span class="o">)</span> <span class="o">{</span>
    <span class="kd">var</span> <span class="n">mailbox</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">ConcurrentLinkedQueue</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;();</span>
    <span class="n">outgoingEmails</span><span class="o">.</span><span class="na">forEach</span><span class="o">(</span><span class="n">mailbox</span><span class="o">::</span><span class="n">offer</span><span class="o">);</span>
    <span class="kd">var</span> <span class="n">sent</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">ArrayList</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;();</span>
    <span class="k">while</span> <span class="o">(</span><span class="kc">true</span><span class="o">)</span> <span class="o">{</span>
        <span class="kd">var</span> <span class="n">email</span> <span class="o">=</span> <span class="n">mailbox</span><span class="o">.</span><span class="na">poll</span><span class="o">();</span>
        <span class="k">if</span> <span class="o">(</span><span class="n">email</span> <span class="o">==</span> <span class="kc">null</span><span class="o">)</span> <span class="o">{</span>
            <span class="k">return</span> <span class="nc">List</span><span class="o">.</span><span class="na">copyOf</span><span class="o">(</span><span class="n">sent</span><span class="o">);</span>
        <span class="o">}</span>
        <span class="n">sent</span><span class="o">.</span><span class="na">add</span><span class="o">(</span><span class="s">"sent:"</span> <span class="o">+</span> <span class="n">email</span><span class="o">);</span>
    <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/messagepassing/MessagePassingExamples.java">View full Java example</a></p>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">def</span> <span class="n">handOffEmailsWithNonBlockingQueue</span><span class="o">(</span><span class="n">outgoingEmails</span><span class="o">:</span> <span class="kt">List</span><span class="o">[</span><span class="kt">String</span><span class="o">]):</span> <span class="kt">List</span><span class="o">[</span><span class="kt">String</span><span class="o">]</span> <span class="o">=</span>
  <span class="k">val</span> <span class="n">mailbox</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">ConcurrentLinkedQueue</span><span class="o">[</span><span class="kt">String</span><span class="o">]()</span>
  <span class="n">outgoingEmails</span><span class="o">.</span><span class="n">foreach</span><span class="o">(</span><span class="n">mailbox</span><span class="o">.</span><span class="n">offer</span><span class="o">)</span>
  <span class="k">val</span> <span class="n">sent</span> <span class="o">=</span> <span class="n">scala</span><span class="o">.</span><span class="n">collection</span><span class="o">.</span><span class="n">mutable</span><span class="o">.</span><span class="n">ListBuffer</span><span class="o">.</span><span class="n">empty</span><span class="o">[</span><span class="kt">String</span><span class="o">]</span>
  <span class="k">var</span> <span class="n">next</span> <span class="o">=</span> <span class="n">mailbox</span><span class="o">.</span><span class="n">poll</span><span class="o">()</span>
  <span class="k">while</span> <span class="n">next</span> <span class="o">!=</span> <span class="kc">null</span> <span class="k">do</span>
    <span class="n">sent</span> <span class="o">+=</span> <span class="s">s"sent:$next"</span>
    <span class="n">next</span> <span class="o">=</span> <span class="n">mailbox</span><span class="o">.</span><span class="n">poll</span><span class="o">()</span>
  <span class="n">sent</span><span class="o">.</span><span class="n">toList</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/messagepassing/MessagePassingExamples.scala">View full Scala example</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">fun</span> <span class="nf">handOffEmailsWithNonBlockingQueue</span><span class="p">(</span><span class="n">outgoingEmails</span><span class="p">:</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">&gt;):</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">&gt;</span> <span class="p">{</span>
    <span class="k">val</span> <span class="py">mailbox</span> <span class="p">=</span> <span class="nf">ConcurrentLinkedQueue</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">&gt;()</span>
    <span class="n">outgoingEmails</span><span class="p">.</span><span class="nf">forEach</span><span class="p">(</span><span class="n">mailbox</span><span class="p">::</span><span class="n">offer</span><span class="p">)</span>
    <span class="k">val</span> <span class="py">sent</span> <span class="p">=</span> <span class="nf">mutableListOf</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">&gt;()</span>
    <span class="k">while</span> <span class="p">(</span><span class="kc">true</span><span class="p">)</span> <span class="p">{</span>
        <span class="k">val</span> <span class="py">email</span> <span class="p">=</span> <span class="n">mailbox</span><span class="p">.</span><span class="nf">poll</span><span class="p">()</span> <span class="o">?:</span> <span class="k">return</span> <span class="n">sent</span><span class="p">.</span><span class="nf">toList</span><span class="p">()</span>
        <span class="n">sent</span><span class="p">.</span><span class="nf">add</span><span class="p">(</span><span class="s">"sent:$email"</span><span class="p">)</span>
    <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/messagepassing/MessagePassingExamples.kt">View full Kotlin example</a></p>
</div>
</div>

<div class="table-wrapper" markdown="1">

| Handoff style | Java 21 building block | Best when | Café analogy |
|---------------|------------------------|-----------|--------------|
| Blocking | `LinkedBlockingQueue.take()` / `poll(timeout)` | Consumers should wait for work instead of spinning | The barista waits for the next order ticket |
| Non-blocking | `ConcurrentLinkedQueue.poll()` | Work is optional; "nothing to do" is a normal outcome | Checking the marketing-email tray on a slow afternoon |
| Bounded blocking | `ArrayBlockingQueue` | You need backpressure and explicit capacity | The bar only has room for 20 cups waiting to be picked up |

</div>

## Building Block 3: One Owner, Many Producers - Solving the Race Condition for Real

Now the finale. Remember the loyalty wallet from the lock example? Here is the same problem solved with message passing: the app, the till, and the kiosk are producers that never touch `balance`. They only ever put a top-up amount onto a queue. One `wallet-owner` thread is the *sole* reader of that queue and the *sole* writer of the balance.

<div class="code-tabs" data-tabs-id="messaging-single-owner-tabs">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">var</span> <span class="n">topUpMessages</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">LinkedBlockingQueue</span><span class="o">&lt;</span><span class="nc">Integer</span><span class="o">&gt;();</span>
<span class="kd">var</span> <span class="n">finalBalanceInCents</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">AtomicInteger</span><span class="o">(</span><span class="mi">0</span><span class="o">);</span>
<span class="kd">var</span> <span class="n">walletOwner</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">Thread</span><span class="o">(()</span> <span class="o">-&gt;</span> <span class="o">{</span>
    <span class="kd">var</span> <span class="n">localBalance</span> <span class="o">=</span> <span class="mi">0</span><span class="o">;</span>
    <span class="k">for</span> <span class="o">(</span><span class="kd">var</span> <span class="n">processed</span> <span class="o">=</span> <span class="mi">0</span><span class="o">;</span> <span class="n">processed</span> <span class="o">&lt;</span> <span class="n">expectedTopUps</span><span class="o">;</span> <span class="n">processed</span><span class="o">++)</span> <span class="o">{</span>
        <span class="kd">var</span> <span class="n">message</span> <span class="o">=</span> <span class="nf">takeIntMessage</span><span class="o">(</span><span class="n">topUpMessages</span><span class="o">);</span>
        <span class="n">localBalance</span> <span class="o">+=</span> <span class="n">message</span><span class="o">;</span>
    <span class="o">}</span>
    <span class="n">finalBalanceInCents</span><span class="o">.</span><span class="na">set</span><span class="o">(</span><span class="n">localBalance</span><span class="o">);</span>
<span class="o">},</span> <span class="s">"wallet-owner"</span><span class="o">);</span>
<span class="n">walletOwner</span><span class="o">.</span><span class="na">start</span><span class="o">();</span>
<span class="c1">// Every producer only ever calls put(...) - it never touches localBalance.</span>
<span class="nf">put</span><span class="o">(</span><span class="n">topUpMessages</span><span class="o">,</span> <span class="n">centsPerTopUp</span><span class="o">);</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/messagepassing/MessagePassingExamples.java">View full Java example</a></p>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">val</span> <span class="n">topUpMessages</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">LinkedBlockingQueue</span><span class="o">[</span><span class="kt">Int</span><span class="o">]()</span>
<span class="k">val</span> <span class="n">finalBalanceInCents</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">AtomicInteger</span><span class="o">(</span><span class="mi">0</span><span class="o">)</span>
<span class="k">val</span> <span class="n">walletOwner</span> <span class="o">=</span> <span class="nc">Thread</span><span class="o">(</span>
  <span class="o">()</span> <span class="o">=&gt;</span>
    <span class="k">var</span> <span class="n">localBalance</span> <span class="o">=</span> <span class="mi">0</span>
    <span class="k">for</span> <span class="n">_</span> <span class="o">&lt;-</span> <span class="mi">0</span> <span class="k">until</span> <span class="n">expectedTopUps</span> <span class="k">do</span>
      <span class="k">val</span> <span class="n">message</span> <span class="o">=</span> <span class="n">takeIntMessage</span><span class="o">(</span><span class="n">topUpMessages</span><span class="o">)</span>
      <span class="n">localBalance</span> <span class="o">+=</span> <span class="n">message</span>
    <span class="n">finalBalanceInCents</span><span class="o">.</span><span class="n">set</span><span class="o">(</span><span class="n">localBalance</span><span class="o">)</span>
  <span class="o">,</span>
  <span class="s">"wallet-owner"</span>
<span class="o">)</span>
<span class="n">walletOwner</span><span class="o">.</span><span class="n">start</span><span class="o">()</span>
<span class="c1">// Every producer only ever calls put(...) - it never touches localBalance.</span>
<span class="n">put</span><span class="o">(</span><span class="n">topUpMessages</span><span class="o">,</span> <span class="n">centsPerTopUp</span><span class="o">)</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/messagepassing/MessagePassingExamples.scala">View full Scala example</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">val</span> <span class="py">topUpMessages</span> <span class="p">=</span> <span class="nf">LinkedBlockingQueue</span><span class="p">&lt;</span><span class="nc">Int</span><span class="p">&gt;()</span>
<span class="k">val</span> <span class="py">finalBalanceInCents</span> <span class="p">=</span> <span class="nf">AtomicInteger</span><span class="p">(</span><span class="mi">0</span><span class="p">)</span>
<span class="k">val</span> <span class="py">walletOwner</span> <span class="p">=</span>
    <span class="nc">Thread</span><span class="p">(</span>
        <span class="p">{</span>
            <span class="k">var</span> <span class="py">localBalance</span> <span class="p">=</span> <span class="mi">0</span>
            <span class="nf">repeat</span><span class="p">(</span><span class="n">expectedTopUps</span><span class="p">)</span> <span class="p">{</span>
                <span class="k">val</span> <span class="py">message</span> <span class="p">=</span> <span class="nf">takeIntMessage</span><span class="p">(</span><span class="n">topUpMessages</span><span class="p">)</span>
                <span class="n">localBalance</span> <span class="o">+=</span> <span class="n">message</span>
            <span class="p">}</span>
            <span class="n">finalBalanceInCents</span><span class="p">.</span><span class="nf">set</span><span class="p">(</span><span class="n">localBalance</span><span class="p">)</span>
        <span class="p">},</span>
        <span class="s">"wallet-owner"</span><span class="p">,</span>
    <span class="p">)</span>
<span class="n">walletOwner</span><span class="p">.</span><span class="nf">start</span><span class="p">()</span>
<span class="c1">// Every producer only ever calls put(...) - it never touches localBalance.</span>
<span class="n">put</span><span class="p">(</span><span class="n">topUpMessages</span><span class="p">,</span> <span class="n">centsPerTopUp</span><span class="p">)</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/messagepassing/MessagePassingExamples.kt">View full Kotlin example</a></p>
</div>
</div>

Run this with a dozen producer threads hammering top-ups at once, and the final balance is always correct - not because of a clever lock, but because there is only ever **one** thread doing arithmetic on `localBalance`. Compare that to the `LockedWallet` from earlier: same guarantee, but here nobody has to remember to synchronize anything, because there is nothing left to forget.

## Ownership Beats Locking (Most of the Time)

<div class="table-wrapper" markdown="1">

| Question you're really asking | Lock-based answer | Message-passing answer |
|---|---|---|
| "Who can change this value?" | Whoever grabs the lock first | Only the owner thread, by construction |
| "What happens under heavy contention?" | Threads block on the lock, or spin-retry with CAS | Messages queue up; owner drains them in order |
| "How do I add a new writer?" | Make sure it acquires the same lock | Give it a reference to the queue - done |
| "How do I shut everything down?" | Careful lock release, maybe `finally` blocks everywhere | Send a shutdown message (a "poison pill") through the queue |
| "How do I test it?" | Simulate interleavings, hope you covered the bad ones | Feed in messages, assert on the final state |

</div>

That last row matters more than it looks. A poison pill is just a special message - "no more work is coming" - that tells the owner thread to stop looping and exit cleanly. It is the message-passing equivalent of `Thread.interrupt()`, except it goes through the same queue as everything else, so the owner never has two different shutdown paths to reason about.

## What the Tests Prove

The mirrored tests in Java, Scala, and Kotlin verify:

1. Blocking queue handoff keeps message order for order processing - the third customer's flat white does not jump the queue.
2. Non-blocking queue drain exits cleanly when no messages remain - no infinite spinning waiting for emails that will never come.
3. Single-owner queue processing applies all concurrent top-ups without lost updates, even with many producer threads racing to enqueue at once.

## Best Practices

- Start by asking "who should own this mutable state?" before reaching for a lock.
- Prefer message passing when one component can naturally be the sole owner of a value.
- Use blocking queues for must-process workflows (orders) and non-blocking queues for opportunistic polling (marketing emails).
- Define an explicit shutdown protocol - a poison pill, a close signal, or a completion marker - instead of just killing threads.
- Keep messages immutable. If a message is a mutable object that producers can still modify after sending it, you have smuggled shared mutable state back in through the side door.
- Validate queue capacity and timeout behavior under real production load, not just a happy-path unit test with three messages.

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>When is message passing simpler than shared-state coordination?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Message passing is simpler whenever one logical owner can process updates in sequence. Instead of proving that every single read/write pair across the whole codebase is protected by the correct lock, you route all updates through one queue and let one consumer apply them one at a time. In the café example, the wallet owner thread is the only place balance math happens, so there is no interleaving to worry about - the app, till, and kiosk just drop messages in a box and walk away.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>What is the practical difference between a queue-based design and a lock-heavy design?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      A queue-based design coordinates through ownership and handoff: producers hand off work, and one consumer is responsible for consistency. A lock-heavy design coordinates through mutual exclusion: everybody who touches the shared object must grab the same lock, in the same order, every single time. The queue-based approach fails safely into "the queue fills up" or "a message waits a bit longer," while the lock-based approach can fail into deadlocks or a forgotten lock that silently corrupts data.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>How do ownership boundaries reduce race conditions compared to more locking?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Ownership boundaries remove race conditions by removing the possibility of two writers, not by making the writers take turns nicely. If only one thread is ever allowed to change the wallet balance, there is no scenario where two threads read the same value and both write back a stale update, because there is no second writer to collide with. The queue becomes the one and only synchronization point, and everything downstream of it - the actual mutation - happens on a single thread with no surprises.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>What Java 21 building blocks help with message-passing architectures?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      The practical core lives in <code>java.util.concurrent</code>: blocking queues like <code>LinkedBlockingQueue</code> and <code>ArrayBlockingQueue</code> for producer-consumer handoff, <code>ConcurrentLinkedQueue</code> for non-blocking mailboxes, and <code>CountDownLatch</code> or structured concurrency scopes for lifecycle coordination such as "wait until all producers are done." Pair these with immutable message payloads and one clearly documented owner per queue. That combination gives you a design that maps cleanly onto Scala's actor and isolation-first mental model, while staying completely idiomatic, boring, dependency-free Java 21.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Does message passing mean I should throw away everything I learned about locks and atomics?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      No - think of it as another tool on the same belt, not a replacement for the whole toolbox. Atomics are still the right choice for a single hot counter, and locks are still fine for short, well-understood critical sections. Message passing shines once you have multiple related pieces of state that must stay consistent together, or once several different producers need to update the same thing - that is exactly when "give one thread ownership" starts paying for itself.
    </div>
  </details>
</div>

## Conclusion

For Scala developers learning Java 21, message passing is often the cleanest way to dodge shared-state traps entirely: instead of asking every caller to lock correctly forever, you isolate the mutation behind one owner, hand off work as immutable messages, and let the queue carry the thread-safety burden. Locks and atomics still have their place - just not everywhere, and definitely not in the back room of The Bean Counter Café anymore.

## Code Samples

All examples in this post are runnable. Find them in the repository:
- [Java 21 message passing examples](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/interview/preparation/messagepassing)
- [Scala 3 message passing examples](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/interview/preparation/messagepassing)
- [Kotlin message passing examples](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/messagepassing)
- [Java 21 `LockedWallet` "old way" example](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/messagepassing/LockedWallet.java)
- [Java 21 tests](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/test/java/io/github/sps23/interview/preparation/messagepassing)
- [Scala 3 tests](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/test/scala/io/github/sps23/interview/preparation/messagepassing)
- [Kotlin tests](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/test/kotlin/io/github/sps23/interview/preparation/messagepassing)

---

*This is part of our [Immutability and Concurrency Preparation Guide]({{ site.baseurl }}{% link _posts/2026-09-24-java21-immutability-concurrency-preparation-guide.md %}). Next related posts: [Atomic Operations: Defuse the Race Condition]({{ site.baseurl }}{% link _posts/2026-09-23-atomic-operations-defuse-the-race-condition.md %}) and [Concurrent Collections: One Pot, Many Spoons]({{ site.baseurl }}{% link _posts/2026-09-23-concurrent-collections-one-pot-many-spoons.md %}).*
