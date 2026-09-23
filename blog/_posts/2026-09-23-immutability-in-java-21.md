---
layout: post
title: "Immutability in Java 21: Beyond Records"
description: "Learn what immutability means in Java 21, how to enforce it with records and immutable collections, and how it compares with Scala 3 and Kotlin data modeling."
date: 2026-09-23 10:00:00 +0000
updated: 2026-09-23 10:00:00 +0000
categories: [interview]
tags: [java, java21, scala, scala3, kotlin, immutability, records, immutable-collections, case-classes, data-classes]
---

You already saw how records remove boilerplate in [Immutable Data with Java Records]({{ site.baseurl }}{% link _posts/2025-11-26-immutable-data-with-java-records.md %}), but the real interview question is usually deeper: *"How do you keep data truly immutable when collections and runtime mutation are involved?"*

## The Problem / Context

For Scala developers, immutability usually feels like the default path. In Java, you can absolutely model immutable data, but you need to be explicit about a few extra rules:

1. Make state final (records help here).
2. Validate state at construction time.
3. Defensively copy mutable inputs.
4. Expose immutable collection views.

If you skip steps 3 and 4, your "immutable" object can still change from the outside.

Immutability matters because shared state is one of the easiest ways to create bugs, especially in concurrent code and long-lived business workflows. When an object can change underneath you, it becomes much harder to reason about what the system is doing at any moment: a thread may read a stale value, a cache may be mutated unexpectedly, and tests become fragile because behavior depends on hidden state changes. In Java 21, we want immutability because it gives us stable snapshots: once a value is created, it stays consistent until we deliberately replace it with a new instance. That makes code easier to read, safer to share across threads, and simpler to validate.

A few places where immutability matters a lot:

- Domain models such as `CustomerProfile`, `Order`, or `Money` values, where a logical object should not change mid-request or mid-transaction.
- Configuration and application settings, where shared configuration should be read-only and predictable during startup and runtime.
- Concurrent systems, where immutable objects can be safely shared across threads without locking each read.
- Cache keys and event payloads, where a value must not be mutated after it has been published or stored.

## Key Concepts

<div class="table-wrapper" markdown="1">

| Concept | Java 21 | Scala 3 | Kotlin |
|---------|---------|---------|--------|
| Data carrier | `record` | `case class` | `data class` |
| Built-in copy for updates | Manual (`withX` method) | `copy(...)` | `copy(...)` |
| Default collection mindset | Mutable ecosystem, choose immutable APIs explicitly | Immutable collections by default in common usage | Read-only interfaces, must still guard Java interop |
| Validation style | Compact constructor / factory checks | `require(...)` in constructor/factory | `require(...)` in `init`/factory |

</div>

## The Solution / Implementation

The example below uses the same `CustomerProfile` idea across Java, Scala, and Kotlin.

