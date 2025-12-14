package io.github.sps23.testing.unittesting;

/**
 * Immutable user domain model using Java record.
 */
public record User(String id, String name, String email, boolean active) {

    public User {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("User name cannot be null or blank");
        }
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("Invalid email address");
        }
    }

    public User withActive(boolean active) {
        return new User(id, name, email, active);
    }
}
