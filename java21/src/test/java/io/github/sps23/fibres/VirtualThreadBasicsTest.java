package io.github.sps23.fibres;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
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
        long start = System.currentTimeMillis();
        VirtualThreadBasics.fetchUserAndOrders();
        long elapsed = System.currentTimeMillis() - start;
        // Sequential would be 100 + 80 = 180ms; parallel finishes in ~100ms
        assertTrue(elapsed < 170,
                "Expected concurrent execution in <170ms but took " + elapsed + "ms");
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
