package io.github.sps23.spring.scopes;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * A real Spring-managed {@code @Component} explicitly scoped as
 * {@code prototype}.
 *
 * <p>
 * Every time this bean is requested from the {@code ApplicationContext} (via
 * {@code getBean()}, constructor injection resolved at creation time, or an
 * {@link org.springframework.beans.factory.ObjectProvider}), Spring creates a
 * <strong>brand-new instance</strong>. Unlike singleton beans, prototypes are
 * not cached — the container hands the instance to the caller and, from that
 * point on, forgets about it entirely (no {@code @PreDestroy} callback will
 * ever be invoked on it).
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ReportBuilder {

    // Safe: each ReportBuilder instance gets its own list, because each
    // instance is brand new.
    private final List<String> sections = new ArrayList<>();

    /**
     * Appends a section to the report being built.
     *
     * @param content
     *            section text to append
     */
    public void addSection(String content) {
        sections.add(content);
    }

    /**
     * Renders all accumulated sections into the final report text.
     *
     * @return the assembled report
     */
    public String build() {
        return String.join("\n\n", sections);
    }
}
