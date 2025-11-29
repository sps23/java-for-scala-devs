package io.github.sps23.interview.preparation.async

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * A service that demonstrates asynchronous data aggregation using Kotlin Coroutines.
 *
 * This class showcases key Kotlin Coroutines concepts compared to Java's CompletableFuture
 * and Scala's Futures:
 *
 * - `async { ... }` - Similar to Java's `supplyAsync()` and Scala's `Future { ... }`
 * - `await()` - Gets the result, similar to Java's `join()` or `get()`
 * - `awaitAll()` - Similar to Java's `allOf()` and Scala's `Future.sequence`
 * - `select { }` - Similar to Java's `anyOf()` and Scala's `Future.firstCompletedOf`
 * - `withTimeoutOrNull()` - Built-in timeout with fallback (like Java 9+'s completeOnTimeout)
 * - `withTimeout()` - Built-in timeout that throws (like Java's orTimeout)
 * - try/catch for exceptions - Similar to Java's `exceptionally()` and Scala's `recover`
 * - Result.fold - Similar to Java's `handle()` and Scala's `transform`
 *
 * Example usage:
 * ```kotlin
 * val aggregator = DataAggregatorService()
 * runBlocking {
 *     val result = aggregator.aggregateFromAllApis(5.seconds)
 *     println("Got ${result.successCount()} responses")
 * }
 * ```
 */
class DataAggregatorService(
    private val defaultTimeout: Duration = 10.seconds,
) {
    /**
     * Fetches data from a simulated weather API.
     *
     * Demonstrates basic coroutine creation with `async`, similar to:
     * ```java
     * // Java
     * CompletableFuture.supplyAsync(() -> { ... }, executor)
     * ```
     * ```scala
     * // Scala
     * Future { ... }
     * ```
     *
     * @return Deferred containing the API response
     */
    fun CoroutineScope.fetchWeatherDataAsync(): Deferred<ApiResponse> =
        async(Dispatchers.IO) {
            simulateApiCall("Weather API", 150)
            ApiResponse.of("weather", """{"temp": 22, "unit": "celsius"}""")
        }

    /**
     * Fetches data from a simulated traffic API.
     *
     * @return Deferred containing the API response
     */
    fun CoroutineScope.fetchTrafficDataAsync(): Deferred<ApiResponse> =
        async(Dispatchers.IO) {
            simulateApiCall("Traffic API", 200)
            ApiResponse.of("traffic", """{"congestion": "moderate"}""")
        }

    /**
     * Fetches data from a simulated news API.
     *
     * @return Deferred containing the API response
     */
    fun CoroutineScope.fetchNewsDataAsync(): Deferred<ApiResponse> =
        async(Dispatchers.IO) {
            simulateApiCall("News API", 100)
            ApiResponse.of("news", """{"headlines": ["Tech stocks rise"]}""")
        }

    /**
     * Fetches data from a slow API with timeout and fallback.
     *
     * Demonstrates `withTimeoutOrNull` for timeout handling with fallback.
     * Similar to Java's `completeOnTimeout`:
     * ```java
     * // Java
     * future.completeOnTimeout(fallback, timeout, TimeUnit.MILLISECONDS)
     * ```
     * ```scala
     * // Scala
     * withTimeout(future, timeout, fallback)
     * ```
     *
     * @param timeout maximum time to wait
     * @return API response or timeout fallback
     */
    suspend fun fetchSlowApiWithFallback(timeout: Duration): ApiResponse {
        val fallback = ApiResponse.of("slow-api", """{"status": "timeout_fallback"}""")

        return withTimeoutOrNull(timeout) {
            simulateApiCall("Slow API", 5000) // Intentionally slow
            ApiResponse.of("slow-api", """{"data": "actual_response"}""")
        } ?: fallback
    }

    /**
     * Fetches data from an unreliable API with exception handling.
     *
     * Demonstrates try/catch for handling failures.
     * Similar to Java's `exceptionally` and Scala's `recover`:
     * ```java
     * // Java
     * future.exceptionally(ex -> fallbackValue)
     * ```
     * ```scala
     * // Scala
     * future.recover { case ex => fallbackValue }
     * ```
     *
     * @return API response or error fallback
     */
    suspend fun fetchUnreliableApiWithRecovery(): ApiResponse =
        try {
            simulateApiCall("Unreliable API", 100)
            if (Math.random() < 0.5) {
                throw RuntimeException("API temporarily unavailable")
            }
            ApiResponse.of("unreliable", """{"status": "success"}""")
        } catch (ex: Exception) {
            ApiResponse.of("unreliable", """{"status": "recovered", "error": "${ex.message}"}""")
        }

    /**
     * Transforms API response using map operation.
     *
     * In Kotlin coroutines, transformations are straightforward with suspend functions.
     * Similar to Java's `thenApply` and Scala's `map`:
     * ```java
     * // Java
     * future.thenApply(response -> response.data().toUpperCase())
     * ```
     * ```scala
     * // Scala
     * future.map(response => response.data.toUpperCase)
     * ```
     *
     * @param responseDeferred the deferred to transform
     * @return transformed data
     */
    suspend fun transformResponse(responseDeferred: Deferred<ApiResponse>): String = responseDeferred.await().data.uppercase()

    /**
     * Chains API calls sequentially.
     *
     * Kotlin coroutines make sequential composition natural and readable.
     * Similar to Java's `thenCompose` and Scala's `flatMap`:
     * ```java
     * // Java
     * fetchWeatherData().thenCompose(weather -> enrichWithLocation(weather))
     * ```
     * ```scala
     * // Scala
     * for {
     *   weather <- fetchWeatherData()
     *   enriched <- enrichWithLocation(weather)
     * } yield enriched
     * ```
     *
     * @return enriched response
     */
    suspend fun fetchAndEnrichWeather(): ApiResponse =
        coroutineScope {
            val weather = fetchWeatherDataAsync().await()
            enrichWithLocation(weather)
        }

    private suspend fun enrichWithLocation(weather: ApiResponse): ApiResponse {
        simulateApiCall("Location API", 50)
        val enrichedData = weather.data.replace("}", """, "location": "NYC"}""")
        return ApiResponse.of("weather-enriched", enrichedData)
    }

    /**
     * Aggregates data from all APIs concurrently using awaitAll.
     *
     * Demonstrates `awaitAll` which is similar to:
     * ```java
     * // Java
     * CompletableFuture.allOf(future1, future2, future3)
     *   .thenApply(v -> List.of(future1.join(), future2.join(), future3.join()))
     * ```
     * ```scala
     * // Scala
     * Future.sequence(List(future1, future2, future3))
     * ```
     *
     * @param timeout maximum time to wait for all responses
     * @return aggregated data
     */
    suspend fun aggregateFromAllApis(timeout: Duration): AggregatedData =
        coroutineScope {
            val weatherDeferred =
                async {
                    fetchWithTimeoutAndRecovery("weather", timeout) {
                        fetchWeatherDataAsync().await()
                    }
                }

            val trafficDeferred =
                async {
                    fetchWithTimeoutAndRecovery("traffic", timeout) {
                        fetchTrafficDataAsync().await()
                    }
                }

            val newsDeferred =
                async {
                    fetchWithTimeoutAndRecovery("news", timeout) {
                        fetchNewsDataAsync().await()
                    }
                }

            val responses = listOf(weatherDeferred, trafficDeferred, newsDeferred).awaitAll()
            val errors =
                responses
                    .filter { it.data.contains("error") }
                    .map { "${it.source}: ${it.data}" }

            if (errors.isEmpty()) {
                AggregatedData.success(responses)
            } else {
                AggregatedData.partial(responses, errors)
            }
        }

    private suspend fun fetchWithTimeoutAndRecovery(
        source: String,
        timeout: Duration,
        block: suspend () -> ApiResponse,
    ): ApiResponse =
        try {
            withTimeoutOrNull(timeout) {
                block()
            } ?: ApiResponse.of(source, """{"error": "timeout"}""")
        } catch (ex: Exception) {
            ApiResponse.of(source, """{"error": "${ex.message}"}""")
        }

    /**
     * Gets the first available response using select.
     *
     * Demonstrates `select` which is similar to:
     * ```java
     * // Java
     * CompletableFuture.anyOf(future1, future2, future3)
     *   .thenApply(result -> (ApiResponse) result)
     * ```
     * ```scala
     * // Scala
     * Future.firstCompletedOf(Seq(future1, future2, future3))
     * ```
     *
     * @return first available response
     */
    suspend fun getFirstAvailableResponse(): ApiResponse =
        coroutineScope {
            val weather = fetchWeatherDataAsync()
            val traffic = fetchTrafficDataAsync()
            val news = fetchNewsDataAsync()

            select {
                weather.onAwait { it }
                traffic.onAwait { it }
                news.onAwait { it }
            }
        }

    /**
     * Demonstrates Result.fold for both success and failure handling.
     *
     * Similar to Java's `handle` and Scala's `transform`:
     * ```java
     * // Java
     * future.handle((response, ex) -> {
     *   if (ex != null) return "Error: " + ex.getMessage();
     *   return "Success: " + response.data();
     * })
     * ```
     * ```scala
     * // Scala
     * future.transform {
     *   case Success(response) => Success(s"Success: ${response.data}")
     *   case Failure(ex) => Success(s"Error: ${ex.getMessage}")
     * }
     * ```
     *
     * @return handled result
     */
    suspend fun fetchWithFullErrorHandling(): String {
        val result =
            runCatching {
                simulateApiCall("Handled API", 100)
                if (Math.random() < 0.3) {
                    throw RuntimeException("Random failure")
                }
                ApiResponse.of("handled", """{"status": "ok"}""")
            }

        return result.fold(
            onSuccess = { response -> "Success: ${response.data}" },
            onFailure = { ex -> "Error occurred: ${ex.message}" },
        )
    }

    /**
     * Demonstrates withTimeout which throws TimeoutCancellationException on timeout.
     *
     * Similar to Java's `orTimeout`:
     * ```java
     * // Java
     * future.orTimeout(timeout, TimeUnit.MILLISECONDS)
     * ```
     *
     * @param timeout maximum time to wait
     * @return API response (may throw TimeoutCancellationException)
     */
    @Throws(TimeoutCancellationException::class)
    suspend fun fetchWithStrictTimeout(timeout: Duration): ApiResponse =
        withTimeout(timeout) {
            simulateApiCall("Strict Timeout API", 5000)
            ApiResponse.of("strict", """{"data": "response"}""")
        }

    @Suppress("UNUSED_PARAMETER")
    private suspend fun simulateApiCall(
        apiName: String,
        delayMs: Long,
    ) {
        delay(delayMs.milliseconds)
    }
}

/**
 * Example usage demonstrating the DataAggregatorService.
 */
fun main() =
    runBlocking {
        val aggregator = DataAggregatorService()

        println("Starting async data aggregation with Kotlin Coroutines...")

        // Example: Aggregate from all APIs
        val result = aggregator.aggregateFromAllApis(5.seconds)

        println("Aggregation complete:")
        println("  Success count: ${result.successCount()}")
        println("  Fully successful: ${result.isFullySuccessful()}")
        result.responses.forEach { r ->
            println("  - ${r.source}: ${r.data}")
        }
    }
