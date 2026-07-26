package io.github.sps23.spring.configuration;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.format.DateTimeFormatter;

/**
 * Plain Java object deliberately left annotation-free so configuration can
 * decide how to create it.
 */
public final class InvoiceFormatter {

    private final Clock clock;
    private final String currencyCode;

    public InvoiceFormatter(Clock clock, String currencyCode) {
        this.clock = clock;
        this.currencyCode = currencyCode;
    }

    public String format(CheckoutRequest request, BigDecimal total, String paymentMode) {
        return "invoice[%s|%s|%s|%s|%s]".formatted(request.customerId(), paymentMode, currencyCode,
                total.toPlainString(), DateTimeFormatter.ISO_INSTANT.format(clock.instant()));
    }
}
