package io.github.sps23.spring.scopes;

import java.util.List;
import org.springframework.stereotype.Service;

/**
 * The classic <strong>prototype-injected-into-a-singleton</strong> bug,
 * reproduced with real Spring wiring so it can be demonstrated (and tested) end
 * to end.
 *
 * <p>
 * {@code StaleReportService} is itself a singleton (the default scope). It
 * takes a {@link ReportBuilder} — a prototype-scoped bean — as a constructor
 * argument. Spring resolves that constructor argument <strong>once</strong>,
 * when this singleton is created during context startup, and injects a single
 * {@code ReportBuilder} instance. Every subsequent call to
 * {@link #generateReport(List)} reuses that same, now-stale instance, so
 * sections accumulate across unrelated calls instead of starting fresh.
 *
 * <p>
 * See {@link ReportService} for the fix using
 * {@link org.springframework.beans.factory.ObjectProvider}.
 */
@Service
public class StaleReportService {

    // ⚠️ Resolved once at startup — this is NOT a fresh prototype per call.
    private final ReportBuilder builder;

    public StaleReportService(ReportBuilder builder) {
        this.builder = builder;
    }

    /**
     * Appends the given sections to the (stale, shared) report builder and renders
     * it.
     *
     * @param sections
     *            sections to append for this call
     * @return the rendered report, which may include sections from previous,
     *         unrelated calls — this is the bug
     */
    public String generateReport(List<String> sections) {
        sections.forEach(builder::addSection);
        return builder.build();
    }
}
