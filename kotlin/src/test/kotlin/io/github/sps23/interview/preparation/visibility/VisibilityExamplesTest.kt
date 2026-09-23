package io.github.sps23.interview.preparation.visibility

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@DisplayName("Visibility Kotlin tests")
class VisibilityExamplesTest {
    @Test
    @DisplayName("Should let a worker observe a volatile stop flag update")
    fun shouldLetAWorkerObserveAVolatileStopFlagUpdate() {
        val flag = VisibilityExamples.VolatileRunningFlag()
        val started = CountDownLatch(1)
        val stopped = CountDownLatch(1)

        val worker = flag.startWorker(started, stopped)

        assertTrue(started.await(1, TimeUnit.SECONDS))
        flag.stop()

        assertTrue(stopped.await(1, TimeUnit.SECONDS))
        worker.join(1_000L)
        assertFalse(worker.isAlive)
    }

    @Test
    @DisplayName("Should show that volatile counter increments can still lose updates")
    fun shouldShowThatVolatileCounterIncrementsCanStillLoseUpdates() {
        val counter = VisibilityExamples.VolatileCounter()

        val finalValue = counter.loseOneIncrementDeterministically()

        assertEquals(1, finalValue)
        assertEquals(1, counter.currentValue())
    }
}
