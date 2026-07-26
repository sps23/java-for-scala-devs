package io.github.sps23.spring.configuration;

import java.math.BigDecimal;

/**
 * Production payment client created with a profile-specific {@code @Bean}.
 */
public final class LivePaymentClient implements PaymentClient {

    private final String merchantId;

    public LivePaymentClient(String merchantId) {
        this.merchantId = merchantId;
    }

    @Override
    public String charge(CheckoutRequest request, BigDecimal total) {
        return "live-%s-%s".formatted(request.customerId(), merchantId);
    }

    @Override
    public String mode() {
        return "live";
    }
}
