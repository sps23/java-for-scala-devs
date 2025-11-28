package io.github.sps23.interview.preparation.payment;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a digital wallet payment method (e.g., PayPal, Apple Pay, Google
 * Pay).
 *
 * <p>
 * This record demonstrates a third payment type in our sealed hierarchy, with
 * wallet-specific attributes like the provider and account identifier.
 *
 * @param provider
 *            the wallet provider name (e.g., "PayPal", "Apple Pay")
 * @param accountId
 *            the user's account identifier with the provider
 * @param amount
 *            the payment amount
 */
public record DigitalWallet(String provider, String accountId,
        BigDecimal amount) implements PaymentMethod {

    /**
     * Compact constructor for validation.
     */
    public DigitalWallet {
        Objects.requireNonNull(provider, "Provider cannot be null");
        if (provider.isBlank()) {
            throw new IllegalArgumentException("Provider cannot be blank");
        }

        Objects.requireNonNull(accountId, "Account ID cannot be null");
        if (accountId.isBlank()) {
            throw new IllegalArgumentException("Account ID cannot be blank");
        }

        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    /**
     * Returns a masked account ID for display purposes.
     *
     * @return masked account ID (e.g., "user***@example.com")
     */
    public String maskedAccountId() {
        int atIndex = accountId.indexOf('@');
        if (atIndex > 0) {
            return accountId.substring(0, Math.min(4, atIndex)) + "***"
                    + accountId.substring(atIndex);
        }
        return accountId.substring(0, Math.min(4, accountId.length())) + "***";
    }
}
