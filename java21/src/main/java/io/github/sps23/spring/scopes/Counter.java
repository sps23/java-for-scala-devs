package io.github.sps23.spring.scopes;

/**
 * A simple stateful counter used to illustrate the difference between Spring
 * bean scopes.
 *
 * <ul>
 * <li><strong>Singleton</strong> (the default): one shared instance for the
 * entire application context. All classes that depend on this counter share the
 * same count — just like a global variable, but managed by Spring.</li>
 * <li><strong>Prototype</strong>: a brand-new instance is created every time
 * the bean is requested. Each caller gets their own count starting at
 * zero.</li>
 * </ul>
 *
 * <p>
 * Spring annotation examples:
 *
 * <pre>
 * // Default scope — one instance for the whole application
 * {@code @Component}
 * public class Counter { ... }
 *
 * // New instance every time — prototype scope
 * {@code @Component}
 * {@code @Scope("prototype")}
 * public class Counter { ... }
 * </pre>
 */
public class Counter {

    private final String name;
    private int count;

    public Counter(String name) {
        this.name = name;
        System.out.println(
                "Counter '" + name + "' created — instance #" + System.identityHashCode(this));
    }

    /** Increments the counter by one. */
    public void increment() {
        count++;
    }

    /** Returns the current count. */
    public int count() {
        return count;
    }

    /** Returns the name of this counter instance. */
    public String name() {
        return name;
    }

    /** Resets the counter back to zero. */
    public void reset() {
        count = 0;
    }
}
