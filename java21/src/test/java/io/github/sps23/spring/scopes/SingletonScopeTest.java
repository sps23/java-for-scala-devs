package io.github.sps23.spring.scopes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Proves the default <strong>singleton</strong> scope with a real Spring
 * {@code ApplicationContext}: every {@code getBean()} call returns the exact
 * same {@link OrderVolumeTracker} instance, and mutable state is therefore
 * shared across every caller.
 */
class SingletonScopeTest {

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
    @DisplayName("getBean() returns the same instance every time")
    void returnsTheSameInstanceOnEveryLookup() {
        var first = context.getBean(OrderVolumeTracker.class);
        var second = context.getBean(OrderVolumeTracker.class);

        assertSame(first, second);
    }

    @Test
    @DisplayName("mutable state is shared across every caller of the singleton")
    void sharesMutableStateAcrossEveryCaller() {
        var trackerSeenByCallerA = context.getBean(OrderVolumeTracker.class);
        var trackerSeenByCallerB = context.getBean(OrderVolumeTracker.class);

        trackerSeenByCallerA.recordOrderProcessed();
        trackerSeenByCallerB.recordOrderProcessed();

        // Both "callers" observe the same accumulated count, because they share
        // one singleton instance - this is the singleton contract, and the risk
        // it carries if the shared state isn't made thread-safe.
        assertEquals(2, trackerSeenByCallerA.ordersProcessed());
        assertEquals(2, trackerSeenByCallerB.ordersProcessed());
    }
}
