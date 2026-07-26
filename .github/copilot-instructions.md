# Copilot Instructions

This repository is a practical, educational guide for Scala developers learning modern Java 21.
Prefer clear, runnable examples and explicit trade-offs over framework-heavy abstractions.

---

## Project Scope and Structure

- Multi-module Gradle project; submodules declared in `settings.gradle`: `blog`, `java21`, `scala2`, `scala3`, `kotlin`.
- Root `build.gradle` provides shared `group`, `version`, and repositories only. Language-specific config belongs in each module's own `build.gradle`.
- `blog/` is Jekyll content built with Bundler/Jekyll. Gradle includes it for repository consistency only.
- Topics are intentionally **mirrored across language modules** (e.g. `io.github.sps23.interview.preparation.payment.FeeCalculator` exists in `java21`, `scala3`, `kotlin`).

---

## Audience and Content Style

- Write for **Scala developers transitioning to Java 21**.
- Every concept should clearly answer "how does this Java feature compare to its Scala/Kotlin equivalent?".
- Keep examples beginner-friendly, practical, and self-contained — they must compile and run from the module root as-is.
- Show "Java vs Scala" by default; add Kotlin when a matching example already exists in the `kotlin/` module.

---

## Code Formatting — No Tabs Anywhere

Tabs are **never** used as indentation in this project across all languages.

### Java 21 (`java21/`)

- **Indentation**: 4 spaces (enforced by Eclipse formatter — `java21/.eclipse-formatter.xml`)
- **Max line length**: 100 characters
- **Brace style**: K&R (opening brace at end of line)
- **Spaces inside parens**: none — `method(arg)` not `method( arg )`
- Spotless enforces this via `eclipse('4.30').configFile(rootProject.file('java21/.eclipse-formatter.xml'))`.

```java
// ✅ Correct Java formatting
public record Person(String name, int age) {
    public String greet() {
        return "Hello, " + name;
    }
}
```

### Scala 2.13 (`scala2/`)

- **Indentation**: 2 spaces (enforced by Scalafmt — `scala2/.scalafmt.conf`)
- **Max column**: 100
- **Dialect**: `scala213`
- **Rewrites**: `RedundantBraces`, `RedundantParens`, `SortImports`, `SortModifiers`
- **Alignment**: `align.preset = more`; aligns `<-`, `=`, `=>` tokens

```scala
// ✅ Correct Scala 2.13 formatting
case class Person(name: String, age: Int) {
  def greet: String = s"Hello, $name"
}
```

### Scala 3 (`scala3/`)

- **Indentation**: 2 spaces (enforced by Scalafmt — `scala3/.scalafmt.conf`)
- **Max column**: 100
- **Dialect**: `scala3`
- **Rewrites**: same as Scala 2 plus `rewrite.scala3.convertToNewSyntax = true` and `rewrite.scala3.removeOptionalBraces = true`
- Prefer new Scala 3 indentation syntax (no braces); use `end` markers for long blocks.

```scala
// ✅ Correct Scala 3 formatting
case class Person(name: String, age: Int):
  def greet: String = s"Hello, $name"
```

### Kotlin (`kotlin/`)

- **Indentation**: 4 spaces (enforced by ktlint via Spotless)
- **Max line length**: standard ktlint default (100)
- Spotless runs `ktlint('1.1.1')`.

```kotlin
// ✅ Correct Kotlin formatting
data class Person(val name: String, val age: Int) {
    fun greet(): String = "Hello, $name"
}
```

**Apply formatting before committing:**
```bash
./gradlew spotlessApply   # auto-fix all modules
./gradlew spotlessCheck   # verify only (run by CI)
```

**Before pushing any branch, always run:**
```bash
./gradlew spotlessApply
./gradlew spotlessCheck
```

---

## Language and Module Standards

### Java 21

- Prefer `record` for pure data carriers; `sealed` + pattern matching for ADTs.
- Use `var` for local variables when type is obvious from the RHS.
- Keep JavaDoc on public API methods when behavior is non-obvious.
- Preserve `--enable-preview` configured in `java21/build.gradle`; never remove it.
- Test stack: **JUnit 5 + Mockito**. Use `@ExtendWith(MockitoExtension.class)`.

### Scala 2.13

- Write idiomatic functional Scala 2.13: `Option`, `Either`, `map`/`flatMap` over null/exceptions.
- Keep examples concise and comparison-friendly with Java counterparts.
- Test stack: **ScalaTest** (`AnyFunSuite`, `AnyFlatSpec`, `AnyWordSpec`) + **JUnit Platform**.
- Use `with Matchers` and `shouldBe` / `should be` style assertions.

