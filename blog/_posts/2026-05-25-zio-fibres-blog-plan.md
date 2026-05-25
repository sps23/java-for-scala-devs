---
layout: post
title: "Plan: ZIO Fibres vs Java Virtual Threads vs Kotlin Coroutines"
description: "A structured plan for the upcoming blog post comparing ZIO fibres in Scala 3 with Java 21 virtual threads and Kotlin coroutines - covering concepts, code examples, tests, and performance analysis."
date: 2026-05-25 12:00:00 +0000
categories: [concurrency]
tags: [scala, scala3, zio, fibres, java, java21, kotlin, coroutines, virtual-threads, concurrency, plan]
---

This post lays out the full plan for an upcoming deep-dive blog post comparing ZIO fibres in Scala 3 with the corresponding concurrency abstractions in Java 21 (virtual threads and `StructuredTaskScope`) and Kotlin (coroutines). Each section below describes what the finished post will cover, the code examples and tests that will be written, and the key points the reader should take away.

## Why ZIO Fibres?

ZIO fibres are the foundational concurrency primitive of the [ZIO](https://zio.dev) effect system, widely used in production Scala services. For a Scala developer, understanding fibres is essential for writing concurrent code that is:

- **safe** – errors and interruptions are tracked in the type system
- **composable** – concurrent tasks are values you can pass around and combine
- **resource-safe** – fibres respect structured lifetimes so resources are always released

For readers coming from Java or Kotlin, fibres are interesting because they solve the same problems as virtual threads and coroutines, but with a different philosophy: everything is a typed effect.

---

## Planned Post Structure

### Section 1 – What Is a Fibre?

**Goal:** Give readers a mental model before they see any ZIO-specific API.

**Content:**
- One-paragraph analogy: a fibre is like a very lightweight thread managed entirely by the ZIO runtime, not the operating system. It occupies a few hundred bytes rather than the ~1 MB a platform thread stack needs.
- Diagram (text-based table) comparing platform threads, virtual threads, and fibres.

<div class="table-wrapper" markdown="1">

| Property | Platform Thread | Java Virtual Thread | ZIO Fibre |
|---|---|---|---|
| Managed by | OS | JVM (Project Loom) | ZIO runtime |
| Stack size | ~1 MB | ~few KB (grows) | ~few hundred bytes |
| Max concurrent | ~thousands | ~millions | ~millions |
| Blocking style | Blocking | Blocking (transparently) | Semantic blocking (never pins carrier) |
| Error model | Exception | Exception | Typed `Cause[E]` |
| Interruption | `Thread.interrupt()` | `Thread.interrupt()` | `Fibre#interrupt` (typed, safe) |
| Structured lifetime | Manual | `StructuredTaskScope` | Built-in via `ZIO.scoped` |

</div>

**Key takeaway:** All three mechanisms let you run millions of concurrent tasks cheaply. The difference is in how errors, interruption, and resource lifetimes are handled.

---

### Section 2 – Setting Up the Examples

**Goal:** Show readers exactly what to add to their build before any code.

**Planned build file change (`scala3/build.gradle`):**

```groovy
dependencies {
    // existing dependencies …
    implementation 'dev.zio:zio_3:2.1.9'
    implementation 'dev.zio:zio-streams_3:2.1.9'
    testImplementation 'dev.zio:zio-test_3:2.1.9'
    testImplementation 'dev.zio:zio-test-sbt_3:2.1.9'
}
```

**Package layout that will be created:**

```
scala3/src/
  main/scala/io/github/sps23/fibres/
    BasicFibres.scala          # fork / join / await
    FibreSupervision.scala     # error handling, Cause
    FibreConcurrency.scala     # race, zipPar, collectAllPar
    ResourceSafety.scala       # ZIO.scoped, acquireRelease
  test/scala/io/github/sps23/fibres/
    BasicFibresSpec.scala
    FibreSupervisionSpec.scala
    FibreConcurrencySpec.scala
    ResourceSafetySpec.scala
java21/src/
  main/java/io/github/sps23/fibres/
    VirtualThreadBasics.java   # Thread.ofVirtual, newVirtualThreadPerTaskExecutor
    StructuredTaskScopeDemo.java
  test/java/io/github/sps23/fibres/
    VirtualThreadBasicsTest.java
kotlin/src/
  main/kotlin/io/github/sps23/fibres/
    CoroutineBasics.kt         # launch, async, await, structured concurrency
  test/kotlin/io/github/sps23/fibres/
    CoroutineBasicsTest.kt
```

---

### Section 3 – Creating and Joining Fibres

**Goal:** Show the absolute basics – spawn work, wait for the result.

**What each example will do:** Fetch a "user profile" and an "order history" concurrently, then combine the results.

**Planned Scala (ZIO) example:**

```scala
import zio.*

object BasicFibres extends ZIOAppDefault:

  val fetchUser: UIO[String] =
    ZIO.sleep(100.millis) *> ZIO.succeed("Alice")

  val fetchOrders: UIO[List[String]] =
    ZIO.sleep(80.millis) *> ZIO.succeed(List("order-1", "order-2"))

  val program: UIO[String] =
    for
      userFibre   <- fetchUser.fork
      orderFibre  <- fetchOrders.fork
      user        <- userFibre.join
      orders      <- orderFibre.join
    yield s"$user has ${orders.length} orders"

  def run = program.flatMap(result => Console.printLine(result))
```

**Planned Java 21 (virtual threads) equivalent:**

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    Future<String> userFuture   = executor.submit(() -> fetchUser());
    Future<List<String>> orderFuture = executor.submit(() -> fetchOrders());
    String user   = userFuture.get();
    List<String> orders = orderFuture.get();
    return user + " has " + orders.size() + " orders";
}
```

**Planned Kotlin (coroutines) equivalent:**

```kotlin
coroutineScope {
    val userDeferred   = async { fetchUser() }
    val ordersDeferred = async { fetchOrders() }
    val user   = userDeferred.await()
    val orders = ordersDeferred.await()
    "$user has ${orders.size} orders"
}
```

**Planned unit test (ZIO Test):**

```scala
import zio.test.*

