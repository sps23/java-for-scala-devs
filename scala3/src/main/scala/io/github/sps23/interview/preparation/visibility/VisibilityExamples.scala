package io.github.sps23.interview.preparation.visibility

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object VisibilityExamples:
  final class VolatileRunningFlag:
    @volatile private var running = true

    def startWorker(started: CountDownLatch, stopped: CountDownLatch): Thread =
      val worker = Thread(
        () =>
          started.countDown()
          while running do Thread.onSpinWait()
          stopped.countDown()
        ,
        "volatile-flag-worker"
      )
      worker.start()
      worker

    def stop(): Unit =
      running = false

  final class VolatileCounter:
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
      val observed = counter
      bothRead.countDown()
      await(allowWrite)
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
