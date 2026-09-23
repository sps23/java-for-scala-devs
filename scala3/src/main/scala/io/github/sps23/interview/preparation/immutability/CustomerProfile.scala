package io.github.sps23.interview.preparation.immutability

case class CustomerProfile private (
    id: Long,
    email: String,
    roles: List[String],
    preferences: Map[String, String]
):
  def withRole(role: String): CustomerProfile =
    val normalizedRole = Option(role).map(_.trim).getOrElse("")
    require(normalizedRole.nonEmpty, "role cannot be blank")

    if roles.contains(normalizedRole) then this
    else copy(roles = roles :+ normalizedRole)

object CustomerProfile:
  def create(
      id: Long,
      email: String,
      roles: scala.collection.Seq[String],
      preferences: scala.collection.Map[String, String]
  ): CustomerProfile =
    require(id > 0, "id must be positive")

    val normalizedEmail = Option(email).map(_.trim).getOrElse("")
    require(normalizedEmail.contains("@"), "email must contain '@'")

    val immutableRoles = roles.toList
    require(immutableRoles.nonEmpty, "roles cannot be empty")
    immutableRoles.foreach(role => require(role.trim.nonEmpty, "role cannot be blank"))

    val immutablePreferences = preferences.toMap
    immutablePreferences.foreach { (key, value) =>
      require(key.trim.nonEmpty, "preference key cannot be blank")
      require(value.trim.nonEmpty, "preference value cannot be blank")
    }

    CustomerProfile(id, normalizedEmail, immutableRoles, immutablePreferences)
