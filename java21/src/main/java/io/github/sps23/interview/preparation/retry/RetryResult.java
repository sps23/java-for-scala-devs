package io.github.sps23.interview.preparation.retry;

/**
 * Represents the result of a retry operation.
 *
 * <p>
 * This sealed interface uses pattern matching to handle success and failure
 * cases. It's similar to Scala's Either or Try types.
 *
 * <p>
 * Comparison with Scala:
 *
 * <pre>
 * // Scala equivalent using sealed trait
 * enum RetryResult[+T]:
 *   case Success(value: T, attempts: Int)
 *   case Failure(error: Throwable, attempts: Int)
 *
 * // Or using Either
 * type RetryResult[T] = Either[RetryFailure, RetrySuccess[T]]
 * </pre>
 *
 * @param <T>
 *            the type of the successful result
 */
public sealed interface RetryResult<T> {

    /**
     * The number of attempts made.
     *
     * @return total attempts including the successful one
     */
    int attempts();

    /**
     * Checks if the operation succeeded.
     *
     * @return true if successful
     */
    boolean isSuccess();

    /**
     * Successful result containing the value.
     *
     * @param value
     *            the successful result
     * @param attempts
     *            number of attempts made
     * @param <T>
     *            type of the result
     */
    record Success<T>(T value, int attempts) implements RetryResult<T> {
        @Override
        public boolean isSuccess() {
            return true;
        }
    }

    /**
     * Failure result containing the final exception.
     *
     * @param error
     *            the last exception encountered
     * @param attempts
     *            number of attempts made
     * @param <T>
     *            type parameter (unused but needed for compatibility)
     */
    record Failure<T>(Throwable error, int attempts) implements RetryResult<T> {
        @Override
        public boolean isSuccess() {
            return false;
        }
    }

    /**
     * Creates a successful result.
     *
     * @param value
     *            the result value
     * @param attempts
     *            number of attempts
     * @param <T>
     *            type of result
     * @return successful retry result
     */
    static <T> RetryResult<T> success(T value, int attempts) {
        return new Success<>(value, attempts);
    }

    /**
     * Creates a failure result.
     *
     * @param error
     *            the final exception
     * @param attempts
     *            number of attempts
     * @param <T>
     *            type parameter
     * @return failure retry result
     */
    static <T> RetryResult<T> failure(Throwable error, int attempts) {
        return new Failure<>(error, attempts);
    }
}
