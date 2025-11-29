package io.github.sps23.interview.preparation.async;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A service that demonstrates asynchronous data aggregation using
 * CompletableFuture.
 *
 * <p>
 * This class showcases key CompletableFuture concepts for Scala developers:
 * <ul>
 * <li>{@code supplyAsync()} - Similar to Scala's {@code Future { ... }}</li>
 * <li>{@code thenApply()} - Similar to Scala's {@code map}</li>
 * <li>{@code thenCompose()} - Similar to Scala's {@code flatMap}</li>
 * <li>{@code allOf()} - Similar to Scala's {@code Future.sequence}</li>
 * <li>{@code anyOf()} - Similar to Scala's {@code Future.firstCompletedOf}</li>
 * <li>{@code completeOnTimeout()} - Built-in timeout with fallback (Java
 * 9+)</li>
 * <li>{@code orTimeout()} - Built-in timeout that fails (Java 9+)</li>
 * <li>{@code exceptionally()} - Similar to Scala's {@code recover}</li>
 * <li>{@code handle()} - Similar to Scala's {@code transform}</li>
 * </ul>
 *
 * <p>
 * Example usage:
 *
 * <pre>{@code
 * var aggregator = new DataAggregatorService();
 * var result = aggregator.aggregateFromAllApis(Duration.ofSeconds(5));
 * result.thenAccept(data -> System.out.println("Got " + data.successCount() + " responses"));
 * }</pre>
 */
public class DataAggregatorService {

    private final ExecutorService executor;
    private final Duration defaultTimeout;

    /**
     * Creates a new DataAggregatorService with default settings.
     */
    public DataAggregatorService() {
        this(Executors.newVirtualThreadPerTaskExecutor(), Duration.ofSeconds(10));
    }

    /**
     * Creates a new DataAggregatorService with custom executor and timeout.
     *
     * @param executor
     *            the executor to use for async operations
     * @param defaultTimeout
     *            the default timeout for API calls
     */
    public DataAggregatorService(ExecutorService executor, Duration defaultTimeout) {
        this.executor = executor;
        this.defaultTimeout = defaultTimeout;
    }

    /**
     * Fetches data from a simulated weather API.
     *
     * <p>
     * Demonstrates {@code supplyAsync()} with a custom executor, similar to:
     *
     * <pre>
     * // Scala
     * Future {
     *   // async computation
     * }(executionContext)
     * </pre>
     *
     * @return CompletableFuture containing the API response
     */
    public CompletableFuture<ApiResponse> fetchWeatherData() {
        return CompletableFuture.supplyAsync(() -> {
            simulateApiCall("Weather API", 150);
            return ApiResponse.of("weather", "{\"temp\": 22, \"unit\": \"celsius\"}");
        }, executor);
    }

    /**
     * Fetches data from a simulated traffic API.
     *
     * @return CompletableFuture containing the API response
     */
    public CompletableFuture<ApiResponse> fetchTrafficData() {
        return CompletableFuture.supplyAsync(() -> {
            simulateApiCall("Traffic API", 200);
            return ApiResponse.of("traffic", "{\"congestion\": \"moderate\"}");
        }, executor);
    }

    /**
     * Fetches data from a simulated news API.
     *
     * @return CompletableFuture containing the API response
     */
    public CompletableFuture<ApiResponse> fetchNewsData() {
        return CompletableFuture.supplyAsync(() -> {
            simulateApiCall("News API", 100);
            return ApiResponse.of("news", "{\"headlines\": [\"Tech stocks rise\"]}");
        }, executor);
    }

