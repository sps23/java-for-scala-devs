---
layout: post
title: "Java 21 Visibility: The Case of the Disappearing Update"
description: "Understand Java 21 visibility with volatile, happens-before, and why counter++ still races, with direct JVM comparisons to Scala 3 and Kotlin."
date: 2026-09-23 15:00:00 +0000
updated: 2026-09-23 15:00:00 +0000
categories: [concurrency]
tags: [java, java21, scala, scala3, kotlin, visibility, volatile, java-memory-model, happens-before, concurrency]
---

You flip a shared `running` flag from `true` to `false`, but another thread keeps looping like nothing happened. The update did happen. The mystery is that the reader thread has no synchronization relationship that guarantees it must observe that write.

## The Problem / Context

A visibility bug is not about arithmetic mistakes. It is about one thread writing a value and another thread not being guaranteed to see that write yet.

This matters because JVM compilers and CPUs are allowed to reorder and cache reads/writes when there is no rule forcing cross-thread visibility. So this code has a data race:

```java
boolean running = true;

// writer thread
running = false;

// reader thread
while (running) {
    Thread.onSpinWait();
}
```

Sometimes it exits quickly. Sometimes it does not. We should not promise one specific runtime outcome, because data races are intentionally hard to reproduce reliably.

## Key Concepts

<div class="table-wrapper" markdown="1">

| Concept | Plain-language meaning | Why it matters |
|---------|------------------------|----------------|
| Java Memory Model (JMM) | Rules for what one thread is allowed to see from another | Prevents "it worked on my machine" reasoning for concurrency |
| Happens-before | A guarantee that earlier actions become visible before later actions in another thread | Lets readers trust published writes |
| `volatile` write/read | A volatile write is visible to later volatile reads of the same field | Fixes visibility for flags and published references |
| Atomicity | An operation happens as one indivisible step | Needed for `count++` style updates |

</div>

## The Solution / Implementation

Use a volatile flag so the writer's update is visible to the reader.

<div class="code-tabs" data-tabs-id="visibility-volatile-flag-tabs">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">private</span> <span class="kd">volatile</span> <span class="kt">boolean</span> <span class="n">running</span> <span class="o">=</span> <span class="kc">true</span><span class="o">;</span>

<span class="kd">public</span> <span class="kt">void</span> <span class="nf">stop</span><span class="o">()</span> <span class="o">{</span>
    <span class="n">running</span> <span class="o">=</span> <span class="kc">false</span><span class="o">;</span>
<span class="o">}</span>

