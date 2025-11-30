---
layout: post
title: "Null-Safe Programming with Optional"
description: "Eliminate NullPointerException with Java Optional API - learn map, flatMap, filter patterns and compare with Scala Option and Kotlin null-safety features."
date: 2025-11-29 18:00:00 +0000
categories: interview
tags: java java21 scala kotlin optional null-safety interview-preparation
---

This is Part 4 of our Java 21 Interview Preparation series. We'll explore the Optional API and null-safe programming patterns, comparing Java 21's approach with Scala 3's Option and Kotlin's built-in null-safety.

## The Problem: Handling Missing Values

One of the most common sources of bugs in Java applications is the dreaded `NullPointerException`. Traditional null checking leads to verbose, error-prone code with nested conditionals.

**Problem Statement:** Implement a service that fetches user preferences with fallback defaults, avoiding null checks.

### Traditional Null Checking (Don't Do This!)

```java
// Nested null checks - verbose and error-prone
String getTheme(String userId) {
    User user = database.get(userId);
    if (user != null) {
        UserPreference pref = user.getPreference();
        if (pref != null) {
            String theme = pref.getTheme();
            if (theme != null) {
                return theme;
            }
        }
    }
    return "light"; // default
}
```

## Optional API Basics

Java 8 introduced `Optional<T>` to represent values that may or may not be present. Modern Java (9+) has enhanced this API significantly.

### Creating Optionals

#### Java 21

```java
// From nullable value
Optional<String> opt1 = Optional.ofNullable(maybeNull);

// From non-null value (throws if null)
Optional<String> opt2 = Optional.of("value");

// Empty optional
Optional<String> opt3 = Optional.empty();
```

#### Scala 3

```scala
// From nullable value (handles null from Java interop)
val opt1: Option[String] = Option(maybeNull)

// Explicit Some/None
val opt2: Option[String] = Some("value")
val opt3: Option[String] = None
```

#### Kotlin

```kotlin
// Kotlin uses nullable types instead of Optional
val opt1: String? = maybeNull

// Non-null value
val opt2: String = "value"

// Null value
val opt3: String? = null
```

## Extracting Values: orElse, orElseGet, orElseThrow

### orElse() - Provide Default Value

Use when the default is already computed or cheap to create.

#### Java 21

```java
// Returns theme or "light" if empty
String theme = findUserPreference(userId)
    .map(UserPreference::theme)
    .orElse("light");
```

#### Scala 3

```scala
// getOrElse is equivalent to orElse
val theme = findUserPreference(userId)
  .flatMap(_.theme)
  .getOrElse("light")
```

#### Kotlin

```kotlin
// Elvis operator (?:) is equivalent to orElse
val theme = findUserPreference(userId)?.theme ?: "light"
```

### orElseGet() - Lazy Default Computation

Use when the default is expensive to compute.

#### Java 21

```java
// Supplier is only called if Optional is empty
String theme = findUserPreference(userId)
    .map(UserPreference::theme)
    .orElseGet(() -> computeExpensiveDefault());
```

**Important Difference:**

```java
// orElse: default is ALWAYS evaluated
opt.orElse(expensiveOperation()); // expensiveOperation() called even if opt has value!

// orElseGet: default is only evaluated if needed
opt.orElseGet(() -> expensiveOperation()); // expensiveOperation() called only if opt is empty
```

#### Scala 3

```scala
// getOrElse is already lazy in Scala (by-name parameter)
val theme = opt.getOrElse(computeExpensiveDefault())
```

#### Kotlin

```kotlin
// Elvis operator is already lazy
val theme = opt ?: computeExpensiveDefault()
```

### orElseThrow() - Throw on Absence

Use when absence is exceptional and should be an error.

#### Java 21

```java
UserPreference pref = findUserPreference(userId)
    .orElseThrow(() -> 
        new NoSuchElementException("User not found: " + userId));
```

#### Scala 3

