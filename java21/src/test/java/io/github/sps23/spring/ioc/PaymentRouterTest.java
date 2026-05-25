package io.github.sps23.spring.ioc;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link PaymentRouter} demonstrating the <em>inject a Map of bean
 * names to implementations</em> pattern from the blog.
 *
 * <p>
 * In production Spring auto-populates a {@code Map<String, PaymentGateway>}
 * with every {@code PaymentGateway} bean in the context. Here we build the map
 * ourselves — constructor injection keeps tests independent of the container.
 */
@DisplayName("PaymentRouter — Map<String, PaymentGateway> injection demo")
class PaymentRouterTest {

    private final FakePaymentGateway stripeGateway = new FakePaymentGateway("txn_stripe_");
    private final FakePaymentGateway paypalGateway = new FakePaymentGateway("txn_paypal_");
    private final PaymentRouter router = new PaymentRouter(
            Map.of("stripe", stripeGateway, "paypal", paypalGateway));

    @Nested
    @DisplayName("Routing to correct gateway")
    class RoutingTests {

        @Test
        @DisplayName("Should route to Stripe gateway when provider is 'stripe'")
        void shouldRouteToStripe() {
            var result = router.route("stripe", new BigDecimal("50.00"), "cust-1");
            assertTrue(result.transactionId().startsWith("txn_stripe_"));
        }

        @Test
        @DisplayName("Should route to PayPal gateway when provider is 'paypal'")
        void shouldRouteToPayPal() {
            var result = router.route("paypal", new BigDecimal("75.00"), "cust-2");
            assertTrue(result.transactionId().startsWith("txn_paypal_"));
        }

        @Test
        @DisplayName("Should pass correct amount to the chosen gateway")
        void shouldPassCorrectAmount() {
            var amount = new BigDecimal("123.45");
            var result = router.route("stripe", amount, "cust-3");
            assertEquals(amount, result.amount());
        }
    }

    @Nested
    @DisplayName("Unknown provider")
    class UnknownProviderTests {

        @Test
        @DisplayName("Should throw IllegalArgumentException for unknown provider")
        void shouldThrowForUnknownProvider() {
            assertThrows(IllegalArgumentException.class,
                    () -> router.route("bitcoin", new BigDecimal("1.00"), "cust-0"));
        }

        @Test
        @DisplayName("Error message should name the unknown provider")
        void errorMessageShouldNameProvider() {
            var ex = assertThrows(IllegalArgumentException.class,
                    () -> router.route("venmo", new BigDecimal("10.00"), "cust-0"));
            assertTrue(ex.getMessage().contains("venmo"));
        }
    }

    @Nested
    @DisplayName("Available providers")
    class AvailableProvidersTests {

        @Test
        @DisplayName("Should report all registered providers")
        void shouldReportAvailableProviders() {
            var providers = router.availableProviders();
            assertTrue(providers.contains("stripe"));
            assertTrue(providers.contains("paypal"));
            assertEquals(2, providers.size());
        }
    }

    @Nested
    @DisplayName("Gateway failure propagation")
    class GatewayFailureTests {

        @Test
        @DisplayName("Should propagate PaymentException from the chosen gateway")
        void shouldPropagateGatewayFailure() {
            var failingRouter = new PaymentRouter(
                    Map.of("stripe", new FakePaymentGateway("").willFail("Card declined")));

            assertThrows(PaymentException.class,
                    () -> failingRouter.route("stripe", new BigDecimal("100.00"), "cust-9"));
        }
    }
}
