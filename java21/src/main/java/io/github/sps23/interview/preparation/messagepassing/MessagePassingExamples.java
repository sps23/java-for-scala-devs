package io.github.sps23.interview.preparation.messagepassing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Message-passing demos that avoid shared mutable state.
 */
public final class MessagePassingExamples {
    private static final String STOP = "__stop__";
    private static final int STOP_TOP_UP = Integer.MIN_VALUE;

    private MessagePassingExamples() {
    }

    public static List<String> handOffOrdersWithBlockingQueue(List<String> incomingOrders) {
        var orders = new LinkedBlockingQueue<String>();
        var prepared = new ArrayList<String>();

        var kitchenWorker = new Thread(() -> {
            while (true) {
                var order = takeStringMessage(orders);
                if (STOP.equals(order)) {
                    return;
                }
                prepared.add("prepared:" + order);
            }
        }, "kitchen-worker");

        kitchenWorker.start();
        incomingOrders.forEach(orders::offer);
        orders.offer(STOP);
        join(kitchenWorker);
        return List.copyOf(prepared);
    }

    public static List<String> handOffEmailsWithNonBlockingQueue(List<String> outgoingEmails) {
        var mailbox = new ConcurrentLinkedQueue<String>();
        outgoingEmails.forEach(mailbox::offer);

        var sent = new ArrayList<String>();
        while (true) {
            var email = mailbox.poll();
            if (email == null) {
                return List.copyOf(sent);
            }
            sent.add("sent:" + email);
        }
    }

    public static int processWalletTopUpsWithSingleOwner(int producerCount, int topUpsPerProducer, int centsPerTopUp) {
        if (producerCount < 1) {
            throw new IllegalArgumentException("producerCount must be at least one");
        }
        if (topUpsPerProducer < 0) {
            throw new IllegalArgumentException("topUpsPerProducer cannot be negative");
        }
        if (centsPerTopUp < 0) {
            throw new IllegalArgumentException("centsPerTopUp cannot be negative");
        }

        var topUpMessages = new LinkedBlockingQueue<Integer>();
        var start = new CountDownLatch(1);
        var producersDone = new CountDownLatch(producerCount);
        var finalBalanceInCents = new AtomicInteger(0);

        var walletOwner = new Thread(() -> {
            var localBalance = 0;
            while (true) {
                var message = takeIntMessage(topUpMessages);
                if (message == STOP_TOP_UP) {
                    finalBalanceInCents.set(localBalance);
                    return;
                }
                localBalance += message;
            }
        }, "wallet-owner");
        walletOwner.start();

        for (var producerIndex = 0; producerIndex < producerCount; producerIndex++) {
            var producer = new Thread(() -> {
                await(start);
                for (var i = 0; i < topUpsPerProducer; i++) {
                    put(topUpMessages, centsPerTopUp);
                }
                producersDone.countDown();
            }, "top-up-producer-" + producerIndex);
            producer.start();
        }

        start.countDown();
        await(producersDone);
        put(topUpMessages, STOP_TOP_UP);
        join(walletOwner);
        return finalBalanceInCents.get();
    }

    private static String takeStringMessage(LinkedBlockingQueue<String> queue) {
        try {
            var value = queue.poll(1, TimeUnit.SECONDS);
            if (value == null) {
                throw new IllegalStateException("Timed out while waiting for a message");
            }
            return value;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for a message", exception);
        }
    }

    private static int takeIntMessage(LinkedBlockingQueue<Integer> queue) {
        try {
            var value = queue.poll(1, TimeUnit.SECONDS);
            if (value == null) {
                throw new IllegalStateException("Timed out while waiting for a message");
            }
            return value;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for a message", exception);
        }
    }

    private static void put(LinkedBlockingQueue<Integer> queue, int message) {
        try {
            queue.put(message);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while handing off a message", exception);
        }
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
            throw new IllegalStateException("Interrupted while waiting for the demo thread", exception);
        }
    }
}
