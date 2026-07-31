package io.github.sps23.designpatterns.builder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder pattern in Java 21.
 * Real-world style HTTP client configuration with required fields, optional
 * defaults, and cross-field validation.
 */
public final class HttpClientConfig {
    private final String host;
    private final int port;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final boolean useSsl;
    private final int maxRetries;
    private final List<Integer> retryBackoffMs;
    private final Map<String, String> defaultHeaders;
    private final int circuitBreakerFailureThreshold;
    private final String apiVersion;
    private final boolean enableCompression;

    private HttpClientConfig(Builder builder) {
        this.host = validateHost(builder.host);
        this.port = validatePort(builder.port);
        this.connectTimeoutMs = validateTimeout(builder.connectTimeoutMs, "Connect timeout");
        this.readTimeoutMs = validateTimeout(builder.readTimeoutMs, "Read timeout");
        this.useSsl = builder.useSsl;
        this.maxRetries = validateMaxRetries(builder.maxRetries);
        this.retryBackoffMs = validateRetryBackoff(builder.retryBackoffMs, maxRetries);
        this.defaultHeaders = validateHeaders(builder.defaultHeaders);
        this.circuitBreakerFailureThreshold = validateThreshold(
                builder.circuitBreakerFailureThreshold);
        this.apiVersion = validateApiVersion(builder.apiVersion);
        this.enableCompression = builder.enableCompression;
        validateTimeoutRelationship(connectTimeoutMs, readTimeoutMs);
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public int connectTimeoutMs() {
        return connectTimeoutMs;
    }

    public int readTimeoutMs() {
        return readTimeoutMs;
    }

    public boolean useSsl() {
        return useSsl;
    }

    public int maxRetries() {
        return maxRetries;
    }

    public List<Integer> retryBackoffMs() {
        return retryBackoffMs;
    }

    public Map<String, String> defaultHeaders() {
        return defaultHeaders;
    }

    public int circuitBreakerFailureThreshold() {
        return circuitBreakerFailureThreshold;
    }

    public String apiVersion() {
        return apiVersion;
    }

    public boolean enableCompression() {
        return enableCompression;
    }

    public String baseUrl() {
        String scheme = useSsl ? "https" : "http";
        return scheme + "://" + host + ":" + port + "/api/" + apiVersion;
    }

    public static Builder builder(String host, int port) {
        return new Builder(host, port);
    }

    private static String validateHost(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Host must not be blank");
        }
        return value;
    }

    private static int validatePort(int value) {
        if (value < 1 || value > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        return value;
    }

    private static int validateTimeout(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
        return value;
    }

    private static void validateTimeoutRelationship(int connectTimeoutMs, int readTimeoutMs) {
        if (readTimeoutMs < connectTimeoutMs) {
            throw new IllegalArgumentException(
                    "Read timeout must be greater than or equal to connect timeout");
        }
    }

    private static int validateMaxRetries(int value) {
        if (value < 0 || value > 10) {
            throw new IllegalArgumentException("Max retries must be between 0 and 10");
        }
        return value;
    }

    private static List<Integer> validateRetryBackoff(List<Integer> value, int maxRetries) {
        if (value == null) {
            throw new IllegalArgumentException("Retry backoff must not be null");
        }
        if (value.size() != maxRetries) {
            throw new IllegalArgumentException("Retry backoff size must match max retries");
        }
        for (Integer backoff : value) {
            if (backoff == null || backoff <= 0) {
                throw new IllegalArgumentException("Retry backoff values must be positive");
            }
        }
        return List.copyOf(value);
    }

    private static Map<String, String> validateHeaders(Map<String, String> value) {
        if (value == null) {
            throw new IllegalArgumentException("Default headers must not be null");
        }
        for (Map.Entry<String, String> entry : value.entrySet()) {
            if (entry.getKey() == null || entry.getKey().isBlank()) {
                throw new IllegalArgumentException("Header key must not be blank");
            }
            if (entry.getValue() == null || entry.getValue().isBlank()) {
                throw new IllegalArgumentException("Header value must not be blank");
            }
        }
        return Map.copyOf(value);
    }

    private static int validateThreshold(int value) {
        if (value < 1 || value > 100) {
            throw new IllegalArgumentException(
                    "Circuit breaker threshold must be between 1 and 100");
        }
        return value;
    }

    private static String validateApiVersion(String value) {
        if (value == null || !value.matches("v\\d+")) {
            throw new IllegalArgumentException("API version must match pattern v{number}");
        }
        return value;
    }

    public static final class Builder {
        private final String host;
        private final int port;
        private int connectTimeoutMs = 500;
        private int readTimeoutMs = 2_000;
        private boolean useSsl = true;
        private int maxRetries = 3;
        private List<Integer> retryBackoffMs = List.of(100, 200, 500);
        private Map<String, String> defaultHeaders = new LinkedHashMap<>(
                Map.of("Accept", "application/json", "User-Agent", "java-for-scala-devs-client"));
        private int circuitBreakerFailureThreshold = 50;
        private String apiVersion = "v1";
        private boolean enableCompression = true;

        private Builder(String host, int port) {
            this.host = host;
            this.port = port;
        }

        public Builder connectTimeoutMs(int connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
            return this;
        }

        public Builder readTimeoutMs(int readTimeoutMs) {
            this.readTimeoutMs = readTimeoutMs;
            return this;
        }

        public Builder useSsl(boolean useSsl) {
            this.useSsl = useSsl;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder retryBackoffMs(List<Integer> retryBackoffMs) {
            this.retryBackoffMs = retryBackoffMs;
            return this;
        }

        public Builder defaultHeaders(Map<String, String> defaultHeaders) {
            this.defaultHeaders = new LinkedHashMap<>(defaultHeaders);
            return this;
        }

        public Builder addDefaultHeader(String key, String value) {
            this.defaultHeaders.put(key, value);
            return this;
        }

        public Builder circuitBreakerFailureThreshold(int circuitBreakerFailureThreshold) {
            this.circuitBreakerFailureThreshold = circuitBreakerFailureThreshold;
            return this;
        }

        public Builder apiVersion(String apiVersion) {
            this.apiVersion = apiVersion;
            return this;
        }

        public Builder enableCompression(boolean enableCompression) {
            this.enableCompression = enableCompression;
            return this;
        }

        public HttpClientConfig build() {
            return new HttpClientConfig(this);
        }
    }
}
