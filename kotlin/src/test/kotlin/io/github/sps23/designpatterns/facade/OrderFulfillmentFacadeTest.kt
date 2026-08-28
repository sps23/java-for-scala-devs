package io.github.sps23.designpatterns.facade

import io.github.sps23.designpatterns.facade.OrderFulfillmentFacade.OrderRequest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.math.BigDecimal

@DisplayName("OrderFulfillmentFacade Kotlin Tests")
class OrderFulfillmentFacadeTest {
    @Test
    fun `should place an order through all subsystems`() {
        val recorder = NotificationRecorder()
        val facade =
            OrderFulfillmentFacade(
                inventoryGateway = InventoryAlwaysAvailable(),
                paymentGateway = PaymentAlwaysAccepted(),
                shippingGateway = ShippingAlwaysScheduled(),
                notificationGateway = recorder,
            )

        val request =
            OrderRequest(
                customerId = "customer-42",
                sku = "SKU-42",
                quantity = 2,
                amount = BigDecimal("149.99"),
                shippingAddress = "12 Main Street, London",
            )

        val result = facade.placeOrder(request)

        assertTrue(result.success)
        assertEquals("Order placed successfully", result.message)
        assertNotNull(result.trackingId)
        assertTrue(recorder.sent)
    }

    @Test
    fun `should reject when inventory is insufficient`() {
        val facade =
            OrderFulfillmentFacade(
                inventoryGateway = InventoryInsufficient(),
                paymentGateway = PaymentAlwaysAccepted(),
                shippingGateway = ShippingAlwaysScheduled(),
                notificationGateway = NotificationRecorder(),
            )

        val result =
            facade.placeOrder(
                OrderRequest(
                    customerId = "customer-99",
                    sku = "SKU-999",
                    quantity = 10,
                    amount = BigDecimal("40.00"),
                    shippingAddress = "99 Market Road, Berlin",
                ),
            )

        assertFalse(result.success)
        assertEquals("Inventory unavailable for SKU SKU-999", result.message)
    }

    @Test
    fun `should reject when payment is declined`() {
        val facade =
            OrderFulfillmentFacade(
                inventoryGateway = InventoryAlwaysAvailable(),
                paymentGateway = PaymentRejected(),
                shippingGateway = ShippingAlwaysScheduled(),
                notificationGateway = NotificationRecorder(),
            )

        val result =
            facade.placeOrder(
                OrderRequest(
                    customerId = "blocked-customer",
                    sku = "SKU-42",
                    quantity = 1,
                    amount = BigDecimal("10.00"),
                    shippingAddress = "1 High Street, Paris",
                ),
            )

        assertFalse(result.success)
        assertEquals("Payment failed for customer blocked-customer", result.message)
    }

    private class InventoryAlwaysAvailable : OrderFulfillmentFacade.InventoryGateway {
        override fun hasStock(
            sku: String,
            quantity: Int,
        ): Boolean = true
    }

    private class InventoryInsufficient : OrderFulfillmentFacade.InventoryGateway {
        override fun hasStock(
            sku: String,
            quantity: Int,
        ): Boolean = false
    }

    private class PaymentAlwaysAccepted : OrderFulfillmentFacade.PaymentGateway {
        override fun charge(
            customerId: String,
            amount: BigDecimal,
        ): Boolean = true
    }

    private class PaymentRejected : OrderFulfillmentFacade.PaymentGateway {
        override fun charge(
            customerId: String,
            amount: BigDecimal,
        ): Boolean = false
    }

    private class ShippingAlwaysScheduled : OrderFulfillmentFacade.ShippingGateway {
        override fun scheduleShipment(
            customerId: String,
            shippingAddress: String,
        ): String = "TRACK-123"
    }

    private class NotificationRecorder : OrderFulfillmentFacade.NotificationGateway {
        var sent = false

        override fun sendConfirmation(
            customerId: String,
            sku: String,
            trackingId: String,
        ) {
            sent = true
        }
    }
}
