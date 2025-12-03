---
layout: post
title: "Sealed Classes and Exhaustive Pattern Matching"
description: "Model type-safe domain logic with Java 17 sealed classes and exhaustive pattern matching - compare with Scala sealed traits and Kotlin sealed classes with payment system examples."
date: 2025-11-28 21:00:00 +0000
categories: [interview]
tags: [java, java21, scala, kotlin, sealed-classes, pattern-matching, interview-preparation]
---

Sealed classes are a powerful feature for type-safe domain modeling. Java 17 introduced sealed classes and interfaces, bringing Java closer to Scala's sealed traits and Kotlin's sealed classes. In this post, we'll explore how to model a payment system using sealed classes and implement fee calculation with exhaustive pattern matching.

## The Problem: Type-Safe Payment Processing

Imagine we need to model a payment system that supports three payment methods:
- **Credit Card** - 2.9% + $0.30 per transaction
- **Bank Transfer** - flat $2.50 (under $1000) or $5.00 (over $1000)
- **Digital Wallet** - 2.5% with $0.50 minimum

We want the compiler to:
1. Restrict which types can represent a payment method
2. Ensure we handle all payment types in our fee calculation
3. Enable destructuring of payment data in pattern matching

## Key Concepts

### Sealed Classes/Interfaces

In all three languages, sealing a type restricts which other types can extend or implement it:

<div class="table-wrapper" markdown="1">

| Language | Keyword | Behavior |
|----------|---------|----------|
| Java | `sealed ... permits` | Must explicitly list permitted subtypes |
| Scala | `sealed` | Subtypes must be in same file |
| Kotlin | `sealed` | Subtypes must be in same package and module |

</div>

### Exhaustive Pattern Matching

When all possible subtypes are known at compile time, the compiler can verify that pattern matching covers all cases:

<div class="table-wrapper" markdown="1">

| Language | Construct | No default needed |
|----------|-----------|-------------------|
| Java | `switch` expression | ✓ |
| Scala | `match` expression | ✓ |
| Kotlin | `when` expression | ✓ |

</div>

### Subtype Requirements

Each language has rules about what sealed subtypes must be:

<div class="table-wrapper" markdown="1">

| Language | Permitted Subtypes |
|----------|-------------------|
| Java | Must be `final`, `sealed`, or `non-sealed` |
| Scala | Case classes, objects, or other sealed traits |
| Kotlin | Data classes, objects, or other sealed classes/interfaces |

</div>

## The Solution: Sealed Payment Types

<div class="code-tabs" data-tabs-id="tabs-1">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// PaymentMethod.java - Sealed interface restricts implementations</span>
<span class="kd">public</span> <span class="kd">sealed</span> <span class="kd">interface</span> <span class="nc">PaymentMethod</span> <span class="n">permits</span> <span class="nc">CreditCard</span><span class="o">,</span> <span class="nc">BankTransfer</span><span class="o">,</span> <span class="nc">DigitalWallet</span> <span class="o">{</span>
    <span class="nc">BigDecimal</span> <span class="nf">amount</span><span class="o">();</span>
<span class="o">}</span>

<span class="c1">// CreditCard.java - Record implementing sealed interface</span>
<span class="kd">public</span> <span class="n">record</span> <span class="nc">CreditCard</span><span class="o">(</span><span class="nc">String</span> <span class="n">cardNumber</span><span class="o">,</span> <span class="nc">YearMonth</span> <span class="n">expiryDate</span><span class="o">,</span> <span class="nc">BigDecimal</span> <span class="n">amount</span><span class="o">)</span>
        <span class="kd">implements</span> <span class="nc">PaymentMethod</span> <span class="o">{</span>
    <span class="kd">public</span> <span class="nf">CreditCard</span> <span class="o">{</span>
        <span class="c1">// Compact constructor for validation</span>
        <span class="nc">Objects</span><span class="o">.</span><span class="na">requireNonNull</span><span class="o">(</span><span class="n">cardNumber</span><span class="o">,</span> <span class="s">"Card number cannot be null"</span><span class="o">);</span>
        <span class="k">if</span> <span class="o">(</span><span class="n">cardNumber</span><span class="o">.</span><span class="na">length</span><span class="o">()</span> <span class="o">!=</span> <span class="mi">16</span> <span class="o">||</span> <span class="o">!</span><span class="n">cardNumber</span><span class="o">.</span><span class="na">matches</span><span class="o">(</span><span class="s">"\\d+"</span><span class="o">))</span> <span class="o">{</span>
            <span class="k">throw</span> <span class="k">new</span> <span class="nf">IllegalArgumentException</span><span class="o">(</span><span class="s">"Card number must be 16 digits"</span><span class="o">);</span>
        <span class="o">}</span>
        <span class="c1">// ... more validation</span>
    <span class="o">}</span>
<span class="o">}</span>

