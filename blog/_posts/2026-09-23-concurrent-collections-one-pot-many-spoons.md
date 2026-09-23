---
layout: post
title: "Concurrent Collections: One Pot, Many Spoons."
description: "Learn how Java 21 concurrent collections prevent shared-state chaos, when to use ConcurrentHashMap or BlockingQueue, and how the same ideas map to Scala and Kotlin."
date: 2026-09-23 15:00:00 +0000
updated: 2026-09-23 15:00:00 +0000
categories: [concurrency]
tags: [java, java21, scala, scala3, kotlin, concurrent-collections, concurrenthashmap, blockingqueue, trieMap, mutex]
---

Two threads walk into one kitchen, both grab the same spoon counter from a shared `HashMap`, both add one, and both put it back. Congratulations: your soup now has Schrödinger's cutlery and the final count is wrong.

## The Problem / Context

For Scala developers, this is the familiar shared-mutable-state trap: one variable, multiple threads, and confidence that disappears faster than free pizza in the office kitchen.

A deterministic race from this repository:

```java
HashMap<String, Integer> sharedPot = new HashMap<>();
sharedPot.put("spoons", 0);

// Both threads read before either thread writes.
// Final value becomes 1, not 2.
```

`HashMap` is not thread-safe, but the deeper lesson is bigger: even with thread-safe structures, two individually safe calls can still form one unsafe business operation if you split the read and write across separate steps.

## What "Thread-Safe" Actually Guarantees

A thread-safe collection guarantees its own operations are safe under concurrency. It does **not** automatically guarantee your whole sequence is atomic.

<div class="table-wrapper" markdown="1">

| Situation | Safe? | Why |
|-----------|-------|-----|
| `queue.offer(x)` on `ConcurrentLinkedQueue` | Yes | One operation is internally synchronized/atomic |
| `map.get(k)` then `map.put(k, v + 1)` | Not as a sequence | Another thread can change `k` between calls |
| `map.merge(k, 1, Integer::sum)` | Yes for that key update | Read-modify-write is one atomic map operation |

</div>

If your logic is "check current value, compute next, then update," prefer a single atomic API such as `merge` or `compute`.

## Java 21 Collection Choices by Use Case

<div class="table-wrapper" markdown="1">

| Collection | Use it when | Trade-off |
|------------|-------------|-----------|
| `ConcurrentHashMap` | Many threads read/update shared key-value state | Per-key atomic helpers are great, but cross-key transactions still need extra coordination |
| `ConcurrentLinkedQueue` | Non-blocking producer/consumer handoff | Great throughput, but consumers must handle empty polls |
| `BlockingQueue` (`LinkedBlockingQueue`) | Producers/consumers should wait instead of spin | Simpler coordination, but blocking semantics affect throughput and cancellation behavior |
| `CopyOnWriteArrayList` | Reads massively outnumber writes | Iteration is stable and lock-free, writes copy the full backing array |

</div>

## The Solution / Implementation

The repository includes a mirrored `ConcurrentCollectionsExamples` implementation in Java 21, Scala 3, and Kotlin.

