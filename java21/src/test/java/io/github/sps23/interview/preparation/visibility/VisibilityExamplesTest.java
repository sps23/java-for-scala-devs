package io.github.sps23.interview.preparation.visibility;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Visibility Java 21 tests")
class VisibilityExamplesTest {

    @Test
    @DisplayName("Should let a worker observe a volatile stop flag update")
    void shouldLetAWorkerObserveAVolatileStopFlagUpdate() throws InterruptedException {
        var flag = new VisibilityExamples.VolatileRunningFlag();
        var started = new CountDownLatch(1);
        var stopped = new CountDownLatch(1);

        var worker = flag.startWorker(started, stopped);

        assertTrue(started.await(1, TimeUnit.SECONDS));
        flag.stop();

        assertTrue(stopped.await(1, TimeUnit.SECONDS));
        worker.join(1_000L);
        assertFalse(worker.isAlive());
    }

    @Test
    @DisplayName("Should show that volatile counter increments can still lose updates")
    void shouldShowThatVolatileCounterIncrementsCanStillLoseUpdates() {
        var counter = new VisibilityExamples.VolatileCounter();

        var finalValue = counter.loseOneIncrementDeterministically();

        assertEquals(1, finalValue);
        assertEquals(1, counter.currentValue());
    }
}
