package io.github.sps23.spring.ioc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * Unit tests for {@link OrderService} that prove the code samples from the
 * "Inversion of Control and Dependency Injection in Spring" blog post work.
 *
 * <p>
 * Key point: every test uses {@code new OrderService(mock, mock, mock)} — no
 * Spring context, no application server, no database, no real email. This is
 * the testability payoff of constructor injection described in the blog.
 *
 * @see <a href=
 *      "https://sps23.github.io/java-for-scala-devs/blog/2026/05/25/spring-ioc-and-dependency-injection.html">Blog
 *      post: IoC and Dependency Injection in Spring</a>
 */
@DisplayName("OrderService — constructor injection testability demo")
class OrderServiceTest {

    private PaymentGateway mockGateway;
    private EmailSender mockEmailSender;
    private OrderRepository mockRepository;
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        // Create fresh Mockito mocks for each test — pure isolation.
        // No Spring context required because OrderService uses constructor injection.
        mockGateway = mock(PaymentGateway.class);
        mockEmailSender = mock(EmailSender.class);
        mockRepository = mock(OrderRepository.class);
        orderService = new OrderService(mockGateway, mockEmailSender, mockRepository);
    }

    @Nested
    @DisplayName("Successful order placement")
    class SuccessfulOrderTests {

        @Test
        @DisplayName("Should charge payment, save order, and send confirmation email")
        void shouldChargePaymentAndSendConfirmationEmail() {
            // Arrange
            var order = new Order("order-1", "cust-42", new BigDecimal("99.99"),
                    "test@example.com");
            var paymentResult = new PaymentResult("txn_abc123", new BigDecimal("99.99"));
            when(mockGateway.charge(order.amount(), order.customerId())).thenReturn(paymentResult);

            // Act
            var confirmation = orderService.placeOrder(order);

            // Assert
            assertEquals("order-1", confirmation.orderId());
            assertEquals("txn_abc123", confirmation.transactionId());

            verify(mockRepository).save(order);
            verify(mockEmailSender).send("test@example.com", "Your order is confirmed!");
        }

        @Test
        @DisplayName("Should return confirmation with payment transaction id")
        void shouldReturnConfirmationWithTransactionId() {
            var order = new Order("order-99", "cust-7", new BigDecimal("250.00"),
                    "buyer@example.com");
            when(mockGateway.charge(any(), any()))
                    .thenReturn(new PaymentResult("txn_xyz789", new BigDecimal("250.00")));

            var confirmation = orderService.placeOrder(order);

            assertEquals("order-99", confirmation.orderId());
            assertEquals("txn_xyz789", confirmation.transactionId());
        }

        @Test
        @DisplayName("Should send confirmation to the customer's email address")
        void shouldSendConfirmationToCustomerEmail() {
            var order = new Order("order-2", "cust-1", new BigDecimal("10.00"),
                    "customer@company.org");
            when(mockGateway.charge(any(), any()))
                    .thenReturn(new PaymentResult("txn_1", new BigDecimal("10.00")));

            orderService.placeOrder(order);

            var emailCaptor = ArgumentCaptor.forClass(String.class);
            verify(mockEmailSender).send(emailCaptor.capture(), any());
            assertEquals("customer@company.org", emailCaptor.getValue());
        }
    }

    @Nested
    @DisplayName("Payment failure handling")
    class PaymentFailureTests {

        @Test
        @DisplayName("Should propagate PaymentException when gateway rejects the charge")
        void shouldPropagatePaymentFailure() {
            var order = new Order("order-2", "cust-99", new BigDecimal("500.00"),
                    "test@example.com");
            when(mockGateway.charge(any(), any())).thenThrow(new PaymentException("Card declined"));

            assertThrows(PaymentException.class, () -> orderService.placeOrder(order));
        }

        @Test
        @DisplayName("Should NOT save order when payment fails")
        void shouldNotSaveOrderWhenPaymentFails() {
            var order = new Order("order-3", "cust-5", new BigDecimal("50.00"), "test@example.com");
            when(mockGateway.charge(any(), any()))
                    .thenThrow(new PaymentException("Insufficient funds"));

            assertThrows(PaymentException.class, () -> orderService.placeOrder(order));

            // Repository must not have been called — no orphaned orders
            verify(mockRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should NOT send email when payment fails")
        void shouldNotSendEmailWhenPaymentFails() {
            var order = new Order("order-4", "cust-6", new BigDecimal("75.00"), "test@example.com");
            when(mockGateway.charge(any(), any())).thenThrow(new PaymentException("Card expired"));

            assertThrows(PaymentException.class, () -> orderService.placeOrder(order));

            // No false "Your order is confirmed!" email
            verify(mockEmailSender, never()).send(any(), any());
        }
    }

    @Nested
    @DisplayName("Constructor injection with stub implementations (no Mockito)")
    class StubImplementationTests {

        @Test
        @DisplayName("Order saved in repository after successful payment")
        void orderSavedAfterSuccessfulPayment() {
            // Use hand-rolled stubs instead of Mockito — demonstrates that
            // constructor injection makes testing easy even without mocking frameworks
            var stubRepo = new InMemoryOrderRepository();
            var stubEmail = new StubEmailSender();
            var fakeGateway = new FakePaymentGateway("txn_stub_");
            var service = new OrderService(fakeGateway, stubEmail, stubRepo);

            var order = new Order("ord-10", "cust-1", new BigDecimal("20.00"), "a@b.com");
            service.placeOrder(order);

            assertTrue(stubRepo.findById("ord-10").isPresent());
            assertEquals(1, stubEmail.sentEmails().size());
            assertEquals("a@b.com", stubEmail.sentEmails().get(0).to());
        }

        @Test
        @DisplayName("No side effects when payment gateway fails")
        void noSideEffectsOnPaymentFailure() {
            var stubRepo = new InMemoryOrderRepository();
            var stubEmail = new StubEmailSender();
            var fakeGateway = new FakePaymentGateway("txn_").willFail("Card blocked");
            var service = new OrderService(fakeGateway, stubEmail, stubRepo);

            var order = new Order("ord-11", "cust-2", new BigDecimal("100.00"), "x@y.com");
            assertThrows(PaymentException.class, () -> service.placeOrder(order));

            assertTrue(stubRepo.findAll().isEmpty(), "No order should be persisted");
            assertTrue(stubEmail.sentEmails().isEmpty(), "No email should be sent");
        }
    }
}
