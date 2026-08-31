---
layout: post
title: "Command Pattern: Making Requests into Objects"
description: "Learn the Command pattern in Java 21, Scala 2, Scala 3, and Kotlin with undo/redo history, queued actions, and real-world editor workflows."
date: 2026-08-29 08:00:00 +0000
updated: 2026-08-28 15:00:00 +0000
categories: [interview, best-practices]
tags: [java, java21, scala, scala2, scala3, kotlin, design-patterns, behavioral-patterns, command-pattern]
---

Imagine you've just typed a dozen lines in a text editor and then hit undo three times. The message that disappears is not magic. It is a clean example of the Command pattern at work: each action is wrapped as an object that knows how to execute itself and how to reverse itself.

## The Problem: A method call is not enough

Most code starts with a direct call:

- `editor.insert("Hello")`
- `paymentService.chargeCard(...)`
- `queue.publish("job-42")`

That works until the caller needs more than one behavior. We want to:

1. store the action for later
2. run it at a specific time
3. undo it when needed
4. log it for auditing or retries
5. support macro recording

If those requirements appear, the logic starts leaking across the application. You end up with flags, special cases, and a pile of hidden coupling. The Command pattern turns the action itself into an object so the system can treat it like data.

## Key Concepts

<div class="table-wrapper" markdown="1">

| Concept | In this example | Why it matters |
|---------|-----------------|----------------|
| Command | `InsertTextCommand` | Encapsulates the action and its undo logic |
| Receiver | `DocumentEditor` | Owns the real state that changes |
| Invoker | `CommandHistory` | Executes, queues, and tracks commands |
| History | undo/redo stacks | Makes reversible workflows possible |
| Transactional behavior | command execution + lifecycle | Keeps the call site simple and consistent |

</div>

## Real Use Case: Undoable Editor Actions

A text editor is one of the clearest places to see the pattern. Every user action is a command:

- insert text
- delete text
- format selection
- paste block
- replace word

Each command can be recorded in history, undone, redone, or replayed. Without commands, `undo` becomes a big list of `if` statements and manual state resets.

The pattern is also useful in:

- payment workflows with retry and rollback
- job queues with scheduled execution
- macro recorders
- background processing pipelines

## The Solution: Commands Across JVM Languages

This example wraps insert operations in a command object and keeps an undo/redo stack in a `CommandHistory` class.

<div class="code-tabs" data-tabs-id="command-impl">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala2" data-lang="Scala 2">Scala 2</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">final</span> <span class="kd">class</span> <span class="nc">DocumentEditor</span> <span class="o">{</span>
    <span class="kd">private</span> <span class="kd">final</span> <span class="nc">StringBuilder</span> <span class="n">content</span> <span class="o">=</span> <span class="k">new</span> <span class="nc">StringBuilder</span><span class="o">();</span>

    <span class="kd">public</span> <span class="kt">void</span> <span class="nf">insert</span><span class="o">(</span><span class="kt">int</span> <span class="n">index</span><span class="o">,</span> <span class="nc">String</span> <span class="n">value</span><span class="o">)</span> <span class="o">{</span>
        <span class="n">content</span><span class="o">.</span><span class="na">insert</span><span class="o">(</span><span class="n">index</span><span class="o">,</span> <span class="n">value</span><span class="o">);</span>
    <span class="o">}</span>

    <span class="kd">public</span> <span class="kt">void</span> <span class="nf">delete</span><span class="o">(</span><span class="kt">int</span> <span class="n">start</span><span class="o">,</span> <span class="kt">int</span> <span class="n">end</span><span class="o">)</span> <span class="o">{</span>
        <span class="n">content</span><span class="o">.</span><span class="na">delete</span><span class="o">(</span><span class="n">start</span><span class="o">,</span> <span class="n">end</span><span class="o">);</span>
    <span class="o">}</span>

    <span class="kd">public</span> <span class="nc">String</span> <span class="nf">text</span><span class="o">()</span> <span class="o">{</span>
        <span class="k">return</span> <span class="n">content</span><span class="o">.</span><span class="na">toString</span><span class="o">();</span>
    <span class="o">}</span>
<span class="o">}</span>

