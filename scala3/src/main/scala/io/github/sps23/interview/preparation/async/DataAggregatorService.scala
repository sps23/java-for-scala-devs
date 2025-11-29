package io.github.sps23.interview.preparation.async

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.util.{Failure, Success, Try}
import java.util.concurrent.{Executors, ScheduledExecutorService, TimeUnit, TimeoutException}

/** A service that demonstrates asynchronous data aggregation using Scala Futures.
  *
  * This class showcases key Scala Future concepts compared to Java's CompletableFuture:
  *
  *   - `Future { ... }` - Analogous to Java's `supplyAsync()`
  *   - `map` - Analogous to Java's `thenApply()`
  *   - `flatMap` - Analogous to Java's `thenCompose()`
  *   - `Future.sequence` - Analogous to Java's `allOf()`
  *   - `Future.firstCompletedOf` - Analogous to Java's `anyOf()`
  *   - `recover` - Analogous to Java's `exceptionally()`
  *   - `transform` - Analogous to Java's `handle()`
  *
  * Example usage:
  * {{{
  * implicit val ec: ExecutionContext = ExecutionContext.global
  * val aggregator = DataAggregatorService()
  * val result = aggregator.aggregateFromAllApis(5.seconds)
  * result.foreach(data => println(s"Got ${data.successCount} responses"))
  * }}}
  */
