package io.github.sps23.interview.preparation.payment;

import java.math.BigDecimal;

/**
 * A sealed interface representing different payment methods in a payment
 * system.
 *
 * <p>
 * Sealed classes (Java 17+) restrict which classes can implement or extend
 * them, enabling exhaustive pattern matching in switch expressions without
 * requiring a default case.
 *
 * <p>
 * Key concepts demonstrated:
 * <ul>
 * <li>{@code sealed} - declares that only permitted subtypes can
 * extend/implement</li>
 * <li>{@code permits} - explicitly lists the allowed subtypes</li>
 * <li>Subtypes must be {@code final}, {@code sealed}, or
 * {@code non-sealed}</li>
 * </ul>
 *
 * <p>
 * This is similar to Scala's sealed traits and Kotlin's sealed classes, which
 * also enable the compiler to verify exhaustive pattern matching.
 *
 * @see CreditCard
 * @see BankTransfer
 * @see DigitalWallet
 */
public sealed interface PaymentMethod permits CreditCard, BankTransfer, DigitalWallet {

    /**
     * Returns the transaction amount for this payment.
     *
     * @return the payment amount
     */
    BigDecimal amount();
}