```scala
val pref = findUserPreference(userId)
  .getOrElse(throw new NoSuchElementException(s"User not found: $userId"))
```

#### Kotlin

```kotlin
val pref = findUserPreference(userId)
    ?: throw NoSuchElementException("User not found: $userId")
```

## Transformation: map(), flatMap(), filter()

### map() - Transform Value

Use when transformation returns a non-Optional value.

#### Java 21

```java
// Transform theme to uppercase if present
Optional<String> uppercase = findUserPreference(userId)
    .map(UserPreference::theme)
    .map(String::toUpperCase);
```

#### Scala 3

```scala
val uppercase = findUserPreference(userId)
  .flatMap(_.theme)
  .map(_.toUpperCase)
```

#### Kotlin

```kotlin
// Safe call operator (?.) is equivalent to map
val uppercase = findUserPreference(userId)?.theme?.uppercase()
```

### flatMap() - Avoid Nested Optionals

Use when transformation returns an Optional.

#### Java 21

```java
// validateTheme returns Optional<String>
Optional<String> validTheme = findUserPreference(userId)
    .map(UserPreference::theme)
    .flatMap(this::validateTheme);

// Without flatMap, you'd get Optional<Optional<String>>!
```

#### Scala 3

```scala
// flatMap prevents Option[Option[T]]
val validTheme = findUserPreference(userId)
  .flatMap(_.theme)
  .flatMap(validateTheme)
```

#### Kotlin

```kotlin
// Use let for flatMap-like behavior
val validTheme = findUserPreference(userId)
    ?.theme
    ?.let { validateTheme(it) }
```

### filter() - Conditional Processing

Keep value only if predicate matches.

#### Java 21

```java
// Only keep font sizes >= 14
Optional<Integer> largeFontSize = findUserPreference(userId)
    .map(UserPreference::fontSize)
    .filter(size -> size >= 14);
```

#### Scala 3

```scala
val largeFontSize = findUserPreference(userId)
  .flatMap(_.fontSize)
  .filter(_ >= 14)
```

#### Kotlin

```kotlin
// takeIf is equivalent to filter
val largeFontSize = findUserPreference(userId)
    ?.fontSize
    ?.takeIf { it >= 14 }
```

## Java 9+ Enhancements

### ifPresentOrElse() - Handle Both Cases

```java
findUserPreference(userId)
    .map(UserPreference::theme)
    .ifPresentOrElse(
        theme -> System.out.println("User theme: " + theme),
        () -> System.out.println("Using default theme")
    );
```

#### Scala 3 Equivalent

```scala
// Pattern matching handles both cases elegantly
findUserPreference(userId).flatMap(_.theme) match
  case Some(theme) => println(s"User theme: $theme")
  case None        => println("Using default theme")

// Or using fold
findUserPreference(userId)
  .flatMap(_.theme)
  .fold(println("Using default theme"))(t => println(s"User theme: $t"))
```

#### Kotlin Equivalent

```kotlin
// When expression with nullable
when (val theme = findUserPreference(userId)?.theme) {
    null -> println("Using default theme")
    else -> println("User theme: $theme")
}
```

### or() - Alternative Optional Source

Provide a fallback Optional when the first is empty.

```java
String theme = findUserPreference(userId)
    .map(UserPreference::theme)
    .or(() -> getFallbackTheme())    // Returns Optional<String>
    .orElse("light");
```

#### Scala 3 Equivalent

```scala
val theme = findUserPreference(userId)
  .flatMap(_.theme)
  .orElse(getFallbackTheme)
  .getOrElse("light")
```

#### Kotlin Equivalent

```kotlin
val theme = findUserPreference(userId)?.theme
    ?: getFallbackTheme()
    ?: "light"
```

## Refactoring Nested Null Checks

### Before: Nested Null Checks

