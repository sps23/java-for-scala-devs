package io.github.sps23.interview.preparation.retry;

/**
 * A functional interface for operations that can throw checked exceptions.
 *
 * <p>
 * Java's standard {@code Supplier<T>} cannot throw checked exceptions. This
 * interface provides a workaround, allowing exception-throwing operations to be
 * used in functional contexts.
 *
 * <p>
 * Comparison with Scala:
 *
 * <pre>
 * // Scala doesn't distinguish checked/unchecked exceptions
 * // so a simple function type works:
 * type ThrowingSupplier[T] = () => T
 *
 * // Or using Try for safer error handling:
 * def safeSupply[T](f: () => T): Try[T] = Try(f())
 * </pre>
 *
 * @param <T>
 *            the type of result supplied
 * @param <E>
 *            the type of exception that may be thrown
 */
@FunctionalInterface
public interface ThrowingSupplier<T, E extends Throwable> {

    /**
     * Gets a result, potentially throwing an exception.
     *
     * @return the result
     * @throws E
     *             if the operation fails
     */
    T get() throws E;

    /**
     * Converts this to a standard Supplier that wraps exceptions.
     *
     * <p>
     * Demonstrates adapting between functional interface types.
     *
     * @return a Supplier that wraps checked exceptions
     */
    default java.util.function.Supplier<T> toSupplier() {
        return () -> {
            try {
                return get();
            } catch (Throwable e) {
                throw new RetryException("Supplier failed", e);
            }
        };
    }

    /**
     * Creates a ThrowingSupplier from a regular Supplier.
     *
     * @param supplier
     *            the supplier to wrap
     * @param <T>
     *            result type
     * @return a ThrowingSupplier wrapping the supplier
     */
    static <T> ThrowingSupplier<T, RuntimeException> from(java.util.function.Supplier<T> supplier) {
        return supplier::get; // Method reference: instance::method
    }
}
