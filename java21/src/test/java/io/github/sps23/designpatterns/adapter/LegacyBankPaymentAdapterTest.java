package io.github.sps23.designpatterns.adapter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Adapter Pattern Java 21 Tests")
class LegacyBankPaymentAdapterTest {

    private final PaymentGateway paymentGateway = new LegacyBankPaymentAdapter(new LegacyBankApi());

    @Test
    @DisplayName("Should approve checkout payment through legacy bank adapter")
    void shouldApproveCheckoutPayment() {
        CheckoutService checkoutService = new CheckoutService(paymentGateway);

        String confirmation = checkoutService.checkout("cust-42", 1599, "eur");

        assertTrue(confirmation.startsWith("CONFIRMED:TX-CUST-42-1599"));
    }

    @Test
    @DisplayName("Should reject unsupported currencies from legacy API")
    void shouldRejectUnsupportedCurrency() {
        PaymentResult result = paymentGateway.charge(new PaymentRequest("cust-42", 1599, "pln"));

        assertEquals(false, result.approved());
        assertEquals("Unsupported currency: PLN", result.message());
    }

    @Test
    @DisplayName("Should fail fast when amount is not positive")
    void shouldFailFastOnInvalidAmount() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> paymentGateway.charge(new PaymentRequest("cust-42", 0, "EUR")));
        assertEquals("Amount must be greater than zero", error.getMessage());
    }
}

final class CheckoutService {
    private final PaymentGateway paymentGateway;

    CheckoutService(PaymentGateway paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    String checkout(String customerId, int amountInCents, String currency) {
        PaymentResult result = paymentGateway
                .charge(new PaymentRequest(customerId, amountInCents, currency));
        if (result.approved()) {
            return "CONFIRMED:" + result.transactionId();
        }
        return "REJECTED:" + result.message();
    }
}
