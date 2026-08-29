---
layout: post
title: "Strategy Pattern: Choosing Algorithms at Runtime"
description: "Learn the Strategy pattern in Java 21, Scala 2, Scala 3, and Kotlin with runtime payment-fee selection, lambda-based strategies, composition helpers, and practical tests."
date: 2026-08-29 07:00:00 +0000
updated: 2026-08-29 14:00:00 +0000
categories: [interview, best-practices]
tags: [java, java21, scala, scala2, scala3, kotlin, design-patterns, behavioral-patterns, strategy-pattern]
---

Imagine your checkout flow supports card payments, bank transfers, digital wallets, and buy-now-pay-later providers. At first, a `switch` in one service feels harmless. A few pricing exceptions later, that same service becomes the place where every fee rule, campaign tweak, and partner integration goes to die. That is exactly where the Strategy pattern helps.

## The Problem: One Service Knows Too Many Algorithms

Different payment methods often need different fee rules:

1. card payments charge a percentage plus a flat fee
2. bank transfers are cheap but usually capped
3. digital wallets may have VIP discounts and a minimum fee

If all of that logic lives in one method, the checkout service becomes harder to test, harder to extend, and harder to explain in an interview.

## Key Concepts

<div class="table-wrapper" markdown="1">

| Concept | In this example | Why it matters |
|---------|-----------------|----------------|
| Strategy | `PaymentFeeStrategy` | Encapsulates one fee algorithm behind one contract |
| Context | `PaymentFeeService` | Delegates to the selected strategy instead of branching everywhere |
| Input | `PaymentRequest` | Keeps payment method, amount, and VIP status together |
| Composition | `withMinimumFee()` / `withCap()` | Lets you layer small pricing rules without rewriting core algorithms |

</div>

## Real Use Case: Payment Fees in a Checkout Platform

Suppose your platform must quote fees before a customer confirms payment:

1. **Card**: `2.9% + 0.30`
2. **Bank transfer**: `0.8%`, capped at `7.50`
3. **Digital wallet**: `1.7%`, or `0.85%` for VIP customers, with a minimum fee of `0.25`

That is a strong Strategy fit because the calling code wants one thing — a fee quote — while the algorithm depends on runtime input.

## Component Walkthrough: What Each Part Does

1. `PaymentRequest` carries the payment method, amount, currency, and VIP flag.
2. `PaymentFeeStrategy` defines the shared algorithm contract.
3. Each concrete strategy is implemented as a lambda or function object.
4. `PaymentFeeService` stores the configured strategies and picks the right one at runtime.
5. `FeeQuote` returns a simple response object with base amount, fee, and total.

## Request Flow: How the Strategy Gets Chosen

1. The client creates a `PaymentRequest`.
2. `PaymentFeeService.quote(request)` reads `request.paymentMethod`.
3. The service looks up the matching strategy in its registry.
4. The selected strategy calculates the fee.
5. The service wraps the result in a `FeeQuote`.

The important part is what does **not** happen: the client does not know whether the fee came from card logic, bank-transfer logic, or a promotional lambda.

## The Solution: Strategy Across JVM Languages

This example uses a payment-fee registry so the context can switch algorithms without a growing `if/else` chain.

<div class="code-tabs" data-tabs-id="strategy-impl">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@FunctionalInterface</span>
<span class="kd">interface</span> <span class="nc">PaymentFeeStrategy</span> <span class="o">{</span>
    <span class="nc">BigDecimal</span> <span class="nf">calculateFee</span><span class="o">(</span><span class="nc">PaymentRequest</span> <span class="n">request</span><span class="o">);</span>

    <span class="k">default</span> <span class="nc">PaymentFeeStrategy</span> <span class="nf">withMinimumFee</span><span class="o">(</span><span class="nc">BigDecimal</span> <span class="n">minimumFee</span><span class="o">)</span> <span class="o">{</span>
        <span class="k">return</span> <span class="n">request</span> <span class="o">-&gt;</span> <span class="nc">PaymentFeeService</span><span class="o">.</span><span class="na">scale</span><span class="o">(</span><span class="n">calculateFee</span><span class="o">(</span><span class="n">request</span><span class="o">).</span><span class="na">max</span><span class="o">(</span><span class="n">minimumFee</span><span class="o">));</span>
    <span class="o">}</span>
<span class="o">}</span>

