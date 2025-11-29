package io.github.sps23.interview.preparation.optional

/** Demonstrates Option API and best practices for null-safe programming in Scala 3.
  *
  * This service fetches user preferences with fallback defaults, avoiding null checks. It
  * showcases:
  *   - Option creation: Some(), None, Option()
  *   - Extraction: getOrElse(), fold(), orElse()
  *   - Transformation: map(), flatMap(), filter(), collect()
  *   - Pattern matching with Option
  *   - for-comprehensions with Option
  *   - Anti-patterns to avoid
  *
  * Problem Statement: Implement a service that fetches user preferences with fallback defaults,
  * avoiding null checks.
  */
class UserPreferenceService:

  // Simulated database of user preferences
  private val database: Map[String, UserPreference] = Map(
    "user1" -> UserPreference(
      "user1",
      theme                = Some("dark"),
      language             = Some("en"),
      fontSize             = Some(16),
      notificationsEnabled = Some(true)
    ),
    "user2" -> UserPreference(
      "user2",
      theme                = None,
      language             = Some("fr"),
      fontSize             = None,
      notificationsEnabled = Some(false)
    ),
    "user3" -> UserPreference("user3") // All preferences None
  )

  // Default preferences
  private val DefaultTheme         = "light"
  private val DefaultLanguage      = "en"
  private val DefaultFontSize      = 14
  private val DefaultNotifications = true

  // ========================================================================
  // Basic Option Creation and Extraction
  // ========================================================================

  /** Finds a user preference by ID.
    *
    * Map.get returns Option[V], which is idiomatic Scala for potentially missing values.
    */
  def findUserPreference(userId: String): Option[UserPreference] =
    database.get(userId)

  /** Demonstrates getOrElse() - provides a default value.
    *
    * Use when the default value is already computed or cheap to create.
    */
  def getThemeWithGetOrElse(userId: String): String =
    findUserPreference(userId)
      .flatMap(_.theme)
      .getOrElse(DefaultTheme)

  /** Demonstrates fold() - handles both Some and None cases.
    *
    * fold is like getOrElse + map combined: handles empty case and transforms value.
    */
  def getThemeWithFold(userId: String): String =
    findUserPreference(userId)
      .flatMap(_.theme)
      .fold(DefaultTheme)(_.toUpperCase)

  /** Demonstrates orElse() - provides alternative Option.
    *
    * Use when you have a chain of fallback sources.
    */
  def getThemeWithOrElse(userId: String): String =
    findUserPreference(userId)
      .flatMap(_.theme)
      .orElse(getFallbackTheme)
      .getOrElse(DefaultTheme)

  // ========================================================================
  // Transformation Methods: map(), flatMap(), filter(), collect()
  // ========================================================================

  /** Demonstrates map() - transforms the value inside Option.
    *
    * Use when the transformation function returns a non-Option value.
    */
  def getUppercaseTheme(userId: String): Option[String] =
    findUserPreference(userId)
      .flatMap(_.theme)
      .map(_.toUpperCase)

  /** Demonstrates flatMap() - transforms when function returns Option.
    *
    * Use when the transformation function itself returns an Option.
    */
  def getValidatedTheme(userId: String): Option[String] =
    findUserPreference(userId)
      .flatMap(_.theme)
      .flatMap(validateTheme)

  /** Demonstrates filter() - keeps value only if predicate matches. */
  def getLargeFontSize(userId: String): Option[Int] =
    findUserPreference(userId)
      .flatMap(_.fontSize)
      .filter(_ >= 14)

  /** Demonstrates collect() - combines filter and map in one operation.
    *
    * Uses partial function to both filter and transform.
    */
  def getThemedFontDescription(userId: String): Option[String] =
    findUserPreference(userId)
      .flatMap(_.theme)
      .collect {
        case "dark"  => "Dark theme with reduced blue light"
        case "light" => "Light theme for daytime use"
      }

  // ========================================================================
  // Pattern Matching with Option
  // ========================================================================

  /** Demonstrates pattern matching - idiomatic Scala for Option handling.
    *
    * Pattern matching is particularly useful for complex conditional logic.
    */
  def describeTheme(userId: String): String =
    findUserPreference(userId).flatMap(_.theme) match
      case Some("dark")  => "Using dark theme for reduced eye strain"
      case Some("light") => "Using light theme for better visibility"
      case Some(custom)  => s"Using custom theme: $custom"
      case None          => s"Using default theme: $DefaultTheme"

  // ========================================================================
  // For-Comprehensions with Option
  // ========================================================================

  /** Demonstrates for-comprehension - elegant chaining of Option operations.
    *
    * For-comprehensions desugar to flatMap/map chains but are more readable.
    */
  def getDisplaySettings(userId: String): Option[String] =
    for
      pref     <- findUserPreference(userId)
      theme    <- pref.theme
      fontSize <- pref.fontSize
    yield s"$theme theme, ${fontSize}px font"

  /** Demonstrates for-comprehension with guards (filters). */
  def getValidatedDisplaySettings(userId: String): Option[String] =
    for
      pref     <- findUserPreference(userId)
      theme    <- pref.theme if isValidTheme(theme)
      fontSize <- pref.fontSize if fontSize >= 10 && fontSize <= 32
    yield s"$theme theme, ${fontSize}px font"

  // ========================================================================
  // Complete Preference Resolution with Fallbacks
  // ========================================================================

  /** Resolves all preferences with proper fallbacks.
    *
    * This method demonstrates the complete pattern for handling optional values with cascading
    * defaults.
    */
  def resolvePreferences(userId: String): ResolvedPreferences =
    val userPref = findUserPreference(userId)

    ResolvedPreferences(
      userId   = userId,
      theme    = userPref.flatMap(_.theme).getOrElse(DefaultTheme),
      language = userPref.flatMap(_.language).getOrElse(DefaultLanguage),
      fontSize = userPref.flatMap(_.fontSize).getOrElse(DefaultFontSize),
      notificationsEnabled =
        userPref.flatMap(_.notificationsEnabled).getOrElse(DefaultNotifications)
    )

  // ========================================================================
  // Option vs Null: The Scala Way
  // ========================================================================

  /** Demonstrates converting nullable Java values to Option.
    *
    * When interoperating with Java code that returns null, use Option() to wrap.
    */
  def fromNullableJavaValue(value: String): Option[String] =
    Option(value) // Returns None if value is null, Some(value) otherwise

  /** Demonstrates explicit Some/None creation. */
  def explicitOptionCreation(): Unit =
    val present: Option[String]   = Some("value")
    val absent: Option[String]    = None
    val maybeNull: Option[String] = Option(null) // Results in None

    println(s"Some: $present, None: $absent, Option(null): $maybeNull")

  // ========================================================================
  // Anti-patterns to Avoid
  // ========================================================================

  /** ANTI-PATTERN: Using .get on Option.
    *
    * .get throws NoSuchElementException on None. Avoid in production code.
    *
    * Instead of:
    * {{{
    * val theme = opt.get // Dangerous!
    * }}}
    *
    * Use:
    * {{{
    * val theme = opt.getOrElse("default")
    * // or
    * opt match { case Some(t) => ... case None => ... }
    * }}}
    */
  def antiPatternGet(userId: String): String =
    // DON'T DO THIS in production code!
    val theme = findUserPreference(userId).flatMap(_.theme)
    if theme.isDefined then theme.get // Anti-pattern!
    else DefaultTheme

  /** ANTI-PATTERN: Using null instead of Option.
    *
    * In idiomatic Scala, avoid null entirely:
    * {{{
    * // DON'T DO THIS
    * def getTheme(): String = null
    *
    * // DO THIS INSTEAD
    * def getTheme(): Option[String] = None
    * }}}
    */

  // ========================================================================
  // Helper Methods
  // ========================================================================

  private def validateTheme(theme: String): Option[String] =
    if theme == "dark" || theme == "light" then Some(theme)
    else None

  private def isValidTheme(theme: String): Boolean =
    theme == "dark" || theme == "light"

  private def getFallbackTheme: Option[String] =
    println("Fetching fallback theme...")
    Some("system")