<div class="code-tabs" data-tabs-id="concurrent-map-atomic-update-tabs"><div class="tab-buttons"><button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button><button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button><button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button></div><div class="tab-content active" data-tab="java"><div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">var</span> <span class="n">sharedPot</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">ConcurrentHashMap</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">,</span> <span class="nc">Integer</span><span class="o">&gt;();</span>
<span class="n">sharedPot</span><span class="o">.</span><span class="na">put</span><span class="o">(</span><span class="s">"spoons"</span><span class="o">,</span> <span class="mi">0</span><span class="o">);</span>
<span class="k">var</span> <span class="n">first</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">Thread</span><span class="o">(()</span> <span class="o">-&gt;</span> <span class="n">sharedPot</span><span class="o">.</span><span class="na">merge</span><span class="o">(</span><span class="s">"spoons"</span><span class="o">,</span> <span class="mi">1</span><span class="o">,</span> <span class="nc">Integer</span><span class="o">::</span><span class="n">sum</span><span class="o">));</span>
<span class="k">var</span> <span class="n">second</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">Thread</span><span class="o">(()</span> <span class="o">-&gt;</span> <span class="n">sharedPot</span><span class="o">.</span><span class="na">merge</span><span class="o">(</span><span class="s">"spoons"</span><span class="o">,</span> <span class="mi">1</span><span class="o">,</span> <span class="nc">Integer</span><span class="o">::</span><span class="n">sum</span><span class="o">));</span>
<span class="n">first</span><span class="o">.</span><span class="na">start</span><span class="o">();</span>
<span class="n">second</span><span class="o">.</span><span class="na">start</span><span class="o">();</span>
</code></pre></div></div></div><div class="tab-content" data-tab="scala"><div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">val</span> <span class="n">sharedPot</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">ConcurrentHashMap</span><span class="o">[</span><span class="kt">String</span><span class="o">,</span> <span class="kt">Int</span><span class="o">]()</span>
<span class="n">sharedPot</span><span class="o">.</span><span class="n">put</span><span class="o">(</span><span class="s">"spoons"</span><span class="o">,</span> <span class="mi">0</span><span class="o">)</span>
<span class="k">val</span> <span class="n">first</span> <span class="o">=</span> <span class="nc">Thread</span><span class="o">(()</span> <span class="o">=&gt;</span> <span class="n">sharedPot</span><span class="o">.</span><span class="n">merge</span><span class="o">(</span><span class="s">"spoons"</span><span class="o">,</span> <span class="mi">1</span><span class="o">,</span> <span class="nc">Integer</span><span class="o">.</span><span class="n">sum</span><span class="o">))</span>
<span class="k">val</span> <span class="n">second</span> <span class="o">=</span> <span class="nc">Thread</span><span class="o">(()</span> <span class="o">=&gt;</span> <span class="n">sharedPot</span><span class="o">.</span><span class="n">merge</span><span class="o">(</span><span class="s">"spoons"</span><span class="o">,</span> <span class="mi">1</span><span class="o">,</span> <span class="nc">Integer</span><span class="o">.</span><span class="n">sum</span><span class="o">))</span>
<span class="n">first</span><span class="o">.</span><span class="n">start</span><span class="o">()</span>
<span class="n">second</span><span class="o">.</span><span class="n">start</span><span class="o">()</span>
</code></pre></div></div></div><div class="tab-content" data-tab="kotlin"><div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">val</span> <span class="py">sharedPot</span> <span class="p">=</span> <span class="nc">ConcurrentHashMap</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">,</span> <span class="nc">Int</span><span class="p">&gt;()</span>
<span class="n">sharedPot</span><span class="p">[</span><span class="s">"spoons"</span><span class="p">]</span> <span class="p">=</span> <span class="mi">0</span>
<span class="k">val</span> <span class="py">first</span> <span class="p">=</span> <span class="nc">Thread</span> <span class="p">{</span> <span class="n">sharedPot</span><span class="p">.</span><span class="nf">merge</span><span class="p">(</span><span class="s">"spoons"</span><span class="p">,</span> <span class="mi">1</span><span class="p">,</span> <span class="nc">Int</span><span class="o">::</span><span class="n">plus</span><span class="p">)</span> <span class="p">}</span>
<span class="k">val</span> <span class="py">second</span> <span class="p">=</span> <span class="nc">Thread</span> <span class="p">{</span> <span class="n">sharedPot</span><span class="p">.</span><span class="nf">merge</span><span class="p">(</span><span class="s">"spoons"</span><span class="p">,</span> <span class="mi">1</span><span class="p">,</span> <span class="nc">Int</span><span class="o">::</span><span class="n">plus</span><span class="p">)</span> <span class="p">}</span>
<span class="n">first</span><span class="p">.</span><span class="nf">start</span><span class="p">()</span>
<span class="n">second</span><span class="p">.</span><span class="nf">start</span><span class="p">()</span>
</code></pre></div></div></div></div>

`merge` performs the read-modify-write as one map operation. No split-brain spoon math.

### Queue and List Options in the Same Demo

The same file also includes:

- `drainOrdersWithConcurrentLinkedQueue()` for non-blocking FIFO draining.
- `drainOrdersWithBlockingQueue()` for consumer-waits-for-work flow.
- `copyOnWriteWaitersSnapshot()` to show snapshot iteration (`Ana`, `Ben`) while writes (`Cara`) happen concurrently.

Those tests make the semantics explicit instead of hand-wavy interview claims.

## Scala and Kotlin Comparison: Immutable vs Concurrent Mutable Sharing

The most useful distinction:

- **Immutable collection**: safe to share as a fixed value.
- **Concurrent collection**: safe to share while mutating from multiple threads.

### Scala

- Prefer immutable `Map`, `List`, `Vector` for stable snapshots and value-oriented design.
- Use `scala.collection.concurrent.TrieMap` when many threads must update shared keys.
- For mutable shared maps on the JVM, Java's `ConcurrentHashMap` is still first-class from Scala.

