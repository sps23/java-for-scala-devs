---
layout: post
title: "Null-Safe Programming with Optional"
description: "Eliminate NullPointerException with Java Optional API - learn map, flatMap, filter patterns and compare with Scala Option and Kotlin null-safety features."
date: 2025-11-29 18:00:00 +0000
categories: interview
tags: java java21 scala kotlin optional null-safety interview-preparation
---

This is Part 4 of our Java 21 Interview Preparation series. We'll explore the Optional API and null-safe programming patterns, comparing Java 21's approach with Scala 3's Option and Kotlin's built-in null-safety.

## The Problem: Handling Missing Values

One of the most common sources of bugs in Java applications is the dreaded `NullPointerException`. Traditional null checking leads to verbose, error-prone code with nested conditionals.

**Problem Statement:** Implement a service that fetches user preferences with fallback defaults, avoiding null checks.

### Traditional Null Checking (Don't Do This!)

```java
// Nested null checks - verbose and error-prone
String getTheme(String userId) {
    User user = database.get(userId);
    if (user != null) {
        UserPreference pref = user.getPreference();
        if (pref != null) {
            String theme = pref.getTheme();
            if (theme != null) {
                return theme;
            }
        }
    }
    return "light"; // default
}
```

## Optional API Basics

Java 8 introduced `Optional<T>` to represent values that may or may not be present. Modern Java (9+) has enhanced this API significantly.

### Creating Optionals

<div class="code-tabs" data-tabs-id="tabs-1">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// From nullable value</span>
<span class="nc">Optional</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">opt1</span> <span class="o">=</span> <span class="nc">Optional</span><span class="o">.</span><span class="na">ofNullable</span><span class="o">(</span><span class="n">maybeNull</span><span class="o">);</span>

<span class="c1">// From non-null value (throws if null)</span>
<span class="nc">Optional</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">opt2</span> <span class="o">=</span> <span class="nc">Optional</span><span class="o">.</span><span class="na">of</span><span class="o">(</span><span class="s">"value"</span><span class="o">);</span>

<span class="c1">// Empty optional</span>
<span class="nc">Optional</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">opt3</span> <span class="o">=</span> <span class="nc">Optional</span><span class="o">.</span><span class="na">empty</span><span class="o">();</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// From nullable value (handles null from Java interop)</span>
<span class="k">val</span> <span class="nv">opt1</span><span class="k">:</span> <span class="kt">Option</span><span class="o">[</span><span class="kt">String</span><span class="o">]</span> <span class="k">=</span> <span class="nc">Option</span><span class="o">(</span><span class="n">maybeNull</span><span class="o">)</span>

<span class="c1">// Explicit Some/None</span>
<span class="k">val</span> <span class="nv">opt2</span><span class="k">:</span> <span class="kt">Option</span><span class="o">[</span><span class="kt">String</span><span class="o">]</span> <span class="k">=</span> <span class="nc">Some</span><span class="o">(</span><span class="s">"value"</span><span class="o">)</span>
<span class="k">val</span> <span class="nv">opt3</span><span class="k">:</span> <span class="kt">Option</span><span class="o">[</span><span class="kt">String</span><span class="o">]</span> <span class="k">=</span> <span class="nc">None</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Kotlin uses nullable types instead of Optional</span>
<span class="kd">val</span> <span class="py">opt1</span><span class="p">:</span> <span class="nc">String</span><span class="p">?</span> <span class="p">=</span> <span class="n">maybeNull</span>

<span class="c1">// Non-null value</span>
<span class="kd">val</span> <span class="py">opt2</span><span class="p">:</span> <span class="nc">String</span> <span class="p">=</span> <span class="s">"value"</span>

<span class="c1">// Null value</span>
<span class="kd">val</span> <span class="py">opt3</span><span class="p">:</span> <span class="nc">String</span><span class="p">?</span> <span class="p">=</span> <span class="k">null</span>
</code></pre></div></div>
</div>
</div>

## Extracting Values: orElse, orElseGet, orElseThrow

### orElse() - Provide Default Value

Use when the default is already computed or cheap to create.

<div class="code-tabs" data-tabs-id="tabs-2">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Returns theme or "light" if empty</span>
<span class="nc">String</span> <span class="n">theme</span> <span class="o">=</span> <span class="n">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
    <span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="nl">UserPreference:</span><span class="o">:</span><span class="n">theme</span><span class="o">)</span>
    <span class="o">.</span><span class="na">orElse</span><span class="o">(</span><span class="s">"light"</span><span class="o">);</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// getOrElse is equivalent to orElse</span>
