---
layout: post
title: "Virtual Threads and Structured Concurrency in Java 21"
date: 2025-11-29 17:00:00 +0000
categories: interview
tags: java java21 scala kotlin virtual-threads concurrency project-loom interview-preparation
---

Project Loom brings revolutionary changes to Java concurrency with virtual threads and structured concurrency. In this post, we'll migrate a thread-pool-based web scraper to virtual threads, demonstrating the dramatic simplification and scalability improvements.

## The Problem: Scaling Concurrent HTTP Requests

Imagine you need to scrape thousands of web pages concurrently. With traditional platform threads, you face several challenges:

| Challenge | Impact |
|-----------|--------|
| Memory | ~1MB stack per thread, limiting total threads |
| Thread Pool Sizing | Too few threads = queuing; too many = memory exhaustion |
| Blocking I/O | Threads sit idle waiting for responses |
| Scalability | 10K concurrent requests requires ~10GB of thread stacks |

## Before: Traditional Thread Pool Approach

Here's how we'd typically implement a web scraper with platform threads:

### Java

```java
public class WebScraperTraditional {
    // Fixed thread pool - typically sized based on available processors
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
    
    private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public List<ScrapedResult> scrapeAll(List<String> urls) {
        List<Callable<ScrapedResult>> tasks = urls.stream()
                .map(url -> (Callable<ScrapedResult>) () -> scrapeUrl(url))
                .toList();

        try {
            List<Future<ScrapedResult>> futures = executor.invokeAll(tasks);
            return futures.stream().map(f -> f.get()).toList();
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            return List.of();
        }
    }
    
    private ScrapedResult scrapeUrl(String url) {
        // Blocking HTTP call - ties up the thread while waiting
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return new ScrapedResult(url, response.statusCode(), response.body().length());
    }
}
```

**Problem**: With 16 threads and 1000 URLs, only 16 requests can run concurrently. The rest queue up.

### Scala

```scala
object WebScraperTraditional:
  private val ThreadPoolSize = Runtime.getRuntime.availableProcessors * 2

  def scrapeAll(urls: List[String]): List[ScrapedResult] =
    Using.resource(Executors.newFixedThreadPool(ThreadPoolSize)) { executor =>
      val tasks = urls.map(url => 
        new Callable[ScrapedResult] { def call() = scrapeUrl(url) }
      ).asJava
      val futures = executor.invokeAll(tasks)
      futures.asScala.map(_.get()).toList
    }
```

### Kotlin

```kotlin
object WebScraperTraditional {
    private val THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2

    fun scrapeAll(urls: List<String>): List<ScrapedResult> {
        val executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE)
        return try {
            val tasks = urls.map { url -> Callable { scrapeUrl(url) } }
            executor.invokeAll(tasks).map { it.get() }
        } finally {
            executor.shutdown()
        }
    }
}
```

## After: Virtual Threads Approach

With Java 21's virtual threads, the migration is surprisingly simple:

### Java

```java
public class WebScraperVirtual {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public List<ScrapedResult> scrapeAll(List<String> urls) {
        // newVirtualThreadPerTaskExecutor() - the key change!
        // Creates a new virtual thread for each task
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<ScrapedResult>> futures = urls.stream()
                    .map(url -> executor.submit(() -> scrapeUrl(url)))
                    .toList();
            
            return futures.stream().map(f -> f.get()).toList();
        }
    }
    
    // The scrapeUrl method is IDENTICAL to before!
    // Blocking code works efficiently with virtual threads
    private ScrapedResult scrapeUrl(String url) {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return new ScrapedResult(url, response.statusCode(), response.body().length());
    }
}
```

**The change**: Just replace `newFixedThreadPool(N)` with `newVirtualThreadPerTaskExecutor()`.

### Scala

```scala
object WebScraperVirtual:
  def scrapeAll(urls: List[String]): List[ScrapedResult] =
    // One virtual thread per URL - all run concurrently!
    Using.resource(Executors.newVirtualThreadPerTaskExecutor()) { executor =>
      val futures = urls.map(url => executor.submit(() => scrapeUrl(url)))
      futures.map(_.get()).toList
    }
```

