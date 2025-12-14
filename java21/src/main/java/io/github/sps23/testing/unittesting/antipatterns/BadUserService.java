package io.github.sps23.testing.unittesting.antipatterns;

import io.github.sps23.testing.unittesting.User;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

/**
 * ANTI-PATTERN: Service with hard-coded dependencies and tight coupling. This
 * design makes unit testing very difficult because: 1. Cannot mock database
 * connections (requires real database) 2. Cannot mock email sending (will send
 * real emails) 3. Uses static methods and new keyword for dependencies 4.
 * Violates Single Responsibility Principle
 */
public class BadUserService {

    // ANTI-PATTERN: Hard-coded database connection string
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/mydb";
    private static final String DB_USER = "admin";
    private static final String DB_PASSWORD = "password";

    public User createUser(String id, String name, String email) {
        // ANTI-PATTERN: Creating connection inside business logic
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            // ANTI-PATTERN: SQL logic mixed with business logic
            String sql = "INSERT INTO users (id, name, email, active) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id);
                stmt.setString(2, name);
                stmt.setString(3, email);
                stmt.setBoolean(4, true);
                stmt.executeUpdate();
            }

            // ANTI-PATTERN: Hard-coded email sending (can't be mocked)
            sendEmailDirectly(email, "Welcome!", "Welcome to our service!");

            return new User(id, name, email, true);

        } catch (Exception e) {
            throw new RuntimeException("Failed to create user", e);
        }
    }

    public Optional<User> getUserById(String id) {
        // ANTI-PATTERN: Duplicate connection code
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {

            String sql = "SELECT * FROM users WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new User(rs.getString("id"), rs.getString("name"),
                                rs.getString("email"), rs.getBoolean("active")));
                    }
                }
            }
            return Optional.empty();

        } catch (Exception e) {
            throw new RuntimeException("Failed to get user", e);
        }
    }

    // ANTI-PATTERN: Private method that makes real external calls
    private void sendEmailDirectly(String to, String subject, String body) {
        // This would actually send an email via SMTP
        System.out.println("Sending email to: " + to);
        // ... SMTP logic here ...
    }
}
