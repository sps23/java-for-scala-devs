package io.github.sps23.spring.ioc;

/**
 * Plugin interface for audit event processing.
 *
 * <p>
 * Multiple implementations can coexist in the same application. Spring collects
 * all beans implementing this interface and injects them as a
 * {@code List<AuditHandler>} into {@link AuditService}, ordered by
 * {@code @Order} annotation value.
 *
 * <p>
 * In tests you simply construct the list yourself — no Spring context needed.
 */
public interface AuditHandler {

    /**
     * Processes an audit event.
     *
     * @param event
     *            the event to handle
     */
    void handle(AuditEvent event);
}
