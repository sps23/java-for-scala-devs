---
layout: post
title: "Virtual Threads and Structured Concurrency"
description: "Master Java 21 virtual threads and Project Loom - migrate from thread pools, use StructuredTaskScope, understand scoped values, and compare with Scala ZIO and Kotlin coroutines."
date: 2025-11-29 17:00:00 +0000
updated: 2026-09-24 19:00:00 +0000
categories: [interview]
tags: [java, java21, scala, kotlin, virtual-threads, concurrency, project-loom, interview-preparation]
---

Project Loom brings revolutionary changes to Java concurrency with virtual threads and structured concurrency. In this post, we'll migrate a thread-pool-based web scraper to virtual threads, demonstrating the dramatic simplification and scalability improvements.

## The Problem: Scaling Concurrent HTTP Requests

Imagine you need to scrape thousands of web pages concurrently. With traditional platform threads, you face several challenges:

<div class="table-wrapper" markdown="1">

| Challenge | Impact |
|-----------|--------|
| Memory | ~1MB stack per thread, limiting total threads |
| Thread Pool Sizing | Too few threads = queuing; too many = memory exhaustion |
| Blocking I/O | Threads sit idle waiting for responses |
| Scalability | 10K concurrent requests requires ~10GB of thread stacks |

</div>

## Before: Traditional Thread Pool Approach

Here's how we'd typically implement a web scraper with platform threads:

