package io.github.sps23.spring.scopes;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Demonstrates the interface-based lifecycle callbacks {@link InitializingBean}
 * and {@link DisposableBean}.
 *
 * <p>
 * These interfaces predate {@code @PostConstruct}/{@code @PreDestroy} and are
 * Spring-specific, which couples this class directly to the Spring API.
 * {@code @PostConstruct}/{@code @PreDestroy} (see
 * {@link ManagedConnectionPool}) are generally preferred today because they
 * keep the class portable, but the interfaces are still fully supported and you
 * will encounter them in existing codebases — Spring calls
 * {@code afterPropertiesSet()} and {@code destroy()} at exactly the same points
 * in the lifecycle as the annotation-based callbacks.
 */
@Component
public class LegacyCacheManager implements InitializingBean, DisposableBean {

    private final AtomicBoolean warmedUp = new AtomicBoolean(false);
    private final AtomicBoolean flushed = new AtomicBoolean(false);

    @Override
    public void afterPropertiesSet() {
        // Runs once, right after dependency injection completes.
        warmedUp.set(true);
    }

    @Override
    public void destroy() {
        // Runs once, during container shutdown.
        flushed.set(true);
    }

    public boolean isWarmedUp() {
        return warmedUp.get();
    }

    public boolean isFlushed() {
        return flushed.get();
    }
}
