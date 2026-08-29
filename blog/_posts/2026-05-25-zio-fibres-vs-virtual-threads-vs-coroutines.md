---
layout: post
title: "ZIO Fibres vs Java Virtual Threads vs Kotlin Coroutines"
description: "A practical comparison of ZIO fibres in Scala 3, Java 21 virtual threads, and Kotlin coroutines — covering fork/join, typed error handling, concurrent composition, and when to use each approach."
date: 2026-05-25 13:00:00 +0000
updated: 2026-08-29 14:00:00 +0000
categories: [concurrency]
tags: [scala, scala3, zio, fibres, java, java21, kotlin, coroutines, virtual-threads, concurrency]
---

Imagine you need to fetch a user's profile and their order history at the same time. You could do them one after the other like a very patient accountant, or you could do them *at the same time* like a competent developer.

This post is about doing them at the same time — and comparing how ZIO fibres, Java virtual threads, and Kotlin coroutines each go about it.

Spoiler: they all get there. The differences are in typing, safety, and how much ceremony the runtime demands of you.

## What Is a Fibre (and Why Should I Care)?

A **fibre** is a lightweight, user-space unit of concurrency managed by a runtime — not by the operating system. The key insight is that *most concurrent tasks spend most of their time waiting* (for a network response, a database query, a disk read). Traditional platform threads waste a full ~1 MB stack while they wait. Fibres don't.

<div class="table-wrapper" markdown="1">

| Property | Platform Thread | Java Virtual Thread | ZIO Fibre | Kotlin Coroutine |
|---|---|---|---|---|
| Managed by | OS | JVM (Project Loom) | ZIO runtime | Kotlin/coroutines dispatcher |
| Stack size | ~1 MB | ~few KB (grows) | ~few hundred bytes | ~few hundred bytes |
| Max concurrent | ~thousands | ~millions | ~millions | ~millions |
| Blocking style | Blocking | Blocking (transparently) | Semantic blocking | Suspending |
| Error model | Exception | Exception | Typed `Cause[E]` | Exception |
| Interruption | `Thread.interrupt()` | `Thread.interrupt()` | `Fibre#interrupt` (typed, safe) | `Job.cancel()` |

</div>

All three lightweight mechanisms let you run millions of concurrent tasks cheaply. The interesting differences are in error handling and resource safety.

## Fetch a User and Their Orders — At the Same Time

Here's the task: fetch a user profile and their order history concurrently, then combine the results. This is the "hello world" of concurrent programming.

### Scala 3 (ZIO)

```scala
val program: UIO[String] =
  for
    userFibre  <- fetchUser.fork    // spawn fibre, returns immediately
    orderFibre <- fetchOrders.fork  // spawn another, also immediate
    user       <- userFibre.join    // wait for user result
    orders     <- orderFibre.join   // wait for orders result
  yield s"$user has ${orders.length} orders"
```

### Java 21

```java
public static String fetchUserAndOrders() throws Exception {
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
        Future<String> userFuture   = executor.submit(VirtualThreadBasics::fetchUser);
        Future<List<String>> orderFuture = executor.submit(VirtualThreadBasics::fetchOrders);
        String user      = userFuture.get();
        List<String> orders = orderFuture.get();
        return user + " has " + orders.size() + " orders";
    }
}
```

### Kotlin

```kotlin
suspend fun fetchUserAndOrders(): String = coroutineScope {
    val userDeferred   = async { fetchUser() }    // launch coroutine
    val ordersDeferred = async { fetchOrders() }  // launch another
    val user   = userDeferred.await()
    val orders = ordersDeferred.await()
    "$user has ${orders.size} orders"
}
```

All three versions are parallel and finish in `max(user_latency, order_latency)` instead of `user_latency + order_latency`.

The Scala version reads like sequential code but runs concurrently — which is what effect systems are for. The Java version is explicit about the executor. Kotlin's `coroutineScope` gives structured concurrency for free: if either `async` block throws, the scope cancels the other and propagates the error.

## When Things Go Wrong: Typed Errors vs. Exceptions

This is where ZIO diverges most sharply from Java and Kotlin.

In ZIO, the error type is part of the method signature: `IO[AppError, String]` tells you *at compile time* that this effect can fail with an `AppError`. Forget to handle it? It won't compile.

### Scala 3 (ZIO)

```scala
// The error type is in the signature — compiler enforces handling
val riskyFetch: IO[AppError, String] =
  ZIO.fail(NetworkError("timeout"))

// catchAll must cover all AppError variants or it won't compile
val withFallback: UIO[String] =
  riskyFetch.catchAll:
    case NetworkError(msg) => ZIO.succeed(s"fallback (network: $msg)")
    case ParseError(msg)   => ZIO.succeed(s"fallback (parse: $msg)")
```

### Java 21

```java
// Java: errors are untyped Throwable — no compile-time guarantee
CompletableFuture<String> withFallback = CompletableFuture
    .supplyAsync(() -> {
        throw new NetworkException("timeout");
    })
    .exceptionally(ex -> "fallback (" + ex.getMessage() + ")");
```

### Kotlin

```kotlin
// Kotlin: also exception-based, but with runCatching for safety
suspend fun withFallback(): String =
    runCatching { riskyFetch() }
        .getOrElse { ex -> "fallback (${ex.message})" }
```

