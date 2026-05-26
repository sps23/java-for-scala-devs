package io.github.sps23.spring.mvc;

import java.time.Instant;

/**
 * Standard error response body returned by the REST API when something goes wrong.
 *
 * <p>
 * In a real Spring MVC application this record would be returned from a
 * {@code @ControllerAdvice} exception handler, wrapped in a
 * {@code ResponseEntity<ApiError>} with the appropriate HTTP status code.
 *
 * <p>
 * Example JSON serialisation:
 *
 * <pre>
 * {
 *   "status": 404,
 *   "message": "Trade trade-999 not found",
 *   "timestamp": "2026-05-26T10:00:00Z"
 * }
 * </pre>
 *
 * @param status
 *            HTTP status code (e.g. 400, 404, 500)
 * @param message
 *            human-readable error description
 * @param timestamp
 *            when the error occurred (UTC)
 */
public record ApiError(int status, String message, Instant timestamp) {

    /**
     * Convenience factory method that sets the timestamp to now.
     *
     * @param status
     *            HTTP status code
     * @param message
     *            error message
     * @return a new {@code ApiError} stamped with the current time
     */
    public static ApiError of(int status, String message) {
        return new ApiError(status, message, Instant.now());
    }
}
