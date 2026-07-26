package io.github.sps23.spring.scopes;

import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

/**
 * An alternative to {@link ReportService}'s {@code ObjectProvider} approach:
 * the {@code @Lookup} method injection pattern.
 *
 * <p>
 * The class must be non-final and the annotated method non-private so Spring
 * can generate a CGLIB subclass at runtime that overrides
 * {@link #newReportBuilder()} to fetch a brand-new prototype bean from the
 * container on every call — the method body below is never actually executed;
 * Spring replaces it entirely.
 */
@Component
public class LookupReportService {

    /**
     * Overridden by Spring's generated subclass to return a fresh
     * {@link ReportBuilder} prototype instance on every call.
     *
     * @return a new {@link ReportBuilder}
     */
    @Lookup
    protected ReportBuilder newReportBuilder() {
        // Never executed - Spring's CGLIB subclass overrides this method.
        throw new UnsupportedOperationException("Overridden by Spring at runtime");
    }

    public String generateReport(String section) {
        var builder = newReportBuilder();
        builder.addSection(section);
        return builder.build();
    }
}
