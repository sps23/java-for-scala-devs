package io.github.sps23.interview.preparation.payment

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.YearMonth

/**
 * Calculates transaction fees for different payment methods.
 *
 * This object demonstrates exhaustive `when` expressions with sealed interfaces in Kotlin.
 * Because `PaymentMethod` is sealed and all implementations are in the same package,
 * the compiler verifies that all cases are handled - no `else` branch is needed.
 *
 * Fee structure:
 * - Credit Card: 2.9% + $0.30 per transaction
 * - Bank Transfer: flat $2.50 for amounts under $1000, $5.00 otherwise
 * - Digital Wallet: 2.5% with $0.50 minimum fee
 */
object FeeCalculator {
    private val CREDIT_CARD_PERCENTAGE = BigDecimal("0.029")
    private val CREDIT_CARD_FIXED_FEE = BigDecimal("0.30")
    private val BANK_TRANSFER_LOW_FEE = BigDecimal("2.50")
    private val BANK_TRANSFER_HIGH_FEE = BigDecimal("5.00")
    private val BANK_TRANSFER_THRESHOLD = BigDecimal("1000")
    private val DIGITAL_WALLET_PERCENTAGE = BigDecimal("0.025")
    private val DIGITAL_WALLET_MIN_FEE = BigDecimal("0.50")

    /**
     * Calculates the transaction fee for a payment method.
     *
     * Uses exhaustive `when` expression with smart casting. The compiler verifies that
     * all permitted subtypes of `PaymentMethod` are handled.
     *
     * @param payment the payment method to calculate fee for
     * @return the calculated fee amount, rounded to 2 decimal places
     */
    fun calculateFee(payment: PaymentMethod): BigDecimal =
        when (payment) {
            is CreditCard -> calculateCreditCardFee(payment.amount)
            is BankTransfer -> calculateBankTransferFee(payment.amount)
            is DigitalWallet -> calculateDigitalWalletFee(payment.amount)
        }

    /**
     * Calculates fee using `when` expression with additional conditions.
     *
     * Demonstrates conditional matching within `when` branches.
     * This is similar to Java's `when` clause and Scala's pattern guards.
     *
     * @param payment the payment method
     * @return a description of the fee calculation
     */
    fun describeFee(payment: PaymentMethod): String =
        when {
            payment is CreditCard && payment.amount > BigDecimal("100") ->
                "Credit card (high value): 2.9% + \$0.30 on ${payment.cardNumber.takeLast(4)}"
            payment is CreditCard ->
                "Credit card (standard): 2.9% + \$0.30 on ${payment.cardNumber.takeLast(4)}"
            payment is BankTransfer && payment.amount >= BANK_TRANSFER_THRESHOLD ->
                "Bank transfer (high value): flat \$5.00 to ${payment.bankCode}"
            payment is BankTransfer ->
                "Bank transfer (standard): flat \$2.50 to ${payment.bankCode}"
            payment is DigitalWallet ->
                "Digital wallet (${payment.provider}): 2.5% (min \$0.50)"
            else -> throw IllegalStateException("Unknown payment type")
        }

    /**
     * Demonstrates simple type-based `when` expression.
     *
     * @param payment the payment method
     * @return the payment type name
     */
    fun getPaymentTypeName(payment: PaymentMethod): String =
        when (payment) {
            is CreditCard -> "Credit Card"
            is BankTransfer -> "Bank Transfer"
            is DigitalWallet -> "Digital Wallet"
        }

    private fun calculateCreditCardFee(amount: BigDecimal): BigDecimal =
        (amount * CREDIT_CARD_PERCENTAGE + CREDIT_CARD_FIXED_FEE).setScale(2, RoundingMode.HALF_UP)

    private fun calculateBankTransferFee(amount: BigDecimal): BigDecimal =
        if (amount < BANK_TRANSFER_THRESHOLD) BANK_TRANSFER_LOW_FEE else BANK_TRANSFER_HIGH_FEE

    private fun calculateDigitalWalletFee(amount: BigDecimal): BigDecimal =
        (amount * DIGITAL_WALLET_PERCENTAGE).max(DIGITAL_WALLET_MIN_FEE).setScale(2, RoundingMode.HALF_UP)
}

/**
 * Example usage of the payment system.
 */
fun main() {
    val creditCard =
        CreditCard(
            cardNumber = "1234567890123456",
            expiryDate = YearMonth.now().plusYears(2),
            amount = BigDecimal("100"),
        )
    val bankTransfer =
        BankTransfer(
            iban = "DE89370400440532013000",
            bankCode = "COBADEFFXXX",
            amount = BigDecimal("500"),
        )
    val digitalWallet =
        DigitalWallet(
            provider = "PayPal",
            accountId = "user@example.com",
            amount = BigDecimal("50"),
        )

    val payments = listOf(creditCard, bankTransfer, digitalWallet)

    payments.forEach { payment ->
        println("Payment: ${FeeCalculator.getPaymentTypeName(payment)}")
        println("  Amount: \$${payment.amount}")
        println("  Fee: \$${FeeCalculator.calculateFee(payment)}")
        println("  Description: ${FeeCalculator.describeFee(payment)}")
        println()
    }
}
