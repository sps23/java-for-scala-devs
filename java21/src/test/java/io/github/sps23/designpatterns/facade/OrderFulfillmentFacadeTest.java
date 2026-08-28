package io.github.sps23.designpatterns.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("OrderFulfillmentFacade Java 21 Tests")
class OrderFulfillmentFacadeTest {

    @Test
    @DisplayName("Should place an order through all subsystems")
    void shouldPlaceOrderThroughFacade() {
        NotificationRecorder notificationRecorder = new NotificationRecorder();
        OrderFulfillmentFacade facade = new OrderFulfillmentFacade(new InventoryAlwaysAvailable(),
                new PaymentAlwaysAccepted(), new ShippingAlwaysScheduled(), notificationRecorder);

        var request = new OrderFulfillmentFacade.OrderRequest("customer-42", "SKU-42", 2,
                new BigDecimal("149.99"), "12 Main Street, London");

        OrderFulfillmentFacade.FulfillmentResult result = facade.placeOrder(request);

        assertTrue(result.success());
        assertEquals("Order placed successfully", result.message());
        assertNotNull(result.trackingId());
        assertTrue(notificationRecorder.sent);
    }

    @Test
    @DisplayName("Should reject when inventory is insufficient")
    void shouldRejectWhenInventoryIsInsufficient() {
        OrderFulfillmentFacade facade = new OrderFulfillmentFacade(new InventoryInsufficient(),
                new PaymentAlwaysAccepted(), new ShippingAlwaysScheduled(),
                new NotificationRecorder());

        var request = new OrderFulfillmentFacade.OrderRequest("customer-99", "SKU-999", 10,
                new BigDecimal("40.00"), "99 Market Road, Berlin");

        OrderFulfillmentFacade.FulfillmentResult result = facade.placeOrder(request);

        assertFalse(result.success());
        assertEquals("Inventory unavailable for SKU SKU-999", result.message());
    }

    @Test
    @DisplayName("Should reject when payment is declined")
    void shouldRejectWhenPaymentIsDeclined() {
        OrderFulfillmentFacade facade = new OrderFulfillmentFacade(new InventoryAlwaysAvailable(),
                new PaymentRejected(), new ShippingAlwaysScheduled(), new NotificationRecorder());

        var request = new OrderFulfillmentFacade.OrderRequest("blocked-customer", "SKU-42", 1,
                new BigDecimal("10.00"), "1 High Street, Paris");

        OrderFulfillmentFacade.FulfillmentResult result = facade.placeOrder(request);

        assertFalse(result.success());
        assertEquals("Payment failed for customer blocked-customer", result.message());
    }

    private static final class InventoryAlwaysAvailable
            implements
                OrderFulfillmentFacade.InventoryGateway {
        @Override
        public boolean hasStock(String sku, int quantity) {
            return true;
        }
    }

    private static final class InventoryInsufficient
            implements
                OrderFulfillmentFacade.InventoryGateway {
        @Override
        public boolean hasStock(String sku, int quantity) {
            return false;
        }
    }

    private static final class PaymentAlwaysAccepted
            implements
                OrderFulfillmentFacade.PaymentGateway {
        @Override
        public boolean charge(String customerId, BigDecimal amount) {
            return true;
        }
    }

    private static final class PaymentRejected implements OrderFulfillmentFacade.PaymentGateway {
        @Override
        public boolean charge(String customerId, BigDecimal amount) {
            return false;
        }
    }

    private static final class ShippingAlwaysScheduled
            implements
                OrderFulfillmentFacade.ShippingGateway {
        @Override
        public String scheduleShipment(String customerId, String shippingAddress) {
            return "TRACK-123";
        }
    }

    private static final class NotificationRecorder
            implements
                OrderFulfillmentFacade.NotificationGateway {
        private boolean sent;

        @Override
        public void sendConfirmation(String customerId, String sku, String trackingId) {
            sent = true;
        }
    }
}
