package io.github.sps23.spring.ioc;

import java.math.BigDecimal;

/**
 * Immutable result returned by a payment gateway after charging a customer.
 */
public record PaymentResult(String transactionId, BigDecimal amount) {
}
