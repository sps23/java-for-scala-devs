package io.github.sps23.interview.preparation.concurrentcollections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Small concurrent collection demos for interview preparation.
 */
public final class ConcurrentCollectionsExamples {

    private ConcurrentCollectionsExamples() {
    }

    public static int loseOneUpdateWithHashMapRace() {
        var sharedPot = new HashMap<String, Integer>();
        sharedPot.put("spoons", 0);

        var start = new CountDownLatch(1);
        var bothRead = new CountDownLatch(2);
        var allowWrite = new CountDownLatch(1);

        var first = new Thread(() -> stagedHashMapIncrement(sharedPot, start, bothRead, allowWrite), "hashmap-chef-1");
        var second = new Thread(() -> stagedHashMapIncrement(sharedPot, start, bothRead, allowWrite), "hashmap-chef-2");

        first.start();
        second.start();
        start.countDown();
        await(bothRead);
        allowWrite.countDown();
        join(first);
        join(second);
        return sharedPot.get("spoons");
    }

    public static int incrementWithConcurrentHashMapMerge() {
        var sharedPot = new ConcurrentHashMap<String, Integer>();
        sharedPot.put("spoons", 0);

        var start = new CountDownLatch(1);
        var done = new CountDownLatch(2);

        var first = new Thread(() -> {
            await(start);
            sharedPot.merge("spoons", 1, Integer::sum);
            done.countDown();
        }, "concurrent-map-chef-1");

        var second = new Thread(() -> {
            await(start);
            sharedPot.merge("spoons", 1, Integer::sum);
            done.countDown();
        }, "concurrent-map-chef-2");

        first.start();
        second.start();
        start.countDown();
        await(done);
        return sharedPot.get("spoons");
    }

    public static List<String> drainOrdersWithConcurrentLinkedQueue() {
        var orders = new ConcurrentLinkedQueue<String>();
        orders.offer("ramen");
        orders.offer("udon");
        orders.offer("pho");

        var served = new ArrayList<String>();
        while (true) {
            var order = orders.poll();
            if (order == null) {
                return served;
            }
            served.add(order);
        }
    }

    public static List<String> drainOrdersWithBlockingQueue() {
        var orders = new LinkedBlockingQueue<String>();
        orders.offer("ramen");
        orders.offer("udon");
        orders.offer("pho");
        orders.offer("service-over");

        var served = new ArrayList<String>();
        while (true) {
            var order = take(orders);
            if ("service-over".equals(order)) {
                return served;
            }
            served.add(order);
        }
    }

    public static CopyOnWriteSnapshot copyOnWriteWaitersSnapshot() {
        var waiters = new CopyOnWriteArrayList<>(List.of("Ana", "Ben"));
        var iterated = new ArrayList<String>();
        for (var waiter : waiters) {
            iterated.add(waiter);
            if ("Ana".equals(waiter)) {
                waiters.add("Cara");
            }
        }
        return new CopyOnWriteSnapshot(iterated, List.copyOf(waiters));
    }

    public record CopyOnWriteSnapshot(List<String> iterated, List<String> finalView) {
    }

    private static void stagedHashMapIncrement(HashMap<String, Integer> sharedPot, CountDownLatch start,
            CountDownLatch bothRead, CountDownLatch allowWrite) {
        await(start);
        var observed = sharedPot.get("spoons");
        bothRead.countDown();
        await(allowWrite);
        sharedPot.put("spoons", observed + 1);
    }

    private static String take(LinkedBlockingQueue<String> orders) {
        try {
            var order = orders.poll(1, TimeUnit.SECONDS);
            if (order == null) {
                throw new IllegalStateException("Timed out while waiting for an order");
            }
            return order;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for an order", exception);
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
            throw new IllegalStateException("Interrupted while waiting for the demo threads", exception);
        }
    }
}
