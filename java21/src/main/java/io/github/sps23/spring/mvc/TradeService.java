package io.github.sps23.spring.mvc;

import java.util.List;
import java.util.Optional;

/**
 * Business logic for the investment trading REST API.
 *
 * <p>
 * This interface is the service layer that a Spring MVC {@code @RestController}
 * would depend on via constructor injection (as described in the <a href=
 * "https://sps23.github.io/java-for-scala-devs/blog/2026/05/25/spring-ioc-and-dependency-injection.html">IoC
 * and Dependency Injection</a> post).
 *
 * <p>
 * Keeping business logic in a service (not in the controller) is a core Spring
 * MVC convention. The controller handles HTTP concerns (parsing requests,
 * setting status codes); the service handles domain logic (trade validation,
 * portfolio calculations).
 */
public interface TradeService {

    /**
     * Returns all trades, optionally filtered by symbol.
     *
     * <p>
     * Maps to: {@code GET /api/trades} or {@code GET /api/trades?symbol=AAPL}
     *
     * @param symbol
     *            if present, filter trades to this symbol; if empty return all
     * @return list of trades (may be empty, never null)
     */
    List<Trade> getTrades(Optional<String> symbol);

    /**
     * Returns a single trade by its ID.
     *
     * <p>
     * Maps to: {@code GET /api/trades/{tradeId}}
     *
     * @param tradeId
     *            the trade identifier
     * @return the trade
     * @throws TradeNotFoundException
     *             if no trade with this ID exists
     */
    Trade getTradeById(String tradeId);

    /**
     * Executes a new trade and returns the persisted record.
     *
     * <p>
     * Maps to: {@code POST /api/trades}
     *
     * @param request
     *            the trade to execute
     * @return the newly created {@link Trade} with its generated ID and timestamp
     */
    Trade executeTrade(TradeRequest request);

    /**
     * Returns a snapshot of the current portfolio.
     *
     * <p>
     * Maps to: {@code GET /api/portfolio}
     *
     * @return portfolio summary with positions and gain/loss calculations
     */
    PortfolioSummary getPortfolio();
}
