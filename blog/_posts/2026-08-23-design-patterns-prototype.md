---
layout: post
title: "Prototype Pattern: Cloning for Success"
description: "Master the prototype design pattern across Java 21, Scala 2, Scala 3, and Kotlin. Learn the difference between shallow and deep copying, why Java's Cloneable interface is risky, and how modern JVM languages handle object cloning more safely."
date: 2026-08-23 10:00:00 +0000
updated: 2026-08-29 14:00:00 +0000
categories: [interview, best-practices]
tags: [java, java21, scala, scala2, scala3, kotlin, design-patterns, creational-patterns, prototype-pattern]
---

Imagine you are building a reporting tool. Every month you need a new "Quarterly Report" document that starts with the same title, author, and boilerplate sections, but each department then adds its own custom sections. Creating the report from scratch every time is tedious and error-prone. Wouldn't it be easier to take an existing report, copy it, and tweak the copy?

That is exactly what the Prototype pattern does: it creates new objects by copying an existing object, called the prototype, rather than building them from scratch.

In this post, we'll implement the Prototype pattern in Java 21, Kotlin, Scala 2, and Scala 3. We'll see why shallow copies can be dangerous, when deep copies matter, and how language features like Kotlin's `data class` and Scala's case classes make cloning far safer than Java's `Cloneable` interface.

## The Problem: Templates That Need Their Own Identity

Let's say you have a `Document` class that contains:

- A title.
- An author.
- A list of sections.

You want to produce a new document from a template. A naive approach is to construct a fresh object each time, but if the template is complex, that becomes repetitive. The Prototype pattern lets you say: "Give me a copy of this object, and I'll adjust it."

The danger is that a simple copy might share mutable state with the original. If one copy adds a section, the template might unexpectedly grow too. That is the shallow vs deep copy problem.

## Key Concepts

<div class="table-wrapper" markdown="1">

| Concept | What it means | Risk |
|---------|---------------|------|
| **Shallow copy** | Copies the object but keeps references to the same nested objects | Mutating nested state leaks between copies |
| **Deep copy** | Recursively copies nested objects so each copy is fully independent | More code, easy to forget a field |
| **Cloneable** | Java's marker interface enabling `Object.clone()` | Weak contract, checked exception, hard to get right |

</div>

## The Solution: Prototype Implementations Across Languages

Below is the same document prototype idea in Java 21, Kotlin, Scala 2, and Scala 3. Java shows the classic `Cloneable` approach plus a safer deep-copy method; the other languages rely on language-level copy mechanisms.

