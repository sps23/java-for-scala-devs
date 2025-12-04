package io.github.sps23.typeclasses;

/**
 * SPI interface for ServiceLoader-based Show instance discovery.
 *
 * Implementations should be registered in:
 * META-INF/services/io.github.sps23.typeclasses.ShowProvider
 */
public interface ShowProvider {
    /**
     * The type this Show instance handles.
     */
    Class<?> targetType();

    /**
     * Create the Show instance.
     */
    Show<?> createShow();
}
