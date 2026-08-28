---
layout: post
title: "Decorator Pattern: Wrapping Objects with Style"
description: "Learn the Decorator pattern with a real report-publishing example in Java 21, Scala 2, Scala 3, and Kotlin: stack compression, encryption, and audit logging at runtime without a class explosion."
date: 2026-08-27 11:00:00 +0000
updated: 2026-08-27 11:00:00 +0000
categories: [interview, best-practices]
tags: [java, java21, scala, scala2, scala3, kotlin, design-patterns, structural-patterns, decorator-pattern]
---

Imagine your platform needs to export financial reports. Some reports are exported as plain text. Some need to be compressed because they are huge. Some contain sensitive data and must be encrypted. Some need an audit trail for compliance. Any given report might need one of these behaviors, all of them, or none at all - and that combination is often only known at runtime, based on user settings or report type.

If you try to solve this with subclassing, you quickly end up with `CompressedReportExporter`, `EncryptedReportExporter`, `CompressedEncryptedReportExporter`, `AuditedCompressedEncryptedReportExporter`, and so on. That is the classic **subclass explosion** the Decorator pattern exists to prevent: instead of baking every combination into the class hierarchy, you wrap a base object with small, focused decorators that can be composed in any order.

## The Problem: One Behavior, Many Optional Add-ons

The base requirement is simple: export report content as a string. The complexity comes from the optional, independently toggleable behaviors layered on top:

- **Compression** for large reports.
- **Encryption** for reports containing sensitive data.
- **Audit logging** for compliance-sensitive exports.

None of these should require touching the others, and none should require the base exporter to know they exist.

## Key Concepts

<div class="table-wrapper" markdown="1">

| Concept | In this example | Why it matters |
|---------|-----------------|----------------|
| Component interface | `ReportExporter` | The stable contract every exporter (base or decorated) implements |
| Concrete component | `PlainTextReportExporter` | The base object that does the real, minimal work |
| Base decorator | `ReportExporterDecorator` | Holds the wrapped component and forwards calls to it |
| Concrete decorators | `CompressionDecorator`, `EncryptionDecorator`, `AuditLoggingDecorator` | Each adds one independent behavior around the wrapped exporter |
| Client | `ReportPublishingService` | Builds a decorator chain from feature flags, unaware of how many decorators are stacked |

</div>

## Real Use Case: Publishing Reports With Configurable Compliance Rules

Picture a reporting platform used by finance and operations teams. Every export request carries a few independent requirements:

1. **Large reports** (quarterly filings, transaction logs) should be compressed before they leave the service.
2. **Reports containing customer or financial data** must be encrypted at rest and in transit.
3. **Regulated exports** must be logged for audit purposes - who exported what, and how much data left the system.

Crucially, these requirements are **orthogonal**: a small internal report might need none of them, while a large, sensitive, regulated report needs all three at once. The Decorator pattern lets `ReportPublishingService` build exactly the right chain for each request, at runtime, using simple boolean flags - without ever creating a subclass like `CompressedEncryptedAuditedReportExporter`.

## Component Walkthrough: What Each Part Is Doing

1. **`ReportExporter` (Component Interface)** defines the single operation every exporter must support: `exportReport(content)`. Both the base exporter and every decorator implement this same interface, which is what allows them to be swapped and stacked transparently.
2. **`PlainTextReportExporter` (Concrete Component)** is the simplest possible implementation: it returns the content unchanged. This is the object every decorator chain eventually wraps.
3. **`ReportExporterDecorator` (Base Decorator)** is an abstract class that stores a reference to the wrapped `ReportExporter` (the `delegate`). It exists purely to avoid repeating that plumbing in every concrete decorator.
4. **`CompressionDecorator`** calls the delegate first, then wraps the result with size metadata - demonstrating a decorator that transforms output *after* delegating.
5. **`EncryptionDecorator`** also transforms the output (via a reversible XOR cipher encoded as Base64) and additionally exposes a `decrypt` helper, showing that a decorator can carry extra capabilities beyond the shared interface.
6. **`AuditLoggingDecorator`** calls the delegate and returns its result **unchanged**, but records a side-effect (an audit entry) - demonstrating that decorators do not have to transform data to be useful.
7. **`ReportPublishingService` (Client)** builds the decorator chain dynamically from `compress`, `encrypt`, and `audit` flags, then calls `exportReport` once on the fully assembled chain.