### Scala 3

- Prefer idiomatic Scala 3: new indentation syntax, `given`/`using` for type-classes, `enum` for ADTs.
- Use `extension` methods instead of implicit classes.
- ZIO is available (`dev.zio:zio_3:2.1.9`): use `UIO`, `ZIO.succeed`, `fork`, `join` for fibre examples.
- Test stack: **ScalaTest** + **JUnit Platform** (`includeEngines('scalatest', 'junit-jupiter')`).
- Run ZIO effects in tests via `Runtime.default.unsafe.run(effect).getOrThrow()`.

### Kotlin

- Use coroutines (`kotlinx-coroutines-core`) for async examples; pair with Java virtual threads for comparison.
- Test stack: **JUnit 5 + Kotest** (`FunSpec`, `StringSpec`, `io.kotest:kotest-runner-junit5`).

---

## Cross-Module Conventions

- Package prefix: `io.github.sps23` in every code module.
- Never remove `tasks.named('check') { dependsOn spotlessCheck }` from any module build.
- Mirror class/package names across modules so readers can compare line-by-line.
- Local-only tests (slow or environment-dependent) use the `runLocalOnlyTests` flag pattern:

  ```groovy
  // In module build.gradle
  if (!project.hasProperty('runLocalOnlyTests')) {
      filter { excludeTestsMatching 'io.github.sps23.SomeSlowTest' }
  }
  ```

  Run locally with: `./gradlew :scala2:test -PrunLocalOnlyTests`

---

## Workflow and Validation

```bash
./gradlew build                          # Build all modules
./gradlew test                           # Run all tests (MathUtilsTest excluded by default)
./gradlew :java21:build                  # Single-module build
./gradlew :scala3:test                   # Single-module test
./gradlew :scala2:test -PrunLocalOnlyTests  # Include local-only tests
./gradlew spotlessApply                  # Auto-format all code
./gradlew spotlessCheck                  # Check formatting (blocks CI on failure)
```

Blog (local preview):
```bash
cd blog && bundle install && bundle exec jekyll serve --config _config.yml,_config.local.yml
```

---

## Blog Post Authoring (`blog/_posts/`)

This section is the complete, step-by-step recipe for creating a new post.
Follow every step in order and cross-check against the real post examples cited.

---

### Step 1 — Choose the Topic

Valid topics are strictly limited to:
- A **Java 21 feature** explained for Scala developers (records, sealed classes, pattern matching, `var`, virtual threads, `Optional`, streams, `switch` expressions, text blocks, structured concurrency)
- A **cross-JVM language comparison** (Java 21 vs Scala 2/3 vs Kotlin) on a shared concept
- A **Spring / Java framework interview topic** when it is core to day-to-day JVM development (IoC, bean scopes, Spring MVC, configuration, testing, transactions, security)
- **Testing** on the JVM (JUnit 5, ScalaTest, Kotest, Mockito)
- **Interview preparation** for Java/JVM roles

Do **not** write about: non-JVM languages, cloud deployment, unrelated design patterns, or opinion pieces without code.

**Check existing posts** in `blog/_posts/` first — if a post already covers the topic, extend it or write a complementary angle rather than duplicating.

---

### Step 1.5 — Identify the Post Family First

Before writing, classify the post. Existing posts in this repository fall into a few recurring families, and each family has a different structure.

#### 1. Aggregated roadmap / guide posts

Use this shape for umbrella posts that introduce a subject and list subtopics:
- `2025-11-25-java21-interview-preparation-plan.md`
- `2025-12-14-spring-framework-interview-preparation-guide.md`

These posts:
- usually use category `interview`
- open with a conversational "why this guide exists" introduction
- organise material into **Basic / Medium / Advanced** sections (optionally Bonus)
- list many subtopics rather than going deep on one
- use a repeated pattern per topic:
  - `### N. Topic Name`
  - `**What It Is:**`
  - `**Read the full post:**`
  - `**What You'll Learn:**`
  - `**Interview Questions You Might Face:**`
  - horizontal rule `---`

Write an aggregated guide only when the user wants a **main subject page**, a **series index**, or an **interview roadmap**.

#### 2. Deep-dive interview/tutorial posts

