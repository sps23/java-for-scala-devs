---
mode: agent
description: Generate a blog post from a plan file/section (JVM: Java 21 vs Scala 2/3 vs Kotlin)
---

## Custom Instruction

You are writing a new entry for a JVM comparison blog series (Java 21 vs Scala 2/3 vs
Kotlin). Always follow the repository guidance in `.github/copilot-instructions.md` and
`AGENTS.md`, especially the blog-post authoring rules, HTML code-tabs rules, and the
Q&A accordion pattern. Treat `${input:planFile}` as the authoritative plan and generate
content for `${input:planSection}` (a specific topic/section/item from that plan).

## Prompt

Create a new blog post for `${input:planSection}` as defined in `${input:planFile}`.

Requirements:

1. Read `${input:planFile}` and locate `${input:planSection}`. Extract the topic intent,
   scope, the parent guide post, and the ordered sibling topics around it. Identify:
   - the parent guide title + file path
   - the current topic entry
   - the next 1-3 sibling topics from the same guide that already have real published posts
   - if there are no later published siblings, the nearest 1-2 related published siblings to use as fallback navigation
2. Use a realistic business case instead of toy examples. Avoid method/type names that
   collide with Scala 3 soft keywords such as `export`, `given`, `enum`, `extension`,
   or `then`.
3. Implement mirrored, production-quality examples in Java 21, Kotlin, Scala 2, and
   Scala 3 under matching `io.github.sps23.<domain>.<topic>` packages, with public API
   documentation where needed.
4. Add mirrored tests in all four modules, including a small realistic client/example
   usage that demonstrates the pattern in action.
5. Run `./gradlew spotlessApply` and then `./gradlew spotlessCheck build`; fix any
   failures before continuing.
6. Write the blog post following the repository’s established structure for deep-dive
   design-pattern posts: problem statement, Key Concepts table, Real Use Case,
   Component Walkthrough, Request Flow, implementation code-tabs, test code-tabs,
   comparison table, When to Use / Best Practices, Conclusion, Code Samples, and series
   footer.
7. If the plan section includes interview Q&A items, add a dedicated `## Interview Q&A: ...`
   section immediately before the Conclusion section. Follow the repository Q&A rules:
   - one question/answer pair per `details` item
   - use `<div class="faq-list">` and `<details class="faq-item" open>`
   - each item must contain `<summary>` plus a right-side `<span class="faq-toggle" aria-hidden="true"></span>`
   - each answer goes inside a `<div class="faq-answer">`
   - keep answers expanded by default via the `open` attribute
   - write simple but precise answers in plain English, with a direct idea, a real-world effect, and a concrete example
   - keep the styling in `blog/assets/css/template1-minimal-dark.css`; do not inline CSS in the Markdown
8. Use valid Jekyll HTML code-tabs with tokenized `<span>` markup for syntax highlighting;
   do not use raw code inside `<code>` blocks.
9. Update `${input:planFile}` so the topic entry links to the new post and refresh the
   `updated:` date.
10. Do not create placeholder WIP pages during the initial creation of a guide post. WIP
   pages are intentionally created only after the guide itself is finalized and only for
   sections whose final article does not yet exist. When a WIP page is needed, use the
   `layout: wip` pattern from `blog/_layouts/wip.html` with a `guide_url` back-link,
   a brief “What You'll Learn” preview, and the shared styling in
   `blog/assets/css/template1-minimal-dark.css`.
11. Add a guide-navigation footer at the bottom of the article, after `## Code Samples`.
    The footer must:
    - link back to the parent guide using the guide post's actual title as the anchor text
    - include 1-3 next related published posts from the same guide
    - fall back to nearby related siblings when this article is the last published item
    - use `{{ site.baseurl }}{% link _posts/YYYY-MM-DD-slug.md %}` for every internal post link so the rendered URL resolves to `/blog/YYYY/MM/DD/slug/`
12. When you update `${input:planFile}`, keep its topic order accurate because downstream
    child posts depend on that order to build related-post navigation correctly.
13. Report back with a concise summary of the changes made.
