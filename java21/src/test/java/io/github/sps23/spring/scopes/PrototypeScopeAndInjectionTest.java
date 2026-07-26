package io.github.sps23.spring.scopes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Proves the {@code prototype} scope contract, the "stale prototype in a
 * singleton" trap, and the
 * {@link org.springframework.beans.factory.ObjectProvider} fix — all against a
 * real Spring {@code ApplicationContext}.
 */
class PrototypeScopeAndInjectionTest {

    private AnnotationConfigApplicationContext context;

    @BeforeEach
    void startContext() {
        context = new AnnotationConfigApplicationContext(ScopesLifecycleConfig.class);
    }

    @AfterEach
    void stopContext() {
        context.close();
    }

    @Test
    @DisplayName("getBean() returns a new instance every time")
    void returnsANewInstanceOnEveryLookup() {
        var first = context.getBean(ReportBuilder.class);
        var second = context.getBean(ReportBuilder.class);

        assertNotSame(first, second);
    }

    @Test
    @DisplayName("constructor-injecting a prototype into a singleton leaks state between calls")
    void constructorInjectingAPrototypeIntoASingletonLeaksStateBetweenCalls() {
        var service = context.getBean(StaleReportService.class);

        var firstReport = service.generateReport(List.of("Q1 summary"));
        var secondReport = service.generateReport(List.of("Q2 summary"));

        // The bug: the second report also contains the first call's section,
        // because StaleReportService captured one ReportBuilder instance at
        // startup and keeps reusing it.
        assertEquals("Q1 summary", firstReport);
        assertEquals("Q1 summary\n\nQ2 summary", secondReport);
    }

    @Test
    @DisplayName("ObjectProvider fetches a fresh prototype on every call")
    void objectProviderFetchesAFreshPrototypeOnEveryCall() {
        var service = context.getBean(ReportService.class);

        var firstReport = service.generateReport(List.of("Q1 summary"));
        var secondReport = service.generateReport(List.of("Q2 summary"));

        // Fixed: each call gets its own fresh ReportBuilder, so reports never
        // leak sections from earlier, unrelated calls.
        assertEquals("Q1 summary", firstReport);
        assertEquals("Q2 summary", secondReport);
    }

    @Test
    @DisplayName("@Lookup method injection also fetches a fresh prototype on every call")
    void lookupMethodInjectionAlsoFetchesAFreshPrototypeOnEveryCall() {
        var service = context.getBean(LookupReportService.class);

        var firstReport = service.generateReport("Q1 summary");
        var secondReport = service.generateReport("Q2 summary");

        assertEquals("Q1 summary", firstReport);
        assertEquals("Q2 summary", secondReport);
    }
}