### Kotlin

```kotlin
object WebScraperVirtual {
    fun scrapeAll(urls: List<String>): List<ScrapedResult> =
        Executors.newVirtualThreadPerTaskExecutor().use { executor ->
            val futures = urls.map { url -> executor.submit<ScrapedResult> { scrapeUrl(url) } }
            futures.map { it.get() }
        }
}
```

## Key Virtual Thread APIs

### Thread.startVirtualThread()

The simplest way to start a virtual thread:

```java
Thread.startVirtualThread(() -> {
    // Your code runs in a virtual thread
    System.out.println("Hello from virtual thread!");
});
```

### Thread.ofVirtual() Builder

For more control over thread creation:

```java
Thread thread = Thread.ofVirtual()
    .name("scraper-", 0)  // Named threads: scraper-0, scraper-1, etc.
    .start(() -> {
        // Your code here
    });
```

### Executors.newVirtualThreadPerTaskExecutor()

The recommended approach for concurrent tasks:

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    // Each submit() creates a new virtual thread
    var future1 = executor.submit(() -> fetchUserData());
    var future2 = executor.submit(() -> fetchProductData());
    
    // All tasks run concurrently
    return combine(future1.get(), future2.get());
}
```

## Structured Concurrency with StructuredTaskScope

Java 21 introduces structured concurrency (preview) for managing related concurrent tasks:

### ShutdownOnFailure - All Must Succeed

```java
public long fetchAllOrFail(List<String> urls) throws Exception {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        // Fork tasks - each runs in its own virtual thread
        List<Subtask<Integer>> subtasks = urls.stream()
                .map(url -> scope.fork(() -> fetchContentLength(url)))
                .toList();

        scope.join();           // Wait for all
        scope.throwIfFailed();  // Throws if any task failed

        // All succeeded - aggregate results
        return subtasks.stream().mapToInt(Subtask::get).sum();
    }
}
```

### ShutdownOnSuccess - First Success Wins

```java
public int fetchAnySuccessful(List<String> mirrors) throws Exception {
    try (var scope = new StructuredTaskScope.ShutdownOnSuccess<Integer>()) {
        // Race multiple mirrors
        for (String url : mirrors) {
            scope.fork(() -> fetchContentLength(url));
        }
        
        scope.join();
        return scope.result();  // Returns first successful result
    }
}
```

### Comparison: ShutdownOnFailure vs ShutdownOnSuccess

| Aspect | ShutdownOnFailure | ShutdownOnSuccess |
|--------|-------------------|-------------------|
| Use case | Need ALL results | Need ANY result |
| On first failure | Cancel all, throw | Continue others |
| On first success | Continue all | Cancel others, return |
| Returns | All results | First success |

## Scoped Values: Modern Alternative to ThreadLocal

Java 21 introduces ScopedValue (preview) as a replacement for ThreadLocal:

```java
private static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();
private static final ScopedValue<UserContext> USER_CONTEXT = ScopedValue.newInstance();

public String handleRequest(String userId, String role, String url) throws Exception {
    String requestId = UUID.randomUUID().toString().substring(0, 8);
    UserContext context = new UserContext(userId, role);

    // Bind scoped values for this request
    return ScopedValue
            .where(REQUEST_ID, requestId)
            .where(USER_CONTEXT, context)
            .call(() -> processRequest(url));
}