```java
// Verbose and error-prone
String getProcessedTheme(String userId) {
    UserPreference user = database.get(userId);
    if (user != null) {
        String theme = user.theme();
        if (theme != null) {
            if (isValidTheme(theme)) {
                return theme.toUpperCase();
            }
        }
    }
    return "LIGHT";
}
```

### After: Fluent Optional Chain

```java
String getProcessedTheme(String userId) {
    return findUserPreference(userId)
        .map(UserPreference::theme)
        .filter(this::isValidTheme)
        .map(String::toUpperCase)
        .orElse("LIGHT");
}
```

### Scala 3 For-Comprehension

```scala
def getDisplaySettings(userId: String): Option[String] =
  for
    pref <- findUserPreference(userId)
    theme <- pref.theme
    fontSize <- pref.fontSize
  yield s"$theme theme, ${fontSize}px font"
```

### Kotlin Scope Functions

```kotlin
fun getDisplaySettings(userId: String): String? =
    findUserPreference(userId)?.run {
        theme?.let { t ->
            fontSize?.let { s ->
                "$t theme, ${s}px font"
            }
        }
    }
```

## Complete Example: Preference Resolution

Here's a complete example showing preference resolution with fallbacks:

### Java 21

```java
public ResolvedPreferences resolvePreferences(String userId) {
    Optional<UserPreference> userPref = findUserPreference(userId);
    
    return new ResolvedPreferences(
        userId,
        userPref.map(UserPreference::theme).orElse(DEFAULT_THEME),
        userPref.map(UserPreference::language).orElse(DEFAULT_LANGUAGE),
        userPref.map(UserPreference::fontSize).orElse(DEFAULT_FONT_SIZE),
        userPref.map(UserPreference::notificationsEnabled)
            .orElse(DEFAULT_NOTIFICATIONS)
    );
}
```

### Scala 3

```scala
def resolvePreferences(userId: String): ResolvedPreferences =
  val userPref = findUserPreference(userId)
  
  ResolvedPreferences(
    userId = userId,
    theme = userPref.flatMap(_.theme).getOrElse(DefaultTheme),
    language = userPref.flatMap(_.language).getOrElse(DefaultLanguage),
    fontSize = userPref.flatMap(_.fontSize).getOrElse(DefaultFontSize),
    notificationsEnabled = 
      userPref.flatMap(_.notificationsEnabled).getOrElse(DefaultNotifications)
  )
```

### Kotlin

```kotlin
fun resolvePreferences(userId: String): ResolvedPreferences {
    val userPref = findUserPreference(userId)
    
    return ResolvedPreferences(
        userId = userId,
        theme = userPref?.theme ?: DEFAULT_THEME,
        language = userPref?.language ?: DEFAULT_LANGUAGE,
        fontSize = userPref?.fontSize ?: DEFAULT_FONT_SIZE,
        notificationsEnabled = userPref?.notificationsEnabled ?: DEFAULT_NOTIFICATIONS
    )
}
```

## Anti-patterns to Avoid

### 1. Using isPresent() with get()

```java
// DON'T DO THIS - defeats the purpose of Optional
Optional<String> opt = getTheme();
if (opt.isPresent()) {
    return opt.get();
}
return "default";

// DO THIS INSTEAD
return getTheme().orElse("default");
```

### 2. Optional as Method Parameter

```java
// DON'T DO THIS - forces callers to create Optional
void setTheme(Optional<String> theme)

// DO THIS INSTEAD - use @Nullable annotation
void setTheme(@Nullable String theme)
```

### 3. Returning null from Optional-returning Method

```java
// DON'T DO THIS
Optional<String> getTheme() {
    if (condition) return null;  // BAD!
    return Optional.of("dark");
}

// DO THIS INSTEAD
Optional<String> getTheme() {
    if (condition) return Optional.empty();
    return Optional.of("dark");
}
```

### 4. Optional for Collection Fields

```java
// DON'T DO THIS
Optional<List<String>> getItems()

// DO THIS INSTEAD - return empty collection
List<String> getItems()
```

