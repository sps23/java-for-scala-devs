package io.github.sps23.interview.preparation.atomicity;

import java.util.concurrent.CountDownLatch;

/**
 * Atomicity examples built around claiming the last available ticket.
 */
public final class AtomicOperationsExamples {

    private AtomicOperationsExamples() {
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while coordinating the demo", exception);
        }
    }

    private static void join(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the demo threads",
                    exception);
        }
    }

    public static final class VolatileCounter {
        private volatile int counter;

        public int loseOneIncrementDeterministically() {
            counter = 0;

            var start = new CountDownLatch(1);
            var bothRead = new CountDownLatch(2);
            var allowWrite = new CountDownLatch(1);

            var first = new Thread(() -> stagedIncrement(start, bothRead, allowWrite),
                    "counter-reader-1");
            var second = new Thread(() -> stagedIncrement(start, bothRead, allowWrite),
                    "counter-reader-2");

            first.start();
            second.start();

            start.countDown();
            await(bothRead);
            allowWrite.countDown();
            join(first);
            join(second);
            return counter;
        }

        public int currentValue() {
            return counter;
        }

        private void stagedIncrement(CountDownLatch start, CountDownLatch bothRead,
                CountDownLatch allowWrite) {
            await(start);
            var observed = counter;
            bothRead.countDown();
            await(allowWrite);
            counter = observed + 1;
        }
    }
}
