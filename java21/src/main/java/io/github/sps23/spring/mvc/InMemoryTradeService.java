package io.github.sps23.spring.mvc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * In-memory implementation of {@link TradeService} — suitable for tests and
 * standalone demonstrations without a database.
 *
 * <p>
 * This implementation uses a plain unsynchronised {@code ArrayList}. It is
 * intentionally single-threaded and designed for tests and standalone
 * demonstrations only. A production implementation would use a database-backed
 * Spring Data repository and would not need to worry about in-process thread
 * safety at this layer.
 *
 * <p>
 * Portfolio gain/loss is calculated using a simplified model: the current price
 * of each position is assumed to be the price of the most recent BUY or SELL
 * trade for that symbol. In a real system you would call a market-data API.
 */
public class InMemoryTradeService implements TradeService {

    private final List<Trade> trades = new ArrayList<>();

    @Override
    public List<Trade> getTrades(Optional<String> symbol) {
        return symbol.map(s -> trades.stream().filter(t -> t.symbol().equalsIgnoreCase(s))
                .collect(Collectors.toList())).orElseGet(() -> List.copyOf(trades));
    }

    @Override
    public Trade getTradeById(String tradeId) {
        return trades.stream().filter(t -> t.tradeId().equals(tradeId)).findFirst()
                .orElseThrow(() -> new TradeNotFoundException(tradeId));
    }

    @Override
    public Trade executeTrade(TradeRequest request) {
        var trade = new Trade(UUID.randomUUID().toString(), request.symbol().toUpperCase(),
                request.type(), request.quantity(), request.pricePerShare(), Instant.now());
        trades.add(trade);
        return trade;
    }

    @Override
    public PortfolioSummary getPortfolio() {
        // Group trades by symbol and compute net position for each
        var tradesBySymbol = trades.stream().collect(
                Collectors.groupingBy(Trade::symbol, LinkedHashMap::new, Collectors.toList()));

        var positions = new ArrayList<PortfolioSummary.PortfolioPosition>();
        var totalInvested = new AtomicReference<>(BigDecimal.ZERO);
        var totalCurrentValue = new AtomicReference<>(BigDecimal.ZERO);

        tradesBySymbol.forEach((symbol, symbolTrades) -> {
            var position = buildPosition(symbol, symbolTrades);
            if (position.sharesHeld() > 0) {
                positions.add(position);
                totalInvested.updateAndGet(v -> v.add(position.averageCostPerShare()
                        .multiply(BigDecimal.valueOf(position.sharesHeld()))));
                totalCurrentValue.updateAndGet(v -> v.add(position.currentValue()));
            }
        });

        var invested = totalInvested.get();
        var current = totalCurrentValue.get();
        return new PortfolioSummary(List.copyOf(positions), invested, current,
                current.subtract(invested));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private PortfolioSummary.PortfolioPosition buildPosition(String symbol,
            List<Trade> symbolTrades) {
        var sharesHeld = 0;
        var totalCost = BigDecimal.ZERO;
        var latestPrice = BigDecimal.ZERO;

        for (var trade : symbolTrades) {
            latestPrice = trade.pricePerShare();
            if (trade.type() == TradeType.BUY) {
                sharesHeld += trade.quantity();
                totalCost = totalCost.add(trade.totalValue());
            } else {
                sharesHeld -= trade.quantity();
                // Reduce cost basis proportionally on sells (simplified FIFO)
                var sharesBeforeSell = sharesHeld + trade.quantity();
                if (sharesBeforeSell > 0) {
                    var sellRatio = BigDecimal.valueOf(trade.quantity())
                            .divide(BigDecimal.valueOf(sharesBeforeSell), 10, RoundingMode.HALF_UP);
                    totalCost = totalCost.subtract(totalCost.multiply(sellRatio));
                }
            }
        }

        var avgCost = sharesHeld > 0
                ? totalCost.divide(BigDecimal.valueOf(sharesHeld), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        var currentValue = latestPrice.multiply(BigDecimal.valueOf(Math.max(sharesHeld, 0)));
        var gainLoss = currentValue.subtract(totalCost);

        return new PortfolioSummary.PortfolioPosition(symbol, Math.max(sharesHeld, 0), avgCost,
                currentValue, gainLoss);
    }

    /**
     * Returns a read-only view of all trades — useful for assertions in tests.
     *
     * @return unmodifiable list of all trades
     */
    public List<Trade> allTrades() {
        return List.copyOf(trades);
    }
}
