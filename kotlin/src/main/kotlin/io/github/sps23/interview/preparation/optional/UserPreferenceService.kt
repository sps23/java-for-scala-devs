package io.github.sps23.interview.preparation.optional

/**
 * Demonstrates Kotlin's null-safety features as an alternative to Optional API.
 *
 * This service fetches user preferences with fallback defaults using Kotlin's
 * built-in null-safety operators:
 * - Safe call operator: ?.
 * - Elvis operator: ?:
 * - Safe cast: as?
 * - Not-null assertion: !! (use sparingly)
 * - let, run, also, apply scope functions
 * - takeIf, takeUnless
 *
 * Problem Statement: Implement a service that fetches user preferences with
 * fallback defaults, avoiding null checks.
 */
class UserPreferenceService {
    // Simulated database of user preferences
    private val database: Map<String, UserPreference> =
        mapOf(
            "user1" to
                UserPreference(
                    userId = "user1",
                    theme = "dark",
                    language = "en",
                    fontSize = 16,
                    notificationsEnabled = true,
                ),
            "user2" to
                UserPreference(
                    userId = "user2",
                    theme = null,
                    language = "fr",
                    fontSize = null,
                    notificationsEnabled = false,
                ),
            // All preferences null
            "user3" to UserPreference(userId = "user3"),
        )

    // Default preferences
    companion object {
        private const val DEFAULT_THEME = "light"
        private const val DEFAULT_LANGUAGE = "en"
        private const val DEFAULT_FONT_SIZE = 14
        private const val DEFAULT_NOTIFICATIONS = true
    }

    // ========================================================================
    // Basic Null-Safe Access
    // ========================================================================

    /**
     * Finds a user preference by ID.
     *
     * Map's get() returns nullable type in Kotlin.
     */
    fun findUserPreference(userId: String): UserPreference? = database[userId]

    /**
     * Demonstrates Elvis operator (?:) - provides default value.
     *
     * The Elvis operator returns the left operand if not null, otherwise the right operand.
     */
    fun getThemeWithElvis(userId: String): String = findUserPreference(userId)?.theme ?: DEFAULT_THEME

    /**
     * Demonstrates safe call chain (?.) - safe navigation through nulls.
     *
     * Multiple ?. operators can be chained for deep null-safe access.
     */
    fun getUppercaseTheme(userId: String): String? = findUserPreference(userId)?.theme?.uppercase()

    /**
     * Demonstrates safe call with Elvis for complete null safety.
     */
    fun getUppercaseThemeOrDefault(userId: String): String = findUserPreference(userId)?.theme?.uppercase() ?: DEFAULT_THEME.uppercase()

    // ========================================================================
    // Scope Functions: let, run, also, apply
    // ========================================================================

    /**
     * Demonstrates let - executes block only if not null.
     *
     * let is the Kotlin equivalent of Java's Optional.map() + ifPresent().
     */
    fun processThemeWithLet(userId: String): String? =
        findUserPreference(userId)?.theme?.let { theme ->
            println("Processing theme: $theme")
            theme.uppercase()
        }

    /**
     * Demonstrates let with Elvis for transformation with default.
     */
    fun getProcessedTheme(userId: String): String = findUserPreference(userId)?.theme?.let { it.uppercase() } ?: DEFAULT_THEME.uppercase()

    /**
     * Demonstrates run - similar to let but uses 'this' instead of 'it'.
     */
    fun getThemeDescriptionWithRun(userId: String): String =
        findUserPreference(userId)?.theme?.run {
            "Theme: ${uppercase()}, length: $length"
        } ?: "Using default theme: $DEFAULT_THEME"

    /**
     * Demonstrates also - for side effects without transforming.
     */
    fun getThemeWithLogging(userId: String): String? =
        findUserPreference(userId)?.theme?.also { theme ->
            println("Retrieved theme: $theme for user: $userId")
        }

    // ========================================================================
    // Filtering: takeIf, takeUnless
    // ========================================================================

    /**
     * Demonstrates takeIf - returns value only if predicate is true.
     *
     * Similar to Optional.filter() in Java.
     */
    fun getValidatedTheme(userId: String): String? = findUserPreference(userId)?.theme?.takeIf { isValidTheme(it) }