Use this shape for the main body of the blog. Examples:
- `2025-11-26-immutable-data-with-java-records.md`
- `2025-11-29-collection-factory-methods-and-stream-basics.md`
- `2026-05-25-spring-ioc-and-dependency-injection.md`
- `2026-07-26-spring-configuration-approaches.md`

These posts:
- usually use category `interview`, `concurrency`, `functional-programming`, or `testing`
- start with a concrete problem or scenario
- teach one focused subject end-to-end
- include runnable code and practical trade-offs
- usually end with `## Code Samples` and a series footer when applicable

Prefer this family unless the user explicitly asks for an overview/index post.

#### 3. Cross-language comparison posts

Use this shape when the same idea is shown in Java, Scala, and optionally Kotlin. Examples:
- `2025-11-29-collection-factory-methods-and-stream-basics.md`
- `2026-05-25-zio-fibres-vs-virtual-threads-vs-coroutines.md`
- `2025-12-01-comparing-jvm-test-frameworks.md`

These posts:
- compare syntax, ergonomics, and trade-offs across JVM languages
- use **HTML code-tabs** for equivalent multi-language examples
- include one or more comparison tables
- explicitly explain how the Java feature maps to a Scala/Kotlin mental model

Default to Java + Scala. Add Kotlin when the repository already contains a matching Kotlin example or the comparison would feel incomplete without it.

#### 4. Java-only framework / best-practice posts

Use this shape for Spring, testing, or Java gotcha posts where forcing Scala/Kotlin would add noise. Examples:
- `2025-12-03-tricky-java-patterns-everyone-uses.md`
- `2025-12-14-effective-unit-testing-in-java.md`
- the Spring series posts under `2026-05-*` and `2026-07-26`

These posts:
- are usually Java-centric
- still speak to Scala developers by referencing familiar ideas and trade-offs
- may organise content by numbered patterns, subsections, or a table of contents instead of code-tabs
- should still include runnable Java code and practical guidance

For Spring posts, do **not** force Scala/Kotlin tabs just for symmetry. Keep them Java-focused unless a comparison genuinely helps explain the concept.

#### 5. Lightweight intro / announcement posts

Use only for short orientation posts such as:
- `2025-11-22-welcome-to-java-for-scala-devs.md`

These are shorter, simpler, and more welcoming than the rest of the blog. Do not use this lightweight structure for serious technical articles.

---

### Step 2 — Create the File

```
blog/_posts/YYYY-MM-DD-kebab-case-title.md
```

- Date = **today's date** unless you intentionally backdate for ordering
- Slug = lowercase, hyphens, no special chars
- Keep the slug concise: 3–6 words

**Real examples:**
```
2025-11-23-java-records-vs-scala-case-classes.md
2025-11-28-sealed-classes-and-exhaustive-pattern-matching.md
2025-12-01-comparing-jvm-test-frameworks.md
2026-05-25-zio-fibres-vs-virtual-threads-vs-coroutines.md
```

---

### Step 3 — Write the Front Matter

Every post **must** open with this seven-field YAML block:

```yaml
---
layout: post
title: "Descriptive Title in Title Case"
description: "One-sentence SEO description covering what the post teaches and why it matters."
date: 2026-MM-DD HH:MM:00 +0000
updated: 2026-MM-DD HH:MM:00 +0000
categories: [<category>]
tags: [java, java21, scala, scala3, kotlin, topic-tag, topic-tag]
---
```

#### `layout`
Always `post`. Never omit.

#### `title`
Title Case, wrapped in double quotes. Be specific — readers should know immediately what they'll learn.

```yaml
# ✅ Good
title: "Sealed Classes and Exhaustive Pattern Matching"
title: "ZIO Fibres vs Java Virtual Threads vs Kotlin Coroutines"

# ❌ Too vague
title: "Pattern Matching"
title: "Concurrency"
```

#### `description`
One sentence, ~150–200 chars, SEO-friendly. Name the specific APIs or concepts covered.

```yaml
# ✅ Good — from real posts
description: "Model type-safe domain logic with Java 17 sealed classes and exhaustive pattern matching - compare with Scala sealed traits and Kotlin sealed classes with payment system examples."
description: "A practical comparison of ZIO fibres in Scala 3, Java 21 virtual threads, and Kotlin coroutines — covering fork/join, typed error handling, concurrent composition, and when to use each approach."
```

#### `date`
Format: `YYYY-MM-DD HH:MM:00 +0000`. Always `+0000` (UTC). Use a realistic time of day:
- Morning post: `10:00:00`
- Afternoon post: `14:00:00` or `15:00:00`
- Evening post: `18:00:00` or `21:00:00`

