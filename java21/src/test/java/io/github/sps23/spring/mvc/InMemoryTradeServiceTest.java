package io.github.sps23.spring.mvc;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link InMemoryTradeService} that prove the service-layer
 * logic described in the "Building RESTful APIs with Spring MVC" blog post
 * works correctly.
 *
 * <p>
 * Key point: the service is instantiated with
 * {@code new InMemoryTradeService()} — no Spring context required. This
 * illustrates the testability benefit of keeping business logic in a plain
 * service class, separate from the {@code @RestController} that handles HTTP
 * concerns.
 *
 * @see <a href=
 *      "https://javaforscaladevs.com/blog/2025/12/15/spring-mvc-restful-apis.html">Blog
 *      post: Building RESTful APIs with Spring MVC</a>
 */
@DisplayName("InMemoryTradeService — Spring MVC service layer")
class InMemoryTradeServiceTest {

    private InMemoryTradeService service;

    @BeforeEach
    void setUp() {
        service = new InMemoryTradeService();
    }

    // =========================================================================
    // GET /api/trades — list all trades (with optional symbol filter)
    // =========================================================================

    @Nested
    @DisplayName("getTrades — listing trades")
    class GetTradesTests {

        @Test
        @DisplayName("Returns empty list when no trades have been executed")
        void returnsEmptyListInitially() {
            var trades = service.getTrades(Optional.empty());
            assertTrue(trades.isEmpty());
        }

        @Test
        @DisplayName("Returns all trades when no symbol filter is applied")
        void returnsAllTradesWithNoFilter() {
            service.executeTrade(buyRequest("AAPL", 10, "182.50"));
            service.executeTrade(buyRequest("TSLA", 5, "250.00"));

            var trades = service.getTrades(Optional.empty());

            assertEquals(2, trades.size());
        }

        @Test
        @DisplayName("Filters trades by symbol (case-insensitive)")
        void filtersTradesBySymbol() {
            service.executeTrade(buyRequest("AAPL", 10, "182.50"));
            service.executeTrade(buyRequest("TSLA", 5, "250.00"));
            service.executeTrade(buyRequest("AAPL", 5, "185.00"));

            // lowercase symbol — case-insensitive filter
            var applTrades = service.getTrades(Optional.of("aapl"));

            assertEquals(2, applTrades.size());
            assertTrue(applTrades.stream().allMatch(t -> t.symbol().equals("AAPL")));
        }

        @Test
        @DisplayName("Returns empty list when filter matches no trades")
        void returnsEmptyListWhenFilterMatchesNothing() {
            service.executeTrade(buyRequest("AAPL", 10, "182.50"));

            var trades = service.getTrades(Optional.of("NVDA"));

            assertTrue(trades.isEmpty());
        }
    }

    // =========================================================================
    // GET /api/trades/{tradeId} — get a single trade by ID
    // =========================================================================

    @Nested
    @DisplayName("getTradeById — looking up a single trade")
    class GetTradeByIdTests {

        @Test
        @DisplayName("Returns the trade when it exists")
        void returnsTradeWhenFound() {
            var created = service.executeTrade(buyRequest("NVDA", 3, "875.20"));

            var found = service.getTradeById(created.tradeId());

            assertEquals(created.tradeId(), found.tradeId());
            assertEquals("NVDA", found.symbol());
            assertEquals(TradeType.BUY, found.type());
            assertEquals(3, found.quantity());
        }

        @Test
        @DisplayName("Throws TradeNotFoundException when trade ID does not exist")
        void throwsWhenTradeNotFound() {
            // This maps to a 404 response in the controller via @ExceptionHandler
            var ex = assertThrows(TradeNotFoundException.class,
                    () -> service.getTradeById("trade-does-not-exist"));

            assertTrue(ex.getMessage().contains("trade-does-not-exist"));
        }
    }

    // =========================================================================
    // POST /api/trades — execute a new trade
    // =========================================================================

    @Nested
    @DisplayName("executeTrade — placing a trade")
    class ExecuteTradeTests {

        @Test
        @DisplayName("Assigns a unique ID and timestamp to the new trade")
        void assignsIdAndTimestamp() {
            var request = buyRequest("AAPL", 10, "182.50");

            var trade = service.executeTrade(request);

            assertNotNull(trade.tradeId(), "Trade must have an ID");
            assertNotNull(trade.executedAt(), "Trade must have a timestamp");
            assertFalse(trade.tradeId().isBlank());
        }

        @Test
        @DisplayName("Two trades get different IDs")
        void eachTradeGetsDistinctId() {
            var t1 = service.executeTrade(buyRequest("AAPL", 5, "180.00"));
            var t2 = service.executeTrade(buyRequest("AAPL", 5, "180.00"));

            assertNotEquals(t1.tradeId(), t2.tradeId());
        }

        @Test
        @DisplayName("Persists trade so it appears in subsequent getTrades() calls")
        void persistsTradeForLaterRetrieval() {
            var request = buyRequest("TSLA", 2, "248.75");
            var created = service.executeTrade(request);

            var all = service.getTrades(Optional.empty());

            assertEquals(1, all.size());
            assertEquals(created.tradeId(), all.get(0).tradeId());
        }

