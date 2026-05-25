package io.github.sps23.spring.scopes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ConnectionPool} — demonstrates that the init/destroy
 * lifecycle methods work correctly in isolation, exactly as Spring would invoke
 * them via {@code @PostConstruct} and {@code @PreDestroy}.
 */
class ConnectionPoolTest {

    private ConnectionPool pool;

    @BeforeEach
    void setUp() {
        pool = new ConnectionPool("jdbc:postgresql://localhost:5432/mydb", 3);
    }

    @Test
    void poolIsClosedBeforeInitialisation() {
        assertFalse(pool.isOpen(),
                "Pool must start closed — Spring hasn't called @PostConstruct yet");
    }

    @Test
    void openInitialisesThePool() {
        pool.open();

        assertTrue(pool.isOpen());
        assertEquals(0, pool.getActiveConnections());
        assertEquals(3, pool.getMaxConnections());
    }

    @Test
    void borrowIncreasesActiveConnections() {
        pool.open();

        assertTrue(pool.borrow());
        assertTrue(pool.borrow());
        assertEquals(2, pool.getActiveConnections());
    }

    @Test
    void cannotBorrowBeyondCapacity() {
        pool.open();

        pool.borrow();
        pool.borrow();
        pool.borrow();

        assertFalse(pool.borrow(), "Fourth borrow should fail — pool is at max capacity");
    }

    @Test
    void releaseDecrementsActiveConnections() {
        pool.open();
        pool.borrow();
        pool.borrow();

        pool.release();
        assertEquals(1, pool.getActiveConnections());
    }

    @Test
    void borrowThrowsWhenPoolIsNotOpen() {
        // Pool was never initialised — @PostConstruct was never called
        assertThrows(IllegalStateException.class, pool::borrow,
                "Borrowing before open() should throw — lifecycle contract violated");
    }

    @Test
    void closeShutsDownThePool() {
        pool.open();
        pool.borrow();

        pool.close(); // simulates @PreDestroy

        assertFalse(pool.isOpen());
        assertEquals(0, pool.getActiveConnections(), "All connections should be released on close");
    }

    @Test
    void fullLifecycle() {
        // Simulates what Spring does: PostConstruct → use → PreDestroy
        pool.open(); // @PostConstruct
        assertTrue(pool.borrow());
        pool.release();
        pool.close(); // @PreDestroy

        assertFalse(pool.isOpen());
        assertEquals(0, pool.getActiveConnections());
    }
}
