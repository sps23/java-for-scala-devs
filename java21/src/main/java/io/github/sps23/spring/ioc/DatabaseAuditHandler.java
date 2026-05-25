package io.github.sps23.spring.ioc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Audit handler that "persists" events to a database (simulated here with an
 * in-memory list).
 *
 * <p>
 * In a Spring application this would be annotated with
 * {@code @Component @Order(3)}.
 */
public class DatabaseAuditHandler implements AuditHandler {

    private final List<AuditEvent> persisted = new ArrayList<>();

    @Override
    public void handle(AuditEvent event) {
        persisted.add(event);
    }

    /**
     * Returns all persisted audit events — useful for assertions in tests.
     *
     * @return unmodifiable list of persisted events
     */
    public List<AuditEvent> persisted() {
        return Collections.unmodifiableList(persisted);
    }
}