<span class="kd">public</span> <span class="kd">static</span> <span class="nc">PaymentFeeService</span> <span class="nf">defaultService</span><span class="o">()</span> <span class="o">{</span>
    <span class="nc">PaymentFeeStrategy</span> <span class="n">card</span> <span class="o">=</span> <span class="n">request</span> <span class="o">-&gt;</span>
            <span class="n">scale</span><span class="o">(</span><span class="n">request</span><span class="o">.</span><span class="na">amount</span><span class="o">().</span><span class="na">multiply</span><span class="o">(</span><span class="n">money</span><span class="o">(</span><span class="s">"0.029"</span><span class="o">)).</span><span class="na">add</span><span class="o">(</span><span class="n">money</span><span class="o">(</span><span class="s">"0.30"</span><span class="o">)));</span>
    <span class="nc">PaymentFeeStrategy</span> <span class="n">bankTransfer</span> <span class="o">=</span> <span class="n">request</span> <span class="o">-&gt;</span>
            <span class="n">scale</span><span class="o">(</span><span class="n">request</span><span class="o">.</span><span class="na">amount</span><span class="o">().</span><span class="na">multiply</span><span class="o">(</span><span class="n">money</span><span class="o">(</span><span class="s">"0.008"</span><span class="o">)));</span>
    <span class="nc">PaymentFeeStrategy</span> <span class="n">digitalWallet</span> <span class="o">=</span> <span class="n">request</span> <span class="o">-&gt;</span>
            <span class="n">scale</span><span class="o">(</span><span class="n">request</span><span class="o">.</span><span class="na">vipCustomer</span><span class="o">()</span>
                    <span class="o">?</span> <span class="n">request</span><span class="o">.</span><span class="na">amount</span><span class="o">().</span><span class="na">multiply</span><span class="o">(</span><span class="n">money</span><span class="o">(</span><span class="s">"0.0085"</span><span class="o">))</span>
                    <span class="o">:</span> <span class="n">request</span><span class="o">.</span><span class="na">amount</span><span class="o">().</span><span class="na">multiply</span><span class="o">(</span><span class="n">money</span><span class="o">(</span><span class="s">"0.017"</span><span class="o">)));</span>

    <span class="k">return</span> <span class="k">new</span> <span class="nf">PaymentFeeService</span><span class="o">(</span><span class="nc">Map</span><span class="o">.</span><span class="na">of</span><span class="o">(</span>
            <span class="nc">PaymentMethod</span><span class="o">.</span><span class="na">CARD</span><span class="o">,</span> <span class="n">card</span><span class="o">,</span>
            <span class="nc">PaymentMethod</span><span class="o">.</span><span class="na">BANK_TRANSFER</span><span class="o">,</span> <span class="n">bankTransfer</span><span class="o">.</span><span class="na">withCap</span><span class="o">(</span><span class="n">money</span><span class="o">(</span><span class="s">"7.50"</span><span class="o">)),</span>
            <span class="nc">PaymentMethod</span><span class="o">.</span><span class="na">DIGITAL_WALLET</span><span class="o">,</span> <span class="n">digitalWallet</span><span class="o">.</span><span class="na">withMinimumFee</span><span class="o">(</span><span class="n">money</span><span class="o">(</span><span class="s">"0.25"</span><span class="o">))));</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">fun</span> <span class="nf">interface</span> <span class="nc">PaymentFeeStrategy</span> <span class="p">{</span>
    <span class="k">fun</span> <span class="nf">calculateFee</span><span class="p">(</span><span class="n">request</span><span class="p">:</span> <span class="nc">PaymentRequest</span><span class="p">):</span> <span class="nc">BigDecimal</span>

    <span class="k">fun</span> <span class="nf">withMinimumFee</span><span class="p">(</span><span class="n">minimumFee</span><span class="p">:</span> <span class="nc">BigDecimal</span><span class="p">):</span> <span class="nc">PaymentFeeStrategy</span> <span class="p">=</span>
        <span class="nc">PaymentFeeStrategy</span> <span class="p">{</span> <span class="n">request</span> <span class="p">-&gt;</span> <span class="nf">calculateFee</span><span class="p">(</span><span class="n">request</span><span class="p">).</span><span class="nf">max</span><span class="p">(</span><span class="n">minimumFee</span><span class="p">).</span><span class="nf">scaled</span><span class="p">()</span> <span class="p">}</span>
<span class="p">}</span>