<span class="k">val</span> <span class="nv">theme</span> <span class="k">=</span> <span class="nf">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
  <span class="o">.</span><span class="py">flatMap</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">theme</span><span class="o">)</span>
  <span class="o">.</span><span class="py">getOrElse</span><span class="o">(</span><span class="s">"light"</span><span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Elvis operator (?:) is equivalent to orElse</span>
<span class="kd">val</span> <span class="py">theme</span> <span class="p">=</span> <span class="nf">findUserPreference</span><span class="p">(</span><span class="n">userId</span><span class="p">)?.</span><span class="n">theme</span> <span class="o">?:</span> <span class="s">"light"</span>
</code></pre></div></div>
</div>
</div>

### orElseGet() - Lazy Default Computation

Use when the default is expensive to compute.

<div class="code-tabs" data-tabs-id="tabs-3">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Supplier is only called if Optional is empty</span>
<span class="nc">String</span> <span class="n">theme</span> <span class="o">=</span> <span class="n">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
    <span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="nl">UserPreference:</span><span class="o">:</span><span class="n">theme</span><span class="o">)</span>
    <span class="o">.</span><span class="na">orElseGet</span><span class="o">(()</span> <span class="o">-&gt;</span> <span class="n">computeExpensiveDefault</span><span class="o">());</span>

<span class="c1">// orElse: default is ALWAYS evaluated</span>
<span class="n">opt</span><span class="o">.</span><span class="na">orElse</span><span class="o">(</span><span class="n">expensiveOperation</span><span class="o">());</span> <span class="c1">// expensiveOperation() called even if opt has value!</span>

<span class="c1">// orElseGet: default is only evaluated if needed</span>
<span class="n">opt</span><span class="o">.</span><span class="na">orElseGet</span><span class="o">(()</span> <span class="o">-&gt;</span> <span class="n">expensiveOperation</span><span class="o">());</span> <span class="c1">// expensiveOperation() called only if opt is empty</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// getOrElse is already lazy in Scala (by-name parameter)</span>
<span class="k">val</span> <span class="nv">theme</span> <span class="k">=</span> <span class="nv">opt</span><span class="o">.</span><span class="py">getOrElse</span><span class="o">(</span><span class="nf">computeExpensiveDefault</span><span class="o">())</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Elvis operator is already lazy</span>
<span class="kd">val</span> <span class="py">theme</span> <span class="p">=</span> <span class="n">opt</span> <span class="o">?:</span> <span class="nf">computeExpensiveDefault</span><span class="p">()</span>
</code></pre></div></div>
</div>
</div>

### orElseThrow() - Throw on Absence

Use when absence is exceptional and should be an error.

<div class="code-tabs" data-tabs-id="tabs-4">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nc">UserPreference</span> <span class="n">pref</span> <span class="o">=</span> <span class="n">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
    <span class="o">.</span><span class="na">orElseThrow</span><span class="o">(()</span> <span class="o">-&gt;</span> 
        <span class="k">new</span> <span class="nf">NoSuchElementException</span><span class="o">(</span><span class="s">"User not found: "</span> <span class="o">+</span> <span class="n">userId</span><span class="o">));</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">val</span> <span class="nv">pref</span> <span class="k">=</span> <span class="nf">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
  <span class="o">.</span><span class="py">getOrElse</span><span class="o">(</span><span class="k">throw</span> <span class="k">new</span> <span class="nc">NoSuchElementException</span><span class="o">(</span><span class="nv">s</span><span class="s">"User not found: $userId"</span><span class="o">))</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">val</span> <span class="py">pref</span> <span class="p">=</span> <span class="nf">findUserPreference</span><span class="p">(</span><span class="n">userId</span><span class="p">)</span>
    <span class="o">?:</span> <span class="k">throw</span> <span class="nc">NoSuchElementException</span><span class="p">(</span><span class="s">"User not found: $userId"</span><span class="p">)</span>
</code></pre></div></div>
</div>
</div>

## Transformation: map(), flatMap(), filter()

### map() - Transform Value

Use when transformation returns a non-Optional value.

