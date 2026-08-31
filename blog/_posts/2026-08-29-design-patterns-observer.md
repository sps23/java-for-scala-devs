---
layout: post
title: "Observer Pattern: Reacting to Changes"
description: "Learn the Observer pattern in Java 21, Scala 2, Scala 3, and Kotlin with live market updates, subscription lifecycles, and practical guidance for avoiding memory leaks."
date: 2026-08-29 07:30:00 +0000
updated: 2026-08-29 17:35:00 +0000
categories: [interview, best-practices]
tags: [java, java21, scala, scala2, scala3, kotlin, design-patterns, behavioral-patterns, observer-pattern]
---

Imagine your trading dashboard needs to refresh when prices change, your alert service wants a push notification, and the analytics worker wants a timestamped snapshot. You could make the price source aware of all of them individually, but then every new consumer becomes a special case. The Observer pattern gives you one clean rule: when the subject changes, it tells everyone who subscribed.

## The Problem: One source, many listeners

A stock ticker is a textbook Observer problem. The source state changes often, while the consumers want to react only when something relevant happens. A brittle implementation would do something like this:

- `MarketTicker` knows about `Dashboard`, `AlertService`, and `AnalyticsReporter`
- every new consumer requires a new branch in the price-update logic
- if a consumer forgets to unsubscribe, you get stale listeners and memory leaks

This is exactly the sort of coupling that makes code hard to test and harder to extend.

## Key Concepts

<div class="table-wrapper" markdown="1">

| Concept | In this example | Why it matters |
|---------|-----------------|----------------|
| Subject | `MarketTicker` | Owns the state and publishes updates |
| Observer | `PriceObserver` | Reacts to updates without the source knowing details |
| Event payload | `PriceUpdate` | Carries the changed data in one immutable object |
| Lifecycle | `subscribe()` / `unsubscribe()` | Keeps the system clean and avoids stale listeners |

</div>

## Real Use Case: Live Price Alerts

Think about a trading app with three subscribers:

1. a dashboard that redraws the chart
2. a notifications service that sends mobile alerts
3. a risk engine that recalculates exposure

The ticker does not need to know which type of consumer each observer is. It just emits a new `PriceUpdate` and lets the listeners decide what to do with it.

## The Design: Subject + Observer + Event

The important part is the contract between the subject and the observers:

- the subject keeps a list of subscribers
- each subscriber receives the same update object
- the update is immutable, so consumers can react without racing the source

## The Solution: Observer Across JVM Languages

This example uses a `MarketTicker` that stores the latest value and notifies all registered observers when a new price arrives.

