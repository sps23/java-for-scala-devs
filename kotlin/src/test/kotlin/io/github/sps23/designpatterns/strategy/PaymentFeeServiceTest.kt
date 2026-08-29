package io.github.sps23.designpatterns.strategy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

@DisplayName("PaymentFeeService Kotlin Tests")
class PaymentFeeServiceTest {
    @Test
    @DisplayName("Should calculate card fees with percentage and flat charge")
    fun shouldCalculateCardFee() {
        val service = PaymentFeeService.defaultService()
        val request = PaymentRequest(PaymentMethod.CARD, BigDecimal("100.00"), "GBP", false)

        val quote = service.quote(request)

        assertEquals(BigDecimal("3.20"), quote.fee)
        assertEquals(BigDecimal("103.20"), quote.totalAmount)
    }

    @Test
    @DisplayName("Should cap bank transfer fees")
    fun shouldCapBankTransferFee() {
        val service = PaymentFeeService.defaultService()
        val request = PaymentRequest(PaymentMethod.BANK_TRANSFER, BigDecimal("1000.00"), "GBP", false)

        val quote = service.quote(request)

        assertEquals(BigDecimal("7.50"), quote.fee)
        assertEquals(BigDecimal("1007.50"), quote.totalAmount)
    }

    @Test
    @DisplayName("Should enforce a minimum wallet fee")
    fun shouldApplyMinimumWalletFee() {
        val service = PaymentFeeService.defaultService()
        val request = PaymentRequest(PaymentMethod.DIGITAL_WALLET, BigDecimal("5.00"), "GBP", false)

        val quote = service.quote(request)

        assertEquals(BigDecimal("0.25"), quote.fee)
        assertEquals(BigDecimal("5.25"), quote.totalAmount)
    }

    @Test
    @DisplayName("Should allow lambda-based custom strategies")
    fun shouldAllowLambdaBasedCustomStrategies() {
        val service =
            PaymentFeeService(
                mapOf(
                    PaymentMethod.BUY_NOW_PAY_LATER to PaymentFeeStrategy { BigDecimal("9.00") },
                ),
            )
        val request = PaymentRequest(PaymentMethod.BUY_NOW_PAY_LATER, BigDecimal("200.00"), "GBP", false)

        val quote = service.quote(request)

        assertEquals(BigDecimal("9.00"), quote.fee)
        assertEquals(BigDecimal("209.00"), quote.totalAmount)
    }

    @Test
    @DisplayName("Should reject unknown strategies")
    fun shouldRejectUnknownStrategies() {
        val service = PaymentFeeService.defaultService()
        val request = PaymentRequest(PaymentMethod.BUY_NOW_PAY_LATER, BigDecimal("200.00"), "GBP", false)

        val error =
            assertThrows(IllegalArgumentException::class.java) {
                service.quote(request)
            }

        assertEquals("No strategy configured for payment method: BUY_NOW_PAY_LATER", error.message)
    }
}
