package io.github.sps23.designpatterns.observer

import java.math.BigDecimal
import java.time.Instant
import scala.collection.mutable

class MarketTicker {
  case class PriceUpdate(symbol: String, price: BigDecimal, timestamp: Instant)

  trait PriceObserver {
    def onPriceUpdate(update: PriceUpdate): Unit
  }

  private val observers    = mutable.Set.empty[PriceObserver]
  private val latestPrices = mutable.Map.empty[String, BigDecimal]

  def subscribe(observer: PriceObserver): Unit = observers += observer

  def unsubscribe(observer: PriceObserver): Unit = observers -= observer

  def publishPrice(symbol: String, price: BigDecimal): PriceUpdate = {
    val update = PriceUpdate(symbol, price, Instant.now())
    latestPrices.update(symbol, price)
    observers.foreach(_.onPriceUpdate(update))
    update
  }

  def latestPrice(symbol: String): Option[BigDecimal] = latestPrices.get(symbol)
}
