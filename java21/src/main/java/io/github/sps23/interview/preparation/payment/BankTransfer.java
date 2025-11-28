package io.github.sps23.interview.preparation.payment;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a bank transfer payment method.
 *
 * <p>
 * This record demonstrates a different payment type with its own specific
 * attributes (IBAN and bank code) while still being part of the sealed
 * {@link PaymentMethod} hierarchy.
 *
 * @param iban
 *            the International Bank Account Number
 * @param bankCode
 *            the bank's identification code (e.g., BIC/SWIFT)
 * @param amount
 *            the transfer amount
 */
public record BankTransfer(String iban, String bankCode,
        BigDecimal amount) implements PaymentMethod {

    /**
     * Compact constructor for validation.
     */
    public BankTransfer {
        Objects.requireNonNull(iban, "IBAN cannot be null");
        if (iban.length() < 15 || iban.length() > 34) {
            throw new IllegalArgumentException("IBAN must be between 15 and 34 characters");
        }

        Objects.requireNonNull(bankCode, "Bank code cannot be null");
        if (bankCode.isBlank()) {
            throw new IllegalArgumentException("Bank code cannot be blank");
        }

        Objects.requireNonNull(amount, "Amount cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    /**
     * Returns a masked IBAN showing only the last 4 characters.
     *
     * @return masked IBAN (e.g., "****1234")
     */
    public String maskedIban() {
        return "****" + iban.substring(iban.length() - 4);
    }
}