<div class="code-tabs" data-tabs-id="immutability-model-tabs">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">record</span> <span class="nc">CustomerProfile</span><span class="o">(</span><span class="kt">long</span> <span class="n">id</span><span class="o">,</span> <span class="nc">String</span> <span class="n">email</span><span class="o">,</span> <span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">roles</span><span class="o">,</span> <span class="nc">Map</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">,</span> <span class="nc">String</span><span class="o">&gt;</span> <span class="n">preferences</span><span class="o">)</span> <span class="o">{</span>
    <span class="kd">public</span> <span class="nf">CustomerProfile</span><span class="o">(</span><span class="kt">long</span> <span class="n">id</span><span class="o">,</span> <span class="nc">String</span> <span class="n">email</span><span class="o">,</span> <span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">roles</span><span class="o">,</span> <span class="nc">Map</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">,</span> <span class="nc">String</span><span class="o">&gt;</span> <span class="n">preferences</span><span class="o">)</span> <span class="o">{</span>
        <span class="k">if</span> <span class="o">(</span><span class="n">id</span> <span class="o">&lt;=</span> <span class="mi">0</span><span class="o">)</span> <span class="k">throw</span> <span class="k">new</span> <span class="nc">IllegalArgumentException</span><span class="o">(</span><span class="s">"id must be positive"</span><span class="o">);</span>
        <span class="k">this</span><span class="o">.</span><span class="na">roles</span> <span class="o">=</span> <span class="nc">List</span><span class="o">.</span><span class="na">copyOf</span><span class="o">(</span><span class="n">roles</span><span class="o">);</span>
        <span class="k">this</span><span class="o">.</span><span class="na">preferences</span> <span class="o">=</span> <span class="nc">Map</span><span class="o">.</span><span class="na">copyOf</span><span class="o">(</span><span class="n">preferences</span><span class="o">);</span>
    <span class="o">}</span>

    <span class="kd">public</span> <span class="nc">CustomerProfile</span> <span class="nf">withRole</span><span class="o">(</span><span class="nc">String</span> <span class="n">role</span><span class="o">)</span> <span class="o">{</span>
        <span class="kd">var</span> <span class="n">normalizedRole</span> <span class="o">=</span> <span class="n">role</span> <span class="o">==</span> <span class="kc">null</span> <span class="o">?</span> <span class="s">""</span> <span class="o">:</span> <span class="n">role</span><span class="o">.</span><span class="na">trim</span><span class="o">();</span>
        <span class="k">if</span> <span class="o">(</span><span class="n">normalizedRole</span><span class="o">.</span><span class="na">isBlank</span><span class="o">())</span> <span class="k">throw</span> <span class="k">new</span> <span class="nc">IllegalArgumentException</span><span class="o">(</span><span class="s">"role cannot be blank"</span><span class="o">);</span>
        <span class="k">if</span> <span class="o">(</span><span class="n">roles</span><span class="o">.</span><span class="na">contains</span><span class="o">(</span><span class="n">normalizedRole</span><span class="o">))</span> <span class="k">return</span> <span class="k">this</span><span class="o">;</span>
        <span class="kd">var</span> <span class="n">updatedRoles</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">ArrayList</span><span class="o">&lt;&gt;(</span><span class="n">roles</span><span class="o">);</span>
        <span class="n">updatedRoles</span><span class="o">.</span><span class="na">add</span><span class="o">(</span><span class="n">normalizedRole</span><span class="o">);</span>
        <span class="k">return</span> <span class="k">new</span> <span class="nc">CustomerProfile</span><span class="o">(</span><span class="n">id</span><span class="o">,</span> <span class="n">email</span><span class="o">,</span> <span class="n">updatedRoles</span><span class="o">,</span> <span class="n">preferences</span><span class="o">);</span>
    <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/immutability/CustomerProfile.java">View full Java example</a></p>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">case</span> <span class="k">class</span> <span class="nc">CustomerProfile</span> <span class="k">private</span> <span class="o">(</span>
    <span class="n">id</span><span class="o">:</span> <span class="kt">Long</span><span class="o">,</span>
    <span class="n">email</span><span class="o">:</span> <span class="kt">String</span><span class="o">,</span>
    <span class="n">roles</span><span class="o">:</span> <span class="kt">List</span><span class="o">[</span><span class="kt">String</span><span class="o">],</span>
    <span class="n">preferences</span><span class="o">:</span> <span class="kt">Map</span><span class="o">[</span><span class="kt">String</span><span class="o">,</span> <span class="kt">String</span><span class="o">]</span>
<span class="o">):</span>
  <span class="k">def</span> <span class="n">withRole</span><span class="o">(</span><span class="n">role</span><span class="o">:</span> <span class="kt">String</span><span class="o">):</span> <span class="kt">CustomerProfile</span> <span class="o">=</span>
    <span class="k">val</span> <span class="n">normalizedRole</span> <span class="o">=</span> <span class="nc">Option</span><span class="o">(</span><span class="n">role</span><span class="o">).</span><span class="n">map</span><span class="o">(_.</span><span class="n">trim</span><span class="o">).</span><span class="n">getOrElse</span><span class="o">(</span><span class="s">""</span><span class="o">)</span>
    <span class="n">require</span><span class="o">(</span><span class="n">normalizedRole</span><span class="o">.</span><span class="n">nonEmpty</span><span class="o">,</span> <span class="s">"role cannot be blank"</span><span class="o">)</span>
    <span class="k">if</span> <span class="n">roles</span><span class="o">.</span><span class="n">contains</span><span class="o">(</span><span class="n">normalizedRole</span><span class="o">)</span> <span class="k">then</span> <span class="k">this</span> <span class="k">else</span> <span class="n">copy</span><span class="o">(</span><span class="n">roles</span> <span class="o">=</span> <span class="n">roles</span> <span class="o">:+</span> <span class="n">normalizedRole</span><span class="o">)</span>

<span class="k">object</span> <span class="nc">CustomerProfile</span><span class="o">:</span>
  <span class="k">def</span> <span class="n">create</span><span class="o">(</span><span class="n">id</span><span class="o">:</span> <span class="kt">Long</span><span class="o">,</span> <span class="n">email</span><span class="o">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">roles</span><span class="o">:</span> <span class="kt">collection</span><span class="o">.</span><span class="kt">Seq</span><span class="o">[</span><span class="kt">String</span><span class="o">],</span> <span class="n">preferences</span><span class="o">:</span> <span class="kt">collection</span><span class="o">.</span><span class="kt">Map</span><span class="o">[</span><span class="kt">String</span><span class="o">,</span> <span class="kt">String</span><span class="o">]):</span> <span class="kt">CustomerProfile</span> <span class="o">=</span>
    <span class="n">require</span><span class="o">(</span><span class="n">id</span> <span class="o">&gt;</span> <span class="mi">0</span><span class="o">,</span> <span class="s">"id must be positive"</span><span class="o">)</span>
    <span class="k">val</span> <span class="n">normalizedEmail</span> <span class="o">=</span> <span class="nc">Option</span><span class="o">(</span><span class="n">email</span><span class="o">).</span><span class="n">map</span><span class="o">(_.</span><span class="n">trim</span><span class="o">).</span><span class="n">getOrElse</span><span class="o">(</span><span class="s">""</span><span class="o">)</span>
    <span class="n">require</span><span class="o">(</span><span class="n">normalizedEmail</span><span class="o">.</span><span class="n">contains</span><span class="o">(</span><span class="s">"@"</span><span class="o">),</span> <span class="s">"email must contain '@'"</span><span class="o">)</span>
    <span class="k">val</span> <span class="n">immutableRoles</span> <span class="o">=</span> <span class="n">roles</span><span class="o">.</span><span class="n">toList</span><span class="o">.</span><span class="n">map</span><span class="o">(</span><span class="n">role</span> <span class="o">=&gt;</span> <span class="nc">Option</span><span class="o">(</span><span class="n">role</span><span class="o">).</span><span class="n">map</span><span class="o">(_.</span><span class="n">trim</span><span class="o">).</span><span class="n">getOrElse</span><span class="o">(</span><span class="s">""</span><span class="o">))</span>
    <span class="k">val</span> <span class="n">immutablePreferences</span> <span class="o">=</span> <span class="n">preferences</span><span class="o">.</span><span class="n">map</span><span class="o">:</span> <span class="k">case</span> <span class="o">(</span><span class="n">key</span><span class="o">,</span> <span class="n">value</span><span class="o">)</span> <span class="o">=&gt;</span> <span class="nc">Option</span><span class="o">(</span><span class="n">key</span><span class="o">).</span><span class="n">map</span><span class="o">(_.</span><span class="n">trim</span><span class="o">).</span><span class="n">getOrElse</span><span class="o">(</span><span class="s">""</span><span class="o">)</span> <span class="o">-&gt;</span> <span class="nc">Option</span><span class="o">(</span><span class="n">value</span><span class="o">).</span><span class="n">map</span><span class="o">(_.</span><span class="n">trim</span><span class="o">).</span><span class="n">getOrElse</span><span class="o">(</span><span class="s">""</span><span class="o">)</span>
    <span class="n">CustomerProfile</span><span class="o">(</span><span class="n">id</span><span class="o">,</span> <span class="n">normalizedEmail</span><span class="o">,</span> <span class="n">immutableRoles</span><span class="o">,</span> <span class="n">immutablePreferences</span><span class="o">.</span><span class="n">toMap</span><span class="o">)</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/immutability/CustomerProfile.scala">View full Scala example</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">data</span> <span class="kd">class</span> <span class="nc">CustomerProfile</span> <span class="k">private</span> <span class="kd">constructor</span><span class="p">(</span>
    <span class="k">val</span> <span class="py">id</span><span class="p">:</span> <span class="nc">Long</span><span class="p">,</span>
    <span class="k">val</span> <span class="py">email</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span>
    <span class="k">val</span> <span class="py">roles</span><span class="p">:</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">&gt;,</span>
    <span class="k">val</span> <span class="py">preferences</span><span class="p">:</span> <span class="nc">Map</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">,</span> <span class="nc">String</span><span class="p">&gt;,</span>
<span class="p">)</span> <span class="p">{</span>
    <span class="k">fun</span> <span class="nf">withRole</span><span class="p">(</span><span class="n">role</span><span class="p">:</span> <span class="nc">String</span><span class="p">):</span> <span class="nc">CustomerProfile</span> <span class="p">{</span>
        <span class="k">val</span> <span class="py">normalizedRole</span> <span class="p">=</span> <span class="n">role</span><span class="p">.</span><span class="nf">trim</span><span class="p">()</span>
        <span class="nf">require</span><span class="p">(</span><span class="n">normalizedRole</span><span class="p">.</span><span class="n">isNotBlank</span><span class="p">())</span> <span class="p">{</span> <span class="s">"role cannot be blank"</span> <span class="p">}</span>
        <span class="k">if</span> <span class="p">(</span><span class="n">roles</span><span class="p">.</span><span class="n">contains</span><span class="p">(</span><span class="n">normalizedRole</span><span class="p">))</span> <span class="k">return</span> <span class="k">this</span>
        <span class="k">return</span> <span class="nf">copy</span><span class="p">(</span><span class="n">roles</span> <span class="p">=</span> <span class="nc">Collections</span><span class="p">.</span><span class="nf">unmodifiableList</span><span class="p">(</span><span class="n">roles</span> <span class="p">+</span> <span class="n">normalizedRole</span><span class="p">))</span>
    <span class="p">}</span>

    <span class="k">companion</span> <span class="k">object</span> <span class="p">{</span>
        <span class="k">fun</span> <span class="nf">create</span><span class="p">(</span><span class="n">id</span><span class="p">:</span> <span class="nc">Long</span><span class="p">,</span> <span class="n">email</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span> <span class="n">roles</span><span class="p">:</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">&gt;,</span> <span class="n">preferences</span><span class="p">:</span> <span class="nc">Map</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">,</span> <span class="nc">String</span><span class="p">&gt;):</span> <span class="nc">CustomerProfile</span> <span class="p">{</span>
            <span class="k">val</span> <span class="py">normalizedEmail</span> <span class="p">=</span> <span class="n">email</span><span class="p">.</span><span class="nf">trim</span><span class="p">()</span>
            <span class="nf">require</span><span class="p">(</span><span class="n">normalizedEmail</span><span class="p">.</span><span class="n">contains</span><span class="p">(</span><span class="s">"@"</span><span class="p">))</span> <span class="p">{</span> <span class="s">"email must contain '@'"</span> <span class="p">}</span>
            <span class="k">val</span> <span class="py">normalizedRoles</span> <span class="p">=</span> <span class="n">roles</span><span class="p">.</span><span class="nf">map</span> <span class="p">{</span> <span class="n">it</span><span class="p">.</span><span class="nf">trim</span><span class="p">()</span> <span class="p">}</span>
            <span class="k">val</span> <span class="py">normalizedPreferences</span> <span class="p">=</span> <span class="n">preferences</span><span class="p">.</span><span class="nf">map</span> <span class="p">{</span> <span class="p">(</span><span class="n">k</span><span class="p">,</span> <span class="n">v</span><span class="p">)</span> <span class="o">-&gt;</span> <span class="n">k</span><span class="p">.</span><span class="nf">trim</span><span class="p">()</span> <span class="k">to</span> <span class="n">v</span><span class="p">.</span><span class="nf">trim</span><span class="p">()</span> <span class="p">}</span>
            <span class="k">return</span> <span class="nc">CustomerProfile</span><span class="p">(</span><span class="n">id</span><span class="p">,</span> <span class="n">normalizedEmail</span><span class="p">,</span> <span class="nc">Collections</span><span class="p">.</span><span class="nf">unmodifiableList</span><span class="p">(</span><span class="n">normalizedRoles</span><span class="p">),</span> <span class="nc">Collections</span><span class="p">.</span><span class="nf">unmodifiableMap</span><span class="p">(</span><span class="n">normalizedPreferences</span><span class="p">.</span><span class="nf">toMap</span><span class="p">()))</span>
        <span class="p">}</span>
    <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/immutability/CustomerProfile.kt">View full Kotlin example</a></p>
</div>
</div>

## Can We Force Immutability in Java?

You can get close, but Java does not have one universal `immutable` keyword.

Instead, you combine language and API choices:

- `record` + validation for stable object state.
- `List.copyOf(...)` / `Map.copyOf(...)` for immutable collection views.
- No setters, no exposed mutable internals.
- `withX(...)` methods that return *new* instances.

So the practical answer is: **you enforce immutability by design**, not by one compiler switch.

## Focused Tests for Immutability

<div class="code-tabs" data-tabs-id="immutability-test-tabs">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="kt">void</span> <span class="nf">shouldExposeUnmodifiableCollections</span><span class="o">()</span> <span class="o">{</span>
    <span class="kd">var</span> <span class="n">profile</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">CustomerProfile</span><span class="o">(</span><span class="mi">1L</span><span class="o">,</span> <span class="s">"alex@example.com"</span><span class="o">,</span> <span class="nc">List</span><span class="o">.</span><span class="na">of</span><span class="o">(</span><span class="s">"user"</span><span class="o">),</span> <span class="nc">Map</span><span class="o">.</span><span class="na">of</span><span class="o">(</span><span class="s">"tier"</span><span class="o">,</span> <span class="s">"standard"</span><span class="o">));</span>
    <span class="n">assertThrows</span><span class="o">(</span><span class="nc">UnsupportedOperationException</span><span class="o">.</span><span class="na">class</span><span class="o">,</span> <span class="o">()</span> <span class="o">-&gt;</span> <span class="n">profile</span><span class="o">.</span><span class="na">roles</span><span class="o">().</span><span class="na">add</span><span class="o">(</span><span class="s">"admin"</span><span class="o">));</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/interview/preparation/immutability/CustomerProfileTest.java">View full Java tests</a></p>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="n">test</span><span class="o">(</span><span class="s">"Should defensively copy mutable constructor inputs"</span><span class="o">)</span> <span class="o">{</span>
  <span class="k">val</span> <span class="n">roles</span> <span class="o">=</span> <span class="n">scala</span><span class="o">.</span><span class="n">collection</span><span class="o">.</span><span class="n">mutable</span><span class="o">.</span><span class="nc">ArrayBuffer</span><span class="o">(</span><span class="s">"user"</span><span class="o">)</span>
  <span class="k">val</span> <span class="n">profile</span> <span class="o">=</span> <span class="nc">CustomerProfile</span><span class="o">.</span><span class="n">create</span><span class="o">(</span><span class="mi">1L</span><span class="o">,</span> <span class="s">"alex@example.com"</span><span class="o">,</span> <span class="n">roles</span><span class="o">,</span> <span class="nc">Map</span><span class="o">(</span><span class="s">"tier"</span> <span class="o">-&gt;</span> <span class="s">"standard"</span><span class="o">))</span>
  <span class="n">roles</span> <span class="o">+=</span> <span class="s">"admin"</span>
  <span class="n">profile</span><span class="o">.</span><span class="n">roles</span> <span class="n">shouldBe</span> <span class="nc">List</span><span class="o">(</span><span class="s">"user"</span><span class="o">)</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/interview/preparation/immutability/CustomerProfileTest.scala">View full Scala tests</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="k">fun</span> <span class="nf">shouldExposeUnmodifiableCollections</span><span class="p">()</span> <span class="p">{</span>
    <span class="k">val</span> <span class="py">profile</span> <span class="p">=</span> <span class="nc">CustomerProfile</span><span class="p">.</span><span class="nf">create</span><span class="p">(</span><span class="mi">1L</span><span class="p">,</span> <span class="s">"alex@example.com"</span><span class="p">,</span> <span class="nf">listOf</span><span class="p">(</span><span class="s">"user"</span><span class="p">),</span> <span class="nf">mapOf</span><span class="p">(</span><span class="s">"tier"</span> <span class="k">to</span> <span class="s">"standard"</span><span class="p">))</span>
    <span class="k">val</span> <span class="py">roles</span> <span class="p">=</span> <span class="n">profile</span><span class="p">.</span><span class="n">roles</span> <span class="k">as</span> <span class="nc">MutableList</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">&gt;</span>
    <span class="nf">assertThrows</span><span class="p">(</span><span class="nc">UnsupportedOperationException</span><span class="o">::</span><span class="k">class</span><span class="p">.</span><span class="na">java</span><span class="p">)</span> <span class="p">{</span> <span class="n">roles</span><span class="p">.</span><span class="nf">add</span><span class="p">(</span><span class="s">"admin"</span><span class="p">)</span> <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/interview/preparation/immutability/CustomerProfileTest.kt">View full Kotlin tests</a></p>
</div>
</div>

## Comparison Table

<div class="table-wrapper" markdown="1">

| Question | Java 21 | Scala 3 | Kotlin |
|----------|---------|---------|--------|
| Are there immutable data types? | Yes (`record`) | Yes (`case class`) | Yes (`data class` with `val`) |
| Are there immutable collections? | Yes via `List.copyOf`, `Map.copyOf`, `List.of`, `Map.of` | Yes, standard immutable collections are common default | Read-only collection types + optional Java unmodifiable wrappers |
| Can you force immutability? | You enforce by conventions + API choices | Strong defaults and type-level nudges | Strong defaults, plus care for Java interop |
| Copy-on-write update ergonomics | Manual `withX` methods | `copy(...)` built-in | `copy(...)` built-in |

</div>

## When to Use / Best Practices

- Use immutable models for domain events, API DTOs, and configuration.
- Validate at construction; never allow "half-valid" objects.
- In Java, always defensively copy incoming collections.
- In Kotlin, remember read-only `List` is not always deeply immutable at runtime.
- In Scala, keep constructor/factory boundaries clear when accepting generic collection inputs.

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>What does immutability really mean in Java?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      In Java, immutability means the observable state of an object cannot change after construction. A record gives you final components, but you still need to protect mutable inputs like lists and maps using defensive copies. So the core idea is not only "no setters" but also "no external handle can mutate my internals".
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>How is Java immutability different from Scala and Kotlin?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Scala and Kotlin make immutable-style modeling feel more natural because copy-based updates are built in and immutable usage is more idiomatic. Java reaches the same outcome, but usually with a little more explicit code, especially around collection handling. In interviews, this is a strong point: Java can be just as safe, but you must be deliberate.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Can teams enforce immutability consistently in Java codebases?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Yes, with team conventions and code review rules: prefer records for value objects, ban mutable fields in DTOs, and require defensive copies for collection components. Add tests that try to mutate exposed collections and expect failures. This combination gives practical, repeatable immutability even without a dedicated language keyword.
    </div>
  </details>
</div>

## Conclusion

For Scala developers moving into Java 21, the key mindset is simple: records are the *start* of immutability, not the full story. Once you combine records, validation, and immutable collection boundaries, Java can model immutable domain data in a way that is robust and interview-ready.

## Code Samples

All examples in this post are runnable. Find them in the repository:
- [Java 21 CustomerProfile](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/immutability/CustomerProfile.java)
- [Scala 3 CustomerProfile](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/immutability/CustomerProfile.scala)
- [Kotlin CustomerProfile](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/immutability/CustomerProfile.kt)
- [Java 21 tests](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/interview/preparation/immutability/CustomerProfileTest.java)
- [Scala 3 tests](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/interview/preparation/immutability/CustomerProfileTest.scala)
- [Kotlin tests](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/interview/preparation/immutability/CustomerProfileTest.kt)

---

*This is part of our [Java 21 Interview Preparation Guide - Your Roadmap to Success]({{ site.baseurl }}{% link _posts/2025-11-25-java21-interview-preparation-plan.md %}). Next related posts: [String Manipulation with Modern APIs]({{ site.baseurl }}{% link _posts/2025-11-28-string-manipulation-with-modern-apis.md %}), [Null-Safe Programming with Optional]({{ site.baseurl }}{% link _posts/2025-11-29-null-safe-programming-with-optional.md %}), and [Collection Factory Methods and Stream Basics]({{ site.baseurl }}{% link _posts/2025-11-29-collection-factory-methods-and-stream-basics.md %}).*