### Kotlin

- Kotlin `List` means read-only **view**, not automatically immutable backing storage.
- For real shared mutation, use JVM concurrent collections (`ConcurrentHashMap`, `BlockingQueue`, etc.).
- Coroutines add `Mutex` for critical sections when your update spans multiple values and cannot be expressed as one collection operation.

## Limits: Concurrent Collections Are Not Transactions

Even with thread-safe collections, multi-step workflows can still fail consistency checks:

1. Remove item from inventory map.
2. Add row to shipping queue.
3. Publish analytics event.

If step 2 fails, you now have half-applied business state. Concurrent collections solve safe shared data structure access; they do not provide ACID transactions across several resources. For those workflows, use explicit orchestration, retries, and compensating actions.

## Best Practices

- Keep shared mutable state small and explicit; if you can model it as immutable snapshots, do that first.
- Prefer atomic collection APIs (`merge`, `compute`, `putIfAbsent`) over manual `get` + `put` sequences.
- Choose queue style intentionally: non-blocking polling (`ConcurrentLinkedQueue`) vs back-pressure waiting (`BlockingQueue`).
- Use `CopyOnWriteArrayList` only when writes are rare and reader stability matters more than write cost.
- When one operation spans multiple structures, coordinate with a higher-level lock or transaction strategy.

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>Why is <code>map.get(k); map.put(k, v + 1)</code> unsafe even on a concurrent map?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Because that is two operations, not one atomic operation. Another thread can update the same key after your <code>get</code> but before your <code>put</code>, so your write overwrites newer data. Use <code>merge</code> or <code>compute</code> to keep the read-modify-write in one map call.
    </div>
  </details>
  <details class="faq-item" open>
    <summary>
      <span>When should I choose <code>BlockingQueue</code> over <code>ConcurrentLinkedQueue</code>?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Choose <code>BlockingQueue</code> when consumers should wait for work instead of spinning or sleeping. It simplifies producer/consumer pipelines because <code>take()</code> naturally blocks until data arrives. Use <code>ConcurrentLinkedQueue</code> when you want non-blocking behavior and can handle empty polls explicitly.
    </div>
  </details>
  <details class="faq-item" open>
    <summary>
      <span>What does Kotlin's read-only <code>List</code> guarantee?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      It guarantees that this reference cannot call mutating list methods. It does not guarantee that another reference cannot mutate the same backing collection. So read-only is an API contract, not a deep immutability guarantee.
    </div>
  </details>
  <details class="faq-item" open>
    <summary>
      <span>When is Scala immutable data enough, and when do I still need concurrent structures?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Immutable values are enough when state is replaced as a whole and then shared as a snapshot. You still need concurrent structures when many threads must mutate shared state in place over time, such as counters, work queues, or shared caches.
    </div>
  </details>
</div>

## Conclusion

Concurrent collections are like a well-organized kitchen: many people can work at once without stabbing each other with forks, but you still need a recipe for multi-step business workflows. For Scala developers moving into Java 21, keep the mental model crisp: immutable values are perfect for stable snapshots; concurrent collections are for shared, changing state; and atomic APIs are your first defense against race-condition soup.

## Code Samples

All examples in this post are runnable:

- [Java 21 concurrent collections example](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/interview/preparation/concurrentcollections)
- [Scala 3 concurrent collections example](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/interview/preparation/concurrentcollections)
- [Kotlin concurrent collections example](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/concurrentcollections)
- [Java 21 tests](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/test/java/io/github/sps23/interview/preparation/concurrentcollections)
- [Scala 3 tests](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/test/scala/io/github/sps23/interview/preparation/concurrentcollections)
- [Kotlin tests](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/test/kotlin/io/github/sps23/interview/preparation/concurrentcollections)

---

*This is part of our Java 21 Interview Preparation series. Start with [Java 21 Interview Preparation Guide - Your Roadmap to Success]({{ site.baseurl }}{% link _posts/2025-11-25-java21-interview-preparation-plan.md %}).
Next related posts: [Collection Factory Methods and Stream Basics]({{ site.baseurl }}{% link _posts/2025-11-29-collection-factory-methods-and-stream-basics.md %}),
[CompletableFuture and Asynchronous Programming]({{ site.baseurl }}{% link _posts/2025-11-29-completablefuture-and-asynchronous-programming.md %}),
and [Virtual Threads and Structured Concurrency]({{ site.baseurl }}{% link _posts/2025-11-29-virtual-threads-and-structured-concurrency.md %}).*
