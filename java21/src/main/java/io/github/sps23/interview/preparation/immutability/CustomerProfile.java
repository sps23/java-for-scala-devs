package io.github.sps23.interview.preparation.immutability;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable customer profile for demonstrating Java 21 immutability patterns.
 */
public record CustomerProfile(long id, String email, List<String> roles,
        Map<String, String> preferences) {

    public CustomerProfile {
        if (id <= 0) {
            throw new IllegalArgumentException("id must be positive");
        }
        Objects.requireNonNull(email, "email cannot be null");
        if (!email.contains("@")) {
            throw new IllegalArgumentException("email must contain '@'");
        }
        Objects.requireNonNull(roles, "roles cannot be null");
        Objects.requireNonNull(preferences, "preferences cannot be null");

        roles.forEach(role -> {
            if (role == null || role.isBlank()) {
                throw new IllegalArgumentException("role cannot be blank");
            }
        });

        preferences.forEach((key, value) -> {
            if (key == null || key.isBlank() || value == null || value.isBlank()) {
                throw new IllegalArgumentException("preference keys and values must be non-blank");
            }
        });

        roles = List.copyOf(roles);
        preferences = Map.copyOf(preferences);
    }

    public CustomerProfile withRole(String role) {
        if (role == null || role.isBlank()) {
            throw new IllegalArgumentException("role cannot be blank");
        }
        if (roles.contains(role)) {
            return this;
        }

        var updatedRoles = new ArrayList<>(roles);
        updatedRoles.add(role);
        return new CustomerProfile(id, email, updatedRoles, new LinkedHashMap<>(preferences));
    }
}
