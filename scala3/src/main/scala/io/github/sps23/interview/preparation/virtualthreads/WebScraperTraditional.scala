package io.github.sps23.interview.preparation.virtualthreads

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.Duration
import java.util.concurrent.{Callable, ExecutorService, Executors}
import scala.jdk.CollectionConverters.*
import scala.util.{Try, Using}

/** Traditional thread-pool-based web scraper in Scala 3.
  *
  * This implementation uses a fixed thread pool, demonstrating the pre-virtual-thread approach that
  * Scala developers might use with standard library futures.
  *
  * Limitations:
  *   - Limited concurrency based on thread pool size
  *   - Thread starvation during blocking I/O
  *   - Memory overhead (~1MB per platform thread)
  *
  * For comparison with:
  *   - Java: see WebScraperTraditional.java
  *   - Virtual threads: see WebScraperVirtual.scala
  */
object WebScraperTraditional:

  /** Thread pool size - typically based on available processors. */
  private val ThreadPoolSize: Int = Runtime.getRuntime.availableProcessors * 2

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

  /** Scrapes multiple URLs using a fixed thread pool.
    *
    * Each URL becomes a task submitted to the pool. Tasks queue up waiting for available threads.
    *
    * @param urls
    *   list of URLs to scrape
    * @return
    *   list of results
    */
  def scrapeAll(urls: List[String]): List[ScrapedResult] =
    Using.resource(Executors.newFixedThreadPool(ThreadPoolSize)) { executor =>
      val tasks: java.util.List[Callable[ScrapedResult]] =
        urls
          .map(url =>
            new Callable[ScrapedResult]:
              def call(): ScrapedResult = scrapeUrl(url)
          )
          .asJava

      val futures = executor.invokeAll(tasks)
      futures.asScala.map(_.get()).toList
    }

  /** Scrapes a single URL (blocking operation). */
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

  /** Returns thread pool size for comparison. */
  def threadPoolSize: Int = ThreadPoolSize

@main def runTraditionalScraper(): Unit =
  println("=== Traditional Thread Pool Web Scraper (Scala 3) ===")
  println(s"Thread pool size: ${WebScraperTraditional.threadPoolSize}")
  println()

  val urls = List(
    "https://httpbin.org/delay/1",
    "https://httpbin.org/get",
    "https://httpbin.org/headers",
    "https://httpbin.org/ip",
    "https://httpbin.org/user-agent"
  )

  val startTime = System.currentTimeMillis()
  val results   = WebScraperTraditional.scrapeAll(urls)
  val duration  = System.currentTimeMillis() - startTime

  println("Results:")
  results.foreach { result =>
    if result.isSuccess then println(s"  ✓ ${result.url} - ${result.contentLength} bytes")
    else println(s"  ✗ ${result.url} - ${result.error.getOrElse("Unknown error")}")
  }
  println()
  println(s"Total time: ${duration}ms")
  println(s"Average per URL: ${duration / urls.size}ms")