<span class="c1">// BankTransfer.java</span>
<span class="kd">public</span> <span class="n">record</span> <span class="nc">BankTransfer</span><span class="o">(</span><span class="nc">String</span> <span class="n">iban</span><span class="o">,</span> <span class="nc">String</span> <span class="n">bankCode</span><span class="o">,</span> <span class="nc">BigDecimal</span> <span class="n">amount</span><span class="o">)</span>
        <span class="kd">implements</span> <span class="nc">PaymentMethod</span> <span class="o">{</span> <span class="c1">/* validation */</span> <span class="o">}</span>

<span class="c1">// DigitalWallet.java</span>
<span class="kd">public</span> <span class="n">record</span> <span class="nc">DigitalWallet</span><span class="o">(</span><span class="nc">String</span> <span class="n">provider</span><span class="o">,</span> <span class="nc">String</span> <span class="n">accountId</span><span class="o">,</span> <span class="nc">BigDecimal</span> <span class="n">amount</span><span class="o">)</span>
        <span class="kd">implements</span> <span class="nc">PaymentMethod</span> <span class="o">{</span> <span class="c1">/* validation */</span> <span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/interview/preparation/payment">View full Java implementation</a></p>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// PaymentMethod.scala - All subtypes must be in same file</span>
<span class="k">sealed</span> <span class="k">trait</span> <span class="nc">PaymentMethod</span><span class="o">:</span>
  <span class="k">def</span> <span class="nf">amount</span><span class="k">:</span> <span class="kt">BigDecimal</span>

<span class="k">case</span> <span class="k">class</span> <span class="nc">CreditCard</span><span class="o">(</span><span class="n">cardNumber</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">expiryDate</span><span class="k">:</span> <span class="kt">YearMonth</span><span class="o">,</span> <span class="n">amount</span><span class="k">:</span> <span class="kt">BigDecimal</span><span class="o">)</span>
    <span class="k">extends</span> <span class="nc">PaymentMethod</span><span class="o">:</span>
  <span class="nf">require</span><span class="o">(</span><span class="n">cardNumber</span><span class="o">.</span><span class="py">length</span> <span class="o">==</span> <span class="mi">16</span> <span class="o">&amp;&amp;</span> <span class="n">cardNumber</span><span class="o">.</span><span class="py">forall</span><span class="o">(</span><span class="nv">_</span><span class="o">.</span><span class="py">isDigit</span><span class="o">),</span> <span class="s">"Card number must be 16 digits"</span><span class="o">)</span>
  <span class="nf">require</span><span class="o">(!</span><span class="n">expiryDate</span><span class="o">.</span><span class="py">isBefore</span><span class="o">(</span><span class="nv">YearMonth</span><span class="o">.</span><span class="py">now</span><span class="o">()),</span> <span class="s">"Card has expired"</span><span class="o">)</span>
  <span class="nf">require</span><span class="o">(</span><span class="n">amount</span> <span class="o">&gt;</span> <span class="mi">0</span><span class="o">,</span> <span class="s">"Amount must be positive"</span><span class="o">)</span>

<span class="k">case</span> <span class="k">class</span> <span class="nc">BankTransfer</span><span class="o">(</span><span class="n">iban</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">bankCode</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">amount</span><span class="k">:</span> <span class="kt">BigDecimal</span><span class="o">)</span>
    <span class="k">extends</span> <span class="nc">PaymentMethod</span><span class="o">:</span>
  <span class="c1">// validation with require()</span>

<span class="k">case</span> <span class="k">class</span> <span class="nc">DigitalWallet</span><span class="o">(</span><span class="n">provider</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">accountId</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">amount</span><span class="k">:</span> <span class="kt">BigDecimal</span><span class="o">)</span>
    <span class="k">extends</span> <span class="nc">PaymentMethod</span><span class="o">:</span>
  <span class="c1">// validation with require()</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/interview/preparation/payment">View full Scala implementation</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// PaymentMethod.kt - All implementations in same package/module</span>
<span class="k">sealed</span> <span class="kd">interface</span> <span class="nc">PaymentMethod</span> <span class="p">{</span>
    <span class="kd">val</span> <span class="py">amount</span><span class="p">:</span> <span class="nc">BigDecimal</span>
<span class="p">}</span>

<span class="kd">data</span> <span class="kd">class</span> <span class="nc">CreditCard</span><span class="p">(</span>
    <span class="kd">val</span> <span class="py">cardNumber</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span>
    <span class="kd">val</span> <span class="py">expiryDate</span><span class="p">:</span> <span class="nc">YearMonth</span><span class="p">,</span>
    <span class="k">override</span> <span class="kd">val</span> <span class="py">amount</span><span class="p">:</span> <span class="nc">BigDecimal</span><span class="p">,</span>
<span class="p">)</span> <span class="p">:</span> <span class="nc">PaymentMethod</span> <span class="p">{</span>
    <span class="k">init</span> <span class="p">{</span>
        <span class="nf">require</span><span class="p">(</span><span class="n">cardNumber</span><span class="p">.</span><span class="n">length</span> <span class="o">==</span> <span class="mi">16</span> <span class="o">&amp;&amp;</span> <span class="n">cardNumber</span><span class="p">.</span><span class="nf">all</span> <span class="p">{</span> <span class="k">it</span><span class="p">.</span><span class="nf">isDigit</span><span class="p">()</span> <span class="p">})</span> <span class="p">{</span>
            <span class="s">"Card number must be 16 digits"</span>
        <span class="p">}</span>
        <span class="nf">require</span><span class="p">(!</span><span class="n">expiryDate</span><span class="p">.</span><span class="nf">isBefore</span><span class="p">(</span><span class="nc">YearMonth</span><span class="p">.</span><span class="nf">now</span><span class="p">()))</span> <span class="p">{</span> <span class="s">"Card has expired"</span> <span class="p">}</span>
        <span class="nf">require</span><span class="p">(</span><span class="n">amount</span> <span class="p">&gt;</span> <span class="nc">BigDecimal</span><span class="p">.</span><span class="nc">ZERO</span><span class="p">)</span> <span class="p">{</span> <span class="s">"Amount must be positive"</span> <span class="p">}</span>
    <span class="p">}</span>
<span class="p">}</span>

<span class="kd">data</span> <span class="kd">class</span> <span class="nc">BankTransfer</span><span class="p">(</span><span class="kd">val</span> <span class="py">iban</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span> <span class="kd">val</span> <span class="py">bankCode</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span> <span class="k">override</span> <span class="kd">val</span> <span class="py">amount</span><span class="p">:</span> <span class="nc">BigDecimal</span><span class="p">)</span>
    <span class="p">:</span> <span class="nc">PaymentMethod</span> <span class="p">{</span> <span class="c1">/* validation */</span> <span class="p">}</span>

<span class="kd">data</span> <span class="kd">class</span> <span class="nc">DigitalWallet</span><span class="p">(</span><span class="kd">val</span> <span class="py">provider</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span> <span class="kd">val</span> <span class="py">accountId</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span> <span class="k">override</span> <span class="kd">val</span> <span class="py">amount</span><span class="p">:</span> <span class="nc">BigDecimal</span><span class="p">)</span>
    <span class="p">:</span> <span class="nc">PaymentMethod</span> <span class="p">{</span> <span class="c1">/* validation */</span> <span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/payment">View full Kotlin implementation</a></p>
</div>
</div>

## Exhaustive Pattern Matching for Fee Calculation

The real power of sealed classes comes from exhaustive pattern matching. Let's implement fee calculation in all three languages.

<div class="code-tabs" data-tabs-id="tabs-2">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">static</span> <span class="nc">BigDecimal</span> <span class="nf">calculateFee</span><span class="o">(</span><span class="nc">PaymentMethod</span> <span class="n">payment</span><span class="o">)</span> <span class="o">{</span>
    <span class="c1">// No default needed - compiler verifies all cases are covered</span>
    <span class="k">return</span> <span class="k">switch</span> <span class="o">(</span><span class="n">payment</span><span class="o">)</span> <span class="o">{</span>
        <span class="k">case</span> <span class="nf">CreditCard</span><span class="o">(</span><span class="kt">var</span> <span class="n">cardNumber</span><span class="o">,</span> <span class="kt">var</span> <span class="n">expiry</span><span class="o">,</span> <span class="kt">var</span> <span class="n">amount</span><span class="o">)</span> <span class="o">-&gt;</span>
            <span class="n">amount</span><span class="o">.</span><span class="na">multiply</span><span class="o">(</span><span class="k">new</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.029"</span><span class="o">)).</span><span class="na">add</span><span class="o">(</span><span class="k">new</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.30"</span><span class="o">));</span>
        <span class="k">case</span> <span class="nf">BankTransfer</span><span class="o">(</span><span class="kt">var</span> <span class="n">iban</span><span class="o">,</span> <span class="kt">var</span> <span class="n">bankCode</span><span class="o">,</span> <span class="kt">var</span> <span class="n">amount</span><span class="o">)</span> <span class="o">-&gt;</span>
            <span class="n">amount</span><span class="o">.</span><span class="na">compareTo</span><span class="o">(</span><span class="k">new</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"1000"</span><span class="o">))</span> <span class="o">&lt;</span> <span class="mi">0</span>
                <span class="o">?</span> <span class="k">new</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"2.50"</span><span class="o">)</span>
                <span class="o">:</span> <span class="k">new</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"5.00"</span><span class="o">);</span>
        <span class="k">case</span> <span class="nf">DigitalWallet</span><span class="o">(</span><span class="kt">var</span> <span class="n">provider</span><span class="o">,</span> <span class="kt">var</span> <span class="n">accountId</span><span class="o">,</span> <span class="kt">var</span> <span class="n">amount</span><span class="o">)</span> <span class="o">-&gt;</span>
            <span class="n">amount</span><span class="o">.</span><span class="na">multiply</span><span class="o">(</span><span class="k">new</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.025"</span><span class="o">)).</span><span class="na">max</span><span class="o">(</span><span class="k">new</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.50"</span><span class="o">));</span>
    <span class="o">};</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">def</span> <span class="nf">calculateFee</span><span class="o">(</span><span class="n">payment</span><span class="k">:</span> <span class="kt">PaymentMethod</span><span class="o">)</span><span class="k">:</span> <span class="kt">BigDecimal</span> <span class="o">=</span>
  <span class="n">payment</span> <span class="k">match</span>
    <span class="k">case</span> <span class="nc">CreditCard</span><span class="o">(</span><span class="k">_</span><span class="o">,</span> <span class="k">_</span><span class="o">,</span> <span class="n">amount</span><span class="o">)</span>    <span class="k">=&gt;</span>
      <span class="n">amount</span> <span class="o">*</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.029"</span><span class="o">)</span> <span class="o">+</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.30"</span><span class="o">)</span>
    <span class="k">case</span> <span class="nc">BankTransfer</span><span class="o">(</span><span class="k">_</span><span class="o">,</span> <span class="k">_</span><span class="o">,</span> <span class="n">amount</span><span class="o">)</span>  <span class="k">=&gt;</span>
      <span class="nf">if</span> <span class="n">amount</span> <span class="o">&lt;</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"1000"</span><span class="o">)</span> <span class="k">then</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"2.50"</span><span class="o">)</span> <span class="k">else</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"5.00"</span><span class="o">)</span>
    <span class="k">case</span> <span class="nc">DigitalWallet</span><span class="o">(</span><span class="k">_</span><span class="o">,</span> <span class="k">_</span><span class="o">,</span> <span class="n">amount</span><span class="o">)</span> <span class="k">=&gt;</span>
      <span class="o">(</span><span class="n">amount</span> <span class="o">*</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.025"</span><span class="o">)).</span><span class="py">max</span><span class="o">(</span><span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.50"</span><span class="o">))</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">fun</span> <span class="nf">calculateFee</span><span class="p">(</span><span class="n">payment</span><span class="p">:</span> <span class="nc">PaymentMethod</span><span class="p">):</span> <span class="nc">BigDecimal</span> <span class="p">=</span>
    <span class="k">when</span> <span class="p">(</span><span class="n">payment</span><span class="p">)</span> <span class="p">{</span>
        <span class="k">is</span> <span class="nc">CreditCard</span> <span class="p">-&gt;</span>
            <span class="n">payment</span><span class="p">.</span><span class="n">amount</span> <span class="p">*</span> <span class="nc">BigDecimal</span><span class="p">(</span><span class="s">"0.029"</span><span class="p">)</span> <span class="p">+</span> <span class="nc">BigDecimal</span><span class="p">(</span><span class="s">"0.30"</span><span class="p">)</span>
        <span class="k">is</span> <span class="nc">BankTransfer</span> <span class="p">-&gt;</span>
            <span class="k">if</span> <span class="p">(</span><span class="n">payment</span><span class="p">.</span><span class="n">amount</span> <span class="p">&lt;</span> <span class="nc">BigDecimal</span><span class="p">(</span><span class="s">"1000"</span><span class="p">))</span> <span class="nc">BigDecimal</span><span class="p">(</span><span class="s">"2.50"</span><span class="p">)</span> <span class="k">else</span> <span class="nc">BigDecimal</span><span class="p">(</span><span class="s">"5.00"</span><span class="p">)</span>
        <span class="k">is</span> <span class="nc">DigitalWallet</span> <span class="p">-&gt;</span>
            <span class="p">(</span><span class="n">payment</span><span class="p">.</span><span class="n">amount</span> <span class="p">*</span> <span class="nc">BigDecimal</span><span class="p">(</span><span class="s">"0.025"</span><span class="p">)).</span><span class="nf">max</span><span class="p">(</span><span class="nc">BigDecimal</span><span class="p">(</span><span class="s">"0.50"</span><span class="p">))</span>
    <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

### Pattern Guards

<div class="code-tabs" data-tabs-id="tabs-3">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">static</span> <span class="nc">String</span> <span class="nf">describeFee</span><span class="o">(</span><span class="nc">PaymentMethod</span> <span class="n">payment</span><span class="o">)</span> <span class="o">{</span>
    <span class="k">return</span> <span class="k">switch</span> <span class="o">(</span><span class="n">payment</span><span class="o">)</span> <span class="o">{</span>
        <span class="k">case</span> <span class="nf">CreditCard</span><span class="o">(</span><span class="kt">var</span> <span class="n">num</span><span class="o">,</span> <span class="kt">var</span> <span class="n">exp</span><span class="o">,</span> <span class="kt">var</span> <span class="n">amt</span><span class="o">)</span> <span class="n">when</span> <span class="n">amt</span><span class="o">.</span><span class="na">compareTo</span><span class="o">(</span><span class="k">new</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"100"</span><span class="o">))</span> <span class="o">&gt;</span> <span class="mi">0</span>
            <span class="o">-&gt;</span> <span class="s">"Credit card (high value): 2.9% + $0.30"</span><span class="o">;</span>
        <span class="k">case</span> <span class="nf">CreditCard</span><span class="o">(</span><span class="kt">var</span> <span class="n">num</span><span class="o">,</span> <span class="kt">var</span> <span class="n">exp</span><span class="o">,</span> <span class="kt">var</span> <span class="n">amt</span><span class="o">)</span>
            <span class="o">-&gt;</span> <span class="s">"Credit card (standard): 2.9% + $0.30"</span><span class="o">;</span>
        <span class="k">case</span> <span class="nf">BankTransfer</span><span class="o">(</span><span class="kt">var</span> <span class="n">iban</span><span class="o">,</span> <span class="kt">var</span> <span class="n">code</span><span class="o">,</span> <span class="kt">var</span> <span class="n">amt</span><span class="o">)</span> <span class="n">when</span> <span class="n">amt</span><span class="o">.</span><span class="na">compareTo</span><span class="o">(</span><span class="k">new</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"1000"</span><span class="o">))</span> <span class="o">&gt;=</span> <span class="mi">0</span>
            <span class="o">-&gt;</span> <span class="s">"Bank transfer (high value): flat $5.00"</span><span class="o">;</span>
        <span class="k">case</span> <span class="nf">BankTransfer</span><span class="o">(</span><span class="kt">var</span> <span class="n">iban</span><span class="o">,</span> <span class="kt">var</span> <span class="n">code</span><span class="o">,</span> <span class="kt">var</span> <span class="n">amt</span><span class="o">)</span>
            <span class="o">-&gt;</span> <span class="s">"Bank transfer (standard): flat $2.50"</span><span class="o">;</span>
        <span class="k">case</span> <span class="nf">DigitalWallet</span><span class="o">(</span><span class="kt">var</span> <span class="n">provider</span><span class="o">,</span> <span class="kt">var</span> <span class="n">id</span><span class="o">,</span> <span class="kt">var</span> <span class="n">amt</span><span class="o">)</span>
            <span class="o">-&gt;</span> <span class="s">"Digital wallet ("</span> <span class="o">+</span> <span class="n">provider</span> <span class="o">+</span> <span class="s">"): 2.5% (min $0.50)"</span><span class="o">;</span>
    <span class="o">};</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">def</span> <span class="nf">describeFee</span><span class="o">(</span><span class="n">payment</span><span class="k">:</span> <span class="kt">PaymentMethod</span><span class="o">)</span><span class="k">:</span> <span class="kt">String</span> <span class="o">=</span>
  <span class="n">payment</span> <span class="k">match</span>
    <span class="k">case</span> <span class="nc">CreditCard</span><span class="o">(</span><span class="n">num</span><span class="o">,</span> <span class="k">_</span><span class="o">,</span> <span class="n">amt</span><span class="o">)</span> <span class="k">if</span> <span class="n">amt</span> <span class="o">&gt;</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"100"</span><span class="o">)</span> <span class="k">=&gt;</span>
      <span class="nv">s</span><span class="s">"Credit card (high value): 2.9% + $$0.30 on ${num.takeRight(4)}"</span>
    <span class="k">case</span> <span class="nc">CreditCard</span><span class="o">(</span><span class="n">num</span><span class="o">,</span> <span class="k">_</span><span class="o">,</span> <span class="k">_</span><span class="o">)</span> <span class="k">=&gt;</span>
      <span class="nv">s</span><span class="s">"Credit card (standard): 2.9% + $$0.30 on ${num.takeRight(4)}"</span>
    <span class="k">case</span> <span class="nc">BankTransfer</span><span class="o">(</span><span class="k">_</span><span class="o">,</span> <span class="k">_</span><span class="o">,</span> <span class="n">amt</span><span class="o">)</span> <span class="k">if</span> <span class="n">amt</span> <span class="o">&gt;=</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"1000"</span><span class="o">)</span> <span class="k">=&gt;</span>
      <span class="s">"Bank transfer (high value): flat $5.00"</span>
    <span class="k">case</span> <span class="nc">BankTransfer</span><span class="o">(</span><span class="k">_</span><span class="o">,</span> <span class="k">_</span><span class="o">,</span> <span class="k">_</span><span class="o">)</span> <span class="k">=&gt;</span>
      <span class="s">"Bank transfer (standard): flat $2.50"</span>
    <span class="k">case</span> <span class="nc">DigitalWallet</span><span class="o">(</span><span class="n">provider</span><span class="o">,</span> <span class="k">_</span><span class="o">,</span> <span class="k">_</span><span class="o">)</span> <span class="k">=&gt;</span>
      <span class="nv">s</span><span class="s">"Digital wallet ($provider): 2.5% (min $$0.50)"</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">fun</span> <span class="nf">describeFee</span><span class="p">(</span><span class="n">payment</span><span class="p">:</span> <span class="nc">PaymentMethod</span><span class="p">):</span> <span class="nc">String</span> <span class="p">=</span>
    <span class="k">when</span> <span class="p">(</span><span class="n">payment</span><span class="p">)</span> <span class="p">{</span>
        <span class="k">is</span> <span class="nc">CreditCard</span> <span class="p">-&gt;</span>
            <span class="k">if</span> <span class="p">(</span><span class="n">payment</span><span class="p">.</span><span class="n">amount</span> <span class="p">&gt;</span> <span class="nc">BigDecimal</span><span class="p">(</span><span class="s">"100"</span><span class="p">))</span> <span class="p">{</span>
                <span class="s">"Credit card (high value): 2.9% + \$0.30"</span>
            <span class="p">}</span> <span class="k">else</span> <span class="p">{</span>
                <span class="s">"Credit card (standard): 2.9% + \$0.30"</span>
            <span class="p">}</span>
        <span class="k">is</span> <span class="nc">BankTransfer</span> <span class="p">-&gt;</span>
            <span class="k">if</span> <span class="p">(</span><span class="n">payment</span><span class="p">.</span><span class="n">amount</span> <span class="p">&gt;=</span> <span class="nc">BigDecimal</span><span class="p">(</span><span class="s">"1000"</span><span class="p">))</span> <span class="p">{</span>
                <span class="s">"Bank transfer (high value): flat \$5.00"</span>
            <span class="p">}</span> <span class="k">else</span> <span class="p">{</span>
                <span class="s">"Bank transfer (standard): flat \$2.50"</span>
            <span class="p">}</span>
        <span class="k">is</span> <span class="nc">DigitalWallet</span> <span class="p">-&gt;</span>
            <span class="s">"Digital wallet (${payment.provider}): 2.5% (min \$0.50)"</span>
    <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

## Comparison Table

<div class="table-wrapper" markdown="1">

| Feature | Java 17+ | Scala 3 | Kotlin |
|---------|----------|---------|--------|
| Sealed declaration | `sealed interface/class ... permits` | `sealed trait/class` | `sealed interface/class` |
| Must list subtypes | Yes (`permits`) | No (same file) | No (same package/module) |
| Record/Data class | `record` | `case class` | `data class` |
| Pattern matching | `switch` expression | `match` expression | `when` expression |
| Destructuring | Record patterns | Case class patterns | Smart casting |
| Guards | `when` clause | `if` clause | Conditions in `when` |
| No default needed | ✓ | ✓ | ✓ |

</div>

## When to Use Sealed Classes

Sealed classes are ideal for:

1. **Domain modeling** - When you have a fixed set of variants (payment types, result types, states)
2. **State machines** - Each state is a subtype with specific data
3. **Result types** - Success/Error patterns with different payloads
4. **Command patterns** - Different commands with type-safe handling

## Best Practices

1. **Keep hierarchies small** - Sealed classes work best with 3-7 subtypes
2. **Use records/data classes** - Combine sealed with immutable data carriers
3. **Validate in constructors** - Ensure invariants at construction time
4. **Leverage exhaustiveness** - Don't add default cases; let the compiler help you
5. **Document the hierarchy** - Make the permitted subtypes clear

## Conclusion

Sealed classes bring type-safe ADT (Algebraic Data Type) patterns to Java, making it easier for Scala developers to apply familiar patterns. While Java's syntax is more verbose than Scala's, the concepts are nearly identical:

- Both use sealing to restrict subtypes
- Both enable exhaustive pattern matching
- Both support destructuring in patterns
- Both provide compile-time safety

The main difference is Java's explicit `permits` clause versus Scala's same-file requirement. Kotlin sits in between, requiring same package/module.

For Scala developers transitioning to Java, sealed classes combined with records provide the closest equivalent to sealed traits with case classes.

## Code Samples

See the complete implementations in our repository:
- [Java 21 Payment System](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/interview/preparation/payment)
- [Scala 3 Payment System](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/interview/preparation/payment)
- [Kotlin Payment System](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/payment)

---

*This is part of our Java 21 Interview Preparation series. Check out the [full preparation plan](/interview/2025/11/28/java21-interview-preparation-plan.html) for more topics.*
