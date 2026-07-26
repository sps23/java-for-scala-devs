---
layout: post
title: "Design Patterns in JVM Languages - Your Guide to the Top 10"
description: "Comprehensive guide to the 10 most commonly used design patterns in Java, Scala, and Kotlin - learn what design patterns are, why you need them, and explore Creational, Structural, and Behavioral pattern categories with real-world examples and interview prep."
date: 2026-07-26 13:00:00 +0000
categories: [interview]
tags: [java, scala, kotlin, design-patterns, interview, preparation, creational, structural, behavioral]
---

So, you've been writing code for a few years now, and you keep hearing other developers throw around phrases like "Oh, that's a classic Singleton pattern" or "We should use a Factory here" or "I'll implement the Observer pattern for this." And you're sitting there thinking, "Uh... okay? Is that just... code?" 

Welcome to the wonderful world of design patterns! If you're a Scala developer transitioning to Java, or you're preparing for a senior developer interview, mastering design patterns is like learning the secret handshake of the engineering world. They're not magic formulas, they're not required reading from God—they're simply proven, reusable solutions to common problems that developers have encountered again and again across decades of programming.

## What Are Design Patterns, Really?

Let me be honest: design patterns are just templates. They're like saying, "Hey, we've seen this problem a thousand times, and here's a nice way to solve it that works well." When you use them effectively, your code becomes more maintainable, more testable, and more readable. Other developers can look at your code and immediately recognize the pattern you're using. It's like using a well-known recipe instead of inventing a new one for every meal.

## Why Should You Care About Design Patterns?

Three words: **recognition, communication, and power**.

**Recognition**: Once you know the patterns, you'll see them everywhere. In frameworks, in libraries, in codebases you inherit. Suddenly, complex code makes sense because you recognize the underlying structure.

**Communication**: When you say "We need a Builder here" or "This should be a Strategy pattern," your team immediately understands the architecture without you needing to explain every detail.

**Power**: Design patterns give you battle-tested solutions. You won't waste time reinventing the wheel when someone else already figured out the best way to handle this exact problem.

They also make code easier to test, modify, and extend. When requirements change (spoiler alert: they always do), having your code organized around proven patterns means you can adapt without burning everything down.

## The Three Categories of Design Patterns

Design patterns fall into three main buckets, each addressing different kinds of problems:

### Creational Patterns
These patterns are about creating objects without specifying their exact classes. They give you flexibility in *what* gets created and *how* it gets created. When you need to decouple object creation from object use, creational patterns are your friend.

### Structural Patterns
These patterns are about how objects are composed to form larger structures. They focus on relationships between entities, helping you understand how objects and classes can be combined into larger, more complex systems. Think of them as the architectural blueprints.

### Behavioral Patterns
These patterns are about communication between objects—how they work together and divide responsibilities. They focus on defining how objects interact and how responsibilities are distributed.

---

## Creational Patterns - Creating Objects Like a Pro

> **Note:** The following links point to upcoming deep-dive posts in this series. Check back as we publish detailed analysis of each pattern!

### 1. Singleton Pattern

**What It Is:** A class that can only have one instance, and you access it globally. Think of it as the "one ring to rule them all" of design patterns. There's only one database connection manager, one logger configuration, one...well, you get the idea.

**Read the full post:** [Singleton Pattern: When There Can Be Only One]({{ site.baseurl }}/interview/2026/07/27/design-patterns-singleton/)

**What You'll Learn:** How to implement thread-safe singletons in Java (eager vs. lazy initialization), why static initialization is powerful, how to handle serialization and reflection attacks, and when NOT to use singletons (spoiler: thread-local singletons aren't real singletons). You'll see how Scala and Kotlin handle the same pattern more elegantly with language-level features.

**Interview Questions You Might Face:**
- "Implement a thread-safe singleton. What are the pitfalls?"
- "Can you break a singleton? How would you protect against reflection attacks?"
- "When is the singleton pattern actually a good idea versus just global mutable state?"
- "How do Scala and Kotlin make singletons easier?"
- "What's the difference between an eager-initialized and a lazy-initialized singleton?"

---

### 2. Factory Pattern

**What It Is:** Instead of directly creating objects with `new`, you use a factory method or factory class to create them. It's like a factory assembly line—you ask for what you want, and the factory figures out exactly what to give you.

**Read the full post:** [Factory Pattern: Let Someone Else Do the Creating]({{ site.baseurl }}/interview/2026/07/28/design-patterns-factory/)

