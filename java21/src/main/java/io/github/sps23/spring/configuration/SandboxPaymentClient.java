package io.github.sps23.spring.configuration;

import java.math.BigDecimal;

/**
 * Development-friendly payment client created with a {@code @Bean} method.
 */
public final class SandboxPaymentClient implements PaymentClient {

    private final String terminalName;

    public SandboxPaymentClient(String terminalName) {
        this.terminalName = terminalName;
    }

    @Override
    public String charge(CheckoutRequest request, BigDecimal total) {
        return "sandbox-%s-%s".formatted(request.customerId(), terminalName);
    }

    @Override
    public String mode() {
        return "sandbox";
    }
}
