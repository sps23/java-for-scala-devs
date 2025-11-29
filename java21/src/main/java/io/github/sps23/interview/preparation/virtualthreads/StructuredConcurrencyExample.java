package io.github.sps23.interview.preparation.virtualthreads;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.StructuredTaskScope;

/**
 * Structured concurrency example using Java 21's StructuredTaskScope (Preview).
 *
 * <p>
 * Structured concurrency brings discipline to concurrent programming:
 * <ul>
 * <li>Tasks have a clear lifecycle - they start together and complete
 * together</li>
 * <li>Cancellation propagates automatically - if one task fails, others are
 * cancelled</li>
 * <li>Resource cleanup is guaranteed - no orphaned threads or leaked
 * resources</li>
 * <li>Stack traces are meaningful - you can see the full call chain</li>
 * </ul>
 *
 * <p>
 * For Scala developers: this is similar to ZIO's structured concurrency or Cats
 * Effect's Resource + Fiber patterns, but built into the JVM.
 *
 * <p>
 * Note: This is a preview feature in Java 21. Compile with --enable-preview
 * flag.
 *
 * @see WebScraperVirtual for basic virtual thread usage
 */
public class StructuredConcurrencyExample {

    private final HttpClient httpClient;

    public StructuredConcurrencyExample() {
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    /**
     * Represents a scraped page with title and content length.
     */
    public record PageInfo(String url, String title, int contentLength) {
    }

    /**
     * Demonstrates ShutdownOnFailure - fails fast if any task fails.
     *
     * <p>
     * Use case: When you need ALL results to proceed (e.g., aggregating data from
     * multiple required services).
     *
     * <p>
     * Behavior:
     * <ul>
     * <li>If any task throws an exception, all other tasks are cancelled</li>
     * <li>The scope throws the first exception encountered</li>
     * <li>Results are only available if all tasks succeed</li>
     * </ul>
     *
     * @param urls
     *            list of URLs - all must succeed
     * @return combined content length from all URLs
     * @throws Exception
     *             if any request fails
     */
    public long fetchAllOrFail(List<String> urls) throws Exception {
        // ShutdownOnFailure: cancel all tasks if any one fails
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            // Fork tasks - each runs in its own virtual thread
            List<StructuredTaskScope.Subtask<Integer>> subtasks = urls.stream()
                    .map(url -> scope.fork(() -> fetchContentLength(url))).toList();

            // Wait for all tasks (or first failure)
            scope.join(); // Blocks until all complete or one fails

            // Throws if any task failed
            scope.throwIfFailed();

            // All succeeded - aggregate results
            return subtasks.stream().mapToInt(StructuredTaskScope.Subtask::get).sum();
        }
    }

    /**
     * Demonstrates ShutdownOnSuccess - returns as soon as one task succeeds.
     *
     * <p>
     * Use case: Racing multiple equivalent services (e.g., trying multiple mirrors,
     * or implementing timeout with fallback).
     *
     * <p>
     * Behavior:
     * <ul>
     * <li>As soon as one task succeeds, all other tasks are cancelled</li>
     * <li>Returns the first successful result</li>
     * <li>Only throws if ALL tasks fail</li>
     * </ul>
     *
     * @param urls
     *            list of URLs - return first successful response
     * @return content length from first successful URL
     * @throws Exception
     *             if all requests fail
     */
    public int fetchAnySuccessful(List<String> urls) throws Exception {
        // ShutdownOnSuccess: return as soon as one succeeds
        try (var scope = new StructuredTaskScope.ShutdownOnSuccess<Integer>()) {

            // Fork all tasks
            for (String url : urls) {
                scope.fork(() -> fetchContentLength(url));
            }

            // Wait for first success (or all failures)
            scope.join();

            // Returns first successful result
            return scope.result();
        }
    }

    /**
     * Demonstrates combining multiple dependent fetches with structured
     * concurrency.
     *
     * <p>
     * Pattern: Fetch data from multiple sources, combine results only if all
     * succeed. This is common when aggregating data from microservices.
     */
    public record AggregatedData(int userDataSize, int productDataSize, int orderDataSize) {
    }

    /**
     * Fetches data from multiple services concurrently with fail-fast behavior.
     *
     * <p>
     * If any service fails, the others are automatically cancelled.
     *
     * @return aggregated data from all services
     * @throws Exception
     *             if any service call fails
     */
    public AggregatedData fetchAggregatedData() throws Exception {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

            // Fork three concurrent fetches
            var userData = scope.fork(() -> fetchContentLength("https://httpbin.org/json"));
            var productData = scope.fork(() -> fetchContentLength("https://httpbin.org/get"));
            var orderData = scope.fork(() -> fetchContentLength("https://httpbin.org/headers"));

            scope.join();
            scope.throwIfFailed();

            // All succeeded - combine results
            return new AggregatedData(userData.get(), productData.get(), orderData.get());
        }
    }

    /**
     * Helper method to fetch content length from a URL.
     */
    private int fetchContentLength(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url))
                .timeout(Duration.ofSeconds(30)).GET().build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("HTTP " + response.statusCode() + " for " + url);
        }

        return response.body().length();
    }

    /**
     * Example usage demonstrating structured concurrency patterns.
     */
    public static void main(String[] args) throws Exception {
        System.out.println("=== Structured Concurrency Examples ===");
        System.out.println();

        StructuredConcurrencyExample example = new StructuredConcurrencyExample();

        // Example 1: Fetch all or fail
        System.out.println("1. ShutdownOnFailure - All must succeed:");
        try {
            List<String> urls = List.of("https://httpbin.org/get", "https://httpbin.org/headers",
                    "https://httpbin.org/ip");
            long total = example.fetchAllOrFail(urls);
            System.out.println("   Total content length: " + total + " bytes");
        } catch (Exception e) {
            System.out.println("   Failed: " + e.getMessage());
        }

        System.out.println();

        // Example 2: Race - first success wins
        System.out.println("2. ShutdownOnSuccess - First success wins:");
        try {
            List<String> mirrors = List.of("https://httpbin.org/delay/2", // Slow
                    "https://httpbin.org/get", // Fast
                    "https://httpbin.org/delay/3" // Slower
            );
            int result = example.fetchAnySuccessful(mirrors);
            System.out.println("   First successful response: " + result + " bytes");
        } catch (Exception e) {
            System.out.println("   All failed: " + e.getMessage());
        }

        System.out.println();

        // Example 3: Aggregate from multiple services
        System.out.println("3. Aggregating data from multiple services:");
        try {
            AggregatedData data = example.fetchAggregatedData();
            System.out.println("   User data: " + data.userDataSize() + " bytes");
            System.out.println("   Product data: " + data.productDataSize() + " bytes");
            System.out.println("   Order data: " + data.orderDataSize() + " bytes");
        } catch (Exception e) {
            System.out.println("   Failed: " + e.getMessage());
        }

        System.out.println();
        System.out.println("Structured concurrency ensures:");
        System.out.println("- No orphaned threads");
        System.out.println("- Automatic cancellation propagation");
        System.out.println("- Clean resource management");
    }
}
