package io.github.sps23.spring.configuration;

import java.math.BigDecimal;

/**
 * Input for the Spring configuration examples.
 *
 * @param customerId
 *            customer placing the order
 * @param countryCode
 *            ISO-like country code used to pick a VAT rate
 * @param subtotal
 *            pre-tax amount
 * @param salesChannel
 *            where the order came from, for legacy support routing
 */
public record CheckoutRequest(String customerId, String countryCode, BigDecimal subtotal,
        String salesChannel) {
}
