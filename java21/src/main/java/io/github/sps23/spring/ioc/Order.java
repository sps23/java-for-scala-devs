package io.github.sps23.spring.ioc;

import java.math.BigDecimal;

/**
 * Immutable order domain model.
 *
 * <p>
 * A plain Java 21 record — no Spring annotations needed. The business model
 * stays framework-agnostic; Spring only wires the services that use it.
 */
public record Order(String id, String customerId, BigDecimal amount, String customerEmail) {

    public Order {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Order ID cannot be null or blank");
        }
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be null or blank");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Order amount must be positive");
        }
        if (customerEmail == null || !customerEmail.contains("@")) {
            throw new IllegalArgumentException("Customer email must be valid");
        }
    }
}
