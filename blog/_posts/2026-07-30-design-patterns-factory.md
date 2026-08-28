---
layout: post
title: "Factory Pattern: Let Someone Else Do the Creating"
description: "Learn how to implement the Factory Pattern in Java 21, Scala 2, Scala 3, and Kotlin to decouple object creation, reduce branching, and make notification workflows easier to test."
date: 2026-07-30 16:00:00 +0000
categories: [interview, best-practices]
tags: [java, java21, scala, scala2, scala3, kotlin, design-patterns, creational-patterns, factory-pattern]
---

Imagine your code needs to send notifications through email, SMS, or push channels based on runtime input. If every service does `if/else` or `switch` and constructs concrete classes directly, object creation logic spreads everywhere, and every new channel means touching multiple files.

That is exactly where the Factory Pattern helps: you ask for a type of object, and a dedicated factory decides which concrete implementation to return.

## The Problem: Creation Logic Everywhere

Without a factory, client code usually looks like this:

```java
Notification notification;
if (channel.equalsIgnoreCase("email")) {
    notification = new EmailNotification();
} else if (channel.equalsIgnoreCase("sms")) {
    notification = new SmsNotification();
} else {
    notification = new PushNotification();
}
```

This works at first, but it creates three issues:

1. **Tight coupling**: client code depends on concrete classes.
2. **Duplication**: branching logic gets repeated in multiple places.
3. **Poor extensibility**: adding a new type requires editing many callers.

## Key Concepts

<div class="table-wrapper" markdown="1">

| Concept | What it means | Why it matters |
|---------|---------------|----------------|
| Product | Common contract (`Notification`) | Clients depend on behavior, not implementation |
| Concrete Products | `Email`, `Sms`, `Push` classes/objects | Encapsulate channel-specific behavior |
| Factory | A single creation entry point | Centralizes validation and object selection |
| Client | Code that requests notifications | Stays simple and open for extension |

</div>

## The Solution: Factory Across JVM Languages

Below is the same notification factory idea in Java 21, Kotlin, Scala 2, and Scala 3.

<div class="code-tabs" data-tabs-id="tabs-1">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">sealed</span> <span class="kd">interface</span> <span class="nc">Notification</span> <span class="kd">permits</span> <span class="nc">EmailNotification</span><span class="o">,</span> <span class="nc">SmsNotification</span><span class="o">,</span> <span class="nc">PushNotification</span> <span class="o">{</span>
    <span class="nc">String</span> <span class="nf">send</span><span class="o">(</span><span class="nc">String</span> <span class="n">recipient</span><span class="o">,</span> <span class="nc">String</span> <span class="n">message</span><span class="o">);</span>
<span class="o">}</span>

