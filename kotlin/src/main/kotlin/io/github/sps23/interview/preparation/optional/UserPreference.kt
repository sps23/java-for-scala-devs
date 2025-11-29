package io.github.sps23.interview.preparation.optional

/**
 * An immutable UserPreference data class representing user settings.
 *
 * This data class demonstrates Kotlin's null-safety features as an alternative
 * to Optional API.
 *
 * @property userId unique user identifier (non-null)
 * @property theme UI theme preference (e.g., "dark", "light"), may be null
 * @property language preferred language code (e.g., "en", "fr"), may be null
 * @property fontSize preferred font size, may be null
 * @property notificationsEnabled whether notifications are enabled, may be null
 */
data class UserPreference(
    val userId: String,
    val theme: String? = null,
    val language: String? = null,
    val fontSize: Int? = null,
    val notificationsEnabled: Boolean? = null,
) {
    init {
        require(userId.isNotBlank()) { "User ID cannot be blank" }
    }

    companion object {
        /**
         * Creates a UserPreference with only the user ID (all preferences null).
         */
        fun withDefaults(userId: String): UserPreference = UserPreference(userId)
    }
}