<div class="code-tabs" data-tabs-id="tabs-5">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Transform theme to uppercase if present</span>
<span class="nc">Optional</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">uppercase</span> <span class="o">=</span> <span class="n">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
    <span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="nl">UserPreference:</span><span class="o">:</span><span class="n">theme</span><span class="o">)</span>
    <span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="nl">String:</span><span class="o">:</span><span class="n">toUpperCase</span><span class="o">);</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">val</span> <span class="nv">uppercase</span> <span class="k">=</span> <span class="nf">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
  <span class="o">.</span><span class="py">flatMap</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">theme</span><span class="o">)</span>
  <span class="o">.</span><span class="py">map</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">toUpperCase</span><span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Safe call operator (?.) is equivalent to map</span>
<span class="kd">val</span> <span class="py">uppercase</span> <span class="p">=</span> <span class="nf">findUserPreference</span><span class="p">(</span><span class="n">userId</span><span class="p">)?.</span><span class="n">theme</span><span class="p">?.</span><span class="nf">uppercase</span><span class="p">()</span>
</code></pre></div></div>
</div>
</div>

### flatMap() - Avoid Nested Optionals

Use when transformation returns an Optional.

<div class="code-tabs" data-tabs-id="tabs-6">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// validateTheme returns Optional&lt;String&gt;</span>
<span class="nc">Optional</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">validTheme</span> <span class="o">=</span> <span class="n">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
    <span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="nl">UserPreference:</span><span class="o">:</span><span class="n">theme</span><span class="o">)</span>
    <span class="o">.</span><span class="na">flatMap</span><span class="o">(</span><span class="k">this</span><span class="o">::</span><span class="n">validateTheme</span><span class="o">);</span>

