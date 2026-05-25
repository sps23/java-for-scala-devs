package io.github.sps23.spring.ioc;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Audit handler that increments a counter per event type (simulating a metrics
 * system like Micrometer or Prometheus).
 *
 * <p>
 * In a Spring application this would be annotated with
 * {@code @Component @Order(2)}.
 */
public class MetricsAuditHandler implements AuditHandler {

    private final AtomicInteger eventCount = new AtomicInteger(0);

    @Override
    public void handle(AuditEvent event) {
        eventCount.incrementAndGet();
    }

    /**
     * Total number of audit events processed.
     *
     * @return event count
     */
    public int eventCount() {
        return eventCount.get();
    }
}
