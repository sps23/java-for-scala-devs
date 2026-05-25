package io.github.sps23.spring.ioc;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AuditService} demonstrating the <em>inject a List of all
 * implementations</em> pattern from the blog.
 *
 * <p>
 * In production, Spring would collect all {@code @Component} beans implementing
 * {@link AuditHandler} and inject them as an ordered list. Here we just
 * construct the list by hand — because constructor injection means no Spring
 * context is needed in tests.
 */
@DisplayName("AuditService — List<AuditHandler> injection demo")
class AuditServiceTest {

    @Nested
    @DisplayName("Dispatching to all handlers")
    class DispatchTests {

        @Test
        @DisplayName("Should dispatch event to every registered handler")
        void shouldDispatchToAllHandlers() {
            var loggingHandler = new LoggingAuditHandler();
            var metricsHandler = new MetricsAuditHandler();
            var dbHandler = new DatabaseAuditHandler();

            // This is exactly what Spring does: collect all AuditHandler beans
            // and inject them as a List — but here we wire it ourselves
            var service = new AuditService(List.of(loggingHandler, metricsHandler, dbHandler));

            var event = new AuditEvent("ORDER_PLACED", "Order ord-1 placed", "cust-42");
            service.audit(event);

            // All three handlers received the event
            assertEquals(1, loggingHandler.entries().size());
            assertEquals(1, metricsHandler.eventCount());
            assertEquals(1, dbHandler.persisted().size());
        }

        @Test
        @DisplayName("Should dispatch multiple events to all handlers in sequence")
        void shouldDispatchMultipleEvents() {
            var metricsHandler = new MetricsAuditHandler();
            var dbHandler = new DatabaseAuditHandler();
            var service = new AuditService(List.of(metricsHandler, dbHandler));

            service.audit(new AuditEvent("LOGIN", "User logged in", "user-1"));
            service.audit(new AuditEvent("PAYMENT", "Payment processed", "user-1"));
            service.audit(new AuditEvent("LOGOUT", "User logged out", "user-1"));

            assertEquals(3, metricsHandler.eventCount());
            assertEquals(3, dbHandler.persisted().size());
        }

        @Test
        @DisplayName("Should preserve handler execution order")
        void shouldPreserveHandlerOrder() {
            var executionOrder = new ArrayList<String>();
            // Anonymous handlers that record their execution order
            AuditHandler first = e -> executionOrder.add("first");
            AuditHandler second = e -> executionOrder.add("second");
            AuditHandler third = e -> executionOrder.add("third");

            var service = new AuditService(List.of(first, second, third));
            service.audit(new AuditEvent("TEST", "test event", "user-0"));

            assertEquals(List.of("first", "second", "third"), executionOrder);
        }
    }

    @Nested
    @DisplayName("Edge cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("Should work with zero handlers (empty list)")
        void shouldHandleEmptyHandlerList() {
            var service = new AuditService(List.of());
            assertEquals(0, service.handlerCount());
            // Should not throw
            assertDoesNotThrow(
                    () -> service.audit(new AuditEvent("X", "nothing to handle", "user-0")));
        }

        @Test
        @DisplayName("Should report correct handler count")
        void shouldReportHandlerCount() {
            var service = new AuditService(
                    List.of(new LoggingAuditHandler(), new MetricsAuditHandler()));
            assertEquals(2, service.handlerCount());
        }

        @Test
        @DisplayName("Handler list is immutable after construction")
        void handlerListIsImmutableAfterConstruction() {
            var mutableList = new ArrayList<AuditHandler>();
            mutableList.add(new LoggingAuditHandler());
            var service = new AuditService(mutableList);

            // Mutating the original list after construction must not affect the service
            mutableList.add(new MetricsAuditHandler());

            assertEquals(1, service.handlerCount());
        }
    }
}
