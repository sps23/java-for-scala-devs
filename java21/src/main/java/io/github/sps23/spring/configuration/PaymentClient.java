package io.github.sps23.spring.configuration;

import java.math.BigDecimal;

/**
 * Payment client chosen by active Spring profiles.
 */
public interface PaymentClient {

    /**
     * Charges the given amount.
     *
     * @param request
     *            checkout request being processed
     * @param total
     *            final amount to charge
     * @return provider-specific transaction id
     */
    String charge(CheckoutRequest request, BigDecimal total);

    /**
     * Describes the active payment mode.
     *
     * @return short environment label
     */
    String mode();
}
