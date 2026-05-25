package io.github.sps23.spring.ioc;

import java.util.List;

/**
 * Dispatches audit events to every registered handler.
 *
 * <p>
 * Demonstrates injecting a <strong>List of all implementations</strong> of an
 * interface — a powerful Spring pattern for plugin-style architectures.
 *
 * <p>
 * In a Spring application the container collects all beans implementing
 * {@link AuditHandler} and injects them in {@code @Order} sequence. In tests
 * you just pass a plain {@code List}:
 *
 * <pre>{@code
 * var service = new AuditService(List.of(new LoggingAuditHandler(), new MetricsAuditHandler()));
 * }</pre>
 */
public class AuditService {

    private final List<AuditHandler> handlers;

    public AuditService(List<AuditHandler> handlers) {
        this.handlers = List.copyOf(handlers); // defensive copy
    }

    /**
     * Dispatches the event to every handler in order.
     *
     * @param event
     *            the event to audit
     */
    public void audit(AuditEvent event) {
        handlers.forEach(h -> h.handle(event));
    }

    /**
     * Returns the number of registered handlers — useful for diagnostics.
     *
     * @return handler count
     */
    public int handlerCount() {
        return handlers.size();
    }
}
