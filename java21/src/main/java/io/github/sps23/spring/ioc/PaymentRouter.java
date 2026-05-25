package io.github.sps23.spring.ioc;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Routes payment requests to the correct gateway based on a provider name.
 *
 * <p>
 * Demonstrates injecting a <strong>Map of bean names to
 * implementations</strong> — a Spring pattern where the container
 * auto-populates a {@code Map<String,
 * PaymentGateway>} with every {@code PaymentGateway} bean keyed by its bean
 * name.
 *
 * <p>
 * In tests you construct the map yourself:
 *
 * <pre>{@code
 * var router = new PaymentRouter(Map.of("stripe", new FakePaymentGateway("txn_stripe"), "paypal",
 *         new FakePaymentGateway("txn_paypal")));
 * }</pre>
 *
 * <p>
 * In a Spring application the container injects the map automatically: every
 * bean implementing {@link PaymentGateway} is included, keyed by its bean name.
 */
public class PaymentRouter {

    private final Map<String, PaymentGateway> gateways;

    public PaymentRouter(Map<String, PaymentGateway> gateways) {
        this.gateways = Map.copyOf(gateways); // defensive copy
    }

    /**
     * Routes a payment to the named provider.
     *
     * @param provider
     *            the provider key — must match a key in the gateway map
     * @param amount
     *            the amount to charge
     * @param customerId
     *            the customer being charged
     * @return the payment result from the chosen gateway
     * @throws IllegalArgumentException
     *             if no gateway is registered for the given provider
     */
    public PaymentResult route(String provider, BigDecimal amount, String customerId) {
        var gateway = gateways.get(provider);
        if (gateway == null) {
            throw new IllegalArgumentException("Unknown payment provider: %s. Available: %s"
                    .formatted(provider, gateways.keySet()));
        }
        return gateway.charge(amount, customerId);
    }

    /**
     * Returns the set of registered provider names.
     *
     * @return registered provider names
     */
    public java.util.Set<String> availableProviders() {
        return gateways.keySet();
    }
}
