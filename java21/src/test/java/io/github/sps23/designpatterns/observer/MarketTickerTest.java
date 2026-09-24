package io.github.sps23.designpatterns.observer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MarketTicker Java 21 Tests")
class MarketTickerTest {

    @Test
    @DisplayName("Should notify subscribers when a new price is published")
    void shouldNotifySubscribersWhenPriceChanges() {
        var ticker = new MarketTicker();
        var received = new ArrayList<String>();
        MarketTicker.PriceObserver observer = update -> received.add(update.symbol() + ":" + update.price());

        ticker.subscribe(observer);
        var update = ticker.publishPrice("AAPL", new BigDecimal("198.75"));

        assertEquals(List.of("AAPL:198.75"), received);
        assertEquals(Optional.of(new BigDecimal("198.75")), ticker.latestPrice("AAPL"));
        assertEquals("AAPL", update.symbol());
        assertEquals(new BigDecimal("198.75"), update.price());
    }

    @Test
    @DisplayName("Should stop notifying unsubscribed observers")
    void shouldStopNotifyingUnsubscribedObservers() {
        var ticker = new MarketTicker();
        var received = new ArrayList<String>();
        MarketTicker.PriceObserver observer = update -> received.add(update.symbol() + ":" + update.price());

        ticker.subscribe(observer);
        ticker.unsubscribe(observer);
        ticker.publishPrice("MSFT", new BigDecimal("425.00"));

        assertEquals(List.of(), received);
    }

    @Test
    @DisplayName("Should notify multiple observers with the same update")
    void shouldNotifyMultipleObservers() {
        var ticker = new MarketTicker();
        var first = new ArrayList<String>();
        var second = new ArrayList<String>();

        ticker.subscribe(update -> first.add(update.symbol() + ":" + update.price()));
        ticker.subscribe(update -> second.add(update.symbol() + ":" + update.price()));

        ticker.publishPrice("NVDA", new BigDecimal("129.10"));

        assertEquals(List.of("NVDA:129.10"), first);
        assertEquals(List.of("NVDA:129.10"), second);
    }
}
