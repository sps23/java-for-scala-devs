package io.github.sps23.interview.preparation.async;

import java.util.List;
import java.util.Objects;

/**
 * Represents aggregated data from multiple API sources.
 *
 * <p>
 * This record holds the combined results from multiple async API calls,
 * including any partial failures that occurred during aggregation.
 *
 * @param responses
 *            successful API responses
 * @param errors
 *            any errors encountered during aggregation
 */
public record AggregatedData(List<ApiResponse> responses, List<String> errors) {

    /**
     * Compact constructor for validation and defensive copying.
     */
    public AggregatedData {
        Objects.requireNonNull(responses, "Responses cannot be null");
        Objects.requireNonNull(errors, "Errors cannot be null");
        // Defensive copy to ensure immutability
        responses = List.copyOf(responses);
        errors = List.copyOf(errors);
    }

    /**
     * Creates an AggregatedData with only successful responses.
     *
     * @param responses
     *            the successful API responses
     * @return new AggregatedData instance
     */
    public static AggregatedData success(List<ApiResponse> responses) {
        return new AggregatedData(responses, List.of());
    }

    /**
     * Creates an AggregatedData with responses and errors.
     *
     * @param responses
     *            the successful API responses
     * @param errors
     *            the error messages
     * @return new AggregatedData instance
     */
    public static AggregatedData partial(List<ApiResponse> responses, List<String> errors) {
        return new AggregatedData(responses, errors);
    }

    /**
     * Returns true if all API calls succeeded.
     *
     * @return true if no errors occurred
     */
    public boolean isFullySuccessful() {
        return errors.isEmpty();
    }

    /**
     * Returns the count of successful responses.
     *
     * @return number of successful responses
     */
    public int successCount() {
        return responses.size();
    }
}
