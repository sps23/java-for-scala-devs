package io.github.sps23.interview.preparation.retry;

import java.time.Duration;
import java.time.Instant;

/**
 * Immutable record holding retry context information.
 *
 * <p>
 * This record provides all necessary context for retry decisions.
 *
 * @param attempt
 *            current attempt number (1-based)
 * @param startTime
 *            when the first attempt started
 * @param lastError
 *            exception from the most recent failed attempt
 */
public record RetryContext(int attempt, Instant startTime, Throwable lastError) {

    /**
     * Creates a new context for the first attempt that just failed.
     *
     * @param error
     *            the exception from the first attempt
     * @return initial retry context
     */
    public static RetryContext initial(Throwable error) {
        return new RetryContext(1, Instant.now(), error);
    }

    /**
     * Creates a new context for the next retry attempt.
     *
     * @param newError
     *            the exception from the current failed attempt
     * @return updated context with incremented attempt count
     */
    public RetryContext nextAttempt(Throwable newError) {
        return new RetryContext(attempt + 1, startTime, newError);
    }

    /**
     * Gets the elapsed time since the first attempt.
     *
     * @return duration since start
     */
    public Duration elapsed() {
        return Duration.between(startTime, Instant.now());
    }
}
