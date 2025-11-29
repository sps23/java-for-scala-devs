package io.github.sps23.interview.preparation.virtualthreads

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.Duration
import java.util.concurrent.{ExecutorService, Executors}
import scala.jdk.CollectionConverters.*
import scala.util.{Try, Using}

/** Virtual threads-based web scraper in Scala 3.
  *
  * This implementation uses Java 21's virtual threads, demonstrating how Scala developers can
  * leverage Project Loom features.
  *
  * Key benefits over traditional thread pools:
  *   - Massive concurrency (millions of virtual threads)
  *   - Efficient blocking I/O (virtual threads unmount during wait)
  *   - Minimal memory overhead (KB vs MB per thread)
  *   - Simple blocking code style
  *
  * For Scala developers familiar with ZIO or Cats Effect: Virtual threads provide similar benefits
  * to fibers, but at the JVM level. You can write blocking-style code that scales like async code.
  *
  * For comparison with:
  *   - Java: see WebScraperVirtual.java
  *   - Traditional: see WebScraperTraditional.scala
  */
object WebScraperVirtual:

  /** Result of scraping a single URL. */
  case class ScrapedResult(
      url: String,
      statusCode: Int,
      contentLength: Long,
      error: Option[String]
  ):
    def isSuccess: Boolean = error.isEmpty

  object ScrapedResult:
    def success(url: String, statusCode: Int, contentLength: Long): ScrapedResult =
      ScrapedResult(url, statusCode, contentLength, None)

    def failure(url: String, error: String): ScrapedResult =
      ScrapedResult(url, -1, -1, Some(error))

  /** HTTP client for making requests. */
  private val httpClient: HttpClient = HttpClient
    .newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .build()

  /** Scrapes multiple URLs using virtual threads - one thread per URL.
    *
    * With virtual threads, creating one thread per task is efficient. The JVM handles scheduling
    * and resource management automatically.
    *
    * Comparison with traditional approach:
    *   - Traditional: 1000 URLs with 16 threads = max 16 concurrent requests
    *   - Virtual: 1000 URLs = 1000 concurrent requests
    *
    * @param urls
    *   list of URLs to scrape
    * @return
    *   list of results
    */
  def scrapeAll(urls: List[String]): List[ScrapedResult] =
    // newVirtualThreadPerTaskExecutor() - the key API for virtual threads
    // Each submitted task runs on its own virtual thread
    Using.resource(Executors.newVirtualThreadPerTaskExecutor()) { executor =>
      val futures = urls.map(url => executor.submit(() => scrapeUrl(url)))
      futures.map(_.get()).toList
    }

  /** Scrapes a single URL.
    *
    * The code is identical to the traditional version! The magic happens at the JVM level:
    *   - When httpClient.send() blocks, the virtual thread is "parked"
    *   - The carrier thread is released to run other virtual threads
    *   - When the response arrives, the virtual thread resumes
    */
  private def scrapeUrl(url: String): ScrapedResult =
    Try {
      val request = HttpRequest
        .newBuilder()
        .uri(URI.create(url))
        .timeout(Duration.ofSeconds(30))
        .GET()
        .build()

      val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
      ScrapedResult.success(url, response.statusCode(), response.body().length)
    }.recover { case e: Exception =>
      ScrapedResult.failure(url, e.getMessage)
    }.get

  /** Alternative: Using Thread.startVirtualThread() directly.
    *
    * For simple cases where you want to start a virtual thread without an executor.
    */
  def scrapeUrlAsync(url: String)(callback: ScrapedResult => Unit): Unit =
    Thread.startVirtualThread(() => callback(scrapeUrl(url)))

  /** Alternative: Using Thread.ofVirtual() builder for more control.
    *
    * The builder pattern allows setting thread name, daemon status, etc.
    */
  def scrapeUrlWithBuilder(url: String)(callback: ScrapedResult => Unit): Thread =
    Thread
      .ofVirtual()
      .name("scraper-", 0)
      .start(() => callback(scrapeUrl(url)))

@main def runVirtualScraper(): Unit =
  println("=== Virtual Threads Web Scraper (Scala 3) ===")
  println("Using: Executors.newVirtualThreadPerTaskExecutor()")
  println()

  val urls = List(
    "https://httpbin.org/delay/1",
    "https://httpbin.org/get",
    "https://httpbin.org/headers",
    "https://httpbin.org/ip",
    "https://httpbin.org/user-agent"
  )

  val startTime = System.currentTimeMillis()
  val results   = WebScraperVirtual.scrapeAll(urls)
  val duration  = System.currentTimeMillis() - startTime

  println("Results:")
  results.foreach { result =>
    if result.isSuccess then println(s"  ✓ ${result.url} - ${result.contentLength} bytes")
    else println(s"  ✗ ${result.url} - ${result.error.getOrElse("Unknown error")}")
  }
  println()
  println(s"Total time: ${duration}ms")
  println(s"Average per URL: ${duration / urls.size}ms")
  println()
  println("Note: With virtual threads, all URLs are fetched concurrently!")
  println("The total time should be close to the slowest single request.")
