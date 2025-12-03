---
layout: post
title: "CompletableFuture and Asynchronous Programming"
description: "Build concurrent applications with Java CompletableFuture - learn async composition, timeouts, and error handling with comparisons to Scala Futures and Kotlin Coroutines."
date: 2025-11-29 10:00:00 +0000
categories: [concurrency]
tags: [java, scala, kotlin, async, futures, completablefuture, coroutines]
---

Asynchronous programming is essential for building responsive, high-performance applications. This post explores how Java, Scala, and Kotlin handle async operations, with a focus on aggregating data from multiple APIs concurrently.

## The Problem

Imagine building a service that aggregates data from multiple external APIs:
- Weather API
- Traffic API
- News API

We need to:
1. Call all APIs concurrently (not sequentially)
2. Handle timeouts gracefully
3. Provide fallback values when APIs fail
4. Combine results into an aggregated response

## Basic Async Operations

<div class="code-tabs" data-tabs-id="tabs-1">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Create an async task - similar to Scala's Future { ... }</span>
<span class="nc">CompletableFuture</span><span class="o">&lt;</span><span class="nc">ApiResponse</span><span class="o">&gt;</span> <span class="nf">fetchWeatherData</span><span class="o">()</span> <span class="o">{</span>
    <span class="k">return</span> <span class="nc">CompletableFuture</span><span class="o">.</span><span class="na">supplyAsync</span><span class="o">(()</span> <span class="o">-&gt;</span> <span class="o">{</span>
        <span class="c1">// Simulate API call</span>
        <span class="nc">Thread</span><span class="o">.</span><span class="na">sleep</span><span class="o">(</span><span class="mi">150</span><span class="o">);</span>
        <span class="k">return</span> <span class="nc">ApiResponse</span><span class="o">.</span><span class="na">of</span><span class="o">(</span><span class="s">"weather"</span><span class="o">,</span> <span class="s">"{\"temp\": 22}"</span><span class="o">);</span>
    <span class="o">},</span> <span class="n">executor</span><span class="o">);</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">def</span> <span class="nf">fetchWeatherData</span><span class="o">()</span><span class="k">:</span> <span class="kt">Future</span><span class="o">[</span><span class="kt">ApiResponse</span><span class="o">]</span> <span class="k">=</span> <span class="nc">Future</span> <span class="o">{</span>
  <span class="nf">simulateApiCall</span><span class="o">(</span><span class="s">"Weather API"</span><span class="o">,</span> <span class="mi">150</span><span class="o">)</span>
  <span class="nc">ApiResponse</span><span class="o">(</span><span class="s">"weather"</span><span class="o">,</span> <span class="s">"""{"temp": 22}"""</span><span class="o">)</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">fun</span> <span class="nc">CoroutineScope</span><span class="p">.</span><span class="nf">fetchWeatherDataAsync</span><span class="p">():</span> <span class="nc">Deferred</span><span class="p">&lt;</span><span class="nc">ApiResponse</span><span class="p">&gt;</span> <span class="p">=</span>
    <span class="nf">async</span><span class="p">(</span><span class="nc">Dispatchers</span><span class="p">.</span><span class="nc">IO</span><span class="p">)</span> <span class="p">{</span>
        <span class="nf">simulateApiCall</span><span class="p">(</span><span class="s">"Weather API"</span><span class="p">,</span> <span class="mi">150</span><span class="p">)</span>
        <span class="nc">ApiResponse</span><span class="p">.</span><span class="nf">of</span><span class="p">(</span><span class="s">"weather"</span><span class="p">,</span> <span class="s">"""{"temp": 22}"""</span><span class="p">)</span>
    <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

## Transformation and Chaining

<div class="code-tabs" data-tabs-id="tabs-2">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Transform the result (like Scala's map)</span>
<span class="nc">CompletableFuture</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="nf">transformResponse</span><span class="o">(</span><span class="nc">CompletableFuture</span><span class="o">&lt;</span><span class="nc">ApiResponse</span><span class="o">&gt;</span> <span class="n">future</span><span class="o">)</span> <span class="o">{</span>
    <span class="k">return</span> <span class="n">future</span><span class="o">.</span><span class="na">thenApply</span><span class="o">(</span><span class="n">response</span> <span class="o">-&gt;</span> <span class="n">response</span><span class="o">.</span><span class="na">data</span><span class="o">().</span><span class="na">toUpperCase</span><span class="o">());</span>
<span class="o">}</span>

<span class="c1">// Chain dependent async operations (like Scala's flatMap)</span>
<span class="nc">CompletableFuture</span><span class="o">&lt;</span><span class="nc">ApiResponse</span><span class="o">&gt;</span> <span class="nf">fetchAndEnrichWeather</span><span class="o">()</span> <span class="o">{</span>
    <span class="k">return</span> <span class="nf">fetchWeatherData</span><span class="o">()</span>
        <span class="o">.</span><span class="na">thenCompose</span><span class="o">(</span><span class="n">weather</span> <span class="o">-&gt;</span> <span class="n">enrichWithLocation</span><span class="o">(</span><span class="n">weather</span><span class="o">));</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Transform the result (map)</span>
<span class="k">def</span> <span class="nf">transformResponse</span><span class="o">(</span><span class="n">future</span><span class="k">:</span> <span class="kt">Future</span><span class="o">[</span><span class="kt">ApiResponse</span><span class="o">])</span><span class="k">:</span> <span class="kt">Future</span><span class="o">[</span><span class="kt">String</span><span class="o">]</span> <span class="k">=</span>
  <span class="nv">future</span><span class="o">.</span><span class="py">map</span><span class="o">(</span><span class="n">response</span> <span class="k">=&gt;</span> <span class="nv">response</span><span class="o">.</span><span class="py">data</span><span class="o">.</span><span class="py">toUpperCase</span><span class="o">)</span>

<span class="c1">// Chain with for-comprehension (flatMap)</span>
<span class="k">def</span> <span class="nf">fetchAndEnrichWeather</span><span class="o">()</span><span class="k">:</span> <span class="kt">Future</span><span class="o">[</span><span class="kt">ApiResponse</span><span class="o">]</span> <span class="k">=</span>
  <span class="k">for</span>
    <span class="n">weather</span> <span class="k">&lt;-</span> <span class="nf">fetchWeatherData</span><span class="o">()</span>
    <span class="n">enriched</span> <span class="k">&lt;-</span> <span class="nf">enrichWithLocation</span><span class="o">(</span><span class="n">weather</span><span class="o">)</span>
  <span class="k">yield</span> <span class="n">enriched</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Sequential operations (looks synchronous but is async!)</span>
<span class="k">suspend</span> <span class="k">fun</span> <span class="nf">fetchAndEnrichWeather</span><span class="p">():</span> <span class="nc">ApiResponse</span> <span class="p">{</span>
    <span class="kd">val</span> <span class="py">weather</span> <span class="p">=</span> <span class="nf">fetchWeatherDataAsync</span><span class="p">().</span><span class="nf">await</span><span class="p">()</span>
    <span class="k">return</span> <span class="nf">enrichWithLocation</span><span class="p">(</span><span class="n">weather</span><span class="p">)</span>
<span class="p">}</span>
</code></pre></div></div>
</div>
</div>

## Combining Multiple Futures

<div class="code-tabs" data-tabs-id="tabs-3">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Wait for all futures to complete</span>
<span class="nc">CompletableFuture</span><span class="o">&lt;</span><span class="nc">AggregatedData</span><span class="o">&gt;</span> <span class="nf">aggregateFromAllApis</span><span class="o">(</span><span class="nc">Duration</span> <span class="n">timeout</span><span class="o">)</span> <span class="o">{</span>
    <span class="kt">var</span> <span class="n">weather</span> <span class="o">=</span> <span class="n">fetchWeatherData</span><span class="o">()</span>
        <span class="o">.</span><span class="na">completeOnTimeout</span><span class="o">(</span><span class="n">fallback</span><span class="o">,</span> <span class="n">timeout</span><span class="o">.</span><span class="na">toMillis</span><span class="o">(),</span> <span class="nc">TimeUnit</span><span class="o">.</span><span class="na">MILLISECONDS</span><span class="o">);</span>
    <span class="kt">var</span> <span class="n">traffic</span> <span class="o">=</span> <span class="n">fetchTrafficData</span><span class="o">()</span>
        <span class="o">.</span><span class="na">completeOnTimeout</span><span class="o">(</span><span class="n">fallback</span><span class="o">,</span> <span class="n">timeout</span><span class="o">.</span><span class="na">toMillis</span><span class="o">(),</span> <span class="nc">TimeUnit</span><span class="o">.</span><span class="na">MILLISECONDS</span><span class="o">);</span>
    <span class="kt">var</span> <span class="n">news</span> <span class="o">=</span> <span class="n">fetchNewsData</span><span class="o">()</span>
        <span class="o">.</span><span class="na">completeOnTimeout</span><span class="o">(</span><span class="n">fallback</span><span class="o">,</span> <span class="n">timeout</span><span class="o">.</span><span class="na">toMillis</span><span class="o">(),</span> <span class="nc">TimeUnit</span><span class="o">.</span><span class="na">MILLISECONDS</span><span class="o">);</span>

    <span class="k">return</span> <span class="nc">CompletableFuture</span><span class="o">.</span><span class="na">allOf</span><span class="o">(</span><span class="n">weather</span><span class="o">,</span> <span class="n">traffic</span><span class="o">,</span> <span class="n">news</span><span class="o">)</span>
        <span class="o">.</span><span class="na">thenApply</span><span class="o">(</span><span class="n">v</span> <span class="o">-&gt;</span> <span class="nc">AggregatedData</span><span class="o">.</span><span class="na">success</span><span class="o">(</span><span class="nc">List</span><span class="o">.</span><span class="na">of</span><span class="o">(</span>
            <span class="n">weather</span><span class="o">.</span><span class="na">join</span><span class="o">(),</span>
            <span class="n">traffic</span><span class="o">.</span><span class="na">join</span><span class="o">(),</span>
            <span class="n">news</span><span class="o">.</span><span class="na">join</span><span class="o">()</span>
        <span class="o">)));</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">def</span> <span class="nf">aggregateFromAllApis</span><span class="o">(</span><span class="n">timeout</span><span class="k">:</span> <span class="kt">FiniteDuration</span><span class="o">)</span><span class="k">:</span> <span class="kt">Future</span><span class="o">[</span><span class="kt">AggregatedData</span><span class="o">]</span> <span class="k">=</span>
  <span class="k">val</span> <span class="nv">futures</span> <span class="k">=</span> <span class="nc">List</span><span class="o">(</span>
    <span class="nf">withTimeout</span><span class="o">(</span><span class="nf">fetchWeatherData</span><span class="o">(),</span> <span class="n">timeout</span><span class="o">,</span> <span class="n">fallback</span><span class="o">),</span>
    <span class="nf">withTimeout</span><span class="o">(</span><span class="nf">fetchTrafficData</span><span class="o">(),</span> <span class="n">timeout</span><span class="o">,</span> <span class="n">fallback</span><span class="o">),</span>
    <span class="nf">withTimeout</span><span class="o">(</span><span class="nf">fetchNewsData</span><span class="o">(),</span> <span class="n">timeout</span><span class="o">,</span> <span class="n">fallback</span><span class="o">)</span>
  <span class="o">)</span>
  
  <span class="nv">Future</span><span class="o">.</span><span class="py">sequence</span><span class="o">(</span><span class="n">futures</span><span class="o">).</span><span class="py">map</span><span class="o">(</span><span class="n">responses</span> <span class="k">=&gt;</span> 
    <span class="nv">AggregatedData</span><span class="o">.</span><span class="py">success</span><span class="o">(</span><span class="n">responses</span><span class="o">)</span>
  <span class="o">)</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">suspend</span> <span class="k">fun</span> <span class="nf">aggregateFromAllApis</span><span class="p">(</span><span class="n">timeout</span><span class="p">:</span> <span class="nc">Duration</span><span class="p">):</span> <span class="nc">AggregatedData</span> <span class="p">=</span>
    <span class="nf">coroutineScope</span> <span class="p">{</span>
        <span class="kd">val</span> <span class="py">weather</span> <span class="p">=</span> <span class="nf">async</span> <span class="p">{</span> <span class="nf">fetchWithTimeout</span><span class="p">(</span><span class="s">"weather"</span><span class="p">,</span> <span class="n">timeout</span><span class="p">)</span> <span class="p">}</span>
        <span class="kd">val</span> <span class="py">traffic</span> <span class="p">=</span> <span class="nf">async</span> <span class="p">{</span> <span class="nf">fetchWithTimeout</span><span class="p">(</span><span class="s">"traffic"</span><span class="p">,</span> <span class="n">timeout</span><span class="p">)</span> <span class="p">}</span>
        <span class="kd">val</span> <span class="py">news</span> <span class="p">=</span> <span class="nf">async</span> <span class="p">{</span> <span class="nf">fetchWithTimeout</span><span class="p">(</span><span class="s">"news"</span><span class="p">,</span> <span class="n">timeout</span><span class="p">)</span> <span class="p">}</span>
        
        <span class="kd">val</span> <span class="py">responses</span> <span class="p">=</span> <span class="nf">listOf</span><span class="p">(</span><span class="n">weather</span><span class="p">,</span> <span class="n">traffic</span><span class="p">,</span> <span class="n">news</span><span class="p">).</span><span class="nf">awaitAll</span><span class="p">()</span>
        <span class="nc">AggregatedData</span><span class="p">.</span><span class="nf">success</span><span class="p">(</span><span class="n">responses</span><span class="p">)</span>
    <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

## Racing Futures

<div class="code-tabs" data-tabs-id="tabs-4">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Get the first result</span>
<span class="nc">CompletableFuture</span><span class="o">&lt;</span><span class="nc">ApiResponse</span><span class="o">&gt;</span> <span class="nf">getFirstAvailableResponse</span><span class="o">()</span> <span class="o">{</span>
    <span class="k">return</span> <span class="nc">CompletableFuture</span><span class="o">.</span><span class="na">anyOf</span><span class="o">(</span>
        <span class="n">fetchWeatherData</span><span class="o">(),</span>
        <span class="n">fetchTrafficData</span><span class="o">(),</span>
        <span class="n">fetchNewsData</span><span class="o">()</span>
    <span class="o">).</span><span class="na">thenApply</span><span class="o">(</span><span class="n">result</span> <span class="o">-&gt;</span> <span class="o">(</span><span class="nc">ApiResponse</span><span class="o">)</span> <span class="n">result</span><span class="o">);</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">def</span> <span class="nf">getFirstAvailableResponse</span><span class="o">()</span><span class="k">:</span> <span class="kt">Future</span><span class="o">[</span><span class="kt">ApiResponse</span><span class="o">]</span> <span class="k">=</span>
  <span class="nv">Future</span><span class="o">.</span><span class="py">firstCompletedOf</span><span class="o">(</span><span class="nc">List</span><span class="o">(</span>
    <span class="nf">fetchWeatherData</span><span class="o">(),</span>
    <span class="nf">fetchTrafficData</span><span class="o">(),</span>
    <span class="nf">fetchNewsData</span><span class="o">()</span>
  <span class="o">))</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">suspend</span> <span class="k">fun</span> <span class="nf">getFirstAvailableResponse</span><span class="p">():</span> <span class="nc">ApiResponse</span> <span class="p">=</span>
    <span class="nf">coroutineScope</span> <span class="p">{</span>
        <span class="kd">val</span> <span class="py">weather</span> <span class="p">=</span> <span class="nf">fetchWeatherDataAsync</span><span class="p">()</span>
        <span class="kd">val</span> <span class="py">traffic</span> <span class="p">=</span> <span class="nf">fetchTrafficDataAsync</span><span class="p">()</span>
        <span class="kd">val</span> <span class="py">news</span> <span class="p">=</span> <span class="nf">fetchNewsDataAsync</span><span class="p">()</span>
        
        <span class="nf">select</span> <span class="p">{</span>
            <span class="n">weather</span><span class="p">.</span><span class="nf">onAwait</span> <span class="p">{</span> <span class="k">it</span> <span class="p">}</span>
            <span class="n">traffic</span><span class="p">.</span><span class="nf">onAwait</span> <span class="p">{</span> <span class="k">it</span> <span class="p">}</span>
            <span class="n">news</span><span class="p">.</span><span class="nf">onAwait</span> <span class="p">{</span> <span class="k">it</span> <span class="p">}</span>
        <span class="p">}</span>
    <span class="p">}</span>
</code></pre></div></div>
</div>
</div>

## Error Handling

<div class="code-tabs" data-tabs-id="tabs-5">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Recovery (like Scala's recover)</span>
<span class="n">future</span><span class="o">.</span><span class="na">exceptionally</span><span class="o">(</span><span class="n">ex</span> <span class="o">-&gt;</span> <span class="nc">ApiResponse</span><span class="o">.</span><span class="na">of</span><span class="o">(</span><span class="s">"fallback"</span><span class="o">,</span> <span class="s">"{}"</span><span class="o">));</span>

<span class="c1">// Full handling (like Scala's transform)</span>
<span class="n">future</span><span class="o">.</span><span class="na">handle</span><span class="o">((</span><span class="n">response</span><span class="o">,</span> <span class="n">ex</span><span class="o">)</span> <span class="o">-&gt;</span> <span class="o">{</span>
    <span class="k">if</span> <span class="o">(</span><span class="n">ex</span> <span class="o">!=</span> <span class="kc">null</span><span class="o">)</span> <span class="k">return</span> <span class="s">"Error: "</span> <span class="o">+</span> <span class="n">ex</span><span class="o">.</span><span class="na">getMessage</span><span class="o">();</span>
    <span class="k">return</span> <span class="s">"Success: "</span> <span class="o">+</span> <span class="n">response</span><span class="o">.</span><span class="na">data</span><span class="o">();</span>
<span class="o">});</span>

<span class="c1">// Timeout handling (Java 9+)</span>
<span class="n">future</span><span class="o">.</span><span class="na">completeOnTimeout</span><span class="o">(</span><span class="n">fallback</span><span class="o">,</span> <span class="mi">5</span><span class="o">,</span> <span class="nc">TimeUnit</span><span class="o">.</span><span class="na">SECONDS</span><span class="o">);</span>
<span class="n">future</span><span class="o">.</span><span class="na">orTimeout</span><span class="o">(</span><span class="mi">5</span><span class="o">,</span> <span class="nc">TimeUnit</span><span class="o">.</span><span class="na">SECONDS</span><span class="o">);</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Recovery</span>
<span class="nv">future</span><span class="o">.</span><span class="py">recover</span> <span class="o">{</span> <span class="k">case</span> <span class="n">ex</span><span class="k">:</span> <span class="kt">Exception</span> <span class="o">=&gt;</span>
  <span class="nc">ApiResponse</span><span class="o">(</span><span class="s">"fallback"</span><span class="o">,</span> <span class="s">"{}"</span><span class="o">)</span>
<span class="o">}</span>

<span class="c1">// Full transformation</span>
<span class="nv">future</span><span class="o">.</span><span class="py">transform</span> <span class="o">{</span>
  <span class="k">case</span> <span class="nc">Success</span><span class="o">(</span><span class="n">response</span><span class="o">)</span> <span class="k">=&gt;</span> <span class="nc">Success</span><span class="o">(</span><span class="nv">s</span><span class="s">"Success: ${response.data}"</span><span class="o">)</span>
  <span class="k">case</span> <span class="nc">Failure</span><span class="o">(</span><span class="n">ex</span><span class="o">)</span> <span class="k">=&gt;</span> <span class="nc">Success</span><span class="o">(</span><span class="nv">s</span><span class="s">"Error: ${ex.getMessage}"</span><span class="o">)</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="c1">// Using runCatching (Kotlin's Result type)</span>
<span class="k">suspend</span> <span class="k">fun</span> <span class="nf">fetchWithHandling</span><span class="p">():</span> <span class="nc">String</span> <span class="p">{</span>
    <span class="kd">val</span> <span class="py">result</span> <span class="p">=</span> <span class="nf">runCatching</span> <span class="p">{</span> <span class="nf">fetchApiData</span><span class="p">()</span> <span class="p">}</span>
    
    <span class="k">return</span> <span class="n">result</span><span class="p">.</span><span class="nf">fold</span><span class="p">(</span>
        <span class="n">onSuccess</span> <span class="p">=</span> <span class="p">{</span> <span class="s">"Success: ${it.data}"</span> <span class="p">},</span>
        <span class="n">onFailure</span> <span class="p">=</span> <span class="p">{</span> <span class="s">"Error: ${it.message}"</span> <span class="p">}</span>
    <span class="p">)</span>
<span class="p">}</span>

<span class="c1">// Built-in timeout handling</span>
<span class="kd">val</span> <span class="py">result</span> <span class="p">=</span> <span class="nf">withTimeoutOrNull</span><span class="p">(</span><span class="mi">5</span><span class="p">.</span><span class="n">seconds</span><span class="p">)</span> <span class="p">{</span>
    <span class="nf">fetchSlowApi</span><span class="p">()</span>
<span class="p">}</span> <span class="o">?:</span> <span class="n">fallbackValue</span>
</code></pre></div></div>
</div>
</div>

## Comparison Table

<div class="table-wrapper" markdown="1">

| Feature | Java CompletableFuture | Scala Future | Kotlin Coroutines |
|---------|----------------------|--------------|-------------------|
| Create async | `supplyAsync()` | `Future { }` | `async { }` |
| Transform | `thenApply()` | `map` | `await()` + transform |
| Chain | `thenCompose()` | `flatMap` / for-comp | Sequential `await()` |
| Combine all | `allOf()` | `sequence` | `awaitAll()` |
| First completed | `anyOf()` | `firstCompletedOf` | `select { }` |
| Timeout fallback | `completeOnTimeout()` | Helper needed | `withTimeoutOrNull()` |
| Timeout exception | `orTimeout()` | Helper needed | `withTimeout()` |
| Recover | `exceptionally()` | `recover` | try/catch |
| Transform both | `handle()` | `transform` | `runCatching().fold()` |

</div>

## Key Insights for Scala Developers

1. **Java's API is verbose but complete**: CompletableFuture has everything you need, including built-in timeout methods (Java 9+).

2. **Kotlin coroutines feel natural**: The suspend function model makes async code look synchronous, which can be easier to read and maintain.

3. **Scala's for-comprehensions are elegant**: Nothing beats the readability of Scala's for-comprehension syntax for chaining futures.

4. **Error handling differs**: 
   - Java uses `exceptionally()` and `handle()`
   - Scala uses `recover` and `transform`
   - Kotlin uses standard try/catch with suspend functions

5. **Timeout handling**: Java 9+ has built-in timeout methods, while Scala requires custom helpers. Kotlin has excellent built-in support.

## Full Working Examples

Check out the complete implementation in our repository:
- [Java CompletableFuture](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/interview/preparation/async)
- [Scala 3 Future](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/interview/preparation/async)
- [Kotlin Coroutines](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/async)

## Conclusion

All three languages provide powerful async primitives. For Scala developers moving to Java:

- Think of `supplyAsync()` as `Future { }`
- Think of `thenApply()` as `map`
- Think of `thenCompose()` as `flatMap`
- Think of `allOf()` as `Future.sequence`
- Think of `exceptionally()` as `recover`

The patterns are similar, just with different syntax. The key is understanding the mental model: futures represent values that will be available in the future, and all three languages let you compose them elegantly.

Happy async coding! 🚀