    /**
     * Demonstrates takeUnless - returns value only if predicate is false.
     *
     * Inverse of takeIf.
     */
    fun getCustomTheme(userId: String): String? = findUserPreference(userId)?.theme?.takeUnless { it == "light" || it == "dark" }

    /**
     * Demonstrates takeIf for filtering nullable integers.
     */
    fun getLargeFontSize(userId: String): Int? = findUserPreference(userId)?.fontSize?.takeIf { it >= 14 }

    // ========================================================================
    // When Expression with Nullable Types
    // ========================================================================

    /**
     * Demonstrates when expression - Kotlin's pattern matching for nullables.
     */
    fun describeTheme(userId: String): String =
        when (val theme = findUserPreference(userId)?.theme) {
            "dark" -> "Using dark theme for reduced eye strain"
            "light" -> "Using light theme for better visibility"
            null -> "Using default theme: $DEFAULT_THEME"
            else -> "Using custom theme: $theme"
        }

    // ========================================================================
    // Chaining with Multiple Nullable Values
    // ========================================================================

    /**
     * Demonstrates combining multiple nullable values with let.
     */
    fun getDisplaySettings(userId: String): String? {
        val pref = findUserPreference(userId) ?: return null
        val theme = pref.theme ?: return null
        val fontSize = pref.fontSize ?: return null
        return "$theme theme, ${fontSize}px font"
    }

    /**
     * Demonstrates alternative approach using run for chaining.
     */
    fun getDisplaySettingsWithRun(userId: String): String? =
        findUserPreference(userId)?.run {
            theme?.let { t ->
                fontSize?.let { s ->
                    "$t theme, ${s}px font"
                }
            }
        }

    // ========================================================================
    // Complete Preference Resolution with Fallbacks
    // ========================================================================

    /**
     * Resolves all preferences with proper fallbacks.
     *
     * This method demonstrates the complete pattern for handling nullable values
     * with cascading defaults using Elvis operator.
     */
    fun resolvePreferences(userId: String): ResolvedPreferences {
        val userPref = findUserPreference(userId)

        return ResolvedPreferences(
            userId = userId,
            theme = userPref?.theme ?: DEFAULT_THEME,
            language = userPref?.language ?: DEFAULT_LANGUAGE,
            fontSize = userPref?.fontSize ?: DEFAULT_FONT_SIZE,
            notificationsEnabled = userPref?.notificationsEnabled ?: DEFAULT_NOTIFICATIONS,
        )
    }

    // ========================================================================
    // Kotlin vs Java Optional Comparison
    // ========================================================================

    /**
     * Demonstrates Kotlin null-safety vs Java Optional equivalents.
     *
     * Java Optional:
     * ```java
     * Optional.ofNullable(value)
     *     .map(String::toUpperCase)
     *     .filter(s -> s.length() > 3)
     *     .orElse("default")
     * ```
     *
     * Kotlin equivalent:
     * ```kotlin
     * value
     *     ?.uppercase()
     *     ?.takeIf { it.length > 3 }
     *     ?: "default"
     * ```
     */
    fun demonstrateKotlinVsJava(value: String?): String =
        value
            ?.uppercase()
            ?.takeIf { it.length > 3 }
            ?: "default"

    // ========================================================================
    // Safe Casts
    // ========================================================================

    /**
     * Demonstrates safe cast (as?) for type-safe casting.
     *
     * Returns null instead of throwing ClassCastException.
     */
    fun safeCastExample(any: Any?): String? = (any as? String)?.uppercase()

    // ========================================================================
    // Anti-patterns to Avoid
    // ========================================================================

    /**
     * ANTI-PATTERN: Using !! (not-null assertion) without validation.
     *
     * !! throws NullPointerException if value is null. Only use when you're
     * absolutely certain the value is not null.
     *
     * Instead of:
     * ```kotlin
     * val theme = preference?.theme!! // Dangerous!
     * ```
     *
     * Use:
     * ```kotlin
     * val theme = preference?.theme ?: "default"
     * // or
     * val theme = preference?.theme ?: throw IllegalStateException("Theme required")
     * ```
     */
    @Suppress("UNNECESSARY_NOT_NULL_ASSERTION")
    fun antiPatternNotNullAssertion(userId: String): String {
        // DON'T DO THIS in production code!
        val pref = findUserPreference(userId)
        return if (pref?.theme != null) {
            pref.theme!! // Anti-pattern! Use ?: instead
        } else {
            DEFAULT_THEME
        }
    }

