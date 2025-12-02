---
layout: post
title: "Collection Factory Methods and Stream Basics"
description: "Master Java 9+ collection factories (List.of, Set.of, Map.of) and Stream API fundamentals - compare with Scala 3 collections and Kotlin stdlib approaches."
date: 2025-11-29 14:00:00 +0000
categories: [interview]
tags: [java, java21, scala, kotlin, collections, streams, interview-preparation]
---

This is Part 3 of our Java 21 Interview Preparation series. We'll explore modern collection factory methods (Java 9+) and Stream API fundamentals, comparing them with Scala 3 and Kotlin approaches.

## The Problem: Processing Transactions

A common programming task involves processing collections of data: filtering by criteria, grouping by category, and calculating statistics. Let's see how this task is handled in modern Java 21, comparing with idiomatic Scala 3 and Kotlin solutions.

**Problem Statement:** Process a list of transactions: filter by amount, group by category, and calculate statistics.

## Collection Factory Methods (Java 9+)

Before Java 9, creating immutable collections was verbose:

```java
// Java 8 style - verbose and error-prone
List<String> list = Collections.unmodifiableList(Arrays.asList("a", "b", "c"));
Set<String> set = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("a", "b", "c")));
Map<String, Integer> map = Collections.unmodifiableMap(new HashMap<>() {{
    put("one", 1);
    put("two", 2);
}});
```

Java 9+ introduced elegant factory methods:

### List.of(), Set.of(), Map.of()

<div class="code-tabs" data-tabs-id="tabs-1">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Immutable list</span>
<span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">list</span> <span class="o">=</span> <span class="nc">List</span><span class="o">.</span><span class="na">of</span><span class="o">(</span><span class="s">"a"</span><span class="o">,</span> <span class="s">"b"</span><span class="o">,</span> <span class="s">"c"</span><span class="o">);</span>

<span class="c1">// Immutable set</span>
<span class="nc">Set</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">set</span> <span class="o">=</span> <span class="nc">Set</span><span class="o">.</span><span class="na">of</span><span class="o">(</span><span class="s">"a"</span><span class="o">,</span> <span class="s">"b"</span><span class="o">,</span> <span class="s">"c"</span><span class="o">);</span>

<span class="c1">// Immutable map (up to 10 entries)</span>
<span class="nc">Map</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">,</span> <span class="nc">Integer</span><span class="o">&gt;</span> <span class="n">map</span> <span class="o">=</span> <span class="nc">Map</span><span class="o">.</span><span class="na">of</span><span class="o">(</span>
    <span class="s">"one"</span><span class="o">,</span> <span class="mi">1</span><span class="o">,</span>
    <span class="s">"two"</span><span class="o">,</span> <span class="mi">2</span><span class="o">,</span>
    <span class="s">"three"</span><span class="o">,</span> <span class="mi">3</span>
<span class="o">);</span>

<span class="c1">// Immutable map (any number of entries)</span>
<span class="nc">Map</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">,</span> <span class="nc">Integer</span><span class="o">&gt;</span> <span class="n">largeMap</span> <span class="o">=</span> <span class="nc">Map</span><span class="o">.</span><span class="na">ofEntries</span><span class="o">(</span>
    <span class="nc">Map</span><span class="o">.</span><span class="na">entry</span><span class="o">(</span><span class="s">"one"</span><span class="o">,</span> <span class="mi">1</span><span class="o">),</span>
    <span class="nc">Map</span><span class="o">.</span><span class="na">entry</span><span class="o">(</span><span class="s">"two"</span><span class="o">,</span> <span class="mi">2</span><span class="o">),</span>
    <span class="c1">// ... more entries</span>
<span class="o">);</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Immutable by default</span>
<span class="k">val</span> <span class="nv">list</span> <span class="k">=</span> <span class="nc">List</span><span class="o">(</span><span class="s">"a"</span><span class="o">,</span> <span class="s">"b"</span><span class="o">,</span> <span class="s">"c"</span><span class="o">)</span>

<span class="c1">// Immutable set</span>
<span class="k">val</span> <span class="nv">set</span> <span class="k">=</span> <span class="nc">Set</span><span class="o">(</span><span class="s">"a"</span><span class="o">,</span> <span class="s">"b"</span><span class="o">,</span> <span class="s">"c"</span><span class="o">)</span>

