package io.github.sps23.interview.preparation.virtualthreads

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * Traditional thread-pool-based web scraper in Kotlin.
 *
 * This implementation uses a fixed thread pool, demonstrating the pre-virtual-thread
 * approach. Kotlin developers might also use coroutines for similar functionality.
 *
 * Limitations:
 * - Limited concurrency based on thread pool size
 * - Thread starvation during blocking I/O
 * - Memory overhead (~1MB per platform thread)
 *
 * For comparison with:
 * - Java: see WebScraperTraditional.java
 * - Virtual threads: see WebScraperVirtual.kt
 */
object WebScraperTraditional {
    /** Thread pool size - typically based on available processors. */
    private val THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2

    /** HTTP client for making requests. */
    private val httpClient: HttpClient =
        HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build()

    /**
     * Result of scraping a single URL.
     */
    data class ScrapedResult(
        val url: String,
        val statusCode: Int,
        val contentLength: Long,
        val error: String?,
    ) {
        val isSuccess: Boolean get() = error == null

        companion object {
            fun success(
                url: String,
                statusCode: Int,
                contentLength: Long,
            ) = ScrapedResult(url, statusCode, contentLength, null)

            fun failure(
                url: String,
                error: String,
            ) = ScrapedResult(url, -1, -1, error)
        }
    }

    /**
     * Scrapes multiple URLs using a fixed thread pool.
     *
     * Each URL becomes a task submitted to the pool. Tasks queue up waiting for
     * available threads.
     *
     * @param urls list of URLs to scrape
     * @return list of results
     */
    fun scrapeAll(urls: List<String>): List<ScrapedResult> {
        val executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)
        return try {
            val tasks =
                urls.map { url ->
                    Callable { scrapeUrl(url) }
                }
            val futures = executor.invokeAll(tasks)
            futures.map { it.get() }
        } finally {
            executor.shutdown()
        }
    }

    /**
     * Scrapes a single URL (blocking operation).
     */
    private fun scrapeUrl(url: String): ScrapedResult =
        try {
            val request =
                HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build()

            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            ScrapedResult.success(url, response.statusCode(), response.body().length.toLong())
        } catch (e: Exception) {
            ScrapedResult.failure(url, e.message ?: "Unknown error")
        }

    /** Returns thread pool size for comparison. */
    fun threadPoolSize(): Int = THREAD_POOL_SIZE
}

fun main() {
    println("=== Traditional Thread Pool Web Scraper (Kotlin) ===")
    println("Thread pool size: ${WebScraperTraditional.threadPoolSize()}")
    println()

    val urls =
        listOf(
            "https://httpbin.org/delay/1",
            "https://httpbin.org/get",
            "https://httpbin.org/headers",
            "https://httpbin.org/ip",
            "https://httpbin.org/user-agent",
        )

    val startTime = System.currentTimeMillis()
    val results = WebScraperTraditional.scrapeAll(urls)
    val duration = System.currentTimeMillis() - startTime

    println("Results:")
    results.forEach { result ->
        if (result.isSuccess) {
            println("  ✓ ${result.url} - ${result.contentLength} bytes")
        } else {
            println("  ✗ ${result.url} - ${result.error}")
        }
    }
    println()
    println("Total time: ${duration}ms")
    println("Average per URL: ${duration / urls.size}ms")
}
