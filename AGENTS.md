# AGENTS.md

## Purpose and Scope
- This repository is an educational Java-vs-Scala(-vs-Kotlin) guide, not a single runtime service; optimize for clear, runnable examples and explain trade-offs (`README.md`, `.github/copilot-instructions.md`).
- Keep examples beginner-friendly for Scala developers learning modern Java, and preserve side-by-side comparisons when adding topics.

## Project Map (What connects to what)
- Gradle multi-module root orchestrates code modules + blog; modules are declared only in `settings.gradle` (`blog`, `java21`, `scala2`, `scala3`, `kotlin`).
- Root `build.gradle` only shares group/version/repositories; language-specific behavior lives in each module `build.gradle`.
- `blog/` is Jekyll content: included in Gradle for repo consistency, but actual site build is Bundler/Jekyll (`blog/build.gradle`).
- Topic structure is intentionally mirrored across languages (example: `interview/preparation/payment/FeeCalculator` in `java21`, `scala3`, `kotlin`) to enable direct comparison.

## Fast Developer Workflow
- Build all code modules: `./gradlew build`
- Run all tests: `./gradlew test`
- Build/test one module: `./gradlew :java21:build`, `./gradlew :scala3:test` (same pattern for `scala2`/`kotlin`).
- Enforce formatting (required by `check`): `./gradlew spotlessCheck` or auto-fix `./gradlew spotlessApply`.
- Run blog locally with local overrides: `cd blog && bundle install && bundle exec jekyll serve --config _config.yml,_config.local.yml`.

## Conventions You Must Preserve
- Use package prefix `io.github.sps23` in code modules (`java21/README.md`, `scala2/README.md`, `scala3/README.md`, `kotlin/README.md`).
- Keep Java 21 preview support intact where configured (`--enable-preview` in `java21/build.gradle`, also enabled in `kotlin/build.gradle` and `scala3/build.gradle`).
- Do not remove `tasks.named('check') { dependsOn spotlessCheck }` gates in module builds.
- Tests intentionally use multiple engines by module:
  - Java: JUnit 5 + Mockito (`java21/build.gradle`, e.g. `java21/src/test/java/io/github/sps23/testing/unittesting/UserServiceTest.java`)
  - Scala: ScalaTest + JUnit Platform (`scala2/build.gradle`, `scala3/build.gradle`)
  - Kotlin: JUnit 5 + Kotest (`kotlin/build.gradle`)

## Blog and Content Rules
- Post files live in `blog/_posts/` with date-prefixed filenames and front matter (`layout`, `title`, `date`, `categories`, `tags`).
- Internal links should use `{{ site.baseurl }}` for environment portability (`blog/DEPLOYMENT_CONFIG.md`).
- For language switchers, use the exact HTML code-tabs pattern from `blog/CODE_TABS.md`; do not replace with Markdown code fences.
- Theme/search/filter behavior is centralized in `blog/assets/js/theme.js`; keep selectors compatible across templates.

## CI/CD and Integration Points
- GitHub Actions workflow `.github/workflows/build-deploy.yml` has 3 jobs: `build-gradle`, `build-jekyll`, then `deploy` (main branch push only).
- CI toolchain expectations: JDK 21 (Temurin) + Ruby 3.2; Jekyll builds with production `_config.yml` only.
- Local vs production blog config is layered: `_config.yml` base, `_config.local.yml` overrides `baseurl`/`url` for local testing.

## Sources of AI Guidance in this Repo
- Primary agent guidance files discovered: `README.md`, `java21/README.md`, `scala2/README.md`, `scala3/README.md`, `kotlin/README.md`, `blog/README.md`, `.github/copilot-instructions.md`.

