package io.github.sps23.spring.ioc;

import java.math.BigDecimal;

/**
 * Fake implementation of {@link PaymentGateway} for testing.
 *
 * <p>
 * Returns a configurable transaction id without calling any external API.
 * Optionally configured to simulate payment failures.
 */
public class FakePaymentGateway implements PaymentGateway {

    private final String transactionIdPrefix;
    private boolean shouldFail;
    private String failureMessage;

    /**
     * Creates a gateway that succeeds and prefixes transaction ids with the given
     * string.
     *
     * @param transactionIdPrefix
     *            prefix for generated transaction ids (e.g. {@code "txn_fake_"})
     */
    public FakePaymentGateway(String transactionIdPrefix) {
        this.transactionIdPrefix = transactionIdPrefix;
    }

    /**
     * Configures this gateway to throw a {@link PaymentException} on the next call
     * to {@link #charge}.
     *
     * @param failureMessage
     *            the exception message
     * @return {@code this} for fluent configuration
     */
    public FakePaymentGateway willFail(String failureMessage) {
        this.shouldFail = true;
        this.failureMessage = failureMessage;
        return this;
    }

    @Override
    public PaymentResult charge(BigDecimal amount, String customerId) {
        if (shouldFail) {
            throw new PaymentException(failureMessage);
        }
        return new PaymentResult(transactionIdPrefix + customerId, amount);
    }
}