object BasicFibresSpec extends ZIOSpecDefault:
  def spec = suite("BasicFibres")(
    test("fork and join runs tasks concurrently") {
      for
        start  <- Clock.currentTime(TimeUnit.MILLISECONDS)
        result <- BasicFibres.program
        end    <- Clock.currentTime(TimeUnit.MILLISECONDS)
      yield assertTrue(result == "Alice has 2 orders") &&
            assertTrue(end - start < 200L) // both tasks in parallel, not 180ms sequential
    }
  )
```

---

### Section 4 – Error Handling and Typed Failures

**Goal:** Illustrate the biggest conceptual difference – ZIO fibres track errors in the type signature, while virtual threads and coroutines rely on exceptions.

**Content:**
- `ZIO[R, E, A]` type – the `E` channel means the compiler tells you what can go wrong.
- `Cause[E]` – ZIO distinguishes between expected failures (`Fail`), defects/unexpected exceptions (`Die`), and interruptions.
- `Fibre#interrupt` vs `Thread.interrupt()` vs coroutine cancellation.

**Planned Scala example (`FibreSupervision.scala`):**

```scala
sealed trait AppError
case class NetworkError(msg: String) extends AppError
case class ParseError(msg: String)   extends AppError

val riskyFetch: IO[AppError, String] =
  ZIO.fail(NetworkError("timeout")).delay(50.millis)

val withFallback: UIO[String] =
  riskyFetch
    .catchAll {
      case NetworkError(msg) => ZIO.succeed(s"fallback (network: $msg)")
      case ParseError(msg)   => ZIO.succeed(s"fallback (parse: $msg)")
    }
```

**Planned Java equivalent** (checked + unchecked exception model for comparison):

```java
CompletableFuture<String> riskyFetch = CompletableFuture
    .supplyAsync(() -> { throw new NetworkException("timeout"); })
    .exceptionally(ex -> "fallback (" + ex.getMessage() + ")");
```

**Planned test:**

```scala
test("catchAll handles typed errors without crashing the fibre") {
  for result <- withFallback
  yield assertTrue(result.startsWith("fallback"))
}
```

---

