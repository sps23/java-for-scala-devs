package io.github.sps23.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Typed configuration properties for the Spring Boot basics example.
 */
@ConfigurationProperties(prefix = "app")
public record AppProperties(String greeting) {
}
