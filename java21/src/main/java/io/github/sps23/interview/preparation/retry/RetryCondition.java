package io.github.sps23.interview.preparation.retry;

import java.util.function.Predicate;

/**
 * A custom functional interface that determines whether a retry should be
 * attempted.
 *
 * <p>
 * While Java provides {@code java.util.function.Predicate<Throwable>}, this
 * custom interface demonstrates:
 * <ul>
 * <li>Domain-specific naming for clarity</li>
 * <li>Additional context (attempt count, elapsed time)</li>
 * <li>Composition methods tailored for retry logic</li>
 * </ul>
 *
 * <p>
 * Comparison with Scala:
 *
 * <pre>
 * // Scala equivalent - using a simple function
 * type ShouldRetry = (Int, Duration, Throwable) => Boolean
 *
 * // Or as a trait with composition
 * trait ShouldRetry:
 *   def shouldRetry(attempt: Int, elapsed: Duration, error: Throwable): Boolean
 *   def and(other: ShouldRetry): ShouldRetry = (a, e, err) =>
 *     this.shouldRetry(a, e, err) && other.shouldRetry(a, e, err)
 * </pre>
 */
@FunctionalInterface
public interface RetryCondition {

    /**
     * Determines whether a retry should be attempted.
     *
     * @param context
     *            the current retry context with attempt info
     * @return true if retry should be attempted
     */
    boolean shouldRetry(RetryContext context);

    /**
     * Creates a condition that limits the number of retry attempts.
     *
     * @param maxAttempts
     *            maximum number of attempts (including initial)
     * @return a retry condition based on attempt count
     */
    static RetryCondition maxAttempts(int maxAttempts) {
        return context -> context.attempt() < maxAttempts;
    }

    /**
     * Creates a condition based on exception type.
     *
     * <p>
     * Demonstrates using Class::isInstance as method reference.
     *
     * @param retryableExceptionTypes
     *            exception types that should trigger retry
     * @return a retry condition based on exception type
     */
    @SafeVarargs
    static RetryCondition forExceptions(Class<? extends Throwable>... retryableExceptionTypes) {
        return context -> {
            for (var exceptionType : retryableExceptionTypes) {
                if (exceptionType.isInstance(context.lastError())) {
                    return true;
                }
            }
            return false;
        };
    }

    /**
     * Creates a condition that checks exception message.
     *
     * <p>
     * Demonstrates lambda with String method reference for contains check.
     *
     * @param messagePattern
     *            pattern to match in exception message
     * @return a retry condition based on message content
     */
    static RetryCondition messageContains(String messagePattern) {
        return context -> context.lastError().getMessage() != null
                && context.lastError().getMessage().contains(messagePattern);
    }

    /**
     * Creates a condition from a simple predicate on the exception.
     *
     * <p>
     * Demonstrates adapting Java's built-in Predicate to our domain-specific
     * interface.
     *
     * @param exceptionPredicate
     *            predicate to test the exception
     * @return a retry condition wrapping the predicate
     */
    static RetryCondition fromPredicate(Predicate<Throwable> exceptionPredicate) {
        return context -> exceptionPredicate.test(context.lastError());
    }

    /**
     * Combines this condition with another using logical AND.
     *
     * @param other
     *            another retry condition
     * @return combined condition requiring both to be true
     */
    default RetryCondition and(RetryCondition other) {
        return context -> this.shouldRetry(context) && other.shouldRetry(context);
    }

    /**
     * Combines this condition with another using logical OR.
     *
     * @param other
     *            another retry condition
     * @return combined condition requiring either to be true
     */
    default RetryCondition or(RetryCondition other) {
        return context -> this.shouldRetry(context) || other.shouldRetry(context);
    }

    /**
     * Negates this condition.
     *
     * @return inverted condition
     */
    default RetryCondition not() {
        return context -> !this.shouldRetry(context);
    }
}
