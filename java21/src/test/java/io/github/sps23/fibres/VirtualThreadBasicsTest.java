package io.github.sps23.fibres;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for the virtual thread and structured-concurrency examples. */
class VirtualThreadBasicsTest {

    @Test
    @DisplayName("fetchUserAndOrders returns the combined result")
    void fetchUserAndOrdersReturnsCombinedResult() throws Exception {
        String result = VirtualThreadBasics.fetchUserAndOrders();
        assertEquals("Alice has 2 orders", result);
    }

    @Test
    @DisplayName("fetchUserAndOrders runs concurrently (not sequentially)")
    void fetchUserAndOrdersRunsConcurrently() throws Exception {
        CountDownLatch userStarted = new CountDownLatch(1);
        CountDownLatch ordersStarted = new CountDownLatch(1);

        String result = assertTimeoutPreemptively(Duration.ofSeconds(1),
                () -> VirtualThreadBasics.fetchUserAndOrders(() -> {
                    userStarted.countDown();
                    assertTrue(ordersStarted.await(1, TimeUnit.SECONDS));
                    return "Alice";
                }, () -> {
                    ordersStarted.countDown();
                    assertTrue(userStarted.await(1, TimeUnit.SECONDS));
                    return List.of("order-1", "order-2");
                }));

        assertEquals("Alice has 2 orders", result);
    }

    @Test
    @DisplayName("raceDataSources returns the faster cache result")
    void raceDataSourcesReturnsCacheResult() throws Exception {
        String result = StructuredTaskScopeDemo.raceDataSources();
        assertEquals("cached", result);
    }

    @Test
    @DisplayName("fetchAllUrls returns content for every URL")
    void fetchAllUrlsReturnsAllResults() throws Exception {
        List<String> urls = List.of("url1", "url2", "url3");
        List<String> results = StructuredTaskScopeDemo.fetchAllUrls(urls);
        assertEquals(3, results.size());
        assertTrue(results.contains("content of url1"));
        assertTrue(results.contains("content of url2"));
        assertTrue(results.contains("content of url3"));
    }
}