<span class="kd">public</span> <span class="kd">final</span> <span class="kd">class</span> <span class="nc">NotificationFactory</span> <span class="o">{</span>
    <span class="kd">public</span> <span class="kd">static</span> <span class="nc">Notification</span> <span class="nf">create</span><span class="o">(</span><span class="nc">String</span> <span class="n">channel</span><span class="o">)</span> <span class="o">{</span>
        <span class="k">if</span> <span class="o">(</span><span class="n">channel</span> <span class="o">==</span> <span class="kc">null</span> <span class="o">||</span> <span class="n">channel</span><span class="o">.</span><span class="na">isBlank</span><span class="o">())</span> <span class="o">{</span>
            <span class="k">throw</span> <span class="k">new</span> <span class="nc">IllegalArgumentException</span><span class="o">(</span><span class="s">"Notification channel must not be blank"</span><span class="o">);</span>
        <span class="o">}</span>
        <span class="k">return</span> <span class="k">switch</span> <span class="o">(</span><span class="n">channel</span><span class="o">.</span><span class="na">trim</span><span class="o">().</span><span class="na">toLowerCase</span><span class="o">(</span><span class="nc">Locale</span><span class="o">.</span><span class="na">ROOT</span><span class="o">))</span> <span class="o">{</span>
        <span class="k">case</span> <span class="s">"email"</span> <span class="o">-&gt;</span> <span class="k">new</span> <span class="nc">EmailNotification</span><span class="o">();</span>
        <span class="k">case</span> <span class="s">"sms"</span> <span class="o">-&gt;</span> <span class="k">new</span> <span class="nc">SmsNotification</span><span class="o">();</span>
        <span class="k">case</span> <span class="s">"push"</span> <span class="o">-&gt;</span> <span class="k">new</span> <span class="nc">PushNotification</span><span class="o">();</span>
        <span class="k">default</span> <span class="o">-&gt;</span> <span class="k">throw</span> <span class="k">new</span> <span class="nc">IllegalArgumentException</span><span class="o">(</span><span class="s">"Unsupported notification channel: "</span> <span class="o">+</span> <span class="n">channel</span><span class="o">);</span>
        <span class="o">};</span>
    <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/designpatterns/factory/NotificationFactory.java">View in repository</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">sealed</span> <span class="kd">interface</span> <span class="nc">Notification</span> <span class="p">{</span>
    <span class="k">fun</span> <span class="nf">send</span><span class="p">(</span><span class="n">recipient</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span> <span class="n">message</span><span class="p">:</span> <span class="nc">String</span><span class="p">):</span> <span class="nc">String</span>
<span class="p">}</span>

<span class="k">object</span> <span class="nc">NotificationFactory</span> <span class="p">{</span>
    <span class="k">fun</span> <span class="nf">create</span><span class="p">(</span><span class="n">channel</span><span class="p">:</span> <span class="nc">String?</span><span class="p">):</span> <span class="nc">Notification</span> <span class="p">{</span>
        <span class="k">val</span> <span class="py">normalized</span> <span class="p">=</span> <span class="n">channel</span><span class="p">?.</span><span class="n">trim</span><span class="p">()?.</span><span class="n">lowercase</span><span class="p">()?.</span><span class="n">takeIf</span> <span class="p">{</span> <span class="n">it</span><span class="p">.</span><span class="n">isNotEmpty</span><span class="p">()</span> <span class="p">}</span>
            <span class="o">?:</span> <span class="k">throw</span> <span class="nc">IllegalArgumentException</span><span class="p">(</span><span class="s">"Notification channel must not be blank"</span><span class="p">)</span>
        <span class="k">return</span> <span class="k">when</span> <span class="p">(</span><span class="n">normalized</span><span class="p">)</span> <span class="p">{</span>
            <span class="s">"email"</span> <span class="o">-&gt;</span> <span class="nc">EmailNotification</span>
            <span class="s">"sms"</span> <span class="o">-&gt;</span> <span class="nc">SmsNotification</span>
            <span class="s">"push"</span> <span class="o">-&gt;</span> <span class="nc">PushNotification</span>
            <span class="k">else</span> <span class="o">-&gt;</span> <span class="k">throw</span> <span class="nc">IllegalArgumentException</span><span class="p">(</span><span class="s">"Unsupported notification channel: </span><span class="si">$</span><span class="n">normalized</span><span class="s">"</span><span class="p">)</span>
        <span class="p">}</span>
    <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/designpatterns/factory/NotificationFactory.kt">View in repository</a></p>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">sealed</span> <span class="k">trait</span> <span class="nc">Notification</span> <span class="o">{</span>
  <span class="k">def</span> <span class="n">send</span><span class="o">(</span><span class="n">recipient</span><span class="o">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">message</span><span class="o">:</span> <span class="kt">String</span><span class="o">):</span> <span class="kt">String</span>
<span class="o">}</span>

<span class="k">object</span> <span class="nc">NotificationFactory</span> <span class="o">{</span>
  <span class="k">def</span> <span class="n">create</span><span class="o">(</span><span class="n">channel</span><span class="o">:</span> <span class="kt">String</span><span class="o">):</span> <span class="kt">Notification</span> <span class="o">=</span>
    <span class="nc">Option</span><span class="o">(</span><span class="n">channel</span><span class="o">).</span><span class="n">map</span><span class="o">(_.</span><span class="n">trim</span><span class="o">.</span><span class="n">toLowerCase</span><span class="o">).</span><span class="n">filter</span><span class="o">(_.</span><span class="n">nonEmpty</span><span class="o">).</span><span class="n">map</span> <span class="o">{</span>
      <span class="k">case</span> <span class="s">"email"</span> <span class="o">=&gt;</span> <span class="nc">EmailNotification</span>
      <span class="k">case</span> <span class="s">"sms"</span>   <span class="o">=&gt;</span> <span class="nc">SmsNotification</span>
      <span class="k">case</span> <span class="s">"push"</span>  <span class="o">=&gt;</span> <span class="nc">PushNotification</span>
      <span class="k">case</span> <span class="n">other</span>   <span class="o">=&gt;</span> <span class="k">throw</span> <span class="k">new</span> <span class="nc">IllegalArgumentException</span><span class="o">(</span><span class="s">s"Unsupported notification channel: </span><span class="si">$</span><span class="n">other</span><span class="s">"</span><span class="o">)</span>
    <span class="o">}.</span><span class="n">getOrElse</span><span class="o">(</span><span class="k">throw</span> <span class="k">new</span> <span class="nc">IllegalArgumentException</span><span class="o">(</span><span class="s">"Notification channel must not be blank"</span><span class="o">))</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/main/scala/io/github/sps23/designpatterns/factory/NotificationFactory.scala">View in repository</a></p>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">sealed</span> <span class="k">trait</span> <span class="nc">Notification</span><span class="o">:</span>
  <span class="k">def</span> <span class="n">send</span><span class="o">(</span><span class="n">recipient</span><span class="o">:</span> <span class="kt">String</span><span class="o">,</span> <span class="n">message</span><span class="o">:</span> <span class="kt">String</span><span class="o">):</span> <span class="kt">String</span>

<span class="k">object</span> <span class="nc">NotificationFactory</span><span class="o">:</span>
  <span class="k">def</span> <span class="n">create</span><span class="o">(</span><span class="n">channel</span><span class="o">:</span> <span class="kt">String</span><span class="o">):</span> <span class="kt">Notification</span> <span class="o">=</span>
    <span class="nc">Option</span><span class="o">(</span><span class="n">channel</span><span class="o">).</span><span class="n">map</span><span class="o">(_.</span><span class="n">trim</span><span class="o">.</span><span class="n">toLowerCase</span><span class="o">).</span><span class="n">filter</span><span class="o">(_.</span><span class="n">nonEmpty</span><span class="o">).</span><span class="n">map:</span>
      <span class="k">case</span> <span class="s">"email"</span> <span class="o">=&gt;</span> <span class="nc">EmailNotification</span>
      <span class="k">case</span> <span class="s">"sms"</span>   <span class="o">=&gt;</span> <span class="nc">SmsNotification</span>
      <span class="k">case</span> <span class="s">"push"</span>  <span class="o">=&gt;</span> <span class="nc">PushNotification</span>
      <span class="k">case</span> <span class="n">other</span>   <span class="o">=&gt;</span> <span class="k">throw</span> <span class="k">new</span> <span class="nc">IllegalArgumentException</span><span class="o">(</span><span class="s">s"Unsupported notification channel: </span><span class="si">$</span><span class="n">other</span><span class="s">"</span><span class="o">)</span>
    <span class="o">.</span><span class="n">getOrElse</span><span class="o">(</span><span class="k">throw</span> <span class="k">new</span> <span class="nc">IllegalArgumentException</span><span class="o">(</span><span class="s">"Notification channel must not be blank"</span><span class="o">))</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/designpatterns/factory/NotificationFactory.scala">View in repository</a></p>
</div>
</div>

### Scala Developer Mental Model

- In **Java 21**, you usually model the product contract with interfaces/sealed interfaces and route with `switch`.
- In **Scala 2/3**, ADTs (`sealed trait` + case objects/classes) make product families explicit and pattern matching ergonomic.
- In **Kotlin**, `sealed interface` plus `when` gives similar expressiveness with low boilerplate.

## Comparison: Java 21 vs Scala 2 vs Scala 3 vs Kotlin

<div class="table-wrapper" markdown="1">

| Language | Factory branching | Product modeling | Boilerplate |
|----------|-------------------|------------------|-------------|
| Java 21 | `switch` expression | `sealed interface` + classes | Medium |
| Scala 2 | `match` expression | `sealed trait` + case objects | Low |
| Scala 3 | `match` + indentation syntax | `sealed trait` + case objects | Low |
| Kotlin | `when` expression | `sealed interface` + objects | Low |

</div>

## Testing the Factory

Factory tests should verify two things:
1. Correct concrete type is returned for valid input.
2. Invalid input fails fast with a clear error.

<div class="code-tabs" data-tabs-id="tabs-test-1">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="kt">void</span> <span class="nf">shouldCreateEmailNotification</span><span class="o">()</span> <span class="o">{</span>
    <span class="nc">Notification</span> <span class="n">notification</span> <span class="o">=</span> <span class="nc">NotificationFactory</span><span class="o">.</span><span class="na">create</span><span class="o">(</span><span class="s">"email"</span><span class="o">);</span>
    <span class="n">assertInstanceOf</span><span class="o">(</span><span class="nc">EmailNotification</span><span class="o">.</span><span class="na">class</span><span class="o">,</span> <span class="n">notification</span><span class="o">);</span>
<span class="o">}</span>

<span class="nd">@Test</span>
<span class="kt">void</span> <span class="nf">shouldRejectUnsupportedChannels</span><span class="o">()</span> <span class="o">{</span>
    <span class="nc">IllegalArgumentException</span> <span class="n">error</span> <span class="o">=</span> <span class="n">assertThrows</span><span class="o">(</span><span class="nc">IllegalArgumentException</span><span class="o">.</span><span class="na">class</span><span class="o">,</span>
        <span class="o">()</span> <span class="o">-&gt;</span> <span class="nc">NotificationFactory</span><span class="o">.</span><span class="na">create</span><span class="o">(</span><span class="s">"fax"</span><span class="o">));</span>
    <span class="n">assertEquals</span><span class="o">(</span><span class="s">"Unsupported notification channel: fax"</span><span class="o">,</span> <span class="n">error</span><span class="o">.</span><span class="na">getMessage</span><span class="o">());</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/designpatterns/factory/NotificationFactoryTest.java">View full test file</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="k">fun</span> <span class="nf">shouldCreateEmailNotification</span><span class="p">()</span> <span class="p">{</span>
    <span class="k">val</span> <span class="py">notification</span> <span class="p">=</span> <span class="nc">NotificationFactory</span><span class="p">.</span><span class="nf">create</span><span class="p">(</span><span class="s">"email"</span><span class="p">)</span>
    <span class="nf">assertTrue</span><span class="p">(</span><span class="n">notification</span> <span class="k">is</span> <span class="nc">EmailNotification</span><span class="p">)</span>
<span class="p">}</span>

<span class="nd">@Test</span>
<span class="k">fun</span> <span class="nf">shouldRejectUnsupportedChannels</span><span class="p">()</span> <span class="p">{</span>
    <span class="k">val</span> <span class="py">error</span> <span class="p">=</span> <span class="nf">assertThrows</span><span class="p">(</span><span class="nc">IllegalArgumentException</span><span class="o">::</span><span class="k">class</span><span class="p">.</span><span class="na">java</span><span class="p">)</span> <span class="p">{</span>
        <span class="nc">NotificationFactory</span><span class="p">.</span><span class="nf">create</span><span class="p">(</span><span class="s">"fax"</span><span class="p">)</span>
    <span class="p">}</span>
    <span class="nf">assertEquals</span><span class="p">(</span><span class="s">"Unsupported notification channel: fax"</span><span class="p">,</span> <span class="n">error</span><span class="p">.</span><span class="n">message</span><span class="p">)</span>
<span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/designpatterns/factory/NotificationFactoryTest.kt">View full test file</a></p>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="n">test</span><span class="o">(</span><span class="s">"Factory should create email notification"</span><span class="o">)</span> <span class="o">{</span>
  <span class="nc">NotificationFactory</span><span class="o">.</span><span class="n">create</span><span class="o">(</span><span class="s">"email"</span><span class="o">)</span> <span class="n">shouldBe</span> <span class="nc">EmailNotification</span>
<span class="o">}</span>

<span class="n">test</span><span class="o">(</span><span class="s">"Factory should reject unsupported channels"</span><span class="o">)</span> <span class="o">{</span>
  <span class="k">val</span> <span class="n">error</span> <span class="o">=</span> <span class="n">the</span><span class="o">[</span><span class="kt">IllegalArgumentException</span><span class="o">]</span> <span class="n">thrownBy</span> <span class="nc">NotificationFactory</span><span class="o">.</span><span class="n">create</span><span class="o">(</span><span class="s">"fax"</span><span class="o">)</span>
  <span class="n">error</span><span class="o">.</span><span class="n">getMessage</span> <span class="n">shouldBe</span> <span class="s">"Unsupported notification channel: fax"</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/test/scala/io/github/sps23/designpatterns/factory/NotificationFactoryTest.scala">View full test file</a></p>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="n">test</span><span class="o">(</span><span class="s">"Factory should create email notification"</span><span class="o">)</span> <span class="o">{</span>
  <span class="nc">NotificationFactory</span><span class="o">.</span><span class="n">create</span><span class="o">(</span><span class="s">"email"</span><span class="o">)</span> <span class="n">shouldBe</span> <span class="nc">EmailNotification</span>
<span class="o">}</span>

<span class="n">test</span><span class="o">(</span><span class="s">"Factory should reject unsupported channels"</span><span class="o">)</span> <span class="o">{</span>
  <span class="k">val</span> <span class="n">error</span> <span class="o">=</span> <span class="n">the</span><span class="o">[</span><span class="kt">IllegalArgumentException</span><span class="o">]</span> <span class="n">thrownBy</span> <span class="nc">NotificationFactory</span><span class="o">.</span><span class="n">create</span><span class="o">(</span><span class="s">"fax"</span><span class="o">)</span>
  <span class="n">error</span><span class="o">.</span><span class="n">getMessage</span> <span class="n">shouldBe</span> <span class="s">"Unsupported notification channel: fax"</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/designpatterns/factory/NotificationFactoryTest.scala">View full test file</a></p>
</div>
</div>

## When to Use Factory Pattern

Use a factory when:

1. Construction logic depends on runtime input.
2. You want to hide concrete classes from clients.
3. You want one place to validate and normalize creation input.
4. You expect object families to grow over time.

Avoid it when creation is trivial and unlikely to change; a direct constructor may be simpler.

## Interview Q&A: Factory Pattern in Practice

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>What problem does the Factory pattern solve?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      It solves the problem of object creation being spread across the codebase. Instead of having callers know all the concrete classes and the rules for choosing them, a factory owns that decision. The client asks for the right kind of object by a simple input, and the factory handles the creation logic. This keeps code easier to change when new product types are added later.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>How is a Factory different from a constructor?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      A constructor is usually a direct, fixed way to create one class. A factory is a higher-level decision point that can choose among multiple implementations. That is why factories are useful when the right object depends on runtime data, such as a channel name, a country code, or a user type. Constructors are simpler for straightforward creation; factories are better when creation logic is dynamic or complex.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Why is a factory easier to test?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Because the creation logic is in one place, you can test that logic separately from the rest of the system. You can also replace the factory with a stub or fake in tests, which means your code does not have to know which concrete class it is using. This makes testing easier and lets you change the implementation without rewriting your callers.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Can you give a real-world example of a factory?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      A typical example is a notification system. The application may send emails, SMS messages, or push notifications depending on the user settings or the event type. A factory receives the channel name, checks the valid options, and returns the correct concrete notifier. The rest of the code can treat them all as the same interface without caring which implementation was created.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>When would you avoid a factory?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      If object creation is trivial and the decision is always obvious, a direct constructor or a plain `new` call is simpler. The factory pattern is most valuable when creation logic is not trivial, when you want to hide implementation details, or when the number of possible types is expected to grow. If the code becomes a giant factory with too much business logic, you may need to split responsibilities instead of pushing everything into one class.
    </div>
  </details>
</div>

## Code Samples

All examples in this post are available in the repository:

**Implementation files:**
- **Java 21:** [NotificationFactory.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/designpatterns/factory/NotificationFactory.java)
- **Kotlin:** [NotificationFactory.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/designpatterns/factory/NotificationFactory.kt)
- **Scala 2:** [NotificationFactory.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/main/scala/io/github/sps23/designpatterns/factory/NotificationFactory.scala)
- **Scala 3:** [NotificationFactory.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/designpatterns/factory/NotificationFactory.scala)

**Test files:**
- **Java 21:** [NotificationFactoryTest.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/designpatterns/factory/NotificationFactoryTest.java)
- **Kotlin:** [NotificationFactoryTest.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/designpatterns/factory/NotificationFactoryTest.kt)
- **Scala 2:** [NotificationFactoryTest.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/test/scala/io/github/sps23/designpatterns/factory/NotificationFactoryTest.scala)
- **Scala 3:** [NotificationFactoryTest.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/designpatterns/factory/NotificationFactoryTest.scala)

---

*This is part of our Design Patterns in JVM Languages series. Check out the [full design patterns guide]({{ site.baseurl }}/interview/2026/07/26/design-patterns-guide-jvm) for more patterns and interview preparation.*
