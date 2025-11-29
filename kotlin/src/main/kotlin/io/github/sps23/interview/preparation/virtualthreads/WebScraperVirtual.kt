package io.github.sps23.interview.preparation.virtualthreads

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.Executors

/**
 * Virtual threads-based web scraper in Kotlin.
 *
 * This implementation uses Java 21's virtual threads, demonstrating how Kotlin
 * developers can leverage Project Loom features alongside coroutines.
 *
 * Key benefits over traditional thread pools:
 * - Massive concurrency (millions of virtual threads)
 * - Efficient blocking I/O (virtual threads unmount during wait)
 * - Minimal memory overhead (KB vs MB per thread)
 * - Simple blocking code style
 *
 * For Kotlin developers: Virtual threads complement coroutines. Use coroutines
 * for structured async code within your Kotlin codebase. Use virtual threads
 * when interoperating with Java libraries or for simpler blocking-style code.
 *
 * For comparison with:
 * - Java: see WebScraperVirtual.java
 * - Traditional: see WebScraperTraditional.kt
 */
object WebScraperVirtual {
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
     * Scrapes multiple URLs using virtual threads - one thread per URL.
     *
     * With virtual threads, creating one thread per task is efficient. The JVM
     * handles scheduling and resource management automatically.
     *
     * Comparison with traditional approach:
     * - Traditional: 1000 URLs with 16 threads = max 16 concurrent requests
     * - Virtual: 1000 URLs = 1000 concurrent requests
     *
     * @param urls list of URLs to scrape
     * @return list of results
     */
    fun scrapeAll(urls: List<String>): List<ScrapedResult> =
        // newVirtualThreadPerTaskExecutor() - the key API for virtual threads
        // Each submitted task runs on its own virtual thread
        Executors.newVirtualThreadPerTaskExecutor().use { executor ->
            val futures = urls.map { url -> executor.submit<ScrapedResult> { scrapeUrl(url) } }
            futures.map { it.get() }
        }

    /**
     * Scrapes a single URL.
     *
     * The code is identical to the traditional version! The magic happens at
     * the JVM level:
     * - When httpClient.send() blocks, the virtual thread is "parked"
     * - The carrier thread is released to run other virtual threads
     * - When the response arrives, the virtual thread resumes
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

    /**
     * Alternative: Using Thread.startVirtualThread() directly.
     *
     * For simple cases where you want to start a virtual thread without an executor.
     */
    fun scrapeUrlAsync(
        url: String,
        callback: (ScrapedResult) -> Unit,
    ) {
        Thread.startVirtualThread { callback(scrapeUrl(url)) }
    }

    /**
     * Alternative: Using Thread.ofVirtual() builder for more control.
     *
     * The builder pattern allows setting thread name, daemon status, etc.
     */
    fun scrapeUrlWithBuilder(
        url: String,
        callback: (ScrapedResult) -> Unit,
    ): Thread =
        Thread.ofVirtual()
            .name("scraper-", 0)
            .start { callback(scrapeUrl(url)) }
}

fun main() {
    println("=== Virtual Threads Web Scraper (Kotlin) ===")
    println("Using: Executors.newVirtualThreadPerTaskExecutor()")
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
    val results = WebScraperVirtual.scrapeAll(urls)
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
    println()
    println("Note: With virtual threads, all URLs are fetched concurrently!")
    println("The total time should be close to the slowest single request.")
}
