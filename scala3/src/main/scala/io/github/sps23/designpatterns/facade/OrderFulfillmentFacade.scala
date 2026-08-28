package io.github.sps23.designpatterns.facade

import java.math.BigDecimal

class OrderFulfillmentFacade(
    inventoryGateway: OrderFulfillmentFacade.InventoryGateway,
    paymentGateway: OrderFulfillmentFacade.PaymentGateway,
    shippingGateway: OrderFulfillmentFacade.ShippingGateway,
    notificationGateway: OrderFulfillmentFacade.NotificationGateway
):
  def placeOrder(
      request: OrderFulfillmentFacade.OrderRequest
  ): OrderFulfillmentFacade.FulfillmentResult =
    require(request != null, "Order request must not be null")

    if !inventoryGateway.hasStock(request.sku, request.quantity) then
      return OrderFulfillmentFacade.FulfillmentResult.failure(
        s"Inventory unavailable for SKU ${request.sku}"
      )

    if !paymentGateway.charge(request.customerId, request.amount) then
      return OrderFulfillmentFacade.FulfillmentResult.failure(
        s"Payment failed for customer ${request.customerId}"
      )

    val trackingId = shippingGateway.scheduleShipment(request.customerId, request.shippingAddress)
    notificationGateway.sendConfirmation(request.customerId, request.sku, trackingId)

    OrderFulfillmentFacade.FulfillmentResult.success(
      "Order placed successfully",
      trackingId
    )

object OrderFulfillmentFacade:
  trait InventoryGateway:
    def hasStock(sku: String, quantity: Int): Boolean

  trait PaymentGateway:
    def charge(customerId: String, amount: BigDecimal): Boolean

  trait ShippingGateway:
    def scheduleShipment(customerId: String, shippingAddress: String): String

  trait NotificationGateway:
    def sendConfirmation(customerId: String, sku: String, trackingId: String): Unit

  case class OrderRequest(
      customerId: String,
      sku: String,
      quantity: Int,
      amount: BigDecimal,
      shippingAddress: String
  ):
    require(customerId != null && customerId.nonEmpty, "Customer ID must not be blank")
    require(sku != null && sku.nonEmpty, "SKU must not be blank")
    require(quantity > 0, "Quantity must be positive")
    require(
      amount != null && amount.compareTo(BigDecimal.ZERO) > 0,
      "Amount must be greater than zero"
    )
    require(
      shippingAddress != null && shippingAddress.nonEmpty,
      "Shipping address must not be blank"
    )

  case class FulfillmentResult(success: Boolean, message: String, trackingId: Option[String])

  object FulfillmentResult:
    def success(message: String, trackingId: String): FulfillmentResult =
      FulfillmentResult(true, message, Some(trackingId))

    def failure(message: String): FulfillmentResult =
      FulfillmentResult(false, message, None)

  class InventoryService extends InventoryGateway:
    override def hasStock(sku: String, quantity: Int): Boolean = sku == "SKU-42" && quantity <= 5

  class PaymentService extends PaymentGateway:
    override def charge(customerId: String, amount: BigDecimal): Boolean =
      customerId != "blocked-customer" && amount.compareTo(BigDecimal.ZERO) > 0

  class ShippingService extends ShippingGateway:
    override def scheduleShipment(customerId: String, shippingAddress: String): String =
      s"TRACK-${customerId.toUpperCase}-${shippingAddress.hashCode()}"

  class NotificationService extends NotificationGateway:
    override def sendConfirmation(customerId: String, sku: String, trackingId: String): Unit =
      println(s"Sending confirmation to $customerId for $sku with tracking ID $trackingId")