<span class="k">fun</span> <span class="nf">defaultService</span><span class="p">():</span> <span class="nc">PaymentFeeService</span> <span class="p">{</span>
    <span class="kd">val</span> <span class="py">card</span> <span class="p">=</span> <span class="nc">PaymentFeeStrategy</span> <span class="p">{</span> <span class="n">request</span> <span class="p">-&gt;</span>
        <span class="n">request</span><span class="p">.</span><span class="n">amount</span><span class="p">.</span><span class="nf">multiply</span><span class="p">(</span><span class="nf">money</span><span class="p">(</span><span class="s">"0.029"</span><span class="p">)).</span><span class="nf">add</span><span class="p">(</span><span class="nf">money</span><span class="p">(</span><span class="s">"0.30"</span><span class="p">)).</span><span class="nf">scaled</span><span class="p">()</span>
    <span class="p">}</span>
    <span class="kd">val</span> <span class="py">bankTransfer</span> <span class="p">=</span> <span class="nc">PaymentFeeStrategy</span> <span class="p">{</span> <span class="n">request</span> <span class="p">-&gt;</span>
        <span class="n">request</span><span class="p">.</span><span class="n">amount</span><span class="p">.</span><span class="nf">multiply</span><span class="p">(</span><span class="nf">money</span><span class="p">(</span><span class="s">"0.008"</span><span class="p">)).</span><span class="nf">scaled</span><span class="p">()</span>
    <span class="p">}</span>
    <span class="kd">val</span> <span class="py">digitalWallet</span> <span class="p">=</span> <span class="nc">PaymentFeeStrategy</span> <span class="p">{</span> <span class="n">request</span> <span class="p">-&gt;</span>
        <span class="kd">val</span> <span class="py">rate</span> <span class="p">=</span> <span class="k">if</span> <span class="p">(</span><span class="n">request</span><span class="p">.</span><span class="n">vipCustomer</span><span class="p">)</span> <span class="nf">money</span><span class="p">(</span><span class="s">"0.0085"</span><span class="p">)</span> <span class="k">else</span> <span class="nf">money</span><span class="p">(</span><span class="s">"0.017"</span><span class="p">)</span>
        <span class="n">request</span><span class="p">.</span><span class="n">amount</span><span class="p">.</span><span class="nf">multiply</span><span class="p">(</span><span class="n">rate</span><span class="p">).</span><span class="nf">scaled</span><span class="p">()</span>
    <span class="p">}</span>

    <span class="k">return</span> <span class="nc">PaymentFeeService</span><span class="p">(</span>
        <span class="nf">mapOf</span><span class="p">(</span>
            <span class="nc">PaymentMethod</span><span class="p">.</span><span class="nc">CARD</span> <span class="n">to</span> <span class="n">card</span><span class="p">,</span>
            <span class="nc">PaymentMethod</span><span class="p">.</span><span class="nc">BANK_TRANSFER</span> <span class="n">to</span> <span class="n">bankTransfer</span><span class="p">.</span><span class="nf">withCap</span><span class="p">(</span><span class="nf">money</span><span class="p">(</span><span class="s">"7.50"</span><span class="p">)),</span>
            <span class="nc">PaymentMethod</span><span class="p">.</span><span class="nc">DIGITAL_WALLET</span> <span class="n">to</span> <span class="n">digitalWallet</span><span class="p">.</span><span class="nf">withMinimumFee</span><span class="p">(</span><span class="nf">money</span><span class="p">(</span><span class="s">"0.25"</span><span class="p">)),</span>
        <span class="p">),</span>
    <span class="p">)</span>
<span class="p">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">trait</span> <span class="nc">PaymentFeeStrategy</span> <span class="o">{</span>
  <span class="k">def</span> <span class="nf">calculateFee</span><span class="o">(</span><span class="n">request</span><span class="k">:</span> <span class="kt">PaymentRequest</span><span class="o">)</span><span class="k">:</span> <span class="kt">BigDecimal</span>

  <span class="k">def</span> <span class="nf">withMinimumFee</span><span class="o">(</span><span class="n">minimumFee</span><span class="k">:</span> <span class="kt">BigDecimal</span><span class="o">)</span><span class="k">:</span> <span class="kt">PaymentFeeStrategy</span> <span class="o">=</span>
    <span class="nc">PaymentFeeStrategy</span><span class="o">(</span><span class="n">request</span> <span class="k">=&gt;</span> <span class="nv">PaymentFeeService</span><span class="o">.</span><span class="py">scale</span><span class="o">(</span><span class="nf">calculateFee</span><span class="o">(</span><span class="n">request</span><span class="o">).</span><span class="py">max</span><span class="o">(</span><span class="n">minimumFee</span><span class="o">)))</span>
<span class="o">}</span>

