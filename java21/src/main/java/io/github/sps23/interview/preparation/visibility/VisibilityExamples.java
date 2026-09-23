package io.github.sps23.interview.preparation.visibility;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Visibility-focused examples for Java 21 interview preparation.
 */
public final class VisibilityExamples {

    private VisibilityExamples() {
    }

    public static final class VolatileRunningFlag {
        private volatile boolean running = true;

        public Thread startWorker(CountDownLatch started, CountDownLatch stopped) {
            var worker = new Thread(() -> {
                started.countDown();
                while (running) {
                    Thread.onSpinWait();
                }
                stopped.countDown();
            }, "volatile-flag-worker");
            worker.start();
            return worker;
        }

        public void stop() {
            running = false;
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

    private static void await(CountDownLatch latch) {
        try {
            var completed = latch.await(1, TimeUnit.SECONDS);
            if (!completed) {
                throw new IllegalStateException("Timed out while coordinating the demo");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while coordinating the demo", exception);
        }
    }

    private static void join(Thread thread) {
        try {
            thread.join(1_000L);
            if (thread.isAlive()) {
                throw new IllegalStateException("Timed out while waiting for the demo thread");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the demo thread", exception);
        }
    }
}
