# Copilot Instructions

This repository is a practical, educational guide for Scala developers learning modern Java.
Prefer clear, runnable examples and explicit trade-offs over framework-heavy abstractions.

## Project Scope and Structure

- Multi-module Gradle project with modules declared in `settings.gradle`: `blog`, `java21`, `scala2`, `scala3`, `kotlin`.
- Root `build.gradle` provides shared `group`, `version`, and repositories only; keep language/tooling behavior inside each module `build.gradle`.
- `blog/` is Jekyll content and is built with Bundler/Jekyll (Gradle includes it for repository consistency).
- Keep comparison topics mirrored across language modules where possible (for example `interview/preparation/payment/FeeCalculator`).

## Audience and Content Style

- Write for Scala developers transitioning to Java 21.
- Keep explanations beginner-friendly and practical; avoid unnecessary jargon.
- When introducing concepts, show "Java vs Scala" and include Kotlin when a matching example already exists.
- Prioritize examples that compile and run from the module as-is.

## Language and Module Standards

### Java 21 (`java21/`)

- Use standard Java naming conventions, meaningful names, and focused methods.
- Prefer immutable types and `record` where it improves clarity.
- Keep JavaDoc for public APIs when the behavior is non-obvious.
- Preserve Java 21 preview support configured in `java21/build.gradle` (`--enable-preview` for compile/test/run).

### Scala 2.13 (`scala2/`)

- Write idiomatic Scala 2.13 with functional style where appropriate.
- Keep examples concise and comparison-friendly with Java counterparts.
- Follow repository formatting rules from `scala2/.scalafmt.conf` via Spotless.

### Scala 3 (`scala3/`)

- Prefer idiomatic Scala 3 syntax and patterns consistent with existing examples.
- Keep feature parity with Java topics when adding new comparison content.
- Respect Scala 3 formatting and rewrites from `scala3/.scalafmt.conf` via Spotless.
- Preserve preview-related compiler settings already configured in `scala3/build.gradle`.

## Cross-Module Conventions

- Use package prefix `io.github.sps23` in code modules.
- Do not remove `tasks.named('check') { dependsOn spotlessCheck }` from module builds.
- Keep tests aligned with module stacks:
  - `java21`: JUnit 5 + Mockito
  - `scala2`/`scala3`: ScalaTest + JUnit Platform
  - `kotlin`: JUnit 5 + Kotest

## Workflow and Validation

- Build all modules: `./gradlew build`
- Run all tests: `./gradlew test`
- Build/test specific modules: `./gradlew :java21:build`, `./gradlew :scala2:test`, `./gradlew :scala3:test`, `./gradlew :kotlin:test`
- Format and verify style: `./gradlew spotlessApply` / `./gradlew spotlessCheck`

## Blog Content Rules (`blog/`)

- Blog posts go in `blog/_posts/` with date-prefixed filenames and complete front matter (`layout`, `title`, `date`, `categories`, `tags`).
- Use `{{ site.baseurl }}` in internal links for environment portability.
- For language-switching snippets, use the exact HTML code-tabs structure from `blog/CODE_TABS.md` (not Markdown fences).
- Keep selectors compatible with `blog/assets/js/theme.js` (search/filter/tabs behavior is centralized there).

## Development Flow for New Topics

1. Add or update mirrored examples in `java21` and at least one Scala module (`scala3` preferred when both are not required).
2. Keep names, package paths, and problem framing aligned so readers can compare line-by-line.
3. Explain differences and trade-offs between Java and Scala approaches in docs/posts.
4. Run module tests and formatting checks before finalizing changes.
