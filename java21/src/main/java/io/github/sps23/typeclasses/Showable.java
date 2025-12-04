package io.github.sps23.typeclasses;

import java.util.function.Function;

/**
 * Wrapper providing extension-method-like syntax for Show (Version 4 from blog
 * post).
 *
 * This allows for fluent APIs like: Showable.of(42,
 * Show.forInteger()).inspect().map(...).print()
 */
public final class Showable<T> {
    private final T value;
    private final Show<T> show;

    private Showable(T value, Show<T> show) {
        this.value = value;
        this.show = show;
    }

    /**
     * Create a Showable with an explicit Show instance.
     */
    public static <T> Showable<T> of(T value, Show<T> show) {
        return new Showable<>(value, show);
    }

    /**
     * Create a Showable using registry lookup.
     */
    public static <T> Showable<T> of(T value, Class<T> clazz) {
        return new Showable<>(value, Show.Registry.summon(clazz));
    }

    /**
     * Get the string representation.
     */
    public String show() {
        return show.show(value);
    }

    /**
     * Print the value to stdout.
     */
    public void print() {
        System.out.println(show());
    }

    /**
     * Print with inspection message.
     */
    public Showable<T> inspect() {
        System.out.println("Inspecting: " + show());
        return this;
    }

    /**
     * Map the value to a new type.
     */
    public <U> Showable<U> map(Function<T, U> f, Show<U> newShow) {
        return new Showable<>(f.apply(value), newShow);
    }

    /**
     * Unwrap the underlying value.
     */
    public T unwrap() {
        return value;
    }
}
