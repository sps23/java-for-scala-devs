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

### Gradle Multi-Module Setup

This project uses a standard Gradle multi-module layout optimized for GitHub Pages:

```
java-for-scala-devs/
├── build.gradle       # Root build config (shared settings)
├── settings.gradle    # Declares all submodules
├── blog/              # Jekyll blog (GitHub Pages content)
│   └── build.gradle   # Minimal Gradle config (Jekyll handles build)
├── java21/
│   └── build.gradle   # Java-specific plugins and config
├── scala2/
│   └── build.gradle   # Scala 2.13-specific config
├── scala3/
│   └── build.gradle   # Scala 3-specific config
└── kotlin/
    └── build.gradle   # Kotlin-specific config
```

**Key points:**
- Submodules are declared in `settings.gradle` using `include` statements (not in `build.gradle`)
- The root `build.gradle` contains shared configuration via `allprojects` and `subprojects` blocks
- Each submodule has its own `build.gradle` for language-specific plugins and dependencies
- The `blog` module is included for consistency but uses Jekyll for the actual build

## Blog

Visit the blog at: https://javaforscaladevs.com

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

### Blog Features

The blog includes several custom features to enhance the learning experience:

#### Interactive Code Tabs

Compare Java, Scala, and Kotlin implementations side-by-side with interactive tabs. Click to switch between languages while maintaining syntax highlighting.

- **Implementation:** Button-based with JavaScript switching
- **Styling:** Language-specific colors (Java=blue, Scala=red, Kotlin=purple)
- **Compatibility:** GitHub Pages compatible, no plugins required
- **Documentation:** See [blog/CODE_TABS.md](blog/CODE_TABS.md)

#### Multiple Theme Templates

Choose from three modern dark theme templates, each optimized for different content styles. See [blog/README.md](blog/README.md) for details.

#### Search and Filtering

Built-in search functionality and category filtering to help readers find relevant content quickly.

Then visit `http://localhost:4000/java-for-scala-devs` in your browser.

## CI/CD

This project uses GitHub Actions for continuous integration. The workflow is triggered on:
- Push to `main` branch
- Pull requests to `main` branch

### GitHub Pages Setup

To deploy the Jekyll blog to GitHub Pages, you need to enable GitHub Pages in repository settings (one-time setup):

1. Go to **Settings** → **Pages** in the repository
2. Under **Build and deployment**, set **Source** to **GitHub Actions**
3. Save the settings

Once enabled, the workflow will automatically build and deploy the blog on push to `main`.

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
