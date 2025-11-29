package io.github.sps23.interview.preparation.virtualthreads

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.time.Duration
import java.util.concurrent.StructuredTaskScope
import scala.util.Using

/** Structured concurrency example using Java 21's StructuredTaskScope in Scala 3.
  *
  * Structured concurrency brings discipline to concurrent programming:
  *   - Tasks have a clear lifecycle (start together, complete together)
  *   - Cancellation propagates automatically
  *   - Resource cleanup is guaranteed
  *   - Stack traces are meaningful
  *
  * For Scala developers: This is similar to ZIO's structured concurrency (ZIO.foreachPar with
  * scoped resources) or Cats Effect's Resource + Fiber patterns.
  *
  * Note: This is a preview feature in Java 21. The JVM must be started with --enable-preview.
  *
  * For comparison with:
  *   - Java: see StructuredConcurrencyExample.java
  */
object StructuredConcurrencyExample:

  /** HTTP client for making requests. */
  private val httpClient: HttpClient = HttpClient
    .newBuilder()
    .connectTimeout(Duration.ofSeconds(10))
    .build()

  /** Demonstrates ShutdownOnFailure - fails fast if any task fails.
    *
    * Use case: When you need ALL results to proceed (e.g., aggregating data from multiple required
    * services).
    *
    * Behavior:
    *   - If any task throws an exception, all other tasks are cancelled
    *   - The scope throws the first exception encountered
    *   - Results are only available if all tasks succeed
    *
    * @param urls
    *   list of URLs - all must succeed
    * @return
    *   combined content length from all URLs
    */
  def fetchAllOrFail(urls: List[String]): Long =
    Using.resource(new StructuredTaskScope.ShutdownOnFailure()) { scope =>
      val subtasks = urls.map(url => scope.fork(() => fetchContentLength(url)))
      scope.join()
      scope.throwIfFailed()
      subtasks.map(_.get()).sum
    }

  /** Demonstrates ShutdownOnSuccess - returns as soon as one task succeeds.
    *
    * Use case: Racing multiple equivalent services (e.g., trying multiple mirrors).
    *
    * Behavior:
    *   - As soon as one task succeeds, all other tasks are cancelled
    *   - Returns the first successful result
    *   - Only throws if ALL tasks fail
    *
    * @param urls
    *   list of URLs - return first successful response
    * @return
    *   content length from first successful URL
    */
  def fetchAnySuccessful(urls: List[String]): Int =
    Using.resource(new StructuredTaskScope.ShutdownOnSuccess[Integer]()) { scope =>
      urls.foreach(url => scope.fork(() => fetchContentLength(url)))
      scope.join()
      scope.result()
    }

  /** Aggregated data from multiple services. */
  case class AggregatedData(userDataSize: Int, productDataSize: Int, orderDataSize: Int)

  /** Fetches data from multiple services concurrently with fail-fast behavior.
    *
    * If any service fails, the others are automatically cancelled.
    *
    * @return
    *   aggregated data from all services
    */
  def fetchAggregatedData(): AggregatedData =
    Using.resource(new StructuredTaskScope.ShutdownOnFailure()) { scope =>
      val userData    = scope.fork(() => fetchContentLength("https://httpbin.org/json"))
      val productData = scope.fork(() => fetchContentLength("https://httpbin.org/get"))
      val orderData   = scope.fork(() => fetchContentLength("https://httpbin.org/headers"))

      scope.join()
      scope.throwIfFailed()

      AggregatedData(userData.get(), productData.get(), orderData.get())
    }

  /** Helper method to fetch content length from a URL. */
  private def fetchContentLength(url: String): Int =
    val request = HttpRequest
      .newBuilder()
      .uri(URI.create(url))
      .timeout(Duration.ofSeconds(30))
      .GET()
      .build()

    val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())

    if response.statusCode() != 200 then
      throw new RuntimeException(s"HTTP ${response.statusCode()} for $url")

    response.body().length

@main def runStructuredConcurrencyExample(): Unit =
  println("=== Structured Concurrency Examples (Scala 3) ===")
  println()

  // Example 1: Fetch all or fail
  println("1. ShutdownOnFailure - All must succeed:")
  try
    val urls = List(
      "https://httpbin.org/get",
      "https://httpbin.org/headers",
      "https://httpbin.org/ip"
    )
    val total = StructuredConcurrencyExample.fetchAllOrFail(urls)
    println(s"   Total content length: $total bytes")
  catch case e: Exception => println(s"   Failed: ${e.getMessage}")

  println()

  // Example 2: Race - first success wins
  println("2. ShutdownOnSuccess - First success wins:")
  try
    val mirrors = List(
      "https://httpbin.org/delay/2",
      "https://httpbin.org/get",
      "https://httpbin.org/delay/3"
    )
    val result = StructuredConcurrencyExample.fetchAnySuccessful(mirrors)
    println(s"   First successful response: $result bytes")
  catch case e: Exception => println(s"   All failed: ${e.getMessage}")

  println()

  // Example 3: Aggregate from multiple services
  println("3. Aggregating data from multiple services:")
  try
    val data = StructuredConcurrencyExample.fetchAggregatedData()
    println(s"   User data: ${data.userDataSize} bytes")
    println(s"   Product data: ${data.productDataSize} bytes")
    println(s"   Order data: ${data.orderDataSize} bytes")
  catch case e: Exception => println(s"   Failed: ${e.getMessage}")

  println()
  println("Structured concurrency ensures:")
  println("- No orphaned threads")
  println("- Automatic cancellation propagation")
  println("- Clean resource management")
