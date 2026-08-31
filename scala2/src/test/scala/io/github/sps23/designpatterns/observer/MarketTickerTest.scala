package io.github.sps23.designpatterns.observer

import java.math.BigDecimal
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class MarketTickerTest extends AnyFunSuite with Matchers {

  test("Observer should notify subscribers when a new price is published") {
    val ticker   = new MarketTicker
    val received = scala.collection.mutable.ListBuffer.empty[String]
    val observer = new ticker.PriceObserver {
      override def onPriceUpdate(update: ticker.PriceUpdate): Unit =
        received += s"${update.symbol}:${update.price}"
    }

    ticker.subscribe(observer)
    val update = ticker.publishPrice("AAPL", BigDecimal("198.75"))

    received.toList shouldBe List("AAPL:198.75")
    ticker.latestPrice("AAPL") shouldBe Some(BigDecimal("198.75"))
    update.symbol shouldBe "AAPL"
    update.price shouldBe BigDecimal("198.75")
  }

  test("Observer should stop notifying unsubscribed listeners") {
    val ticker   = new MarketTicker
    val received = scala.collection.mutable.ListBuffer.empty[String]
    val observer = new ticker.PriceObserver {
      override def onPriceUpdate(update: ticker.PriceUpdate): Unit =
        received += s"${update.symbol}:${update.price}"
    }

    ticker.subscribe(observer)
    ticker.unsubscribe(observer)
    ticker.publishPrice("MSFT", BigDecimal("425.00"))

    received.toList shouldBe List.empty
  }

  test("Observer should notify multiple listeners with the same update") {
    val ticker = new MarketTicker
    val first  = scala.collection.mutable.ListBuffer.empty[String]
    val second = scala.collection.mutable.ListBuffer.empty[String]

    ticker.subscribe(new ticker.PriceObserver {
      override def onPriceUpdate(update: ticker.PriceUpdate): Unit =
        first += s"${update.symbol}:${update.price}"
    })
    ticker.subscribe(new ticker.PriceObserver {
      override def onPriceUpdate(update: ticker.PriceUpdate): Unit =
        second += s"${update.symbol}:${update.price}"
    })

    ticker.publishPrice("NVDA", BigDecimal("129.10"))

    first.toList shouldBe List("NVDA:129.10")
    second.toList shouldBe List("NVDA:129.10")
  }
}
