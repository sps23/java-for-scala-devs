package io.github.sps23.fibres

import zio.*

/** High-level concurrent composition patterns with ZIO fibres.
  *
  * ZIO provides expressive combinators that turn common concurrency patterns into one-liners:
  *
  *   - `race` – run two effects, take the first to complete
  *   - `ZIO.collectAllPar` – run a list of effects all at once, collect all results
  *   - `zipPar` – run two effects in parallel and combine their results as a tuple
  *
  * These are safer than raw fork/join because ZIO automatically cancels the loser in a race and
  * propagates errors properly.
  *
  * For comparison: - Java uses `StructuredTaskScope.ShutdownOnSuccess` for racing -
  * `StructuredTaskScope.ShutdownOnFailure` for parallel-all - Kotlin uses `select {}` for racing
  * and `.awaitAll()` for parallel-all
  */
object FibreConcurrency:

  /** A fast "cache" data source (10 ms simulated latency). */
  val fromCache: UIO[String] = ZIO.sleep(10.millis) *> ZIO.succeed("cached")

  /** A slow "database" data source (200 ms simulated latency). */
  val fromDb: UIO[String] = ZIO.sleep(200.millis) *> ZIO.succeed("db")

  /** Races fromCache against fromDb and returns whichever finishes first.
    *
    * ZIO automatically interrupts the loser once a winner is declared.
    */
  val fastest: UIO[String] = fromCache race fromDb

  /** A list of URLs to "fetch" in parallel. */
  val urls: List[String] = List("url1", "url2", "url3")

  /** Fetches all URLs concurrently and collects all results.
    *
    * `collectAllPar` is the concurrent equivalent of `ZIO.collectAll` (which runs sequentially).
    */
  val allResults: UIO[List[String]] =
    ZIO.collectAllPar(urls.map(url => ZIO.succeed(s"content of $url")))

  /** Runs two effects in parallel and combines their results into a tuple.
    *
    * Useful when you need both results and neither can be considered the "loser".
    */
  val combined: UIO[(String, List[String])] =
    BasicFibres.fetchUser zipPar BasicFibres.fetchOrders
