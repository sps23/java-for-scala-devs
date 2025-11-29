package io.github.sps23.interview.preparation.virtualthreads;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Traditional thread-pool-based web scraper demonstrating pre-virtual thread
 * approach.
 *
 * <p>
 * This implementation uses a fixed thread pool, which has several limitations:
 * <ul>
 * <li>Limited concurrency - only as many concurrent requests as threads in
 * pool</li>
 * <li>Thread starvation - blocking I/O ties up threads</li>
 * <li>Memory overhead - each platform thread uses ~1MB of stack memory</li>
 * <li>Scalability issues - handling thousands of concurrent requests requires
 * tuning</li>
 * </ul>
 *
 * <p>
 * For Scala developers: this is similar to using a fixed-size thread pool with
 * Future.traverse on a bounded ExecutionContext. The same scalability
 * limitations apply.
 *
 * @see WebScraperVirtual for the virtual threads approach
 */
public class WebScraperTraditional {

    /** Fixed thread pool - typical size based on available processors. */
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;

    private final ExecutorService executor;
    private final HttpClient httpClient;

    /**
     * Creates a traditional web scraper with a fixed thread pool.
     *
     * <p>
     * Note: With only THREAD_POOL_SIZE threads, we can only make that many
     * concurrent requests. If we have 1000 URLs and 16 threads, requests will be
     * queued.
     */
    public WebScraperTraditional() {
        this.executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    /**
     * Represents the result of scraping a single URL.
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
     * Scrapes multiple URLs using a traditional thread pool.
     *
     * <p>
     * Each URL becomes a task submitted to the fixed thread pool. With many URLs,
     * tasks will queue up waiting for available threads.
     *
     * @param urls
     *            list of URLs to scrape
     * @return list of results, one per URL
     */
    public List<ScrapedResult> scrapeAll(List<String> urls) {
        List<Callable<ScrapedResult>> tasks = urls.stream()
                .map(url -> (Callable<ScrapedResult>) () -> scrapeUrl(url)).toList();

        List<ScrapedResult> results = new ArrayList<>();

        try {
            List<Future<ScrapedResult>> futures = executor.invokeAll(tasks);
            for (Future<ScrapedResult> future : futures) {
                results.add(future.get());
            }
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }

        return results;
    }

    /**
     * Scrapes a single URL (blocking operation).
     *
     * <p>
     * This method blocks the calling thread while waiting for the HTTP response.
     * With platform threads, this means the thread cannot do other work.
     */
    private ScrapedResult scrapeUrl(String url) {
        try {
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30)).GET().build();

            HttpResponse<String> response = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            return ScrapedResult.success(url, response.statusCode(), response.body().length());
        } catch (Exception e) {
            return ScrapedResult.failure(url, e.getMessage());
        }
    }

    /**
     * Shuts down the executor service.
     */
    public void shutdown() {
        executor.shutdown();
    }

    /**
     * Returns the thread pool size for comparison purposes.
     */
    public int getThreadPoolSize() {
        return THREAD_POOL_SIZE;
    }

    /**
     * Example usage demonstrating the traditional approach.
     */
    public static void main(String[] args) {
        System.out.println("=== Traditional Thread Pool Web Scraper ===");
        System.out.println("Thread pool size: " + THREAD_POOL_SIZE);
        System.out.println();

        // Sample URLs (using httpbin.org for testing)
        List<String> urls = List.of("https://httpbin.org/delay/1", "https://httpbin.org/get",
                "https://httpbin.org/headers", "https://httpbin.org/ip",
                "https://httpbin.org/user-agent");

        WebScraperTraditional scraper = new WebScraperTraditional();

        try {
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
        } finally {
            scraper.shutdown();
        }
    }
}
