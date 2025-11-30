package io.github.sps23.interview.preparation.retry;

import java.time.Duration;

/**
 * A custom functional interface that defines a retry policy.
 *
 * <p>
 * The {@code @FunctionalInterface} annotation ensures this interface has
 * exactly one abstract method, making it usable as a lambda target. This is
 * similar to Scala's single-abstract-method traits.
 *
 * <p>
 * Comparison with Scala:
 *
 * <pre>
 * // Scala equivalent - just a function type
 * type RetryPolicy = (Int, Throwable) => Duration
 *
 * // Or as a trait
 * trait RetryPolicy:
 *   def delayFor(attempt: Int, error: Throwable): Duration
 * </pre>
 */
@FunctionalInterface
public interface RetryPolicy {

    /**
     * Calculates the delay before the next retry attempt.
     *
     * @param attempt
     *            the current attempt number (1-based)
     * @param lastError
     *            the exception from the previous attempt
     * @return the duration to wait before the next attempt
     */
    Duration delayFor(int attempt, Throwable lastError);

    /**
     * Creates a fixed delay policy.
     *
     * <p>
     * Demonstrates static method reference: {@code RetryPolicy::fixed}
     *
     * @param delay
     *            the fixed delay between retries
     * @return a retry policy with constant delay
     */
    static RetryPolicy fixed(Duration delay) {
        return (attempt, error) -> delay;
    }

    /**
     * Creates an exponential backoff policy.
     *
     * <p>
     * Demonstrates lambda expression with calculation logic.
     *
     * @param initialDelay
     *            the delay for the first retry
     * @param maxDelay
     *            the maximum delay cap
     * @return a retry policy with exponential backoff
     */
    static RetryPolicy exponentialBackoff(Duration initialDelay, Duration maxDelay) {
        return (attempt, error) -> {
            long delayMs = initialDelay.toMillis() * (long) Math.pow(2, attempt - 1);
            return Duration.ofMillis(Math.min(delayMs, maxDelay.toMillis()));
        };
    }

    /**
     * Creates a policy that varies delay based on exception type.
     *
     * <p>
     * Demonstrates conditional logic in lambdas.
     *
     * @param networkDelay
     *            delay for network errors
     * @param otherDelay
     *            delay for other errors
     * @return a retry policy with error-specific delays
     */
    static RetryPolicy errorSpecific(Duration networkDelay, Duration otherDelay) {
        return (attempt, error) -> error instanceof java.io.IOException ? networkDelay : otherDelay;
    }

    /**
     * Combines this policy with another, taking the maximum delay.
     *
     * <p>
     * Demonstrates default method for functional interface composition.
     *
     * @param other
     *            another retry policy
     * @return a combined policy returning the maximum delay
     */
    default RetryPolicy maxWith(RetryPolicy other) {
        return (attempt, error) -> {
            Duration thisDelay = this.delayFor(attempt, error);
            Duration otherDelay = other.delayFor(attempt, error);
            return thisDelay.compareTo(otherDelay) > 0 ? thisDelay : otherDelay;
        };
    }
}