### 5. Kotlin: Excessive !! (Not-Null Assertion)

```kotlin
// DON'T DO THIS - throws NPE if null
val theme = preference?.theme!!

// DO THIS INSTEAD
val theme = preference?.theme ?: "default"
```

## Language Comparison

| Operation | Java Optional | Scala Option | Kotlin |
|-----------|--------------|--------------|--------|
| Wrap nullable | `Optional.ofNullable(x)` | `Option(x)` | `x` (nullable type) |
| Create present | `Optional.of(x)` | `Some(x)` | `x` (non-null) |
| Create empty | `Optional.empty()` | `None` | `null` |
| Default value | `orElse(default)` | `getOrElse(default)` | `?: default` |
| Lazy default | `orElseGet(() -> ...)` | `getOrElse(...)` (lazy) | `?: ...` (lazy) |
| Throw if empty | `orElseThrow(...)` | `getOrElse(throw ...)` | `?: throw ...` |
| Transform | `map(f)` | `map(f)` | `?.let { f(it) }` |
| Flatten | `flatMap(f)` | `flatMap(f)` | `?.let { f(it) }` |
| Filter | `filter(p)` | `filter(p)` | `?.takeIf { p(it) }` |
| Handle both | `ifPresentOrElse(f, g)` | `match/fold` | `when` expression |
| Fallback source | `or(() -> ...)` | `orElse(...)` | `?: ... ?: ...` |

## Best Practices Summary

1. **Prefer Optional for return types**, not for fields or parameters
2. **Use orElseGet() over orElse()** for expensive default computations
3. **Chain operations** instead of nesting null checks
4. **Never return null** from Optional-returning methods
5. **Consider flatMap()** when your transformation returns Optional
6. **Use filter()** for conditional processing
7. **Leverage Java 9+ features** like ifPresentOrElse() and or()

## Code Samples

See the complete implementations in our repository:

- [Java 21 UserPreference.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/optional/UserPreference.java)
- [Java 21 UserPreferenceService.java](https://github.com/sps23/java-for-scala-devs/blob/main/java21/src/main/java/io/github/sps23/interview/preparation/optional/UserPreferenceService.java)
- [Scala 3 UserPreference.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/optional/UserPreference.scala)
- [Scala 3 UserPreferenceService.scala](https://github.com/sps23/java-for-scala-devs/blob/main/scala3/src/main/scala/io/github/sps23/interview/preparation/optional/UserPreferenceService.scala)
- [Kotlin UserPreference.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/optional/UserPreference.kt)
- [Kotlin UserPreferenceService.kt](https://github.com/sps23/java-for-scala-devs/blob/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/optional/UserPreferenceService.kt)

## Conclusion

Modern Java's Optional API provides a robust way to handle nullable values, though it's more verbose than Scala's Option or Kotlin's built-in null-safety. Key takeaways:

- **Java 21**: Use Optional with fluent API chains; leverage Java 9+ additions like `ifPresentOrElse()` and `or()`
- **Scala 3**: Option is deeply integrated with for-comprehensions and pattern matching
- **Kotlin**: Built-in null-safety with `?.`, `?:`, and scope functions eliminates the need for a wrapper type

For Scala developers, Java's Optional will feel familiar but more verbose. The good news is that modern Java (9+) has significantly improved the Optional API, making null-safe code more idiomatic.

---

*This is Part 4 of our Java 21 Interview Preparation series. Check out [Part 1: Immutable Data with Java Records](/interview/2025/11/26/immutable-data-with-java-records.html), [Part 2: Sealed Classes and Exhaustive Pattern Matching](/interview/2025/11/28/sealed-classes-and-exhaustive-pattern-matching.html), [Part 3: Collection Factory Methods and Stream Basics](/interview/2025/11/29/collection-factory-methods-and-stream-basics.html), and the [full preparation plan](/interview/2025/11/25/java21-interview-preparation-plan.html).*