<span class="c1">// Without flatMap, you'd get Optional&lt;Optional&lt;String&gt;&gt;!</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// flatMap prevents Option[Option[T]]</span>
<span class="k">val</span> <span class="nv">validTheme</span> <span class="k">=</span> <span class="nf">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
  <span class="o">.</span><span class="py">flatMap</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">theme</span><span class="o">)</span>
  <span class="o">.</span><span class="py">flatMap</span><span class="o">(</span><span class="n">validateTheme</span><span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Use let for flatMap-like behavior</span>
<span class="kd">val</span> <span class="py">validTheme</span> <span class="p">=</span> <span class="nf">findUserPreference</span><span class="p">(</span><span class="n">userId</span><span class="p">)</span>
    <span class="p">?.</span><span class="n">theme</span>
    <span class="p">?.</span><span class="nf">let</span> <span class="p">{</span> <span class="nf">validateTheme</span><span class="p">(</span><span class="k">it</span><span class="p">)</span> <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

### filter() - Conditional Processing

Keep value only if predicate matches.

<div class="code-tabs" data-tabs-id="tabs-7">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Only keep font sizes &gt;= 14</span>
<span class="nc">Optional</span><span class="o">&lt;</span><span class="nc">Integer</span><span class="o">&gt;</span> <span class="n">largeFontSize</span> <span class="o">=</span> <span class="n">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
    <span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="nl">UserPreference:</span><span class="o">:</span><span class="n">fontSize</span><span class="o">)</span>
    <span class="o">.</span><span class="na">filter</span><span class="o">(</span><span class="n">size</span> <span class="o">-&gt;</span> <span class="n">size</span> <span class="o">&gt;=</span> <span class="mi">14</span><span class="o">);</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">val</span> <span class="nv">largeFontSize</span> <span class="k">=</span> <span class="nf">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
  <span class="o">.</span><span class="py">flatMap</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">fontSize</span><span class="o">)</span>
  <span class="o">.</span><span class="py">filter</span><span class="o">(</span><span class="k">_</span> <span class="o">&gt;=</span> <span class="mi">14</span><span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// takeIf is equivalent to filter</span>
<span class="kd">val</span> <span class="py">largeFontSize</span> <span class="p">=</span> <span class="nf">findUserPreference</span><span class="p">(</span><span class="n">userId</span><span class="p">)</span>
    <span class="p">?.</span><span class="n">fontSize</span>
    <span class="p">?.</span><span class="nf">takeIf</span> <span class="p">{</span> <span class="k">it</span> <span class="p">&gt;=</span> <span class="mi">14</span> <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

## Java 9+ Enhancements

### ifPresentOrElse() - Handle Both Cases

<div class="code-tabs" data-tabs-id="tabs-8">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="n">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
    <span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="nl">UserPreference:</span><span class="o">:</span><span class="n">theme</span><span class="o">)</span>
    <span class="o">.</span><span class="na">ifPresentOrElse</span><span class="o">(</span>
        <span class="n">theme</span> <span class="o">-&gt;</span> <span class="nc">System</span><span class="o">.</span><span class="na">out</span><span class="o">.</span><span class="na">println</span><span class="o">(</span><span class="s">"User theme: "</span> <span class="o">+</span> <span class="n">theme</span><span class="o">),</span>
        <span class="o">()</span> <span class="o">-&gt;</span> <span class="nc">System</span><span class="o">.</span><span class="na">out</span><span class="o">.</span><span class="na">println</span><span class="o">(</span><span class="s">"Using default theme"</span><span class="o">)</span>
    <span class="o">);</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Pattern matching handles both cases elegantly</span>
<span class="nf">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">).</span><span class="py">flatMap</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">theme</span><span class="o">)</span> <span class="k">match</span>
  <span class="k">case</span> <span class="nc">Some</span><span class="o">(</span><span class="n">theme</span><span class="o">)</span> <span class="k">=&gt;</span> <span class="nf">println</span><span class="o">(</span><span class="nv">s</span><span class="s">"User theme: $theme"</span><span class="o">)</span>
  <span class="k">case</span> <span class="nc">None</span>        <span class="k">=&gt;</span> <span class="nf">println</span><span class="o">(</span><span class="s">"Using default theme"</span><span class="o">)</span>

<span class="c1">// Or using fold</span>
<span class="nf">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
  <span class="o">.</span><span class="py">flatMap</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">theme</span><span class="o">)</span>
  <span class="o">.</span><span class="py">fold</span><span class="o">(</span><span class="nf">println</span><span class="o">(</span><span class="s">"Using default theme"</span><span class="o">))(</span><span class="n">t</span> <span class="k">=&gt;</span> <span class="nf">println</span><span class="o">(</span><span class="nv">s</span><span class="s">"User theme: $t"</span><span class="o">))</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// When expression with nullable</span>
<span class="k">when</span> <span class="p">(</span><span class="kd">val</span> <span class="py">theme</span> <span class="p">=</span> <span class="nf">findUserPreference</span><span class="p">(</span><span class="n">userId</span><span class="p">)?.</span><span class="n">theme</span><span class="p">)</span> <span class="p">{</span>
    <span class="k">null</span> <span class="p">-&gt;</span> <span class="nf">println</span><span class="p">(</span><span class="s">"Using default theme"</span><span class="p">)</span>
    <span class="k">else</span> <span class="p">-&gt;</span> <span class="nf">println</span><span class="p">(</span><span class="s">"User theme: $theme"</span><span class="p">)</span>
<span class="p">}</span>
</code></pre></div></div>
</div>
</div>

### or() - Alternative Optional Source

Provide a fallback Optional when the first is empty.

<div class="code-tabs" data-tabs-id="tabs-9">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nc">String</span> <span class="n">theme</span> <span class="o">=</span> <span class="n">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
    <span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="nl">UserPreference:</span><span class="o">:</span><span class="n">theme</span><span class="o">)</span>
    <span class="o">.</span><span class="na">or</span><span class="o">(()</span> <span class="o">-&gt;</span> <span class="n">getFallbackTheme</span><span class="o">())</span>    <span class="c1">// Returns Optional&lt;String&gt;</span>
    <span class="o">.</span><span class="na">orElse</span><span class="o">(</span><span class="s">"light"</span><span class="o">);</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">val</span> <span class="nv">theme</span> <span class="k">=</span> <span class="nf">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
  <span class="o">.</span><span class="py">flatMap</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">theme</span><span class="o">)</span>
  <span class="o">.</span><span class="py">orElse</span><span class="o">(</span><span class="n">getFallbackTheme</span><span class="o">)</span>
  <span class="o">.</span><span class="py">getOrElse</span><span class="o">(</span><span class="s">"light"</span><span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">val</span> <span class="py">theme</span> <span class="p">=</span> <span class="nf">findUserPreference</span><span class="p">(</span><span class="n">userId</span><span class="p">)?.</span><span class="n">theme</span>
    <span class="o">?:</span> <span class="nf">getFallbackTheme</span><span class="p">()</span>
    <span class="o">?:</span> <span class="s">"light"</span>
</code></pre></div></div>
</div>
</div>

## Refactoring Nested Null Checks

### Before: Nested Null Checks

```java
// Verbose and error-prone
String getProcessedTheme(String userId) {
    UserPreference user = database.get(userId);
    if (user != null) {
        String theme = user.theme();
        if (theme != null) {
            if (isValidTheme(theme)) {
                return theme.toUpperCase();
            }
        }
    }
    return "LIGHT";
}
```

### After: Fluent Optional Chain

<div class="code-tabs" data-tabs-id="tabs-10">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nc">String</span> <span class="nf">getProcessedTheme</span><span class="o">(</span><span class="nc">String</span> <span class="n">userId</span><span class="o">)</span> <span class="o">{</span>
    <span class="k">return</span> <span class="nf">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
        <span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="nl">UserPreference:</span><span class="o">:</span><span class="n">theme</span><span class="o">)</span>
        <span class="o">.</span><span class="na">filter</span><span class="o">(</span><span class="k">this</span><span class="o">::</span><span class="n">isValidTheme</span><span class="o">)</span>
        <span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="nl">String:</span><span class="o">:</span><span class="n">toUpperCase</span><span class="o">)</span>
        <span class="o">.</span><span class="na">orElse</span><span class="o">(</span><span class="s">"LIGHT"</span><span class="o">);</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">def</span> <span class="nf">getDisplaySettings</span><span class="o">(</span><span class="n">userId</span><span class="k">:</span> <span class="kt">String</span><span class="o">)</span><span class="k">:</span> <span class="kt">Option</span><span class="o">[</span><span class="kt">String</span><span class="o">]</span> <span class="k">=</span>
  <span class="k">for</span>
    <span class="n">pref</span> <span class="k">&lt;-</span> <span class="nf">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
    <span class="n">theme</span> <span class="k">&lt;-</span> <span class="nv">pref</span><span class="o">.</span><span class="py">theme</span>
    <span class="n">fontSize</span> <span class="k">&lt;-</span> <span class="nv">pref</span><span class="o">.</span><span class="py">fontSize</span>
  <span class="k">yield</span> <span class="nv">s</span><span class="s">"$theme theme, ${fontSize}px font"</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">fun</span> <span class="nf">getDisplaySettings</span><span class="p">(</span><span class="n">userId</span><span class="p">:</span> <span class="nc">String</span><span class="p">):</span> <span class="nc">String</span><span class="p">?</span> <span class="p">=</span>
    <span class="nf">findUserPreference</span><span class="p">(</span><span class="n">userId</span><span class="p">)?.</span><span class="nf">run</span> <span class="p">{</span>
        <span class="n">theme</span><span class="p">?.</span><span class="nf">let</span> <span class="p">{</span> <span class="n">t</span> <span class="p">-&gt;</span>
            <span class="n">fontSize</span><span class="p">?.</span><span class="nf">let</span> <span class="p">{</span> <span class="n">s</span> <span class="p">-&gt;</span>
                <span class="s">"$t theme, ${s}px font"</span>
            <span class="p">}</span>
        <span class="p">}</span>
    <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

## Complete Example: Preference Resolution

Here's a complete example showing preference resolution with fallbacks:

<div class="code-tabs" data-tabs-id="tabs-11">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="nc">ResolvedPreferences</span> <span class="nf">resolvePreferences</span><span class="o">(</span><span class="nc">String</span> <span class="n">userId</span><span class="o">)</span> <span class="o">{</span>
    <span class="nc">Optional</span><span class="o">&lt;</span><span class="nc">UserPreference</span><span class="o">&gt;</span> <span class="n">userPref</span> <span class="o">=</span> <span class="n">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">);</span>
    
    <span class="k">return</span> <span class="k">new</span> <span class="nf">ResolvedPreferences</span><span class="o">(</span>
        <span class="n">userId</span><span class="o">,</span>
        <span class="n">userPref</span><span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="nl">UserPreference:</span><span class="o">:</span><span class="n">theme</span><span class="o">).</span><span class="na">orElse</span><span class="o">(</span><span class="no">DEFAULT_THEME</span><span class="o">),</span>
        <span class="n">userPref</span><span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="nl">UserPreference:</span><span class="o">:</span><span class="n">language</span><span class="o">).</span><span class="na">orElse</span><span class="o">(</span><span class="no">DEFAULT_LANGUAGE</span><span class="o">),</span>
        <span class="n">userPref</span><span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="nl">UserPreference:</span><span class="o">:</span><span class="n">fontSize</span><span class="o">).</span><span class="na">orElse</span><span class="o">(</span><span class="no">DEFAULT_FONT_SIZE</span><span class="o">),</span>
        <span class="n">userPref</span><span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="nl">UserPreference:</span><span class="o">:</span><span class="n">notificationsEnabled</span><span class="o">)</span>
            <span class="o">.</span><span class="na">orElse</span><span class="o">(</span><span class="no">DEFAULT_NOTIFICATIONS</span><span class="o">)</span>
    <span class="o">);</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">def</span> <span class="nf">resolvePreferences</span><span class="o">(</span><span class="n">userId</span><span class="k">:</span> <span class="kt">String</span><span class="o">)</span><span class="k">:</span> <span class="kt">ResolvedPreferences</span> <span class="o">=</span>
  <span class="k">val</span> <span class="nv">userPref</span> <span class="k">=</span> <span class="nf">findUserPreference</span><span class="o">(</span><span class="n">userId</span><span class="o">)</span>
  
  <span class="nc">ResolvedPreferences</span><span class="o">(</span>
    <span class="n">userId</span> <span class="k">=</span> <span class="n">userId</span><span class="o">,</span>
    <span class="n">theme</span> <span class="k">=</span> <span class="nv">userPref</span><span class="o">.</span><span class="py">flatMap</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">theme</span><span class="o">).</span><span class="py">getOrElse</span><span class="o">(</span><span class="nc">DefaultTheme</span><span class="o">),</span>
    <span class="n">language</span> <span class="k">=</span> <span class="nv">userPref</span><span class="o">.</span><span class="py">flatMap</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">language</span><span class="o">).</span><span class="py">getOrElse</span><span class="o">(</span><span class="nc">DefaultLanguage</span><span class="o">),</span>
    <span class="n">fontSize</span> <span class="k">=</span> <span class="nv">userPref</span><span class="o">.</span><span class="py">flatMap</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">fontSize</span><span class="o">).</span><span class="py">getOrElse</span><span class="o">(</span><span class="nc">DefaultFontSize</span><span class="o">),</span>
    <span class="n">notificationsEnabled</span> <span class="k">=</span> 
      <span class="nv">userPref</span><span class="o">.</span><span class="py">flatMap</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">notificationsEnabled</span><span class="o">).</span><span class="py">getOrElse</span><span class="o">(</span><span class="nc">DefaultNotifications</span><span class="o">)</span>
  <span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">fun</span> <span class="nf">resolvePreferences</span><span class="p">(</span><span class="n">userId</span><span class="p">:</span> <span class="nc">String</span><span class="p">):</span> <span class="nc">ResolvedPreferences</span> <span class="p">{</span>
    <span class="kd">val</span> <span class="py">userPref</span> <span class="p">=</span> <span class="nf">findUserPreference</span><span class="p">(</span><span class="n">userId</span><span class="p">)</span>
    
    <span class="k">return</span> <span class="nc">ResolvedPreferences</span><span class="p">(</span>
        <span class="n">userId</span> <span class="p">=</span> <span class="n">userId</span><span class="p">,</span>
        <span class="n">theme</span> <span class="p">=</span> <span class="n">userPref</span><span class="p">?.</span><span class="n">theme</span> <span class="o">?:</span> <span class="nc">DEFAULT_THEME</span><span class="p">,</span>
        <span class="n">language</span> <span class="p">=</span> <span class="n">userPref</span><span class="p">?.</span><span class="n">language</span> <span class="o">?:</span> <span class="nc">DEFAULT_LANGUAGE</span><span class="p">,</span>
        <span class="n">fontSize</span> <span class="p">=</span> <span class="n">userPref</span><span class="p">?.</span><span class="n">fontSize</span> <span class="o">?:</span> <span class="nc">DEFAULT_FONT_SIZE</span><span class="p">,</span>
        <span class="n">notificationsEnabled</span> <span class="p">=</span> <span class="n">userPref</span><span class="p">?.</span><span class="n">notificationsEnabled</span> <span class="o">?:</span> <span class="nc">DEFAULT_NOTIFICATIONS</span>
    <span class="p">)</span>
<span class="p">}</span>
</code></pre></div></div>
</div>
</div>

