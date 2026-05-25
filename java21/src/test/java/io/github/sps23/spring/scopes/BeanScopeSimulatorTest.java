package io.github.sps23.spring.scopes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link BeanScopeSimulator} — demonstrates the key behavioural
 * difference between singleton and prototype Spring bean scopes.
 *
 * <ul>
 * <li>Singleton: every call returns the <em>same</em> object reference.</li>
 * <li>Prototype: every call returns a <em>new</em> object reference.</li>
 * </ul>
 */
class BeanScopeSimulatorTest {

    private BeanScopeSimulator simulator;

    @BeforeEach
    void setUp() {
        var singletonCounter = new Counter("singleton");
        simulator = new BeanScopeSimulator(singletonCounter, () -> new Counter("prototype"));
    }

    // --- Singleton behaviour ---

    @Test
    void singletonAlwaysReturnsTheSameInstance() {
        var first = simulator.getSingleton();
        var second = simulator.getSingleton();

        assertSame(first, second, "Singleton must always return the exact same object reference");
    }

    @Test
    void singletonStateIsSharedAcrossAllCallers() {
        // Caller A increments
        simulator.getSingleton().increment();
        simulator.getSingleton().increment();

        // Caller B can see Caller A's increments — shared mutable state!
        assertEquals(2, simulator.getSingleton().count(),
                "Increments from any caller accumulate on the shared singleton");
    }

    // --- Prototype behaviour ---

    @Test
    void prototypeAlwaysReturnsANewInstance() {
        var first = simulator.newPrototype();
        var second = simulator.newPrototype();

        assertNotSame(first, second, "Prototype must return a new instance on every call");
    }

    @Test
    void prototypeInstancesDoNotShareState() {
        var counterA = simulator.newPrototype();
        var counterB = simulator.newPrototype();

        counterA.increment();
        counterA.increment();
        counterA.increment();

        // counterB is a completely separate object — its count is untouched
        assertEquals(0, counterB.count(),
                "Prototype instances must not share state — each starts at zero");
        assertEquals(3, counterA.count());
    }

    // --- The classic prototype-in-singleton trap ---

    @Test
    void injectingPrototypeThroughAFactoryGivesAFreshInstanceEachTime() {
        // This models what ObjectProvider<Counter> or @Lookup does in real Spring.
        // Each request through the factory produces a new Counter.
        var requestA = simulator.newPrototype();
        requestA.increment();

        var requestB = simulator.newPrototype();

        assertEquals(1, requestA.count());
        assertEquals(0, requestB.count(),
                "Using a factory ensures each 'request' gets its own prototype, "
                        + "not the stale instance injected at startup");
    }
}
