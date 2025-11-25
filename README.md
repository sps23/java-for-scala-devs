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

## Local Blog Development

To run the Jekyll blog locally:

```bash
cd blog
bundle install
bundle exec jekyll serve
```

Then visit `http://localhost:4000/java-for-scala-devs` in your browser.

## License

This project is open source and available under the MIT License.
