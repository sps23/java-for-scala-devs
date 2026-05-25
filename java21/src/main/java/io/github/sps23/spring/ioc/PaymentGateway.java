package io.github.sps23.spring.ioc;

import java.math.BigDecimal;

/**
 * Abstraction over a payment provider.
 *
 * <p>
 * Programming to this interface (rather than directly to
 * {@code StripePaymentGateway}) means you can swap implementations — in
 * production use Stripe, in tests use a {@link FakePaymentGateway} — without
 * touching the service code. This is the Open/Closed Principle in practice.
 *
 * <p>
 * In a Spring application a concrete implementation would be annotated with
 * {@code @Component} (or defined via {@code @Bean} in a {@code @Configuration}
 * class) and Spring would inject it wherever {@code PaymentGateway} is
 * required.
 */
public interface PaymentGateway {

    /**
     * Charges the customer the given amount.
     *
     * @param amount
     *            amount to charge (must be positive)
     * @param customerId
     *            identifier of the customer being charged
     * @return result of the payment operation including a transaction id
     * @throws PaymentException
     *             if the charge fails
     */
    PaymentResult charge(BigDecimal amount, String customerId);
}