/** Record for fully resolved preferences (no Option values). */
case class ResolvedPreferences(
    userId: String,
    theme: String,
    language: String,
    fontSize: Int,
    notificationsEnabled: Boolean
):
  override def toString: String =
    s"Preferences[user=$userId, theme=$theme, lang=$language, font=${fontSize}px, notify=$notificationsEnabled]"

object UserPreferenceService:

  /** Main method demonstrating all Option API features. */
  @main def runUserPreferenceDemo(): Unit =
    val service = new UserPreferenceService()

    println("=== Scala 3 Option API Demo ===\n")

    // Demo 1: Basic Option operations
    println("--- Basic Option Operations ---\n")

    println("User1 (has all preferences):")
    println(s"  Theme with getOrElse: ${service.getThemeWithGetOrElse("user1")}")
    println(s"  Theme with fold: ${service.getThemeWithFold("user1")}")

    println("\nUser2 (missing theme and fontSize):")
    println(s"  Theme with getOrElse: ${service.getThemeWithGetOrElse("user2")}")
    println(s"  Theme with fold: ${service.getThemeWithFold("user2")}")

    println("\nUnknown user:")
    println(s"  Theme with getOrElse: ${service.getThemeWithGetOrElse("unknown")}")

    // Demo 2: Transformation methods
    println("\n--- Transformation Methods ---\n")
    println(s"Uppercase theme (user1): ${service.getUppercaseTheme("user1")}")
    println(s"Validated theme (user1): ${service.getValidatedTheme("user1")}")
    println(s"Large font size (user1): ${service.getLargeFontSize("user1")}")
    println(s"Large font size (user2): ${service.getLargeFontSize("user2")}")
    println(s"Themed description (user1): ${service.getThemedFontDescription("user1")}")

    // Demo 3: Pattern matching
    println("\n--- Pattern Matching ---\n")
    println(s"Theme description (user1): ${service.describeTheme("user1")}")
    println(s"Theme description (user2): ${service.describeTheme("user2")}")
    println(s"Theme description (unknown): ${service.describeTheme("unknown")}")

    // Demo 4: For-comprehensions
    println("\n--- For-Comprehensions ---\n")
    println(s"Display settings (user1): ${service.getDisplaySettings("user1")}")
    println(s"Display settings (user2): ${service.getDisplaySettings("user2")}")
    println(s"Validated settings (user1): ${service.getValidatedDisplaySettings("user1")}")

    // Demo 5: orElse fallback chain
    println("\n--- orElse Fallback Chain ---\n")
    println(s"Theme with orElse (user1): ${service.getThemeWithOrElse("user1")}")
    println(s"Theme with orElse (user3): ${service.getThemeWithOrElse("user3")}")

    // Demo 6: Complete resolution
    println("\n--- Complete Preference Resolution ---\n")
    println(s"user1: ${service.resolvePreferences("user1")}")
    println(s"user2: ${service.resolvePreferences("user2")}")
    println(s"user3: ${service.resolvePreferences("user3")}")
    println(s"unknown: ${service.resolvePreferences("unknown")}")

    // Demo 7: Option creation
    println("\n--- Option Creation ---\n")
    service.explicitOptionCreation()
    println(s"From nullable (non-null): ${service.fromNullableJavaValue("value")}")
    println(s"From nullable (null): ${service.fromNullableJavaValue(null)}")
