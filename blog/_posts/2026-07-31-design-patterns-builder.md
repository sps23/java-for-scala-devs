---
layout: post
title: "Builder Pattern: Constructing Complex Objects Clearly"
description: "Learn the Builder pattern in Java 21, Scala 2, Scala 3, and Kotlin with practical HTTP client configuration examples, validation rules, default values, and test strategies."
date: 2026-07-31 15:00:00 +0000
categories: [interview, best-practices]
tags: [java, java21, scala, scala2, scala3, kotlin, design-patterns, creational-patterns, builder-pattern]
---

Imagine your production HTTP client needs host, port, separate connect/read timeouts, retry strategy with backoff, default headers, API versioning, compression, and circuit-breaker tuning. A giant constructor quickly becomes unreadable, and every optional parameter makes call sites harder to understand.

That is exactly where the Builder pattern helps: keep required fields explicit, set optional fields fluently, and validate everything in one place before creating the final object.

## The Problem: Telescoping Constructors and Confusing Calls

Without a builder, constructor calls become fragile:

```java
new HttpClientConfig(
    "api.example.com", 443, 500, 2000, true, 3,
    List.of(100, 200, 500), Map.of("Accept", "application/json"),
    50, "v1", true
);
```

This creates three common problems:

1. **Poor readability**: it is hard to remember what each argument means.
2. **Easy mistakes**: swapping `timeout` and `maxRetries` still compiles if types match.
3. **Scattered validation**: invalid states can sneak into different constructors.

## Key Concepts

<div class="table-wrapper" markdown="1">

| Concept | What it means | Why it matters |
|---------|---------------|----------------|
| Required fields | Values needed to build a valid object (`host`, `port`) | Prevents half-built configurations |
| Optional fields | Values with safe defaults (timeouts, retries, headers, version, compression) | Keeps call sites concise |
| Fluent API | Chained method calls on builder | Improves readability |
| Central validation | Validation inside `build()` | Ensures one source of truth |
| Cross-field rules | Validate relationships (e.g. read timeout ≥ connect timeout) | Prevents invalid runtime configs |

</div>

## The Solution: Builder Across JVM Languages

Below is the same `HttpClientConfig` builder idea in Java 21, Kotlin, Scala 2, and Scala 3.