## Anti-patterns to Avoid

### 1. Using isPresent() with get()

```java
// DON'T DO THIS - defeats the purpose of Optional
Optional<String> opt = getTheme();
if (opt.isPresent()) {
    return opt.get();
}
return "default";

// DO THIS INSTEAD
return getTheme().orElse("default");
```

### 2. Optional as Method Parameter

```java
// DON'T DO THIS - forces callers to create Optional
void setTheme(Optional<String> theme)

// DO THIS INSTEAD - use @Nullable annotation
void setTheme(@Nullable String theme)
```

### 3. Returning null from Optional-returning Method

```java
// DON'T DO THIS
Optional<String> getTheme() {
    if (condition) return null;  // BAD!
    return Optional.of("dark");
}

// DO THIS INSTEAD
Optional<String> getTheme() {
    if (condition) return Optional.empty();
    return Optional.of("dark");
}
```

### 4. Optional for Collection Fields

```java
// DON'T DO THIS
Optional<List<String>> getItems()

// DO THIS INSTEAD - return empty collection
List<String> getItems()
```

### 5. Kotlin: Excessive !! (Not-Null Assertion)

```kotlin
// DON'T DO THIS - throws NPE if null
val theme = preference?.theme!!

// DO THIS INSTEAD
val theme = preference?.theme ?: "default"
```

