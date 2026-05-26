package io.github.sps23.spring.mvc;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * An executed stock trade.
 *
 * <p>
 * In a real Spring MVC application this record would be returned from a
 * {@code @RestController} and automatically serialised to JSON by Jackson.
 * The {@code @JsonFormat} annotation (not shown here) would control timestamp formatting.
 *
 * <p>
 * Using a Java {@code record} means Spring (via Jackson) can deserialise JSON into it without any
 * boilerplate — no getters, no setters, no {@code @JsonProperty}. The compact constructor handles
 * validation.
 *
 * @param tradeId
 *            unique identifier for this trade
 * @param symbol
 *            stock ticker symbol (e.g. "AAPL", "NVDA")
 * @param type
 *            BUY or SELL
 * @param quantity
 *            number of shares
 * @param pricePerShare
 *            price per share at execution time
 * @param executedAt
 *            when the trade was executed (UTC)
 */
public record Trade(
        String tradeId,
        String symbol,
        TradeType type,
        int quantity,
        BigDecimal pricePerShare,
        Instant executedAt) {

    public Trade {
        if (tradeId == null || tradeId.isBlank()) {
            throw new IllegalArgumentException("tradeId must not be blank");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("symbol must not be blank");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity must be positive");
        }
        if (pricePerShare == null || pricePerShare.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("pricePerShare must be positive");
        }
    }

    /**
     * Total value of this trade (quantity × price per share).
     *
     * @return total value
     */
    public BigDecimal totalValue() {
        return pricePerShare.multiply(BigDecimal.valueOf(quantity));
    }
}