private String processRequest(String url) throws Exception {
    // Access scoped values without explicit parameters!
    String requestId = REQUEST_ID.get();
    UserContext user = USER_CONTEXT.get();
    
    log("Processing request for user " + user.userId());
    return fetchWithLogging(url);
}
```

### ThreadLocal vs ScopedValue

| Feature | ThreadLocal | ScopedValue |
|---------|-------------|-------------|
| Mutability | Mutable | Immutable per scope |
| Cleanup | Manual remove() | Automatic with scope |
| Memory | Can leak | Cleaned up automatically |
| Virtual threads | Works, but heavy | Optimized |

## When to Use Virtual Threads vs Platform Threads

### Use Virtual Threads For:

✅ I/O-bound operations (HTTP calls, database queries, file I/O)  
✅ High-concurrency servers (web servers, API gateways)  
✅ Microservices making many outbound API calls  
✅ Batch processing with parallel I/O operations  
✅ Replacing callback-based async code  

### Use Platform Threads For:

✅ CPU-bound computations (number crunching, cryptography)  
✅ Native code integration (JNI calls)  
✅ Operations requiring thread affinity  
✅ Code using synchronized blocks extensively  

## Migration Checklist

1. **Replace ExecutorService creation**
   ```java
   // Before
   Executors.newFixedThreadPool(200)
   // After
   Executors.newVirtualThreadPerTaskExecutor()
   ```

2. **Replace ThreadLocal with ScopedValue**
   ```java
   // Before
   ThreadLocal<User> currentUser = new ThreadLocal<>();
   // After
   ScopedValue<User> CURRENT_USER = ScopedValue.newInstance();
   ```

3. **Replace synchronized with ReentrantLock** (to avoid "pinning")
   ```java
   // Before - can pin virtual thread
   synchronized (lock) { blockingCall(); }
   
   // After - allows virtual thread to unmount
   lock.lock();
   try { blockingCall(); } 
   finally { lock.unlock(); }
   ```

4. **Consider Structured Concurrency** for related tasks

## For Scala Developers

Virtual threads provide similar benefits to effect systems like ZIO or Cats Effect:

| Feature | Virtual Threads | ZIO/Cats Effect |
|---------|-----------------|-----------------|
| Lightweight concurrency | ✓ | ✓ (Fibers) |
| Non-blocking semantics | ✓ | ✓ |
| Blocking code style | ✓ | Via blocking wrapper |
| Structured concurrency | StructuredTaskScope | Built-in |
| Effect tracking | No | Yes (IO monad) |

**Key insight**: Virtual threads let you write blocking-style code that scales like async code, without needing an effect system.

## For Kotlin Developers

Virtual threads complement Kotlin coroutines:

| Use Case | Virtual Threads | Coroutines |
|----------|-----------------|------------|
| Kotlin-only codebase | Can use | Preferred |
| Java interop | Preferred | Possible |
| Blocking Java libraries | Excellent | Needs Dispatchers.IO |
| Structured concurrency | StructuredTaskScope | Built-in |

## Performance Comparison

With 1000 URLs that each take 1 second to fetch:

| Approach | Threads | Time | Memory |
|----------|---------|------|--------|
| Sequential | 1 | ~1000s | ~1MB |
| Thread pool (16) | 16 | ~63s | ~16MB |
| Thread pool (200) | 200 | ~5s | ~200MB |
| Virtual threads | 1000 | ~1s | ~few MB |

Virtual threads achieve maximum parallelism with minimal memory!

## Conclusion

Project Loom's virtual threads represent a paradigm shift in Java concurrency:

- **Simple migration**: Often just change `newFixedThreadPool()` to `newVirtualThreadPerTaskExecutor()`
- **Massive scalability**: Handle millions of concurrent operations
- **Familiar code style**: Write blocking code that scales like async
- **Structured concurrency**: Better resource management and cancellation

For I/O-bound workloads, virtual threads provide dramatic simplification while improving scalability. Combined with structured concurrency and scoped values, Java 21 offers a complete modern concurrency toolkit.

## Code Samples

See the complete implementations in our repository:
- [Java 21 Virtual Threads](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/interview/preparation/virtualthreads)
- [Scala 3 Virtual Threads](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/interview/preparation/virtualthreads)
- [Kotlin Virtual Threads](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/virtualthreads)

---

*This is part of our Java 21 Interview Preparation series. Check out the [full preparation plan](/interview/2025/11/28/java21-interview-preparation-plan.html) for more topics.*