```yaml
date: 2026-05-25 13:00:00 +0000   # ✅
date: 2025-11-28 21:00:00 +0000   # ✅
date: 2026-05-25 +0000            # ❌ missing time
date: 2026-05-25 13:00:00 +0100   # ❌ wrong timezone
```

#### `updated`
**Optional.** Set this field when a blog post has been meaningfully modified after its initial publication. Format matches `date`: `YYYY-MM-DD HH:MM:00 +ZZZZ`.

- **First publication**: omit `updated` entirely (or use `date` if you prefer explicit tracking)
- **After edits**: add `updated` with the most recent modification timestamp
- **Each change**: refresh the `updated` field to the current date/time
- **Homepage sorting**: posts are sorted by `updated` (if present) or `date` (fallback); posts with newer `updated` dates float to the top of "Latest Posts" and "Post Timeline"

```yaml
# ✅ New post — no updates yet
date: 2026-05-25 13:00:00 +0000

# ✅ Post updated once
date: 2026-05-25 13:00:00 +0000
updated: 2026-07-26 11:05:20 +0100

# ✅ Post updated multiple times — keep current `updated` date
date: 2026-05-25 13:00:00 +0000
updated: 2026-08-10 15:30:00 +0100  # most recent edit
```

When a post is significantly updated (code samples fixed, examples improved, sections rewritten, links corrected), always refresh `updated` to bring it to the top of the homepage feed.

#### `categories`
Use **one** primary category from this controlled list. A second category is acceptable only when the post genuinely spans two styles.

| Category | When to use |
|---|---|
| `introduction` | Welcome or overview posts (`2025-11-22-welcome-to-java-for-scala-devs.md`) |
| `features` | Focused language-feature comparisons, especially older concise Java-vs-Scala posts |
| `functional-programming` | Lambdas, streams, `Optional`, higher-order functions |
| `concurrency` | Virtual threads, ZIO fibres, coroutines, structured concurrency |
| `testing` | JUnit 5, ScalaTest, Kotest, testing patterns, anti-patterns |
| `interview` | Interview prep series, framework guides, and practical deep-dives used in interview preparation |
| `best-practices` | Java-only gotchas, performance pitfalls, and practice-oriented guidance |
| `humor` | Secondary category only for intentionally playful posts such as satire; do not use by default |

```yaml
categories: [concurrency]          # ✅ typical
categories: [interview]            # ✅ sealed classes post uses this
categories: [testing]              # ✅
categories: [testing, best-practices]  # ✅ two when both apply
```

#### `tags`
Always include the **language tags** for every language the post covers, then add **topic tags**:

```yaml
# Feature comparison post (Java + Scala + Kotlin)
tags: [java, java21, scala, scala3, kotlin, sealed-classes, pattern-matching]

# Java-only deep dive
tags: [java, java21, virtual-threads, project-loom, concurrency]

# ZIO/concurrency comparison
tags: [scala, scala3, zio, fibres, java, java21, kotlin, coroutines, virtual-threads, concurrency]

# Testing post
tags: [java, scala, kotlin, junit, scalatest, kotest, testing]

# Java-only Spring / framework post
tags: [java, java21, spring, spring-boot, dependency-injection, ioc, interview-preparation]

# Java-only gotchas / best-practices post
tags: [java, java21, performance, best-practices, gotchas, optimization]
```

---

### Step 4 — Write the Lead Paragraph

The very first paragraph (right after the front matter, no heading) must hook the reader with a **concrete problem or scenario**, not a definition.

```markdown
# ✅ From 2026-05-25-zio-fibres-vs-virtual-threads-vs-coroutines.md
Imagine you need to fetch a user's profile and their order history at the same time.
You could do them one after the other like a very patient accountant, or you could do
them *at the same time* like a competent developer.

# ✅ From 2025-11-28-sealed-classes-and-exhaustive-pattern-matching.md
Sealed classes are a powerful feature for type-safe domain modeling. Java 17 introduced
sealed classes and interfaces, bringing Java closer to Scala's sealed traits and Kotlin's
sealed classes. In this post, we'll explore how to model a payment system...

# ❌ Bad — starts with a dictionary definition
A sealed class is a class that restricts which other classes can extend it.
```

---

### Step 5 — Structure the Body

Use the structure that matches the post family.

#### Default structure for deep-dive posts

Use this section order as the baseline template:

