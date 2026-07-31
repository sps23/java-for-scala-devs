package io.github.sps23.designpatterns.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("HttpClientConfig Java 21 Builder Tests")
class HttpClientConfigTest {

    @Test
    @DisplayName("Should build config with defaults")
    void shouldBuildWithDefaults() {
        HttpClientConfig config = HttpClientConfig.builder("api.example.com", 443).build();

        assertEquals("api.example.com", config.host());
        assertEquals(443, config.port());
        assertEquals(500, config.connectTimeoutMs());
        assertEquals(2000, config.readTimeoutMs());
        assertTrue(config.useSsl());
        assertEquals(3, config.maxRetries());
        assertEquals(List.of(100, 200, 500), config.retryBackoffMs());
        assertEquals("application/json", config.defaultHeaders().get("Accept"));
        assertEquals("v1", config.apiVersion());
        assertTrue(config.enableCompression());
        assertEquals("https://api.example.com:443/api/v1", config.baseUrl());
    }

    @Test
    @DisplayName("Should build config with custom values")
    void shouldBuildWithCustomValues() {
        HttpClientConfig config = HttpClientConfig.builder("localhost", 8080).connectTimeoutMs(300)
                .readTimeoutMs(1500).useSsl(false).maxRetries(2).retryBackoffMs(List.of(50, 100))
                .defaultHeaders(Map.of("Accept", "application/json"))
                .addDefaultHeader("X-Correlation-Id", "request-123").apiVersion("v2")
                .circuitBreakerFailureThreshold(25).enableCompression(false).build();

        assertEquals("localhost", config.host());
        assertEquals(8080, config.port());
        assertEquals(300, config.connectTimeoutMs());
        assertEquals(1500, config.readTimeoutMs());
        assertFalse(config.useSsl());
        assertEquals(2, config.maxRetries());
        assertEquals(List.of(50, 100), config.retryBackoffMs());
        assertEquals("request-123", config.defaultHeaders().get("X-Correlation-Id"));
        assertEquals("v2", config.apiVersion());
        assertEquals(25, config.circuitBreakerFailureThreshold());
        assertFalse(config.enableCompression());
        assertEquals("http://localhost:8080/api/v2", config.baseUrl());
    }

    @Test
    @DisplayName("Should reject blank host")
    void shouldRejectBlankHost() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> HttpClientConfig.builder("  ", 443).build());
        assertEquals("Host must not be blank", error.getMessage());
    }

    @Test
    @DisplayName("Should reject invalid port")
    void shouldRejectInvalidPort() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> HttpClientConfig.builder("api.example.com", 70000).build());
        assertEquals("Port must be between 1 and 65535", error.getMessage());
    }

    @Test
    @DisplayName("Should reject non-positive timeout")
    void shouldRejectConnectTimeout() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> HttpClientConfig.builder("api.example.com", 443).connectTimeoutMs(0).build());
        assertEquals("Connect timeout must be positive", error.getMessage());
    }

    @Test
    @DisplayName("Should reject read timeout smaller than connect timeout")
    void shouldRejectTimeoutRelationship() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> HttpClientConfig.builder("api.example.com", 443).connectTimeoutMs(2000)
                        .readTimeoutMs(1000).build());
        assertEquals("Read timeout must be greater than or equal to connect timeout",
                error.getMessage());
    }

    @Test
    @DisplayName("Should reject retries outside supported range")
    void shouldRejectRetries() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> HttpClientConfig.builder("api.example.com", 443).maxRetries(11).build());
        assertEquals("Max retries must be between 0 and 10", error.getMessage());
    }

    @Test
    @DisplayName("Should reject retry backoff size mismatch")
    void shouldRejectBackoffSizeMismatch() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> HttpClientConfig.builder("api.example.com", 443).maxRetries(2)
                        .retryBackoffMs(List.of(100)).build());
        assertEquals("Retry backoff size must match max retries", error.getMessage());
    }

    @Test
    @DisplayName("Should reject blank header key")
    void shouldRejectBlankHeaderKey() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> HttpClientConfig.builder("api.example.com", 443).addDefaultHeader(" ", "x")
                        .build());
        assertEquals("Header key must not be blank", error.getMessage());
    }

    @Test
    @DisplayName("Should reject invalid API version")
    void shouldRejectApiVersion() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> HttpClientConfig.builder("api.example.com", 443).apiVersion("latest")
                        .build());
        assertEquals("API version must match pattern v{number}", error.getMessage());
    }
}
