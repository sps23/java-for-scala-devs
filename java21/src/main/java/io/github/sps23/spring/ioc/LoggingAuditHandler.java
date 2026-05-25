package io.github.sps23.spring.ioc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Audit handler that records events in a log (in-memory list for demo/test
 * purposes; a real implementation would write to SLF4J or similar).
 *
 * <p>
 * In a Spring application this would be annotated with
 * {@code @Component @Order(1)} so it runs before other handlers.
 */
public class LoggingAuditHandler implements AuditHandler {

    /**
     * A logged audit entry.
     */
    public record LogEntry(Instant timestamp, String type, String message, String userId) {
    }

    private final List<LogEntry> log = new ArrayList<>();

    @Override
    public void handle(AuditEvent event) {
        log.add(new LogEntry(Instant.now(), event.type(), event.message(), event.userId()));
    }

    /**
     * Returns all log entries — useful for assertions in tests.
     *
     * @return unmodifiable list of log entries
     */
    public List<LogEntry> entries() {
        return Collections.unmodifiableList(log);
    }
}
