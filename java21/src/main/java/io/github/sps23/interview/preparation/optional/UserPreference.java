package io.github.sps23.interview.preparation.optional;

import java.util.Objects;

/**
 * An immutable UserPreference record representing user settings.
 *
 * <p>
 * This record is used to demonstrate Optional API best practices for handling
 * potentially missing values.
 *
 * @param userId
 *            unique user identifier
 * @param theme
 *            UI theme preference (e.g., "dark", "light"), may be null
 * @param language
 *            preferred language code (e.g., "en", "fr"), may be null
 * @param fontSize
 *            preferred font size, may be null
 * @param notificationsEnabled
 *            whether notifications are enabled, may be null
 */
public record UserPreference(String userId, String theme, String language, Integer fontSize,
        Boolean notificationsEnabled) {

    /**
     * Compact constructor for validation.
     */
    public UserPreference {
        Objects.requireNonNull(userId, "User ID cannot be null");
        if (userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be blank");
        }
    }

    /**
     * Creates a UserPreference with only the user ID (all preferences null).
     *
     * @param userId
     *            the user ID
     * @return new UserPreference with default null values
     */
    public static UserPreference withDefaults(String userId) {
        return new UserPreference(userId, null, null, null, null);
    }

    /**
     * Creates a builder for fluent UserPreference creation.
     *
     * @param userId
     *            the user ID
     * @return new Builder instance
     */
    public static Builder builder(String userId) {
        return new Builder(userId);
    }

    /**
     * Builder for creating UserPreference instances fluently.
     */
    public static final class Builder {

        private final String userId;
        private String theme;
        private String language;
        private Integer fontSize;
        private Boolean notificationsEnabled;

        private Builder(String userId) {
            this.userId = userId;
        }

        public Builder theme(String theme) {
            this.theme = theme;
            return this;
        }

        public Builder language(String language) {
            this.language = language;
            return this;
        }

        public Builder fontSize(Integer fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public Builder notificationsEnabled(Boolean notificationsEnabled) {
            this.notificationsEnabled = notificationsEnabled;
            return this;
        }

        public UserPreference build() {
            return new UserPreference(userId, theme, language, fontSize, notificationsEnabled);
        }
    }
}