    /**
     * Fetches data from a slow API that may timeout.
     *
     * <p>
     * Demonstrates {@code completeOnTimeout()} for providing a fallback value when
     * an operation takes too long. Similar to Scala pattern:
     *
     * <pre>
     * // Scala
     * Future.firstCompletedOf(Seq(
     *   actualFuture,
     *   Future { Thread.sleep(timeout); fallbackValue }
     * ))
     * </pre>
     *
     * @param timeout
     *            maximum time to wait
     * @return CompletableFuture with response or timeout fallback
     */
    public CompletableFuture<ApiResponse> fetchSlowApiWithFallback(Duration timeout) {
        var fallback = ApiResponse.of("slow-api", "{\"status\": \"timeout_fallback\"}");
        return CompletableFuture.supplyAsync(() -> {
            simulateApiCall("Slow API", 5000); // Intentionally slow
            return ApiResponse.of("slow-api", "{\"data\": \"actual_response\"}");
        }, executor).completeOnTimeout(fallback, timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * Fetches data from an unreliable API with exception handling.
     *
     * <p>
     * Demonstrates {@code exceptionally()} for handling failures, similar to:
     *
     * <pre>
     * // Scala
     * future.recover {
     *   case ex: Exception => fallbackValue
     * }
     * </pre>
     *
     * @return CompletableFuture with response or error fallback
     */
    public CompletableFuture<ApiResponse> fetchUnreliableApiWithRecovery() {
        return CompletableFuture.supplyAsync(() -> {
            simulateApiCall("Unreliable API", 100);
            if (Math.random() < 0.5) {
                throw new RuntimeException("API temporarily unavailable");
            }
            return ApiResponse.of("unreliable", "{\"status\": \"success\"}");
        }, executor).exceptionally(ex -> ApiResponse.of("unreliable",
                "{\"status\": \"recovered\", \"error\": \"" + ex.getMessage() + "\"}"));
    }

    /**
     * Transforms API response using thenApply (map operation).
     *
     * <p>
     * Demonstrates {@code thenApply()} which is analogous to Scala's {@code map}:
     *
     * <pre>
     * // Scala
     * future.map(response => response.data.toUpperCase)
     * </pre>
     *
     * @param responseFuture
     *            the future to transform
     * @return CompletableFuture with transformed data
     */
    public CompletableFuture<String> transformResponse(
            CompletableFuture<ApiResponse> responseFuture) {
        return responseFuture.thenApply(response -> response.data().toUpperCase());
    }

    /**
     * Chains API calls using thenCompose (flatMap operation).
     *
     * <p>
     * Demonstrates {@code thenCompose()} which is analogous to Scala's
     * {@code flatMap}:
     *
     * <pre>
     * // Scala
     * for {
     *   weather <- fetchWeather()
     *   enriched <- enrichWithLocation(weather)
     * } yield enriched
     * </pre>
     *
     * @return CompletableFuture with enriched response
     */
    public CompletableFuture<ApiResponse> fetchAndEnrichWeather() {
        return fetchWeatherData().thenCompose(weather -> enrichWithLocation(weather));
    }

    private CompletableFuture<ApiResponse> enrichWithLocation(ApiResponse weather) {
        return CompletableFuture.supplyAsync(() -> {
            simulateApiCall("Location API", 50);
            var enrichedData = weather.data().replace("}", ", \"location\": \"NYC\"}");
            return ApiResponse.of("weather-enriched", enrichedData);
        }, executor);
    }

    /**
     * Aggregates data from all APIs concurrently using allOf.
     *
     * <p>
     * Demonstrates {@code CompletableFuture.allOf()} which is similar to:
     *
     * <pre>
     * // Scala
     * Future.sequence(List(future1, future2, future3))
     * </pre>
     *
     * @param timeout
     *            maximum time to wait for all responses
     * @return CompletableFuture with aggregated data
     */
    public CompletableFuture<AggregatedData> aggregateFromAllApis(Duration timeout) {
        var weatherFuture = fetchWeatherData()
                .completeOnTimeout(ApiResponse.of("weather", "{\"error\": \"timeout\"}"),
                        timeout.toMillis(), TimeUnit.MILLISECONDS)
                .exceptionally(ex -> ApiResponse.of("weather",
                        "{\"error\": \"" + ex.getMessage() + "\"}"));

        var trafficFuture = fetchTrafficData()
                .completeOnTimeout(ApiResponse.of("traffic", "{\"error\": \"timeout\"}"),
                        timeout.toMillis(), TimeUnit.MILLISECONDS)
                .exceptionally(ex -> ApiResponse.of("traffic",
                        "{\"error\": \"" + ex.getMessage() + "\"}"));

        var newsFuture = fetchNewsData()
                .completeOnTimeout(ApiResponse.of("news", "{\"error\": \"timeout\"}"),
                        timeout.toMillis(), TimeUnit.MILLISECONDS)
                .exceptionally(
                        ex -> ApiResponse.of("news", "{\"error\": \"" + ex.getMessage() + "\"}"));

        return CompletableFuture.allOf(weatherFuture, trafficFuture, newsFuture).thenApply(v -> {
            var responses = List.of(weatherFuture.join(), trafficFuture.join(), newsFuture.join());
            var errors = new ArrayList<String>();
            for (var response : responses) {
                if (response.data().contains("error")) {
                    errors.add(response.source() + ": " + response.data());
                }
            }
            return errors.isEmpty()
                    ? AggregatedData.success(responses)
                    : AggregatedData.partial(responses, errors);
        });
    }

    /**
     * Gets the first available response using anyOf.
     *
     * <p>
     * Demonstrates {@code CompletableFuture.anyOf()} which is similar to:
     *
     * <pre>
     * // Scala
     * Future.firstCompletedOf(Seq(future1, future2, future3))
     * </pre>
     *
     * @return CompletableFuture with first available response
     */
    public CompletableFuture<ApiResponse> getFirstAvailableResponse() {
        var weather = fetchWeatherData();
        var traffic = fetchTrafficData();
        var news = fetchNewsData();

        return CompletableFuture.anyOf(weather, traffic, news)
                .thenApply(result -> (ApiResponse) result);
    }

    /**
     * Demonstrates handle() for both success and failure handling.
     *
     * <p>
     * {@code handle()} is similar to Scala's {@code transform}:
     *
     * <pre>
     * // Scala
     * future.transform {
     *   case Success(value) => Success(processValue(value))
     *   case Failure(ex) => Success(fallbackValue)
     * }
     * </pre>
     *
     * @return CompletableFuture with handled result
     */
    public CompletableFuture<String> fetchWithFullErrorHandling() {
        return CompletableFuture.supplyAsync(() -> {
            simulateApiCall("Handled API", 100);
            if (Math.random() < 0.3) {
                throw new RuntimeException("Random failure");
            }
            return ApiResponse.of("handled", "{\"status\": \"ok\"}");
        }, executor).handle((response, ex) -> {
            if (ex != null) {
                return "Error occurred: " + ex.getMessage();
            }
            return "Success: " + response.data();
        });
    }

    /**
     * Demonstrates orTimeout() which throws TimeoutException on timeout.
     *
     * <p>
     * Unlike completeOnTimeout(), this method fails the future if timeout occurs.
     * Similar to Scala pattern with Await.result:
     *
     * <pre>
     * // Scala (blocking, not recommended)
     * Try(Await.result(future, duration))
     * </pre>
     *
     * @param timeout
     *            maximum time to wait
     * @return CompletableFuture that may fail with TimeoutException
     */
    public CompletableFuture<ApiResponse> fetchWithStrictTimeout(Duration timeout) {
        return CompletableFuture.supplyAsync(() -> {
            simulateApiCall("Strict Timeout API", 5000);
            return ApiResponse.of("strict", "{\"data\": \"response\"}");
        }, executor).orTimeout(timeout.toMillis(), TimeUnit.MILLISECONDS);
    }

    private void simulateApiCall(String apiName, long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("API call interrupted: " + apiName, e);
        }
    }

    /**
     * Shuts down the executor service.
     */
    public void shutdown() {
        executor.shutdown();
    }
}
