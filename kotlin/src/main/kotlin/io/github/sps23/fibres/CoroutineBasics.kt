package io.github.sps23.fibres

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

/**
 * Basic Kotlin coroutine patterns – the Kotlin counterpart to ZIO fibres and Java virtual threads.
 *
 * Kotlin coroutines use **structured concurrency**: every coroutine lives inside a scope. When the
 * scope finishes, all its children are guaranteed to have either completed or been cancelled. No
 * coroutine can outlive its parent scope.
 *
 * Key operations:
 * - `async { }` – launches a new coroutine and returns a `Deferred<T>` (like ZIO's `.fork`)
 * - `deferred.await()` – suspends until the result is ready (like ZIO's `fibre.join`)
 * - `coroutineScope { }` – creates a scope and waits for all children before returning
 *
 * For comparison with other languages see:
 * - Scala 3: BasicFibres.scala (ZIO fork/join)
 * - Java 21: VirtualThreadBasics.java (Executors.newVirtualThreadPerTaskExecutor)
 */
object CoroutineBasics {
    /**
     * Fetches a user and their orders concurrently using Kotlin coroutines.
     *
     * Both [async] blocks start immediately. [await] then collects the results once both are done.
     * Total time ≈ max(100ms, 80ms) rather than 100ms + 80ms = 180ms.
     */
    suspend fun fetchUserAndOrders(): String =
        coroutineScope {
            val userDeferred = async { fetchUser() }
            val ordersDeferred = async { fetchOrders() }
            val user = userDeferred.await()
            val orders = ordersDeferred.await()
            "$user has ${orders.size} orders"
        }

    /**
     * Fetches all URLs in parallel and collects every result.
     *
     * `awaitAll()` is the idiomatic way to run a list of [async] coroutines and collect all
     * results – equivalent to ZIO's `collectAllPar` and Java's `ShutdownOnFailure` scope.
     */
    suspend fun fetchAllUrls(urls: List<String>): List<String> =
        coroutineScope {
            urls.map { url -> async { "content of $url" } }.awaitAll()
        }

    /** Simulates a remote user-profile fetch (100 ms latency). */
    private suspend fun fetchUser(): String {
        delay(100)
        return "Alice"
    }

    /** Simulates a remote order-history fetch (80 ms latency). */
    private suspend fun fetchOrders(): List<String> {
        delay(80)
        return listOf("order-1", "order-2")
    }
}