<span class="k">object</span> <span class="nc">PaymentFeeService</span> <span class="o">{</span>
  <span class="k">def</span> <span class="nf">defaultService</span><span class="k">:</span> <span class="kt">PaymentFeeService</span> <span class="o">=</span> <span class="o">{</span>
    <span class="k">val</span> <span class="nv">card</span> <span class="k">=</span>
      <span class="nc">PaymentFeeStrategy</span><span class="o">(</span><span class="n">request</span> <span class="k">=&gt;</span> <span class="nf">scale</span><span class="o">(</span><span class="nv">request</span><span class="o">.</span><span class="py">amount</span> <span class="o">*</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.029"</span><span class="o">)</span> <span class="o">+</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.30"</span><span class="o">)))</span>
    <span class="k">val</span> <span class="nv">bankTransfer</span> <span class="k">=</span>
      <span class="nc">PaymentFeeStrategy</span><span class="o">(</span><span class="n">request</span> <span class="k">=&gt;</span> <span class="nf">scale</span><span class="o">(</span><span class="nv">request</span><span class="o">.</span><span class="py">amount</span> <span class="o">*</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.008"</span><span class="o">)))</span>
    <span class="k">val</span> <span class="nv">digitalWallet</span> <span class="k">=</span> <span class="nc">PaymentFeeStrategy</span> <span class="o">{</span> <span class="n">request</span> <span class="k">=&gt;</span>
      <span class="k">val</span> <span class="nv">rate</span> <span class="k">=</span> <span class="nf">if</span> <span class="o">(</span><span class="nv">request</span><span class="o">.</span><span class="py">vipCustomer</span><span class="o">)</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.0085"</span><span class="o">)</span> <span class="k">else</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.017"</span><span class="o">)</span>
      <span class="nf">scale</span><span class="o">(</span><span class="nv">request</span><span class="o">.</span><span class="py">amount</span> <span class="o">*</span> <span class="n">rate</span><span class="o">)</span>
    <span class="o">}</span>

    <span class="k">new</span> <span class="nc">PaymentFeeService</span><span class="o">(</span>
      <span class="nc">Map</span><span class="o">(</span>
        <span class="nc">CARD</span>           <span class="o">-&gt;</span> <span class="n">card</span><span class="o">,</span>
        <span class="nc">BANK_TRANSFER</span>  <span class="o">-&gt;</span> <span class="nv">bankTransfer</span><span class="o">.</span><span class="py">withCap</span><span class="o">(</span><span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"7.50"</span><span class="o">)),</span>
        <span class="nc">DIGITAL_WALLET</span> <span class="o">-&gt;</span> <span class="nv">digitalWallet</span><span class="o">.</span><span class="py">withMinimumFee</span><span class="o">(</span><span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.25"</span><span class="o">))</span>
      <span class="o">)</span>
    <span class="o">)</span>
  <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">trait</span> <span class="nc">PaymentFeeStrategy</span><span class="k">:</span>
  <span class="kt">def</span> <span class="kt">calculateFee</span><span class="o">(</span><span class="kt">request:</span> <span class="kt">PaymentRequest</span><span class="o">)</span><span class="kt">:</span> <span class="kt">BigDecimal</span>

  <span class="k">def</span> <span class="nf">withMinimumFee</span><span class="o">(</span><span class="n">minimumFee</span><span class="k">:</span> <span class="kt">BigDecimal</span><span class="o">)</span><span class="k">:</span> <span class="kt">PaymentFeeStrategy</span> <span class="o">=</span>
    <span class="nc">PaymentFeeStrategy</span><span class="o">(</span><span class="n">request</span> <span class="k">=&gt;</span> <span class="nv">PaymentFeeService</span><span class="o">.</span><span class="py">scale</span><span class="o">(</span><span class="nf">calculateFee</span><span class="o">(</span><span class="n">request</span><span class="o">).</span><span class="py">max</span><span class="o">(</span><span class="n">minimumFee</span><span class="o">)))</span>

<span class="k">object</span> <span class="nc">PaymentFeeService</span><span class="k">:</span>
  <span class="kt">def</span> <span class="kt">defaultService:</span> <span class="kt">PaymentFeeService</span> <span class="o">=</span>
    <span class="k">val</span> <span class="nv">card</span> <span class="k">=</span>
      <span class="nc">PaymentFeeStrategy</span><span class="o">(</span><span class="n">request</span> <span class="k">=&gt;</span> <span class="nf">scale</span><span class="o">(</span><span class="nv">request</span><span class="o">.</span><span class="py">amount</span> <span class="o">*</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.029"</span><span class="o">)</span> <span class="o">+</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.30"</span><span class="o">)))</span>
    <span class="k">val</span> <span class="nv">bankTransfer</span> <span class="k">=</span>
      <span class="nc">PaymentFeeStrategy</span><span class="o">(</span><span class="n">request</span> <span class="k">=&gt;</span> <span class="nf">scale</span><span class="o">(</span><span class="nv">request</span><span class="o">.</span><span class="py">amount</span> <span class="o">*</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.008"</span><span class="o">)))</span>
    <span class="k">val</span> <span class="nv">digitalWallet</span> <span class="k">=</span> <span class="nc">PaymentFeeStrategy</span><span class="k">:</span> <span class="kt">request</span> <span class="o">=&gt;</span>
      <span class="k">val</span> <span class="nv">rate</span> <span class="k">=</span> <span class="k">if</span> <span class="nv">request</span><span class="o">.</span><span class="py">vipCustomer</span> <span class="n">then</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.0085"</span><span class="o">)</span> <span class="k">else</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.017"</span><span class="o">)</span>
      <span class="nf">scale</span><span class="o">(</span><span class="nv">request</span><span class="o">.</span><span class="py">amount</span> <span class="o">*</span> <span class="n">rate</span><span class="o">)</span>

    <span class="k">new</span> <span class="nc">PaymentFeeService</span><span class="o">(</span>
      <span class="nc">Map</span><span class="o">(</span>
        <span class="nv">PaymentMethod</span><span class="o">.</span><span class="py">CARD</span>           <span class="o">-&gt;</span> <span class="n">card</span><span class="o">,</span>
        <span class="nv">PaymentMethod</span><span class="o">.</span><span class="py">BANK_TRANSFER</span>  <span class="o">-&gt;</span> <span class="nv">bankTransfer</span><span class="o">.</span><span class="py">withCap</span><span class="o">(</span><span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"7.50"</span><span class="o">)),</span>
        <span class="nv">PaymentMethod</span><span class="o">.</span><span class="py">DIGITAL_WALLET</span> <span class="o">-&gt;</span> <span class="nv">digitalWallet</span><span class="o">.</span><span class="py">withMinimumFee</span><span class="o">(</span><span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"0.25"</span><span class="o">))</span>
      <span class="o">)</span>
    <span class="o">)</span>
