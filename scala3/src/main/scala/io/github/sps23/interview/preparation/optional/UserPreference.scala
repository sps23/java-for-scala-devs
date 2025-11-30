package io.github.sps23.interview.preparation.optional

/** An immutable UserPreference case class representing user settings.
  *
  * This case class is used to demonstrate Option API best practices for handling potentially
  * missing values in Scala 3.
  *
  * @param userId
  *   unique user identifier
  * @param theme
  *   UI theme preference (e.g., "dark", "light"), may be None
  * @param language
  *   preferred language code (e.g., "en", "fr"), may be None
  * @param fontSize
  *   preferred font size, may be None
  * @param notificationsEnabled
  *   whether notifications are enabled, may be None
  */
case class UserPreference(
    userId: String,
    theme: Option[String]                 = None,
    language: Option[String]              = None,
    fontSize: Option[Int]                 = None,
    notificationsEnabled: Option[Boolean] = None
):
  require(userId != null && userId.nonEmpty, "User ID cannot be null or empty")

object UserPreference:
  /** Creates a UserPreference with only the user ID (all preferences None). */
  def withDefaults(userId: String): UserPreference = UserPreference(userId)