<div class="code-tabs" data-tabs-id="tabs-1">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">final</span> <span class="kd">class</span> <span class="nc">HttpClientConfig</span> <span class="o">{</span>
    <span class="kd">public</span> <span class="kd">static</span> <span class="nc">Builder</span> <span class="nf">builder</span><span class="o">(</span><span class="nc">String</span> <span class="n">host</span><span class="o">,</span> <span class="kt">int</span> <span class="n">port</span><span class="o">)</span> <span class="o">{</span>
        <span class="k">return</span> <span class="k">new</span> <span class="nc">Builder</span><span class="o">(</span><span class="n">host</span><span class="o">,</span> <span class="n">port</span><span class="o">);</span>
    <span class="o">}</span>
    <span class="kd">public</span> <span class="kd">static</span> <span class="kd">final</span> <span class="kd">class</span> <span class="nc">Builder</span> <span class="o">{</span>
        <span class="kd">private</span> <span class="kt">int</span> <span class="n">timeoutSeconds</span> <span class="o">=</span> <span class="mi">30</span><span class="o">;</span>
        <span class="kd">private</span> <span class="kt">boolean</span> <span class="n">useSsl</span> <span class="o">=</span> <span class="kc">true</span><span class="o">;</span>
        <span class="kd">public</span> <span class="nc">Builder</span> <span class="nf">timeoutSeconds</span><span class="o">(</span><span class="kt">int</span> <span class="n">value</span><span class="o">)</span> <span class="o">{</span> <span class="n">timeoutSeconds</span> <span class="o">=</span> <span class="n">value</span><span class="o">;</span> <span class="k">return</span> <span class="k">this</span><span class="o">;</span> <span class="o">}</span>
        <span class="kd">public</span> <span class="nc">Builder</span> <span class="nf">useSsl</span><span class="o">(</span><span class="kt">boolean</span> <span class="n">value</span><span class="o">)</span> <span class="o">{</span> <span class="n">useSsl</span> <span class="o">=</span> <span class="n">value</span><span class="o">;</span> <span class="k">return</span> <span class="k">this</span><span class="o">;</span> <span class="o">}</span>
        <span class="kd">public</span> <span class="nc">HttpClientConfig</span> <span class="nf">build</span><span class="o">()</span> <span class="o">{</span>
            <span class="k">if</span> <span class="o">(</span><span class="n">timeoutSeconds</span> <span class="o">&lt;=</span> <span class="mi">0</span><span class="o">)</span> <span class="k">throw</span> <span class="k">new</span> <span class="nc">IllegalArgumentException</span><span class="o">(</span><span class="s">"Timeout must be positive"</span><span class="o">);</span>
            <span class="k">return</span> <span class="k">new</span> <span class="nc">HttpClientConfig</span><span class="o">(</span><span class="k">this</span><span class="o">);</span>
        <span class="o">}</span>
    <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/designpatterns/builder/HttpClientConfig.java">View in repository</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">data</span> <span class="kd">class</span> <span class="nc">HttpClientConfig</span><span class="p">(</span>
    <span class="kd">val</span> <span class="py">host</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span>
    <span class="kd">val</span> <span class="py">port</span><span class="p">:</span> <span class="nc">Int</span><span class="p">,</span>
    <span class="kd">val</span> <span class="py">timeoutSeconds</span><span class="p">:</span> <span class="nc">Int</span> <span class="p">=</span> <span class="m">30</span>
<span class="p">)</span>
<span class="kd">class</span> <span class="nc">HttpClientConfigBuilder</span> <span class="kd">private constructor</span><span class="p">(</span><span class="kd">private</span> <span class="kd">val</span> <span class="py">host</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span> <span class="kd">private</span> <span class="kd">val</span> <span class="py">port</span><span class="p">:</span> <span class="nc">Int</span><span class="p">)</span> <span class="p">{</span>
    <span class="kd">private</span> <span class="kd">var</span> <span class="py">timeoutSeconds</span><span class="p">:</span> <span class="nc">Int</span> <span class="p">=</span> <span class="m">30</span>
    <span class="k">fun</span> <span class="nf">timeoutSeconds</span><span class="p">(</span><span class="n">value</span><span class="p">:</span> <span class="nc">Int</span><span class="p">):</span> <span class="nc">HttpClientConfigBuilder</span> <span class="p">{</span> <span class="n">timeoutSeconds</span> <span class="p">=</span> <span class="n">value</span><span class="p">;</span> <span class="k">return</span> <span class="k">this</span> <span class="p">}</span>
    <span class="k">fun</span> <span class="nf">build</span><span class="p">():</span> <span class="nc">HttpClientConfig</span> <span class="p">{</span>
        <span class="n">require</span><span class="p">(</span><span class="n">timeoutSeconds</span> <span class="p">&gt;</span> <span class="m">0</span><span class="p">)</span> <span class="p">{</span> <span class="s">"Timeout must be positive"</span> <span class="p">}</span>
        <span class="k">return</span> <span class="nc">HttpClientConfig</span><span class="p">(</span><span class="n">host</span> <span class="p">=</span> <span class="n">host</span><span class="p">,</span> <span class="n">port</span> <span class="p">=</span> <span class="n">port</span><span class="p">,</span> <span class="n">timeoutSeconds</span> <span class="p">=</span> <span class="n">timeoutSeconds</span><span class="p">)</span>
    <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/designpatterns/builder/HttpClientConfig.kt">View in repository</a></p>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">final</span> <span class="k">case</span> <span class="k">class</span> <span class="nc">HttpClientConfig</span><span class="o">(</span><span class="n">host</span><span class="o">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">port</span><span class="o">:</span> <span class="kt">Int</span><span class="o">,</span> <span class="n">timeoutSeconds</span><span class="o">:</span> <span class="kt">Int</span> <span class="o">=</span> <span class="mi">30</span><span class="o">)</span>
<span class="k">object</span> <span class="nc">HttpClientConfigBuilder</span> <span class="o">{</span>
  <span class="k">def</span> <span class="n">builder</span><span class="o">(</span><span class="n">host</span><span class="o">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">port</span><span class="o">:</span> <span class="kt">Int</span><span class="o">):</span> <span class="kt">HttpClientConfigBuilder</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">HttpClientConfigBuilder</span><span class="o">(</span><span class="n">host</span><span class="o">,</span> <span class="n">port</span><span class="o">)</span>
<span class="o">}</span>
<span class="k">final</span> <span class="k">class</span> <span class="nc">HttpClientConfigBuilder</span> <span class="k">private</span> <span class="o">(</span><span class="n">host</span><span class="o">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">port</span><span class="o">:</span> <span class="kt">Int</span><span class="o">,</span> <span class="n">timeoutSeconds</span><span class="o">:</span> <span class="kt">Int</span> <span class="o">=</span> <span class="mi">30</span><span class="o">)</span> <span class="o">{</span>
  <span class="k">def</span> <span class="n">timeoutSeconds</span><span class="o">(</span><span class="n">value</span><span class="o">:</span> <span class="kt">Int</span><span class="o">):</span> <span class="kt">HttpClientConfigBuilder</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">HttpClientConfigBuilder</span><span class="o">(</span><span class="n">host</span><span class="o">,</span> <span class="n">port</span><span class="o">,</span> <span class="n">value</span><span class="o">)</span>
  <span class="k">def</span> <span class="n">build</span><span class="o">():</span> <span class="kt">HttpClientConfig</span> <span class="o">=</span> <span class="o">{</span>
    <span class="k">if</span> <span class="o">(</span><span class="n">timeoutSeconds</span> <span class="o">&lt;=</span> <span class="mi">0</span><span class="o">)</span> <span class="k">throw</span> <span class="k">new</span> <span class="nc">IllegalArgumentException</span><span class="o">(</span><span class="s">"Timeout must be positive"</span><span class="o">)</span>
    <span class="nc">HttpClientConfig</span><span class="o">(</span><span class="n">host</span><span class="o">,</span> <span class="n">port</span><span class="o">,</span> <span class="n">timeoutSeconds</span><span class="o">)</span>
  <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/main/scala/io/github/sps23/designpatterns/builder/HttpClientConfig.scala">View in repository</a></p>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">final</span> <span class="k">case</span> <span class="k">class</span> <span class="nc">HttpClientConfig</span><span class="o">(</span><span class="n">host</span><span class="o">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">port</span><span class="o">:</span> <span class="kt">Int</span><span class="o">,</span> <span class="n">timeoutSeconds</span><span class="o">:</span> <span class="kt">Int</span> <span class="o">=</span> <span class="mi">30</span><span class="o">)</span>
<span class="k">object</span> <span class="nc">HttpClientConfigBuilder</span><span class="o">:</span>
  <span class="k">def</span> <span class="n">builder</span><span class="o">(</span><span class="n">host</span><span class="o">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">port</span><span class="o">:</span> <span class="kt">Int</span><span class="o">):</span> <span class="kt">HttpClientConfigBuilder</span> <span class="o">=</span> <span class="nc">HttpClientConfigBuilder</span><span class="o">(</span><span class="n">host</span><span class="o">,</span> <span class="n">port</span><span class="o">)</span>
<span class="k">final</span> <span class="k">case</span> <span class="k">class</span> <span class="nc">HttpClientConfigBuilder</span> <span class="k">private</span> <span class="o">(</span><span class="n">host</span><span class="o">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">port</span><span class="o">:</span> <span class="kt">Int</span><span class="o">,</span> <span class="n">timeoutSeconds</span><span class="o">:</span> <span class="kt">Int</span> <span class="o">=</span> <span class="mi">30</span><span class="o">):</span>
  <span class="k">def</span> <span class="n">withTimeoutSeconds</span><span class="o">(</span><span class="n">value</span><span class="o">:</span> <span class="kt">Int</span><span class="o">):</span> <span class="kt">HttpClientConfigBuilder</span> <span class="o">=</span> <span class="n">copy</span><span class="o">(</span><span class="n">timeoutSeconds</span> <span class="o">=</span> <span class="n">value</span><span class="o">)</span>
  <span class="k">def</span> <span class="n">build</span><span class="o">():</span> <span class="kt">HttpClientConfig</span> <span class="o">=</span>
    <span class="k">if</span> <span class="n">timeoutSeconds</span> <span class="o">&lt;=</span> <span class="mi">0</span> <span class="k">then</span> <span class="k">throw</span> <span class="k">new</span> <span class="nc">IllegalArgumentException</span><span class="o">(</span><span class="s">"Timeout must be positive"</span><span class="o">)</span>
    <span class="nc">HttpClientConfig</span><span class="o">(</span><span class="n">host</span><span class="o">,</span> <span class="n">port</span><span class="o">,</span> <span class="n">timeoutSeconds</span><span class="o">)</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/designpatterns/builder/HttpClientConfig.scala">View in repository</a></p>
</div>
</div>

## Comparison: Java 21 vs Scala 2 vs Scala 3 vs Kotlin

<div class="table-wrapper" markdown="1">

| Language | Builder style | Defaults style | Validation style |
|----------|---------------|----------------|------------------|
| Java 21 | Nested mutable fluent builder | Fields in builder class | Throw in `build()` |
| Scala 2 | Immutable fluent builder returning new instances | Default params in case class + builder defaults | Throw in `build()` |
| Scala 3 | Case-class builder with fluent `with...` methods | Default params + `copy` ergonomics | Throw in `build()` |
| Kotlin | Mutable fluent builder + data class target | Data class defaults and builder defaults | `require(...)` in `build()` |

</div>

## Testing the Builder

Builder tests should verify:
1. defaults are applied correctly;
2. custom values override defaults;
3. cross-field validation rules hold;
4. invalid input fails fast with useful errors.

<div class="code-tabs" data-tabs-id="tabs-test-1">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="kt">void</span> <span class="nf">shouldBuildWithDefaults</span><span class="o">()</span> <span class="o">{</span>
    <span class="nc">HttpClientConfig</span> <span class="n">config</span> <span class="o">=</span> <span class="nc">HttpClientConfig</span><span class="o">.</span><span class="na">builder</span><span class="o">(</span><span class="s">"api.example.com"</span><span class="o">,</span> <span class="mi">443</span><span class="o">).</span><span class="na">build</span><span class="o">();</span>
    <span class="n">assertEquals</span><span class="o">(</span><span class="mi">500</span><span class="o">,</span> <span class="n">config</span><span class="o">.</span><span class="na">connectTimeoutMs</span><span class="o">());</span>
<span class="o">}</span>
<span class="nd">@Test</span>
<span class="kt">void</span> <span class="nf">shouldRejectInvalidPort</span><span class="o">()</span> <span class="o">{</span>
    <span class="n">assertThrows</span><span class="o">(</span><span class="nc">IllegalArgumentException</span><span class="o">.</span><span class="na">class</span><span class="o">,</span> <span class="o">()</span> <span class="o">-&gt;</span> <span class="nc">HttpClientConfig</span><span class="o">.</span><span class="na">builder</span><span class="o">(</span><span class="s">"api.example.com"</span><span class="o">,</span> <span class="mi">70000</span><span class="o">).</span><span class="na">build</span><span class="o">());</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/designpatterns/builder/HttpClientConfigTest.java">View full test file</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="k">fun</span> <span class="nf">shouldBuildWithDefaults</span><span class="p">()</span> <span class="p">{</span>
    <span class="k">val</span> <span class="py">config</span> <span class="p">=</span> <span class="nc">HttpClientConfigBuilder</span><span class="p">.</span><span class="nf">builder</span><span class="p">(</span><span class="s">"api.example.com"</span><span class="p">,</span> <span class="m">443</span><span class="p">).</span><span class="n">build</span><span class="p">()</span>
    <span class="nf">assertEquals</span><span class="p">(</span><span class="m">500</span><span class="p">,</span> <span class="n">config</span><span class="p">.</span><span class="n">connectTimeoutMs</span><span class="p">)</span>
<span class="p">}</span>
<span class="nd">@Test</span>
<span class="k">fun</span> <span class="nf">shouldRejectInvalidPort</span><span class="p">()</span> <span class="p">{</span>
    <span class="nf">assertThrows</span><span class="p">(</span><span class="nc">IllegalArgumentException</span><span class="o">::</span><span class="k">class</span><span class="p">.</span><span class="na">java</span><span class="p">)</span> <span class="p">{</span> <span class="nc">HttpClientConfigBuilder</span><span class="p">.</span><span class="nf">builder</span><span class="p">(</span><span class="s">"api.example.com"</span><span class="p">,</span> <span class="m">70000</span><span class="p">).</span><span class="n">build</span><span class="p">()</span> <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/designpatterns/builder/HttpClientConfigTest.kt">View full test file</a></p>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="n">test</span><span class="o">(</span><span class="s">"Builder should create config with defaults"</span><span class="o">)</span> <span class="o">{</span>
  <span class="k">val</span> <span class="n">config</span> <span class="o">=</span> <span class="nc">HttpClientConfigBuilder</span><span class="o">.</span><span class="n">builder</span><span class="o">(</span><span class="s">"api.example.com"</span><span class="o">,</span> <span class="mi">443</span><span class="o">).</span><span class="n">build</span><span class="o">()</span>
  <span class="n">config</span><span class="o">.</span><span class="n">connectTimeoutMs</span> <span class="n">shouldBe</span> <span class="mi">500</span>
<span class="o">}</span>
<span class="n">test</span><span class="o">(</span><span class="s">"Builder should reject invalid port"</span><span class="o">)</span> <span class="o">{</span>
  <span class="n">the</span><span class="o">[</span><span class="kt">IllegalArgumentException</span><span class="o">]</span> <span class="n">thrownBy</span> <span class="nc">HttpClientConfigBuilder</span><span class="o">.</span><span class="n">builder</span><span class="o">(</span><span class="s">"api.example.com"</span><span class="o">,</span> <span class="mi">70000</span><span class="o">).</span><span class="n">build</span><span class="o">()</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/test/scala/io/github/sps23/designpatterns/builder/HttpClientConfigTest.scala">View full test file</a></p>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="n">test</span><span class="o">(</span><span class="s">"Builder should create config with defaults"</span><span class="o">)</span> <span class="o">{</span>
  <span class="k">val</span> <span class="n">config</span> <span class="o">=</span> <span class="nc">HttpClientConfigBuilder</span><span class="o">.</span><span class="n">builder</span><span class="o">(</span><span class="s">"api.example.com"</span><span class="o">,</span> <span class="mi">443</span><span class="o">).</span><span class="n">build</span><span class="o">()</span>
  <span class="n">config</span><span class="o">.</span><span class="n">connectTimeoutMs</span> <span class="n">shouldBe</span> <span class="mi">500</span>
<span class="o">}</span>
<span class="n">test</span><span class="o">(</span><span class="s">"Builder should reject invalid port"</span><span class="o">)</span> <span class="o">{</span>
  <span class="n">the</span><span class="o">[</span><span class="kt">IllegalArgumentException</span><span class="o">]</span> <span class="n">thrownBy</span> <span class="nc">HttpClientConfigBuilder</span><span class="o">.</span><span class="n">builder</span><span class="o">(</span><span class="s">"api.example.com"</span><span class="o">,</span> <span class="mi">70000</span><span class="o">).</span><span class="n">build</span><span class="o">()</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/designpatterns/builder/HttpClientConfigTest.scala">View full test file</a></p>
</div>
</div>

## When to Use the Builder Pattern

Use Builder when:
1. objects have many optional parameters;
2. you need readable, self-documenting construction;
3. validation should happen once, at object creation.

Prefer simple constructors or data class defaults when the object has only a few fields and no complex validation.

## Where Builder Is Most Common in Real Projects

You will see Builder used heavily in production code where objects have many options and strict invariants:

1. **HTTP and SDK clients** (`OkHttpClient.Builder`, AWS SDK builders, Elasticsearch clients).
2. **Database connection and pool configs** (timeouts, retries, TLS, failover).
3. **Messaging and event publisher configs** (batch sizes, delivery guarantees, backpressure).
4. **Domain commands/events** where required fields and optional metadata must be explicit.
5. **Test fixtures** for complex object setup without noisy constructors.

This is exactly why Builder is so valuable: it balances **readability**, **safe defaults**, and **validation** while still making call sites pleasant to read.

## Code Samples

All examples in this post are available in the repository:

**Implementation files:**
- **Java 21:** [HttpClientConfig.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/designpatterns/builder/HttpClientConfig.java)
- **Kotlin:** [HttpClientConfig.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/designpatterns/builder/HttpClientConfig.kt)
- **Scala 2:** [HttpClientConfig.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/main/scala/io/github/sps23/designpatterns/builder/HttpClientConfig.scala)
- **Scala 3:** [HttpClientConfig.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/designpatterns/builder/HttpClientConfig.scala)

**Test files:**
- **Java 21:** [HttpClientConfigTest.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/designpatterns/builder/HttpClientConfigTest.java)
- **Kotlin:** [HttpClientConfigTest.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/designpatterns/builder/HttpClientConfigTest.kt)
- **Scala 2:** [HttpClientConfigTest.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/test/scala/io/github/sps23/designpatterns/builder/HttpClientConfigTest.scala)
- **Scala 3:** [HttpClientConfigTest.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/designpatterns/builder/HttpClientConfigTest.scala)

---

*This is part of our Design Patterns in JVM Languages series. Check out the [full design patterns guide]({{ site.baseurl }}/interview/2026/07/26/design-patterns-guide-jvm) for more patterns and interview preparation.*
