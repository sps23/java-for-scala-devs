package io.github.sps23.designpatterns.strategy

import java.math.BigDecimal
import java.math.RoundingMode

enum class PaymentMethod {
    CARD,
    BANK_TRANSFER,
    DIGITAL_WALLET,
    BUY_NOW_PAY_LATER,
}

data class PaymentRequest(
    val paymentMethod: PaymentMethod,
    val amount: BigDecimal,
    val currency: String,
    val vipCustomer: Boolean,
) {
    init {
        require(amount > BigDecimal.ZERO) { "Amount must be greater than zero" }
        require(currency.isNotBlank()) { "Currency must not be blank" }
    }
}

data class FeeQuote(
    val paymentMethod: PaymentMethod,
    val baseAmount: BigDecimal,
    val fee: BigDecimal,
    val totalAmount: BigDecimal,
)

fun interface PaymentFeeStrategy {
    fun calculateFee(request: PaymentRequest): BigDecimal

    fun withMinimumFee(minimumFee: BigDecimal): PaymentFeeStrategy =
        PaymentFeeStrategy { request -> calculateFee(request).max(minimumFee).scaled() }

    fun withCap(maximumFee: BigDecimal): PaymentFeeStrategy =
        PaymentFeeStrategy { request -> calculateFee(request).min(maximumFee).scaled() }
}

class PaymentFeeService(
    private val strategies: Map<PaymentMethod, PaymentFeeStrategy>,
) {
    fun quote(request: PaymentRequest): FeeQuote {
        val strategy =
            strategies[request.paymentMethod]
                ?: throw IllegalArgumentException(
                    "No strategy configured for payment method: ${request.paymentMethod}",
                )

        val baseAmount = request.amount.scaled()
        val fee = strategy.calculateFee(request).scaled()
        return FeeQuote(
            paymentMethod = request.paymentMethod,
            baseAmount = baseAmount,
            fee = fee,
            totalAmount = baseAmount.add(fee).scaled(),
        )
    }

    companion object {
        fun defaultService(): PaymentFeeService {
            val card =
                PaymentFeeStrategy { request ->
                    request.amount.multiply(money("0.029")).add(money("0.30")).scaled()
                }
            val bankTransfer =
                PaymentFeeStrategy { request ->
                    request.amount.multiply(money("0.008")).scaled()
                }
            val digitalWallet =
                PaymentFeeStrategy { request ->
                    val baseFee = request.amount.multiply(money("0.017"))
                    val discounted = if (request.vipCustomer) baseFee.multiply(money("0.50")) else baseFee
                    discounted.scaled()
                }

            return PaymentFeeService(
                mapOf(
                    PaymentMethod.CARD to card,
                    PaymentMethod.BANK_TRANSFER to bankTransfer.withCap(money("7.50")),
                    PaymentMethod.DIGITAL_WALLET to digitalWallet.withMinimumFee(money("0.25")),
                ),
            )
        }

        private fun money(value: String): BigDecimal = BigDecimal(value)
    }
}

private fun BigDecimal.scaled(): BigDecimal = setScale(2, RoundingMode.HALF_UP)