</code></pre></div></div>
</div>
</div>

### Scala Developer Mental Model

- **Java 21** uses a functional interface, so Strategy can look object-oriented or lambda-based.
- **Scala 2/3** already treats functions as values, so Strategy often feels like “pass a function with a meaningful domain name”.
- **Kotlin** sits in the middle: `fun interface` keeps the Strategy vocabulary while still feeling lightweight.

## Testing the Strategy: Proving Runtime Swapping Works

The most useful Strategy test is not just “does card math work?” It is “can I swap in a different algorithm without changing the calling code?”

<div class="code-tabs" data-tabs-id="strategy-test">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="kt">void</span> <span class="nf">shouldAllowLambdaBasedCustomStrategies</span><span class="o">()</span> <span class="o">{</span>
    <span class="kt">var</span> <span class="n">service</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">PaymentFeeService</span><span class="o">(</span><span class="nc">Map</span><span class="o">.</span><span class="na">of</span><span class="o">(</span>
            <span class="nc">PaymentMethod</span><span class="o">.</span><span class="na">BUY_NOW_PAY_LATER</span><span class="o">,</span>
            <span class="n">request</span> <span class="o">-&gt;</span> <span class="k">new</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"9.00"</span><span class="o">)));</span>
    <span class="kt">var</span> <span class="n">request</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">PaymentRequest</span><span class="o">(</span><span class="nc">PaymentMethod</span><span class="o">.</span><span class="na">BUY_NOW_PAY_LATER</span><span class="o">,</span> <span class="k">new</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"200.00"</span><span class="o">),</span> <span class="s">"GBP"</span><span class="o">,</span> <span class="kc">false</span><span class="o">);</span>

    <span class="kt">var</span> <span class="n">quote</span> <span class="o">=</span> <span class="n">service</span><span class="o">.</span><span class="na">quote</span><span class="o">(</span><span class="n">request</span><span class="o">);</span>

    <span class="n">assertEquals</span><span class="o">(</span><span class="k">new</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"9.00"</span><span class="o">),</span> <span class="n">quote</span><span class="o">.</span><span class="na">fee</span><span class="o">());</span>
    <span class="n">assertEquals</span><span class="o">(</span><span class="k">new</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"209.00"</span><span class="o">),</span> <span class="n">quote</span><span class="o">.</span><span class="na">totalAmount</span><span class="o">());</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="k">fun</span> <span class="nf">shouldAllowLambdaBasedCustomStrategies</span><span class="p">()</span> <span class="p">{</span>
    <span class="kd">val</span> <span class="py">service</span> <span class="p">=</span> <span class="nc">PaymentFeeService</span><span class="p">(</span>
        <span class="nf">mapOf</span><span class="p">(</span>
            <span class="nc">PaymentMethod</span><span class="p">.</span><span class="nc">BUY_NOW_PAY_LATER</span> <span class="n">to</span> <span class="nc">PaymentFeeStrategy</span> <span class="p">{</span> <span class="nc">BigDecimal</span><span class="p">(</span><span class="s">"9.00"</span><span class="p">)</span> <span class="p">},</span>
        <span class="p">),</span>
    <span class="p">)</span>
    <span class="kd">val</span> <span class="py">request</span> <span class="p">=</span> <span class="nc">PaymentRequest</span><span class="p">(</span><span class="nc">PaymentMethod</span><span class="p">.</span><span class="nc">BUY_NOW_PAY_LATER</span><span class="p">,</span> <span class="nc">BigDecimal</span><span class="p">(</span><span class="s">"200.00"</span><span class="p">),</span> <span class="s">"GBP"</span><span class="p">,</span> <span class="k">false</span><span class="p">)</span>

    <span class="kd">val</span> <span class="py">quote</span> <span class="p">=</span> <span class="n">service</span><span class="p">.</span><span class="nf">quote</span><span class="p">(</span><span class="n">request</span><span class="p">)</span>

    <span class="nf">assertEquals</span><span class="p">(</span><span class="nc">BigDecimal</span><span class="p">(</span><span class="s">"9.00"</span><span class="p">),</span> <span class="n">quote</span><span class="p">.</span><span class="n">fee</span><span class="p">)</span>
    <span class="nf">assertEquals</span><span class="p">(</span><span class="nc">BigDecimal</span><span class="p">(</span><span class="s">"209.00"</span><span class="p">),</span> <span class="n">quote</span><span class="p">.</span><span class="n">totalAmount</span><span class="p">)</span>
<span class="p">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nf">test</span><span class="o">(</span><span class="s">"Strategy should allow lambda-based custom strategies"</span><span class="o">)</span> <span class="o">{</span>
  <span class="k">val</span> <span class="nv">service</span> <span class="k">=</span> <span class="k">new</span> <span class="nc">PaymentFeeService</span><span class="o">(</span>
    <span class="nc">Map</span><span class="o">(</span><span class="nc">BUY_NOW_PAY_LATER</span> <span class="o">-&gt;</span> <span class="nc">PaymentFeeStrategy</span><span class="o">(</span><span class="k">_</span> <span class="k">=&gt;</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"9.00"</span><span class="o">)))</span>
  <span class="o">)</span>
  <span class="k">val</span> <span class="nv">quote</span> <span class="k">=</span> <span class="nv">service</span><span class="o">.</span><span class="py">quote</span><span class="o">(</span>
    <span class="nc">PaymentRequest</span><span class="o">(</span><span class="nc">BUY_NOW_PAY_LATER</span><span class="o">,</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"200.00"</span><span class="o">),</span> <span class="s">"GBP"</span><span class="o">,</span> <span class="n">vipCustomer</span> <span class="k">=</span> <span class="kc">false</span><span class="o">)</span>
  <span class="o">)</span>

  <span class="nv">quote</span><span class="o">.</span><span class="py">fee</span> <span class="n">shouldBe</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"9.00"</span><span class="o">)</span>
  <span class="nv">quote</span><span class="o">.</span><span class="py">totalAmount</span> <span class="n">shouldBe</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"209.00"</span><span class="o">)</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nf">test</span><span class="o">(</span><span class="s">"Strategy should allow lambda-based custom strategies"</span><span class="o">)</span> <span class="o">{</span>
  <span class="k">val</span> <span class="nv">service</span> <span class="k">=</span> <span class="k">new</span> <span class="nc">PaymentFeeService</span><span class="o">(</span>
    <span class="nc">Map</span><span class="o">(</span><span class="nv">PaymentMethod</span><span class="o">.</span><span class="py">BUY_NOW_PAY_LATER</span> <span class="o">-&gt;</span> <span class="nc">PaymentFeeStrategy</span><span class="o">(</span><span class="k">_</span> <span class="k">=&gt;</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"9.00"</span><span class="o">)))</span>
  <span class="o">)</span>
  <span class="k">val</span> <span class="nv">quote</span> <span class="k">=</span> <span class="nv">service</span><span class="o">.</span><span class="py">quote</span><span class="o">(</span>
    <span class="nc">PaymentRequest</span><span class="o">(</span><span class="nv">PaymentMethod</span><span class="o">.</span><span class="py">BUY_NOW_PAY_LATER</span><span class="o">,</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"200.00"</span><span class="o">),</span> <span class="s">"GBP"</span><span class="o">,</span> <span class="n">vipCustomer</span> <span class="k">=</span> <span class="kc">false</span><span class="o">)</span>
  <span class="o">)</span>

  <span class="nv">quote</span><span class="o">.</span><span class="py">fee</span> <span class="n">shouldBe</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"9.00"</span><span class="o">)</span>
  <span class="nv">quote</span><span class="o">.</span><span class="py">totalAmount</span> <span class="n">shouldBe</span> <span class="nc">BigDecimal</span><span class="o">(</span><span class="s">"209.00"</span><span class="o">)</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