<span class="kd">interface</span> <span class="nc">Command</span> <span class="o">{</span>
    <span class="kt">void</span> <span class="nf">execute</span><span class="o">();</span>
    <span class="kt">void</span> <span class="nf">undo</span><span class="o">();</span>
<span class="o">}</span>

<span class="kd">final</span> <span class="kd">class</span> <span class="nc">InsertTextCommand</span> <span class="kd">implements</span> <span class="nc">Command</span> <span class="o">{</span>
    <span class="kd">private</span> <span class="kd">final</span> <span class="nc">DocumentEditor</span> <span class="n">editor</span><span class="o">;</span>
    <span class="kd">private</span> <span class="kd">final</span> <span class="kt">int</span> <span class="n">index</span><span class="o">;</span>
    <span class="kd">private</span> <span class="kd">final</span> <span class="nc">String</span> <span class="n">value</span><span class="o">;</span>

    <span class="nc">InsertTextCommand</span><span class="o">(</span><span class="nc">DocumentEditor</span> <span class="n">editor</span><span class="o">,</span> <span class="kt">int</span> <span class="n">index</span><span class="o">,</span> <span class="nc">String</span> <span class="n">value</span><span class="o">)</span> <span class="o">{</span>
        <span class="k">this</span><span class="o">.</span><span class="na">editor</span> <span class="o">=</span> <span class="n">editor</span><span class="o">;</span>
        <span class="k">this</span><span class="o">.</span><span class="na">index</span> <span class="o">=</span> <span class="n">index</span><span class="o">;</span>
        <span class="k">this</span><span class="o">.</span><span class="na">value</span> <span class="o">=</span> <span class="n">value</span><span class="o">;</span>
    <span class="o">}</span>

    <span class="kd">public</span> <span class="kt">void</span> <span class="nf">execute</span><span class="o">()</span> <span class="o">{</span>
        <span class="n">editor</span><span class="o">.</span><span class="na">insert</span><span class="o">(</span><span class="n">index</span><span class="o">,</span> <span class="n">value</span><span class="o">);</span>
    <span class="o">}</span>

    <span class="kd">public</span> <span class="kt">void</span> <span class="nf">undo</span><span class="o">()</span> <span class="o">{</span>
        <span class="n">editor</span><span class="o">.</span><span class="na">delete</span><span class="o">(</span><span class="n">index</span><span class="o">,</span> <span class="n">index</span> <span class="o">+</span> <span class="n">value</span><span class="o">.</span><span class="na">length</span><span class="o">());</span>
    <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">class</span> <span class="nc">DocumentEditor</span> <span class="p">{</span>
    <span class="k">private</span> <span class="k">val</span> <span class="py">content</span> <span class="p">=</span> <span class="nc">StringBuilder</span><span class="p">()</span>

    <span class="k">fun</span> <span class="nf">insert</span><span class="p">(</span><span class="n">index</span><span class="p">:</span> <span class="nc">Int</span><span class="p">,</span> <span class="n">value</span><span class="p">:</span> <span class="nc">String</span><span class="p">)</span> <span class="p">{</span>
        <span class="n">content</span><span class="p">.</span><span class="nf">insert</span><span class="p">(</span><span class="n">index</span><span class="p">,</span> <span class="n">value</span><span class="p">)</span>
    <span class="p">}</span>

    <span class="k">fun</span> <span class="nf">delete</span><span class="p">(</span><span class="n">start</span><span class="p">:</span> <span class="nc">Int</span><span class="p">,</span> <span class="n">end</span><span class="p">:</span> <span class="nc">Int</span><span class="p">)</span> <span class="p">{</span>
        <span class="n">content</span><span class="p">.</span><span class="nf">delete</span><span class="p">(</span><span class="n">start</span><span class="p">,</span> <span class="n">end</span><span class="p">)</span>
    <span class="p">}</span>

    <span class="k">fun</span> <span class="nf">text</span><span class="p">():</span> <span class="nc">String</span> <span class="p">=</span> <span class="n">content</span><span class="p">.</span><span class="nf">toString</span><span class="p">()</span>
<span class="p">}</span>

<span class="k">interface</span> <span class="nc">Command</span> <span class="p">{</span>
    <span class="k">fun</span> <span class="nf">execute</span><span class="p">()</span>
    <span class="k">fun</span> <span class="nf">undo</span><span class="p">()</span>
<span class="p">}</span>

<span class="k">class</span> <span class="nc">InsertTextCommand</span><span class="p">(</span>
    <span class="k">private</span> <span class="k">val</span> <span class="py">editor</span><span class="p">:</span> <span class="nc">DocumentEditor</span><span class="p">,</span>
    <span class="k">private</span> <span class="k">val</span> <span class="py">index</span><span class="p">:</span> <span class="nc">Int</span><span class="p">,</span>
    <span class="k">private</span> <span class="k">val</span> <span class="py">value</span><span class="p">:</span> <span class="nc">String</span>,
<span class="p">)</span> <span class="p">:</span> <span class="nc">Command</span> <span class="p">{</span>
    <span class="k">override</span> <span class="k">fun</span> <span class="nf">execute</span><span class="p">()</span> <span class="p">{</span>
        <span class="n">editor</span><span class="p">.</span><span class="nf">insert</span><span class="p">(</span><span class="n">index</span><span class="p">,</span> <span class="n">value</span><span class="p">)</span>
    <span class="p">}</span>

    <span class="k">override</span> <span class="k">fun</span> <span class="nf">undo</span><span class="p">()</span> <span class="p">{</span>
        <span class="n">editor</span><span class="p">.</span><span class="nf">delete</span><span class="p">(</span><span class="n">index</span><span class="p">,</span> <span class="n">index</span> <span class="p">+</span> <span class="n">value</span><span class="p">.</span><span class="n">length</span><span class="p">)</span>
    <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala2">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">final</span> <span class="k">class</span> <span class="nc">DocumentEditor</span> <span class="o">{</span>
  <span class="k">private</span> <span class="k">val</span> <span class="nv">content</span> <span class="k">=</span> <span class="k">new</span> <span class="nc">StringBuilder</span>

  <span class="k">def</span> <span class="nf">insert</span><span class="o">(</span><span class="n">index</span><span class="k">:</span> <span class="kt">Int</span><span class="o">,</span> <span class="n">value</span><span class="k">:</span> <span class="kt">String</span><span class="o">)</span><span class="k">:</span> <span class="kt">Unit</span> <span class="o">=</span> <span class="n">content</span><span class="o">.</span><span class="py">insert</span><span class="o">(</span><span class="n">index</span><span class="o">,</span> <span class="n">value</span><span class="o">)</span>
  <span class="k">def</span> <span class="nf">delete</span><span class="o">(</span><span class="n">start</span><span class="k">:</span> <span class="kt">Int</span><span class="o">,</span> <span class="n">end</span><span class="k">:</span> <span class="kt">Int</span><span class="o">)</span><span class="k">:</span> <span class="kt">Unit</span> <span class="o">=</span> <span class="n">content</span><span class="o">.</span><span class="py">delete</span><span class="o">(</span><span class="n">start</span><span class="o">,</span> <span class="n">end</span><span class="o">)</span>
  <span class="k">def</span> <span class="nf">text</span><span class="k">:</span> <span class="kt">String</span> <span class="o">=</span> <span class="n">content</span><span class="o">.</span><span class="py">toString</span>
<span class="o">}</span>

<span class="k">trait</span> <span class="nc">Command</span> <span class="o">{</span>
  <span class="k">def</span> <span class="nf">execute</span><span class="o">()</span><span class="k">:</span> <span class="kt">Unit</span>
  <span class="k">def</span> <span class="nf">undo</span><span class="o">()</span><span class="k">:</span> <span class="kt">Unit</span>
<span class="o">}</span>

<span class="k">final</span> <span class="k">case</span> <span class="k">class</span> <span class="nc">InsertTextCommand</span><span class="o">(</span><span class="n">editor</span><span class="k">:</span> <span class="kt">DocumentEditor</span><span class="o">,</span> <span class="n">index</span><span class="k">:</span> <span class="kt">Int</span><span class="o">,</span> <span class="n">value</span><span class="k">:</span> <span class="kt">String</span><span class="o">)</span> <span class="k">extends</span> <span class="nc">Command</span> <span class="o">{</span>
  <span class="k">override</span> <span class="k">def</span> <span class="nf">execute</span><span class="o">()</span><span class="k">:</span> <span class="kt">Unit</span> <span class="o">=</span> <span class="n">editor</span><span class="o">.</span><span class="py">insert</span><span class="o">(</span><span class="n">index</span><span class="o">,</span> <span class="n">value</span><span class="o">)</span>
  <span class="k">override</span> <span class="k">def</span> <span class="nf">undo</span><span class="o">()</span><span class="k">:</span> <span class="kt">Unit</span> <span class="o">=</span> <span class="n">editor</span><span class="o">.</span><span class="py">delete</span><span class="o">(</span><span class="n">index</span><span class="o">,</span> <span class="n">index</span> <span class="o">+</span> <span class="n">value</span><span class="o">.</span><span class="py">length</span><span class="o">)</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">final</span> <span class="k">class</span> <span class="nc">DocumentEditor</span><span class="k">:</span>
  <span class="k">private</span> <span class="k">val</span> <span class="nv">content</span> <span class="k">=</span> <span class="nc">StringBuilder</span><span class="o">()</span>

  <span class="k">def</span> <span class="nf">insert</span><span class="o">(</span><span class="n">index</span><span class="k">:</span> <span class="kt">Int</span><span class="o">,</span> <span class="n">value</span><span class="k">:</span> <span class="kt">String</span><span class="o">)</span><span class="k">:</span> <span class="kt">Unit</span> <span class="o">=</span> <span class="n">content</span><span class="o">.</span><span class="py">insert</span><span class="o">(</span><span class="n">index</span><span class="o">,</span> <span class="n">value</span><span class="o">)</span>
  <span class="k">def</span> <span class="nf">delete</span><span class="o">(</span><span class="n">start</span><span class="k">:</span> <span class="kt">Int</span><span class="o">,</span> <span class="n">end</span><span class="k">:</span> <span class="kt">Int</span><span class="o">)</span><span class="k">:</span> <span class="kt">Unit</span> <span class="o">=</span> <span class="n">content</span><span class="o">.</span><span class="py">delete</span><span class="o">(</span><span class="n">start</span><span class="o">,</span> <span class="n">end</span><span class="o">)</span>
  <span class="k">def</span> <span class="nf">text</span><span class="k">:</span> <span class="kt">String</span> <span class="o">=</span> <span class="n">content</span><span class="o">.</span><span class="py">toString</span>

<span class="k">trait</span> <span class="nc">Command</span><span class="k">:</span>
  <span class="k">def</span> <span class="nf">execute</span><span class="o">()</span><span class="k">:</span> <span class="kt">Unit</span>
  <span class="k">def</span> <span class="nf">undo</span><span class="o">()</span><span class="k">:</span> <span class="kt">Unit</span>

<span class="k">final</span> <span class="k">case</span> <span class="k">class</span> <span class="nc">InsertTextCommand</span><span class="o">(</span><span class="n">editor</span><span class="k">:</span> <span class="kt">DocumentEditor</span><span class="o">,</span> <span class="n">index</span><span class="k">:</span> <span class="kt">Int</span><span class="o">,</span> <span class="n">value</span><span class="k">:</span> <span class="kt">String</span><span class="o">)</span> <span class="k">extends</span> <span class="nc">Command</span><span class="k">:</span>
  <span class="k">override</span> <span class="k">def</span> <span class="nf">execute</span><span class="o">()</span><span class="k">:</span> <span class="kt">Unit</span> <span class="o">=</span> <span class="n">editor</span><span class="o">.</span><span class="py">insert</span><span class="o">(</span><span class="n">index</span><span class="o">,</span> <span class="n">value</span><span class="o">)</span>
  <span class="k">override</span> <span class="k">def</span> <span class="nf">undo</span><span class="o">()</span><span class="k">:</span> <span class="kt">Unit</span> <span class="o">=</span> <span class="n">editor</span><span class="o">.</span><span class="py">delete</span><span class="o">(</span><span class="n">index</span><span class="o">,</span> <span class="n">index</span> <span class="o">+</span> <span class="n">value</span><span class="o">.</span><span class="py">length</span><span class="o">)</span>
</code></pre></div></div>
</div>
</div>

## Why this pattern is powerful

The important idea is not that the code is written as a class. The important idea is that the caller no longer knows the details of the work. It just says, "execute this command".

That gives you some nice benefits:

- you can log commands before execution
- you can queue them for batch processing
- you can reverse them in a clean undo stack
- you can test each command in isolation
- you can record a macro of commands and replay it later

The same structure shows up in real systems: a payment service may queue a `ChargeCardCommand`, a workflow engine may execute a `DeployReleaseCommand`, and a UI may store a `CopyTextCommand` for easy undo.

## Comparison Table

<div class="table-wrapper" markdown="1">

| Concern | Java 21 | Scala 2/3 | Kotlin |
|---------|----------|-----------|--------|
| Command contract | interface + concrete implementations | trait + case class | interface + class |
| Undo logic | explicit `undo()` method | same | same |
| History tracking | `Deque<Command>` | `ArrayDeque` / mutable stack | `ArrayDeque` |
| Typical use | UI editors, task queues | domain workflows, replayable actions | coroutine jobs, actions, app commands |
| Mental model | object-oriented command object | case class + function-like action | data class + interface |

</div>

## Best Practices

- Keep commands small and explicit. A command should represent one business action.
- Put the state change in the receiver, not in the command invoker.
- Store enough data to undo exactly what was done.
- Think carefully about idempotency and retry semantics when commands are queued.
- When you have command composition, prefer a clear history model over vague flags and booleans.

## Interview Q&A: Command Pattern in Practice

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>What problem does the Command pattern solve?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      It turns a request into an object so you can store it, delay it, inspect it, or undo it. That matters when the action is more than a direct method call, especially for editors, queues, and transactional flows. Instead of scattering logic across the UI and service layers, the command becomes the single unit of work.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>How is Command different from Strategy?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Strategy chooses an algorithm. Command wraps an action. A strategy may tell you how to calculate a fee; a command tells you to "insert this text" or "charge this card" and records how to undo it. The key difference is intent: Strategy is about selecting behavior, while Command is about executing a request as a first-class object.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Why do undo/redo systems love commands?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Because each command knows both the forward action and the reverse action. When the user presses undo, the system simply asks the last command to reverse itself. That keeps history logic simple and avoids writing custom rollback logic everywhere the app mutates state.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>When would you choose a command queue over direct calls?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      Use a queue when the action must be delayed, retried, logged, or replayed. A payment system might queue a charge request, a worker service might process jobs from a queue, and a UI might record a series of edits for macro playback. Direct calls are simpler for one-off behavior; commands are better when the action has a lifecycle.
    </div>
  </details>
</div>

## Conclusion

The Command pattern is a great fit when the code needs a clean lifecycle around an action: execute it, record it, undo it, replay it, or queue it. In Java, Scala, and Kotlin, the pattern takes different shapes, but the idea stays the same — treat each action as an object instead of as a loose pile of instructions.

That is exactly why it shows up in editors, workflows, and distributed processing systems. Once you see commands as first-class actions, the rest of the system becomes easier to test, easier to reason about, and much easier to undo when reality bites back.

## Code Samples

All examples in this post are runnable and mirrored across the JVM languages in the repository:

- [Java 21 implementation](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/designpatterns/command)
- [Kotlin implementation](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/designpatterns/command)
- [Scala 2 implementation](https://github.com/sps23/java-for-scala-devs/tree/main/scala2/src/main/scala/io/github/sps23/designpatterns/command)
- [Scala 3 implementation](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/designpatterns/command)

- [Java 21 tests](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/test/java/io/github/sps23/designpatterns/command/DocumentEditorTest.java)
- [Kotlin tests](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/test/kotlin/io/github/sps23/designpatterns/command/DocumentEditorTest.kt)
- [Scala 2 tests](https://github.com/sps23/java-for-scala-devs/blob/main/scala2/src/test/scala/io/github/sps23/designpatterns/command/DocumentEditorTest.scala)
- [Scala 3 tests](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/test/scala/io/github/sps23/designpatterns/command/DocumentEditorTest.scala)

---

*This is part of our Design Patterns series for JVM developers. Start with [Design Patterns in JVM Languages - Your Guide to the Top 10]({{ site.baseurl }}{% link _posts/2026-07-26-design-patterns-guide-jvm.md %}). Next related posts: [Strategy Pattern: Choosing Algorithms at Runtime]({{ site.baseurl }}/blog/2026/08/29/design-patterns-strategy/) and [Observer Pattern: Reacting to Changes]({{ site.baseurl }}/blog/2026/08/29/design-patterns-observer/).* 
