package io.github.sps23.spring.configuration;

import org.springframework.stereotype.Service;

/**
 * Service discovered by component scanning and wired by constructor injection.
 */
@Service
public class CheckoutService {

    private final PaymentClient paymentClient;
    private final TaxCalculator taxCalculator;
    private final InvoiceFormatter invoiceFormatter;
    private final LegacySupportPolicy legacySupportPolicy;

    public CheckoutService(PaymentClient paymentClient, TaxCalculator taxCalculator,
            InvoiceFormatter invoiceFormatter, LegacySupportPolicy legacySupportPolicy) {
        this.paymentClient = paymentClient;
        this.taxCalculator = taxCalculator;
        this.invoiceFormatter = invoiceFormatter;
        this.legacySupportPolicy = legacySupportPolicy;
    }

    /**
     * Calculates tax, charges the customer, formats an invoice, and chooses a
     * support queue.
     *
     * @param request
     *            checkout request
     * @return receipt with configuration-driven details
     */
    public CheckoutReceipt checkout(CheckoutRequest request) {
        var total = taxCalculator.totalWithVat(request.subtotal(), request.countryCode());
        var transactionId = paymentClient.charge(request, total);
        var invoiceLine = invoiceFormatter.format(request, total, paymentClient.mode());
        var supportQueue = legacySupportPolicy.supportQueueFor(request.salesChannel());
        return new CheckoutReceipt(transactionId, paymentClient.mode(), total, invoiceLine,
                supportQueue);
    }
}
