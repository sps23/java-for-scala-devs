package io.github.sps23.interview.preparation.payment;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Objects;

/**
 * Represents a credit card payment method.
 *
 * <p>
 * As a {@code record} implementing a {@code sealed interface}, this class:
 * <ul>
 * <li>Is implicitly {@code final} (records cannot be extended)</li>
 * <li>Provides immutable storage for payment data</li>
 * <li>Enables record pattern destructuring in switch expressions</li>
 * </ul>
 *
 * <p>
 * Example usage with pattern matching:
 *
 * <pre>{@code
 * switch (payment) {
 *     case CreditCard(var num, var exp, var amt) -> "Card ending in " + num.substring(12);
 *     // ... other cases
 * }
 * }</pre>
 *
 * @param cardNumber
 *            the 16-digit card number
 * @param expiryDate
 *            the card expiry date
 * @param amount
 *            the transaction amount
 */
public record CreditCard(String cardNumber, YearMonth expiryDate,
        BigDecimal amount) implements PaymentMethod {

    /**
     * Compact constructor for validation.
     */
    public CreditCard {
        Objects.requireNonNull(cardNumber, "Card number cannot be null");
        if (cardNumber.length() != 16 || !cardNumber.matches("\\d+")) {
            throw new IllegalArgumentException("Card number must be 16 digits");
        }

        Objects.requireNonNull(expiryDate, "Expiry date cannot be null");
        if (expiryDate.isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("Card has expired");
        }

        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    /**
     * Returns a masked card number showing only the last 4 digits.
     *
     * @return masked card number (e.g., "****-****-****-1234")
     */
    public String maskedCardNumber() {
        return "****-****-****-" + cardNumber.substring(12);
    }
}