### Section 5 – Concurrent Composition Patterns

**Goal:** Show the high-level combinators that make ZIO concurrency expressive.

**Patterns to cover:**

| Pattern | ZIO | Java 21 | Kotlin |
|---|---|---|---|
| Run two tasks, take both results | `zipPar` | `invokeAll` / `Future.allOf` | `awaitAll` |
| Run N tasks, take first success | `ZIO.raceAll` | `ShutdownOnSuccess` scope | `select` |
| Run N tasks in parallel, collect all | `ZIO.collectAllPar` | `StructuredTaskScope` | `awaitAll` |
| Timeout a fibre | `.timeout(dur)` | `Future.get(timeout)` | `withTimeout` |

**Planned Scala (`FibreConcurrency.scala`):**

```scala
// Race two data sources – first one to respond wins
val fromCache: IO[Nothing, String] = ZIO.sleep(10.millis)  *> ZIO.succeed("cached")
val fromDb:    IO[Nothing, String] = ZIO.sleep(200.millis) *> ZIO.succeed("db")

val fastest: UIO[String] = fromCache raceFirst fromDb
// result is always "cached" because it resolves first

// Parallel collection – like Future.sequence but concurrent
val urls = List("url1", "url2", "url3")
val allResults: UIO[List[String]] =
  ZIO.collectAllPar(urls.map(url => ZIO.succeed(s"content of $url")))
```

**Planned Java (`StructuredTaskScopeDemo.java`):**

```java
// Race – first success wins
try (var scope = new StructuredTaskScope.ShutdownOnSuccess<String>()) {
    scope.fork(() -> fetchFromCache());
    scope.fork(() -> fetchFromDb());
    scope.join();
    return scope.result();
}

// All – wait for every task
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    var subtasks = urls.stream().map(url -> scope.fork(() -> fetch(url))).toList();
    scope.join().throwIfFailed();
    return subtasks.stream().map(Subtask::get).toList();
}
```

**Planned Kotlin:**

```kotlin
// Race via select{}
val result = select<String> {
    async { fetchFromCache() }.onAwait { it }
    async { fetchFromDb()    }.onAwait { it }
}

// All parallel
val results = urls.map { url -> async { fetch(url) } }.awaitAll()
```

**Planned tests:**

```scala
test("raceFirst returns the fastest result") {
  for result <- fastest
  yield assertTrue(result == "cached")
},
test("collectAllPar fetches all URLs in parallel") {
  for results <- allResults
  yield assertTrue(results.length == 3)
}
```

---

### Section 6 – Resource Safety and Structured Lifetimes

**Goal:** Show why fibres are a better fit for resource management than raw threads.

**Content:**
- `ZIO.acquireRelease` guarantees cleanup even if a fibre is interrupted.
- Compare with Java `try-with-resources` inside a virtual thread.
- Kotlin `use {}` with coroutine scope.

**Planned Scala example (`ResourceSafety.scala`):**

```scala
val managedConnection: ZIO[Scope, Nothing, DbConnection] =
  ZIO.acquireRelease(
    acquire = ZIO.succeed(DbConnection.open()) <* Console.printLine("opened")
  )(
    release = conn => ZIO.succeed(conn.close()) *> Console.printLine("closed").orDie
  )

val program: Task[Int] =
  ZIO.scoped {
    for
      conn  <- managedConnection
      count <- conn.query("SELECT COUNT(*) FROM users")
    yield count
  }
// Connection is always closed, even if the query fibre is interrupted
```

**Planned Java equivalent:**

```java
try (var conn = DbConnection.open()) {          // AutoCloseable
    return conn.query("SELECT COUNT(*) FROM users");
}   // always closed, but only within the virtual thread's lifetime
```

**Planned test:**

```scala
test("connection is closed even when fibre is interrupted") {
  var closed = false
  val managed = ZIO.acquireRelease(ZIO.succeed("conn"))(
    _ => ZIO.succeed { closed = true }
  )
  for
    fibre  <- ZIO.scoped(managed *> ZIO.never).fork
    _      <- fibre.interrupt
    _      <- ZIO.sleep(50.millis)   // let release run
  yield assertTrue(closed)
}
```

---

