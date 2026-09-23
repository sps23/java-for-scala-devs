package io.github.sps23.interview.preparation.immutability

import java.util.Collections

data class CustomerProfile private constructor(
    val id: Long,
    val email: String,
    val roles: List<String>,
    val preferences: Map<String, String>,
) {
    fun withRole(role: String): CustomerProfile {
        val normalizedRole = role.trim()
        require(normalizedRole.isNotBlank()) { "role cannot be blank" }
        if (roles.contains(normalizedRole)) {
            return this
        }
        return copy(roles = Collections.unmodifiableList(roles + normalizedRole))
    }

    companion object {
        fun create(
            id: Long,
            email: String,
            roles: List<String>,
            preferences: Map<String, String>,
        ): CustomerProfile {
            require(id > 0) { "id must be positive" }
            val normalizedEmail = email.trim()
            require(normalizedEmail.contains("@")) { "email must contain '@'" }
            val normalizedRoles = roles.map { it.trim() }
            require(normalizedRoles.isNotEmpty()) { "roles cannot be empty" }
            require(normalizedRoles.all { it.isNotBlank() }) { "role cannot be blank" }
            val normalizedPreferences =
                preferences.map { (key, value) ->
                    key.trim() to value.trim()
                }
            require(normalizedPreferences.all { (key, _) -> key.isNotBlank() }) {
                "preference key cannot be blank"
            }
            require(normalizedPreferences.all { (_, value) -> value.isNotBlank() }) {
                "preference value cannot be blank"
            }

            return CustomerProfile(
                id = id,
                email = normalizedEmail,
                roles = Collections.unmodifiableList(normalizedRoles),
                preferences = Collections.unmodifiableMap(normalizedPreferences.toMap()),
            )
        }
    }
}
