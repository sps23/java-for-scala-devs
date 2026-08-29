package io.github.sps23.designpatterns.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PaymentFeeService Java 21 Tests")
class PaymentFeeServiceTest {

    @Test
    @DisplayName("Should calculate card fees with percentage and flat charge")
    void shouldCalculateCardFee() {
        var service = PaymentFeeService.defaultService();
        var request = new PaymentRequest(PaymentMethod.CARD, new BigDecimal("100.00"), "GBP",
                false);

        var quote = service.quote(request);

        assertEquals(new BigDecimal("3.20"), quote.fee());
        assertEquals(new BigDecimal("103.20"), quote.totalAmount());
    }

    @Test
    @DisplayName("Should cap bank transfer fees")
    void shouldCapBankTransferFee() {
        var service = PaymentFeeService.defaultService();
        var request = new PaymentRequest(PaymentMethod.BANK_TRANSFER, new BigDecimal("1000.00"),
                "GBP", false);

        var quote = service.quote(request);

        assertEquals(new BigDecimal("7.50"), quote.fee());
        assertEquals(new BigDecimal("1007.50"), quote.totalAmount());
    }

    @Test
    @DisplayName("Should enforce a minimum wallet fee")
    void shouldApplyMinimumWalletFee() {
        var service = PaymentFeeService.defaultService();
        var request = new PaymentRequest(PaymentMethod.DIGITAL_WALLET, new BigDecimal("5.00"),
                "GBP", false);

        var quote = service.quote(request);

        assertEquals(new BigDecimal("0.25"), quote.fee());
        assertEquals(new BigDecimal("5.25"), quote.totalAmount());
    }

    @Test
    @DisplayName("Should allow lambda-based custom strategies")
    void shouldAllowLambdaBasedCustomStrategies() {
        var service = new PaymentFeeService(
                Map.of(PaymentMethod.BUY_NOW_PAY_LATER, request -> new BigDecimal("9.00")));
        var request = new PaymentRequest(PaymentMethod.BUY_NOW_PAY_LATER, new BigDecimal("200.00"),
                "GBP", false);

        var quote = service.quote(request);

        assertEquals(new BigDecimal("9.00"), quote.fee());
        assertEquals(new BigDecimal("209.00"), quote.totalAmount());
    }

    @Test
    @DisplayName("Should reject unknown strategies")
    void shouldRejectUnknownStrategies() {
        var service = PaymentFeeService.defaultService();
        var request = new PaymentRequest(PaymentMethod.BUY_NOW_PAY_LATER, new BigDecimal("200.00"),
                "GBP", false);

        var error = assertThrows(IllegalArgumentException.class, () -> service.quote(request));

        assertEquals("No strategy configured for payment method: BUY_NOW_PAY_LATER",
                error.getMessage());
    }
}
