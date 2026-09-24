package io.github.sps23.interview.preparation.immutability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.HashMap;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("CustomerProfile Java 21 Tests")
class CustomerProfileTest {

    @Test
    @DisplayName("Should defensively copy mutable constructor inputs")
    void shouldDefensivelyCopyMutableInputs() {
        var roles = new ArrayList<String>();
        roles.add("user");
        var preferences = new HashMap<String, String>();
        preferences.put("tier", "standard");

        var profile = new CustomerProfile(1L, "alex@example.com", roles, preferences);
        roles.add("admin");
        preferences.put("region", "eu");

        assertEquals(1, profile.roles().size());
        assertEquals(1, profile.preferences().size());
    }

    @Test
    @DisplayName("Should expose unmodifiable collections")
    void shouldExposeUnmodifiableCollections() {
        var profile = new CustomerProfile(1L, "alex@example.com", java.util.List.of("user"),
                java.util.Map.of("tier", "standard"));

        assertThrows(UnsupportedOperationException.class, () -> profile.roles().add("admin"));
        assertThrows(UnsupportedOperationException.class, () -> profile.preferences().put("region", "eu"));
    }

    @Test
    @DisplayName("Should create a new instance when adding a role")
    void shouldCreateNewInstanceWhenAddingRole() {
        var original = new CustomerProfile(1L, "alex@example.com", java.util.List.of("user"),
                java.util.Map.of("tier", "standard"));

        var updated = original.withRole("admin");

        assertNotSame(original, updated);
        assertEquals(java.util.List.of("user"), original.roles());
        assertEquals(java.util.List.of("user", "admin"), updated.roles());
    }

    @Test
    @DisplayName("Should reject invalid email")
    void shouldRejectInvalidEmail() {
        var error = assertThrows(IllegalArgumentException.class, () -> new CustomerProfile(1L, "invalid-email",
                java.util.List.of("user"), java.util.Map.of("tier", "standard")));

        assertEquals("email must contain '@'", error.getMessage());
    }
}
