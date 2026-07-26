package io.github.sps23.spring.scopes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Proves that all three lifecycle callback styles fire at the correct point
 * relative to {@code context.refresh()} (implicit in the
 * {@link AnnotationConfigApplicationContext} constructor) and
 * {@code context.close()}:
 *
 * <ul>
 * <li>{@code @PostConstruct}/{@code @PreDestroy}
 * ({@link ManagedConnectionPool})</li>
 * <li>{@code InitializingBean}/{@code DisposableBean}
 * ({@link LegacyCacheManager})</li>
 * <li>{@code @Bean(initMethod, destroyMethod)} for a plain POJO
 * ({@link EmbeddedMessageBroker})</li>
 * </ul>
 */
class LifecycleCallbacksTest {

    @Test
    @DisplayName("@PostConstruct/@PreDestroy run at context startup/shutdown")
    void postConstructAndPreDestroyRunAtStartupAndShutdown() {
        var context = new AnnotationConfigApplicationContext(ScopesLifecycleConfig.class);
        var pool = context.getBean(ManagedConnectionPool.class);

        assertTrue(pool.isOpen(), "pool should be opened by @PostConstruct before use");
        assertEquals(5, pool.availableConnections());

        context.close();

        assertFalse(pool.isOpen(), "pool should be closed by @PreDestroy on context shutdown");
    }

    @Test
    @DisplayName("InitializingBean/DisposableBean run at context startup/shutdown")
    void initializingBeanAndDisposableBeanRunAtStartupAndShutdown() {
        var context = new AnnotationConfigApplicationContext(ScopesLifecycleConfig.class);
        var cacheManager = context.getBean(LegacyCacheManager.class);

        assertTrue(cacheManager.isWarmedUp(), "afterPropertiesSet() should run before use");
        assertFalse(cacheManager.isFlushed(), "destroy() should not have run yet");

        context.close();

        assertTrue(cacheManager.isFlushed(), "destroy() should run on context shutdown");
    }

    @Test
    @DisplayName("@Bean(initMethod, destroyMethod) drives lifecycle for a plain POJO")
    void beanInitAndDestroyMethodsRunAtStartupAndShutdownForAPlainPojo() {
        var context = new AnnotationConfigApplicationContext(ScopesLifecycleConfig.class);
        var broker = context.getBean(EmbeddedMessageBroker.class);

        assertTrue(broker.isRunning(), "start() should have been invoked automatically");

        context.close();

        assertFalse(broker.isRunning(), "stop() should have been invoked automatically");
    }
}