### Section 7 – Performance, Memory, and Resource Usage

**Goal:** Give concrete numbers so readers can make informed choices.

**What will be benchmarked (planned JMH / hand-rolled benchmark):**

<div class="table-wrapper" markdown="1">

| Workload | Java Platform Threads | Java Virtual Threads | ZIO Fibres | Kotlin Coroutines |
|---|---|---|---|---|
| 10 000 concurrent sleep(1s) tasks | ~10 s + OOM risk | ~1 s, ~50 MB heap | ~1 s, ~30 MB heap | ~1 s, ~40 MB heap |
| 100 000 concurrent tasks | thread pool queuing | ~1–2 s | ~1–2 s | ~1–2 s |
| Startup (first fibre / thread) | ~1 ms | ~0.3 ms | ~0.5 ms (ZIO runtime init) | ~0.5 ms |
| Memory per task | ~1 MB stack | ~few KB | ~few hundred bytes | ~few hundred bytes |

</div>

**Key observations the post will make:**
1. **Heap vs stack**: ZIO fibres and Kotlin coroutines live on the heap, growing only as needed. Virtual threads use small but growable stacks. Platform threads allocate ~1 MB stack upfront.
2. **Scheduler overhead**: ZIO has its own M:N scheduler (fibres on carrier threads). Virtual threads use ForkJoinPool as the carrier. Kotlin coroutines depend on the dispatcher.
3. **CPU-bound work**: All three mechanisms ultimately run on platform threads; for pure CPU work the scheduler overhead is similar. Virtual threads have a slight edge because Loom is integrated directly into the JVM.
4. **I/O-bound work**: All three shine here. ZIO wins slightly on memory footprint; virtual threads win on simplicity for existing Java code.

---

### Section 8 – When to Use What

**Goal:** Leave the reader with clear guidance.

<div class="table-wrapper" markdown="1">

| Situation | Recommendation |
|---|---|
| Pure Scala / ZIO codebase | ZIO fibres (natural fit, typed errors, composable) |
| Scala + Java library interop | ZIO fibres wrapping blocking calls with `ZIO.attemptBlockingIO` |
| Java 21 greenfield service | Virtual threads + `StructuredTaskScope` |
| Migrating Java thread-pool code | Virtual threads (minimal code change) |
| Kotlin-first service | Coroutines with structured concurrency |
| Mixed JVM polyglot | Virtual threads (common JVM primitive) |

</div>

---

### Section 9 – Code Repository Links

The finished post will link to the committed examples:

- Scala 3 ZIO fibres: `scala3/src/main/scala/io/github/sps23/fibres/`
- Scala 3 tests: `scala3/src/test/scala/io/github/sps23/fibres/`
- Java 21 virtual threads: `java21/src/main/java/io/github/sps23/fibres/`
- Java 21 tests: `java21/src/test/java/io/github/sps23/fibres/`
- Kotlin coroutines: `kotlin/src/main/kotlin/io/github/sps23/fibres/`
- Kotlin tests: `kotlin/src/test/kotlin/io/github/sps23/fibres/`

---

## Implementation Checklist

For reference, these are the tasks needed to go from this plan to a published post:

- [ ] Add ZIO 2 and ZIO Test dependencies to `scala3/build.gradle`
- [ ] Write `BasicFibres.scala` and `BasicFibresSpec.scala`
- [ ] Write `FibreSupervision.scala` and `FibreSupervisionSpec.scala`
- [ ] Write `FibreConcurrency.scala` and `FibreConcurrencySpec.scala`
- [ ] Write `ResourceSafety.scala` and `ResourceSafetySpec.scala`
- [ ] Write `VirtualThreadBasics.java`, `StructuredTaskScopeDemo.java`, and corresponding JUnit 5 tests
- [ ] Write `CoroutineBasics.kt` and corresponding Kotest tests
- [ ] Run benchmarks and record real numbers for the performance table
- [ ] Write the full blog post using the code tabs HTML pattern from `blog/CODE_TABS.md`
- [ ] Add post to the blog index and update the interview preparation plan if applicable

---

*This is a planning post. The full blog post with complete code, tests, and benchmark results will follow.*