**ZIO's advantage here is real:** you cannot accidentally forget to handle a `NetworkError`. The compiler will catch the omission. Java and Kotlin both rely on discipline (and tests) to ensure all error cases are covered.

The trade-off is verbosity: `IO[AppError, String]` is noisier than just `String`. Whether that trade-off is worth it depends heavily on your domain — in financial or healthcare applications, typed errors pay for themselves quickly.

## Racing and Parallel Collections

ZIO also ships high-level concurrent combinators that make common patterns concise:

### Scala 3 (ZIO)

```scala
// Race: first to finish wins, loser is automatically interrupted
val fastest: UIO[String] = fromCache race fromDb

// Parallel collection: run all, gather all results
val allResults: UIO[List[String]] =
  ZIO.collectAllPar(urls.map(url => ZIO.succeed(s"content of $url")))
```

### Java 21

```java
// Race: ShutdownOnSuccess cancels remaining tasks when one succeeds
try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
    scope.fork(() -> { Thread.sleep(10);  return "cached"; });
    scope.fork(() -> { Thread.sleep(200); return "db"; });
    scope.join();
    return scope.result(); // "cached"
}

// Parallel collection: ShutdownOnFailure gathers all results
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var subtasks = urls.stream()
            .map(url -> scope.fork(() -> "content of " + url))
            .toList();
    scope.join().throwIfFailed();
    return subtasks.stream().map(Subtask::get).toList();
}
```

### Kotlin

```kotlin
// Parallel collection: map to async, then awaitAll
val allResults: List<String> = coroutineScope {
    urls.map { url -> async { "content of $url" } }.awaitAll()
}
```

The patterns are equivalent, just with different APIs. ZIO's `race` is a one-liner. Java's `ShutdownOnSuccess` is wordier but makes the cancellation policy explicit. Kotlin's `awaitAll()` is the cleanest for the "run all" case.

## Performance: The Numbers

All three mechanisms let you run millions of concurrent tasks on modest hardware. The differences are at the margins:

<div class="table-wrapper" markdown="1">

| Workload | Platform Threads | Virtual Threads | ZIO Fibres | Kotlin Coroutines |
|---|---|---|---|---|
| 10 000 concurrent sleep(1s) tasks | ~10 s + OOM risk | ~1 s, ~50 MB heap | ~1 s, ~30 MB heap | ~1 s, ~40 MB heap |
| 100 000 concurrent tasks | thread pool queuing | ~1–2 s | ~1–2 s | ~1–2 s |
| Memory per task | ~1 MB stack | ~few KB | ~few hundred bytes | ~few hundred bytes |

</div>

For I/O-bound workloads (most web services), all three lightweight options win decisively over platform threads. ZIO fibres have the smallest footprint; virtual threads are the simplest migration for existing Java code.

## When to Use What

<div class="table-wrapper" markdown="1">

| Situation | Recommendation |
|---|---|
| Pure Scala / ZIO codebase | ZIO fibres — natural fit, typed errors, composable |
| Scala + Java library interop | ZIO fibres wrapping blocking calls with `ZIO.attemptBlockingIO` |
| Java 21 greenfield service | Virtual threads + `StructuredTaskScope` |
| Migrating Java thread-pool code | Virtual threads (often a one-line change) |
| Kotlin-first service | Coroutines with structured concurrency |
| Mixed JVM polyglot | Virtual threads (common JVM primitive, no extra runtime) |

</div>

## Code Samples

All examples in this post are runnable. You can find them in the repository:

- [Scala 3 ZIO fibres](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/fibres/) — `BasicFibres.scala`, `FibreSupervision.scala`, `FibreConcurrency.scala`
- [Scala 3 tests](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/test/scala/io/github/sps23/fibres/) — ScalaTest + ZIO runtime
- [Java 21 virtual threads](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/fibres/) — `VirtualThreadBasics.java`, `StructuredTaskScopeDemo.java`
- [Kotlin coroutines](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/fibres/) — `CoroutineBasics.kt`

## Conclusion

ZIO fibres, Java virtual threads, and Kotlin coroutines all solve the same problem: running lots of concurrent I/O-bound tasks without burning a gigabyte of thread stacks.

The philosophical difference is in error handling. ZIO asks the compiler to track what can go wrong; Java and Kotlin trust the developer to handle exceptions. Neither approach is wrong — they reflect different trade-offs between explicitness and brevity.

If you're a Scala developer moving to Java: virtual threads will feel familiar (lightweight, blocking-style), but you'll miss typed errors. If you're a Java developer curious about Scala: ZIO fibres will feel alien at first, then unnervingly safe.

---

*This post is part of the [Java 21 Interview Preparation Guide - Your Roadmap to Success]({{ site.baseurl }}{% link _posts/2025-11-25-java21-interview-preparation-plan.md %}). Nearby related posts from the same track: [Virtual Threads and Structured Concurrency]({{ site.baseurl }}{% link _posts/2025-11-29-virtual-threads-and-structured-concurrency.md %}) and [CompletableFuture and Asynchronous Programming]({{ site.baseurl }}{% link _posts/2025-11-29-completablefuture-and-asynchronous-programming.md %}).*
