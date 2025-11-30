package io.github.sps23.interview.preparation.retry;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A configurable retry executor demonstrating functional interfaces and lambda
 * expressions.
 *
 * <p>
 * This class showcases key Java functional programming concepts:
 * <ul>
 * <li>{@code @FunctionalInterface} for custom single-method interfaces</li>
 * <li>Built-in functional interfaces: Function, Predicate, Consumer,
 * Supplier</li>
 * <li>Method references: static, instance, and constructor</li>
 * <li>Exception handling in lambdas</li>
 * <li>Function composition with andThen, compose</li>
 * </ul>
 *
 * <p>
 * Comparison with Scala:
 *
 * <pre>
 * // Scala version would use function types directly:
 * class RetryExecutor[T](
 *   policy: (Int, Throwable) => Duration,
 *   condition: RetryContext => Boolean,
 *   onRetry: (RetryContext, Duration) => Unit,
 *   resultTransformer: T => T
 * )
 * </pre>
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * var executor = RetryExecutor.<String>builder()
 *         .withPolicy(
 *                 RetryPolicy.exponentialBackoff(Duration.ofMillis(100), Duration.ofSeconds(5)))
 *         .withCondition(RetryCondition.maxAttempts(3)
 *                 .and(RetryCondition.forExceptions(IOException.class)))
 *         .withRetryListener((ctx, delay) -> System.out.println("Retrying in " + delay))
 *         .withResultTransformer(String::trim).build();
 *
 * RetryResult<String> result = executor.execute(() -> fetchData());
 * }</pre>
 *
 * @param <T>
 *            the type of result from the retried operation
 */
public final class RetryExecutor<T> {

    private final RetryPolicy policy;
    private final RetryCondition condition;
    private final Consumer<RetryEvent> retryListener;
    private final Function<T, T> resultTransformer;

    private RetryExecutor(Builder<T> builder) {
        this.policy = builder.policy;
        this.condition = builder.condition;
        this.retryListener = builder.retryListener;
        this.resultTransformer = builder.resultTransformer;
    }

    /**
     * Executes an operation with retry logic.
     *
     * <p>
     * Demonstrates:
     * <ul>
     * <li>Using ThrowingSupplier for exception-throwing lambdas</li>
     * <li>Consumer for side effects (logging)</li>
     * <li>Function for result transformation</li>
     * </ul>
     *
     * @param operation
     *            the operation to execute and potentially retry
     * @param <E>
     *            the type of exception the operation may throw
     * @return the retry result (success or failure)
     */
    public <E extends Throwable> RetryResult<T> execute(ThrowingSupplier<T, E> operation) {
        int attempt = 0;
        Throwable lastError = null;
        RetryContext context = null;

        while (true) {
            attempt++;
            try {
                T result = operation.get();
                // Apply result transformation - demonstrates Function usage
                T transformed = resultTransformer.apply(result);
                return RetryResult.success(transformed, attempt);
            } catch (Throwable e) {
                lastError = e;

                if (context == null) {
                    context = RetryContext.initial(e);
                } else {
                    context = context.nextAttempt(e);
                }

                // Check if we should retry - demonstrates Predicate-like usage
                if (!condition.shouldRetry(context)) {
                    return RetryResult.failure(e, attempt);
                }

                // Calculate delay - demonstrates custom functional interface
                Duration delay = policy.delayFor(attempt, e);

                // Notify listener - demonstrates Consumer usage
                retryListener.accept(new RetryEvent(context, delay));

                // Wait before retry
                sleep(delay);
            }
        }
    }