<div class="code-tabs" data-tabs-id="tabs-prototype-impl">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">class</span> <span class="nc">Document</span> <span class="kd">implements</span> <span class="nc">Cloneable</span> <span class="o">{</span>
    <span class="kd">private</span> <span class="nc">String</span> <span class="n">title</span><span class="o">;</span>
    <span class="kd">private</span> <span class="nc">String</span> <span class="n">author</span><span class="o">;</span>
    <span class="kd">private</span> <span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">sections</span><span class="o">;</span>

    <span class="kd">public</span> <span class="nf">Document</span><span class="o">(</span><span class="nc">String</span> <span class="n">title</span><span class="o">,</span> <span class="nc">String</span> <span class="n">author</span><span class="o">,</span> <span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">sections</span><span class="o">)</span> <span class="o">{</span>
        <span class="k">this</span><span class="o">.</span><span class="na">title</span> <span class="o">=</span> <span class="n">title</span><span class="o">;</span>
        <span class="k">this</span><span class="o">.</span><span class="na">author</span> <span class="o">=</span> <span class="n">author</span><span class="o">;</span>
        <span class="k">this</span><span class="o">.</span><span class="na">sections</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">ArrayList</span><span class="o">&lt;&gt;(</span><span class="n">sections</span><span class="o">);</span>
    <span class="o">}</span>

    <span class="nd">@Override</span>
    <span class="kd">public</span> <span class="nc">Document</span> <span class="nf">clone</span><span class="o">()</span> <span class="o">{</span>
        <span class="k">try</span> <span class="o">{</span>
            <span class="k">return</span> <span class="o">(</span><span class="nc">Document</span><span class="o">)</span> <span class="kd">super</span><span class="o">.</span><span class="na">clone</span><span class="o">();</span>
        <span class="o">}</span> <span class="k">catch</span> <span class="o">(</span><span class="nc">CloneNotSupportedException</span> <span class="n">e</span><span class="o">)</span> <span class="o">{</span>
            <span class="k">throw</span> <span class="k">new</span> <span class="nf">AssertionError</span><span class="o">(</span><span class="n">e</span><span class="o">);</span>
        <span class="o">}</span>
    <span class="o">}</span>

    <span class="kd">public</span> <span class="nc">Document</span> <span class="nf">deepCopy</span><span class="o">()</span> <span class="o">{</span>
        <span class="k">return</span> <span class="k">new</span> <span class="nf">Document</span><span class="o">(</span><span class="n">title</span><span class="o">,</span> <span class="n">author</span><span class="o">,</span> <span class="k">new</span> <span class="nc">ArrayList</span><span class="o">&lt;&gt;(</span><span class="n">sections</span><span class="o">));</span>
    <span class="o">}</span>

    <span class="kd">public</span> <span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="nf">getSections</span><span class="o">()</span> <span class="o">{</span> <span class="k">return</span> <span class="n">sections</span><span class="o">;</span> <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/designpatterns/prototype/Document.java">View in repository</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">data</span> <span class="kd">class</span> <span class="nc">Document</span><span class="p">(</span>
    <span class="kd">val</span> <span class="py">title</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span>
    <span class="kd">val</span> <span class="py">author</span><span class="p">:</span> <span class="nc">String</span><span class="p">,</span>
    <span class="kd">val</span> <span class="py">sections</span><span class="p">:</span> <span class="nc">MutableList</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">&gt;</span> <span class="p">=</span> <span class="nf">mutableListOf</span><span class="p">(),</span>
<span class="p">)</span> <span class="p">{</span>
    <span class="k">fun</span> <span class="nf">deepCopy</span><span class="p">():</span> <span class="nc">Document</span> <span class="p">=</span> <span class="nf">copy</span><span class="p">(</span><span class="n">sections</span> <span class="p">=</span> <span class="n">sections</span><span class="p">.</span><span class="nf">toMutableList</span><span class="p">())</span>
<span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/designpatterns/prototype/Document.kt">View in repository</a></p>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">case</span> <span class="k">class</span> <span class="nc">Document</span><span class="o">(</span>
    <span class="n">title</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span>
    <span class="n">author</span><span class="k">:</span> <span class="kt">String</span><span class="o">,</span>
    <span class="n">sections</span><span class="k">:</span> <span class="kt">List</span><span class="o">[</span><span class="kt">String</span><span class="o">]</span> <span class="k">=</span> <span class="nc">Nil</span>
<span class="o">)</span> <span class="o">{</span>

  <span class="k">def</span> <span class="n">addSection</span><span class="o">(</span><span class="n">section</span><span class="k">:</span> <span class="kt">String</span><span class="o">)</span><span class="k">:</span> <span class="kt">Document</span> <span class="o">=</span>
    <span class="nf">copy</span><span class="o">(</span><span class="n">sections</span> <span class="k">=</span> <span class="n">sections</span> <span class="o">:+</span> <span class="n">section</span><span class="o">)</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/main/scala/io/github/sps23/designpatterns/prototype/Document.scala">View in repository</a></p>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">case</span> <span class="k">class</span> <span class="nc">Document</span><span class="o">(</span>
    <span class="n">title</span><span class="o">:</span> <span class="kt">String</span><span class="o">,</span>
    <span class="n">author</span><span class="o">:</span> <span class="kt">String</span><span class="o">,</span>
    <span class="n">sections</span><span class="o">:</span> <span class="kt">List</span><span class="o">[</span><span class="kt">String</span><span class="o">]</span> <span class="k">=</span> <span class="nc">Nil</span>
<span class="o">)</span><span class="k">:</span>

  <span class="k">def</span> <span class="n">addSection</span><span class="o">(</span><span class="n">section</span><span class="o">:</span> <span class="kt">String</span><span class="o">)</span><span class="k">:</span> <span class="kt">Document</span> <span class="o">=</span>
    <span class="n">copy</span><span class="o">(</span><span class="n">sections</span> <span class="k">=</span> <span class="n">sections</span> <span class="o">:+</span> <span class="n">section</span><span class="o">)</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/designpatterns/prototype/Document.scala">View in repository</a></p>
</div>
</div>

### Scala Developer Mental Model

- In **Java 21**, the Prototype pattern is usually tied to the `Cloneable` interface and `Object.clone()`. It works, but the contract is weak: `clone()` is `protected`, throws a checked exception, and produces a shallow copy by default. A dedicated `deepCopy()` method or copy constructor is usually safer.
- In **Kotlin**, `data class` gives you a `copy()` method for free. It is the preferred prototype mechanism, but remember that it copies references for mutable nested state, so add a `deepCopy()` helper when needed.
- In **Scala 2/3**, case classes give you `copy()` automatically. Because the default collections are immutable, the shallow vs deep distinction almost disappears: you get a new value, and the original cannot be mutated through its reference.

## Shallow vs Deep Copy in Action

The snippet below shows the practical difference between a shallow clone and a deep copy in Java and Kotlin. In Scala, the same test demonstrates that `copy()` produces an independent value.

<div class="code-tabs" data-tabs-id="tabs-prototype-usage">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nc">Document</span> <span class="n">original</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">Document</span><span class="o">(</span><span class="s">"Report"</span><span class="o">,</span> <span class="s">"Ada"</span><span class="o">,</span>
    <span class="k">new</span> <span class="nc">ArrayList</span><span class="o">&lt;&gt;(</span><span class="nc">List</span><span class="o">.</span><span class="na">of</span><span class="o">(</span><span class="s">"Introduction"</span><span class="o">)));</span>

<span class="nc">Document</span> <span class="n">shallow</span> <span class="o">=</span> <span class="n">original</span><span class="o">.</span><span class="na">clone</span><span class="o">();</span>
<span class="n">shallow</span><span class="o">.</span><span class="na">getSections</span><span class="o">().</span><span class="na">add</span><span class="o">(</span><span class="s">"Conclusion"</span><span class="o">);</span>

<span class="c1">// ❌ original now also has "Conclusion"</span>

<span class="nc">Document</span> <span class="n">deep</span> <span class="o">=</span> <span class="n">original</span><span class="o">.</span><span class="na">deepCopy</span><span class="o">();</span>
<span class="n">deep</span><span class="o">.</span><span class="na">getSections</span><span class="o">().</span><span class="na">add</span><span class="o">(</span><span class="s">"Appendix"</span><span class="o">);</span>

<span class="c1">// ✓ original is untouched</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">val</span> <span class="py">original</span> <span class="p">=</span> <span class="nc">Document</span><span class="p">(</span><span class="s">"Report"</span><span class="p">,</span> <span class="s">"Ada"</span><span class="p">,</span> <span class="nf">mutableListOf</span><span class="p">(</span><span class="s">"Introduction"</span><span class="p">))</span>

<span class="kd">val</span> <span class="py">shallow</span> <span class="p">=</span> <span class="n">original</span><span class="p">.</span><span class="nf">copy</span><span class="p">()</span>
<span class="n">shallow</span><span class="p">.</span><span class="n">sections</span><span class="p">.</span><span class="nf">add</span><span class="p">(</span><span class="s">"Conclusion"</span><span class="p">)</span>

<span class="c1">// ❌ original.sections now also has "Conclusion"</span>

<span class="kd">val</span> <span class="py">deep</span> <span class="p">=</span> <span class="n">original</span><span class="p">.</span><span class="nf">deepCopy</span><span class="p">()</span>
<span class="n">deep</span><span class="p">.</span><span class="n">sections</span><span class="p">.</span><span class="nf">add</span><span class="p">(</span><span class="s">"Appendix"</span><span class="p">)</span>

<span class="c1">// ✓ original is untouched</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">val</span> <span class="nv">original</span> <span class="k">=</span> <span class="nc">Document</span><span class="o">(</span><span class="s">"Report"</span><span class="o">,</span> <span class="s">"Ada"</span><span class="o">,</span> <span class="nc">List</span><span class="o">(</span><span class="s">"Introduction"</span><span class="o">))</span>
<span class="k">val</span> <span class="nv">updated</span> <span class="k">=</span> <span class="nv">original</span><span class="o">.</span><span class="n">addSection</span><span class="o">(</span><span class="s">"Conclusion"</span><span class="o">)</span>

<span class="c1">// ✓ original is unchanged because List is immutable</span>
<span class="c1">// updated.sections == List("Introduction", "Conclusion")</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">val</span> <span class="n">original</span> <span class="k">=</span> <span class="nc">Document</span><span class="o">(</span><span class="s">"Report"</span><span class="o">,</span> <span class="s">"Ada"</span><span class="o">,</span> <span class="nc">List</span><span class="o">(</span><span class="s">"Introduction"</span><span class="o">))</span>
<span class="k">val</span> <span class="n">updated</span> <span class="k">=</span> <span class="n">original</span><span class="o">.</span><span class="n">addSection</span><span class="o">(</span><span class="s">"Conclusion"</span><span class="o">)</span>

<span class="c1">// ✓ original is unchanged because List is immutable</span>
<span class="c1">// updated.sections == List("Introduction", "Conclusion")</span>
</code></pre></div></div>
</div>
</div>

## Comparison: Java 21 vs Scala 2 vs Scala 3 vs Kotlin

<div class="table-wrapper" markdown="1">

| Language | Prototype mechanism | Handles nested mutable state | Boilerplate |
|----------|---------------------|------------------------------|-------------|
| **Java 21** | `Cloneable` + `clone()`, or copy constructor | Manual deep copy required | High |
| **Kotlin** | `data class` `copy()` + custom `deepCopy()` | Manual deep copy for mutable nested state | Low |
| **Scala 2** | case class `copy()` | Immutability makes it safe by default | Very low |
| **Scala 3** | case class `copy()` (cleaner syntax) | Immutability makes it safe by default | Very low |

</div>

## Testing the Prototype

Prototype tests should prove two things:

1. The copy is a different object.
2. Mutating the copy does not affect the original (for deep copies).

<div class="code-tabs" data-tabs-id="tabs-prototype-test">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="nd">@DisplayName</span><span class="o">(</span><span class="s">"Deep copy should create an independent document"</span><span class="o">)</span>
<span class="kt">void</span> <span class="nf">deepCopyShouldBeIndependent</span><span class="o">()</span> <span class="o">{</span>
    <span class="nc">Document</span> <span class="n">original</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">Document</span><span class="o">(</span><span class="s">"Annual Report"</span><span class="o">,</span> <span class="s">"Ada"</span><span class="o">,</span>
            <span class="k">new</span> <span class="nc">ArrayList</span><span class="o">&lt;&gt;(</span><span class="nc">List</span><span class="o">.</span><span class="na">of</span><span class="o">(</span><span class="s">"Introduction"</span><span class="o">,</span> <span class="s">"Market Analysis"</span><span class="o">)));</span>

    <span class="nc">Document</span> <span class="n">deepCopy</span> <span class="o">=</span> <span class="n">original</span><span class="o">.</span><span class="na">deepCopy</span><span class="o">();</span>
    <span class="n">deepCopy</span><span class="o">.</span><span class="na">getSections</span><span class="o">().</span><span class="na">add</span><span class="o">(</span><span class="s">"Conclusion"</span><span class="o">);</span>

    <span class="n">assertNotSame</span><span class="o">(</span><span class="n">original</span><span class="o">,</span> <span class="n">deepCopy</span><span class="o">);</span>
    <span class="n">assertEquals</span><span class="o">(</span><span class="mi">2</span><span class="o">,</span> <span class="n">original</span><span class="o">.</span><span class="na">getSections</span><span class="o">().</span><span class="na">size</span><span class="o">());</span>
    <span class="n">assertEquals</span><span class="o">(</span><span class="mi">3</span><span class="o">,</span> <span class="n">deepCopy</span><span class="o">.</span><span class="na">getSections</span><span class="o">().</span><span class="na">size</span><span class="o">());</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/designpatterns/prototype/DocumentTest.java">View full test file</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="k">fun</span> <span class="nf">deepCopyShouldBeIndependent</span><span class="p">()</span> <span class="p">{</span>
    <span class="kd">val</span> <span class="py">original</span> <span class="p">=</span> <span class="nc">Document</span><span class="p">(</span><span class="s">"Annual Report"</span><span class="p">,</span> <span class="s">"Ada"</span><span class="p">,</span>
        <span class="nf">mutableListOf</span><span class="p">(</span><span class="s">"Introduction"</span><span class="p">,</span> <span class="s">"Market Analysis"</span><span class="p">))</span>
    <span class="kd">val</span> <span class="py">deepCopy</span> <span class="p">=</span> <span class="n">original</span><span class="p">.</span><span class="nf">deepCopy</span><span class="p">()</span>
    <span class="n">deepCopy</span><span class="p">.</span><span class="n">sections</span><span class="p">.</span><span class="nf">add</span><span class="p">(</span><span class="s">"Conclusion"</span><span class="p">)</span>

    <span class="nf">assertNotSame</span><span class="p">(</span><span class="n">original</span><span class="p">,</span> <span class="n">deepCopy</span><span class="p">)</span>
    <span class="nf">assertEquals</span><span class="p">(</span><span class="m">2</span><span class="p">,</span> <span class="n">original</span><span class="p">.</span><span class="n">sections</span><span class="p">.</span><span class="n">size</span><span class="p">)</span>
    <span class="nf">assertEquals</span><span class="p">(</span><span class="m">3</span><span class="p">,</span> <span class="n">deepCopy</span><span class="p">.</span><span class="n">sections</span><span class="p">.</span><span class="n">size</span><span class="p">)</span>
<span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/designpatterns/prototype/DocumentTest.kt">View full test file</a></p>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="nd">@DisplayName</span><span class="o">(</span><span class="s">"copy() should create an independent document"</span><span class="o">)</span>
<span class="k">def</span> <span class="nf">copyShouldCreateIndependentDocument</span><span class="o">()</span><span class="k">:</span> <span class="kt">Unit</span> <span class="o">=</span> <span class="o">{</span>
  <span class="k">val</span> <span class="nv">original</span> <span class="k">=</span> <span class="nc">Document</span><span class="o">(</span><span class="s">"Annual Report"</span><span class="o">,</span> <span class="s">"Ada"</span><span class="o">,</span>
    <span class="nc">List</span><span class="o">(</span><span class="s">"Introduction"</span><span class="o">,</span> <span class="s">"Market Analysis"</span><span class="o">))</span>
  <span class="k">val</span> <span class="nv">updated</span> <span class="k">=</span> <span class="nv">original</span><span class="o">.</span><span class="py">addSection</span><span class="o">(</span><span class="s">"Conclusion"</span><span class="o">)</span>

  <span class="nf">assertNotSame</span><span class="o">(</span><span class="n">original</span><span class="o">,</span> <span class="n">updated</span><span class="o">)</span>
  <span class="nf">assertEquals</span><span class="o">(</span><span class="mi">2</span><span class="o">,</span> <span class="nv">original</span><span class="o">.</span><span class="py">sections</span><span class="o">.</span><span class="py">size</span><span class="o">)</span>
  <span class="nf">assertEquals</span><span class="o">(</span><span class="mi">3</span><span class="o">,</span> <span class="nv">updated</span><span class="o">.</span><span class="py">sections</span><span class="o">.</span><span class="py">size</span><span class="o">)</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/test/scala/io/github/sps23/designpatterns/prototype/DocumentTest.scala">View full test file</a></p>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="nd">@DisplayName</span><span class="o">(</span><span class="s">"copy() should create an independent document"</span><span class="o">)</span>
<span class="k">def</span> <span class="n">copyShouldCreateIndependentDocument</span><span class="o">()</span><span class="k">:</span> <span class="kt">Unit</span> <span class="o">=</span>
  <span class="k">val</span> <span class="n">original</span> <span class="k">=</span> <span class="nc">Document</span><span class="o">(</span><span class="s">"Annual Report"</span><span class="o">,</span> <span class="s">"Ada"</span><span class="o">,</span>
    <span class="nc">List</span><span class="o">(</span><span class="s">"Introduction"</span><span class="o">,</span> <span class="s">"Market Analysis"</span><span class="o">))</span>
  <span class="k">val</span> <span class="n">updated</span> <span class="k">=</span> <span class="n">original</span><span class="o">.</span><span class="n">addSection</span><span class="o">(</span><span class="s">"Conclusion"</span><span class="o">)</span>

  <span class="n">assertNotSame</span><span class="o">(</span><span class="n">original</span><span class="o">,</span> <span class="n">updated</span><span class="o">)</span>
  <span class="n">assertEquals</span><span class="o">(</span><span class="mi">2</span><span class="o">,</span> <span class="n">original</span><span class="o">.</span><span class="n">sections</span><span class="o">.</span><span class="n">size</span><span class="o">)</span>
  <span class="n">assertEquals</span><span class="o">(</span><span class="mi">3</span><span class="o">,</span> <span class="n">updated</span><span class="o">.</span><span class="n">sections</span><span class="o">.</span><span class="n">size</span><span class="o">)</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/designpatterns/prototype/DocumentTest.scala">View full test file</a></p>
</div>
</div>

## When to Use the Prototype Pattern

Use the Prototype pattern when:

1. Creating an object from scratch is expensive or complex.
2. Objects are similar and only differ in a few fields or nested values.
3. You want to avoid subclass explosion just to vary object state.
4. You need to preserve a known-good configuration and branch from it.

Avoid it when:

1. Objects are cheap to construct and state is simple.
2. Your language gives you safer copy mechanisms (Kotlin `data class`, Scala case class).
3. Deep-copy logic becomes so complex that a builder or factory is clearer.

## Interview Q&A: Prototype Pattern in Practice

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>What problem does the Prototype pattern solve?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      It solves the problem of creating a new object that is very similar to an existing one. Instead of rebuilding everything from scratch, you clone or copy an existing object and then change only the pieces you need. This is useful when object creation is expensive or when you want to start from a known-good model.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>What is the difference between shallow copy and deep copy?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      A shallow copy creates a new object, but it may still share inner objects with the original. A deep copy creates a fully independent duplicate of the nested data as well. This matters a lot when the object has lists, maps, or child objects. If you only copy the top-level object, the two versions may still affect each other unexpectedly.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Why is the Prototype pattern less common in modern Java?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Modern Java often uses constructors, records, and immutable data structures, which makes copying simpler and safer in many cases. The prototype pattern is still useful when you have a complex object or a configuration template that should be cloned repeatedly, but in everyday code it is less common than it used to be. The language and standard libraries have reduced the need for it in many situations.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Can you give a real-world example of Prototype?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Think of a document template or an email draft. You start with a base version that already has the right layout, brand styling, and default sections. Then you create a copy for a new client or a new campaign and adjust only the small differences. That is a good use of prototype-style cloning because the base object already has the correct structure.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Why do Scala and Kotlin make Prototype simpler?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Scala case classes and Kotlin data classes already give you a clean, safe copy operation. In many projects, that reduces the need for a custom prototype implementation. The point of the pattern is still the same: create a new object based on an existing one. The modern language features just make that easier and less error-prone.
    </div>
  </details>
</div>

## Code Samples

All examples in this post are available in the repository:

**Implementation files:**
- **Java 21:** [Document.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/designpatterns/prototype/Document.java)
- **Kotlin:** [Document.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/designpatterns/prototype/Document.kt)
- **Scala 2:** [Document.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/main/scala/io/github/sps23/designpatterns/prototype/Document.scala)
- **Scala 3:** [Document.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/designpatterns/prototype/Document.scala)

**Test files:**
- **Java 21:** [DocumentTest.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/designpatterns/prototype/DocumentTest.java)
- **Kotlin:** [DocumentTest.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/designpatterns/prototype/DocumentTest.kt)
- **Scala 2:** [DocumentTest.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/test/scala/io/github/sps23/designpatterns/prototype/DocumentTest.scala)
- **Scala 3:** [DocumentTest.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/designpatterns/prototype/DocumentTest.scala)

---

## Key Takeaways

1. **In Java 21**, prefer a copy constructor or dedicated `deepCopy()` method over `Cloneable`. If you use `clone()`, remember it produces a shallow copy by default.
2. **In Kotlin**, `data class` `copy()` is the idiomatic prototype. Add a `deepCopy()` helper when you have mutable nested state.
3. **In Scala 2/3**, case classes and immutable collections make prototypes trivial and safe. The shallow vs deep distinction rarely matters.
4. **Deep copies are only necessary when nested state is mutable**. If everything is immutable, a reference copy is already safe.
5. **The Prototype pattern is about saving construction cost**, not about avoiding constructors entirely. Use it where it actually simplifies the code.

---

*This post is part of the [Design Patterns in JVM Languages - Your Guide to the Top 10]({{ site.baseurl }}{% link _posts/2026-07-26-design-patterns-guide-jvm.md %}). Next related posts: [Adapter Pattern: Making Incompatible Payment APIs Work Together]({{ site.baseurl }}{% link _posts/2026-08-25-design-patterns-adapter.md %}) and [Decorator Pattern: Wrapping Objects with Style]({{ site.baseurl }}{% link _posts/2026-08-27-design-patterns-decorator.md %}).*
