package io.github.sps23.interview.preparation.atomicity

import java.util.concurrent.CountDownLatch

object AtomicOperationsExamples {
    class VolatileCounter {
        @Volatile
        private var counter: Int = 0

        fun loseOneIncrementDeterministically(): Int {
            counter = 0

            val start = CountDownLatch(1)
            val bothRead = CountDownLatch(2)
            val allowWrite = CountDownLatch(1)

            val first = Thread({ stagedIncrement(start, bothRead, allowWrite) }, "counter-reader-1")
            val second = Thread({ stagedIncrement(start, bothRead, allowWrite) }, "counter-reader-2")

            first.start()
            second.start()

            start.countDown()
            await(bothRead)
            allowWrite.countDown()
            join(first)
            join(second)
            return counter
        }

        fun currentValue(): Int = counter

        private fun stagedIncrement(start: CountDownLatch, bothRead: CountDownLatch, allowWrite: CountDownLatch) {
            await(start)
            val observed = counter
            bothRead.countDown()
            await(allowWrite)
            counter = observed + 1
        }
    }

    private fun await(latch: CountDownLatch) {
        try {
            latch.await()
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("Interrupted while coordinating the demo", exception)
        }
    }

    private fun join(thread: Thread) {
        try {
            thread.join()
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException(
                "Interrupted while waiting for the demo threads",
                exception,
            )
        }
    }
}
