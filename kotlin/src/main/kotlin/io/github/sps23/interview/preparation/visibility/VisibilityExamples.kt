package io.github.sps23.interview.preparation.visibility

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object VisibilityExamples {
    class VolatileRunningFlag {
        @Volatile
        private var running = true

        fun startWorker(started: CountDownLatch, stopped: CountDownLatch): Thread {
            val worker =
                Thread(
                    {
                        started.countDown()
                        while (running) {
                            Thread.onSpinWait()
                        }
                        stopped.countDown()
                    },
                    "volatile-flag-worker",
                )
            worker.start()
            return worker
        }

        fun stop() {
            running = false
        }
    }

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

        private fun stagedIncrement(
            start: CountDownLatch,
            bothRead: CountDownLatch,
            allowWrite: CountDownLatch,
        ) {
            await(start)
            val observed = counter
            bothRead.countDown()
            await(allowWrite)
            counter = observed + 1
        }
    }

    private fun await(latch: CountDownLatch) {
        try {
            val completed = latch.await(1, TimeUnit.SECONDS)
            if (!completed) {
                throw IllegalStateException("Timed out while coordinating the demo")
            }
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("Interrupted while coordinating the demo", exception)
        }
    }

    private fun join(thread: Thread) {
        try {
            thread.join(1_000L)
            if (thread.isAlive) {
                throw IllegalStateException("Timed out while waiting for the demo thread")
            }
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("Interrupted while waiting for the demo thread", exception)
        }
    }
}
