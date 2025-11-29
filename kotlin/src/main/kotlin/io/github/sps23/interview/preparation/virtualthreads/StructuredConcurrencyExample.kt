package io.github.sps23.interview.preparation.virtualthreads

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.StructuredTaskScope

/**
 * Structured concurrency example using Java 21's StructuredTaskScope in Kotlin.
 *
 * Structured concurrency brings discipline to concurrent programming:
 * - Tasks have a clear lifecycle (start together, complete together)
 * - Cancellation propagates automatically
 * - Resource cleanup is guaranteed
 * - Stack traces are meaningful
 *
 * For Kotlin developers: This complements Kotlin coroutines' structured concurrency.
 * While coroutines provide this within Kotlin code, StructuredTaskScope works at the
 * JVM level and can be used with any JVM language or blocking Java code.
 *
 * Note: This is a preview feature in Java 21. The JVM must be started with --enable-preview.
 *
 * For comparison with:
 * - Java: see StructuredConcurrencyExample.java
 * - Scala: see StructuredConcurrencyExample.scala
 */
object StructuredConcurrencyExample {
    /** HTTP client for making requests. */
    private val httpClient: HttpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()

    /**
     * Demonstrates ShutdownOnFailure - fails fast if any task fails.
     *
     * Use case: When you need ALL results to proceed (e.g., aggregating data from
     * multiple required services).
     *
     * Behavior:
     * - If any task throws an exception, all other tasks are cancelled
     * - The scope throws the first exception encountered
     * - Results are only available if all tasks succeed
     *
     * @param urls list of URLs - all must succeed
     * @return combined content length from all URLs
     */
    fun fetchAllOrFail(urls: List<String>): Long =
        StructuredTaskScope.ShutdownOnFailure().use { scope ->
            val subtasks = urls.map { url -> scope.fork { fetchContentLength(url) } }
            scope.join()
            scope.throwIfFailed()
            subtasks.sumOf { it.get().toLong() }
        }

    /**
     * Demonstrates ShutdownOnSuccess - returns as soon as one task succeeds.
     *
     * Use case: Racing multiple equivalent services (e.g., trying multiple mirrors).
     *
     * Behavior:
     * - As soon as one task succeeds, all other tasks are cancelled
     * - Returns the first successful result
     * - Only throws if ALL tasks fail
     *
     * @param urls list of URLs - return first successful response
     * @return content length from first successful URL
     */
    fun fetchAnySuccessful(urls: List<String>): Int =
        StructuredTaskScope.ShutdownOnSuccess<Int>().use { scope ->
            urls.forEach { url -> scope.fork { fetchContentLength(url) } }
            scope.join()
            scope.result()
        }

    /**
     * Aggregated data from multiple services.
     */
    data class AggregatedData(
        val userDataSize: Int,
        val productDataSize: Int,
        val orderDataSize: Int,
    )

    /**
     * Fetches data from multiple services concurrently with fail-fast behavior.
     *
     * If any service fails, the others are automatically cancelled.
     *
     * @return aggregated data from all services
     */
    fun fetchAggregatedData(): AggregatedData =
        StructuredTaskScope.ShutdownOnFailure().use { scope ->
            val userData = scope.fork { fetchContentLength("https://httpbin.org/json") }
            val productData = scope.fork { fetchContentLength("https://httpbin.org/get") }
            val orderData = scope.fork { fetchContentLength("https://httpbin.org/headers") }

            scope.join()
            scope.throwIfFailed()

            AggregatedData(userData.get(), productData.get(), orderData.get())
        }

    /**
     * Helper method to fetch content length from a URL.
     */
    private fun fetchContentLength(url: String): Int {
        val request =
            HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

        if (response.statusCode() != 200) {
            throw RuntimeException("HTTP ${response.statusCode()} for $url")
        }

        return response.body().length
    }
}

fun main() {
    println("=== Structured Concurrency Examples (Kotlin) ===")
    println()

    // Example 1: Fetch all or fail
    println("1. ShutdownOnFailure - All must succeed:")
    try {
        val urls =
            listOf(
                "https://httpbin.org/get",
                "https://httpbin.org/headers",
                "https://httpbin.org/ip",
            )
        val total = StructuredConcurrencyExample.fetchAllOrFail(urls)
        println("   Total content length: $total bytes")
    } catch (e: Exception) {
        println("   Failed: ${e.message}")
    }

    println()

    // Example 2: Race - first success wins
    println("2. ShutdownOnSuccess - First success wins:")
    try {
        val mirrors =
            listOf(
                "https://httpbin.org/delay/2",
                "https://httpbin.org/get",
                "https://httpbin.org/delay/3",
            )
        val result = StructuredConcurrencyExample.fetchAnySuccessful(mirrors)
        println("   First successful response: $result bytes")
    } catch (e: Exception) {
        println("   All failed: ${e.message}")
    }

    println()

    // Example 3: Aggregate from multiple services
    println("3. Aggregating data from multiple services:")
    try {
        val data = StructuredConcurrencyExample.fetchAggregatedData()
        println("   User data: ${data.userDataSize} bytes")
        println("   Product data: ${data.productDataSize} bytes")
        println("   Order data: ${data.orderDataSize} bytes")
    } catch (e: Exception) {
        println("   Failed: ${e.message}")
    }

    println()
    println("Structured concurrency ensures:")
    println("- No orphaned threads")
    println("- Automatic cancellation propagation")
    println("- Clean resource management")
}
