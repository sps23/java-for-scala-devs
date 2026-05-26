package io.github.sps23.spring.mvc;

import java.math.BigDecimal;
import java.util.List;

/**
 * A snapshot of the current portfolio state.
 *
 * <p>
 * In a real Spring MVC application this record would be the return value of a
 * {@code @GetMapping} handler and automatically serialised to JSON.
 *
 * @param positions
 *            list of current open positions (one per symbol)
 * @param totalInvested
 *            total amount of money that has been put into all trades
 * @param totalCurrentValue
 *            current market value of all positions
 * @param totalGainLoss
 *            profit or loss (negative = tanking, as expected)
 */
public record PortfolioSummary(List<PortfolioPosition> positions, BigDecimal totalInvested,
        BigDecimal totalCurrentValue, BigDecimal totalGainLoss) {

    /**
     * A single stock position within the portfolio.
     *
     * @param symbol
     *            stock ticker symbol
     * @param sharesHeld
     *            number of shares currently held (net of buys minus sells)
     * @param averageCostPerShare
     *            weighted average cost per share across all BUY trades
     * @param currentValue
     *            current market value (sharesHeld × current price)
     * @param gainLoss
     *            unrealised gain or loss for this position
     */
    public record PortfolioPosition(String symbol, int sharesHeld, BigDecimal averageCostPerShare,
            BigDecimal currentValue, BigDecimal gainLoss) {
    }
}