**What You'll Learn:** The difference between Simple Factory, Factory Method, and Abstract Factory patterns. You'll learn when to use each one, how to reduce coupling in your codebase, and how to make testing easier by injecting factories instead of constructors. You'll see practical examples of how frameworks like Spring use factory patterns extensively.

**Interview Questions You Might Face:**
- "What's the difference between Factory Method and Abstract Factory?"
- "When would you use a factory instead of a constructor?"
- "How do factories help with testing?"
- "Can you show me an example of Abstract Factory in a real codebase?"
- "How does dependency injection relate to the factory pattern?"

---

### 3. Builder Pattern

**What It Is:** Instead of passing a dozen constructor parameters or overloading constructors endlessly, a Builder gives you a fluent, readable way to construct complex objects step by step.

**Read the full post:** [Builder Pattern: Constructing Complexity with Elegance]({{ site.baseurl }}/interview/2026/07/29/design-patterns-builder/)

**What You'll Learn:** How to write effective builders in Java (before and after records), why they beat telescoping constructors, and how to make your builders thread-safe and immutable. You'll see how Scala case classes with default parameters achieve similar goals more concisely, and how Kotlin's named parameters make builders less necessary.

**Interview Questions You Might Face:**
- "When do you use a builder instead of a constructor?"
- "How would you implement a fluent builder? Show the code."
- "Can builders be thread-safe? How?"
- "How do Java records change the builder pattern landscape?"
- "What are the trade-offs between builders and optional/default parameters?"

---

### 4. Prototype Pattern

**What It Is:** Create new objects by copying an existing object (the prototype) rather than creating from scratch. It's less common in modern Java, but powerful when you need deep copies or want to avoid expensive object creation.

**Read the full post:** [Prototype Pattern: Cloning for Success]({{ site.baseurl }}/interview/2026/07/30/design-patterns-prototype/)

**What You'll Learn:** How to implement cloning correctly, shallow vs. deep copying, the dangers of the `Cloneable` interface, and when prototyping actually saves you performance. You'll see how immutability in Scala and functional programming approaches reduce the need for this pattern.

**Interview Questions You Might Face:**
- "Explain the difference between shallow and deep copying."
- "How do you implement the Prototype pattern in Java?"
- "What are the problems with Java's Cloneable interface?"
- "When would you actually use prototyping instead of just creating new objects?"
- "How does this pattern relate to immutability?"

---

## Structural Patterns - Building Better Architectures

### 5. Adapter Pattern

**What It Is:** Convert the interface of one class into another interface that clients expect. It's like a power adapter—you've got a European plug but an American outlet, so you use an adapter. Adapters make incompatible things work together.

**Read the full post:** [Adapter Pattern: Making Incompatible Things Play Nice]({{ site.baseurl }}/interview/2026/07/31/design-patterns-adapter/)

**What You'll Learn:** When to use class adapters vs. object adapters, how to write adapters that don't add unnecessary complexity, and how adapters help when you're integrating legacy code or third-party libraries. You'll see real examples of how adapters simplify API migration.

**Interview Questions You Might Face:**
- "What's the difference between a class adapter and an object adapter?"
- "When would you use an adapter instead of just modifying the class directly?"
- "How do adapters help with legacy code integration?"
- "Can you show me an adapter in a real framework or library?"
- "What's the relationship between adapters and the Decorator pattern?"

---

### 6. Decorator Pattern

**What It Is:** Add behavior to objects dynamically, without modifying their structure. Instead of subclassing endlessly to add features, you wrap objects with decorators. It's like adding accessories to your outfit—you can keep the same core outfit and just add or remove decorations.

**Read the full post:** [Decorator Pattern: Wrapping Objects with Style]({{ site.baseurl }}/interview/2026/08/01/design-patterns-decorator/)

**What You'll Learn:** How to implement decorators without bloating your class hierarchy, how Java's I/O streams (BufferedInputStream, etc.) are a textbook example of decorators, and when decorators are better than inheritance. You'll see how functional composition in Scala and Kotlin provides alternatives to decorators.

**Interview Questions You Might Face:**
- "How is the Decorator pattern different from inheritance?"
- "What's a real-world example of decorators in Java?"
- "Can decorators be stacked? How do you manage that?"
- "When would you choose decoration over inheritance?"
- "How does functional composition relate to decorators?"

---

### 7. Facade Pattern

**What It Is:** Provide a unified, simplified interface to a complex subsystem. A facade hides the complexity, letting clients work with the subsystem through a simple entry point. Think of it as the customer service representative for your codebase—they handle all the complexity behind the scenes.

