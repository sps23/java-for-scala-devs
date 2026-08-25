package io.github.sps23.designpatterns.adapter

import java.util.Locale

sealed interface PaymentGateway {
    fun charge(request: PaymentRequest): PaymentResult
}

data class PaymentRequest(
    val customerId: String,
    val amountInCents: Int,
    val currency: String,
)

data class PaymentResult(
    val approved: Boolean,
    val transactionId: String,
    val message: String,
)

data class LegacyBankResponse(
    val statusCode: String,
    val reference: String,
    val detail: String,
)

class LegacyBankApi {
    fun submitPayment(
        clientCode: String,
        minorUnits: Long,
        isoCurrency: String,
    ): LegacyBankResponse =
        when {
            minorUnits <= 0 -> LegacyBankResponse("12", "N/A", "Amount must be greater than zero")
            isoCurrency !in setOf("USD", "EUR", "GBP") ->
                LegacyBankResponse("14", "N/A", "Unsupported currency: $isoCurrency")
            else -> LegacyBankResponse("00", "TX-${clientCode.uppercase()}-$minorUnits", "Approved")
        }
}

/**
 * Adapter pattern in Kotlin.
 *
 * Wraps a legacy banking API and exposes a modern payment-gateway interface for
 * checkout services.
 */
class LegacyBankPaymentAdapter(
    private val legacyBankApi: LegacyBankApi,
) : PaymentGateway {
    override fun charge(request: PaymentRequest): PaymentResult {
        require(request.customerId.isNotBlank()) { "Customer id must not be blank" }
        require(request.amountInCents > 0) { "Amount must be greater than zero" }

        val currency = normalizeCurrency(request.currency)
        val response =
            legacyBankApi.submitPayment(
                clientCode = request.customerId.trim(),
                minorUnits = request.amountInCents.toLong(),
                isoCurrency = currency,
            )

        val approved = response.statusCode == "00"
        return PaymentResult(
            approved = approved,
            transactionId = response.reference,
            message = if (approved) "Payment approved" else response.detail,
        )
    }

    private fun normalizeCurrency(currency: String): String {
        require(currency.isNotBlank()) { "Currency must not be blank" }
        return currency.trim().uppercase(Locale.ROOT)
    }
}