<div class="code-tabs" data-tabs-id="observer-impl">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">final</span> <span class="kd">class</span> <span class="nc">MarketTicker</span> <span class="o">{</span>
    <span class="kd">public</span> <span class="n">record</span> <span class="nc">PriceUpdate</span><span class="o">(</span><span class="nc">String</span> <span class="n">symbol</span><span class="o">,</span> <span class="nc">BigDecimal</span> <span class="n">price</span><span class="o">,</span> <span class="nc">Instant</span> <span class="n">timestamp</span><span class="o">)</span> <span class="o">{}</span>

    <span class="nd">@FunctionalInterface</span>
    <span class="kd">public</span> <span class="kd">interface</span> <span class="nc">PriceObserver</span> <span class="o">{</span>
        <span class="kt">void</span> <span class="nf">onPriceUpdate</span><span class="o">(</span><span class="nc">PriceUpdate</span> <span class="n">update</span><span class="o">);</span>
    <span class="o">}</span>

    <span class="kd">private</span> <span class="kd">final</span> <span class="nc">Set</span><span class="o">&lt;</span><span class="nc">PriceObserver</span><span class="o">&gt;</span> <span class="n">observers</span> <span class="o">=</span> <span class="nc">ConcurrentHashMap</span><span class="o">.</span><span class="na">newKeySet</span><span class="o">();</span>

    <span class="kd">public</span> <span class="kt">void</span> <span class="nf">subscribe</span><span class="o">(</span><span class="nc">PriceObserver</span> <span class="n">observer</span><span class="o">)</span> <span class="o">{</span>
        <span class="n">observers</span><span class="o">.</span><span class="na">add</span><span class="o">(</span><span class="n">observer</span><span class="o">);</span>
    <span class="o">}</span>

    <span class="kd">public</span> <span class="nc">PriceUpdate</span> <span class="nf">publishPrice</span><span class="o">(</span><span class="nc">String</span> <span class="n">symbol</span><span class="o">,</span> <span class="nc">BigDecimal</span> <span class="n">price</span><span class="o">)</span> <span class="o">{</span>
        <span class="nc">PriceUpdate</span> <span class="n">update</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">PriceUpdate</span><span class="o">(</span><span class="n">symbol</span><span class="o">,</span> <span class="n">price</span><span class="o">,</span> <span class="nc">Instant</span><span class="o">.</span><span class="na">now</span><span class="o">());</span>
        <span class="n">observers</span><span class="o">.</span><span class="na">forEach</span><span class="o">(</span><span class="n">observer</span> <span class="o">-&gt;</span> <span class="n">observer</span><span class="o">.</span><span class="na">onPriceUpdate</span><span class="o">(</span><span class="n">update</span><span class="o">));</span>
        <span class="k">return</span> <span class="n">update</span><span class="o">;</span>
    <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">class</span> <span class="nc">MarketTicker</span> <span class="p">{</span>
    <span class="k">data</span> <span class="k">class</span> <span class="nc">PriceUpdate</span><span class="p">(</span>
        <span class="k">val</span> <span class="py">symbol</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span>
        <span class="k">val</span> <span class="py">price</span><span class="p">:</span> <span class="nc">BigDecimal</span><span class="p">,</span>
        <span class="k">val</span> <span class="py">timestamp</span><span class="p">:</span> <span class="nc">Instant</span>
    <span class="p">)</span>

    <span class="k">fun</span> <span class="nf">interface</span> <span class="nc">PriceObserver</span> <span class="p">{</span>
        <span class="k">fun</span> <span class="nf">onPriceUpdate</span><span class="p">(</span><span class="n">update</span><span class="p">:</span> <span class="nc">PriceUpdate</span><span class="p">)</span>
    <span class="p">}</span>

    <span class="k">private</span> <span class="k">val</span> <span class="py">observers</span> <span class="p">=</span> <span class="nf">mutableSetOf</span><span class="p">&lt;</span><span class="nc">PriceObserver</span><span class="p">&gt;()</span>

    <span class="k">fun</span> <span class="nf">subscribe</span><span class="p">(</span><span class="n">observer</span><span class="p">:</span> <span class="nc">PriceObserver</span><span class="p">)</span> <span class="p">{</span>
        <span class="n">observers</span> <span class="p">+=</span> <span class="n">observer</span>
    <span class="p">}</span>

    <span class="k">fun</span> <span class="nf">publishPrice</span><span class="p">(</span><span class="n">symbol</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span> <span class="n">price</span><span class="p">:</span> <span class="nc">BigDecimal</span><span class="p">):</span> <span class="nc">PriceUpdate</span> <span class="p">{</span>
        <span class="k">val</span> <span class="py">update</span> <span class="p">=</span> <span class="nc">PriceUpdate</span><span class="p">(</span><span class="n">symbol</span><span class="p">,</span> <span class="n">price</span><span class="p">,</span> <span class="nc">Instant</span><span class="p">.</span><span class="n">now</span><span class="p">())</span>
        <span class="n">observers</span><span class="p">.</span><span class="nf">toList</span><span class="p">().</span><span class="nf">forEach</span> <span class="p">{</span> <span class="n">it</span><span class="p">.</span><span class="nf">onPriceUpdate</span><span class="p">(</span><span class="n">update</span><span class="p">)</span> <span class="p">}</span>
        <span class="k">return</span> <span class="n">update</span>
    <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">class</span> <span class="nc">MarketTicker</span> <span class="o">{</span>
  <span class="k">case</span> <span class="k">class</span> <span class="nc">PriceUpdate</span><span class="o">(</span><span class="n">symbol</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">price</span><span class="k">:</span> <span class="kt">BigDecimal</span><span class="o">,</span> <span class="n">timestamp</span><span class="k">:</span> <span class="kt">Instant</span><span class="o">)</span>

  <span class="k">trait</span> <span class="nc">PriceObserver</span> <span class="o">{</span>
    <span class="k">def</span> <span class="nf">onPriceUpdate</span><span class="o">(</span><span class="n">update</span><span class="k">:</span> <span class="kt">PriceUpdate</span><span class="o">)</span><span class="k">:</span> <span class="kt">Unit</span>
  <span class="o">}</span>

  <span class="k">private</span> <span class="k">val</span> <span class="nv">observers</span> <span class="k">=</span> <span class="n">mutable</span><span class="o">.</span><span class="py">Set</span><span class="o">.</span><span class="py">empty</span><span class="o">[</span><span class="nc">PriceObserver</span><span class="o">]</span>

  <span class="k">def</span> <span class="nf">subscribe</span><span class="o">(</span><span class="n">observer</span><span class="k">:</span> <span class="kt">PriceObserver</span><span class="o">)</span><span class="k">:</span> <span class="kt">Unit</span> <span class="o">=</span> <span class="n">observers</span> <span class="o">+=</span> <span class="n">observer</span>

  <span class="k">def</span> <span class="nf">publishPrice</span><span class="o">(</span><span class="n">symbol</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">price</span><span class="k">:</span> <span class="kt">BigDecimal</span><span class="o">)</span><span class="k">:</span> <span class="kt">PriceUpdate</span> <span class="o">=</span> <span class="o">{</span>
    <span class="k">val</span> <span class="nv">update</span> <span class="k">=</span> <span class="nc">PriceUpdate</span><span class="o">(</span><span class="n">symbol</span><span class="o">,</span> <span class="n">price</span><span class="o">,</span> <span class="nc">Instant</span><span class="o">.</span><span class="py">now</span><span class="o">())</span>
    <span class="n">observers</span><span class="o">.</span><span class="py">foreach</span><span class="o">(</span><span class="k">_</span><span class="o">.</span><span class="py">onPriceUpdate</span><span class="o">(</span><span class="n">update</span><span class="o">))</span>
    <span class="n">update</span>
  <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">class</span> <span class="nc">MarketTicker</span><span class="k">:</span>
  <span class="k">case</span> <span class="k">class</span> <span class="nc">PriceUpdate</span><span class="o">(</span><span class="n">symbol</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">price</span><span class="k">:</span> <span class="kt">BigDecimal</span><span class="o">,</span> <span class="n">timestamp</span><span class="k">:</span> <span class="kt">Instant</span><span class="o">)</span>

  <span class="k">trait</span> <span class="nc">PriceObserver</span><span class="k">:</span>
    <span class="k">def</span> <span class="nf">onPriceUpdate</span><span class="o">(</span><span class="n">update</span><span class="k">:</span> <span class="kt">PriceUpdate</span><span class="o">)</span><span class="k">:</span> <span class="kt">Unit</span>

  <span class="k">private</span> <span class="k">val</span> <span class="nv">observers</span> <span class="k">=</span> <span class="n">mutable</span><span class="o">.</span><span class="py">Set</span><span class="o">.</span><span class="py">empty</span><span class="o">[</span><span class="nc">PriceObserver</span><span class="o">]</span>

  <span class="k">def</span> <span class="nf">subscribe</span><span class="o">(</span><span class="n">observer</span><span class="k">:</span> <span class="kt">PriceObserver</span><span class="o">)</span><span class="k">:</span> <span class="kt">Unit</span> <span class="o">=</span> <span class="n">observers</span> <span class="o">+=</span> <span class="n">observer</span>

  <span class="k">def</span> <span class="nf">publishPrice</span><span class="o">(</span><span class="n">symbol</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">price</span><span class="k">:</span> <span class="kt">BigDecimal</span><span class="o">)</span><span class="k">:</span> <span class="kt">PriceUpdate</span> <span class="o">=</span>
    <span class="k">val</span> <span class="nv">update</span> <span class="k">=</span> <span class="nc">PriceUpdate</span><span class="o">(</span><span class="n">symbol</span><span class="o">,</span> <span class="n">price</span><span class="o">,</span> <span class="nc">Instant</span><span class="o">.</span><span class="py">now</span><span class="o">())</span>
    <span class="n">observers</span><span class="o">.</span><span class="py">foreach</span><span class="o">(</span><span class="k">_</span><span class="o">.</span><span class="py">onPriceUpdate</span><span class="o">(</span><span class="n">update</span><span class="o">))</span>
    <span class="n">update</span>
</code></pre></div></div>
</div>
</div>

## When Observers Cause Trouble

The pattern is powerful, but it has a few common traps:

- forgetting to unsubscribe leads to stale listeners and memory leaks
- a noisy subject can trigger expensive work for every subscriber
- invoking observers while holding internal locks can create deadlocks
- callbacks can accidentally re-enter the subject and create recursive update loops

This is why real systems often combine `Observer` with explicit lifecycle rules, batching, or back-pressure. The pattern itself is not the problem; the careless execution is.

## Comparison Table

<div class="table-wrapper" markdown="1">

| Concern | Java 21 | Scala 2/3 | Kotlin |
|---------|----------|-----------|--------|
| Subject contract | interface + set of listeners | trait + mutable set | fun interface + mutable set |
| Event payload | record | case class | data class |
| Subscription lifecycle | `subscribe()` / `unsubscribe()` | same | same |
| Typical use | Swing, listeners, event buses | reactive streams, domain events | Flow, coroutine-based streams |
| Main risk | stale callbacks | mutable state + side-effects | leaking coroutine scopes |

</div>

## Best Practices

- Keep the observer callback small and side-effect free when possible.
- Prefer immutable payloads so consumers can safely read a snapshot.
- Implement explicit unsubscribe paths for long-lived objects.
- Consider weak references or managed lifecycles when the subject outlives the observers.
- Be careful with notification storms; if many listeners exist, batch updates or debounce them.

## Interview Q&A: Observer Pattern in Practice

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>What is the Observer pattern really doing?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      It creates a one-to-many relationship between a subject and its dependents. When the subject changes, it notifies all registered observers without the subject needing to know the exact type of each consumer. That makes the producer and consumers loosely coupled, which is usually a good thing.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>How is the Observer pattern different from callbacks?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      A callback is usually a single function passed to another function for one-time handling. The Observer pattern is a broader mechanism: many listeners can subscribe, unsubscribe, and receive future events from the same subject. In that sense, callbacks are a building block, while Observer is a reusable event-listener architecture.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>What are the common pitfalls with observers?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      The classic problems are stale listeners, memory leaks, and notification storms. If a consumer forgets to unsubscribe, the subject may keep a reference to it long after it should be gone. If too many observers react to every update, performance can degrade quickly. That is why lifecycle management and batching matter in production systems.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>How do modern reactive frameworks relate to the Observer pattern?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Frameworks such as RxJava, Reactor, Kotlin Flow, and ZIO build on the same core idea: a source emits values and downstream consumers react to them. They add stronger typing, back-pressure, composition, and scheduling. In other words, modern reactive systems are more sophisticated observers, not a completely different concept.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>When should I choose Observer over a simple method call?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Use Observer when the relationship is inherently one-to-many and the publisher should not need to know every consumer. If one component just needs to ask another component to do a single job, a direct method call is simpler. If many components need to react to the same event over time, Observer is usually the cleaner design.
    </div>
  </details>
</div>

## Conclusion

The Observer pattern is one of the clearest examples of a one-to-many dependency in real software. If you are coming from Scala, think of it as a publisher that pushes immutable events to registered listeners. If you are coming from Java, think of it as a well-defined event contract that avoids direct coupling between the producer and each consumer.

The real win is not just the callback list. It is the separation of concerns: the subject knows how to publish state changes, while the observers decide what to do with them.

## Code Samples

All examples in this post are runnable. Find them in the repository:

- [Java 21 implementation](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/designpatterns/observer/MarketTicker.java)
- [Java 21 tests](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/designpatterns/observer/MarketTickerTest.java)
- [Kotlin implementation](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/designpatterns/observer/MarketTicker.kt)
- [Kotlin tests](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/designpatterns/observer/MarketTickerTest.kt)
- [Scala 2 implementation](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/main/scala/io/github/sps23/designpatterns/observer/MarketTicker.scala)
- [Scala 2 tests](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/test/scala/io/github/sps23/designpatterns/observer/MarketTickerTest.scala)
- [Scala 3 implementation](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/designpatterns/observer/MarketTicker.scala)
- [Scala 3 tests](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/designpatterns/observer/MarketTickerTest.scala)

---

*This post is part of the [Design Patterns in JVM Languages - Your Guide to the Top 10]({{ site.baseurl }}{% link _posts/2026-07-26-design-patterns-guide-jvm.md %}). Nearby related posts from the same guide: [Strategy Pattern: Choosing Algorithms at Runtime]({{ site.baseurl }}{% link _posts/2026-08-29-design-patterns-strategy.md %}) and [Facade Pattern: Simplifying Complex Systems]({{ site.baseurl }}{% link _posts/2026-08-28-design-patterns-facade.md %}).*
