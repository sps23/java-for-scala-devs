package io.github.sps23.spring.scopes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Ties every example in this package together into a single, bootable
 * {@code ApplicationContext}.
 *
 * <p>
 * {@code @ComponentScan} picks up every {@code @Component}/{@code @Service} in
 * this package (singleton and prototype beans, lifecycle-annotated beans,
 * request/session-scoped beans). {@link EmbeddedMessageBroker} is a plain POJO
 * with no Spring annotations, so it is registered explicitly with
 * {@code @Bean(initMethod = ..., destroyMethod = ...)} instead.
 */
@Configuration
@ComponentScan(basePackageClasses = ScopesLifecycleConfig.class)
public class ScopesLifecycleConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    public EmbeddedMessageBroker embeddedMessageBroker() {
        return new EmbeddedMessageBroker();
    }
}
