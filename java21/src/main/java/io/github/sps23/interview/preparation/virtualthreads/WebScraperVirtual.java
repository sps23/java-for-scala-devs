package io.github.sps23.interview.preparation.virtualthreads;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Virtual threads-based web scraper demonstrating Java 21's Project Loom
 * features.
 *
 * <p>
 * This implementation uses virtual threads, which have several advantages:
 * <ul>
 * <li>Massive concurrency - millions of concurrent virtual threads
 * possible</li>
 * <li>Efficient blocking - virtual threads unmount from carrier threads during
 * I/O</li>
 * <li>Minimal memory - virtual threads use a few KB vs ~1MB for platform
 * threads</li>
 * <li>Simple code - blocking style without callback complexity</li>
 * </ul>
 *
 * <p>
 * Key differences from traditional thread pools:
 * <ul>
 * <li>{@code Executors.newVirtualThreadPerTaskExecutor()} creates a new virtual
 * thread per task</li>
 * <li>No thread pool sizing - virtual threads are cheap, create one per
 * task</li>
 * <li>Blocking I/O doesn't waste resources - JVM handles scheduling
 * efficiently</li>
 * </ul>
 *
 * <p>
 * For Scala developers: this is similar to using ZIO or Cats Effect fibers, but
 * at the JVM level. Virtual threads provide the same benefits (lightweight,
 * non-blocking semantics with blocking code) without needing a functional
 * effect system.
 *
 * @see WebScraperTraditional for the traditional thread pool approach
 */
public class WebScraperVirtual {

    private final HttpClient httpClient;

    /**
     * Creates a virtual thread-based web scraper.
     *
     * <p>
     * No thread pool configuration needed - virtual threads scale automatically.
     */
    public WebScraperVirtual() {
        // HttpClient uses virtual threads internally when available
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    /**
     * Represents the result of scraping a single URL.
     *
     * <p>
     * Same as WebScraperTraditional.ScrapedResult - using a separate record to keep
     * the examples self-contained.
     *
     * @param url
     *            the scraped URL
     * @param statusCode
     *            HTTP status code, or -1 if request failed
     * @param contentLength
     *            content length, or -1 if unavailable
     * @param error
     *            error message if request failed, null otherwise
     */
    public record ScrapedResult(String url, int statusCode, long contentLength, String error) {

        /** Factory method for successful scrape. */
        public static ScrapedResult success(String url, int statusCode, long contentLength) {
            return new ScrapedResult(url, statusCode, contentLength, null);
        }

        /** Factory method for failed scrape. */
        public static ScrapedResult failure(String url, String error) {
            return new ScrapedResult(url, -1, -1, error);
        }

        /** Returns true if the scrape was successful. */
        public boolean isSuccess() {
            return error == null;
        }
    }

    /**
     * Scrapes multiple URLs using virtual threads - one thread per URL.
     *
     * <p>
     * With virtual threads, we create a new thread for each URL. This is efficient
     * because virtual threads are lightweight (~KB) and blocking I/O doesn't waste
     * OS resources.
     *
     * <p>
     * Comparison with traditional approach:
     * <ul>
     * <li>Traditional: 1000 URLs with 16 threads = max 16 concurrent requests</li>
     * <li>Virtual: 1000 URLs = 1000 concurrent requests (all URLs fetched in
     * parallel)</li>
     * </ul>
     *
     * @param urls
     *            list of URLs to scrape
     * @return list of results, one per URL
     */
    public List<ScrapedResult> scrapeAll(List<String> urls) {
        // newVirtualThreadPerTaskExecutor() - the key API for virtual threads
        // Each submitted task runs on its own virtual thread
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<ScrapedResult>> futures = new ArrayList<>();

            // Submit all tasks - each runs on its own virtual thread
            for (String url : urls) {
                futures.add(executor.submit(() -> scrapeUrl(url)));
            }

            // Collect results
            List<ScrapedResult> results = new ArrayList<>();
            for (Future<ScrapedResult> future : futures) {
                try {
                    results.add(future.get());
                } catch (Exception e) {
                    // Handle interruption
                    Thread.currentThread().interrupt();
                }
            }

            return results;
        } // executor is auto-closed (try-with-resources)
    }

    /**
     * Scrapes a single URL using a virtual thread.
     *
     * <p>
     * The code is identical to the traditional version! The magic happens at the
     * JVM level:
     * <ul>
     * <li>When httpClient.send() blocks, the virtual thread is "parked"</li>
     * <li>The carrier thread is released to run other virtual threads</li>
     * <li>When the response arrives, the virtual thread resumes</li>
     * </ul>
     */
    private ScrapedResult scrapeUrl(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30)).GET().build();

            // This blocking call doesn't waste resources with virtual threads!
            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            return ScrapedResult.success(url, response.statusCode(), response.body().length());
        } catch (Exception e) {
            return ScrapedResult.failure(url, e.getMessage());
        }
    }

    /**
     * Alternative: Using Thread.startVirtualThread() directly.
     *
     * <p>
     * For simple cases where you want to start a virtual thread without an
     * executor.
     */
    public void scrapeUrlAsync(String url, java.util.function.Consumer<ScrapedResult> callback) {
        Thread.startVirtualThread(() -> {
            ScrapedResult result = scrapeUrl(url);
            callback.accept(result);
        });
    }

    /**
     * Alternative: Using Thread.ofVirtual() builder for more control.
     *
     * <p>
     * The builder pattern allows setting thread name, daemon status, etc.
     */
    public Thread scrapeUrlWithBuilder(String url,
            java.util.function.Consumer<ScrapedResult> callback) {
        return Thread.ofVirtual().name("scraper-", 0) // Named threads: scraper-0, scraper-1, etc.
                .start(() -> {
                    ScrapedResult result = scrapeUrl(url);
                    callback.accept(result);
                });
    }

    /**
     * Example usage demonstrating the virtual threads approach.
     */
    public static void main(String[] args) {
        System.out.println("=== Virtual Threads Web Scraper ===");
        System.out.println("Using: Executors.newVirtualThreadPerTaskExecutor()");
        System.out.println();

        // Sample URLs (using httpbin.org for testing)
        List<String> urls = List.of("https://httpbin.org/delay/1", "https://httpbin.org/get",
                "https://httpbin.org/headers", "https://httpbin.org/ip",
                "https://httpbin.org/user-agent");

        WebScraperVirtual scraper = new WebScraperVirtual();

        long startTime = System.currentTimeMillis();
        List<ScrapedResult> results = scraper.scrapeAll(urls);
        long duration = System.currentTimeMillis() - startTime;

        System.out.println("Results:");
        for (ScrapedResult result : results) {
            if (result.isSuccess()) {
                System.out.printf("  ✓ %s - %d bytes%n", result.url(), result.contentLength());
            } else {
                System.out.printf("  ✗ %s - %s%n", result.url(), result.error());
            }
        }
        System.out.println();
        System.out.println("Total time: " + duration + "ms");
        System.out.println("Average per URL: " + (duration / urls.size()) + "ms");
        System.out.println();
        System.out.println("Note: With virtual threads, all URLs are fetched concurrently!");
        System.out.println("The total time should be close to the slowest single request.");
    }
}
