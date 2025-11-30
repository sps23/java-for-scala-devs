package io.github.sps23.interview.preparation.retry;

/**
 * Exception wrapper for retry-related failures.
 */
public class RetryException extends RuntimeException {

    public RetryException(String message) {
        super(message);
    }

    public RetryException(String message, Throwable cause) {
        super(message, cause);
    }
}
