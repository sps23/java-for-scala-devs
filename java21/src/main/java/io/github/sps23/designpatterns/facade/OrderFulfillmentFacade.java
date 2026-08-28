package io.github.sps23.designpatterns.facade;

import java.math.BigDecimal;
import java.util.Objects;

public final class OrderFulfillmentFacade {
    private final InventoryGateway inventoryGateway;
    private final PaymentGateway paymentGateway;
    private final ShippingGateway shippingGateway;
    private final NotificationGateway notificationGateway;

    public OrderFulfillmentFacade(InventoryGateway inventoryGateway, PaymentGateway paymentGateway,
            ShippingGateway shippingGateway, NotificationGateway notificationGateway) {
        this.inventoryGateway = Objects.requireNonNull(inventoryGateway,
                "Inventory gateway must not be null");
        this.paymentGateway = Objects.requireNonNull(paymentGateway,
                "Payment gateway must not be null");
        this.shippingGateway = Objects.requireNonNull(shippingGateway,
                "Shipping gateway must not be null");
        this.notificationGateway = Objects.requireNonNull(notificationGateway,
                "Notification gateway must not be null");
    }

    public FulfillmentResult placeOrder(OrderRequest request) {
        Objects.requireNonNull(request, "Order request must not be null");

        if (!inventoryGateway.hasStock(request.sku(), request.quantity())) {
            return FulfillmentResult.failure("Inventory unavailable for SKU " + request.sku());
        }

        if (!paymentGateway.charge(request.customerId(), request.amount())) {
            return FulfillmentResult.failure("Payment failed for customer " + request.customerId());
        }

        String trackingId = shippingGateway.scheduleShipment(request.customerId(),
                request.shippingAddress());
        notificationGateway.sendConfirmation(request.customerId(), request.sku(), trackingId);

        return FulfillmentResult.success("Order placed successfully", trackingId);
    }

    public interface InventoryGateway {
        boolean hasStock(String sku, int quantity);
    }

    public interface PaymentGateway {
        boolean charge(String customerId, BigDecimal amount);
    }

    public interface ShippingGateway {
        String scheduleShipment(String customerId, String shippingAddress);
    }

    public interface NotificationGateway {
        void sendConfirmation(String customerId, String sku, String trackingId);
    }

    public record OrderRequest(String customerId, String sku, int quantity, BigDecimal amount,
            String shippingAddress) {

        public OrderRequest {
            Objects.requireNonNull(customerId, "Customer ID must not be null");
            Objects.requireNonNull(sku, "SKU must not be null");
            Objects.requireNonNull(amount, "Amount must not be null");
            Objects.requireNonNull(shippingAddress, "Shipping address must not be null");
            if (quantity <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be greater than zero");
            }
        }
    }

    public record FulfillmentResult(boolean success, String message, String trackingId) {
        public static FulfillmentResult success(String message, String trackingId) {
            return new FulfillmentResult(true, message, trackingId);
        }

        public static FulfillmentResult failure(String message) {
            return new FulfillmentResult(false, message, null);
        }
    }

    public static final class InventoryService implements InventoryGateway {
        @Override
        public boolean hasStock(String sku, int quantity) {
            return "SKU-42".equals(sku) && quantity <= 5;
        }
    }

    public static final class PaymentService implements PaymentGateway {
        @Override
        public boolean charge(String customerId, BigDecimal amount) {
            return !"blocked-customer".equals(customerId) && amount.compareTo(BigDecimal.ZERO) > 0;
        }
    }

    public static final class ShippingService implements ShippingGateway {
        @Override
        public String scheduleShipment(String customerId, String shippingAddress) {
            if (shippingAddress == null || shippingAddress.isBlank()) {
                throw new IllegalArgumentException("Shipping address must not be blank");
            }
            return "TRACK-" + customerId.toUpperCase() + "-" + shippingAddress.hashCode();
        }
    }

    public static final class NotificationService implements NotificationGateway {
        @Override
        public void sendConfirmation(String customerId, String sku, String trackingId) {
            System.out.println("Sending confirmation to " + customerId + " for " + sku
                    + " with tracking ID " + trackingId);
        }
    }
}
