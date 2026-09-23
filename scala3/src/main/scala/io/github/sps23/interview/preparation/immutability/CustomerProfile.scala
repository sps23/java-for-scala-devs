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

    val immutableRoles = roles.toList.map(role => Option(role).map(_.trim).getOrElse(""))
    require(immutableRoles.nonEmpty, "roles cannot be empty")
    immutableRoles.foreach(role => require(role.nonEmpty, "role cannot be blank"))

    val immutablePreferences = preferences.map { (key, value) =>
      val normalizedKey   = Option(key).map(_.trim).getOrElse("")
      val normalizedValue = Option(value).map(_.trim).getOrElse("")
      require(normalizedKey.nonEmpty, "preference key cannot be blank")
      require(normalizedValue.nonEmpty, "preference value cannot be blank")
      normalizedKey -> normalizedValue
    }.toMap

    CustomerProfile(id, normalizedEmail, immutableRoles, immutablePreferences)
