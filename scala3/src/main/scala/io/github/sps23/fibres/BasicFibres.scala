package io.github.sps23.fibres

import zio.*

/** Demonstrates the basics of ZIO fibres: forking tasks and joining results.
  *
  * A ZIO fibre is a lightweight, user-space "thread" managed entirely by the ZIO runtime. Unlike
  * platform threads (~1 MB stack each) or even virtual threads (~few KB), fibres use only a few
  * hundred bytes and are scheduled cooperatively on a small pool of carrier threads.
  *
  * The key operations: - `effect.fork` – spawns a new fibre, returns a `Fibre` handle immediately
  *   - `fibre.join` – waits for the fibre to finish and returns its value
  *   - `fibre.interrupt` – cancels the fibre (always safe, releases resources)
  *
  * For comparison with Java and Kotlin equivalents see: - Java: VirtualThreadBasics.java
  * (Executors.newVirtualThreadPerTaskExecutor) - Kotlin: CoroutineBasics.kt (async/await inside
  * coroutineScope)
  */
object BasicFibres:

  /** Simulates fetching a user profile with a short delay. */
  val fetchUser: UIO[String] =
    ZIO.sleep(100.millis) *> ZIO.succeed("Alice")

  /** Simulates fetching order history with a shorter delay. */
  val fetchOrders: UIO[List[String]] =
    ZIO.sleep(80.millis) *> ZIO.succeed(List("order-1", "order-2"))

  /** Runs both fetches in parallel using fork/join and combines the results.
    *
    * Total time ≈ max(100ms, 80ms) = 100ms, not 180ms, because both tasks run concurrently.
    */
  val program: UIO[String] =
    for
      userFibre  <- fetchUser.fork
      orderFibre <- fetchOrders.fork
      user       <- userFibre.join
      orders     <- orderFibre.join
    yield s"$user has ${orders.length} orders"
