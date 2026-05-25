package io.github.sps23.spring.ioc;

/**
 * Core business service that places orders.
 *
 * <p>
 * Demonstrates <strong>constructor injection</strong> — the pattern Spring
 * recommends for mandatory dependencies. All three dependencies are declared
 * {@code final}, which means:
 * <ul>
 * <li>The object is fully initialised the moment it's created.</li>
 * <li>Dependencies cannot be replaced after construction (immutability).</li>
 * <li>The class can be instantiated in unit tests with
 * {@code new OrderService(mock, mock, mock)} — no Spring context required.</li>
 * </ul>
 *
 * <p>
 * In a Spring application this class would be annotated with {@code @Service}
 * and Spring would locate implementations of each interface in the application
 * context and inject them automatically.
 */
public class OrderService {

    private final PaymentGateway paymentGateway;
    private final EmailSender emailSender;
    private final OrderRepository repository;

    public OrderService(PaymentGateway paymentGateway, EmailSender emailSender,
            OrderRepository repository) {
        this.paymentGateway = paymentGateway;
        this.emailSender = emailSender;
        this.repository = repository;
    }

    /**
     * Places an order: charges the customer, persists the order, and sends a
     * confirmation email.
     *
     * <p>
     * If the payment fails a {@link PaymentException} is propagated; the order is
     * not saved and no email is sent.
     *
     * @param order
     *            the order to place
     * @return confirmation containing the order id and payment transaction id
     * @throws PaymentException
     *             if the payment gateway rejects the charge
     */
    public OrderConfirmation placeOrder(Order order) {
        // charge first — if it fails we don't want a saved order with no payment
        var payment = paymentGateway.charge(order.amount(), order.customerId());
        repository.save(order);
        emailSender.send(order.customerEmail(), "Your order is confirmed!");
        return new OrderConfirmation(order.id(), payment.transactionId());
    }
}
