package io.github.sps23.spring.mvc;

import java.math.BigDecimal;

/**
 * Request body for executing a new trade.
 *
 * <p>
 * In a real Spring MVC application this record would be annotated with Bean Validation constraints
 * and bound from a JSON request body using {@code @RequestBody @Valid TradeRequest request}.
 * Spring's {@code HandlerMethodArgumentResolver} deserialises the JSON automatically.
 *
 * <p>
 * The validation annotations shown in comments below are what you would add in a Spring app:
 *
 * <pre>
 * {@code @NotBlank String symbol}
 * {@code @NotNull TradeType type}
 * {@code @Min(1) int quantity}
 * {@code @DecimalMin("0.01") BigDecimal pricePerShare}
 * </pre>
 *
 * @param symbol
 *            stock ticker symbol (e.g. "TSLA")
 * @param type
 *            BUY or SELL
 * @param quantity
 *            number of shares (must be at least 1)
 * @param pricePerShare
 *            limit price per share (must be positive)
 */
public record TradeRequest(
        String symbol,
        TradeType type,
        int quantity,
        BigDecimal pricePerShare) {

    public TradeRequest {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        if (type == null) {
            throw new IllegalArgumentException("type must not be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (pricePerShare == null || pricePerShare.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("pricePerShare must be positive");
        }
    }
}
