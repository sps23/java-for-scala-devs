package io.github.sps23.typeclasses;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * The Show typeclass - provides pretty printing for values.
 *
 * This demonstrates the typeclass pattern in Java, similar to Scala's Show
 * typeclass. Unlike Scala, Java doesn't have implicit resolution, so instances
 * must be passed explicitly or looked up from a registry.
 */
@FunctionalInterface
public interface Show<T> {
    /**
     * Convert a value to a human-readable string representation.
     */
    String show(T value);

    // ============================================================================
    // Factory Methods for Common Types
    // ============================================================================

    static Show<Integer> forInteger() {
        return value -> "Int(" + value + ")";
    }

    static Show<String> forString() {
        return value -> "\"" + value + "\"";
    }

    static Show<Double> forDouble() {
        return value -> "Double(" + value + ")";
    }

    static Show<Boolean> forBoolean() {
        return value -> value ? "True" : "False";
    }

    static <T> Show<T> forAny() {
        return value -> value == null ? "null" : value.toString();
    }

    static <T> Show<List<T>> forList(Show<T> elementShow) {
        return list -> list.stream().map(elementShow::show)
                .collect(Collectors.joining(", ", "List(", ")"));
    }

    static <K, V> Show<Map<K, V>> forMap(Show<K> keyShow, Show<V> valueShow) {
        return map -> map.entrySet().stream()
                .map(e -> keyShow.show(e.getKey()) + " -> " + valueShow.show(e.getValue()))
                .collect(Collectors.joining(", ", "Map(", ")"));
    }

    // ============================================================================
    // Utility Methods
    // ============================================================================

    /**
     * Print a value using its Show instance.
     */
    static <T> void print(T value, Show<T> show) {
        System.out.println(show.show(value));
    }

    /**
     * Show a value using its Show instance.
     */
    static <T> String showValue(T value, Show<T> show) {
        return show.show(value);
    }

    // ============================================================================
    // Registry-Based Lookup (Version 2 from blog post)
    // ============================================================================

    /**
     * Global registry for Show instances. This simulates Scala's implicit
     * resolution, but at runtime.
     */
    class Registry {
        private static final Map<Class<?>, Show<?>> instances = new ConcurrentHashMap<>();

        static {
            // Register default instances
            register(Integer.class, Show.forInteger());
            register(String.class, Show.forString());
            register(Double.class, Show.forDouble());
            register(Boolean.class, Show.forBoolean());
        }

        /**
         * Register a Show instance for a type.
         */
        public static <T> void register(Class<T> clazz, Show<T> instance) {
            instances.put(clazz, instance);
        }

        /**
         * Look up a Show instance for a type.
         */
        @SuppressWarnings("unchecked")
        public static <T> Optional<Show<T>> lookup(Class<T> clazz) {
            return Optional.ofNullable((Show<T>) instances.get(clazz));
        }

        /**
         * Get or create a Show instance for a type (like Scala's summon).
         */
        @SuppressWarnings("unchecked")
        public static <T> Show<T> summon(Class<T> clazz) {
            return (Show<T>) instances.computeIfAbsent(clazz, c -> Show.forAny());
        }
    }

    // ============================================================================
    // ServiceLoader-Based Discovery (Version 3 from blog post)
    // ============================================================================

    /**
     * ServiceLoader-based registry for Show instances. This uses Java's standard
     * ServiceLoader mechanism for plugin discovery.
     */
    class ServiceRegistry {
        private static final Map<Class<?>, Show<?>> cache = new ConcurrentHashMap<>();

        static {
            // Load all Show implementations from classpath
            ServiceLoader<ShowProvider> loader = ServiceLoader.load(ShowProvider.class);
            for (ShowProvider provider : loader) {
                cache.put(provider.targetType(), provider.createShow());
            }
        }

        /**
         * Look up a Show instance using ServiceLoader.
         */
        @SuppressWarnings("unchecked")
        public static <T> Show<T> lookup(Class<T> clazz) {
            return (Show<T>) cache.computeIfAbsent(clazz, c -> {
                // Reload in case new services were added
                ServiceLoader<ShowProvider> loader = ServiceLoader.load(ShowProvider.class);
                loader.reload();
                for (ShowProvider provider : loader) {
                    if (provider.targetType().equals(c)) {
                        return provider.createShow();
                    }
                }
                return Show.forAny();
            });
        }
    }
}
