package io.github.sps23.designpatterns.facade

import java.math.BigDecimal

class OrderFulfillmentFacade(
    private val inventoryGateway: InventoryGateway,
    private val paymentGateway: PaymentGateway,
    private val shippingGateway: ShippingGateway,
    private val notificationGateway: NotificationGateway,
) {
    fun placeOrder(request: OrderRequest): FulfillmentResult {
        requireNotNull(request)

        if (!inventoryGateway.hasStock(request.sku, request.quantity)) {
            return FulfillmentResult.failure("Inventory unavailable for SKU ${request.sku}")
        }

        if (!paymentGateway.charge(request.customerId, request.amount)) {
            return FulfillmentResult.failure("Payment failed for customer ${request.customerId}")
        }

        val trackingId =
            shippingGateway.scheduleShipment(
                request.customerId,
                request.shippingAddress,
            )
        notificationGateway.sendConfirmation(request.customerId, request.sku, trackingId)

        return FulfillmentResult.success("Order placed successfully", trackingId)
    }

    interface InventoryGateway {
        fun hasStock(
            sku: String,
            quantity: Int,
        ): Boolean
    }

    interface PaymentGateway {
        fun charge(
            customerId: String,
            amount: BigDecimal,
        ): Boolean
    }

    interface ShippingGateway {
        fun scheduleShipment(
            customerId: String,
            shippingAddress: String,
        ): String
    }

    interface NotificationGateway {
        fun sendConfirmation(
            customerId: String,
            sku: String,
            trackingId: String,
        )
    }

    data class OrderRequest(
        val customerId: String,
        val sku: String,
        val quantity: Int,
        val amount: BigDecimal,
        val shippingAddress: String,
    ) {
        init {
            require(customerId.isNotBlank()) { "Customer ID must not be blank" }
            require(sku.isNotBlank()) { "SKU must not be blank" }
            require(quantity > 0) { "Quantity must be positive" }
            require(amount > BigDecimal.ZERO) { "Amount must be greater than zero" }
            require(shippingAddress.isNotBlank()) { "Shipping address must not be blank" }
        }
    }

    data class FulfillmentResult(val success: Boolean, val message: String, val trackingId: String?) {
        companion object {
            fun success(
                message: String,
                trackingId: String,
            ): FulfillmentResult = FulfillmentResult(true, message, trackingId)

            fun failure(message: String): FulfillmentResult = FulfillmentResult(false, message, null)
        }
    }

    class InventoryService : InventoryGateway {
        override fun hasStock(
            sku: String,
            quantity: Int,
        ): Boolean = sku == "SKU-42" && quantity <= 5
    }

    class PaymentService : PaymentGateway {
        override fun charge(
            customerId: String,
            amount: BigDecimal,
        ): Boolean = customerId != "blocked-customer" && amount > BigDecimal.ZERO
    }

    class ShippingService : ShippingGateway {
        override fun scheduleShipment(
            customerId: String,
            shippingAddress: String,
        ): String = "TRACK-${customerId.uppercase()}-${shippingAddress.hashCode()}"
    }

    class NotificationService : NotificationGateway {
        override fun sendConfirmation(
            customerId: String,
            sku: String,
            trackingId: String,
        ) {
            println("Sending confirmation to $customerId for $sku with tracking ID $trackingId")
        }
    }
}
