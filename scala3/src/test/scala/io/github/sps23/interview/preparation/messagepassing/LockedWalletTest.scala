package io.github.sps23.interview.preparation.messagepassing

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LockedWalletTest extends AnyFunSuite with Matchers:

  test("Should apply a single top up correctly") {
    val wallet = new LockedWallet()

    wallet.topUp(500)

    wallet.balance shouldBe 500
  }

  test("Should apply all concurrent top ups without lost updates") {
    val wallet            = new LockedWallet()
    val producerCount     = 8
    val topUpsPerProducer = 500
    val centsPerTopUp     = 25
    val start             = new CountDownLatch(1)
    val producersDone     = new CountDownLatch(producerCount)
    val executor          = Executors.newFixedThreadPool(producerCount)

    try
      for _ <- 0 until producerCount do
        executor.submit(
          new Runnable:
            override def run(): Unit =
              start.await()
              for _ <- 0 until topUpsPerProducer do wallet.topUp(centsPerTopUp)
              producersDone.countDown()
        )
      start.countDown()
      producersDone.await()
    finally
      executor.shutdown()
      executor.awaitTermination(5, TimeUnit.SECONDS)

    val expectedBalance = producerCount * topUpsPerProducer * centsPerTopUp
    wallet.balance shouldBe expectedBalance
  }
