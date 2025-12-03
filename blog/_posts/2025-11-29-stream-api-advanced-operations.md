---
layout: post
title: "Stream API Advanced Operations"
description: "Advanced Java Stream API operations - learn takeWhile, dropWhile, custom collectors, parallel streams, and data analysis patterns with Scala and Kotlin comparisons."
date: 2025-11-29 16:00:00 +0000
categories: [interview]
tags: [java, java21, scala, kotlin, streams, collectors, parallel, interview-preparation]
---

This is Part 4 of our Java 21 Interview Preparation series. We'll explore advanced Stream operations and collectors, comparing Java 21, Scala 3, and Kotlin approaches.

## The Problem: Analyzing Orders

A common programming task involves analyzing datasets: calculating running totals, finding top N items by category, and generating summary reports. Let's see how this is handled across our three JVM languages.

**Problem Statement:** Analyze a dataset of orders: calculate running totals, find top N by category, and generate a summary report.

## Order Data Model

Let's start with our Order model in all three languages:

<div class="code-tabs" data-tabs-id="tabs-1">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="n">record</span> <span class="nc">Order</span><span class="o">(</span>
    <span class="kt">long</span> <span class="n">id</span><span class="o">,</span>
    <span class="nc">String</span> <span class="n">category</span><span class="o">,</span>
    <span class="kt">double</span> <span class="n">amount</span><span class="o">,</span>
    <span class="nc">String</span> <span class="n">customer</span><span class="o">,</span>
    <span class="nc">LocalDate</span> <span class="n">date</span><span class="o">,</span>
    <span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">items</span>
<span class="o">)</span> <span class="o">{</span>
    <span class="kd">public</span> <span class="nf">Order</span> <span class="o">{</span>
        <span class="k">if</span> <span class="o">(</span><span class="n">id</span> <span class="o">&lt;=</span> <span class="mi">0</span><span class="o">)</span> <span class="k">throw</span> <span class="k">new</span> <span class="nf">IllegalArgumentException</span><span class="o">(</span>
            <span class="s">"Order ID must be positive"</span><span class="o">);</span>
        <span class="nc">Objects</span><span class="o">.</span><span class="na">requireNonNull</span><span class="o">(</span><span class="n">category</span><span class="o">);</span>
        <span class="k">if</span> <span class="o">(</span><span class="n">amount</span> <span class="o">&lt;</span> <span class="mi">0</span><span class="o">)</span> <span class="k">throw</span> <span class="k">new</span> <span class="nf">IllegalArgumentException</span><span class="o">(</span>
            <span class="s">"Amount cannot be negative"</span><span class="o">);</span>
        <span class="c1">// Defensive copy for immutability</span>
        <span class="n">items</span> <span class="o">=</span> <span class="nc">List</span><span class="o">.</span><span class="na">copyOf</span><span class="o">(</span><span class="n">items</span><span class="o">);</span>
    <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">case</span> <span class="k">class</span> <span class="nc">Order</span><span class="o">(</span>
    <span class="n">id</span><span class="k">:</span> <span class="kt">Long</span><span class="o">,</span>
    <span class="n">category</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span>
    <span class="n">amount</span><span class="k">:</span> <span class="kt">Double</span><span class="o">,</span>
    <span class="n">customer</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span>
    <span class="n">date</span><span class="k">:</span> <span class="kt">LocalDate</span><span class="o">,</span>
    <span class="n">items</span><span class="k">:</span> <span class="kt">List</span><span class="o">[</span><span class="kt">String</span><span class="o">]</span>
<span class="o">):</span>
  <span class="nf">require</span><span class="o">(</span><span class="n">id</span> <span class="o">&gt;</span> <span class="mi">0</span><span class="o">,</span> <span class="nv">s</span><span class="s">"Order ID must be positive"</span><span class="o">)</span>
  <span class="nf">require</span><span class="o">(</span><span class="nv">category</span><span class="o">.</span><span class="py">nonEmpty</span><span class="o">,</span> <span class="s">"Category required"</span><span class="o">)</span>
  <span class="nf">require</span><span class="o">(</span><span class="n">amount</span> <span class="o">&gt;=</span> <span class="mi">0</span><span class="o">,</span> <span class="s">"Amount cannot be negative"</span><span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">data</span> <span class="kd">class</span> <span class="nc">Order</span><span class="p">(</span>
    <span class="kd">val</span> <span class="py">id</span><span class="p">:</span> <span class="nc">Long</span><span class="p">,</span>
    <span class="kd">val</span> <span class="py">category</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span>
    <span class="kd">val</span> <span class="py">amount</span><span class="p">:</span> <span class="nc">Double</span><span class="p">,</span>
    <span class="kd">val</span> <span class="py">customer</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span>
    <span class="kd">val</span> <span class="py">date</span><span class="p">:</span> <span class="nc">LocalDate</span><span class="p">,</span>
    <span class="kd">val</span> <span class="py">items</span><span class="p">:</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">&gt;</span>
<span class="p">)</span> <span class="p">{</span>
    <span class="k">init</span> <span class="p">{</span>
        <span class="nf">require</span><span class="p">(</span><span class="n">id</span> <span class="p">&gt;</span> <span class="mi">0</span><span class="p">)</span> <span class="p">{</span> <span class="s">"Order ID must be positive"</span> <span class="p">}</span>
        <span class="nf">require</span><span class="p">(</span><span class="n">category</span><span class="p">.</span><span class="nf">isNotBlank</span><span class="p">())</span> <span class="p">{</span> <span class="s">"Category required"</span> <span class="p">}</span>
        <span class="nf">require</span><span class="p">(</span><span class="n">amount</span> <span class="p">&gt;=</span> <span class="mi">0</span><span class="p">)</span> <span class="p">{</span> <span class="s">"Amount cannot be negative"</span> <span class="p">}</span>
    <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
</div>
</div>

## Stream.takeWhile() and dropWhile() (Java 9+)

Java 9 introduced `takeWhile()` and `dropWhile()` for conditional stream processing. These operations work best with **ordered streams**.

### takeWhile() - Take While Condition is True

<div class="code-tabs" data-tabs-id="tabs-2">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Take orders while amount stays below threshold</span>
<span class="kd">public</span> <span class="kd">static</span> <span class="nc">List</span><span class="o">&lt;</span><span class="nc">Order</span><span class="o">&gt;</span> <span class="nf">takeOrdersWhileBelowBudget</span><span class="o">(</span>
        <span class="nc">List</span><span class="o">&lt;</span><span class="nc">Order</span><span class="o">&gt;</span> <span class="n">orders</span><span class="o">,</span> <span class="kt">double</span> <span class="n">maxAmount</span><span class="o">)</span> <span class="o">{</span>
    <span class="k">return</span> <span class="n">orders</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
        <span class="o">.</span><span class="na">sorted</span><span class="o">(</span><span class="nc">Comparator</span><span class="o">.</span><span class="na">comparingDouble</span><span class="o">(</span><span class="nl">Order:</span><span class="o">:</span><span class="n">amount</span><span class="o">))</span>
        <span class="o">.</span><span class="na">takeWhile</span><span class="o">(</span><span class="n">order</span> <span class="o">-&gt;</span> <span class="n">order</span><span class="o">.</span><span class="na">amount</span><span class="o">()</span> <span class="o">&lt;</span> <span class="n">maxAmount</span><span class="o">)</span>
        <span class="o">.</span><span class="na">toList</span><span class="o">();</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Scala's takeWhile is a direct collection operation</span>
<span class="k">def</span> <span class="nf">takeOrdersWhileBelowBudget</span><span class="o">(</span>
    <span class="n">orders</span><span class="k">:</span> <span class="kt">List</span><span class="o">[</span><span class="kt">Order</span><span class="o">],</span> 
    <span class="n">maxAmount</span><span class="k">:</span> <span class="kt">Double</span>
<span class="o">)</span><span class="k">:</span> <span class="kt">List</span><span class="o">[</span><span class="kt">Order</span><span class="o">]</span> <span class="k">=</span>
  <span class="nv">orders</span><span class="o">.</span><span class="py">sortBy</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">amount</span><span class="o">).</span><span class="py">takeWhile</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">amount</span> <span class="o">&lt;</span> <span class="n">maxAmount</span><span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Kotlin's takeWhile is also a direct collection operation</span>
<span class="k">fun</span> <span class="nf">takeOrdersWhileBelowBudget</span><span class="p">(</span>
    <span class="n">orders</span><span class="p">:</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">Order</span><span class="p">&gt;,</span>
    <span class="n">maxAmount</span><span class="p">:</span> <span class="nc">Double</span>
<span class="p">):</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">Order</span><span class="p">&gt;</span> <span class="p">=</span> 
    <span class="n">orders</span><span class="p">.</span><span class="nf">sortedBy</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">amount</span> <span class="p">}</span>
        <span class="p">.</span><span class="nf">takeWhile</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">amount</span> <span class="p">&lt;</span> <span class="n">maxAmount</span> <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

### dropWhile() - Skip While Condition is True

<div class="code-tabs" data-tabs-id="tabs-3">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Skip orders below threshold</span>
<span class="kd">public</span> <span class="kd">static</span> <span class="nc">List</span><span class="o">&lt;</span><span class="nc">Order</span><span class="o">&gt;</span> <span class="nf">dropOrdersBelowThreshold</span><span class="o">(</span>
        <span class="nc">List</span><span class="o">&lt;</span><span class="nc">Order</span><span class="o">&gt;</span> <span class="n">orders</span><span class="o">,</span> <span class="kt">double</span> <span class="n">threshold</span><span class="o">)</span> <span class="o">{</span>
    <span class="k">return</span> <span class="n">orders</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
        <span class="o">.</span><span class="na">sorted</span><span class="o">(</span><span class="nc">Comparator</span><span class="o">.</span><span class="na">comparingDouble</span><span class="o">(</span><span class="nl">Order:</span><span class="o">:</span><span class="n">amount</span><span class="o">))</span>
        <span class="o">.</span><span class="na">dropWhile</span><span class="o">(</span><span class="n">order</span> <span class="o">-&gt;</span> <span class="n">order</span><span class="o">.</span><span class="na">amount</span><span class="o">()</span> <span class="o">&lt;</span> <span class="n">threshold</span><span class="o">)</span>
        <span class="o">.</span><span class="na">toList</span><span class="o">();</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">def</span> <span class="nf">dropOrdersBelowThreshold</span><span class="o">(</span>
    <span class="n">orders</span><span class="k">:</span> <span class="kt">List</span><span class="o">[</span><span class="kt">Order</span><span class="o">],</span> 
    <span class="n">threshold</span><span class="k">:</span> <span class="kt">Double</span>
<span class="o">)</span><span class="k">:</span> <span class="kt">List</span><span class="o">[</span><span class="kt">Order</span><span class="o">]</span> <span class="k">=</span>
  <span class="nv">orders</span><span class="o">.</span><span class="py">sortBy</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">amount</span><span class="o">).</span><span class="py">dropWhile</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">amount</span> <span class="o">&lt;</span> <span class="n">threshold</span><span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">fun</span> <span class="nf">dropOrdersBelowThreshold</span><span class="p">(</span>
    <span class="n">orders</span><span class="p">:</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">Order</span><span class="p">&gt;,</span>
    <span class="n">threshold</span><span class="p">:</span> <span class="nc">Double</span>
<span class="p">):</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">Order</span><span class="p">&gt;</span> <span class="p">=</span> 
    <span class="n">orders</span><span class="p">.</span><span class="nf">sortedBy</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">amount</span> <span class="p">}</span>
        <span class="p">.</span><span class="nf">dropWhile</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">amount</span> <span class="p">&lt;</span> <span class="n">threshold</span> <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

### Combining takeWhile and dropWhile for Range Selection

```java
// Java: Select orders in amount range [$100, $1000)
public static List<Order> getOrdersInAmountRange(
        List<Order> orders, double minAmount, double maxAmount) {
    return orders.stream()
        .sorted(Comparator.comparingDouble(Order::amount))
        .dropWhile(order -> order.amount() < minAmount)
        .takeWhile(order -> order.amount() < maxAmount)
        .toList();
}
```

**Key Insight:** These operations are efficient because they stop processing once the condition changes. However, they rely on the stream being ordered correctly.

## Collectors.groupingBy() with Downstream Collectors

Java's `groupingBy()` collector becomes powerful when combined with downstream collectors.

### Basic Grouping with Count

<div class="code-tabs" data-tabs-id="tabs-4">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Count orders by category</span>
<span class="nc">Map</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">,</span> <span class="nc">Long</span><span class="o">&gt;</span> <span class="n">countByCategory</span> <span class="o">=</span> <span class="n">orders</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
    <span class="o">.</span><span class="na">collect</span><span class="o">(</span><span class="nc">Collectors</span><span class="o">.</span><span class="na">groupingBy</span><span class="o">(</span>
        <span class="nl">Order:</span><span class="o">:</span><span class="n">category</span><span class="o">,</span>
        <span class="nc">Collectors</span><span class="o">.</span><span class="na">counting</span><span class="o">()</span>
    <span class="o">));</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Using groupBy with size</span>
<span class="k">val</span> <span class="nv">countByCategory</span> <span class="k">=</span> <span class="n">orders</span>
  <span class="o">.</span><span class="py">groupBy</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">category</span><span class="o">)</span>
  <span class="o">.</span><span class="py">view</span><span class="o">.</span><span class="py">mapValues</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">size</span><span class="o">).</span><span class="py">toMap</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">val</span> <span class="py">countByCategory</span> <span class="p">=</span> <span class="n">orders</span>
    <span class="p">.</span><span class="nf">groupBy</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">category</span> <span class="p">}</span>
    <span class="p">.</span><span class="nf">mapValues</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">value</span><span class="p">.</span><span class="n">size</span> <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

### Grouping with Sum

<div class="code-tabs" data-tabs-id="tabs-5">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Total amount by category</span>
<span class="nc">Map</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">,</span> <span class="nc">Double</span><span class="o">&gt;</span> <span class="n">totalByCategory</span> <span class="o">=</span> <span class="n">orders</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
    <span class="o">.</span><span class="na">collect</span><span class="o">(</span><span class="nc">Collectors</span><span class="o">.</span><span class="na">groupingBy</span><span class="o">(</span>
        <span class="nl">Order:</span><span class="o">:</span><span class="n">category</span><span class="o">,</span>
        <span class="nc">Collectors</span><span class="o">.</span><span class="na">summingDouble</span><span class="o">(</span><span class="nl">Order:</span><span class="o">:</span><span class="n">amount</span><span class="o">)</span>
    <span class="o">));</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Efficient single-pass with groupMapReduce</span>
<span class="k">val</span> <span class="nv">totalByCategory</span> <span class="k">=</span> <span class="n">orders</span>
  <span class="o">.</span><span class="py">groupMapReduce</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">category</span><span class="o">)(</span><span class="nv">_</span><span class="o">.</span><span class="py">amount</span><span class="o">)(</span><span class="k">_</span> <span class="o">+</span> <span class="k">_</span><span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">val</span> <span class="py">totalByCategory</span> <span class="p">=</span> <span class="n">orders</span>
    <span class="p">.</span><span class="nf">groupBy</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">category</span> <span class="p">}</span>
    <span class="p">.</span><span class="nf">mapValues</span> <span class="p">{</span> <span class="p">(</span><span class="n">_</span><span class="p">,</span> <span class="n">orders</span><span class="p">)</span> <span class="p">-&gt;</span> 
        <span class="n">orders</span><span class="p">.</span><span class="nf">sumOf</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">amount</span> <span class="p">}</span> 
    <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

### Grouping with Statistics

#### Java 21

```java
// Full statistics by category
Map<String, DoubleSummaryStatistics> stats = orders.stream()
    .collect(Collectors.groupingBy(
        Order::category,
        Collectors.summarizingDouble(Order::amount)
    ));

stats.forEach((category, s) -> 
    System.out.printf("%s: count=%d, sum=%.2f, avg=%.2f%n",
        category, s.getCount(), s.getSum(), s.getAverage()));
```

### Nested Grouping

<div class="code-tabs" data-tabs-id="tabs-6">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Group by category, then by customer</span>
<span class="nc">Map</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">,</span> <span class="nc">Map</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">,</span> <span class="nc">List</span><span class="o">&lt;</span><span class="nc">Order</span><span class="o">&gt;&gt;&gt;</span> <span class="n">nested</span> <span class="o">=</span> <span class="n">orders</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
    <span class="o">.</span><span class="na">collect</span><span class="o">(</span><span class="nc">Collectors</span><span class="o">.</span><span class="na">groupingBy</span><span class="o">(</span>
        <span class="nl">Order:</span><span class="o">:</span><span class="n">category</span><span class="o">,</span>
        <span class="nc">Collectors</span><span class="o">.</span><span class="na">groupingBy</span><span class="o">(</span><span class="nl">Order:</span><span class="o">:</span><span class="n">customer</span><span class="o">)</span>
    <span class="o">));</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">val</span> <span class="nv">nested</span> <span class="k">=</span> <span class="n">orders</span>
  <span class="o">.</span><span class="py">groupBy</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">category</span><span class="o">)</span>
  <span class="o">.</span><span class="py">view</span><span class="o">.</span><span class="py">mapValues</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">groupBy</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">customer</span><span class="o">)).</span><span class="py">toMap</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">val</span> <span class="py">nested</span> <span class="p">=</span> <span class="n">orders</span>
    <span class="p">.</span><span class="nf">groupBy</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">category</span> <span class="p">}</span>
    <span class="p">.</span><span class="nf">mapValues</span> <span class="p">{</span> <span class="p">(</span><span class="n">_</span><span class="p">,</span> <span class="n">orders</span><span class="p">)</span> <span class="p">-&gt;</span> 
        <span class="n">orders</span><span class="p">.</span><span class="nf">groupBy</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">customer</span> <span class="p">}</span> 
    <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

### Top N per Group

This is a common interview question: "Find the top 3 orders by amount in each category."

<div class="code-tabs" data-tabs-id="tabs-7">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">static</span> <span class="nc">Map</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">,</span> <span class="nc">List</span><span class="o">&lt;</span><span class="nc">Order</span><span class="o">&gt;&gt;</span> <span class="nf">topNOrdersByCategory</span><span class="o">(</span>
        <span class="nc">List</span><span class="o">&lt;</span><span class="nc">Order</span><span class="o">&gt;</span> <span class="n">orders</span><span class="o">,</span> <span class="kt">int</span> <span class="n">topN</span><span class="o">)</span> <span class="o">{</span>
    <span class="k">return</span> <span class="n">orders</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
        <span class="o">.</span><span class="na">collect</span><span class="o">(</span><span class="nc">Collectors</span><span class="o">.</span><span class="na">groupingBy</span><span class="o">(</span>
            <span class="nl">Order:</span><span class="o">:</span><span class="n">category</span><span class="o">,</span>
            <span class="nc">Collectors</span><span class="o">.</span><span class="na">collectingAndThen</span><span class="o">(</span>
                <span class="nc">Collectors</span><span class="o">.</span><span class="na">toList</span><span class="o">(),</span>
                <span class="n">list</span> <span class="o">-&gt;</span> <span class="n">list</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
                    <span class="o">.</span><span class="na">sorted</span><span class="o">(</span><span class="nc">Comparator</span><span class="o">.</span><span class="na">comparingDouble</span><span class="o">(</span>
                        <span class="nl">Order:</span><span class="o">:</span><span class="n">amount</span><span class="o">).</span><span class="na">reversed</span><span class="o">())</span>
                    <span class="o">.</span><span class="na">limit</span><span class="o">(</span><span class="n">topN</span><span class="o">)</span>
                    <span class="o">.</span><span class="na">toList</span><span class="o">()</span>
            <span class="o">)</span>
        <span class="o">));</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">def</span> <span class="nf">topNOrdersByCategory</span><span class="o">(</span>
    <span class="n">orders</span><span class="k">:</span> <span class="kt">List</span><span class="o">[</span><span class="kt">Order</span><span class="o">],</span> 
    <span class="n">topN</span><span class="k">:</span> <span class="kt">Int</span>
<span class="o">)</span><span class="k">:</span> <span class="kt">Map</span><span class="o">[</span><span class="kt">String</span>, <span class="kt">List</span><span class="o">[</span><span class="kt">Order</span><span class="o">]]</span> <span class="k">=</span>
  <span class="nv">orders</span><span class="o">.</span><span class="py">groupBy</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">category</span><span class="o">).</span><span class="py">view</span><span class="o">.</span><span class="py">mapValues</span> <span class="o">{</span> <span class="n">categoryOrders</span> <span class="k">=&gt;</span>
    <span class="nv">categoryOrders</span><span class="o">.</span><span class="py">sortBy</span><span class="o">(-</span><span class="nv">_</span><span class="o">.</span><span class="py">amount</span><span class="o">).</span><span class="py">take</span><span class="o">(</span><span class="n">topN</span><span class="o">)</span>
  <span class="o">}.</span><span class="py">toMap</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">fun</span> <span class="nf">topNOrdersByCategory</span><span class="p">(</span>
    <span class="n">orders</span><span class="p">:</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">Order</span><span class="p">&gt;,</span>
    <span class="n">topN</span><span class="p">:</span> <span class="nc">Int</span>
<span class="p">):</span> <span class="nc">Map</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">,</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">Order</span><span class="p">&gt;&gt;</span> <span class="p">=</span>
    <span class="n">orders</span><span class="p">.</span><span class="nf">groupBy</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">category</span> <span class="p">}</span>
        <span class="p">.</span><span class="nf">mapValues</span> <span class="p">{</span> <span class="p">(</span><span class="n">_</span><span class="p">,</span> <span class="n">orders</span><span class="p">)</span> <span class="p">-&gt;</span>
            <span class="n">orders</span><span class="p">.</span><span class="nf">sortedByDescending</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">amount</span> <span class="p">}.</span><span class="nf">take</span><span class="p">(</span><span class="n">topN</span><span class="p">)</span>
        <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

## Collectors.partitioningBy()

`partitioningBy()` is a special case of grouping that creates exactly two groups based on a predicate.

### Basic Partitioning

<div class="code-tabs" data-tabs-id="tabs-8">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Partition into high-value (&gt;=500) and regular orders</span>
<span class="nc">Map</span><span class="o">&lt;</span><span class="nc">Boolean</span><span class="o">,</span> <span class="nc">List</span><span class="o">&lt;</span><span class="nc">Order</span><span class="o">&gt;&gt;</span> <span class="n">partitioned</span> <span class="o">=</span> <span class="n">orders</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
    <span class="o">.</span><span class="na">collect</span><span class="o">(</span><span class="nc">Collectors</span><span class="o">.</span><span class="na">partitioningBy</span><span class="o">(</span>
        <span class="n">order</span> <span class="o">-&gt;</span> <span class="n">order</span><span class="o">.</span><span class="na">amount</span><span class="o">()</span> <span class="o">&gt;=</span> <span class="mf">500.0</span>
    <span class="o">));</span>

<span class="nc">List</span><span class="o">&lt;</span><span class="nc">Order</span><span class="o">&gt;</span> <span class="n">highValue</span> <span class="o">=</span> <span class="n">partitioned</span><span class="o">.</span><span class="na">get</span><span class="o">(</span><span class="kc">true</span><span class="o">);</span>
<span class="nc">List</span><span class="o">&lt;</span><span class="nc">Order</span><span class="o">&gt;</span> <span class="n">regular</span> <span class="o">=</span> <span class="n">partitioned</span><span class="o">.</span><span class="na">get</span><span class="o">(</span><span class="kc">false</span><span class="o">);</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Returns a tuple (matching, non-matching)</span>
<span class="k">val</span> <span class="o">(</span><span class="n">highValue</span><span class="o">,</span> <span class="n">regular</span><span class="o">)</span> <span class="k">=</span> <span class="nv">orders</span><span class="o">.</span><span class="py">partition</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">amount</span> <span class="o">&gt;=</span> <span class="mf">500.0</span><span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Returns a Pair (matching, non-matching)</span>
<span class="kd">val</span> <span class="p">(</span><span class="py">highValue</span><span class="p">,</span> <span class="n">regular</span><span class="p">)</span> <span class="p">=</span> <span class="n">orders</span><span class="p">.</span><span class="nf">partition</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">amount</span> <span class="p">&gt;=</span> <span class="mf">500.0</span> <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

**Key Difference:** Java returns `Map<Boolean, List>`, while Scala and Kotlin return tuples/pairs which are more type-safe and explicit.

### Partitioning with Downstream Collector

```java
// Partition and sum each group
Map<Boolean, Double> totals = orders.stream()
    .collect(Collectors.partitioningBy(
        order -> order.amount() >= 500.0,
        Collectors.summingDouble(Order::amount)
    ));

System.out.println("High-value total: $" + totals.get(true));
System.out.println("Regular total: $" + totals.get(false));
```

## Custom Collectors

Custom collectors allow you to define exactly how stream elements are accumulated. This is useful for complex aggregations like running totals.

### Running Totals with Custom Collector

<div class="code-tabs" data-tabs-id="tabs-9">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="n">record</span> <span class="nc">OrderWithRunningTotal</span><span class="o">(</span>
    <span class="nc">Order</span> <span class="n">order</span><span class="o">,</span> 
    <span class="kt">double</span> <span class="n">runningTotal</span>
<span class="o">)</span> <span class="o">{}</span>

<span class="kd">public</span> <span class="kd">static</span> <span class="nc">Collector</span><span class="o">&lt;</span><span class="nc">Order</span><span class="o">,</span> <span class="o">?,</span> <span class="nc">List</span><span class="o">&lt;</span><span class="nc">OrderWithRunningTotal</span><span class="o">&gt;&gt;</span> 
        <span class="nf">runningTotalCollector</span><span class="o">()</span> <span class="o">{</span>
    <span class="k">return</span> <span class="nc">Collector</span><span class="o">.</span><span class="na">of</span><span class="o">(</span>
        <span class="c1">// Supplier: create accumulator</span>
        <span class="o">()</span> <span class="o">-&gt;</span> <span class="o">{</span>
            <span class="nc">List</span><span class="o">&lt;</span><span class="nc">OrderWithRunningTotal</span><span class="o">&gt;</span> <span class="n">list</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">ArrayList</span><span class="o">&lt;&gt;();</span>
            <span class="k">return</span> <span class="k">new</span> <span class="nc">Object</span><span class="o">[]</span> <span class="o">{</span> <span class="n">list</span><span class="o">,</span> <span class="k">new</span> <span class="kt">double</span><span class="o">[]</span> <span class="o">{</span> <span class="mf">0.0</span> <span class="o">}</span> <span class="o">};</span>
        <span class="o">},</span>
        <span class="c1">// Accumulator: add each element</span>
        <span class="o">(</span><span class="n">acc</span><span class="o">,</span> <span class="n">order</span><span class="o">)</span> <span class="o">-&gt;</span> <span class="o">{</span>
            <span class="nc">List</span><span class="o">&lt;</span><span class="nc">OrderWithRunningTotal</span><span class="o">&gt;</span> <span class="n">list</span> <span class="o">=</span> 
                <span class="o">(</span><span class="nc">List</span><span class="o">&lt;</span><span class="nc">OrderWithRunningTotal</span><span class="o">&gt;)</span> <span class="n">acc</span><span class="o">[</span><span class="mi">0</span><span class="o">];</span>
            <span class="kt">double</span><span class="o">[]</span> <span class="n">total</span> <span class="o">=</span> <span class="o">(</span><span class="kt">double</span><span class="o">[])</span> <span class="n">acc</span><span class="o">[</span><span class="mi">1</span><span class="o">];</span>
            <span class="n">total</span><span class="o">[</span><span class="mi">0</span><span class="o">]</span> <span class="o">+=</span> <span class="n">order</span><span class="o">.</span><span class="na">amount</span><span class="o">();</span>
            <span class="n">list</span><span class="o">.</span><span class="na">add</span><span class="o">(</span><span class="k">new</span> <span class="nc">OrderWithRunningTotal</span><span class="o">(</span><span class="n">order</span><span class="o">,</span> <span class="n">total</span><span class="o">[</span><span class="mi">0</span><span class="o">]));</span>
        <span class="o">},</span>
        <span class="c1">// Combiner: merge accumulators (for parallel)</span>
        <span class="o">(</span><span class="n">acc1</span><span class="o">,</span> <span class="n">acc2</span><span class="o">)</span> <span class="o">-&gt;</span> <span class="o">{</span> <span class="k">return</span> <span class="n">acc1</span><span class="o">;</span> <span class="o">},</span>
        <span class="c1">// Finisher: extract result</span>
        <span class="n">acc</span> <span class="o">-&gt;</span> <span class="o">(</span><span class="nc">List</span><span class="o">&lt;</span><span class="nc">OrderWithRunningTotal</span><span class="o">&gt;)</span> <span class="n">acc</span><span class="o">[</span><span class="mi">0</span><span class="o">]</span>
    <span class="o">);</span>
<span class="o">}</span>

<span class="c1">// Usage</span>
<span class="nc">List</span><span class="o">&lt;</span><span class="nc">OrderWithRunningTotal</span><span class="o">&gt;</span> <span class="n">withTotals</span> <span class="o">=</span> <span class="n">orders</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
    <span class="o">.</span><span class="na">sorted</span><span class="o">(</span><span class="nc">Comparator</span><span class="o">.</span><span class="na">comparing</span><span class="o">(</span><span class="nl">Order:</span><span class="o">:</span><span class="n">date</span><span class="o">))</span>
    <span class="o">.</span><span class="na">collect</span><span class="o">(</span><span class="n">runningTotalCollector</span><span class="o">());</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Scala's scanLeft is more elegant for running totals</span>
<span class="k">case</span> <span class="k">class</span> <span class="nc">OrderWithRunningTotal</span><span class="o">(</span><span class="n">order</span><span class="k">:</span> <span class="kt">Order</span><span class="o">,</span> <span class="n">runningTotal</span><span class="k">:</span> <span class="kt">Double</span><span class="o">)</span>

<span class="k">def</span> <span class="nf">calculateRunningTotals</span><span class="o">(</span>
    <span class="n">orders</span><span class="k">:</span> <span class="kt">List</span><span class="o">[</span><span class="kt">Order</span><span class="o">]</span>
<span class="o">)</span><span class="k">:</span> <span class="kt">List</span><span class="o">[</span><span class="kt">OrderWithRunningTotal</span><span class="o">]</span> <span class="k">=</span>
  <span class="k">val</span> <span class="nv">sorted</span> <span class="k">=</span> <span class="nv">orders</span><span class="o">.</span><span class="py">sortBy</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">date</span><span class="o">)</span>
  <span class="n">sorted</span>
    <span class="o">.</span><span class="py">scanLeft</span><span class="o">(</span><span class="mf">0.0</span><span class="o">)((</span><span class="n">acc</span><span class="o">,</span> <span class="n">order</span><span class="o">)</span> <span class="k">=&gt;</span> <span class="n">acc</span> <span class="o">+</span> <span class="nv">order</span><span class="o">.</span><span class="py">amount</span><span class="o">)</span>
    <span class="o">.</span><span class="py">tail</span>  <span class="c1">// Remove initial 0.0</span>
    <span class="o">.</span><span class="py">zip</span><span class="o">(</span><span class="n">sorted</span><span class="o">)</span>
    <span class="o">.</span><span class="py">map</span> <span class="o">{</span> <span class="k">case</span> <span class="o">(</span><span class="n">total</span><span class="o">,</span> <span class="n">order</span><span class="o">)</span> <span class="k">=&gt;</span> 
      <span class="nc">OrderWithRunningTotal</span><span class="o">(</span><span class="n">order</span><span class="o">,</span> <span class="n">total</span><span class="o">)</span> 
    <span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Kotlin's scan is similar to Scala's scanLeft</span>
<span class="kd">data</span> <span class="kd">class</span> <span class="nc">OrderWithRunningTotal</span><span class="p">(</span>
    <span class="kd">val</span> <span class="py">order</span><span class="p">:</span> <span class="nc">Order</span><span class="p">,</span>
    <span class="kd">val</span> <span class="py">runningTotal</span><span class="p">:</span> <span class="nc">Double</span>
<span class="p">)</span>

<span class="k">fun</span> <span class="nf">calculateRunningTotals</span><span class="p">(</span>
    <span class="n">orders</span><span class="p">:</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">Order</span><span class="p">&gt;</span>
<span class="p">):</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">OrderWithRunningTotal</span><span class="p">&gt;</span> <span class="p">{</span>
    <span class="kd">val</span> <span class="py">sorted</span> <span class="p">=</span> <span class="n">orders</span><span class="p">.</span><span class="nf">sortedBy</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">date</span> <span class="p">}</span>
    <span class="kd">var</span> <span class="py">total</span> <span class="p">=</span> <span class="mf">0.0</span>
    <span class="k">return</span> <span class="n">sorted</span><span class="p">.</span><span class="nf">map</span> <span class="p">{</span> <span class="n">order</span> <span class="p">-&gt;</span>
        <span class="n">total</span> <span class="p">+=</span> <span class="n">order</span><span class="p">.</span><span class="n">amount</span>
        <span class="nc">OrderWithRunningTotal</span><span class="p">(</span><span class="n">order</span><span class="p">,</span> <span class="n">total</span><span class="p">)</span>
    <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
</div>
</div>

**Key Insight:** Java requires verbose custom collectors, while Scala and Kotlin have built-in `scan`/`scanLeft` operations that elegantly handle running calculations.

## Parallel Streams and When to Use Them

### When to Use Parallel Streams

✅ **Good candidates:**
- Large datasets (thousands of elements)
- CPU-intensive operations (complex calculations)
- Independent, stateless operations
- Data in easily splittable structures (ArrayList, arrays)

❌ **Avoid when:**
- Small datasets (overhead exceeds benefit)
- I/O operations (thread blocking)
- Order matters (forEachOrdered adds overhead)
- Shared mutable state exists
- Source is not easily splittable (LinkedList, I/O streams)

### Java Parallel Stream Example

```java
// Simple parallel sum
double total = orders.parallelStream()
    .mapToDouble(Order::amount)
    .sum();

// Parallel grouping with concurrent map
Map<String, List<Order>> byCategory = orders.parallelStream()
    .collect(Collectors.groupingByConcurrent(Order::category));
```

### Performance Comparison

```java
public static String compareSequentialVsParallel(List<Order> orders) {
    // Warm up JIT
    for (int i = 0; i < 100; i++) {
        orders.stream().mapToDouble(Order::amount).sum();
        orders.parallelStream().mapToDouble(Order::amount).sum();
    }

    // Sequential timing
    long seqStart = System.nanoTime();
    for (int i = 0; i < 1000; i++) {
        orders.stream().mapToDouble(Order::amount).sum();
    }
    long seqTime = System.nanoTime() - seqStart;

    // Parallel timing
    long parStart = System.nanoTime();
    for (int i = 0; i < 1000; i++) {
        orders.parallelStream().mapToDouble(Order::amount).sum();
    }
    long parTime = System.nanoTime() - parStart;

    return String.format(
        "Sequential: %.3f ms, Parallel: %.3f ms",
        seqTime / 1_000_000.0, parTime / 1_000_000.0);
}
```

**Warning:** For small datasets, parallel streams are often **slower** due to thread management overhead!

## Summary Report Generation

Putting it all together, here's a complete report generator:

### Java 21

```java
public record CategorySummary(
    String category,
    long orderCount,
    double totalAmount,
    double averageAmount,
    double minAmount,
    double maxAmount,
    long uniqueCustomers
) {}

public record OrderReport(
    long totalOrders,
    double totalRevenue,
    Map<String, CategorySummary> categorySummaries,
    Map<String, List<Order>> topOrdersByCategory,
    List<Order> highValueOrders,
    List<Order> regularOrders
) {}

public static OrderReport generateReport(
        List<Order> orders, double highValueThreshold) {
    Map<Boolean, List<Order>> partitioned = 
        partitionByHighValue(orders, highValueThreshold);

    return new OrderReport(
        orders.size(),
        orders.stream().mapToDouble(Order::amount).sum(),
        generateCategorySummaries(orders),
        topNOrdersByCategory(orders, 3),
        partitioned.get(true),
        partitioned.get(false)
    );
}
```

## Feature Comparison Table

<div class="table-wrapper" markdown="1">

| Feature | Java 21 | Scala 3 | Kotlin |
|---------|---------|---------|--------|
| takeWhile/dropWhile | `stream().takeWhile()` | `list.takeWhile()` | `list.takeWhile {}` |
| Group by | `groupingBy()` | `groupBy()` | `groupBy {}` |
| Partition | `partitioningBy()` → Map<Boolean> | `partition()` → Tuple | `partition {}` → Pair |
| Downstream collectors | Extensive library | mapValues/transform | mapValues/transform |
| Custom collectors | `Collector.of()` | foldLeft/scanLeft | fold/scan |
| Running totals | Custom collector | `scanLeft` | `scan` |
| Parallel | `.parallelStream()` | `.par` (separate lib) | Coroutines preferred |

</div>

## Best Practices

1. **Use takeWhile/dropWhile with sorted streams** - They rely on order for predictable results
2. **Combine groupingBy with downstream collectors** - More efficient than separate operations
3. **Prefer partitioningBy for binary splits** - Clearer intent than filter + filter-not
4. **Consider scan operations for running calculations** - Scala/Kotlin's scan is cleaner than custom Java collectors
5. **Benchmark before parallelizing** - Parallel streams have overhead; measure first
6. **Use thread-safe collectors with parallel streams** - `groupingByConcurrent()` instead of `groupingBy()`

## Code Samples

See the complete implementations in our repository:

- [Java 21 Order.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/streams/Order.java)
- [Java 21 OrderAnalyzer.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/streams/OrderAnalyzer.java)
- [Scala 3 Order.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/streams/Order.scala)
- [Scala 3 OrderAnalyzer.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/streams/OrderAnalyzer.scala)
- [Kotlin Order.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/streams/Order.kt)
- [Kotlin OrderAnalyzer.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/streams/OrderAnalyzer.kt)

## Conclusion

Java's Stream API has evolved to include powerful operations like `takeWhile()`, `dropWhile()`, and sophisticated collectors. While Scala and Kotlin often provide more concise syntax (especially for operations like `scan` and `partition`), Java 21 offers comprehensive functionality through its collector framework.

For complex data processing pipelines:
- **Java** excels with its extensive collector library and clear parallel stream support
- **Scala** shines with its expressive operations like `groupMapReduce` and `scanLeft`
- **Kotlin** offers clean syntax with features like `scan` while integrating well with Java's Stream API when needed

Choose based on your team's expertise and the specific requirements of your data processing pipeline.

---

*This is Part 4 of our Java 21 Interview Preparation series. Check out [Part 1: Immutable Data with Java Records](/interview/2025/11/26/immutable-data-with-java-records.html), [Part 2: String Manipulation with Modern APIs](/interview/2025/11/28/string-manipulation-with-modern-apis.html), [Part 3: Collection Factory Methods and Stream Basics](/interview/2025/11/29/collection-factory-methods-and-stream-basics.html), and the [full preparation plan](/interview/2025/11/25/java21-interview-preparation-plan.html).*