        @Test
        @DisplayName("Converts symbol to uppercase for consistency")
        void normalisesSymbolToUpperCase() {
            var request = new TradeRequest("tsla", TradeType.BUY, 1, new BigDecimal("250.00"));

            var trade = service.executeTrade(request);

            assertEquals("TSLA", trade.symbol());
        }

        @Test
        @DisplayName("Calculates correct total value for the trade")
        void calculatesTotalValueCorrectly() {
            var request = buyRequest("AAPL", 10, "182.50");

            var trade = service.executeTrade(request);

            // 10 × $182.50 = $1,825.00
            assertEquals(new BigDecimal("1825.00"), trade.totalValue());
        }

        @Test
        @DisplayName("Rejects a TradeRequest with zero quantity")
        void rejectsZeroQuantity() {
            assertThrows(IllegalArgumentException.class,
                    () -> new TradeRequest("AAPL", TradeType.BUY, 0, new BigDecimal("100.00")));
        }

        @Test
        @DisplayName("Rejects a TradeRequest with negative price")
        void rejectsNegativePrice() {
            assertThrows(IllegalArgumentException.class,
                    () -> new TradeRequest("AAPL", TradeType.BUY, 1, new BigDecimal("-1.00")));
        }
    }

    // =========================================================================
    // GET /api/portfolio — portfolio summary
    // =========================================================================

    @Nested
    @DisplayName("getPortfolio — portfolio summary")
    class GetPortfolioTests {

        @Test
        @DisplayName("Returns zero values when portfolio is empty")
        void emptyPortfolioHasZeroValues() {
            var portfolio = service.getPortfolio();

            assertTrue(portfolio.positions().isEmpty());
            assertEquals(BigDecimal.ZERO, portfolio.totalInvested());
            assertEquals(BigDecimal.ZERO, portfolio.totalCurrentValue());
            assertEquals(BigDecimal.ZERO, portfolio.totalGainLoss());
        }

        @Test
        @DisplayName("Portfolio reflects all BUY trades for a single symbol")
        void portfolioReflectsBuyTrades() {
            // Buy 10 AAPL @ $180
            service.executeTrade(buyRequest("AAPL", 10, "180.00"));

            var portfolio = service.getPortfolio();

            assertEquals(1, portfolio.positions().size());
            var position = portfolio.positions().get(0);
            assertEquals("AAPL", position.symbol());
            assertEquals(10, position.sharesHeld());
            // Current value should be 10 × $180 = $1,800
            assertEquals(new BigDecimal("1800.00"), position.currentValue());
        }

        @Test
        @DisplayName("SELL trades reduce the share count")
        void sellReducesShareCount() {
            service.executeTrade(buyRequest("AAPL", 10, "180.00"));
            service.executeTrade(sellRequest("AAPL", 4, "195.00")); // sell 4, keep 6

            var portfolio = service.getPortfolio();

            assertEquals(1, portfolio.positions().size());
            assertEquals(6, portfolio.positions().get(0).sharesHeld());
        }

        @Test
        @DisplayName("Multiple symbols appear as separate positions")
        void multipleSymbolsCreateSeparatePositions() {
            service.executeTrade(buyRequest("AAPL", 5, "180.00"));
            service.executeTrade(buyRequest("NVDA", 2, "875.00"));

            var portfolio = service.getPortfolio();

            assertEquals(2, portfolio.positions().size());
        }

        @Test
        @DisplayName("Gain/loss is negative when most recent trade price is below average cost")
        void gainLossReflectsPriceDifference() {
            // Buy 10 TSLA at $200 then buy 10 more at $160 (price dropped — oof)
            service.executeTrade(buyRequest("TSLA", 10, "200.00"));
            service.executeTrade(buyRequest("TSLA", 10, "160.00")); // "averaging down"

            var portfolio = service.getPortfolio();
            var pos = portfolio.positions().get(0);

            // 20 shares held, latest price $160, current value = $3,200
            assertEquals(20, pos.sharesHeld());
            assertEquals(new BigDecimal("3200.00"), pos.currentValue());
            // Average cost = (10×200 + 10×160) / 20 = $180; gain/loss = 3200 - 3600 = -$400
            assertTrue(pos.gainLoss().compareTo(BigDecimal.ZERO) < 0,
                    "Should have a loss when current price is below average cost");
        }
    }

    // =========================================================================
    // ApiError — error response factory
    // =========================================================================

    @Nested
    @DisplayName("ApiError — error response body")
    class ApiErrorTests {

        @Test
        @DisplayName("ApiError.of() sets status and message and a non-null timestamp")
        void factoryMethodSetsAllFields() {
            var error = ApiError.of(404, "Trade xyz not found");

            assertEquals(404, error.status());
            assertEquals("Trade xyz not found", error.message());
            assertNotNull(error.timestamp());
        }

        @Test
        @DisplayName("TradeNotFoundException message includes the trade ID")
        void notFoundExceptionIncludesId() {
            var ex = new TradeNotFoundException("trade-abc");

            assertTrue(ex.getMessage().contains("trade-abc"));
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static TradeRequest buyRequest(String symbol, int qty, String price) {
        return new TradeRequest(symbol, TradeType.BUY, qty, new BigDecimal(price));
    }

    private static TradeRequest sellRequest(String symbol, int qty, String price) {
        return new TradeRequest(symbol, TradeType.SELL, qty, new BigDecimal(price));
    }
}
