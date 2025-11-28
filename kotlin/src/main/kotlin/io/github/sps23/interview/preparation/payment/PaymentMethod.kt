package io.github.sps23.interview.preparation.payment

import java.math.BigDecimal
import java.time.YearMonth

/**
 * A sealed interface representing different payment methods in a payment system.
 *
 * In Kotlin, sealed interfaces (and classes) restrict implementations to the same
 * package and module, enabling exhaustive `when` expressions without requiring an
 * `else` branch.
 *
 * Key concepts demonstrated:
 * - `sealed interface` - restricts implementations to same compilation unit
 * - Exhaustive `when` expressions - compiler verifies all cases are covered
 * - Data classes implementing sealed interfaces - similar to Java's records with sealed
 */
sealed interface PaymentMethod {
    val amount: BigDecimal
}

/**
 * Represents a credit card payment method.
 *
 * @property cardNumber the 16-digit card number
 * @property expiryDate the card expiry date
 * @property amount the transaction amount
 */
data class CreditCard(
    val cardNumber: String,
    val expiryDate: YearMonth,
    override val amount: BigDecimal,
) : PaymentMethod {
    init {
        require(cardNumber.length == 16 && cardNumber.all { it.isDigit() }) {
            "Card number must be 16 digits"
        }
        require(!expiryDate.isBefore(YearMonth.now())) { "Card has expired" }
        require(amount > BigDecimal.ZERO) { "Amount must be positive" }
    }

    /**
     * Returns a masked card number showing only the last 4 digits.
     */
    fun maskedCardNumber(): String = "****-****-****-${cardNumber.takeLast(4)}"
}

/**
 * Represents a bank transfer payment method.
 *
 * @property iban the International Bank Account Number
 * @property bankCode the bank's identification code (e.g., BIC/SWIFT)
 * @property amount the transfer amount
 */
data class BankTransfer(
    val iban: String,
    val bankCode: String,
    override val amount: BigDecimal,
) : PaymentMethod {
    init {
        require(iban.length in 15..34) { "IBAN must be between 15 and 34 characters" }
        require(bankCode.isNotBlank()) { "Bank code cannot be blank" }
        require(amount > BigDecimal.ZERO) { "Amount must be positive" }
    }

    /**
     * Returns a masked IBAN showing only the last 4 characters.
     */
    fun maskedIban(): String = "****${iban.takeLast(4)}"
}

/**
 * Represents a digital wallet payment method (e.g., PayPal, Apple Pay, Google Pay).
 *
 * @property provider the wallet provider name
 * @property accountId the user's account identifier
 * @property amount the payment amount
 */
data class DigitalWallet(
    val provider: String,
    val accountId: String,
    override val amount: BigDecimal,
) : PaymentMethod {
    init {
        require(provider.isNotBlank()) { "Provider cannot be blank" }
        require(accountId.isNotBlank()) { "Account ID cannot be blank" }
        require(amount > BigDecimal.ZERO) { "Amount must be positive" }
    }

    /**
     * Returns a masked account ID for display purposes.
     */
    fun maskedAccountId(): String {
        val atIndex = accountId.indexOf('@')
        return if (atIndex > 0) {
            "${accountId.take(minOf(4, atIndex))}***${accountId.drop(atIndex)}"
        } else {
            "${accountId.take(4)}***"
        }
    }
}
