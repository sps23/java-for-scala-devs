package io.github.sps23.fibres;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Basic virtual thread patterns in Java 21 – the counterpart to ZIO fibres.
 *
 * <p>
 * Virtual threads (Project Loom) give you millions of lightweight threads with
 * blocking-style code, similar to ZIO fibres but without the typed-error model.
 * Key API:
 * <ul>
 * <li>{@code Executors.newVirtualThreadPerTaskExecutor()} – creates a new
 * virtual thread per submitted task</li>
 * <li>{@code executor.submit(callable)} – schedules a task; returns a
 * {@link Future}</li>
 * <li>{@code future.get()} – blocks the <em>virtual</em> thread (not the
 * carrier) until the result is ready</li>
 * </ul>
 *
 * <p>
 * For comparison with Scala and Kotlin equivalents see:
 * <ul>
 * <li>Scala 3: BasicFibres.scala (ZIO fork/join)</li>
 * <li>Kotlin: CoroutineBasics.kt (async/await)</li>
 * </ul>
 */
public class VirtualThreadBasics {

    /**
     * Fetches a user and their orders concurrently using virtual threads.
     *
     * <p>
     * Both tasks run on separate virtual threads simultaneously. Total time ≈
     * max(100ms, 80ms) rather than 100ms + 80ms = 180ms.
     *
     * @return a summary string such as "Alice has 2 orders"
     * @throws Exception
     *             if either task fails
     */
    public static String fetchUserAndOrders() throws Exception {
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> userFuture = executor.submit(VirtualThreadBasics::fetchUser);
            Future<List<String>> orderFuture = executor.submit(VirtualThreadBasics::fetchOrders);
            String user = userFuture.get();
            List<String> orders = orderFuture.get();
            return user + " has " + orders.size() + " orders";
        }
    }

    /**
     * Simulates a remote user-profile fetch (100 ms latency).
     */
    static String fetchUser() throws InterruptedException {
        Thread.sleep(100);
        return "Alice";
    }

    /**
     * Simulates a remote order-history fetch (80 ms latency).
     */
    static List<String> fetchOrders() throws InterruptedException {
        Thread.sleep(80);
        return List.of("order-1", "order-2");
    }
}