class DataAggregatorService(
    scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1),
    defaultTimeout: FiniteDuration      = Duration(10, TimeUnit.SECONDS)
)(using ec: ExecutionContext):

  /** Fetches data from a simulated weather API.
    *
    * Demonstrates basic Future creation, similar to Java's supplyAsync:
    * {{{
    * // Java equivalent
    * CompletableFuture.supplyAsync(() -> {
    *   // async computation
    * }, executor)
    * }}}
    *
    * @return
    *   Future containing the API response
    */
  def fetchWeatherData(): Future[ApiResponse] = Future:
    simulateApiCall("Weather API", 150)
    ApiResponse("weather", """{"temp": 22, "unit": "celsius"}""")

  /** Fetches data from a simulated traffic API.
    *
    * @return
    *   Future containing the API response
    */
  def fetchTrafficData(): Future[ApiResponse] = Future:
    simulateApiCall("Traffic API", 200)
    ApiResponse("traffic", """{"congestion": "moderate"}""")

  /** Fetches data from a simulated news API.
    *
    * @return
    *   Future containing the API response
    */
  def fetchNewsData(): Future[ApiResponse] = Future:
    simulateApiCall("News API", 100)
    ApiResponse("news", """{"headlines": ["Tech stocks rise"]}""")

  /** Fetches data from a slow API with timeout and fallback.
    *
    * Demonstrates timeout handling with fallback, similar to Java's completeOnTimeout:
    * {{{
    * // Java equivalent
    * future.completeOnTimeout(fallback, timeout, TimeUnit.MILLISECONDS)
    * }}}
    *
    * @param timeout
    *   maximum time to wait
    * @return
    *   Future with response or timeout fallback
    */
  def fetchSlowApiWithFallback(timeout: FiniteDuration): Future[ApiResponse] =
    val fallback = ApiResponse("slow-api", """{"status": "timeout_fallback"}""")
    val actualFuture = Future:
      simulateApiCall("Slow API", 5000) // Intentionally slow
      ApiResponse("slow-api", """{"data": "actual_response"}""")

    withTimeout(actualFuture, timeout, fallback)

  /** Fetches data from an unreliable API with exception handling.
    *
    * Demonstrates `recover` for handling failures, analogous to Java's exceptionally:
    * {{{
    * // Java equivalent
    * future.exceptionally(ex -> fallbackValue)
    * }}}
    *
    * @return
    *   Future with response or error fallback
    */
  def fetchUnreliableApiWithRecovery(): Future[ApiResponse] =
    Future:
      simulateApiCall("Unreliable API", 100)
      if Math.random() < 0.5 then throw RuntimeException("API temporarily unavailable")
      ApiResponse("unreliable", """{"status": "success"}""")
    .recover { case ex: Exception =>
      ApiResponse("unreliable", s"""{"status": "recovered", "error": "${ex.getMessage}"}""")
    }

  /** Transforms API response using map operation.
    *
    * Demonstrates `map` which is analogous to Java's thenApply:
    * {{{
    * // Java equivalent
    * future.thenApply(response -> response.data().toUpperCase())
    * }}}
    *
    * @param responseFuture
    *   the future to transform
    * @return
    *   Future with transformed data
    */
  def transformResponse(responseFuture: Future[ApiResponse]): Future[String] =
    responseFuture.map(response => response.data.toUpperCase)

  /** Chains API calls using flatMap operation.
    *
    * Demonstrates `flatMap` for sequential composition, analogous to Java's thenCompose:
    * {{{
    * // Java equivalent
    * fetchWeatherData()
    *   .thenCompose(weather -> enrichWithLocation(weather))
    * }}}
    *
    * For-comprehension makes this even more readable in Scala:
    * {{{
    * for {
    *   weather <- fetchWeatherData()
    *   enriched <- enrichWithLocation(weather)
    * } yield enriched
    * }}}
    *
    * @return
    *   Future with enriched response
    */
  def fetchAndEnrichWeather(): Future[ApiResponse] =
    for
      weather  <- fetchWeatherData()
      enriched <- enrichWithLocation(weather)
    yield enriched

  private def enrichWithLocation(weather: ApiResponse): Future[ApiResponse] = Future:
    simulateApiCall("Location API", 50)
    val enrichedData = weather.data.replace("}", """, "location": "NYC"}""")
    ApiResponse("weather-enriched", enrichedData)

  /** Aggregates data from all APIs concurrently using Future.sequence.
    *
    * Demonstrates `Future.sequence` which is analogous to Java's allOf:
    * {{{
    * // Java equivalent
    * CompletableFuture.allOf(future1, future2, future3)
    *   .thenApply(v -> List.of(future1.join(), future2.join(), future3.join()))
    * }}}
    *
    * @param timeout
    *   maximum time to wait for all responses
    * @return
    *   Future with aggregated data
    */
  def aggregateFromAllApis(timeout: FiniteDuration): Future[AggregatedData] =
    val weatherFuture = withTimeoutAndRecovery(
      fetchWeatherData(),
      timeout,
      "weather"
    )

    val trafficFuture = withTimeoutAndRecovery(
      fetchTrafficData(),
      timeout,
      "traffic"
    )

    val newsFuture = withTimeoutAndRecovery(
      fetchNewsData(),
      timeout,
      "news"
    )

    Future.sequence(List(weatherFuture, trafficFuture, newsFuture)).map { responses =>
      val errors = responses.filter(_.data.contains("error")).map(r => s"${r.source}: ${r.data}")
      if errors.isEmpty then AggregatedData.success(responses)
      else AggregatedData.partial(responses, errors)
    }

  private def withTimeoutAndRecovery(
      future: Future[ApiResponse],
      timeout: FiniteDuration,
      source: String
  ): Future[ApiResponse] =
    val timeoutFallback = ApiResponse(source, """{"error": "timeout"}""")
    withTimeout(future, timeout, timeoutFallback).recover { case ex: Exception =>
      ApiResponse(source, s"""{"error": "${ex.getMessage}"}""")
    }

  /** Gets the first available response using Future.firstCompletedOf.
    *
    * Demonstrates `Future.firstCompletedOf` which is analogous to Java's anyOf:
    * {{{
    * // Java equivalent
    * CompletableFuture.anyOf(future1, future2, future3)
    *   .thenApply(result -> (ApiResponse) result)
    * }}}
    *
    * @return
    *   Future with first available response
    */
  def getFirstAvailableResponse(): Future[ApiResponse] =
    Future.firstCompletedOf(
      List(
        fetchWeatherData(),
        fetchTrafficData(),
        fetchNewsData()
      )
    )

  /** Demonstrates transform for both success and failure handling.
    *
    * `transform` is analogous to Java's handle:
    * {{{
    * // Java equivalent
    * future.handle((response, ex) -> {
    *   if (ex != null) return "Error: " + ex.getMessage();
    *   return "Success: " + response.data();
    * })
    * }}}
    *
    * @return
    *   Future with handled result
    */
  def fetchWithFullErrorHandling(): Future[String] =
    Future:
      simulateApiCall("Handled API", 100)
      if Math.random() < 0.3 then throw RuntimeException("Random failure")
      ApiResponse("handled", """{"status": "ok"}""")
    .transform:
      case Success(response) => Success(s"Success: ${response.data}")
      case Failure(ex)       => Success(s"Error occurred: ${ex.getMessage}")

  /** Demonstrates strict timeout that fails with TimeoutException.
    *
    * Unlike withTimeout with fallback, this method fails the future if timeout occurs. Similar to
    * Java's orTimeout:
    * {{{
    * // Java equivalent
    * future.orTimeout(timeout, TimeUnit.MILLISECONDS)
    * }}}
    *
    * @param timeout
    *   maximum time to wait
    * @return
    *   Future that may fail with TimeoutException
    */
  def fetchWithStrictTimeout(timeout: FiniteDuration): Future[ApiResponse] =
    val actualFuture = Future:
      simulateApiCall("Strict Timeout API", 5000)
      ApiResponse("strict", """{"data": "response"}""")

    withStrictTimeout(actualFuture, timeout)

  /** Helper method to add timeout with fallback to a Future.
    *
    * This pattern is commonly used in Scala to achieve similar functionality to Java 9+'s
    * completeOnTimeout.
    */
  private def withTimeout[T](future: Future[T], timeout: FiniteDuration, fallback: T): Future[T] =
    val promise = Promise[T]()

    // Schedule timeout fallback
    scheduler.schedule(
      new Runnable:
        def run(): Unit = promise.trySuccess(fallback)
      ,
      timeout.toMillis,
      TimeUnit.MILLISECONDS
    )

    // Complete with actual result if it arrives first
    future.onComplete(result => promise.tryComplete(result))

    promise.future

  /** Helper method to add strict timeout that fails with TimeoutException. */
  private def withStrictTimeout[T](future: Future[T], timeout: FiniteDuration): Future[T] =
    val promise = Promise[T]()

    // Schedule timeout failure
    scheduler.schedule(
      new Runnable:
        def run(): Unit = promise.tryFailure(new TimeoutException(s"Timeout after $timeout"))
      ,
      timeout.toMillis,
      TimeUnit.MILLISECONDS
    )

    // Complete with actual result if it arrives first
    future.onComplete(result => promise.tryComplete(result))

    promise.future

  private def simulateApiCall(apiName: String, delayMs: Long): Unit =
    try Thread.sleep(delayMs)
    catch
      case e: InterruptedException =>
        Thread.currentThread().interrupt()
        throw RuntimeException(s"API call interrupted: $apiName", e)

  /** Shuts down the scheduler. */
  def shutdown(): Unit = scheduler.shutdown()

/** Companion object with example usage. */
object DataAggregatorService:
  @main def runAsyncExample(): Unit =
    given ExecutionContext = ExecutionContext.global
    val aggregator         = DataAggregatorService()

    println("Starting async data aggregation...")

    import scala.concurrent.duration.*

    // Example: Aggregate from all APIs
    val result = aggregator.aggregateFromAllApis(5.seconds)

    result.foreach { data =>
      println(s"Aggregation complete:")
      println(s"  Success count: ${data.successCount}")
      println(s"  Fully successful: ${data.isFullySuccessful}")
      data.responses.foreach(r => println(s"  - ${r.source}: ${r.data}"))
    }

    // Wait for completion
    Thread.sleep(3000)
    aggregator.shutdown()
