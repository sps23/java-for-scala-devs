package io.github.sps23.interview.preparation.messagepassing

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object MessagePassingExamples:
  def handOffOrdersWithBlockingQueue(incomingOrders: List[String]): List[String] =
    val orders   = new LinkedBlockingQueue[String]()
    val prepared = scala.collection.mutable.ListBuffer.empty[String]

    val kitchenWorker = Thread(
      () =>
        for _ <- incomingOrders.indices do
          val order = takeStringMessage(orders)
          prepared += s"prepared:$order"
      ,
      "kitchen-worker"
    )

    kitchenWorker.start()
    incomingOrders.foreach(orders.offer)
    join(kitchenWorker)
    prepared.toList

  def handOffEmailsWithNonBlockingQueue(outgoingEmails: List[String]): List[String] =
    val mailbox = new ConcurrentLinkedQueue[String]()
    outgoingEmails.foreach(mailbox.offer)

    val sent = scala.collection.mutable.ListBuffer.empty[String]
    var next = mailbox.poll()
    while next != null do
      sent += s"sent:$next"
      next = mailbox.poll()
    sent.toList

  def processWalletTopUpsWithSingleOwner(
      producerCount: Int,
      topUpsPerProducer: Int,
      centsPerTopUp: Int
  ): Int =
    require(producerCount >= 1, "producerCount must be at least one")
    require(topUpsPerProducer >= 0, "topUpsPerProducer cannot be negative")
    require(centsPerTopUp >= 0, "centsPerTopUp cannot be negative")
    val expectedTopUps = Math.multiplyExact(producerCount, topUpsPerProducer)

    val topUpMessages       = new LinkedBlockingQueue[Int]()
    val start               = new CountDownLatch(1)
    val producersDone       = new CountDownLatch(producerCount)
    val finalBalanceInCents = new AtomicInteger(0)

    val walletOwner = Thread(
      () =>
        var localBalance = 0
        for _ <- 0 until expectedTopUps do
          val message = takeIntMessage(topUpMessages)
          localBalance += message
        finalBalanceInCents.set(localBalance)
      ,
      "wallet-owner"
    )
    walletOwner.start()

    for producerIndex <- 0 until producerCount do
      val producer = Thread(
        () =>
          await(start)
          for _ <- 0 until topUpsPerProducer do put(topUpMessages, centsPerTopUp)
          producersDone.countDown()
        ,
        s"top-up-producer-$producerIndex"
      )
      producer.start()

    start.countDown()
    await(producersDone)
    join(walletOwner)
    finalBalanceInCents.get()

  private def takeStringMessage(queue: LinkedBlockingQueue[String]): String =
    try
      val value = queue.poll(1, TimeUnit.SECONDS)
      if value == null then throw IllegalStateException("Timed out while waiting for a message")
      value
    catch
      case exception: InterruptedException =>
        Thread.currentThread().interrupt()
        throw IllegalStateException("Interrupted while waiting for a message", exception)

  private def takeIntMessage(queue: LinkedBlockingQueue[Int]): Int =
    try
      val value: Integer = queue.poll(1, TimeUnit.SECONDS)
      if value == null then throw IllegalStateException("Timed out while waiting for a message")
      value.intValue()
    catch
      case exception: InterruptedException =>
        Thread.currentThread().interrupt()
        throw IllegalStateException("Interrupted while waiting for a message", exception)

  private def put(queue: LinkedBlockingQueue[Int], message: Int): Unit =
    try queue.put(message)
    catch
      case exception: InterruptedException =>
        Thread.currentThread().interrupt()
        throw IllegalStateException("Interrupted while handing off a message", exception)

  private def await(latch: CountDownLatch): Unit =
    try latch.await()
    catch
      case exception: InterruptedException =>
        Thread.currentThread().interrupt()
        throw IllegalStateException("Interrupted while coordinating the demo", exception)

  private def join(thread: Thread): Unit =
    try thread.join()
    catch
      case exception: InterruptedException =>
        Thread.currentThread().interrupt()
        throw IllegalStateException("Interrupted while waiting for the demo thread", exception)
