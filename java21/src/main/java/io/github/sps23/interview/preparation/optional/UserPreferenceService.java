package io.github.sps23.interview.preparation.optional;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Demonstrates Optional API and best practices for null-safe programming.
 *
 * <p>
 * This service fetches user preferences with fallback defaults, avoiding null
 * checks. It showcases:
 * <ul>
 * <li>{@code Optional.ofNullable()}, {@code orElse()}, {@code orElseGet()},
 * {@code orElseThrow()}</li>
 * <li>{@code Optional.map()}, {@code flatMap()}, {@code filter()}</li>
 * <li>{@code Optional.ifPresentOrElse()} (Java 9+)</li>
 * <li>{@code Optional.or()} (Java 9+)</li>
 * <li>Anti-patterns to avoid</li>
 * </ul>
 *
 * <h2>Problem Statement:</h2>
 * <p>
 * Implement a service that fetches user preferences with fallback defaults,
 * avoiding null checks.
 */
public final class UserPreferenceService {

    // Simulated database of user preferences
    private final Map<String, UserPreference> database = new HashMap<>();

    // Default preferences
    private static final String DEFAULT_THEME = "light";
    private static final String DEFAULT_LANGUAGE = "en";
    private static final int DEFAULT_FONT_SIZE = 14;
    private static final boolean DEFAULT_NOTIFICATIONS = true;

    public UserPreferenceService() {
        // Initialize with some sample data
        database.put("user1", UserPreference.builder("user1").theme("dark").language("en")
                .fontSize(16).notificationsEnabled(true).build());
        database.put("user2", UserPreference.builder("user2").theme(null).language("fr")
                .fontSize(null).notificationsEnabled(false).build());
        database.put("user3", UserPreference.builder("user3").build()); // All preferences null
    }

    // ========================================================================
    // Basic Optional Creation and Extraction
    // ========================================================================

    /**
     * Demonstrates Optional.ofNullable() - wraps a potentially null value.
     *
     * <p>
     * Use this when you receive a value that might be null from external sources.
     *
     * @param userId
     *            the user ID to look up
     * @return Optional containing the user preference, or empty if not found
     */
    public Optional<UserPreference> findUserPreference(String userId) {
        return Optional.ofNullable(database.get(userId));
    }

    /**
     * Demonstrates orElse() - provides a default value.
     *
     * <p>
     * Use when the default value is already computed or cheap to create.
     *
     * @param userId
     *            the user ID
     * @return the theme, or default if not set
     */
    public String getThemeWithOrElse(String userId) {
        return findUserPreference(userId).map(UserPreference::theme).orElse(DEFAULT_THEME);
    }

    /**
     * Demonstrates orElseGet() - provides a default via supplier.
     *
     * <p>
     * Use when the default value is expensive to compute (lazy evaluation).
     *
     * @param userId
     *            the user ID
     * @return the theme, or computed default if not set
     */
    public String getThemeWithOrElseGet(String userId) {
        return findUserPreference(userId).map(UserPreference::theme)
                .orElseGet(this::computeDefaultTheme);
    }

