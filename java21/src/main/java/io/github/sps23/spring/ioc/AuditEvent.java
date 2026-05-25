package io.github.sps23.spring.ioc;

/**
 * An audit event describing something notable that happened in the system.
 *
 * <p>
 * Used to demonstrate injecting a {@code List<AuditHandler>} — Spring collects
 * all beans implementing {@link AuditHandler} and injects them as an ordered
 * list.
 */
public record AuditEvent(String type, String message, String userId) {
}
