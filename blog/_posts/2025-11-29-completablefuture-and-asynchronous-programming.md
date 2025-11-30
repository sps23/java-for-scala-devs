---
layout: post
title: "CompletableFuture and Asynchronous Programming"
description: "Build concurrent applications with Java CompletableFuture - learn async composition, timeouts, and error handling with comparisons to Scala Futures and Kotlin Coroutines."
date: 2025-11-29 10:00:00 +0000
categories: concurrency
tags: java scala kotlin async futures completablefuture coroutines
---

Asynchronous programming is essential for building responsive, high-performance applications. This post explores how Java, Scala, and Kotlin handle async operations, with a focus on aggregating data from multiple APIs concurrently.

## The Problem

Imagine building a service that aggregates data from multiple external APIs:
- Weather API
- Traffic API
- News API

We need to:
1. Call all APIs concurrently (not sequentially)
2. Handle timeouts gracefully
3. Provide fallback values when APIs fail
4. Combine results into an aggregated response

## Java: CompletableFuture

Java's `CompletableFuture` (introduced in Java 8, enhanced in Java 9+) provides a rich API for async programming:

### Basic Async Operation

```java
// Create an async task - similar to Scala's Future { ... }
CompletableFuture<ApiResponse> fetchWeatherData() {
    return CompletableFuture.supplyAsync(() -> {
        // Simulate API call
        Thread.sleep(150);
        return ApiResponse.of("weather", "{\"temp\": 22}");
    }, executor);
}
```

### Transformation with thenApply (like Scala's map)

```java
// Transform the result
CompletableFuture<String> transformResponse(CompletableFuture<ApiResponse> future) {
    return future.thenApply(response -> response.data().toUpperCase());
}
```

### Chaining with thenCompose (like Scala's flatMap)

```java
// Chain dependent async operations
CompletableFuture<ApiResponse> fetchAndEnrichWeather() {
    return fetchWeatherData()
        .thenCompose(weather -> enrichWithLocation(weather));
}
```

### Combining Futures with allOf

```java
// Wait for all futures to complete
CompletableFuture<AggregatedData> aggregateFromAllApis(Duration timeout) {
    var weather = fetchWeatherData()
        .completeOnTimeout(fallback, timeout.toMillis(), TimeUnit.MILLISECONDS);
    var traffic = fetchTrafficData()
        .completeOnTimeout(fallback, timeout.toMillis(), TimeUnit.MILLISECONDS);
    var news = fetchNewsData()
        .completeOnTimeout(fallback, timeout.toMillis(), TimeUnit.MILLISECONDS);

    return CompletableFuture.allOf(weather, traffic, news)
        .thenApply(v -> AggregatedData.success(List.of(
            weather.join(),
            traffic.join(),
            news.join()
        )));
}
```

### Race Conditions with anyOf

```java
// Get the first result
CompletableFuture<ApiResponse> getFirstAvailableResponse() {
    return CompletableFuture.anyOf(
        fetchWeatherData(),
        fetchTrafficData(),
        fetchNewsData()
    ).thenApply(result -> (ApiResponse) result);
}
```

### Timeout Handling (Java 9+)

```java
// With fallback value (completeOnTimeout)
future.completeOnTimeout(fallback, 5, TimeUnit.SECONDS);

// With exception (orTimeout)
future.orTimeout(5, TimeUnit.SECONDS);
```

### Error Handling

```java
// Recovery (like Scala's recover)
future.exceptionally(ex -> ApiResponse.of("fallback", "{}"));

// Full handling (like Scala's transform)
future.handle((response, ex) -> {
    if (ex != null) return "Error: " + ex.getMessage();
    return "Success: " + response.data();
});
```

## Scala: Futures

Scala's `Future` has been a core part of async programming in Scala for years. The syntax is often more concise:

### Basic Async Operation

```scala
def fetchWeatherData(): Future[ApiResponse] = Future {
  simulateApiCall("Weather API", 150)
  ApiResponse("weather", """{"temp": 22}""")
}
```

### Transformation with map

```scala
def transformResponse(future: Future[ApiResponse]): Future[String] =
  future.map(response => response.data.toUpperCase)
```

### Chaining with flatMap and for-comprehensions

```scala
// Using flatMap
def fetchAndEnrichWeather(): Future[ApiResponse] =
  fetchWeatherData().flatMap(weather => enrichWithLocation(weather))

// More elegantly with for-comprehension
def fetchAndEnrichWeather(): Future[ApiResponse] =
  for
    weather <- fetchWeatherData()
    enriched <- enrichWithLocation(weather)
  yield enriched
```

### Combining Futures with sequence

```scala
def aggregateFromAllApis(timeout: FiniteDuration): Future[AggregatedData] =
  val futures = List(
    withTimeout(fetchWeatherData(), timeout, fallback),
    withTimeout(fetchTrafficData(), timeout, fallback),
    withTimeout(fetchNewsData(), timeout, fallback)
  )
  
  Future.sequence(futures).map(responses => 
    AggregatedData.success(responses)
  )
```

### Racing Futures with firstCompletedOf

```scala
def getFirstAvailableResponse(): Future[ApiResponse] =
  Future.firstCompletedOf(List(
    fetchWeatherData(),
    fetchTrafficData(),
    fetchNewsData()
  ))
```