```
## The Problem / Context           ← what are we solving, and why does it matter?
## Key Concepts                    ← comparison table of syntax/behaviour differences
## The Solution / Implementation   ← code tabs showing Java, Scala, Kotlin side by side
## [Optional deep-dive section]    ← pattern guards, error handling, advanced usage
## Comparison Table                ← summary <div class="table-wrapper"> table
## When to Use / Best Practices    ← 4–6 bullet points
## Conclusion                      ← 2–4 sentences bridging Java ↔ Scala mental model
## Code Samples                    ← links to runnable repo files
```

Not every post needs all sections, but this order must be preserved when sections exist.

#### Structure for aggregated roadmap / guide posts

Use this shape instead of the deep-dive template when writing an overview/index post:

```
Lead paragraph(s): why this guide exists, who it is for
## Basic Level - ...
### 1. Topic Name
**What It Is:**
**Read the full post:**
**What You'll Learn:**
**Interview Questions You Might Face:**
---
## Medium Level - ...
... repeat ...
## Advanced Level - ...
... repeat ...
## Conclusion / Next Step (optional)
```

These guide posts are intentionally repetitive and scannable. Do not add large code blocks to the guide itself; save code for the linked deep-dive posts.

#### Structure for Java-only best-practice / gotcha posts

When the post is organised around multiple pitfalls or patterns, use this shape:

```
Lead paragraph: why these pitfalls matter
## 1. Pattern / Pitfall Name
### The Problem / Confusion
### The Truth / Explanation
### Correct Approach / Takeaway
## 2. ...
## Key Takeaways / Conclusion
```

This is the pattern used by `2025-12-03-tricky-java-patterns-everyone-uses.md`.

#### Structure for longer Java-only testing/framework guides

When the post is a long Java-only reference article (for example testing or Spring), a table of contents is acceptable:

```
Lead paragraph
## Table of Contents
## Section 1
## Section 2
...
## Best Practices
## Common Anti-Patterns / Conclusion
```

Use this shape only when the article genuinely needs many long sections. Prefer the deep-dive template for normal posts.

#### Note on old vs. current house style

Some early posts in `2025-11-22` and `2025-11-23` are shorter and simpler. Treat them as historical examples, not the default target. For new posts, prefer the richer, more structured style used by:
- `2025-11-26-immutable-data-with-java-records.md`
- `2025-11-29-collection-factory-methods-and-stream-basics.md`
- `2026-05-25-spring-ioc-and-dependency-injection.md`
- `2026-07-26-spring-configuration-approaches.md`

---

### Step 6 — Comparison Tables

Wrap **every** Markdown table in a scrollable div:

```markdown
<div class="table-wrapper" markdown="1">

| Feature | Java 21 | Scala 3 | Kotlin |
|---------|---------|---------|--------|
| Sealed keyword | `sealed ... permits` | `sealed trait/class` | `sealed class` |
| Must list subtypes | Yes (`permits`) | No (same file) | No (same package) |
| Pattern matching | `switch` expression | `match` expression | `when` expression |

</div>
```

The blank lines inside the div are **required** for `markdown="1"` to parse the table.

---

### Step 7 — Code Examples

#### Single-language blocks — use Markdown fences

Use regular fenced code blocks when showing one language at a time, or in prose comparisons:

````markdown
```java
public record Person(String name, int age) {}
```

```scala
case class Person(name: String, age: Int)
```
````

#### Multi-language blocks — use HTML code-tabs

When the **same concept** is shown in Java, Scala, and Kotlin side by side, use the HTML code-tabs structure. **Never** use Markdown fences here — Jekyll's syntax highlighter won't work inside them.

```html
<div class="code-tabs" data-tabs-id="UNIQUE-ID-PER-POST">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="scala" data-lang="Scala 3">Scala 3</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code>public record Person(String name, int age) {}
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code>case class Person(name: String, age: Int)
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code>data class Person(val name: String, val age: Int)
</code></pre></div></div>
</div>
</div>
```

**Code-tabs rules (violations break the page):**

| Rule | Detail |
|---|---|
| Unique `data-tabs-id` | Use sequential IDs per post: `tabs-1`, `tabs-2`, … or descriptive: `sealed-basic`, `sealed-guards` |
| Java tab first + `active` | First button and first content div always get `class="active"` |
| `data-lang` on buttons | Include `data-lang="Java 21"` alongside `data-tab="java"` |
| No blank lines inside `<div>` | Jekyll inserts `<p>` tags around blank lines, breaking the structure |
| `<code>` ends on its own line | The closing `</code>` must be on a line immediately after the last code line |
| Language class matches | `language-java`, `language-scala`, `language-kotlin` must match the actual language |