    /**
     * Demonstrates orElseThrow() - throws exception if empty.
     *
     * <p>
     * Use when absence of value is exceptional and should be handled as error.
     *
     * @param userId
     *            the user ID
     * @return the user preference
     * @throws NoSuchElementException
     *             if user not found
     */
    public UserPreference getPreferenceOrThrow(String userId) {
        return findUserPreference(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));
    }

    // ========================================================================
    // Transformation Methods: map(), flatMap(), filter()
    // ========================================================================

    /**
     * Demonstrates map() - transforms the value inside Optional.
     *
     * <p>
     * Use when the transformation function returns a non-Optional value.
     *
     * @param userId
     *            the user ID
     * @return Optional containing uppercase theme, or empty
     */
    public Optional<String> getUppercaseTheme(String userId) {
        return findUserPreference(userId).map(UserPreference::theme).map(String::toUpperCase);
    }

    /**
     * Demonstrates flatMap() - transforms when function returns Optional.
     *
     * <p>
     * Use when the transformation function itself returns an Optional, to avoid
     * nested Optional&lt;Optional&lt;T&gt;&gt;.
     *
     * @param userId
     *            the user ID
     * @return Optional containing the validated theme
     */
    public Optional<String> getValidatedTheme(String userId) {
        return findUserPreference(userId).map(UserPreference::theme).flatMap(this::validateTheme);
    }

    /**
     * Demonstrates filter() - keeps value only if predicate matches.
     *
     * <p>
     * Use when you need to conditionally process values.
     *
     * @param userId
     *            the user ID
     * @return Optional containing font size if it's above minimum
     */
    public Optional<Integer> getLargeFontSize(String userId) {
        return findUserPreference(userId).map(UserPreference::fontSize).filter(size -> size >= 14);
    }

    // ========================================================================
    // Java 9+ Methods: ifPresentOrElse(), or()
    // ========================================================================

    /**
     * Demonstrates ifPresentOrElse() (Java 9+) - handles both cases.
     *
     * <p>
     * Use when you need to perform different actions based on presence/absence.
     *
     * @param userId
     *            the user ID
     */
    public void printThemeWithIfPresentOrElse(String userId) {
        findUserPreference(userId).map(UserPreference::theme).ifPresentOrElse(
                theme -> System.out.println("User theme: " + theme),
                () -> System.out.println("Using default theme: " + DEFAULT_THEME));
    }

    /**
     * Demonstrates or() (Java 9+) - provides alternative Optional.
     *
     * <p>
     * Use when you have a chain of fallback sources, each returning Optional.
     *
     * @param userId
     *            the user ID
     * @return theme from user preference or from fallback source
     */
    public String getThemeWithOr(String userId) {
        return findUserPreference(userId).map(UserPreference::theme).or(this::getFallbackTheme)
                .orElse(DEFAULT_THEME);
    }

    // ========================================================================
    // Chaining Multiple Optional Operations
    // ========================================================================

    /**
     * Demonstrates chaining - refactoring nested null checks.
     *
     * <p>
     * Traditional null-check code:
     *
     * <pre>
     * if (user != null) {
     *     if (user.theme() != null) {
     *         if (isValidTheme(user.theme())) {
     *             return user.theme().toUpperCase();
     *         }
     *     }
     * }
     * return "LIGHT";
     * </pre>
     *
     * <p>
     * Refactored with Optional chain:
     *
     * @param userId
     *            the user ID
     * @return processed theme value
     */
    public String getProcessedTheme(String userId) {
        return findUserPreference(userId).map(UserPreference::theme).filter(this::isValidTheme)
                .map(String::toUpperCase).orElse(DEFAULT_THEME.toUpperCase());
    }

    /**
     * Demonstrates flatMap chaining for dependent operations.
     *
     * @param userId
     *            the user ID
     * @return display settings string combining theme and font size
     */
    public String getDisplaySettings(String userId) {
        return findUserPreference(userId)
                .flatMap(pref -> Optional.ofNullable(pref.theme())
                        .flatMap(theme -> Optional.ofNullable(pref.fontSize())
                                .map(size -> theme + " theme, " + size + "px font")))
                .orElse(DEFAULT_THEME + " theme, " + DEFAULT_FONT_SIZE + "px font");
    }

    // ========================================================================
    // Complete Preference Resolution with Fallbacks
    // ========================================================================

    /**
     * Resolves all preferences with proper fallbacks.
     *
     * <p>
     * This method demonstrates the complete pattern for handling optional values
     * with cascading defaults.
     *
     * @param userId
     *            the user ID
     * @return resolved preferences record with no null values
     */
    public ResolvedPreferences resolvePreferences(String userId) {
        Optional<UserPreference> userPref = findUserPreference(userId);

        String theme = userPref.map(UserPreference::theme).orElse(DEFAULT_THEME);

        String language = userPref.map(UserPreference::language).orElse(DEFAULT_LANGUAGE);

        int fontSize = userPref.map(UserPreference::fontSize).orElse(DEFAULT_FONT_SIZE);

        boolean notifications = userPref.map(UserPreference::notificationsEnabled)
                .orElse(DEFAULT_NOTIFICATIONS);

        return new ResolvedPreferences(userId, theme, language, fontSize, notifications);
    }

    /**
     * Record for fully resolved preferences (no null values).
     *
     * @param userId
     *            user identifier
     * @param theme
     *            resolved theme
     * @param language
     *            resolved language
     * @param fontSize
     *            resolved font size
     * @param notificationsEnabled
     *            resolved notification setting
     */
    public record ResolvedPreferences(String userId, String theme, String language, int fontSize,
            boolean notificationsEnabled) {

        @Override
        public String toString() {
            return String.format("Preferences[user=%s, theme=%s, lang=%s, font=%dpx, notify=%s]",
                    userId, theme, language, fontSize, notificationsEnabled);
        }
    }

    // ========================================================================
    // Anti-patterns to Avoid
    // ========================================================================

    /**
     * ANTI-PATTERN: Using isPresent() with get().
     *
     * <p>
     * This is essentially the same as null checking and defeats the purpose of
     * Optional.
     *
     * <pre>
     * // DON'T DO THIS
     * Optional&lt;String&gt; opt = getTheme();
     * if (opt.isPresent()) {
     *     return opt.get();
     * }
     * return "default";
     *
     * // DO THIS INSTEAD
     * return getTheme().orElse("default");
     * </pre>
     *
     * @param userId
     *            the user ID
     * @return theme value (demonstrating anti-pattern)
     */
    @SuppressWarnings("java:S3655") // Intentional anti-pattern demonstration
    public String antiPatternIsPresentGet(String userId) {
        // DON'T DO THIS in production code!
        Optional<String> theme = findUserPreference(userId).map(UserPreference::theme);
        if (theme.isPresent()) {
            return theme.get();
        }
        return DEFAULT_THEME;
    }

    /**
     * ANTI-PATTERN: Using Optional as method parameter.
     *
     * <p>
     * Optional should be used as return type, not as parameter.
     *
     * <pre>
     * // DON'T DO THIS
     * void setTheme(Optional&lt;String&gt; theme)
     *
     * // DO THIS INSTEAD
     * void setTheme(String theme)  // nullable
     * void setThemeIfPresent(String theme)  // @Nullable annotation
     * </pre>
     *
     * @param theme
     *            optional theme (anti-pattern demonstration)
     */
    public void antiPatternOptionalParameter(Optional<String> theme) {
        // This signature is an anti-pattern
        // Callers are forced to create Optional unnecessarily
        System.out.println("Theme: " + theme.orElse(DEFAULT_THEME));
    }

    /**
     * ANTI-PATTERN: Returning null instead of empty Optional.
     *
     * <p>
     * If a method returns Optional, it should never return null.
     *
     * <pre>
     * // DON'T DO THIS
     * Optional&lt;String&gt; getTheme() {
     *     if (condition)
     *         return null; // BAD!
     *     return Optional.of("dark");
     * }
     *
     * // DO THIS INSTEAD
     * Optional&lt;String&gt; getTheme() {
     *     if (condition)
     *         return Optional.empty();
     *     return Optional.of("dark");
     * }
     * </pre>
     */

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private String computeDefaultTheme() {
        // Simulates expensive computation
        System.out.println("Computing default theme...");
        return DEFAULT_THEME;
    }

    private Optional<String> validateTheme(String theme) {
        if (theme != null && (theme.equals("dark") || theme.equals("light"))) {
            return Optional.of(theme);
        }
        return Optional.empty();
    }

    private boolean isValidTheme(String theme) {
        return theme != null && (theme.equals("dark") || theme.equals("light"));
    }

    private Optional<String> getFallbackTheme() {
        // Simulates fetching from another source
        System.out.println("Fetching fallback theme...");
        return Optional.of("system");
    }

    // ========================================================================
    // Demonstration
    // ========================================================================

    /**
     * Main method demonstrating all Optional API features.
     *
     * @param args
     *            command-line arguments (not used)
     */
    public static void main(String[] args) {
        UserPreferenceService service = new UserPreferenceService();

        System.out.println("=== Optional API Demo ===\n");

        // Demo 1: Basic Optional operations
        System.out.println("--- Basic Optional Operations ---\n");

        System.out.println("User1 (has all preferences):");
        System.out.println("  Theme with orElse: " + service.getThemeWithOrElse("user1"));
        System.out.println("  Theme with orElseGet: " + service.getThemeWithOrElseGet("user1"));

        System.out.println("\nUser2 (missing theme and fontSize):");
        System.out.println("  Theme with orElse: " + service.getThemeWithOrElse("user2"));
        System.out.println("  Theme with orElseGet: " + service.getThemeWithOrElseGet("user2"));

        System.out.println("\nUnknown user:");
        System.out.println("  Theme with orElse: " + service.getThemeWithOrElse("unknown"));

        // Demo 2: orElseThrow
        System.out.println("\n--- orElseThrow() ---\n");
        try {
            service.getPreferenceOrThrow("unknown");
        } catch (NoSuchElementException e) {
            System.out.println("Caught exception: " + e.getMessage());
        }

        // Demo 3: Transformation methods
        System.out.println("\n--- Transformation Methods ---\n");
        System.out.println("Uppercase theme (user1): " + service.getUppercaseTheme("user1"));
        System.out.println("Validated theme (user1): " + service.getValidatedTheme("user1"));
        System.out.println("Large font size (user1): " + service.getLargeFontSize("user1"));
        System.out.println("Large font size (user2): " + service.getLargeFontSize("user2"));

        // Demo 4: Java 9+ features
        System.out.println("\n--- Java 9+ Features ---\n");
        System.out.print("ifPresentOrElse (user1): ");
        service.printThemeWithIfPresentOrElse("user1");
        System.out.print("ifPresentOrElse (user3): ");
        service.printThemeWithIfPresentOrElse("user3");

        System.out.println("\nor() fallback chain:");
        System.out.println("  Theme for user1: " + service.getThemeWithOr("user1"));
        System.out.println("  Theme for user3: " + service.getThemeWithOr("user3"));

        // Demo 5: Chaining
        System.out.println("\n--- Optional Chaining ---\n");
        System.out.println("Processed theme (user1): " + service.getProcessedTheme("user1"));
        System.out.println("Processed theme (user3): " + service.getProcessedTheme("user3"));
        System.out.println("Display settings (user1): " + service.getDisplaySettings("user1"));
        System.out.println("Display settings (user2): " + service.getDisplaySettings("user2"));

        // Demo 6: Complete resolution
        System.out.println("\n--- Complete Preference Resolution ---\n");
        System.out.println("user1: " + service.resolvePreferences("user1"));
        System.out.println("user2: " + service.resolvePreferences("user2"));
        System.out.println("user3: " + service.resolvePreferences("user3"));
        System.out.println("unknown: " + service.resolvePreferences("unknown"));

        // Demo 7: orElse vs orElseGet difference
        System.out.println("\n--- orElse vs orElseGet (Performance Difference) ---\n");
        System.out.println("orElse with user1 (value exists):");
        System.out.println("  Result: " + service.getThemeWithOrElse("user1"));
        System.out.println("\norElseGet with user1 (value exists):");
        System.out.println("  Result: " + service.getThemeWithOrElseGet("user1"));
        System.out.println("\nNote: orElseGet avoids computing default when value exists");
    }
}
