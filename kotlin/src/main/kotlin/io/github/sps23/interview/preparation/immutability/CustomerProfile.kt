package io.github.sps23.interview.preparation.immutability

import java.util.Collections

data class CustomerProfile private constructor(
    val id: Long,
    val email: String,
    val roles: List<String>,
    val preferences: Map<String, String>,
) {
    fun withRole(role: String): CustomerProfile {
        require(role.isNotBlank()) { "role cannot be blank" }
        if (roles.contains(role)) {
            return this
        }
        return copy(roles = Collections.unmodifiableList(roles + role))
    }

    companion object {
        fun create(
            id: Long,
            email: String,
            roles: List<String>,
            preferences: Map<String, String>,
        ): CustomerProfile {
            require(id > 0) { "id must be positive" }
            require(email.contains("@")) { "email must contain '@'" }
            require(roles.isNotEmpty()) { "roles cannot be empty" }
            require(roles.all { it.isNotBlank() }) { "role cannot be blank" }
            require(preferences.keys.all { it.isNotBlank() }) { "preference key cannot be blank" }
            require(preferences.values.all { it.isNotBlank() }) {
                "preference value cannot be blank"
            }

            return CustomerProfile(
                id = id,
                email = email.trim(),
                roles = Collections.unmodifiableList(roles.toList()),
                preferences = Collections.unmodifiableMap(preferences.toMap()),
            )
        }
    }
}