    private void sleep(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RetryException("Retry interrupted", ie);
        }
    }

    /**
     * Creates a new builder for RetryExecutor.
     *
     * @param <T>
     *            the result type
     * @return a new builder instance
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Event record passed to retry listeners.
     *
     * @param context
     *            the current retry context
     * @param delay
     *            the delay before next attempt
     */
    public record RetryEvent(RetryContext context, Duration delay) {
    }

    /**
     * Builder for RetryExecutor demonstrating method chaining with lambdas.
     *
     * @param <T>
     *            the result type
     */
    public static final class Builder<T> {

        private RetryPolicy policy = RetryPolicy.fixed(Duration.ofSeconds(1));
        private RetryCondition condition = RetryCondition.maxAttempts(3);
        private Consumer<RetryEvent> retryListener = event -> {
        }; // No-op default
        private Function<T, T> resultTransformer = Function.identity(); // Pass-through

        /**
         * Sets the retry policy.
         *
         * @param policy
         *            the retry policy
         * @return this builder
         */
        public Builder<T> withPolicy(RetryPolicy policy) {
            this.policy = policy;
            return this;
        }

        /**
         * Sets the retry condition.
         *
         * @param condition
         *            the retry condition
         * @return this builder
         */
        public Builder<T> withCondition(RetryCondition condition) {
            this.condition = condition;
            return this;
        }

        /**
         * Sets the retry listener using a Consumer.
         *
         * <p>
         * Demonstrates Consumer functional interface:
         *
         * <pre>{@code
         * // Using lambda
         * .withRetryListener(event -> log.info("Retry: {}", event))
         *
         * // Using method reference
         * .withRetryListener(this::logRetry)
         * .withRetryListener(System.out::println)
         * }</pre>
         *
         * @param listener
         *            the retry event listener
         * @return this builder
         */
        public Builder<T> withRetryListener(Consumer<RetryEvent> listener) {
            this.retryListener = listener;
            return this;
        }

        /**
         * Sets the result transformer using a Function.
         *
         * <p>
         * Demonstrates Function interface and method references:
         *
         * <pre>{@code
         * // Using method reference - instance method
         * .withResultTransformer(String::trim)
         *
         * // Using method reference - static method
         * .withResultTransformer(String::valueOf)
         *
         * // Using lambda with composition
         * .withResultTransformer(s -> s.trim().toLowerCase())
         *
         * // Using Function.andThen composition
         * .withResultTransformer(String::trim.andThen(String::toLowerCase))
         * }</pre>
         *
         * @param transformer
         *            the result transformer
         * @return this builder
         */
        public Builder<T> withResultTransformer(Function<T, T> transformer) {
            this.resultTransformer = transformer;
            return this;
        }

        /**
         * Adds an additional result transformer using Function.andThen.
         *
         * <p>
         * Demonstrates function composition:
         *
         * <pre>{@code
         * builder.withResultTransformer(String::trim).andThenTransform(String::toLowerCase)
         * }</pre>
         *
         * @param additionalTransformer
         *            transformer to apply after current one
         * @return this builder
         */
        public Builder<T> andThenTransform(Function<T, T> additionalTransformer) {
            this.resultTransformer = this.resultTransformer.andThen(additionalTransformer);
            return this;
        }

        /**
         * Builds the RetryExecutor.
         *
         * @return configured RetryExecutor instance
         */
        public RetryExecutor<T> build() {
            return new RetryExecutor<>(this);
        }
    }

    /**
     * Demonstrates various method reference types.
     *
     * <p>
     * This method exists purely for educational purposes to show all method
     * reference types in Java:
     * <ul>
     * <li>Static method reference: {@code ClassName::staticMethod}</li>
     * <li>Instance method reference on object: {@code object::instanceMethod}</li>
     * <li>Instance method reference on type: {@code ClassName::instanceMethod}</li>
     * <li>Constructor reference: {@code ClassName::new}</li>
     * </ul>
     */
    public static void demonstrateMethodReferences() {
        // 1. Static method reference: ClassName::staticMethod
        Function<Long, Duration> staticMethodRef = Duration::ofMillis;
        System.out.println("Static method ref: " + staticMethodRef.apply(1000L));

        // 2. Instance method reference on specific object: object::instanceMethod
        String prefix = "Result: ";
        Function<String, String> boundInstanceRef = prefix::concat;
        System.out.println("Bound instance ref: " + boundInstanceRef.apply("value"));

        // 3. Instance method reference on type: ClassName::instanceMethod
        // The first argument becomes the receiver
        Function<String, String> unboundInstanceRef = String::trim;
        System.out.println("Unbound instance ref: " + unboundInstanceRef.apply("  hello  "));

        // 4. Constructor reference: ClassName::new
        Function<String, IOException> constructorRef = IOException::new;
        System.out.println("Constructor ref: " + constructorRef.apply("test error").getMessage());
    }

    /**
     * Main method demonstrating complete retry functionality.
     *
     * @param args
     *            command line arguments (unused)
     */
    public static void main(String[] args) {
        System.out.println("=== Retry Executor Demo ===\n");

        // Demonstrate method references
        System.out.println("1. Method References:");
        demonstrateMethodReferences();
        System.out.println();

        // Create a retry executor with various functional components
        System.out.println("2. Building RetryExecutor with lambdas:");

        var executor = RetryExecutor.<String>builder()
                // Custom functional interface lambda
                .withPolicy(RetryPolicy.exponentialBackoff(Duration.ofMillis(100),
                        Duration.ofSeconds(2)))
                // Combining conditions with and()
                .withCondition(RetryCondition.maxAttempts(4)
                        .and(RetryCondition.forExceptions(RuntimeException.class)))
                // Consumer lambda for logging
                .withRetryListener(
                        event -> System.out.println("  Retry attempt " + event.context().attempt()
                                + ", waiting " + event.delay().toMillis() + "ms"))
                // Function method reference
                .withResultTransformer(String::trim).andThenTransform(String::toLowerCase).build();

        System.out.println("  Executor built successfully\n");

        // Execute with a flaky operation
        System.out.println("3. Executing flaky operation:");

        var counter = new int[]{0}; // Mutable counter for demo
        var result = executor.execute(() -> {
            counter[0]++;
            if (counter[0] < 3) {
                throw new RuntimeException("Simulated failure #" + counter[0]);
            }
            return "  SUCCESS after " + counter[0] + " attempts  ";
        });

        // Pattern matching on result
        System.out.println("\n4. Result (using pattern matching):");
        switch (result) {
            case RetryResult.Success<String> s -> System.out
                    .println("  Success: '" + s.value() + "' in " + s.attempts() + " attempts");
            case RetryResult.Failure<String> f -> System.out.println("  Failure: "
                    + f.error().getMessage() + " after " + f.attempts() + " attempts");
        }

        // Demonstrate composition
        System.out.println("\n5. Policy composition:");
        var combinedPolicy = RetryPolicy.fixed(Duration.ofMillis(100)).maxWith(
                RetryPolicy.exponentialBackoff(Duration.ofMillis(50), Duration.ofSeconds(1)));
        System.out.println("  Combined policy delay for attempt 1: "
                + combinedPolicy.delayFor(1, new RuntimeException()));
        System.out.println("  Combined policy delay for attempt 3: "
                + combinedPolicy.delayFor(3, new RuntimeException()));

        System.out.println("\n=== Demo Complete ===");
    }
}