**Read the full post:** [Facade Pattern: Simplifying Complex Systems]({{ site.baseurl }}/interview/2026/08/02/design-patterns-facade/)

**What You'll Learn:** How to identify when a subsystem needs a facade, how to design facades that don't just add a thin wrapper, and how facades improve testability. You'll see examples from popular libraries (Spring's various template classes, for example) that use the facade pattern extensively.

**Interview Questions You Might Face:**
- "What's the purpose of the Facade pattern?"
- "How is a Facade different from an Adapter?"
- "Can you give me an example of a facade in a real library?"
- "When does a facade start to become bloated?"
- "How do facades relate to the Principle of Least Surprise?"

---

## Behavioral Patterns - Objects Playing Well Together

### 8. Strategy Pattern

**What It Is:** Define a family of algorithms, encapsulate each one, and make them interchangeable. It's like having different strategies to solve a problem, and you pick the right one for the situation.

**Read the full post:** [Strategy Pattern: Choosing Algorithms at Runtime]({{ site.baseurl }}/interview/2026/08/03/design-patterns-strategy/)

**What You'll Learn:** How to avoid massive if-else chains by using strategies, how to compose strategies, and how dependency injection often replaces explicit strategy switching. You'll see how functional programming (lambdas in Java, first-class functions in Scala) simplifies strategy implementation.

**Interview Questions You Might Face:**
- "How would you implement different payment processing strategies?"
- "What's the relationship between strategies and lambdas?"
- "When do you use strategy instead of just if-else statements?"
- "Can you compose strategies together?"
- "How does dependency injection relate to the Strategy pattern?"

---

### 9. Observer Pattern

**What It Is:** Define a one-to-many dependency where when one object changes state, all its dependents are notified automatically. It's the foundation for event-driven programming and reactive systems. When something important happens, tell everyone who cares.

**Read the full post:** [Observer Pattern: Reacting to Changes]({{ site.baseurl }}/interview/2026/08/04/design-patterns-observer/)

**What You'll Learn:** How to implement observers without creating memory leaks, the dangers of strong vs. weak references, and how modern reactive frameworks (RxJava, ZIO) are sophisticated implementations of the observer pattern. You'll see how event listeners in GUI frameworks use this pattern, and how it powers real-time applications.

**Interview Questions You Might Face:**
- "Explain the Observer pattern and show me a simple implementation."
- "What are common pitfalls with observers? How do you handle them?"
- "How do modern reactive frameworks (Rx, ZIO, etc.) relate to observers?"
- "What's the difference between observers and callbacks?"
- "How do you handle circular dependencies with observers?"

---

### 10. Command Pattern

**What It Is:** Encapsulate a request as an object, allowing you to parameterize clients with different requests, queue requests, and support undoable operations. It's like putting instructions in a bottle and passing it around—the recipient doesn't need to know who sent it, just how to execute it.

**Read the full post:** [Command Pattern: Making Requests into Objects]({{ site.baseurl }}/interview/2026/08/05/design-patterns-command/)

**What You'll Learn:** How to implement undo/redo systems, how to queue and execute commands, and how command patterns enable macro recording and complex transaction handling. You'll see how this pattern is essential for implementing features like undo/redo, and how it differs from simple callbacks.

**Interview Questions You Might Face:**
- "How would you implement an undo system? Would you use commands?"
- "What's the difference between Command and Strategy?"
- "How do you handle command history and undo/redo?"
- "Can you give me a real-world example of when Command is useful?"
- "How do commands relate to functional approaches with first-class functions?"

---

## Why These Ten?

These ten patterns are the bread and butter of professional software development. They appear in popular frameworks, they solve real problems you'll encounter, and they come up in technical interviews. Once you understand these, learning other patterns becomes much easier—they're mostly combinations and variations of these core ideas.

## Learning Doesn't End Here

This guide is your roadmap. Each pattern has complexities, trade-offs, and subtle variations that deserve their own deep dives. Some of these patterns work better in Java, others shine in Scala's functional world, and Kotlin brings its own idioms to the table.

The key takeaway: **design patterns are not laws**. They're tools. Use them when they solve a real problem, and don't force them just because you know about them. The worst code I've seen comes from developers who tried to apply every pattern they learned in a single codebase.

Start with understanding these ten patterns conceptually. Then, as you work on real projects, you'll recognize where each pattern naturally fits. That's when you'll truly master them.

---

*This is the main guide for our Design Patterns Deep-Dive series for JVM developers. Individual deep-dive posts for each pattern will be published as the series develops. Stay tuned!*

