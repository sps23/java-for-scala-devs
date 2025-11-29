package io.github.sps23.interview.preparation.collections;

import java.time.LocalDate;
import java.util.Objects;

/**
 * An immutable Transaction record demonstrating Java Records (Java 16+).
 *
 * <p>
 * This record represents a financial transaction with:
 * <ul>
 * <li>Unique transaction ID</li>
 * <li>Category for grouping (e.g., "Food", "Transport", "Entertainment")</li>
 * <li>Amount in the local currency</li>
 * <li>Description of the transaction</li>
 * <li>Date of the transaction</li>
 * </ul>
 *
 * @param id
 *            unique transaction identifier, must be positive
 * @param category
 *            transaction category for grouping, must not be null or blank
 * @param amount
 *            transaction amount, must be positive
 * @param description
 *            transaction description, must not be null
 * @param date
 *            transaction date, must not be null
 */
public record Transaction(long id, String category, double amount, String description,
        LocalDate date) {

    /**
     * Compact constructor for validation.
     *
     * <p>
     * Validates all fields to ensure data integrity. In a compact constructor,
     * parameter assignments to record components happen automatically at the end.
     */
    public Transaction {
        // Validate id
        if (id <= 0) {
            throw new IllegalArgumentException("Transaction ID must be positive, got: " + id);
        }

        // Validate category
        Objects.requireNonNull(category, "Category cannot be null");
        if (category.isBlank()) {
            throw new IllegalArgumentException("Category cannot be blank");
        }

        // Validate amount
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive, got: " + amount);
        }

        // Validate description
        Objects.requireNonNull(description, "Description cannot be null");

        // Validate date
        Objects.requireNonNull(date, "Date cannot be null");
    }

    /**
     * Static factory method for creating transactions.
     *
     * @param id
     *            transaction ID
     * @param category
     *            transaction category
     * @param amount
     *            transaction amount
     * @param description
     *            transaction description
     * @param date
     *            transaction date
     * @return new Transaction instance
     */
    public static Transaction of(long id, String category, double amount, String description,
            LocalDate date) {
        return new Transaction(id, category, amount, description, date);
    }

    /**
     * Creates a Transaction with today's date.
     *
     * @param id
     *            transaction ID
     * @param category
     *            transaction category
     * @param amount
     *            transaction amount
     * @param description
     *            transaction description
     * @return new Transaction instance with current date
     */
    public static Transaction today(long id, String category, double amount, String description) {
        return new Transaction(id, category, amount, description, LocalDate.now());
    }

    /**
     * Returns a formatted string representation of the transaction.
     *
     * @return formatted transaction string
     */
    public String toFormattedString() {
        return String.format("[%d] %s: $%.2f - %s (%s)", id, category, amount, description, date);
    }
}