<span class="k">while</span> <span class="o">(</span><span class="n">running</span><span class="o">)</span> <span class="o">{</span> <span class="nc">Thread</span><span class="o">.</span><span class="na">onSpinWait</span><span class="o">();</span> <span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@volatile</span> <span class="k">private</span> <span class="k">var</span> <span class="n">running</span> <span class="o">=</span> <span class="kc">true</span>

<span class="k">def</span> <span class="n">stop</span><span class="o">():</span> <span class="kt">Unit</span> <span class="o">=</span>
  <span class="n">running</span> <span class="o">=</span> <span class="kc">false</span>

<span class="k">while</span> <span class="n">running</span> <span class="k">do</span> <span class="nc">Thread</span><span class="o">.</span><span class="n">onSpinWait</span><span class="o">()</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Volatile</span>
<span class="k">private</span> <span class="k">var</span> <span class="py">running</span><span class="p">:</span> <span class="nc">Boolean</span> <span class="p">=</span> <span class="kc">true</span>

<span class="k">fun</span> <span class="nf">stop</span><span class="p">()</span> <span class="p">{</span>
    <span class="n">running</span> <span class="p">=</span> <span class="kc">false</span>
<span class="p">}</span>

<span class="k">while</span> <span class="p">(</span><span class="n">running</span><span class="p">)</span> <span class="p">{</span> <span class="nc">Thread</span><span class="p">.</span><span class="nf">onSpinWait</span><span class="p">()</span> <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

The shared meaning across all three examples is JVM-level: a volatile write to `running` happens-before a subsequent volatile read of `running` in another thread.

## The Second Trick: `volatile` Does Not Fix `count++`

`volatile` gives visibility, not atomicity.

```java
volatile int count = 0;
count++; // read, add, write
```

Another thread can intervene between the read and the write. That is why the visibility sample tests include a deterministic lost-update demonstration.

For the fix, use atomic updates such as `AtomicInteger.incrementAndGet()` or compare-and-set loops. See [Atomic Operations: Defuse the Race Condition]({{ site.baseurl }}{% link _posts/2026-09-23-atomic-operations-defuse-the-race-condition.md %}) for the full walk-through.

Kotlin's JVM concurrency guidance makes the same distinction: visibility (`@Volatile`) is useful, but arithmetic updates still need atomic coordination.

## Bridge from the Immutability Series

Immutability and visibility fit together:

- An immutable object is safer to share across threads after publication.
- But if you replace the reference over time, you still need safe publication for that changing reference.

So the rule is: immutable values reduce mutation risk, and visibility rules make publication reliable. If you missed the first part, start with [Immutability in Java 21: Beyond Records]({{ site.baseurl }}{% link _posts/2026-09-23-immutability-in-java-21.md %}).

## Decision Guide

<div class="table-wrapper" markdown="1">

| Need | Preferred tool | Why |
|------|----------------|-----|
| A simple stop flag or a published reference | `volatile` | Guarantees reader visibility with low ceremony |
| One value that must be updated atomically | Atomic type (`AtomicInteger`, `AtomicReference`, etc.) | The update happens as one indivisible action |
| Several values that must change together | Lock (`synchronized`/`ReentrantLock`) or one immutable snapshot + CAS | Preserves cross-field consistency |

</div>

## Visibility in Practice:

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>What does "happens-before" mean in practical terms?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      It means a reader thread is allowed to rely on seeing a writer thread's earlier action. Without that rule, a value may have changed in memory but still look old to another thread. With a volatile write/read pair, your code gets a concrete visibility guarantee instead of wishful thinking.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Why can <code>count++</code> still lose updates when <code>count</code> is volatile?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Because increment is three steps: read, compute, write. Volatile makes each read/write visible, but it does not merge those steps into one atomic operation. Two threads can still read the same old value and both write back the same next value.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>When should I choose an atomic type over <code>volatile</code>?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Choose an atomic type when you need to change a value based on its current value safely, like incrementing counters or swapping references with compare-and-set. Use volatile when you only need one thread's write to become visible to another thread.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>How does immutability help with visibility?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Immutability means readers cannot corrupt the shared object after they receive it, which removes a whole category of bugs. You still need correct publication of the reference itself, though, so readers reliably see the latest object when that reference changes.
    </div>
  </details>
</div>

## Conclusion

For Scala developers moving to Java 21, visibility is the missing rule behind many "ghost" concurrency bugs: your write exists, but another thread is not guaranteed to see it yet. Use `volatile` for visibility, atomic types for read-modify-write, and locks or immutable snapshots when several values must move together.

## Code Samples

All examples in this post are runnable. Find them in the repository:
- [Java 21 VisibilityExamples](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/visibility/VisibilityExamples.java)
- [Scala 3 VisibilityExamples](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/visibility/VisibilityExamples.scala)
- [Kotlin VisibilityExamples](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/visibility/VisibilityExamples.kt)
- [Java 21 tests](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/interview/preparation/visibility/VisibilityExamplesTest.java)
- [Scala 3 tests](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/interview/preparation/visibility/VisibilityExamplesTest.scala)
- [Kotlin tests](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/interview/preparation/visibility/VisibilityExamplesTest.kt)

---

*This is part of our [Java 21 Interview Preparation Guide - Your Roadmap to Success]({{ site.baseurl }}{% link _posts/2025-11-25-java21-interview-preparation-plan.md %}). Next related posts: [Immutability in Java 21: Beyond Records]({{ site.baseurl }}{% link _posts/2026-09-23-immutability-in-java-21.md %}) and [Atomic Operations: Defuse the Race Condition]({{ site.baseurl }}{% link _posts/2026-09-23-atomic-operations-defuse-the-race-condition.md %}).*
