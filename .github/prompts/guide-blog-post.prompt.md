---
mode: agent
description: Create a new guide / roadmap blog post using repository examples and the /grill-me skill
---

## Custom Instruction

You are creating a new **guide-style blog post** for this repository. A guide post is an
umbrella roadmap article like:

- `blog/_posts/2025-11-25-java21-interview-preparation-plan.md`
- `blog/_posts/2025-12-14-spring-framework-interview-preparation-guide.md`
- `blog/_posts/2026-07-26-design-patterns-guide-jvm.md`

Always follow the repository guidance in `.github/copilot-instructions.md` and `AGENTS.md`,
especially the rules for:

- aggregated roadmap / guide posts
- post front matter and `updated:` metadata
- internal post links using `{{ site.baseurl }}/blog/YYYY/MM/DD/slug/`
- WIP placeholder pages being created only **after** the guide is finalized, never during
  the initial guide creation

This prompt must also use the repository skill `/grill-me` as the discovery mechanism before
writing. The `/grill-me` skill is a stateless, depth-first interview helper that asks about
settled prerequisites in small batches and provides recommended default answers. Use it to
shape the guide scope, target audience, section grouping, and topic coverage before drafting.

## Prompt

Create a new guide / roadmap blog post for `${input:topic}`.

Requirements:

1. **Start with `/grill-me` before drafting.**
   Use the `/grill-me` skill to ask depth-first questions about the guide until the
   prerequisites are settled. Use the skill to clarify:
   - the audience and goal of the guide
   - whether the guide should be grouped by difficulty, by topic family, or both
   - which sections belong in the guide
   - which topics belong in each section
   - whether linked deep-dive posts already exist or are still future topics

2. **Model the structure on the repository’s existing guide posts.**
   Cross-check the shape against:
   - `2025-11-25-java21-interview-preparation-plan.md`
   - `2025-12-14-spring-framework-interview-preparation-guide.md`
   - `2026-07-26-design-patterns-guide-jvm.md`

3. **Write the guide as an aggregated roadmap post, not a deep dive.**
   The guide should introduce the subject, explain why it matters, and organize the reader’s
   learning path. Do not include large implementation code blocks in the guide itself.

4. **Use the correct guide-post structure.**
   The guide must contain:
   - a strong lead paragraph built around a real scenario or reader problem
   - multiple main sections grouped either by difficulty level or by a clear topic family
   - for each main section:
     - a `## Section Title`
     - a short punchy introduction paragraph that explains why this section matters
     - a sequence of topic entries
   - for each topic entry:
     - `### N. Topic Name`
     - `**What It Is:**`
     - `**Read the full post:**`
     - `**What You'll Learn:**`
     - `**Interview Questions You Might Face:**`
     - exactly 5 interview questions unless there is a strong repository-backed reason to diverge
     - a separator line `---` after the topic

5. **End the guide with the richer closing structure.**
   After all topic sections, include:
   - `## Your Study Plan`
   - `## Additional Resources`
   - `## Final Thoughts`

6. **Use repository-consistent tone and content.**
   - Write for Scala developers learning Java / JVM ecosystem topics.
   - Keep the voice practical, a bit conversational, and interview-oriented.
   - Prefer concrete learning outcomes over abstract textbook definitions.
   - Each topic should clearly tell the reader why it matters in real work or interviews.

7. **Use valid front matter and metadata.**
   - Use the correct Jekyll post filename under `blog/_posts/`.
   - Include `layout`, `title`, `description`, `date`, `categories`, and `tags`.
   - If editing an existing guide, refresh `updated:` using the current UTC timestamp rounded
     to the hour.
   - Use `roadmap` as a tag for guide-style series posts when appropriate.

8. **Handle links correctly.**
   - Internal links to existing posts must use the exact format
     `{{ site.baseurl }}/blog/YYYY/MM/DD/slug/`.
   - Match the year/month/day to the target post filename exactly.
   - Do not use category prefixes like `/interview/`.
   - Do not guess missing links.
   - If a topic does not have a real article yet, leave the guide entry pointing to the planned
     target only if that is already the repository convention for that guide, and do **not**
     auto-create WIP pages as part of this prompt.

9. **Do not manually add previous/next navigation in Markdown.**
   The blog layout already renders previous and next article links at the bottom of the page.
   Rely on the shared post templates for that behavior instead of hand-authoring navigation.

10. **Keep the guide consistent with the real examples, but prefer the richer format.**
    Use the existing guides as reference, then normalize toward this target shape:
    intro → grouped sections with punch lines → topic entries → study plan →
    additional resources → final thoughts.

11. **Validate the result.**
    Build or preview the blog using the repository’s existing Jekyll workflow and ensure the
    guide renders correctly, links are well-formed, and the structure matches the conventions.

12. **Report back briefly.**
    Summarize the guide created, the section structure chosen, and any assumptions made after
    the `/grill-me` discovery pass.
