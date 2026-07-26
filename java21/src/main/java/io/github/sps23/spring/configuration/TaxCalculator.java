package io.github.sps23.spring.configuration;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import org.springframework.stereotype.Component;

/**
 * Component-scanned collaborator used by the checkout service.
 */
@Component
public class TaxCalculator {

    /**
     * Applies a tiny VAT table suitable for demos and tests.
     *
     * @param subtotal
     *            amount before tax
     * @param countryCode
     *            country used to resolve the VAT rate
     * @return amount including VAT
     */
    public BigDecimal totalWithVat(BigDecimal subtotal, String countryCode) {
        var vatRate = switch (countryCode.toUpperCase(Locale.ROOT)) {
            case "PL" -> new BigDecimal("0.23");
            case "DE" -> new BigDecimal("0.19");
            default -> new BigDecimal("0.20");
        };
        return subtotal.multiply(BigDecimal.ONE.add(vatRate)).setScale(2, RoundingMode.HALF_UP);
    }
}