## Language Comparison

| Operation | Java Optional | Scala Option | Kotlin |
|-----------|--------------|--------------|--------|
| Wrap nullable | `Optional.ofNullable(x)` | `Option(x)` | `x` (nullable type) |
| Create present | `Optional.of(x)` | `Some(x)` | `x` (non-null) |
| Create empty | `Optional.empty()` | `None` | `null` |
| Default value | `orElse(default)` | `getOrElse(default)` | `?: default` |
| Lazy default | `orElseGet(() -> ...)` | `getOrElse(...)` (lazy) | `?: ...` (lazy) |
| Throw if empty | `orElseThrow(...)` | `getOrElse(throw ...)` | `?: throw ...` |
| Transform | `map(f)` | `map(f)` | `?.let { f(it) }` |
| Flatten | `flatMap(f)` | `flatMap(f)` | `?.let { f(it) }` |
| Filter | `filter(p)` | `filter(p)` | `?.takeIf { p(it) }` |
| Handle both | `ifPresentOrElse(f, g)` | `match/fold` | `when` expression |
| Fallback source | `or(() -> ...)` | `orElse(...)` | `?: ... ?: ...` |

## Best Practices Summary

1. **Prefer Optional for return types**, not for fields or parameters
2. **Use orElseGet() over orElse()** for expensive default computations
3. **Chain operations** instead of nesting null checks
4. **Never return null** from Optional-returning methods
5. **Consider flatMap()** when your transformation returns Optional
6. **Use filter()** for conditional processing
7. **Leverage Java 9+ features** like ifPresentOrElse() and or()

