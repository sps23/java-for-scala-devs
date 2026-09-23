package io.github.sps23.interview.preparation.immutability

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("CustomerProfile Kotlin Tests")
class CustomerProfileTest {
    @Test
    @DisplayName("Should defensively copy mutable constructor inputs")
    fun shouldDefensivelyCopyMutableInputs() {
        val roles = mutableListOf("user")
        val preferences = mutableMapOf("tier" to "standard")

        val profile = CustomerProfile.create(1L, "alex@example.com", roles, preferences)
        roles.add("admin")
        preferences["region"] = "eu"

        assertEquals(listOf("user"), profile.roles)
        assertEquals(mapOf("tier" to "standard"), profile.preferences)
    }

    @Test
    @DisplayName("Should expose unmodifiable collections")
    fun shouldExposeUnmodifiableCollections() {
        val profile =
            CustomerProfile.create(
                1L,
                "alex@example.com",
                listOf("user"),
                mapOf("tier" to "standard"),
            )

        @Suppress("UNCHECKED_CAST")
        val roles = profile.roles as MutableList<String>

        @Suppress("UNCHECKED_CAST")
        val preferences = profile.preferences as MutableMap<String, String>

        assertThrows(UnsupportedOperationException::class.java) {
            roles.add("admin")
        }
        assertThrows(UnsupportedOperationException::class.java) {
            preferences["region"] = "eu"
        }
    }

    @Test
    @DisplayName("Should create a new instance when adding a role")
    fun shouldCreateNewInstanceWhenAddingRole() {
        val original =
            CustomerProfile.create(
                1L,
                "alex@example.com",
                listOf("user"),
                mapOf("tier" to "standard"),
            )

        val updated = original.withRole("admin")

        assertNotSame(original, updated)
        assertEquals(listOf("user"), original.roles)
        assertEquals(listOf("user", "admin"), updated.roles)
    }

    @Test
    @DisplayName("Should reject invalid email")
    fun shouldRejectInvalidEmail() {
        val error =
            assertThrows(IllegalArgumentException::class.java) {
                CustomerProfile.create(
                    1L,
                    "invalid-email",
                    listOf("user"),
                    mapOf("tier" to "standard"),
                )
            }

        assertEquals("email must contain '@'", error.message)
    }
}
