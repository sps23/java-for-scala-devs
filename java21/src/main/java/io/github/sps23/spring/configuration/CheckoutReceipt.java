package io.github.sps23.spring.configuration;

import java.math.BigDecimal;

/**
 * Output returned by {@link CheckoutService}.
 *
 * @param transactionId
 *            payment provider transaction id
 * @param paymentMode
 *            active payment configuration, for example sandbox or live
 * @param total
 *            final amount after tax
 * @param invoiceLine
 *            formatted invoice summary
 * @param supportQueue
 *            queue selected by legacy XML configuration
 */
public record CheckoutReceipt(String transactionId, String paymentMode, BigDecimal total,
        String invoiceLine, String supportQueue) {
}