</div>

## Comparison: Java 21 vs Scala 2 vs Scala 3 vs Kotlin

<div class="table-wrapper" markdown="1">

| Language | Strategy shape | Best part here | Trade-off |
|----------|----------------|----------------|-----------|
| Java 21 | `@FunctionalInterface` + `Map` registry | Explicit and interview-friendly | More ceremony than Scala/Kotlin |
| Scala 2 | trait + function-valued companion | Very natural functional composition | Older syntax is slightly noisier |
| Scala 3 | trait + indentation syntax | Strategy reads almost like DSL code | Teams need to be comfortable with Scala 3 style |
| Kotlin | `fun interface` + concise lambdas | Lightweight with clear OO naming | `BigDecimal` math is still Java-flavored |

</div>

## When to Use / Best Practices

1. Use Strategy when the **algorithm varies but the caller should stay stable**.
2. Prefer small, focused strategies over one strategy object with its own branching.
3. Keep selection logic in one place, often a registry or dependency-injected map.
4. Add composition helpers like `withCap()` and `withMinimumFee()` when pricing rules layer naturally.
5. Do not force Strategy for one-off conditionals that are unlikely to grow.

## Interview Q&A: Strategy Pattern in Practice

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>How would you implement different payment processing strategies?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Start with one shared contract such as <code>PaymentFeeStrategy</code>, then implement one algorithm per payment method and store them behind a selector. The real benefit is that checkout code calls one method while the fee logic changes independently. For example, card, wallet, and bank-transfer pricing can all live behind the same <code>quote()</code> API.
    </div>
  </details>
  <details class="faq-item" open>
    <summary>
      <span>What is the relationship between the Strategy pattern and lambda expressions?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      A lambda is often the smallest possible Strategy implementation. The pattern is still the same: one interchangeable algorithm behind one contract. In Java 21, Kotlin, and Scala, a simple pricing rule can be written as a lambda instead of a named class, which keeps the code shorter without losing the design idea.
    </div>
  </details>
  <details class="faq-item" open>
    <summary>
      <span>When should you use Strategy instead of plain if-else statements?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Use Strategy when the branching is likely to grow, needs separate tests, or changes for business reasons over time. A tiny two-branch condition is fine as an <code>if</code>. But once each branch has its own rules, caps, discounts, or integrations, Strategy keeps the system easier to extend safely.
    </div>
  </details>
  <details class="faq-item" open>
    <summary>
      <span>Can you compose strategies together?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Yes, and composition is often where Strategy becomes really useful. You can take one base algorithm and wrap it with extra rules such as minimum fees, caps, or promotional adjustments. In this example, <code>withMinimumFee()</code> and <code>withCap()</code> let you layer policy without rewriting the core calculation.
    </div>
  </details>
  <details class="faq-item" open>
    <summary>
      <span>How does dependency injection relate to the Strategy pattern?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Dependency injection is often the delivery mechanism for strategies. Instead of hard-coding which algorithm to use, the context receives a map or list of strategies from configuration. That means adding a new payment method can become a wiring change plus one new strategy, rather than editing the main service every time.
    </div>
  </details>
