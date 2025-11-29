package io.github.sps23.interview.preparation.async;

import java.util.Objects;

/**
 * Represents an API response with data from an external service.
 *
 * <p>
 * This record demonstrates using Java Records for immutable data carriers in an
 * async context. Records work seamlessly with CompletableFuture for
 * representing results from async operations.
 *
 * @param source
 *            the name of the API source
 * @param data
 *            the response data
 * @param timestamp
 *            when the response was received
 */
public record ApiResponse(String source, String data, long timestamp) {

    /**
     * Compact constructor for validation.
     */
    public ApiResponse {
        Objects.requireNonNull(source, "Source cannot be null");
        Objects.requireNonNull(data, "Data cannot be null");
        if (timestamp <= 0) {
            throw new IllegalArgumentException("Timestamp must be positive");
        }
    }

    /**
     * Creates an ApiResponse with the current timestamp.
     *
     * @param source
     *            the API source name
     * @param data
     *            the response data
     * @return new ApiResponse instance
     */
    public static ApiResponse of(String source, String data) {
        return new ApiResponse(source, data, System.currentTimeMillis());
    }
}