## Code Samples

See the complete implementations in our repository:

- [Java 21 UserPreference.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/optional/UserPreference.java)
- [Java 21 UserPreferenceService.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/optional/UserPreferenceService.java)
- [Scala 3 UserPreference.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/optional/UserPreference.scala)
- [Scala 3 UserPreferenceService.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/optional/UserPreferenceService.scala)
- [Kotlin UserPreference.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/optional/UserPreference.kt)
- [Kotlin UserPreferenceService.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/optional/UserPreferenceService.kt)

## Conclusion

Modern Java's Optional API provides a robust way to handle nullable values, though it's more verbose than Scala's Option or Kotlin's built-in null-safety. Key takeaways:

- **Java 21**: Use Optional with fluent API chains; leverage Java 9+ additions like `ifPresentOrElse()` and `or()`
- **Scala 3**: Option is deeply integrated with for-comprehensions and pattern matching
- **Kotlin**: Built-in null-safety with `?.`, `?:`, and scope functions eliminates the need for a wrapper type

For Scala developers, Java's Optional will feel familiar but more verbose. The good news is that modern Java (9+) has significantly improved the Optional API, making null-safe code more idiomatic.

---

*This is Part 4 of our Java 21 Interview Preparation series. Check out [Part 1: Immutable Data with Java Records](/interview/2025/11/26/immutable-data-with-java-records.html), [Part 2: Sealed Classes and Exhaustive Pattern Matching](/interview/2025/11/28/sealed-classes-and-exhaustive-pattern-matching.html), [Part 3: Collection Factory Methods and Stream Basics](/interview/2025/11/29/collection-factory-methods-and-stream-basics.html), and the [full preparation plan](/interview/2025/11/25/java21-interview-preparation-plan.html).*
