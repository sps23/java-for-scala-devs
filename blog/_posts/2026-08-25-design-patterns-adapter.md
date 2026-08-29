---
layout: post
title: "Adapter Pattern: Making Incompatible Payment APIs Work Together"
description: "Learn the Adapter pattern with a real checkout-to-legacy-bank integration example in Java 21, Scala 2, Scala 3, and Kotlin, including production-style tests and trade-offs."
date: 2026-08-25 10:00:00 +0000
updated: 2026-08-29 14:00:00 +0000
categories: [interview, best-practices]
tags: [java, java21, scala, scala2, scala3, kotlin, design-patterns, structural-patterns, adapter-pattern]
---

Imagine your checkout service already depends on a modern `PaymentGateway` interface, but your bank integration still exposes a legacy API with different field names, status codes, and validation rules. Rewriting every client is risky, expensive, and usually not an option during a migration.

The Adapter pattern solves this by translating between the interface your application expects and the one your legacy or third-party system actually provides.

## The Problem: New Checkout, Old Banking API

In this example, the application expects a clean `charge(PaymentRequest)` contract, while the legacy bank API expects `submitPayment(clientCode, minorUnits, isoCurrency)` and returns status codes like `"00"` and `"14"`.

Without an adapter, that translation leaks into checkout code everywhere.

## Key Concepts

<div class="table-wrapper" markdown="1">

| Concept | In this example | Why it matters |
|---------|-----------------|----------------|
| Target interface | `PaymentGateway` | Keeps checkout code stable |
| Adaptee | `LegacyBankApi` | Existing dependency we cannot easily change |
| Adapter | `LegacyBankPaymentAdapter` | Maps request/response and normalizes validation |
| Client | `CheckoutService` | Depends only on the target interface |

</div>

## Real Use Case: Checkout Migration Without Downtime

Suppose your team is modernizing an e-commerce platform. The checkout service is new and clean, but payment settlement is still handled by a legacy bank integration shared by multiple systems. You cannot replace that legacy API immediately because:

1. It is already audited and certified in production.
2. Other teams still depend on it.
3. Replacing it would require a risky, big-bang migration.

The adapter gives you a safe middle path. Checkout keeps using a modern `PaymentGateway` contract, while the adapter translates every request and response to the legacy format. When you later swap the bank integration, you only replace the adapter internals, not every checkout caller.

## Component Walkthrough: What Each Part Is Doing

The key concepts table identifies the pieces; here is their operational role in this concrete implementation:

1. **`PaymentGateway` (Target Interface)** defines exactly what checkout needs: `charge(PaymentRequest)`. It protects the rest of the application from legacy protocol details.
2. **`PaymentRequest` and `PaymentResult` (Application DTOs)** model business-level input/output, not bank-specific data. They are the language your domain code understands.
3. **`LegacyBankApi` (Adaptee)** represents an external interface with historical constraints: parameter names like `clientCode` and status codes like `"00"` or `"14"`.
4. **`LegacyBankPaymentAdapter` (Translator + Guardrail)** validates input, normalizes currency, maps request fields to legacy parameters, interprets legacy status codes, and maps them back to a domain-friendly result.
5. **`CheckoutService` (Client)** depends only on `PaymentGateway`, so it never needs to know which bank provider or protocol is behind the scenes.
6. **Tests (Executable Contract)** lock the expected behavior: approved payments stay approved, legacy rejections are mapped clearly, and invalid input fails fast before hitting the legacy API.

## Request Flow: End-to-End in This Example

1. Checkout calls `charge(PaymentRequest(customerId, amountInCents, currency))`.
2. Adapter validates business rules (`customerId` not blank, amount positive, currency present).
3. Adapter normalizes and translates fields to `submitPayment(clientCode, minorUnits, isoCurrency)`.
4. Legacy API returns bank-centric response (`statusCode`, `reference`, `detail`).
5. Adapter maps it to `PaymentResult(approved, transactionId, message)` for checkout.

## The Solution: Adapter Across JVM Languages