    /**
     * ANTI-PATTERN: Excessive null checks instead of using scope functions.
     *
     * Instead of:
     * ```kotlin
     * if (value != null) {
     *     if (value.property != null) {
     *         process(value.property)
     *     }
     * }
     * ```
     *
     * Use:
     * ```kotlin
     * value?.property?.let { process(it) }
     * ```
     */
    fun antiPatternNestedNullChecks(userId: String): String {
        // DON'T DO THIS in production code!
        val pref = findUserPreference(userId)
        if (pref != null) {
            if (pref.theme != null) {
                return pref.theme.uppercase()
            }
        }
        return DEFAULT_THEME
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private fun isValidTheme(theme: String): Boolean = theme == "dark" || theme == "light"
}

/**
 * Data class for fully resolved preferences (no nullable values).
 */
data class ResolvedPreferences(
    val userId: String,
    val theme: String,
    val language: String,
    val fontSize: Int,
    val notificationsEnabled: Boolean,
) {
    override fun toString(): String =
        "Preferences[user=$userId, theme=$theme, lang=$language, font=${fontSize}px, notify=$notificationsEnabled]"
}

/**
 * Main function demonstrating all Kotlin null-safety features.
 */
fun main() {
    val service = UserPreferenceService()

    println("=== Kotlin Null-Safety Demo ===\n")

    // Demo 1: Basic null-safe operations
    println("--- Basic Null-Safe Operations ---\n")

    println("User1 (has all preferences):")
    println("  Theme with Elvis: ${service.getThemeWithElvis("user1")}")
    println("  Uppercase theme: ${service.getUppercaseTheme("user1")}")

    println("\nUser2 (missing theme and fontSize):")
    println("  Theme with Elvis: ${service.getThemeWithElvis("user2")}")
    println("  Uppercase theme: ${service.getUppercaseTheme("user2")}")

    println("\nUnknown user:")
    println("  Theme with Elvis: ${service.getThemeWithElvis("unknown")}")

    // Demo 2: Scope functions
    println("\n--- Scope Functions (let, run) ---\n")
    println("Processed theme (user1): ${service.getProcessedTheme("user1")}")
    println("Processed theme (user2): ${service.getProcessedTheme("user2")}")
    println("Theme description (user1): ${service.getThemeDescriptionWithRun("user1")}")

    // Demo 3: takeIf / takeUnless
    println("\n--- takeIf / takeUnless ---\n")
    println("Validated theme (user1): ${service.getValidatedTheme("user1")}")
    println("Large font size (user1): ${service.getLargeFontSize("user1")}")
    println("Large font size (user2): ${service.getLargeFontSize("user2")}")

    // Demo 4: When expression
    println("\n--- When Expression ---\n")
    println("Theme description (user1): ${service.describeTheme("user1")}")
    println("Theme description (user2): ${service.describeTheme("user2")}")
    println("Theme description (unknown): ${service.describeTheme("unknown")}")

    // Demo 5: Chaining
    println("\n--- Chaining Nullable Values ---\n")
    println("Display settings (user1): ${service.getDisplaySettings("user1")}")
    println("Display settings (user2): ${service.getDisplaySettings("user2")}")

    // Demo 6: Complete resolution
    println("\n--- Complete Preference Resolution ---\n")
    println("user1: ${service.resolvePreferences("user1")}")
    println("user2: ${service.resolvePreferences("user2")}")
    println("user3: ${service.resolvePreferences("user3")}")
    println("unknown: ${service.resolvePreferences("unknown")}")

    // Demo 7: Kotlin vs Java comparison
    println("\n--- Kotlin vs Java Optional ---\n")
    println("demonstrateKotlinVsJava('hello'): ${service.demonstrateKotlinVsJava("hello")}")
    println("demonstrateKotlinVsJava('hi'): ${service.demonstrateKotlinVsJava("hi")}")
    println("demonstrateKotlinVsJava(null): ${service.demonstrateKotlinVsJava(null)}")

    // Demo 8: Safe cast
    println("\n--- Safe Cast ---\n")
    println("safeCastExample('hello'): ${service.safeCastExample("hello")}")
    println("safeCastExample(123): ${service.safeCastExample(123)}")
    println("safeCastExample(null): ${service.safeCastExample(null)}")
}
