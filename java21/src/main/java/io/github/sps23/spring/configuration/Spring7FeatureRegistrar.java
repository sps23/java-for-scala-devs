package io.github.sps23.spring.configuration;

import org.springframework.beans.factory.BeanRegistrar;
import org.springframework.beans.factory.BeanRegistry;
import org.springframework.core.env.Environment;

/**
 * Demonstrates the first-class programmatic bean registration API introduced in
 * Spring 7.
 */
public final class Spring7FeatureRegistrar implements BeanRegistrar {

    @Override
    public void register(BeanRegistry registry, Environment env) {
        registry.registerBean(Spring7FeatureNote.class,
                spec -> spec.description("Spring 7 BeanRegistrar example")
                        .supplier(context -> new Spring7FeatureNote(env.matchesProfiles("prod")
                                ? "Registered with BeanRegistrar for the production profile"
                                : "Registered with BeanRegistrar for the development profile")));
    }
}