</div>

## Conclusion

The Strategy pattern is one of the clearest places where Java and Scala thinking overlap. If you are coming from Scala, think of Strategy as “give the program a named function with a stable contract”. If you are coming from Java, think of lambdas as a lighter way to implement a familiar object-oriented pattern.

## Code Samples

All examples in this post are runnable. Find them in the repository:

- [Java 21 implementation](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/designpatterns/strategy/PaymentFeeService.java)
- [Java 21 tests](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/designpatterns/strategy/PaymentFeeServiceTest.java)
- [Kotlin implementation](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/designpatterns/strategy/PaymentFeeService.kt)
- [Kotlin tests](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/designpatterns/strategy/PaymentFeeServiceTest.kt)
- [Scala 2 implementation](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/main/scala/io/github/sps23/designpatterns/strategy/PaymentFeeService.scala)
- [Scala 2 tests](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/test/scala/io/github/sps23/designpatterns/strategy/PaymentFeeServiceTest.scala)
- [Scala 3 implementation](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/designpatterns/strategy/PaymentFeeService.scala)
- [Scala 3 tests](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/designpatterns/strategy/PaymentFeeServiceTest.scala)

---

*This post is part of the [Design Patterns in JVM Languages - Your Guide to the Top 10]({{ site.baseurl }}{% link _posts/2026-07-26-design-patterns-guide-jvm.md %}). Nearby related posts from the same guide: [Facade Pattern: Simplifying Complex Systems]({{ site.baseurl }}{% link _posts/2026-08-28-design-patterns-facade.md %}) and [Decorator Pattern: Wrapping Objects with Style]({{ site.baseurl }}{% link _posts/2026-08-27-design-patterns-decorator.md %}).*