## Request Flow: Stacking Decorators at Runtime

For a report that needs compression, encryption, and an audit trail, the chain is assembled like this:

1. Start with `PlainTextReportExporter` (the concrete component).
2. Wrap it in `CompressionDecorator` if the report is large.
3. Wrap *that* in `EncryptionDecorator` if the report is sensitive.
4. Wrap *that* in `AuditLoggingDecorator` if the export must be logged.
5. Call `exportReport(content)` once on the outermost decorator - each layer delegates inward, then applies its own behavior on the way back out.

Because every layer honors the same `ReportExporter` contract, the order of decorators can change the result (compress-then-encrypt differs from encrypt-then-compress), which is a deliberate part of the pattern: composition order is a design decision, not an accident.

## The Solution: Decorator Across JVM Languages

Below is the target interface, the base decorator, and one concrete decorator (`CompressionDecorator`) in Java 21, Kotlin, Scala 2, and Scala 3. The full source for all three decorators is linked at the end of this post.

<div class="code-tabs" data-tabs-id="tabs-decorator-impl">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">sealed</span> <span class="kd">interface</span> <span class="nc">ReportExporter</span> <span class="kd">permits</span> <span class="nc">PlainTextReportExporter</span><span class="o">,</span> <span class="nc">ReportExporterDecorator</span> <span class="o">{</span>
    <span class="nc">String</span> <span class="nf">exportReport</span><span class="o">(</span><span class="nc">String</span> <span class="n">content</span><span class="o">);</span>
<span class="o">}</span>

<span class="kd">abstract</span> <span class="kd">sealed</span> <span class="kd">class</span> <span class="nc">ReportExporterDecorator</span> <span class="kd">implements</span> <span class="nc">ReportExporter</span>
        <span class="kd">permits</span> <span class="nc">CompressionDecorator</span><span class="o">,</span> <span class="nc">EncryptionDecorator</span><span class="o">,</span> <span class="nc">AuditLoggingDecorator</span> <span class="o">{</span>
    <span class="kd">protected</span> <span class="kd">final</span> <span class="nc">ReportExporter</span> <span class="n">delegate</span><span class="o">;</span>

    <span class="kd">protected</span> <span class="nf">ReportExporterDecorator</span><span class="o">(</span><span class="nc">ReportExporter</span> <span class="n">delegate</span><span class="o">)</span> <span class="o">{</span>
        <span class="k">this</span><span class="o">.</span><span class="na">delegate</span> <span class="o">=</span> <span class="n">delegate</span><span class="o">;</span>
    <span class="o">}</span>
<span class="o">}</span>

