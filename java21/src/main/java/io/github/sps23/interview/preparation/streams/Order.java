package io.github.sps23.interview.preparation.streams;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * An immutable Order record demonstrating Java Records (Java 16+).
 *
 * <p>
 * This record represents an order with:
 * <ul>
 * <li>Unique order ID</li>
 * <li>Category for grouping (e.g., "Electronics", "Clothing", "Books")</li>
 * <li>Total amount of the order</li>
 * <li>Customer name</li>
 * <li>Order date</li>
 * <li>List of items in the order</li>
 * </ul>
 *
 * @param id
 *            unique order identifier, must be positive
 * @param category
 *            order category for grouping, must not be null or blank
 * @param amount
 *            order total amount, must be non-negative
 * @param customer
 *            customer name, must not be null or blank
 * @param date
 *            order date, must not be null
 * @param items
 *            list of items in the order, must not be null
 */
public record Order(long id, String category, double amount, String customer, LocalDate date,
        List<String> items) {

    /**
     * Compact constructor for validation.
     *
     * <p>
     * Validates all fields to ensure data integrity. In a compact constructor,
     * parameter assignments to record components happen automatically at the end.
     */
    public Order {
        // Validate id
        if (id <= 0) {
            throw new IllegalArgumentException("Order ID must be positive, got: " + id);
        }

        // Validate category
        Objects.requireNonNull(category, "Category cannot be null");
        if (category.isBlank()) {
            throw new IllegalArgumentException("Category cannot be blank");
        }

        // Validate amount
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative, got: " + amount);
        }

        // Validate customer
        Objects.requireNonNull(customer, "Customer cannot be null");
        if (customer.isBlank()) {
            throw new IllegalArgumentException("Customer cannot be blank");
        }

        // Validate date
        Objects.requireNonNull(date, "Date cannot be null");

        // Validate items and make defensive copy
        Objects.requireNonNull(items, "Items cannot be null");
        items = List.copyOf(items); // Defensive copy to ensure immutability
    }

    /**
     * Static factory method for creating orders.
     *
     * @param id
     *            order ID
     * @param category
     *            order category
     * @param amount
     *            order amount
     * @param customer
     *            customer name
     * @param date
     *            order date
     * @param items
     *            list of items
     * @return new Order instance
     */
    public static Order of(long id, String category, double amount, String customer, LocalDate date,
            List<String> items) {
        return new Order(id, category, amount, customer, date, items);
    }

    /**
     * Creates an Order with today's date.
     *
     * @param id
     *            order ID
     * @param category
     *            order category
     * @param amount
     *            order amount
     * @param customer
     *            customer name
     * @param items
     *            list of items
     * @return new Order instance with current date
     */
    public static Order today(long id, String category, double amount, String customer,
            List<String> items) {
        return new Order(id, category, amount, customer, LocalDate.now(), items);
    }

    /**
     * Returns the number of items in this order.
     *
     * @return number of items
     */
    public int itemCount() {
        return items.size();
    }

    /**
     * Returns a formatted string representation of the order.
     *
     * @return formatted order string
     */
    public String toFormattedString() {
        return String.format("[%d] %s: $%.2f by %s (%s) - %d items", id, category, amount, customer,
                date, itemCount());
    }
}
