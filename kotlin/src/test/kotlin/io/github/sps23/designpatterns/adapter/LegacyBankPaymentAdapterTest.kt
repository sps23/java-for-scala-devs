package io.github.sps23.designpatterns.adapter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Adapter Pattern Kotlin Tests")
class LegacyBankPaymentAdapterTest {
    private val paymentGateway: PaymentGateway = LegacyBankPaymentAdapter(LegacyBankApi())

    @Test
    @DisplayName("Should approve checkout payment through legacy bank adapter")
    fun shouldApproveCheckoutPayment() {
        val checkoutService = CheckoutService(paymentGateway)

        val confirmation = checkoutService.checkout("cust-42", 1599, "eur")

        assertTrue(confirmation.startsWith("CONFIRMED:TX-CUST-42-1599"))
    }

    @Test
    @DisplayName("Should reject unsupported currencies from legacy API")
    fun shouldRejectUnsupportedCurrency() {
        val result = paymentGateway.charge(PaymentRequest("cust-42", 1599, "pln"))

        assertEquals(false, result.approved)
        assertEquals("Unsupported currency: PLN", result.message)
    }

    @Test
    @DisplayName("Should fail fast when amount is not positive")
    fun shouldFailFastOnInvalidAmount() {
        val error =
            assertThrows(IllegalArgumentException::class.java) {
                paymentGateway.charge(PaymentRequest("cust-42", 0, "EUR"))
            }
        assertEquals("Amount must be greater than zero", error.message)
    }
}

class CheckoutService(
    private val paymentGateway: PaymentGateway,
) {
    fun checkout(
        customerId: String,
        amountInCents: Int,
        currency: String,
    ): String {
        val result = paymentGateway.charge(PaymentRequest(customerId, amountInCents, currency))
        return if (result.approved) {
            "CONFIRMED:${result.transactionId}"
        } else {
            "REJECTED:${result.message}"
        }
    }
}
