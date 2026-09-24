package io.github.sps23.interview.preparation.visibility

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object VisibilityExamples:
  final class VolatileRunningFlag:
    // volatile makes a write in one thread visible to the worker's read.
    @volatile private var running = true

    def startWorker(started: CountDownLatch, stopped: CountDownLatch): Thread =
      val worker = Thread(
        () =>
          started.countDown()
          // Without volatile, this loop could keep reading a stale true value.
          while running do Thread.onSpinWait()
          stopped.countDown()
        ,
        "volatile-flag-worker"
      )
      worker.start()
      worker

    def stop(): Unit =
      // The volatile write publishes the stop request to the worker thread.
      running = false

  final class VolatileCounter:
    // volatile provides visibility, but it does not make counter += 1 atomic.
    @volatile private var counter = 0

    def loseOneIncrementDeterministically(): Int =
      counter = 0

      val start      = new CountDownLatch(1)
      val bothRead   = new CountDownLatch(2)
      val allowWrite = new CountDownLatch(1)

      val first = Thread(() => stagedIncrement(start, bothRead, allowWrite), "counter-reader-1")
      val second = Thread(
        () => stagedIncrement(start, bothRead, allowWrite),
        "counter-reader-2"
      )

      first.start()
      second.start()

      start.countDown()
      // Release both workers after they have reached the read phase.
      await(bothRead)
      allowWrite.countDown()
      join(first)
      join(second)
      counter

    def currentValue: Int = counter

    private def stagedIncrement(
        start: CountDownLatch,
        bothRead: CountDownLatch,
        allowWrite: CountDownLatch
    ): Unit =
      await(start)
      // Both workers can observe the same value before either one writes.
      val observed = counter
      // This makes the read/read interleaving deterministic for the example.
      bothRead.countDown()
      await(allowWrite)
      // Each worker writes observed + 1, so one increment is overwritten.
      counter = observed + 1

  private def await(latch: CountDownLatch): Unit =
    try
      val completed = latch.await(1, TimeUnit.SECONDS)
      if !completed then throw IllegalStateException("Timed out while coordinating the demo")
    catch
      case exception: InterruptedException =>
        Thread.currentThread().interrupt()
        throw IllegalStateException("Interrupted while coordinating the demo", exception)

  private def join(thread: Thread): Unit =
    try
      thread.join(1000L)
      if thread.isAlive then throw IllegalStateException("Timed out while waiting for the demo thread")
    catch
      case exception: InterruptedException =>
        Thread.currentThread().interrupt()
        throw IllegalStateException("Interrupted while waiting for the demo thread", exception)