### Error Handling

```scala
// Recovery
future.recover { case ex: Exception =>
  ApiResponse("fallback", "{}")
}

// Full transformation
future.transform {
  case Success(response) => Success(s"Success: ${response.data}")
  case Failure(ex) => Success(s"Error: ${ex.getMessage}")
}
```

### Timeout Handling (requires helper)

Scala's Future doesn't have built-in timeout. Here's a pattern using `Promise`:

```scala
def withTimeout[T](future: Future[T], timeout: FiniteDuration, fallback: T): Future[T] =
  val promise = Promise[T]()
  
  scheduler.schedule(
    () => promise.trySuccess(fallback),
    timeout.toMillis,
    TimeUnit.MILLISECONDS
  )
  
  future.onComplete(result => promise.tryComplete(result))
  promise.future
```

## Kotlin: Coroutines

Kotlin takes a different approach with coroutines, providing structured concurrency that looks like synchronous code:

### Basic Async Operation

```kotlin
fun CoroutineScope.fetchWeatherDataAsync(): Deferred<ApiResponse> =
    async(Dispatchers.IO) {
        simulateApiCall("Weather API", 150)
        ApiResponse.of("weather", """{"temp": 22}""")
    }
```

### Sequential Operations (Natural Syntax!)

```kotlin
// This looks synchronous but is actually async!
suspend fun fetchAndEnrichWeather(): ApiResponse {
    val weather = fetchWeatherDataAsync().await()
    return enrichWithLocation(weather)
}
```

### Concurrent Operations with awaitAll

```kotlin
suspend fun aggregateFromAllApis(timeout: Duration): AggregatedData =
    coroutineScope {
        val weather = async { fetchWithTimeout("weather", timeout) }
        val traffic = async { fetchWithTimeout("traffic", timeout) }
        val news = async { fetchWithTimeout("news", timeout) }
        
        val responses = listOf(weather, traffic, news).awaitAll()
        AggregatedData.success(responses)
    }
```

### Racing with select

```kotlin
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
```

### Built-in Timeout Handling

```kotlin
// With fallback (returns null on timeout)
val result = withTimeoutOrNull(5.seconds) {
    fetchSlowApi()
} ?: fallbackValue

// With exception
val result = withTimeout(5.seconds) {
    fetchSlowApi()
}
```

### Error Handling

```kotlin
// Using runCatching (Kotlin's Result type)
suspend fun fetchWithHandling(): String {
    val result = runCatching {
        fetchApiData()
    }
    
    return result.fold(
        onSuccess = { "Success: ${it.data}" },
        onFailure = { "Error: ${it.message}" }
    )
}

// Simple try/catch (works naturally with suspend functions)
suspend fun fetchWithRecovery(): ApiResponse =
    try {
        fetchUnreliableApi()
    } catch (ex: Exception) {
        ApiResponse.of("fallback", "{}")
    }
```

## Comparison Table

| Feature | Java CompletableFuture | Scala Future | Kotlin Coroutines |
|---------|----------------------|--------------|-------------------|
| Create async | `supplyAsync()` | `Future { }` | `async { }` |
| Transform | `thenApply()` | `map` | `await()` + transform |
| Chain | `thenCompose()` | `flatMap` / for-comp | Sequential `await()` |
| Combine all | `allOf()` | `sequence` | `awaitAll()` |
| First completed | `anyOf()` | `firstCompletedOf` | `select { }` |
| Timeout fallback | `completeOnTimeout()` | Helper needed | `withTimeoutOrNull()` |
| Timeout exception | `orTimeout()` | Helper needed | `withTimeout()` |
| Recover | `exceptionally()` | `recover` | try/catch |
| Transform both | `handle()` | `transform` | `runCatching().fold()` |

## Key Insights for Scala Developers

1. **Java's API is verbose but complete**: CompletableFuture has everything you need, including built-in timeout methods (Java 9+).

2. **Kotlin coroutines feel natural**: The suspend function model makes async code look synchronous, which can be easier to read and maintain.

3. **Scala's for-comprehensions are elegant**: Nothing beats the readability of Scala's for-comprehension syntax for chaining futures.

4. **Error handling differs**: 
   - Java uses `exceptionally()` and `handle()`
   - Scala uses `recover` and `transform`
   - Kotlin uses standard try/catch with suspend functions

5. **Timeout handling**: Java 9+ has built-in timeout methods, while Scala requires custom helpers. Kotlin has excellent built-in support.

## Full Working Examples

Check out the complete implementation in our repository:
- [Java CompletableFuture](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/interview/preparation/async)
- [Scala 3 Future](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/interview/preparation/async)
- [Kotlin Coroutines](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/async)

## Conclusion

All three languages provide powerful async primitives. For Scala developers moving to Java:

- Think of `supplyAsync()` as `Future { }`
- Think of `thenApply()` as `map`
- Think of `thenCompose()` as `flatMap`
- Think of `allOf()` as `Future.sequence`
- Think of `exceptionally()` as `recover`

The patterns are similar, just with different syntax. The key is understanding the mental model: futures represent values that will be available in the future, and all three languages let you compose them elegantly.

Happy async coding! 🚀
