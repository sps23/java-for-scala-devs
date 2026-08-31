package io.github.sps23.designpatterns.observer

import java.math.BigDecimal
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class MarketTicker {
    data class PriceUpdate(
        val symbol: String,
        val price: BigDecimal,
        val timestamp: Instant,
    )

    fun interface PriceObserver {
        fun onPriceUpdate(update: PriceUpdate)
    }

    private val observers = mutableSetOf<PriceObserver>()
    private val latestPrices = ConcurrentHashMap<String, BigDecimal>()

    fun subscribe(observer: PriceObserver) {
        observers += observer
    }

    fun unsubscribe(observer: PriceObserver) {
        observers -= observer
    }

    fun publishPrice(
        symbol: String,
        price: BigDecimal,
    ): PriceUpdate {
        val update = PriceUpdate(symbol, price, Instant.now())
        latestPrices[symbol] = price
        observers.toList().forEach { it.onPriceUpdate(update) }
        return update
    }

    fun latestPrice(symbol: String): BigDecimal? = latestPrices[symbol]
}
