package io.github.sps23.designpatterns.adapter;

import java.util.Locale;

sealed interface PaymentGateway permits LegacyBankPaymentAdapter {
    /**
     * Charges a customer using the normalized application-level payment model.
     */
    PaymentResult charge(PaymentRequest request);
}

/**
 * Business-level payment request understood by checkout and other application
 * services.
 */
record PaymentRequest(String customerId, int amountInCents, String currency) {
}

/**
 * Business-level payment result returned to the application after legacy
 * translation.
 */
record PaymentResult(boolean approved, String transactionId, String message) {
}

/**
 * Legacy bank response format (status code + textual details).
 */
record LegacyBankResponse(String statusCode, String reference, String detail) {
}

/**
 * Simulates a legacy banking API with protocol-specific parameter names and
 * status codes.
 */
final class LegacyBankApi {
    /**
     * Legacy contract where "00" means approved and other codes represent
     * rejections.
     */
    LegacyBankResponse submitPayment(String clientCode, long minorUnits, String isoCurrency) {
        if (minorUnits <= 0) {
            return new LegacyBankResponse("12", "N/A", "Amount must be greater than zero");
        }
        if (!"USD".equals(isoCurrency) && !"EUR".equals(isoCurrency)
                && !"GBP".equals(isoCurrency)) {
            return new LegacyBankResponse("14", "N/A", "Unsupported currency: " + isoCurrency);
        }
        return new LegacyBankResponse("00",
                "TX-" + clientCode.toUpperCase(Locale.ROOT) + "-" + minorUnits, "Approved");
    }
}

/**
 * Adapter pattern in Java 21.
 *
 * Wraps a legacy banking API and exposes a modern payment-gateway interface for
 * checkout services.
 */
public final class LegacyBankPaymentAdapter implements PaymentGateway {
    private final LegacyBankApi legacyBankApi;

    public LegacyBankPaymentAdapter(LegacyBankApi legacyBankApi) {
        this.legacyBankApi = legacyBankApi;
    }

    @Override
    public PaymentResult charge(PaymentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Payment request must not be null");
        }
        if (request.customerId() == null || request.customerId().isBlank()) {
            throw new IllegalArgumentException("Customer id must not be blank");
        }
        if (request.amountInCents() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        String currency = normalizeCurrency(request.currency());
        LegacyBankResponse response = legacyBankApi.submitPayment(request.customerId(),
                request.amountInCents(), currency);

        boolean approved = "00".equals(response.statusCode());
        return new PaymentResult(approved, response.reference(),
                approved ? "Payment approved" : response.detail());
    }

    private String normalizeCurrency(String currency) {
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("Currency must not be blank");
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }
}