<span class="kd">final</span> <span class="kd">class</span> <span class="nc">CompressionDecorator</span> <span class="kd">extends</span> <span class="nc">ReportExporterDecorator</span> <span class="o">{</span>
    <span class="kd">public</span> <span class="nf">CompressionDecorator</span><span class="o">(</span><span class="nc">ReportExporter</span> <span class="n">delegate</span><span class="o">)</span> <span class="o">{</span>
        <span class="kd">super</span><span class="o">(</span><span class="n">delegate</span><span class="o">);</span>
    <span class="o">}</span>

    <span class="nd">@Override</span>
    <span class="kd">public</span> <span class="nc">String</span> <span class="nf">exportReport</span><span class="o">(</span><span class="nc">String</span> <span class="n">content</span><span class="o">)</span> <span class="o">{</span>
        <span class="nc">String</span> <span class="n">exported</span> <span class="o">=</span> <span class="n">delegate</span><span class="o">.</span><span class="na">exportReport</span><span class="o">(</span><span class="n">content</span><span class="o">);</span>
        <span class="k">return</span> <span class="s">"COMPRESSED["</span> <span class="o">+</span> <span class="n">exported</span><span class="o">.</span><span class="na">length</span><span class="o">()</span> <span class="o">+</span> <span class="s">"]:"</span> <span class="o">+</span> <span class="n">exported</span><span class="o">;</span>
    <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/designpatterns/decorator/ReportExporter.java">View in repository</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">sealed</span> <span class="kd">interface</span> <span class="nc">ReportExporter</span> <span class="p">{</span>
    <span class="k">fun</span> <span class="nf">exportReport</span><span class="p">(</span><span class="n">content</span><span class="p">:</span> <span class="nc">String</span><span class="p">):</span> <span class="nc">String</span>
<span class="p">}</span>

<span class="kd">sealed</span> <span class="k">class</span> <span class="nc">ReportExporterDecorator</span><span class="p">(</span>
    <span class="k">protected</span> <span class="k">val</span> <span class="py">delegate</span><span class="p">:</span> <span class="nc">ReportExporter</span><span class="p">,</span>
<span class="p">)</span> <span class="p">:</span> <span class="nc">ReportExporter</span>

<span class="k">class</span> <span class="nc">CompressionDecorator</span><span class="p">(</span>
    <span class="n">delegate</span><span class="p">:</span> <span class="nc">ReportExporter</span><span class="p">,</span>
<span class="p">)</span> <span class="p">:</span> <span class="nc">ReportExporterDecorator</span><span class="p">(</span><span class="n">delegate</span><span class="p">)</span> <span class="p">{</span>
    <span class="k">override</span> <span class="k">fun</span> <span class="nf">exportReport</span><span class="p">(</span><span class="n">content</span><span class="p">:</span> <span class="nc">String</span><span class="p">):</span> <span class="nc">String</span> <span class="p">{</span>
        <span class="k">val</span> <span class="py">exported</span> <span class="p">=</span> <span class="n">delegate</span><span class="p">.</span><span class="nf">exportReport</span><span class="p">(</span><span class="n">content</span><span class="p">)</span>
        <span class="k">return</span> <span class="s">"COMPRESSED[</span><span class="si">${</span><span class="n">exported</span><span class="p">.</span><span class="n">length</span><span class="si">}</span><span class="s">]:</span><span class="si">$</span><span class="n">exported</span><span class="s">"</span>
    <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/designpatterns/decorator/ReportExporter.kt">View in repository</a></p>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">trait</span> <span class="nc">ReportExporter</span> <span class="o">{</span>
  <span class="k">def</span> <span class="n">exportReport</span><span class="o">(</span><span class="n">content</span><span class="o">:</span> <span class="kt">String</span><span class="o">):</span> <span class="kt">String</span>
<span class="o">}</span>

<span class="k">abstract</span> <span class="k">class</span> <span class="nc">ReportExporterDecorator</span><span class="o">(</span><span class="k">protected</span> <span class="k">val</span> <span class="n">delegate</span><span class="o">:</span> <span class="kt">ReportExporter</span><span class="o">)</span>
    <span class="k">extends</span> <span class="nc">ReportExporter</span>

<span class="k">class</span> <span class="nc">CompressionDecorator</span><span class="o">(</span><span class="n">delegate</span><span class="o">:</span> <span class="kt">ReportExporter</span><span class="o">)</span> <span class="k">extends</span> <span class="nc">ReportExporterDecorator</span><span class="o">(</span><span class="n">delegate</span><span class="o">)</span> <span class="o">{</span>
  <span class="k">override</span> <span class="k">def</span> <span class="n">exportReport</span><span class="o">(</span><span class="n">content</span><span class="o">:</span> <span class="kt">String</span><span class="o">):</span> <span class="kt">String</span> <span class="o">=</span> <span class="o">{</span>
    <span class="k">val</span> <span class="n">exported</span> <span class="o">=</span> <span class="n">delegate</span><span class="o">.</span><span class="n">exportReport</span><span class="o">(</span><span class="n">content</span><span class="o">)</span>
    <span class="s">s"COMPRESSED[</span><span class="si">${</span><span class="n">exported</span><span class="o">.</span><span class="n">length</span><span class="si">}</span><span class="s">]:</span><span class="si">$</span><span class="n">exported</span><span class="s">"</span>
  <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/main/scala/io/github/sps23/designpatterns/decorator/ReportExporter.scala">View in repository</a></p>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">trait</span> <span class="nc">ReportExporter</span><span class="o">:</span>
  <span class="k">def</span> <span class="n">exportReport</span><span class="o">(</span><span class="n">content</span><span class="o">:</span> <span class="kt">String</span><span class="o">):</span> <span class="kt">String</span>

<span class="k">abstract</span> <span class="k">class</span> <span class="nc">ReportExporterDecorator</span><span class="o">(</span><span class="k">protected</span> <span class="k">val</span> <span class="n">delegate</span><span class="o">:</span> <span class="kt">ReportExporter</span><span class="o">)</span>
    <span class="k">extends</span> <span class="nc">ReportExporter</span>

<span class="k">class</span> <span class="nc">CompressionDecorator</span><span class="o">(</span><span class="n">delegate</span><span class="o">:</span> <span class="kt">ReportExporter</span><span class="o">)</span> <span class="k">extends</span> <span class="nc">ReportExporterDecorator</span><span class="o">(</span><span class="n">delegate</span><span class="o">)</span><span class="o">:</span>
  <span class="k">override</span> <span class="k">def</span> <span class="n">exportReport</span><span class="o">(</span><span class="n">content</span><span class="o">:</span> <span class="kt">String</span><span class="o">):</span> <span class="kt">String</span> <span class="o">=</span>
    <span class="k">val</span> <span class="n">exported</span> <span class="o">=</span> <span class="n">delegate</span><span class="o">.</span><span class="n">exportReport</span><span class="o">(</span><span class="n">content</span><span class="o">)</span>
    <span class="s">s"COMPRESSED[</span><span class="si">${</span><span class="n">exported</span><span class="o">.</span><span class="n">length</span><span class="si">}</span><span class="s">]:</span><span class="si">$</span><span class="n">exported</span><span class="s">"</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/designpatterns/decorator/ReportExporter.scala">View in repository</a></p>
</div>
</div>

### Scala Developer Mental Model

- In **Java 21**, `sealed` interfaces and classes with `permits` make the decorator hierarchy exhaustive and explicit: the compiler knows exactly which classes can implement `ReportExporter` or extend `ReportExporterDecorator`.
- In **Scala 2/3**, traits and abstract classes give you the same shape with less ceremony. Because there is no built-in equivalent of `permits`, the hierarchy is closed by convention rather than by the compiler (unless you add `sealed trait`).
- In **Kotlin**, `sealed interface` plus a `sealed class` base decorator mirrors Java's exhaustiveness guarantees while keeping constructor boilerplate minimal thanks to primary constructor properties.

## Comparison: Java 21 vs Scala 2 vs Scala 3 vs Kotlin

<div class="table-wrapper" markdown="1">

| Language | Component contract | Base decorator | Composability |
|----------|--------------------|-----------------|----------------|
| Java 21 | `sealed interface` + `permits` | `abstract sealed class` | Explicit constructor chaining via `super(delegate)` |
| Scala 2 | `trait` | `abstract class` with a `protected val` | Constructor chaining via `extends ReportExporterDecorator(delegate)` |
| Scala 3 | `trait` (colon syntax) | `abstract class` with a `protected val` | Same as Scala 2, less boilerplate |
| Kotlin | `sealed interface` | `sealed class` with primary constructor property | Very concise; `sealed` is implicitly abstract |

</div>

## Testing the Decorator: Proving Stackability

The most important test for a decorator is not any single decorator in isolation - it is proving that decorators **compose**: the final result reflects every layer, in the order they were applied, and behaviors like audit logging can be verified independently of the data transformation.

<div class="code-tabs" data-tabs-id="tabs-decorator-test">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="kt">void</span> <span class="nf">shouldStackMultipleDecoratorsInAnyOrder</span><span class="o">()</span> <span class="o">{</span>
    <span class="nc">List</span><span class="o">&lt;</span><span class="nc">String</span><span class="o">&gt;</span> <span class="n">auditLog</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">ArrayList</span><span class="o">&lt;&gt;();</span>
    <span class="nc">ReportExporter</span> <span class="n">exporter</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">AuditLoggingDecorator</span><span class="o">(</span>
            <span class="k">new</span> <span class="nc">EncryptionDecorator</span><span class="o">(</span><span class="k">new</span> <span class="nc">CompressionDecorator</span><span class="o">(</span><span class="k">new</span> <span class="nc">PlainTextReportExporter</span><span class="o">()),</span> <span class="mi">7</span><span class="o">),</span>
            <span class="n">auditLog</span><span class="o">);</span>

    <span class="nc">String</span> <span class="n">exported</span> <span class="o">=</span> <span class="n">exporter</span><span class="o">.</span><span class="na">exportReport</span><span class="o">(</span><span class="nc">REPORT</span><span class="o">);</span>

    <span class="n">assertTrue</span><span class="o">(</span><span class="n">exported</span><span class="o">.</span><span class="na">startsWith</span><span class="o">(</span><span class="s">"ENCRYPTED:"</span><span class="o">));</span>
    <span class="n">assertEquals</span><span class="o">(</span><span class="mi">1</span><span class="o">,</span> <span class="n">auditLog</span><span class="o">.</span><span class="na">size</span><span class="o">());</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/designpatterns/decorator/ReportExporterTest.java">View full test file</a></p>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="k">fun</span> <span class="nf">shouldStackMultipleDecoratorsInAnyOrder</span><span class="p">()</span> <span class="p">{</span>
    <span class="k">val</span> <span class="py">auditLog</span> <span class="p">=</span> <span class="nf">mutableListOf</span><span class="p">&lt;</span><span class="nc">String</span><span class="p">&gt;()</span>
    <span class="k">val</span> <span class="py">exporter</span><span class="p">:</span> <span class="nc">ReportExporter</span> <span class="p">=</span>
        <span class="nc">AuditLoggingDecorator</span><span class="p">(</span>
            <span class="nc">EncryptionDecorator</span><span class="p">(</span><span class="nc">CompressionDecorator</span><span class="p">(</span><span class="nc">PlainTextReportExporter</span><span class="p">()),</span> <span class="m">7</span><span class="p">),</span>
            <span class="n">auditLog</span><span class="p">,</span>
        <span class="p">)</span>

    <span class="k">val</span> <span class="py">exported</span> <span class="p">=</span> <span class="n">exporter</span><span class="p">.</span><span class="nf">exportReport</span><span class="p">(</span><span class="n">report</span><span class="p">)</span>

    <span class="nf">assertTrue</span><span class="p">(</span><span class="n">exported</span><span class="p">.</span><span class="n">startsWith</span><span class="p">(</span><span class="s">"ENCRYPTED:"</span><span class="p">))</span>
    <span class="nf">assertEquals</span><span class="p">(</span><span class="m">1</span><span class="p">,</span> <span class="n">auditLog</span><span class="p">.</span><span class="n">size</span><span class="p">)</span>
<span class="p">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/designpatterns/decorator/ReportExporterTest.kt">View full test file</a></p>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="nd">@DisplayName</span><span class="o">(</span><span class="s">"Decorators should stack in any order without a subclass explosion"</span><span class="o">)</span>
<span class="k">def</span> <span class="n">shouldStackMultipleDecoratorsInAnyOrder</span><span class="o">()</span><span class="k">:</span> <span class="kt">Unit</span> <span class="o">=</span> <span class="o">{</span>
  <span class="k">val</span> <span class="nv">auditLog</span> <span class="k">=</span> <span class="nc">ArrayBuffer</span><span class="o">.</span><span class="py">empty</span><span class="o">[</span><span class="kt">String</span><span class="o">]</span>
  <span class="k">val</span> <span class="nv">exporter</span><span class="k">:</span> <span class="kt">ReportExporter</span> <span class="k">=</span> <span class="k">new</span> <span class="nc">AuditLoggingDecorator</span><span class="o">(</span>
    <span class="k">new</span> <span class="nc">EncryptionDecorator</span><span class="o">(</span><span class="k">new</span> <span class="nc">CompressionDecorator</span><span class="o">(</span><span class="k">new</span> <span class="nc">PlainTextReportExporter</span><span class="o">),</span> <span class="mi">7</span><span class="o">),</span>
    <span class="n">auditLog</span>
  <span class="o">)</span>

  <span class="k">val</span> <span class="nv">exported</span> <span class="k">=</span> <span class="n">exporter</span><span class="o">.</span><span class="py">exportReport</span><span class="o">(</span><span class="n">report</span><span class="o">)</span>

  <span class="n">assertTrue</span><span class="o">(</span><span class="n">exported</span><span class="o">.</span><span class="py">startsWith</span><span class="o">(</span><span class="s">"ENCRYPTED:"</span><span class="o">))</span>
  <span class="n">assertEquals</span><span class="o">(</span><span class="mi">1</span><span class="o">,</span> <span class="nv">auditLog</span><span class="o">.</span><span class="py">size</span><span class="o">)</span>
<span class="o">}</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/test/scala/io/github/sps23/designpatterns/decorator/ReportExporterTest.scala">View full test file</a></p>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="nd">@Test</span>
<span class="nd">@DisplayName</span><span class="o">(</span><span class="s">"Decorators should stack in any order without a subclass explosion"</span><span class="o">)</span>
<span class="k">def</span> <span class="n">shouldStackMultipleDecoratorsInAnyOrder</span><span class="o">()</span><span class="k">:</span> <span class="kt">Unit</span> <span class="o">=</span>
  <span class="k">val</span> <span class="n">auditLog</span> <span class="k">=</span> <span class="nc">ArrayBuffer</span><span class="o">.</span><span class="n">empty</span><span class="o">[</span><span class="kt">String</span><span class="o">]</span>
  <span class="k">val</span> <span class="n">exporter</span><span class="k">:</span> <span class="kt">ReportExporter</span> <span class="k">=</span> <span class="nc">AuditLoggingDecorator</span><span class="o">(</span>
    <span class="nc">EncryptionDecorator</span><span class="o">(</span><span class="nc">CompressionDecorator</span><span class="o">(</span><span class="nc">PlainTextReportExporter</span><span class="o">()),</span> <span class="mi">7</span><span class="o">),</span>
    <span class="n">auditLog</span>
  <span class="o">)</span>

  <span class="k">val</span> <span class="n">exported</span> <span class="k">=</span> <span class="n">exporter</span><span class="o">.</span><span class="n">exportReport</span><span class="o">(</span><span class="n">report</span><span class="o">)</span>

  <span class="n">assertTrue</span><span class="o">(</span><span class="n">exported</span><span class="o">.</span><span class="n">startsWith</span><span class="o">(</span><span class="s">"ENCRYPTED:"</span><span class="o">))</span>
  <span class="n">assertEquals</span><span class="o">(</span><span class="mi">1</span><span class="o">,</span> <span class="n">auditLog</span><span class="o">.</span><span class="n">size</span><span class="o">)</span>
</code></pre></div></div>
<p><a href="https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/designpatterns/decorator/ReportExporterTest.scala">View full test file</a></p>
</div>
</div>

Each language's test suite also verifies the simpler building blocks - a plain export leaves content untouched, compression adds size metadata, encryption is reversible with the correct key, and audit logging records an entry without altering the exported content - plus a realistic scenario where `ReportPublishingService` assembles the whole chain from `compress` / `encrypt` / `audit` flags, exactly like a production feature-flag-driven export pipeline would.

## When to Use the Decorator Pattern

Use a decorator when:

1. You need to add optional, independently combinable behaviors to an object.
2. The number of possible combinations would otherwise force a subclass per combination.
3. You want to add or remove a behavior without touching the component's core logic.
4. Behaviors should be composable and reorderable at runtime, not fixed at compile time.

Avoid it when:

1. There is only one fixed combination of behaviors - a single class is simpler.
2. The "behaviors" actually change the object's core identity or contract, not just add to it (that is closer to Strategy or plain inheritance).
3. Deep decorator chains become hard to debug; consider a pipeline or middleware abstraction instead once the chain grows very long.

## Interview Q&A: Decorator Pattern in Practice

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>How is the Decorator pattern different from inheritance?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Inheritance adds behavior by creating a new subclass. That is useful when the change is fixed and permanent, but it gets messy fast when you need many optional combinations. Decorator works differently: you keep the original object and wrap it with small extra layers. This lets you add one feature at a time, such as compression or audit logging, without creating a giant class tree like `CompressedEncryptedAuditedReportExporter`.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>What is a real-world example of decorators in Java?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      The best-known Java example is the I/O stack. `BufferedInputStream`, `GZIPInputStream`, and `DataInputStream` all wrap an existing stream and add behavior. The caller still works with a stream object, but the final object can read data differently, compress it, or add buffering. That is exactly the decorator idea: add features around a base object without rewriting the core type.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Can decorators be stacked? How do you manage that?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Yes, and that is one of the main reasons decorators are useful. You start with a plain exporter, then wrap it with a compression decorator, then an encryption decorator, and finally an audit decorator if needed. Each layer keeps the same outer contract, so the caller still uses one method. The order matters, because the chain is executed from the outside in. That makes the behavior predictable and easy to reason about when you build it intentionally.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>When would you choose decoration over inheritance?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Choose decoration when the extra features are optional and can be mixed in different ways. If a report is sometimes compressed, sometimes encrypted, and sometimes audited, a decorator chain is much cleaner than a huge hierarchy of subclasses. Use inheritance when the behavior is a core part of the type itself and not just a temporary add-on. If the class is really “a different kind of thing,” inheritance is fine. If it is “the same thing, with extra optional behavior,” decoration is usually the better fit.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>How does functional composition relate to decorators?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Both ideas solve the same basic problem: combine small pieces of behavior into a bigger behavior without rewriting everything from scratch. In a decorator chain, each wrapper calls the inner object and then adds its own step. In functional code, you often build a pipeline with functions that transform the value one step at a time. The difference is mainly style and tooling. Decorators are object-oriented and explicit, while functional composition tends to be more concise and data-focused. Both are ways of keeping logic modular and composable.
    </div>
  </details>
</div>

## Code Samples

All examples in this post are available in the repository:

**Implementation files:**
- **Java 21:** [ReportExporter.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/designpatterns/decorator/ReportExporter.java)
- **Kotlin:** [ReportExporter.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/designpatterns/decorator/ReportExporter.kt)
- **Scala 2:** [ReportExporter.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/main/scala/io/github/sps23/designpatterns/decorator/ReportExporter.scala)
- **Scala 3:** [ReportExporter.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/designpatterns/decorator/ReportExporter.scala)

**Test files:**
- **Java 21:** [ReportExporterTest.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/designpatterns/decorator/ReportExporterTest.java)
- **Kotlin:** [ReportExporterTest.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/designpatterns/decorator/ReportExporterTest.kt)
- **Scala 2:** [ReportExporterTest.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/test/scala/io/github/sps23/designpatterns/decorator/ReportExporterTest.scala)
- **Scala 3:** [ReportExporterTest.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/designpatterns/decorator/ReportExporterTest.scala)

---

*This is part of our Design Patterns in JVM Languages series. Check out the [full design patterns guide]({{ site.baseurl }}/interview/2026/07/26/design-patterns-guide-jvm) for more patterns and interview preparation.*
