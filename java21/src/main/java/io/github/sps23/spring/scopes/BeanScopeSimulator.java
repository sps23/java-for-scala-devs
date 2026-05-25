package io.github.sps23.spring.scopes;

import java.util.function.Supplier;

/**
 * Demonstrates the behavioural difference between singleton and prototype bean
 * scopes without needing an actual Spring application context.
 *
 * <p>
 * In a real Spring application:
 * <ul>
 * <li>A <em>singleton</em> bean is created once, cached in the context, and
 * the same instance is returned every time it is requested.</li>
 * <li>A <em>prototype</em> bean is created afresh every time it is requested
 * from the context — Spring does <em>not</em> cache it.</li>
 * </ul>
 *
 * <p>
 * The "prototype factory" pattern shown here (using a {@link Supplier}) is how
 * you solve the classic problem of injecting a prototype bean into a singleton:
 * instead of injecting the prototype directly (which would give you a single,
 * stale instance), you inject a factory that creates a fresh one on demand.
 *
 * <p>
 * In Spring this is done with {@code ObjectProvider<T>},
 * {@code @Lookup}-annotated methods, or JSR-330 {@code Provider<T>}.
 */
public class BeanScopeSimulator {

    // Simulates a singleton — always the same instance
    private final Counter singletonCounter;

    // Simulates a prototype factory — produces a new Counter every time
    private final Supplier<Counter> prototypeFactory;

    public BeanScopeSimulator(Counter singletonCounter, Supplier<Counter> prototypeFactory) {
        this.singletonCounter = singletonCounter;
        this.prototypeFactory = prototypeFactory;
    }

    /**
     * Returns the shared singleton counter. Every call returns the exact same
     * object — increments accumulate across all callers.
     */
    public Counter getSingleton() {
        return singletonCounter;
    }

    /**
     * Creates and returns a fresh prototype counter. Every call produces a new
     * object — no shared state between callers.
     */
    public Counter newPrototype() {
        return prototypeFactory.get();
    }
}
