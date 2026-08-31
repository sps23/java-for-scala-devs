package io.github.sps23.designpatterns.observer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

@DisplayName("MarketTicker Kotlin Tests")
class MarketTickerTest {
    @Test
    @DisplayName("Should notify subscribers when a new price is published")
    fun shouldNotifySubscribersWhenPriceChanges() {
        val ticker = MarketTicker()
        val received = mutableListOf<String>()
        val observer = MarketTicker.PriceObserver { update -> received += "${update.symbol}:${update.price}" }

        ticker.subscribe(observer)
        val update = ticker.publishPrice("AAPL", BigDecimal("198.75"))

        assertEquals(listOf("AAPL:198.75"), received)
        assertEquals(BigDecimal("198.75"), ticker.latestPrice("AAPL"))
        assertEquals("AAPL", update.symbol)
        assertEquals(BigDecimal("198.75"), update.price)
    }

    @Test
    @DisplayName("Should stop notifying unsubscribed observers")
    fun shouldStopNotifyingUnsubscribedObservers() {
        val ticker = MarketTicker()
        val received = mutableListOf<String>()
        val observer = MarketTicker.PriceObserver { update -> received += "${update.symbol}:${update.price}" }

        ticker.subscribe(observer)
        ticker.unsubscribe(observer)
        ticker.publishPrice("MSFT", BigDecimal("425.00"))

        assertEquals(emptyList<String>(), received)
    }

    @Test
    @DisplayName("Should notify multiple observers with the same update")
    fun shouldNotifyMultipleObservers() {
        val ticker = MarketTicker()
        val first = mutableListOf<String>()
        val second = mutableListOf<String>()

        ticker.subscribe(MarketTicker.PriceObserver { update -> first += "${update.symbol}:${update.price}" })
        ticker.subscribe(MarketTicker.PriceObserver { update -> second += "${update.symbol}:${update.price}" })

        ticker.publishPrice("NVDA", BigDecimal("129.10"))

        assertEquals(listOf("NVDA:129.10"), first)
        assertEquals(listOf("NVDA:129.10"), second)
    }
}