<div class="code-tabs" data-tabs-id="tabs-adapter-impl">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">final</span> <span class="kd">class</span> <span class="nc">LegacyBankPaymentAdapter</span> <span class="kd">implements</span> <span class="nc">PaymentGateway</span> <span class="o">{</span>
    <span class="kd">private</span> <span class="kd">final</span> <span class="nc">LegacyBankApi</span> <span class="n">legacyBankApi</span><span class="o">;</span>
    <span class="nd">@Override</span>
    <span class="kd">public</span> <span class="nc">PaymentResult</span> <span class="nf">charge</span><span class="o">(</span><span class="nc">PaymentRequest</span> <span class="n">request</span><span class="o">)</span> <span class="o">{</span>
        <span class="nc">String</span> <span class="n">currency</span> <span class="o">=</span> <span class="n">request</span><span class="o">.</span><span class="na">currency</span><span class="o">().</span><span class="na">trim</span><span class="o">().</span><span class="na">toUpperCase</span><span class="o">(</span><span class="nc">Locale</span><span class="o">.</span><span class="na">ROOT</span><span class="o">);</span>
        <span class="nc">LegacyBankResponse</span> <span class="n">response</span> <span class="o">=</span> <span class="n">legacyBankApi</span><span class="o">.</span><span class="na">submitPayment</span><span class="o">(</span>
            <span class="n">request</span><span class="o">.</span><span class="na">customerId</span><span class="o">(),</span> <span class="n">request</span><span class="o">.</span><span class="na">amountInCents</span><span class="o">(),</span> <span class="n">currency</span><span class="o">);</span>
        <span class="kt">boolean</span> <span class="n">approved</span> <span class="o">=</span> <span class="s">"00"</span><span class="o">.</span><span class="na">equals</span><span class="o">(</span><span class="n">response</span><span class="o">.</span><span class="na">statusCode</span><span class="o">());</span>
        <span class="k">return</span> <span class="k">new</span> <span class="nc">PaymentResult</span><span class="o">(</span><span class="n">approved</span><span class="o">,</span> <span class="n">response</span><span class="o">.</span><span class="na">reference</span><span class="o">(),</span>
            <span class="n">approved</span> <span class="o">?</span> <span class="s">"Payment approved"</span> <span class="o">:</span> <span class="n">response</span><span class="o">.</span><span class="na">detail</span><span class="o">());</span>
    <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/designpatterns/adapter/LegacyBankPaymentAdapter.java">View in repository</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">class</span> <span class="nc">LegacyBankPaymentAdapter</span><span class="p">(</span>
    <span class="k">private</span> <span class="k">val</span> <span class="py">legacyBankApi</span><span class="p">:</span> <span class="nc">LegacyBankApi</span><span class="p">,</span>
<span class="p">)</span> <span class="p">:</span> <span class="nc">PaymentGateway</span> <span class="p">{</span>
    <span class="k">override</span> <span class="k">fun</span> <span class="nf">charge</span><span class="p">(</span><span class="n">request</span><span class="p">:</span> <span class="nc">PaymentRequest</span><span class="p">):</span> <span class="nc">PaymentResult</span> <span class="p">{</span>
        <span class="k">val</span> <span class="py">currency</span> <span class="p">=</span> <span class="n">request</span><span class="p">.</span><span class="n">currency</span><span class="p">.</span><span class="n">trim</span><span class="p">().</span><span class="n">uppercase</span><span class="p">(</span><span class="nc">Locale</span><span class="p">.</span><span class="n">ROOT</span><span class="p">)</span>
        <span class="k">val</span> <span class="py">response</span> <span class="p">=</span> <span class="n">legacyBankApi</span><span class="p">.</span><span class="nf">submitPayment</span><span class="p">(</span>
            <span class="n">request</span><span class="p">.</span><span class="n">customerId</span><span class="p">,</span> <span class="n">request</span><span class="p">.</span><span class="n">amountInCents</span><span class="p">.</span><span class="nf">toLong</span><span class="p">(),</span> <span class="n">currency</span><span class="p">)</span>
        <span class="k">val</span> <span class="py">approved</span> <span class="p">=</span> <span class="n">response</span><span class="p">.</span><span class="n">statusCode</span> <span class="o">==</span> <span class="s">"00"</span>
        <span class="k">return</span> <span class="nc">PaymentResult</span><span class="p">(</span><span class="n">approved</span><span class="p">,</span> <span class="n">response</span><span class="p">.</span><span class="n">reference</span><span class="p">,</span>
            <span class="k">if</span> <span class="p">(</span><span class="n">approved</span><span class="p">)</span> <span class="s">"Payment approved"</span> <span class="k">else</span> <span class="n">response</span><span class="p">.</span><span class="n">detail</span><span class="p">)</span>
    <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/designpatterns/adapter/LegacyBankPaymentAdapter.kt">View in repository</a></p>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">class</span> <span class="nc">LegacyBankPaymentAdapter</span><span class="o">(</span><span class="n">legacyBankApi</span><span class="k">:</span> <span class="kt">LegacyBankApi</span><span class="o">)</span> <span class="k">extends</span> <span class="nc">PaymentGateway</span> <span class="o">{</span>
  <span class="k">override</span> <span class="k">def</span> <span class="n">charge</span><span class="o">(</span><span class="n">request</span><span class="k">:</span> <span class="kt">PaymentRequest</span><span class="o">):</span> <span class="kt">PaymentResult</span> <span class="o">=</span> <span class="o">{</span>
    <span class="k">val</span> <span class="nv">currency</span> <span class="k">=</span> <span class="nf">normalizeCurrency</span><span class="o">(</span><span class="n">request</span><span class="o">.</span><span class="py">currency</span><span class="o">)</span>
    <span class="k">val</span> <span class="nv">response</span> <span class="k">=</span> <span class="n">legacyBankApi</span><span class="o">.</span><span class="py">submitPayment</span><span class="o">(</span>
      <span class="n">request</span><span class="o">.</span><span class="py">customerId</span><span class="o">,</span> <span class="n">request</span><span class="o">.</span><span class="py">amountInCents</span><span class="o">,</span> <span class="n">currency</span><span class="o">)</span>
    <span class="k">val</span> <span class="nv">approved</span> <span class="k">=</span> <span class="n">response</span><span class="o">.</span><span class="py">statusCode</span> <span class="o">==</span> <span class="s">"00"</span>
    <span class="nc">PaymentResult</span><span class="o">(</span><span class="n">approved</span><span class="o">,</span> <span class="n">response</span><span class="o">.</span><span class="py">reference</span><span class="o">,</span>
      <span class="k">if</span> <span class="o">(</span><span class="n">approved</span><span class="o">)</span> <span class="s">"Payment approved"</span> <span class="k">else</span> <span class="n">response</span><span class="o">.</span><span class="py">detail</span><span class="o">)</span>
  <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/main/scala/io/github/sps23/designpatterns/adapter/LegacyBankPaymentAdapter.scala">View in repository</a></p>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">class</span> <span class="nc">LegacyBankPaymentAdapter</span><span class="o">(</span><span class="n">legacyBankApi</span><span class="o">:</span> <span class="kt">LegacyBankApi</span><span class="o">)</span> <span class="k">extends</span> <span class="nc">PaymentGateway</span><span class="o">:</span>
  <span class="k">override</span> <span class="k">def</span> <span class="n">charge</span><span class="o">(</span><span class="n">request</span><span class="o">:</span> <span class="kt">PaymentRequest</span><span class="o">):</span> <span class="kt">PaymentResult</span> <span class="o">=</span>
    <span class="k">val</span> <span class="n">currency</span> <span class="k">=</span> <span class="n">normalizeCurrency</span><span class="o">(</span><span class="n">request</span><span class="o">.</span><span class="n">currency</span><span class="o">)</span>
    <span class="k">val</span> <span class="n">response</span> <span class="k">=</span> <span class="n">legacyBankApi</span><span class="o">.</span><span class="n">submitPayment</span><span class="o">(</span>
      <span class="n">request</span><span class="o">.</span><span class="n">customerId</span><span class="o">,</span> <span class="n">request</span><span class="o">.</span><span class="n">amountInCents</span><span class="o">,</span> <span class="n">currency</span><span class="o">)</span>
    <span class="k">val</span> <span class="n">approved</span> <span class="k">=</span> <span class="n">response</span><span class="o">.</span><span class="n">statusCode</span> <span class="o">==</span> <span class="s">"00"</span>
    <span class="nc">PaymentResult</span><span class="o">(</span><span class="n">approved</span><span class="o">,</span> <span class="n">response</span><span class="o">.</span><span class="n">reference</span><span class="o">,</span>
      <span class="k">if</span> <span class="n">approved</span> <span class="k">then</span> <span class="s">"Payment approved"</span> <span class="k">else</span> <span class="n">response</span><span class="o">.</span><span class="n">detail</span><span class="o">)</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/designpatterns/adapter/LegacyBankPaymentAdapter.scala">View in repository</a></p>
</div>
</div>

### Scala Developer Mental Model

- In **Java 21**, Adapter often appears as an explicit class implementing the target interface and wrapping a legacy dependency.
- In **Scala 2/3**, this translation layer is still explicit, but ADTs and concise case classes reduce ceremony around request and response mapping.
- In **Kotlin**, data classes plus concise null/validation handling make object adapters clean and readable.

## Comparison: Java 21 vs Scala 2 vs Scala 3 vs Kotlin

<div class="table-wrapper" markdown="1">

| Language | Adapter shape | Validation style | Mapping clarity |
|----------|---------------|------------------|-----------------|
| Java 21 | `class ... implements PaymentGateway` | Guards + exceptions | Explicit and verbose |
| Scala 2 | `class ... extends PaymentGateway` | `Option` + exceptions | Compact, expressive |
| Scala 3 | Same as Scala 2 with indentation syntax | `Option` + exceptions | Compact and modern |
| Kotlin | `class ... : PaymentGateway` | `require(...)` | Very concise |

</div>

## Testing the Adapter with a Real Checkout Scenario

The tests prove practical behavior: checkout approval, legacy rejection mapping, and fail-fast validation.

<div class="code-tabs" data-tabs-id="tabs-adapter-test">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="kt">void</span> <span class="nf">shouldApproveCheckoutPayment</span><span class="o">()</span> <span class="o">{</span>
    <span class="nc">CheckoutService</span> <span class="n">service</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">CheckoutService</span><span class="o">(</span><span class="n">paymentGateway</span><span class="o">);</span>
    <span class="nc">String</span> <span class="n">confirmation</span> <span class="o">=</span> <span class="n">service</span><span class="o">.</span><span class="na">checkout</span><span class="o">(</span><span class="s">"cust-42"</span><span class="o">,</span> <span class="mi">1599</span><span class="o">,</span> <span class="s">"eur"</span><span class="o">);</span>
    <span class="n">assertTrue</span><span class="o">(</span><span class="n">confirmation</span><span class="o">.</span><span class="na">startsWith</span><span class="o">(</span><span class="s">"CONFIRMED:TX-CUST-42-1599"</span><span class="o">));</span>
<span class="o">}</span>

<span class="nd">@Test</span>
<span class="kt">void</span> <span class="nf">shouldRejectUnsupportedCurrency</span><span class="o">()</span> <span class="o">{</span>
    <span class="nc">PaymentResult</span> <span class="n">result</span> <span class="o">=</span> <span class="n">paymentGateway</span><span class="o">.</span><span class="na">charge</span><span class="o">(</span>
        <span class="k">new</span> <span class="nc">PaymentRequest</span><span class="o">(</span><span class="s">"cust-42"</span><span class="o">,</span> <span class="mi">1599</span><span class="o">,</span> <span class="s">"pln"</span><span class="o">));</span>
    <span class="n">assertEquals</span><span class="o">(</span><span class="s">"Unsupported currency: PLN"</span><span class="o">,</span> <span class="n">result</span><span class="o">.</span><span class="na">message</span><span class="o">());</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/designpatterns/adapter/LegacyBankPaymentAdapterTest.java">View full test file</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="k">fun</span> <span class="nf">shouldApproveCheckoutPayment</span><span class="p">()</span> <span class="p">{</span>
    <span class="k">val</span> <span class="py">service</span> <span class="p">=</span> <span class="nc">CheckoutService</span><span class="p">(</span><span class="n">paymentGateway</span><span class="p">)</span>
    <span class="k">val</span> <span class="py">confirmation</span> <span class="p">=</span> <span class="n">service</span><span class="p">.</span><span class="nf">checkout</span><span class="p">(</span><span class="s">"cust-42"</span><span class="p">,</span> <span class="m">1599</span><span class="p">,</span> <span class="s">"eur"</span><span class="p">)</span>
    <span class="nf">assertTrue</span><span class="p">(</span><span class="n">confirmation</span><span class="p">.</span><span class="n">startsWith</span><span class="p">(</span><span class="s">"CONFIRMED:TX-CUST-42-1599"</span><span class="p">))</span>
<span class="p">}</span>

<span class="nd">@Test</span>
<span class="k">fun</span> <span class="nf">shouldRejectUnsupportedCurrency</span><span class="p">()</span> <span class="p">{</span>
    <span class="k">val</span> <span class="py">result</span> <span class="p">=</span> <span class="n">paymentGateway</span><span class="p">.</span><span class="nf">charge</span><span class="p">(</span><span class="nc">PaymentRequest</span><span class="p">(</span><span class="s">"cust-42"</span><span class="p">,</span> <span class="m">1599</span><span class="p">,</span> <span class="s">"pln"</span><span class="p">))</span>
    <span class="nf">assertEquals</span><span class="p">(</span><span class="s">"Unsupported currency: PLN"</span><span class="p">,</span> <span class="n">result</span><span class="p">.</span><span class="n">message</span><span class="p">)</span>
<span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/designpatterns/adapter/LegacyBankPaymentAdapterTest.kt">View full test file</a></p>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="n">test</span><span class="o">(</span><span class="s">"Adapter should approve checkout payment through legacy bank API"</span><span class="o">)</span> <span class="o">{</span>
  <span class="k">val</span> <span class="nv">service</span> <span class="k">=</span> <span class="k">new</span> <span class="nc">CheckoutService</span><span class="o">(</span><span class="n">paymentGateway</span><span class="o">)</span>
  <span class="k">val</span> <span class="nv">confirmation</span> <span class="k">=</span> <span class="n">service</span><span class="o">.</span><span class="py">checkout</span><span class="o">(</span><span class="s">"cust-42"</span><span class="o">,</span> <span class="mi">1599</span><span class="o">,</span> <span class="s">"eur"</span><span class="o">)</span>
  <span class="n">confirmation</span> <span class="n">should</span> <span class="n">startWith</span> <span class="o">(</span><span class="s">"CONFIRMED:TX-CUST-42-1599"</span><span class="o">)</span>
<span class="o">}</span>

<span class="n">test</span><span class="o">(</span><span class="s">"Adapter should reject unsupported currencies from legacy API"</span><span class="o">)</span> <span class="o">{</span>
  <span class="n">paymentGateway</span><span class="o">.</span><span class="py">charge</span><span class="o">(</span><span class="nc">PaymentRequest</span><span class="o">(</span><span class="s">"cust-42"</span><span class="o">,</span> <span class="mi">1599</span><span class="o">,</span> <span class="s">"pln"</span><span class="o">)).</span><span class="py">message</span> <span class="n">shouldBe</span>
    <span class="s">"Unsupported currency: PLN"</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/test/scala/io/github/sps23/designpatterns/adapter/LegacyBankPaymentAdapterTest.scala">View full test file</a></p>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="n">test</span><span class="o">(</span><span class="s">"Adapter should approve checkout payment through legacy bank API"</span><span class="o">):</span>
  <span class="k">val</span> <span class="n">service</span> <span class="k">=</span> <span class="nc">CheckoutService</span><span class="o">(</span><span class="n">paymentGateway</span><span class="o">)</span>
  <span class="k">val</span> <span class="n">confirmation</span> <span class="k">=</span> <span class="n">service</span><span class="o">.</span><span class="n">checkout</span><span class="o">(</span><span class="s">"cust-42"</span><span class="o">,</span> <span class="mi">1599</span><span class="o">,</span> <span class="s">"eur"</span><span class="o">)</span>
  <span class="n">confirmation</span> <span class="n">should</span> <span class="n">startWith</span> <span class="o">(</span><span class="s">"CONFIRMED:TX-CUST-42-1599"</span><span class="o">)</span>

<span class="n">test</span><span class="o">(</span><span class="s">"Adapter should reject unsupported currencies from legacy API"</span><span class="o">):</span>
  <span class="n">paymentGateway</span><span class="o">.</span><span class="n">charge</span><span class="o">(</span><span class="nc">PaymentRequest</span><span class="o">(</span><span class="s">"cust-42"</span><span class="o">,</span> <span class="mi">1599</span><span class="o">,</span> <span class="s">"pln"</span><span class="o">)).</span><span class="n">message</span> <span class="n">shouldBe</span>
    <span class="s">"Unsupported currency: PLN"</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/designpatterns/adapter/LegacyBankPaymentAdapterTest.scala">View full test file</a></p>
</div>
</div>

## When to Use Adapter Pattern

Use an adapter when:

1. You must integrate legacy or third-party APIs you cannot change.
2. Your core application interface is cleaner than the external dependency's model.
3. You want migration logic in one place instead of scattered across services.
4. You need to preserve backward compatibility during phased rewrites.

Avoid it when both interfaces are already under your control and can be unified directly.

## Interview Q&A: Adapter Pattern in Practice

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>What does the Adapter pattern do?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      An adapter helps two incompatible pieces of code work together. It keeps one side unchanged and creates a wrapper that translates the required calls into the format the other side expects. The aim is not to change the underlying library or system, but to make it fit the interface your application already uses.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>How is an Adapter different from a Facade?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      An adapter solves a mismatch between two interfaces. A facade simplifies a whole complex subsystem behind one easy entry point. So if two pieces of code cannot talk to each other because their APIs do not match, use an adapter. If a system is hard to use because it has many moving parts, use a facade.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Why not just change the legacy code directly?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Sometimes you can, but often you should not. Legacy systems, third-party libraries, and vendor APIs may be outside your control. Changing them directly can be risky, expensive, or impossible. An adapter gives you a safe layer that keeps your core application clean while reducing the impact of external code.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>What is a real-world example of an adapter?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      A common example is integrating a payment provider from another team or vendor. Your app wants to call a clean `charge(customer, amount)` method, but the provider exposes a different API with different field names and response types. The adapter translates your request into their format and converts their response back into your own model. This keeps the rest of the application simple.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>When would you avoid using an adapter?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      If both sides are already under your control and can be aligned directly, an adapter may be unnecessary. It is also a bad fit when the adapter starts doing too much business logic instead of just translating calls. The right adapter is a thin boundary: it translates, not transforms the meaning of the system.
    </div>
  </details>
</div>

## Code Samples

All examples in this post are available in the repository:

**Implementation files:**
- **Java 21:** [LegacyBankPaymentAdapter.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/designpatterns/adapter/LegacyBankPaymentAdapter.java)
- **Kotlin:** [LegacyBankPaymentAdapter.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/designpatterns/adapter/LegacyBankPaymentAdapter.kt)
- **Scala 2:** [LegacyBankPaymentAdapter.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/main/scala/io/github/sps23/designpatterns/adapter/LegacyBankPaymentAdapter.scala)
- **Scala 3:** [LegacyBankPaymentAdapter.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/designpatterns/adapter/LegacyBankPaymentAdapter.scala)

**Test files:**
- **Java 21:** [LegacyBankPaymentAdapterTest.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/designpatterns/adapter/LegacyBankPaymentAdapterTest.java)
- **Kotlin:** [LegacyBankPaymentAdapterTest.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/designpatterns/adapter/LegacyBankPaymentAdapterTest.kt)
- **Scala 2:** [LegacyBankPaymentAdapterTest.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/test/scala/io/github/sps23/designpatterns/adapter/LegacyBankPaymentAdapterTest.scala)
- **Scala 3:** [LegacyBankPaymentAdapterTest.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/designpatterns/adapter/LegacyBankPaymentAdapterTest.scala)

---

*This post is part of the [Design Patterns in JVM Languages - Your Guide to the Top 10]({{ site.baseurl }}{% link _posts/2026-07-26-design-patterns-guide-jvm.md %}). Next related posts: [Decorator Pattern: Wrapping Objects with Style]({{ site.baseurl }}{% link _posts/2026-08-27-design-patterns-decorator.md %}) and [Facade Pattern: Simplifying Complex Systems]({{ site.baseurl }}{% link _posts/2026-08-28-design-patterns-facade.md %}).*