**Real post to study:** `2025-11-28-sealed-classes-and-exhaustive-pattern-matching.md` — three full code-tabs blocks from `tabs-1` to `tabs-3`.

#### Code size guidelines

- 10–30 lines per block is ideal
- If a block exceeds 30 lines, split into multiple smaller examples with intermediate explanations
- Add comments inside code to highlight the Java ↔ Scala difference (e.g. `// No default needed - compiler verifies all cases are covered`)

#### When NOT to use code-tabs

Do **not** use code-tabs when:
- the post is intentionally Java-only (Spring, JUnit/Mockito, tricky Java patterns)
- there is no meaningful matching Scala/Kotlin implementation
- the examples are not equivalent across languages

In those cases, use normal fenced code blocks and explain the Scala/Kotlin mental model in prose instead.

---

### Step 8 — Internal Links and Repo Links

**Internal links** — always use `{{ site.baseurl }}`:
```markdown
[full preparation plan]({{ site.baseurl }}/interview/2025/11/25/java21-interview-preparation-plan)
```

**Repo links** — link directly to GitHub with full path:
```markdown
- [Java 21 Payment System](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/interview/preparation/payment)
- [Scala 3 Payment System](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/interview/preparation/payment)
- [Kotlin Payment System](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/payment)
```

---

### Step 9 — End the Post

Close with two optional but strongly encouraged sections:

**Code Samples** — always link to the runnable repo files:
```markdown
## Code Samples

All examples in this post are runnable. Find them in the repository:
- [Scala 3 implementation](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/...)
- [Java 21 implementation](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/...)
- [Kotlin implementation](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/...)
```

**Series footer** — if the post belongs to a series, add an italicised footnote:
```markdown
---

*This is part of our Java 21 Interview Preparation series. Check out the
[full preparation plan]({{ site.baseurl }}/interview/2025/11/25/java21-interview-preparation-plan) for more topics.*
```

Use the correct series footer for the subject:
- Java 21 interview topics → link to `2025-11-25-java21-interview-preparation-plan.md`
- Spring posts → link to `2025-12-14-spring-framework-interview-preparation-guide.md`
- Standalone posts (for example tricky Java patterns) may omit the footer if there is no real parent series

---

### Step 10 — Validate Before Committing

```bash
# Preview the post locally
cd blog && bundle exec jekyll serve --config _config.yml,_config.local.yml

# Then open http://localhost:4000 and:
# 1. Click every code tab — all three languages must render with syntax highlighting
# 2. Resize to mobile width — comparison tables must scroll horizontally
# 3. Check all internal links resolve correctly
```

---

### Quick-Reference Checklist

```
[ ] File named YYYY-MM-DD-slug.md in blog/_posts/
[ ] All 7 front matter fields present (layout, title, description, date, updated [if modified], categories, tags)
[ ] date uses +0000 timezone with realistic time (HH:MM:00)
[ ] updated present and accurate if post was edited after initial publication
[ ] categories uses exactly one value from the controlled list
[ ] tags includes language tags (java, java21, scala, scala3, kotlin) + topic tags
[ ] Lead paragraph opens with a problem/scenario, not a definition
[ ] Comparison tables wrapped in <div class="table-wrapper" markdown="1">
[ ] Multi-language code uses HTML code-tabs (not Markdown fences)
[ ] Every code-tabs block has a unique data-tabs-id within the post
[ ] Java tab comes first and has class="active"
[ ] No blank lines inside <div> tags in code-tabs
[ ] data-lang attribute present on all tab buttons
[ ] Code blocks are 10-30 lines each
[ ] Internal links use {{ site.baseurl }}
[ ] Post ends with Code Samples section linking to GitHub repo paths
[ ] Previewed locally with Jekyll before committing
```

See `blog/CODE_TABS.md` for the full code-tabs specification and troubleshooting guide.

---

## Development Flow for New Topics

1. Identify a Java 21 feature or pattern and its Scala/Kotlin equivalent.
2. Add runnable code to `java21/` and `scala3/` (and `kotlin/` if applicable), with mirrored package paths.
3. Write tests for all language variants using the module's test stack.
4. Run `./gradlew spotlessApply` then `./gradlew build` to validate.
5. Create a blog post in `blog/_posts/` following the front matter and code-tabs conventions above.
6. Preview locally with Jekyll before committing.
