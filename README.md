# Java for Scala Developers

Java for Scala developers - practical guide if you want to write Java again.

## Project Structure

This is a multi-module Gradle project with the following modules:

| Module | Description |
|--------|-------------|
| [blog](./blog) | Jekyll-based GitHub Pages blog |
| [java21](./java21) | Java 21 examples with preview features |
| [scala2](./scala2) | Scala 2.13 examples |
| [scala3](./scala3) | Scala 3 examples |
| [kotlin](./kotlin) | Kotlin examples for comparison |

## Blog

Visit the blog at: https://sps23.github.io/java-for-scala-devs

## Building

### Prerequisites

- JDK 21
- Gradle 8.4+ (wrapper included)

### Build All Modules

```bash
./gradlew build
```

### Build Specific Module

```bash
./gradlew :java21:build
./gradlew :scala2:build
./gradlew :scala3:build
./gradlew :kotlin:build
```

### Run Tests

```bash
./gradlew test
```

## Code Formatting

This project uses [Spotless](https://github.com/diffplug/spotless) for code formatting with the following formatters:

| Module | Formatter | Configuration |
|--------|-----------|---------------|
| java21 | Eclipse JDT | [.eclipse-formatter.xml](./java21/.eclipse-formatter.xml) |
| kotlin | ktlint | Default ktlint rules |
| scala2 | Scalafmt | [.scalafmt.conf](./scala2/.scalafmt.conf) |
| scala3 | Scalafmt | [.scalafmt.conf](./scala3/.scalafmt.conf) |

### Check Code Formatting

To check if all code is properly formatted:

```bash
./gradlew spotlessCheck
```

### Apply Code Formatting

To automatically format all code:

```bash
./gradlew spotlessApply
```

### Format Specific Module

```bash
./gradlew :java21:spotlessApply
./gradlew :scala2:spotlessApply
./gradlew :scala3:spotlessApply
./gradlew :kotlin:spotlessApply
```

**Note:** The `build` task automatically includes `spotlessCheck`, so the build will fail if code is not properly formatted.

## Local Blog Development

To run the Jekyll blog locally:

```bash
cd blog
bundle install
bundle exec jekyll serve
```

Then visit `http://localhost:4000/java-for-scala-devs` in your browser.

## CI/CD

This project uses GitHub Actions for continuous integration. The workflow is triggered on:
- Push to `main` branch
- Pull requests to `main` branch

### Required Status Checks

To enforce that all builds pass before merging PRs, configure branch protection rules:

1. Go to **Settings** → **Branches** in the repository
2. Click **Add branch protection rule** (or edit the existing rule for `main`)
3. Set **Branch name pattern** to `main`
4. Check **Require status checks to pass before merging**
5. Search and select the following required status checks:
   - `build-gradle`
   - `build-jekyll`
6. Optionally check **Require branches to be up to date before merging**
7. Click **Save changes**

## License

This project is open source and available under the MIT License.