<div class="code-tabs" data-tabs-id="traditional-thread-pool">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">class</span> <span class="nc">WebScraperTraditional</span> <span class="o">{</span>
    <span class="c1">// Fixed thread pool - typically sized based on available processors</span>
    <span class="kd">private</span> <span class="kd">static</span> <span class="kd">final</span> <span class="kt">int</span> <span class="nc">THREAD_POOL_SIZE</span> <span class="o">=</span> <span class="nc">Runtime</span><span class="o">.</span><span class="na">getRuntime</span><span class="o">().</span><span class="na">availableProcessors</span><span class="o">()</span> <span class="o">*</span> <span class="mi">2</span><span class="o">;</span>

    <span class="kd">private</span> <span class="kd">final</span> <span class="nc">ExecutorService</span> <span class="n">executor</span> <span class="o">=</span> <span class="nc">Executors</span><span class="o">.</span><span class="na">newFixedThreadPool</span><span class="o">(</span><span class="nc">THREAD_POOL_SIZE</span><span class="o">);</span>
    <span class="kd">private</span> <span class="kd">final</span> <span class="nc">HttpClient</span> <span class="n">httpClient</span> <span class="o">=</span> <span class="nc">HttpClient</span><span class="o">.</span><span class="na">newBuilder</span><span class="o">()</span>
            <span class="o">.</span><span class="na">connectTimeout</span><span class="o">(</span><span class="nc">Duration</span><span class="o">.</span><span class="na">ofSeconds</span><span class="o">(</span><span class="mi">10</span><span class="o">))</span>
            <span class="o">.</span><span class="na">build</span><span class="o">();</span>

    <span class="kd">public</span> <span class="nc">List</span><span class="o">&lt;</span><span class="nc">ScrapedResult</span><span class="o">&gt;</span> <span class="nf">scrapeAll</span><span class="o">(</span><span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">urls</span><span class="o">)</span> <span class="o">{</span>
        <span class="nc">List</span><span class="o">&lt;</span><span class="nc">Callable</span><span class="o">&lt;</span><span class="nc">ScrapedResult</span><span class="o">&gt;&gt;</span> <span class="n">tasks</span> <span class="o">=</span> <span class="n">urls</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
                <span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="n">url</span> <span class="o">-&gt;</span> <span class="o">(</span><span class="nc">Callable</span><span class="o">&lt;</span><span class="nc">ScrapedResult</span><span class="o">&gt;)</span> <span class="o">()</span> <span class="o">-&gt;</span> <span class="nf">scrapeUrl</span><span class="o">(</span><span class="n">url</span><span class="o">))</span>
                <span class="o">.</span><span class="na">toList</span><span class="o">();</span>

        <span class="k">try</span> <span class="o">{</span>
            <span class="nc">List</span><span class="o">&lt;</span><span class="nc">Future</span><span class="o">&lt;</span><span class="nc">ScrapedResult</span><span class="o">&gt;&gt;</span> <span class="n">futures</span> <span class="o">=</span> <span class="n">executor</span><span class="o">.</span><span class="na">invokeAll</span><span class="o">(</span><span class="n">tasks</span><span class="o">);</span>
            <span class="k">return</span> <span class="n">futures</span><span class="o">.</span><span class="na">stream</span><span class="o">().</span><span class="na">map</span><span class="o">(</span><span class="n">f</span> <span class="o">-&gt;</span> <span class="n">f</span><span class="o">.</span><span class="na">get</span><span class="o">()).</span><span class="na">toList</span><span class="o">();</span>
        <span class="o">}</span> <span class="k">catch</span> <span class="o">(</span><span class="nc">Exception</span> <span class="n">e</span><span class="o">)</span> <span class="o">{</span>
            <span class="nc">Thread</span><span class="o">.</span><span class="na">currentThread</span><span class="o">().</span><span class="na">interrupt</span><span class="o">();</span>
            <span class="k">return</span> <span class="nc">List</span><span class="o">.</span><span class="na">of</span><span class="o">();</span>
        <span class="o">}</span>
    <span class="o">}</span>

    <span class="kd">private</span> <span class="nc">ScrapedResult</span> <span class="nf">scrapeUrl</span><span class="o">(</span><span class="nc">String</span> <span class="n">url</span><span class="o">)</span> <span class="o">{</span>
        <span class="c1">// Blocking HTTP call - ties up the thread while waiting</span>
        <span class="nc">HttpRequest</span> <span class="n">request</span> <span class="o">=</span> <span class="nc">HttpRequest</span><span class="o">.</span><span class="na">newBuilder</span><span class="o">().</span><span class="na">uri</span><span class="o">(</span><span class="nc">URI</span><span class="o">.</span><span class="na">create</span><span class="o">(</span><span class="n">url</span><span class="o">)).</span><span class="na">GET</span><span class="o">().</span><span class="na">build</span><span class="o">();</span>
        <span class="nc">HttpResponse</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">response</span> <span class="o">=</span> <span class="n">httpClient</span><span class="o">.</span><span class="na">send</span><span class="o">(</span><span class="n">request</span><span class="o">,</span> <span class="nc">HttpResponse</span><span class="o">.</span><span class="na">BodyHandlers</span><span class="o">.</span><span class="na">ofString</span><span class="o">());</span>
        <span class="k">return</span> <span class="k">new</span> <span class="nc">ScrapedResult</span><span class="o">(</span><span class="n">url</span><span class="o">,</span> <span class="n">response</span><span class="o">.</span><span class="na">statusCode</span><span class="o">(),</span> <span class="n">response</span><span class="o">.</span><span class="na">body</span><span class="o">().</span><span class="na">length</span><span class="o">());</span>
    <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">object</span> <span class="nc">WebScraperTraditional</span><span class="o">:</span>
  <span class="k">private</span> <span class="k">val</span> <span class="nc">ThreadPoolSize</span> <span class="o">=</span> <span class="nc">Runtime</span><span class="o">.</span><span class="n">getRuntime</span><span class="o">.</span><span class="n">availableProcessors</span> <span class="o">*</span> <span class="mi">2</span>

  <span class="k">def</span> <span class="nf">scrapeAll</span><span class="o">(</span><span class="n">urls</span><span class="o">:</span> <span class="kt">List</span><span class="o">[</span><span class="kt">String</span><span class="o">]):</span> <span class="kt">List</span><span class="o">[</span><span class="nc">ScrapedResult</span><span class="o">]</span> <span class="o">=</span>
    <span class="nc">Using</span><span class="o">.</span><span class="n">resource</span><span class="o">(</span><span class="nc">Executors</span><span class="o">.</span><span class="n">newFixedThreadPool</span><span class="o">(</span><span class="nc">ThreadPoolSize</span><span class="o">))</span> <span class="o">{</span> <span class="n">executor</span> <span class="o">=&gt;</span>
      <span class="k">val</span> <span class="n">tasks</span> <span class="o">=</span> <span class="n">urls</span><span class="o">.</span><span class="n">map</span><span class="o">(</span><span class="n">url</span> <span class="o">=&gt;</span>
        <span class="k">new</span> <span class="nc">Callable</span><span class="o">[</span><span class="nc">ScrapedResult</span><span class="o">]</span> <span class="o">{</span> <span class="k">def</span> <span class="n">call</span><span class="o">()</span> <span class="o">=</span> <span class="n">scrapeUrl</span><span class="o">(</span><span class="n">url</span><span class="o">)</span> <span class="o">}</span>
      <span class="o">).</span><span class="n">asJava</span>
      <span class="k">val</span> <span class="n">futures</span> <span class="o">=</span> <span class="n">executor</span><span class="o">.</span><span class="n">invokeAll</span><span class="o">(</span><span class="n">tasks</span><span class="o">)</span>
      <span class="n">futures</span><span class="o">.</span><span class="n">asScala</span><span class="o">.</span><span class="n">map</span><span class="o">(</span><span class="n">_</span><span class="o">.</span><span class="n">get</span><span class="o">()).</span><span class="n">toList</span>
    <span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">object</span> <span class="nc">WebScraperTraditional</span> <span class="p">{</span>
    <span class="k">private</span> <span class="k">val</span> <span class="py">THREAD_POOL_SIZE</span> <span class="p">=</span> <span class="nc">Runtime</span><span class="p">.</span><span class="n">getRuntime</span><span class="p">().</span><span class="n">availableProcessors</span><span class="p">()</span> <span class="p">*</span> <span class="mi">2</span>

    <span class="k">fun</span> <span class="nf">scrapeAll</span><span class="p">(</span><span class="n">urls</span><span class="p">:</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">&gt;):</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">ScrapedResult</span><span class="p">&gt;</span> <span class="p">{</span>
        <span class="k">val</span> <span class="py">executor</span> <span class="p">=</span> <span class="nc">Executors</span><span class="p">.</span><span class="n">newFixedThreadPool</span><span class="p">(</span><span class="nc">THREAD_POOL_SIZE</span><span class="p">)</span>
        <span class="k">return</span> <span class="k">try</span> <span class="p">{</span>
            <span class="k">val</span> <span class="py">tasks</span> <span class="p">=</span> <span class="n">urls</span><span class="p">.</span><span class="n">map</span> <span class="p">{</span> <span class="n">url</span> <span class="o">-&gt;</span> <span class="nc">Callable</span> <span class="p">{</span> <span class="nf">scrapeUrl</span><span class="p">(</span><span class="n">url</span><span class="p">)</span> <span class="p">}</span> <span class="p">}</span>
            <span class="n">executor</span><span class="p">.</span><span class="n">invokeAll</span><span class="p">(</span><span class="n">tasks</span><span class="p">).</span><span class="n">map</span> <span class="p">{</span> <span class="n">it</span><span class="p">.</span><span class="n">get</span><span class="p">()</span> <span class="p">}</span>
        <span class="p">}</span> <span class="k">finally</span> <span class="p">{</span>
            <span class="n">executor</span><span class="p">.</span><span class="n">shutdown</span><span class="p">()</span>
        <span class="p">}</span>
    <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
</div>
</div>

**Problem**: With 16 threads and 1000 URLs, only 16 requests can run concurrently. The rest queue up.

## After: Virtual Threads Approach

With Java 21's virtual threads, the migration is surprisingly simple:

<div class="code-tabs" data-tabs-id="virtual-threads-approach">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">class</span> <span class="nc">WebScraperVirtual</span> <span class="o">{</span>
    <span class="kd">private</span> <span class="kd">final</span> <span class="nc">HttpClient</span> <span class="n">httpClient</span> <span class="o">=</span> <span class="nc">HttpClient</span><span class="o">.</span><span class="na">newBuilder</span><span class="o">()</span>
            <span class="o">.</span><span class="na">connectTimeout</span><span class="o">(</span><span class="nc">Duration</span><span class="o">.</span><span class="na">ofSeconds</span><span class="o">(</span><span class="mi">10</span><span class="o">))</span>
            <span class="o">.</span><span class="na">build</span><span class="o">();</span>

    <span class="kd">public</span> <span class="nc">List</span><span class="o">&lt;</span><span class="nc">ScrapedResult</span><span class="o">&gt;</span> <span class="nf">scrapeAll</span><span class="o">(</span><span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">urls</span><span class="o">)</span> <span class="o">{</span>
        <span class="c1">// newVirtualThreadPerTaskExecutor() - the key change!</span>
        <span class="c1">// Creates a new virtual thread for each task</span>
        <span class="k">try</span> <span class="o">(</span><span class="nc">ExecutorService</span> <span class="n">executor</span> <span class="o">=</span> <span class="nc">Executors</span><span class="o">.</span><span class="na">newVirtualThreadPerTaskExecutor</span><span class="o">())</span> <span class="o">{</span>
            <span class="nc">List</span><span class="o">&lt;</span><span class="nc">Future</span><span class="o">&lt;</span><span class="nc">ScrapedResult</span><span class="o">&gt;&gt;</span> <span class="n">futures</span> <span class="o">=</span> <span class="n">urls</span><span class="o">.</span><span class="na">stream</span><span class="o">()</span>
                    <span class="o">.</span><span class="na">map</span><span class="o">(</span><span class="n">url</span> <span class="o">-&gt;</span> <span class="n">executor</span><span class="o">.</span><span class="na">submit</span><span class="o">(()</span> <span class="o">-&gt;</span> <span class="nf">scrapeUrl</span><span class="o">(</span><span class="n">url</span><span class="o">)))</span>
                    <span class="o">.</span><span class="na">toList</span><span class="o">();</span>

            <span class="k">return</span> <span class="n">futures</span><span class="o">.</span><span class="na">stream</span><span class="o">().</span><span class="na">map</span><span class="o">(</span><span class="n">f</span> <span class="o">-&gt;</span> <span class="n">f</span><span class="o">.</span><span class="na">get</span><span class="o">()).</span><span class="na">toList</span><span class="o">();</span>
        <span class="o">}</span>
    <span class="o">}</span>

    <span class="c1">// The scrapeUrl method is IDENTICAL to before!</span>
    <span class="c1">// Blocking code works efficiently with virtual threads</span>
    <span class="kd">private</span> <span class="nc">ScrapedResult</span> <span class="nf">scrapeUrl</span><span class="o">(</span><span class="nc">String</span> <span class="n">url</span><span class="o">)</span> <span class="o">{</span>
        <span class="nc">HttpResponse</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">response</span> <span class="o">=</span> <span class="n">httpClient</span><span class="o">.</span><span class="na">send</span><span class="o">(</span><span class="n">request</span><span class="o">,</span> <span class="nc">HttpResponse</span><span class="o">.</span><span class="na">BodyHandlers</span><span class="o">.</span><span class="na">ofString</span><span class="o">());</span>
        <span class="k">return</span> <span class="k">new</span> <span class="nc">ScrapedResult</span><span class="o">(</span><span class="n">url</span><span class="o">,</span> <span class="n">response</span><span class="o">.</span><span class="na">statusCode</span><span class="o">(),</span> <span class="n">response</span><span class="o">.</span><span class="na">body</span><span class="o">().</span><span class="na">length</span><span class="o">());</span>
    <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">object</span> <span class="nc">WebScraperVirtual</span><span class="o">:</span>
  <span class="k">def</span> <span class="nf">scrapeAll</span><span class="o">(</span><span class="n">urls</span><span class="o">:</span> <span class="kt">List</span><span class="o">[</span><span class="kt">String</span><span class="o">]):</span> <span class="kt">List</span><span class="o">[</span><span class="nc">ScrapedResult</span><span class="o">]</span> <span class="o">=</span>
    <span class="c1">// One virtual thread per URL - all run concurrently!</span>
    <span class="nc">Using</span><span class="o">.</span><span class="n">resource</span><span class="o">(</span><span class="nc">Executors</span><span class="o">.</span><span class="n">newVirtualThreadPerTaskExecutor</span><span class="o">())</span> <span class="o">{</span> <span class="n">executor</span> <span class="o">=&gt;</span>
      <span class="k">val</span> <span class="n">futures</span> <span class="o">=</span> <span class="n">urls</span><span class="o">.</span><span class="n">map</span><span class="o">(</span><span class="n">url</span> <span class="o">=&gt;</span> <span class="n">executor</span><span class="o">.</span><span class="n">submit</span><span class="o">(()</span> <span class="o">=&gt;</span> <span class="n">scrapeUrl</span><span class="o">(</span><span class="n">url</span><span class="o">)))</span>
      <span class="n">futures</span><span class="o">.</span><span class="n">map</span><span class="o">(</span><span class="n">_</span><span class="o">.</span><span class="n">get</span><span class="o">()).</span><span class="n">toList</span>
    <span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">object</span> <span class="nc">WebScraperVirtual</span> <span class="p">{</span>
    <span class="k">fun</span> <span class="nf">scrapeAll</span><span class="p">(</span><span class="n">urls</span><span class="p">:</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">&gt;):</span> <span class="nc">List</span><span class="p">&lt;</span><span class="nc">ScrapedResult</span><span class="p">&gt;</span> <span class="p">=</span>
        <span class="nc">Executors</span><span class="p">.</span><span class="n">newVirtualThreadPerTaskExecutor</span><span class="p">().</span><span class="n">use</span> <span class="p">{</span> <span class="n">executor</span> <span class="o">-&gt;</span>
            <span class="k">val</span> <span class="py">futures</span> <span class="p">=</span> <span class="n">urls</span><span class="p">.</span><span class="n">map</span> <span class="p">{</span> <span class="n">url</span> <span class="o">-&gt;</span> <span class="n">executor</span><span class="p">.</span><span class="n">submit</span><span class="p">&lt;</span><span class="nc">ScrapedResult</span><span class="p">&gt;</span> <span class="p">{</span> <span class="nf">scrapeUrl</span><span class="p">(</span><span class="n">url</span><span class="p">)</span> <span class="p">}</span> <span class="p">}</span>
            <span class="n">futures</span><span class="p">.</span><span class="n">map</span> <span class="p">{</span> <span class="n">it</span><span class="p">.</span><span class="n">get</span><span class="p">()</span> <span class="p">}</span>
        <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
</div>
</div>

**The change**: Just replace `newFixedThreadPool(N)` with `newVirtualThreadPerTaskExecutor()`.

## Key Virtual Thread APIs

### Thread.startVirtualThread()

The simplest way to start a virtual thread:

```java
Thread.startVirtualThread(() -> {
    // Your code runs in a virtual thread
    System.out.println("Hello from virtual thread!");
});
```

### Thread.ofVirtual() Builder

For more control over thread creation:

```java
Thread thread = Thread.ofVirtual()
    .name("scraper-", 0)  // Named threads: scraper-0, scraper-1, etc.
    .start(() -> {
        // Your code here
    });
```

### Executors.newVirtualThreadPerTaskExecutor()

The recommended approach for concurrent tasks:

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    // Each submit() creates a new virtual thread
    var future1 = executor.submit(() -> fetchUserData());
    var future2 = executor.submit(() -> fetchProductData());
    
    // All tasks run concurrently
    return combine(future1.get(), future2.get());
}
```

## Structured Concurrency with StructuredTaskScope

Java 21 introduces structured concurrency (preview) for managing related concurrent tasks:

### ShutdownOnFailure - All Must Succeed

```java
public long fetchAllOrFail(List<String> urls) throws Exception {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        // Fork tasks - each runs in its own virtual thread
        List<Subtask<Integer>> subtasks = urls.stream()
                .map(url -> scope.fork(() -> fetchContentLength(url)))
                .toList();

        scope.join();           // Wait for all
        scope.throwIfFailed();  // Throws if any task failed

        // All succeeded - aggregate results
        return subtasks.stream().mapToInt(Subtask::get).sum();
    }
}
```

### ShutdownOnSuccess - First Success Wins

```java
public int fetchAnySuccessful(List<String> mirrors) throws Exception {
    try (var scope = new StructuredTaskScope.ShutdownOnSuccess<Integer>()) {
        // Race multiple mirrors
        for (String url : mirrors) {
            scope.fork(() -> fetchContentLength(url));
        }
        
        scope.join();
        return scope.result();  // Returns first successful result
    }
}
```

### Comparison: ShutdownOnFailure vs ShutdownOnSuccess

<div class="table-wrapper" markdown="1">

| Aspect | ShutdownOnFailure | ShutdownOnSuccess |
|--------|-------------------|-------------------|
| Use case | Need ALL results | Need ANY result |
| On first failure | Cancel all, throw | Continue others |
| On first success | Continue all | Cancel others, return |
| Returns | All results | First success |

</div>

## Scoped Values: Modern Alternative to ThreadLocal

Java 21 introduces ScopedValue (preview) as a replacement for ThreadLocal:

```java
private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();
private static final ScopedValue<UserContext> USER_CONTEXT = ScopedValue.newInstance();

public String handleRequest(String userId, String role, String url) throws Exception {
    String requestId = UUID.randomUUID().toString().substring(0, 8);
    UserContext context = new UserContext(userId, role);

    // Bind scoped values for this request
    return ScopedValue
            .where(REQUEST_ID, requestId)
            .where(USER_CONTEXT, context)
            .call(() -> processRequest(url));
}

private String processRequest(String url) throws Exception {
    // Access scoped values without explicit parameters!
    String requestId = REQUEST_ID.get();
    UserContext user = USER_CONTEXT.get();
    
    log("Processing request for user " + user.userId());
    return fetchWithLogging(url);
}
```

### ThreadLocal vs ScopedValue

<div class="table-wrapper" markdown="1">

| Feature | ThreadLocal | ScopedValue |
|---------|-------------|-------------|
| Mutability | Mutable | Immutable per scope |
| Cleanup | Manual remove() | Automatic with scope |
| Memory | Can leak | Cleaned up automatically |
| Virtual threads | Works, but heavy | Optimized |

</div>

## When to Use Virtual Threads vs Platform Threads

### Use Virtual Threads For:

✅ I/O-bound operations (HTTP calls, database queries, file I/O)  
✅ High-concurrency servers (web servers, API gateways)  
✅ Microservices making many outbound API calls  
✅ Batch processing with parallel I/O operations  
✅ Replacing callback-based async code  

### Use Platform Threads For:

✅ CPU-bound computations (number crunching, cryptography)  
✅ Native code integration (JNI calls)  
✅ Operations requiring thread affinity  
✅ Code using synchronized blocks extensively  

## Migration Checklist

1. **Replace ExecutorService creation**
   ```java
   // Before
   Executors.newFixedThreadPool(200)
   // After
   Executors.newVirtualThreadPerTaskExecutor()
   ```

2. **Replace ThreadLocal with ScopedValue**
   ```java
   // Before
   ThreadLocal<User> currentUser = new ThreadLocal<>();
   // After
   ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();
   ```

3. **Replace synchronized with ReentrantLock** (to avoid "pinning")
   ```java
   // Before - can pin virtual thread
   synchronized (lock) { blockingCall(); }
   
   // After - allows virtual thread to unmount
   lock.lock();
   try { blockingCall(); } 
   finally { lock.unlock(); }
   ```

4. **Consider Structured Concurrency** for related tasks

## For Scala Developers

Virtual threads provide similar benefits to effect systems like ZIO or Cats Effect:

<div class="table-wrapper" markdown="1">

| Feature | Virtual Threads | ZIO/Cats Effect |
|---------|-----------------|-----------------|
| Lightweight concurrency | ✓ | ✓ (Fibers) |
| Non-blocking semantics | ✓ | ✓ |
| Blocking code style | ✓ | Via blocking wrapper |
| Structured concurrency | StructuredTaskScope | Built-in |
| Effect tracking | No | Yes (IO monad) |

</div>

**Key insight**: Virtual threads let you write blocking-style code that scales like async code, without needing an effect system.

## For Kotlin Developers

Virtual threads complement Kotlin coroutines:

<div class="table-wrapper" markdown="1">

| Use Case | Virtual Threads | Coroutines |
|----------|-----------------|------------|
| Kotlin-only codebase | Can use | Preferred |
| Java interop | Preferred | Possible |
| Blocking Java libraries | Excellent | Needs Dispatchers.IO |
| Structured concurrency | StructuredTaskScope | Built-in |

</div>

## Performance Comparison

With 1000 URLs that each take 1 second to fetch:

<div class="table-wrapper" markdown="1">

| Approach | Threads | Time | Memory |
|----------|---------|------|--------|
| Sequential | 1 | ~1000s | ~1MB |
| Thread pool (16) | 16 | ~63s | ~16MB |
| Thread pool (200) | 200 | ~5s | ~200MB |
| Virtual threads | 1000 | ~1s | ~few MB |

</div>

Virtual threads achieve maximum parallelism with minimal memory!

## Interview Q&A: Virtual Threads in Practice

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>What are virtual threads?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Virtual threads are lightweight threads from Java's Project Loom. They let you run a very large number of tasks without using a huge amount of memory, which is especially helpful for I/O-heavy systems. In practical terms, they make the “one thread per task” model much more realistic.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Why do they matter for Java 21?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Because they reduce the cost of concurrency. Traditional platform threads are expensive, so many systems use thread pools with limited size. Virtual threads make it easier to scale to many concurrent requests or tasks without turning the system into a complicated tuning exercise.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>How are they different from platform threads?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Platform threads are tied to the OS thread model and consume more memory. Virtual threads are much lighter, and the JVM can schedule many of them efficiently. That makes them better suited to high-concurrency workloads, especially when tasks spend time waiting on I/O.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>When would I still use a platform thread?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      When the task is CPU-bound, long-lived, or tightly coupled to platform-specific behavior, a platform thread may still be a better fit. Not every concurrency problem needs virtual threads. The right answer is usually: use virtual threads for many small tasks, and platform threads when you need a more traditional lower-level model.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>How do they compare to Kotlin coroutines?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Both aim to make concurrency cheaper and easier to write. Kotlin coroutines are a language-level abstraction with strong syntax support, while Java virtual threads are a platform feature that works with the JVM runtime. They solve similar problems in slightly different ways, and both are useful in their own ecosystems.
    </div>
  </details>
</div>


## Conclusion

Project Loom's virtual threads represent a paradigm shift in Java concurrency:

- **Simple migration**: Often just change `newFixedThreadPool()` to `newVirtualThreadPerTaskExecutor()`
- **Massive scalability**: Handle millions of concurrent operations
- **Familiar code style**: Write blocking code that scales like async
- **Structured concurrency**: Better resource management and cancellation

For I/O-bound workloads, virtual threads provide dramatic simplification while improving scalability. Combined with structured concurrency and scoped values, Java 21 offers a complete modern concurrency toolkit.

## Code Samples

See the complete implementations in our repository:
- [Java 21 Virtual Threads](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/interview/preparation/virtualthreads)
- [Scala 3 Virtual Threads](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/interview/preparation/virtualthreads)
- [Kotlin Virtual Threads](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/virtualthreads)

---

*This post is part of the [Java 21 Interview Preparation Guide - Your Roadmap to Success]({{ site.baseurl }}{% link _posts/2025-11-25-java21-interview-preparation-plan.md %}). Next related posts: [String Templates Preview]({{ site.baseurl }}{% link _posts/2025-11-29-string-templates-preview.md %}) and [Foreign Function and Memory API]({{ site.baseurl }}{% link _posts/2025-11-29-foreign-function-and-memory-api.md %}).*