<span class="c1">// Immutable map</span>
<span class="k">val</span> <span class="nv">map</span> <span class="k">=</span> <span class="nc">Map</span><span class="o">(</span>
  <span class="s">"one"</span> <span class="o">-&gt;</span> <span class="mi">1</span><span class="o">,</span>
  <span class="s">"two"</span> <span class="o">-&gt;</span> <span class="mi">2</span><span class="o">,</span>
  <span class="s">"three"</span> <span class="o">-&gt;</span> <span class="mi">3</span>
<span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Read-only list</span>
<span class="kd">val</span> <span class="py">list</span> <span class="p">=</span> <span class="nf">listOf</span><span class="p">(</span><span class="s">"a"</span><span class="p">,</span> <span class="s">"b"</span><span class="p">,</span> <span class="s">"c"</span><span class="p">)</span>

<span class="c1">// Read-only set</span>
<span class="kd">val</span> <span class="py">set</span> <span class="p">=</span> <span class="nf">setOf</span><span class="p">(</span><span class="s">"a"</span><span class="p">,</span> <span class="s">"b"</span><span class="p">,</span> <span class="s">"c"</span><span class="p">)</span>

<span class="c1">// Read-only map</span>
<span class="kd">val</span> <span class="py">map</span> <span class="p">=</span> <span class="nf">mapOf</span><span class="p">(</span>
    <span class="s">"one"</span> <span class="n">to</span> <span class="mi">1</span><span class="p">,</span>
    <span class="s">"two"</span> <span class="n">to</span> <span class="mi">2</span><span class="p">,</span>
    <span class="s">"three"</span> <span class="n">to</span> <span class="mi">3</span>
<span class="p">)</span>
</code></pre></div></div>
</div>
</div>

### Key Characteristics

<div class="table-wrapper" markdown="1">

| Feature | Java 9+ | Scala 3 | Kotlin |
|---------|---------|---------|--------|
| Default mutability | Immutable with factory methods | Immutable | Read-only (immutable view) |
| Null elements | Not allowed | Allowed | Allowed |
| Modification | UnsupportedOperationException | New collection created | UnsupportedOperationException |
| Duplicate keys (Map) | IllegalArgumentException | Last value wins | Last value wins |

</div>

## Immutable vs Mutable Collections

Understanding the difference between immutable and mutable collections is crucial for writing thread-safe, predictable code.

<div class="code-tabs" data-tabs-id="tabs-2">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Mutable (traditional)</span>
<span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">mutable</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">ArrayList</span><span class="o">&lt;&gt;();</span>
<span class="n">mutable</span><span class="o">.</span><span class="na">add</span><span class="o">(</span><span class="s">"Apple"</span><span class="o">);</span>
<span class="n">mutable</span><span class="o">.</span><span class="na">add</span><span class="o">(</span><span class="s">"Banana"</span><span class="o">);</span>

<span class="c1">// Immutable (Java 9+)</span>
<span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">immutable</span> <span class="o">=</span> <span class="nc">List</span><span class="o">.</span><span class="na">of</span><span class="o">(</span><span class="s">"Apple"</span><span class="o">,</span> <span class="s">"Banana"</span><span class="o">);</span>
<span class="c1">// immutable.add("Cherry"); // UnsupportedOperationException!</span>

<span class="c1">// Convert immutable to mutable when needed</span>
<span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">copy</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">ArrayList</span><span class="o">&lt;&gt;(</span><span class="n">immutable</span><span class="o">);</span>
<span class="n">copy</span><span class="o">.</span><span class="na">add</span><span class="o">(</span><span class="s">"Cherry"</span><span class="o">);</span> <span class="c1">// OK</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Immutable (default)</span>
<span class="k">val</span> <span class="nv">immutable</span> <span class="k">=</span> <span class="nc">List</span><span class="o">(</span><span class="s">"Apple"</span><span class="o">,</span> <span class="s">"Banana"</span><span class="o">)</span>

<span class="c1">// To "modify", create new collection</span>
<span class="k">val</span> <span class="nv">newList</span> <span class="k">=</span> <span class="n">immutable</span> <span class="o">:+</span> <span class="s">"Cherry"</span>
<span class="c1">// Original unchanged!</span>

<span class="c1">// Mutable (explicit import)</span>
<span class="k">import</span> <span class="nn">scala.collection.mutable.ListBuffer</span>
<span class="k">val</span> <span class="nv">mutable</span> <span class="k">=</span> <span class="nc">ListBuffer</span><span class="o">(</span><span class="s">"Apple"</span><span class="o">,</span> <span class="s">"Banana"</span><span class="o">)</span>
<span class="n">mutable</span> <span class="o">+=</span> <span class="s">"Cherry"</span> <span class="c1">// Modifies in place</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Read-only (default)</span>
<span class="kd">val</span> <span class="py">readOnly</span> <span class="p">=</span> <span class="nf">listOf</span><span class="p">(</span><span class="s">"Apple"</span><span class="p">,</span> <span class="s">"Banana"</span><span class="p">)</span>

<span class="c1">// To "modify", create new collection</span>
<span class="kd">val</span> <span class="py">newList</span> <span class="p">=</span> <span class="n">readOnly</span> <span class="p">+</span> <span class="s">"Cherry"</span>

<span class="c1">// Mutable (explicit)</span>
<span class="kd">val</span> <span class="py">mutable</span> <span class="p">=</span> <span class="nf">mutableListOf</span><span class="p">(</span><span class="s">"Apple"</span><span class="p">,</span> <span class="s">"Banana"</span><span class="p">)</span>
<span class="n">mutable</span><span class="p">.</span><span class="nf">add</span><span class="p">(</span><span class="s">"Cherry"</span><span class="p">)</span> <span class="c1">// Modifies in place</span>
</code></pre></div></div>
</div>
</div>

## Stream API: Filter, Group, Statistics

### Filtering Transactions

<div class="code-tabs" data-tabs-id="tabs-3">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Java 8 style - mutable result</span>
<span class="nc">List</span><span class="o">&lt;</span><span class="nc">Transaction</span><span class="o">&gt;</span> <span class="n">filtered</span> <span class="o">=</span> <span class="n">transactions</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
    <span class="o">.</span><span class="na">filter</span><span class="o">(</span><span class="n">t</span> <span class="o">-&gt;</span> <span class="n">t</span><span class="o">.</span><span class="na">amount</span><span class="o">()</span> <span class="o">&gt;=</span> <span class="mf">50.0</span><span class="o">)</span>
    <span class="o">.</span><span class="na">collect</span><span class="o">(</span><span class="nc">Collectors</span><span class="o">.</span><span class="na">toList</span><span class="o">());</span>

<span class="c1">// Java 16+ style - immutable result</span>
<span class="nc">List</span><span class="o">&lt;</span><span class="nc">Transaction</span><span class="o">&gt;</span> <span class="n">filtered</span> <span class="o">=</span> <span class="n">transactions</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
    <span class="o">.</span><span class="na">filter</span><span class="o">(</span><span class="n">t</span> <span class="o">-&gt;</span> <span class="n">t</span><span class="o">.</span><span class="na">amount</span><span class="o">()</span> <span class="o">&gt;=</span> <span class="mf">50.0</span><span class="o">)</span>
    <span class="o">.</span><span class="na">toList</span><span class="o">();</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Direct collection operations</span>
<span class="k">val</span> <span class="nv">filtered</span> <span class="k">=</span> <span class="n">transactions</span>
  <span class="o">.</span><span class="py">filter</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">amount</span> <span class="o">&gt;=</span> <span class="mf">50.0</span><span class="o">)</span>
<span class="c1">// Result is immutable by default</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Direct collection operations</span>
<span class="kd">val</span> <span class="py">filtered</span> <span class="p">=</span> <span class="n">transactions</span>
    <span class="p">.</span><span class="nf">filter</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">amount</span> <span class="p">&gt;=</span> <span class="mf">50.0</span> <span class="p">}</span>
<span class="c1">// Result is read-only by default</span>
</code></pre></div></div>
</div>
</div>

### Grouping by Category

<div class="code-tabs" data-tabs-id="tabs-4">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Group transactions by category</span>
<span class="nc">Map</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">,</span> <span class="nc">List</span><span class="o">&lt;</span><span class="nc">Transaction</span><span class="o">&gt;&gt;</span> <span class="n">byCategory</span> <span class="o">=</span> 
    <span class="n">transactions</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
        <span class="o">.</span><span class="na">collect</span><span class="o">(</span><span class="nc">Collectors</span><span class="o">.</span><span class="na">groupingBy</span><span class="o">(</span>
            <span class="nl">Transaction:</span><span class="o">:</span><span class="n">category</span>
        <span class="o">));</span>

<span class="c1">// Calculate total per category</span>
<span class="nc">Map</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">,</span> <span class="nc">Double</span><span class="o">&gt;</span> <span class="n">totals</span> <span class="o">=</span> 
    <span class="n">transactions</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
        <span class="o">.</span><span class="na">collect</span><span class="o">(</span><span class="nc">Collectors</span><span class="o">.</span><span class="na">groupingBy</span><span class="o">(</span>
            <span class="nl">Transaction:</span><span class="o">:</span><span class="n">category</span><span class="o">,</span>
            <span class="nc">Collectors</span><span class="o">.</span><span class="na">summingDouble</span><span class="o">(</span>
                <span class="nl">Transaction:</span><span class="o">:</span><span class="n">amount</span>
            <span class="o">)</span>
        <span class="o">));</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Group transactions by category</span>
<span class="k">val</span> <span class="nv">byCategory</span> <span class="k">=</span> <span class="n">transactions</span>
  <span class="o">.</span><span class="py">groupBy</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">category</span><span class="o">)</span>

<span class="c1">// Calculate total per category</span>
<span class="c1">// groupMapReduce: single-pass operation</span>
<span class="k">val</span> <span class="nv">totals</span> <span class="k">=</span> <span class="n">transactions</span>
  <span class="o">.</span><span class="py">groupMapReduce</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">category</span><span class="o">)(</span><span class="nv">_</span><span class="o">.</span><span class="py">amount</span><span class="o">)(</span><span class="k">_</span> <span class="o">+</span> <span class="k">_</span><span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Group transactions by category</span>
<span class="kd">val</span> <span class="py">byCategory</span> <span class="p">=</span> <span class="n">transactions</span>
    <span class="p">.</span><span class="nf">groupBy</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">category</span> <span class="p">}</span>

<span class="c1">// Calculate total per category</span>
<span class="kd">val</span> <span class="py">totals</span> <span class="p">=</span> <span class="n">transactions</span>
    <span class="p">.</span><span class="nf">groupBy</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">category</span> <span class="p">}</span>
    <span class="p">.</span><span class="nf">mapValues</span> <span class="p">{</span> <span class="p">(</span><span class="n">_</span><span class="p">,</span> <span class="n">txns</span><span class="p">)</span> <span class="p">-&gt;</span> 
        <span class="n">txns</span><span class="p">.</span><span class="nf">sumOf</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">amount</span> <span class="p">}</span> 
    <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

### Calculating Statistics

<div class="code-tabs" data-tabs-id="tabs-5">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// DoubleSummaryStatistics provides:</span>
<span class="c1">// count, sum, min, max, average</span>
<span class="nc">DoubleSummaryStatistics</span> <span class="n">stats</span> <span class="o">=</span> 
    <span class="n">transactions</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
        <span class="o">.</span><span class="na">mapToDouble</span><span class="o">(</span><span class="nl">Transaction:</span><span class="o">:</span><span class="n">amount</span><span class="o">)</span>
        <span class="o">.</span><span class="na">summaryStatistics</span><span class="o">();</span>

<span class="nc">System</span><span class="o">.</span><span class="na">out</span><span class="o">.</span><span class="na">println</span><span class="o">(</span><span class="s">"Count: "</span> <span class="o">+</span> <span class="n">stats</span><span class="o">.</span><span class="na">getCount</span><span class="o">());</span>
<span class="nc">System</span><span class="o">.</span><span class="na">out</span><span class="o">.</span><span class="na">println</span><span class="o">(</span><span class="s">"Sum: "</span> <span class="o">+</span> <span class="n">stats</span><span class="o">.</span><span class="na">getSum</span><span class="o">());</span>
<span class="nc">System</span><span class="o">.</span><span class="na">out</span><span class="o">.</span><span class="na">println</span><span class="o">(</span><span class="s">"Average: "</span> <span class="o">+</span> <span class="n">stats</span><span class="o">.</span><span class="na">getAverage</span><span class="o">());</span>
<span class="nc">System</span><span class="o">.</span><span class="na">out</span><span class="o">.</span><span class="na">println</span><span class="o">(</span><span class="s">"Min: "</span> <span class="o">+</span> <span class="n">stats</span><span class="o">.</span><span class="na">getMin</span><span class="o">());</span>
<span class="nc">System</span><span class="o">.</span><span class="na">out</span><span class="o">.</span><span class="na">println</span><span class="o">(</span><span class="s">"Max: "</span> <span class="o">+</span> <span class="n">stats</span><span class="o">.</span><span class="na">getMax</span><span class="o">());</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Calculate statistics manually</span>
<span class="k">val</span> <span class="nv">amounts</span> <span class="k">=</span> <span class="n">transactions</span><span class="o">.</span><span class="py">map</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">amount</span><span class="o">)</span>
<span class="k">val</span> <span class="nv">stats</span> <span class="k">=</span> <span class="nc">Statistics</span><span class="o">(</span>
  <span class="n">count</span> <span class="k">=</span> <span class="nv">transactions</span><span class="o">.</span><span class="py">size</span><span class="o">,</span>
  <span class="n">total</span> <span class="k">=</span> <span class="nv">amounts</span><span class="o">.</span><span class="py">sum</span><span class="o">,</span>
  <span class="n">average</span> <span class="k">=</span> <span class="nv">amounts</span><span class="o">.</span><span class="py">sum</span> <span class="o">/</span> <span class="nv">transactions</span><span class="o">.</span><span class="py">size</span><span class="o">,</span>
  <span class="n">min</span> <span class="k">=</span> <span class="nv">amounts</span><span class="o">.</span><span class="py">min</span><span class="o">,</span>
  <span class="n">max</span> <span class="k">=</span> <span class="nv">amounts</span><span class="o">.</span><span class="py">max</span>
<span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Calculate statistics using stdlib</span>
<span class="kd">val</span> <span class="py">amounts</span> <span class="p">=</span> <span class="n">transactions</span><span class="p">.</span><span class="nf">map</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">amount</span> <span class="p">}</span>
<span class="kd">val</span> <span class="py">stats</span> <span class="p">=</span> <span class="nc">Statistics</span><span class="p">(</span>
    <span class="n">count</span> <span class="p">=</span> <span class="n">transactions</span><span class="p">.</span><span class="n">size</span><span class="p">,</span>
    <span class="n">total</span> <span class="p">=</span> <span class="n">amounts</span><span class="p">.</span><span class="nf">sum</span><span class="p">(),</span>
    <span class="n">average</span> <span class="p">=</span> <span class="n">amounts</span><span class="p">.</span><span class="nf">average</span><span class="p">(),</span>
    <span class="n">min</span> <span class="p">=</span> <span class="n">amounts</span><span class="p">.</span><span class="nf">min</span><span class="p">(),</span>
    <span class="n">max</span> <span class="p">=</span> <span class="n">amounts</span><span class="p">.</span><span class="nf">max</span><span class="p">()</span>
<span class="p">)</span>
</code></pre></div></div>
</div>
</div>

## Collectors.teeing() (Java 12+)

`Collectors.teeing()` combines two collectors and merges their results in a single pass. This is particularly useful when you need multiple aggregate values.

### Finding Min and Max in One Pass

<div class="code-tabs" data-tabs-id="tabs-6">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="n">record</span> <span class="nc">MinMaxResult</span><span class="o">(</span>
    <span class="nc">Transaction</span> <span class="n">min</span><span class="o">,</span> 
    <span class="nc">Transaction</span> <span class="n">max</span>
<span class="o">)</span> <span class="o">{}</span>

<span class="nc">MinMaxResult</span> <span class="n">result</span> <span class="o">=</span> <span class="n">transactions</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
    <span class="o">.</span><span class="na">collect</span><span class="o">(</span><span class="nc">Collectors</span><span class="o">.</span><span class="na">teeing</span><span class="o">(</span>
        <span class="nc">Collectors</span><span class="o">.</span><span class="na">minBy</span><span class="o">(</span>
            <span class="nc">Comparator</span><span class="o">.</span><span class="na">comparingDouble</span><span class="o">(</span>
                <span class="nl">Transaction:</span><span class="o">:</span><span class="n">amount</span>
            <span class="o">)</span>
        <span class="o">),</span>
        <span class="nc">Collectors</span><span class="o">.</span><span class="na">maxBy</span><span class="o">(</span>
            <span class="nc">Comparator</span><span class="o">.</span><span class="na">comparingDouble</span><span class="o">(</span>
                <span class="nl">Transaction:</span><span class="o">:</span><span class="n">amount</span>
            <span class="o">)</span>
        <span class="o">),</span>
        <span class="o">(</span><span class="n">minOpt</span><span class="o">,</span> <span class="n">maxOpt</span><span class="o">)</span> <span class="o">-&gt;</span> <span class="k">new</span> <span class="nc">MinMaxResult</span><span class="o">(</span>
            <span class="n">minOpt</span><span class="o">.</span><span class="na">orElse</span><span class="o">(</span><span class="kc">null</span><span class="o">),</span>
            <span class="n">maxOpt</span><span class="o">.</span><span class="na">orElse</span><span class="o">(</span><span class="kc">null</span><span class="o">)</span>
        <span class="o">)</span>
    <span class="o">));</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">case</span> <span class="k">class</span> <span class="nc">MinMaxResult</span><span class="o">(</span>
  <span class="n">min</span><span class="k">:</span> <span class="kt">Option</span><span class="o">[</span><span class="kt">Transaction</span><span class="o">],</span>
  <span class="n">max</span><span class="k">:</span> <span class="kt">Option</span><span class="o">[</span><span class="kt">Transaction</span><span class="o">]</span>
<span class="o">)</span>

<span class="c1">// Simpler in Scala - no need for teeing</span>
<span class="k">val</span> <span class="nv">result</span> <span class="k">=</span> <span class="nc">MinMaxResult</span><span class="o">(</span>
  <span class="n">min</span> <span class="k">=</span> <span class="nv">transactions</span><span class="o">.</span><span class="py">minByOption</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">amount</span><span class="o">),</span>
  <span class="n">max</span> <span class="k">=</span> <span class="nv">transactions</span><span class="o">.</span><span class="py">maxByOption</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">amount</span><span class="o">)</span>
<span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">data</span> <span class="kd">class</span> <span class="nc">MinMaxResult</span><span class="p">(</span>
    <span class="kd">val</span> <span class="py">min</span><span class="p">:</span> <span class="nc">Transaction</span><span class="p">?,</span>
    <span class="kd">val</span> <span class="py">max</span><span class="p">:</span> <span class="nc">Transaction</span><span class="p">?</span>
<span class="p">)</span>

<span class="c1">// Simpler in Kotlin - no need for teeing</span>
<span class="kd">val</span> <span class="py">result</span> <span class="p">=</span> <span class="nc">MinMaxResult</span><span class="p">(</span>
    <span class="n">min</span> <span class="p">=</span> <span class="n">transactions</span><span class="p">.</span><span class="nf">minByOrNull</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">amount</span> <span class="p">},</span>
    <span class="n">max</span> <span class="p">=</span> <span class="n">transactions</span><span class="p">.</span><span class="nf">maxByOrNull</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="n">amount</span> <span class="p">}</span>
<span class="p">)</span>
</code></pre></div></div>
</div>
</div>

### Combined Summary Statistics

```java
record SummaryResult(double total, long count, double average) {}

SummaryResult summary = transactions.stream()
    .collect(Collectors.teeing(
        Collectors.summingDouble(Transaction::amount),
        Collectors.counting(),
        (sum, count) -> new SummaryResult(
            sum,
            count,
            count > 0 ? sum / count : 0.0
        )
    ));
```

## Stream.toList() vs Collectors.toList()

Java 16 introduced `Stream.toList()` as a more concise alternative to `Collectors.toList()`:

<div class="table-wrapper" markdown="1">

| Method | Return Type | Mutability | Java Version |
|--------|-------------|------------|--------------|
| `Collectors.toList()` | ArrayList | Mutable | Java 8+ |
| `Stream.toList()` | Unmodifiable List | Immutable | Java 16+ |
| `Collectors.toUnmodifiableList()` | Unmodifiable List | Immutable | Java 10+ |

</div>

```java
// Java 8 style - returns mutable ArrayList
List<Transaction> mutableList = transactions.stream()
    .filter(t -> t.amount() > 50)
    .collect(Collectors.toList());
mutableList.add(newTransaction); // OK

// Java 16+ style - returns unmodifiable list
List<Transaction> immutableList = transactions.stream()
    .filter(t -> t.amount() > 50)
    .toList();
// immutableList.add(newTransaction); // UnsupportedOperationException!
```

**Recommendation:** Prefer `Stream.toList()` for new code when you don't need mutability.

## Transaction Data Model

Here's our Transaction record used in the examples:

<div class="code-tabs" data-tabs-id="tabs-7">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="n">record</span> <span class="nc">Transaction</span><span class="o">(</span>
    <span class="kt">long</span> <span class="n">id</span><span class="o">,</span>
    <span class="nc">String</span> <span class="n">category</span><span class="o">,</span>
    <span class="kt">double</span> <span class="n">amount</span><span class="o">,</span>
    <span class="nc">String</span> <span class="n">description</span><span class="o">,</span>
    <span class="nc">LocalDate</span> <span class="n">date</span>
<span class="o">)</span> <span class="o">{</span>
    <span class="kd">public</span> <span class="nf">Transaction</span> <span class="o">{</span>
        <span class="k">if</span> <span class="o">(</span><span class="n">id</span> <span class="o">&lt;=</span> <span class="mi">0</span><span class="o">)</span> <span class="k">throw</span> <span class="k">new</span> <span class="nf">IllegalArgumentException</span><span class="o">(</span>
            <span class="s">"ID must be positive"</span><span class="o">);</span>
        <span class="nc">Objects</span><span class="o">.</span><span class="na">requireNonNull</span><span class="o">(</span><span class="n">category</span><span class="o">);</span>
        <span class="k">if</span> <span class="o">(</span><span class="n">amount</span> <span class="o">&lt;=</span> <span class="mi">0</span><span class="o">)</span> <span class="k">throw</span> <span class="k">new</span> <span class="nf">IllegalArgumentException</span><span class="o">(</span>
            <span class="s">"Amount must be positive"</span><span class="o">);</span>
    <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">case</span> <span class="k">class</span> <span class="nc">Transaction</span><span class="o">(</span>
    <span class="n">id</span><span class="k">:</span> <span class="kt">Long</span><span class="o">,</span>
    <span class="n">category</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span>
    <span class="n">amount</span><span class="k">:</span> <span class="kt">Double</span><span class="o">,</span>
    <span class="n">description</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span>
    <span class="n">date</span><span class="k">:</span> <span class="kt">LocalDate</span>
<span class="o">):</span>
  <span class="nf">require</span><span class="o">(</span><span class="n">id</span> <span class="o">&gt;</span> <span class="mi">0</span><span class="o">,</span> <span class="s">"ID must be positive"</span><span class="o">)</span>
  <span class="nf">require</span><span class="o">(</span><span class="nv">category</span><span class="o">.</span><span class="py">nonEmpty</span><span class="o">,</span> <span class="s">"Category required"</span><span class="o">)</span>
  <span class="nf">require</span><span class="o">(</span><span class="n">amount</span> <span class="o">&gt;</span> <span class="mi">0</span><span class="o">,</span> <span class="s">"Amount must be positive"</span><span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">data</span> <span class="kd">class</span> <span class="nc">Transaction</span><span class="p">(</span>
    <span class="kd">val</span> <span class="py">id</span><span class="p">:</span> <span class="nc">Long</span><span class="p">,</span>
    <span class="kd">val</span> <span class="py">category</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span>
    <span class="kd">val</span> <span class="py">amount</span><span class="p">:</span> <span class="nc">Double</span><span class="p">,</span>
    <span class="kd">val</span> <span class="py">description</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span>
    <span class="kd">val</span> <span class="py">date</span><span class="p">:</span> <span class="nc">LocalDate</span>
<span class="p">)</span> <span class="p">{</span>
    <span class="k">init</span> <span class="p">{</span>
        <span class="nf">require</span><span class="p">(</span><span class="n">id</span> <span class="p">&gt;</span> <span class="mi">0</span><span class="p">)</span> <span class="p">{</span> <span class="s">"ID must be positive"</span> <span class="p">}</span>
        <span class="nf">require</span><span class="p">(</span><span class="n">category</span><span class="p">.</span><span class="nf">isNotBlank</span><span class="p">())</span> <span class="p">{</span> <span class="s">"Category required"</span> <span class="p">}</span>
        <span class="nf">require</span><span class="p">(</span><span class="n">amount</span> <span class="p">&gt;</span> <span class="mi">0</span><span class="p">)</span> <span class="p">{</span> <span class="s">"Amount must be positive"</span> <span class="p">}</span>
    <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
</div>
</div>

## Summary: Feature Comparison

<div class="table-wrapper" markdown="1">

| Feature | Java 8 | Java 21 | Scala 3 | Kotlin |
|---------|--------|---------|---------|--------|
| Immutable List | `Collections.unmodifiableList()` | `List.of()` | `List()` | `listOf()` |
| Immutable Set | `Collections.unmodifiableSet()` | `Set.of()` | `Set()` | `setOf()` |
| Immutable Map | `Collections.unmodifiableMap()` | `Map.of()` | `Map()` | `mapOf()` |
| Stream to List | `collect(toList())` | `toList()` | N/A (direct) | N/A (direct) |
| Group By | `groupingBy()` | `groupingBy()` | `groupBy()` | `groupBy()` |
| Sum By Group | `groupingBy(..., summingDouble())` | Same | `groupMapReduce()` | `groupBy().mapValues()` |
| Combined Collectors | N/A | `teeing()` | N/A (not needed) | N/A (not needed) |
| Statistics | `summaryStatistics()` | `summaryStatistics()` | Manual | Manual |

</div>

## Best Practices

1. **Prefer immutable collections** - Use `List.of()`, `Set.of()`, `Map.of()` for data that shouldn't change
2. **Use `Stream.toList()`** (Java 16+) - More concise and returns immutable list
3. **Consider `Collectors.teeing()`** (Java 12+) - When you need multiple aggregations in one pass
4. **Understand null handling** - Java factory methods don't allow nulls; Scala and Kotlin do
5. **Know mutability semantics** - Java's `List.of()` throws on modification; Kotlin's `listOf()` is a read-only view

## Code Samples

See the complete implementations in our repository:

- [Java 21 Transaction.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/collections/Transaction.java)
- [Java 21 TransactionProcessor.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/collections/TransactionProcessor.java)
- [Scala 3 Transaction.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/collections/Transaction.scala)
- [Scala 3 TransactionProcessor.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/collections/TransactionProcessor.scala)
- [Kotlin Transaction.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/collections/Transaction.kt)
- [Kotlin TransactionProcessor.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/collections/TransactionProcessor.kt)

## Conclusion

Java's collection APIs have evolved significantly from Java 8 to Java 21. The modern factory methods and Stream enhancements provide:

- **Cleaner code** with concise factory methods
- **Better immutability** with `List.of()`, `Set.of()`, `Map.of()`
- **Improved Stream API** with `toList()` and `teeing()`
- **More functional style** approaching Scala and Kotlin idioms

For Scala and Kotlin developers, modern Java feels more familiar. While Scala and Kotlin still offer advantages like default immutability and more expressive collection operations, Java 21 has significantly closed the gap.

---

*This is Part 3 of our Java 21 Interview Preparation series. Check out [Part 1: Immutable Data with Java Records](/interview/2025/11/28/immutable-data-with-java-records.html), [Part 2: String Manipulation with Modern APIs](/interview/2025/11/28/string-manipulation-with-modern-apis.html), and the [full preparation plan](/interview/2025/11/28/java21-interview-preparation-plan.html).*
