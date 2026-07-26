package io.github.sps23.spring.scopes;

import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;

/**
 * The correct way to consume a {@code prototype}-scoped bean
 * ({@link ReportBuilder}) from a {@code singleton} service, using
 * {@link ObjectProvider}.
 *
 * <p>
 * Instead of injecting a {@code ReportBuilder} instance directly (see
 * {@link StaleReportService} for what goes wrong), this service injects an
 * {@code ObjectProvider<ReportBuilder>} — a factory. Calling
 * {@link ObjectProvider#getObject()} asks the container for a fresh prototype
 * instance every single time, so each call to {@link #generateReport(List)}
 * starts from a clean slate.
 *
 * <p>
 * Equivalent alternatives in Spring: an {@code @Lookup}-annotated abstract
 * method (Spring generates a CGLIB subclass that overrides it to fetch a new
 * bean per call), or the JSR-330 {@code jakarta.inject.Provider<T>}. Calling
 * {@code ApplicationContext.getBean(...)} directly also works but couples your
 * code to the container and should be a last resort.
 */
@Service
public class ReportService {

    private final ObjectProvider<ReportBuilder> builderFactory;

    public ReportService(ObjectProvider<ReportBuilder> builderFactory) {
        this.builderFactory = builderFactory;
    }

    /**
     * Builds a fresh report from the given sections. Because a brand-new
     * {@link ReportBuilder} is fetched on every call, sections never leak between
     * unrelated calls.
     *
     * @param sections
     *            sections to include in this report
     * @return the rendered report, containing only the given sections
     */
    public String generateReport(List<String> sections) {
        var builder = builderFactory.getObject();
        sections.forEach(builder::addSection);
        return builder.build();
    }
}
