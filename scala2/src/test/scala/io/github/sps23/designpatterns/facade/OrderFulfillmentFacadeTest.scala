package io.github.sps23.designpatterns.facade

import java.math.BigDecimal
import org.junit.jupiter.api.Assertions.{assertEquals, assertFalse, assertNotNull, assertTrue}
import org.junit.jupiter.api.{DisplayName, Test}

@DisplayName("OrderFulfillmentFacade Scala 2 Tests")
class OrderFulfillmentFacadeTest {
  @Test
  def shouldPlaceOrderThroughFacade(): Unit = {
    val recorder = new NotificationRecorder
    val facade = new OrderFulfillmentFacade(
      new InventoryAlwaysAvailable,
      new PaymentAlwaysAccepted,
      new ShippingAlwaysScheduled,
      recorder
    )

    val request = OrderFulfillmentFacade.OrderRequest(
      "customer-42",
      "SKU-42",
      2,
      new BigDecimal("149.99"),
      "12 Main Street, London"
    )

    val result = facade.placeOrder(request)

    assertTrue(result.success)
    assertEquals("Order placed successfully", result.message)
    assertNotNull(result.trackingId.orNull)
    assertTrue(recorder.sent)
  }

  @Test
  def shouldRejectWhenInventoryIsInsufficient(): Unit = {
    val facade = new OrderFulfillmentFacade(
      new InventoryInsufficient,
      new PaymentAlwaysAccepted,
      new ShippingAlwaysScheduled,
      new NotificationRecorder
    )

    val result = facade.placeOrder(
      OrderFulfillmentFacade.OrderRequest(
        "customer-99",
        "SKU-999",
        10,
        new BigDecimal("40.00"),
        "99 Market Road, Berlin"
      )
    )

    assertFalse(result.success)
    assertEquals("Inventory unavailable for SKU SKU-999", result.message)
  }

  @Test
  def shouldRejectWhenPaymentIsDeclined(): Unit = {
    val facade = new OrderFulfillmentFacade(
      new InventoryAlwaysAvailable,
      new PaymentRejected,
      new ShippingAlwaysScheduled,
      new NotificationRecorder
    )

    val result = facade.placeOrder(
      OrderFulfillmentFacade.OrderRequest(
        "blocked-customer",
        "SKU-42",
        1,
        new BigDecimal("10.00"),
        "1 High Street, Paris"
      )
    )

    assertFalse(result.success)
    assertEquals("Payment failed for customer blocked-customer", result.message)
  }

  class InventoryAlwaysAvailable extends OrderFulfillmentFacade.InventoryGateway {
    override def hasStock(sku: String, quantity: Int): Boolean = true
  }

  class InventoryInsufficient extends OrderFulfillmentFacade.InventoryGateway {
    override def hasStock(sku: String, quantity: Int): Boolean = false
  }

  class PaymentAlwaysAccepted extends OrderFulfillmentFacade.PaymentGateway {
    override def charge(customerId: String, amount: BigDecimal): Boolean = true
  }

  class PaymentRejected extends OrderFulfillmentFacade.PaymentGateway {
    override def charge(customerId: String, amount: BigDecimal): Boolean = false
  }

  class ShippingAlwaysScheduled extends OrderFulfillmentFacade.ShippingGateway {
    override def scheduleShipment(customerId: String, shippingAddress: String): String = "TRACK-123"
  }

  class NotificationRecorder extends OrderFulfillmentFacade.NotificationGateway {
    var sent: Boolean = false
    override def sendConfirmation(customerId: String, sku: String, trackingId: String): Unit =
      sent = true
  }
}
