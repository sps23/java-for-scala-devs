package io.github.sps23.spring.ioc;

/**
 * Immutable order confirmation returned after a successful order is placed.
 */
public record OrderConfirmation(String orderId, String transactionId) {
}
