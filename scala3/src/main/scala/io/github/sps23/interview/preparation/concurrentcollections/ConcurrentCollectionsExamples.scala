package io.github.sps23.interview.preparation.concurrentcollections

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue

import scala.jdk.CollectionConverters.*

object ConcurrentCollectionsExamples:
  def loseOneUpdateWithHashMapRace(): Int =
    val sharedPot = new java.util.HashMap[String, Int]()
    sharedPot.put("spoons", 0)

    val start      = new CountDownLatch(1)
    val bothRead   = new CountDownLatch(2)
    val allowWrite = new CountDownLatch(1)

    val first = Thread(
      () => stagedHashMapIncrement(sharedPot, start, bothRead, allowWrite),
      "hashmap-chef-1"
    )
    val second = Thread(
      () => stagedHashMapIncrement(sharedPot, start, bothRead, allowWrite),
      "hashmap-chef-2"
    )

    first.start()
    second.start()
    start.countDown()
    await(bothRead)
    allowWrite.countDown()
    join(first)
    join(second)
    sharedPot.get("spoons")

  def incrementWithConcurrentHashMapMerge(): Int =
    val sharedPot = new ConcurrentHashMap[String, Int]()
    sharedPot.put("spoons", 0)

    val start = new CountDownLatch(1)
    val done  = new CountDownLatch(2)

    val first = Thread(
      () =>
        await(start)
        sharedPot.merge("spoons", 1, Integer.sum)
        done.countDown()
      ,
      "concurrent-map-chef-1"
    )
    val second = Thread(
      () =>
        await(start)
        sharedPot.merge("spoons", 1, Integer.sum)
        done.countDown()
      ,
      "concurrent-map-chef-2"
    )

    first.start()
    second.start()
    start.countDown()
    await(done)
    sharedPot.get("spoons")

  def drainOrdersWithConcurrentLinkedQueue(): List[String] =
    val orders = new ConcurrentLinkedQueue[String]()
    orders.offer("ramen")
    orders.offer("udon")
    orders.offer("pho")

    val served = scala.collection.mutable.ListBuffer.empty[String]
    var next   = orders.poll()
    while next != null do
      served += next
      next = orders.poll()
    served.toList

  def drainOrdersWithBlockingQueue(): List[String] =
    val orders = new LinkedBlockingQueue[String]()
    orders.offer("ramen")
    orders.offer("udon")
    orders.offer("pho")
    orders.offer("service-over")

    val served = scala.collection.mutable.ListBuffer.empty[String]
    var done   = false
    while !done do
      val order = take(orders)
      if order == "service-over" then done = true
      else served += order
    served.toList

  def copyOnWriteWaitersSnapshot(): CopyOnWriteSnapshot =
    val waiters  = new CopyOnWriteArrayList[String](List("Ana", "Ben").asJava)
    val iterated = scala.collection.mutable.ListBuffer.empty[String]
    for waiter <- waiters.asScala do
      iterated += waiter
      if waiter == "Ana" then waiters.add("Cara")
    CopyOnWriteSnapshot(iterated.toList, waiters.asScala.toList)

  final case class CopyOnWriteSnapshot(iterated: List[String], finalView: List[String])

  private def stagedHashMapIncrement(
      sharedPot: java.util.HashMap[String, Int],
      start: CountDownLatch,
      bothRead: CountDownLatch,
      allowWrite: CountDownLatch
  ): Unit =
    await(start)
    val observed = sharedPot.get("spoons")
    bothRead.countDown()
    await(allowWrite)
    sharedPot.put("spoons", observed + 1)

  private def take(orders: LinkedBlockingQueue[String]): String =
    try orders.take()
    catch
      case exception: InterruptedException =>
        Thread.currentThread().interrupt()
        throw IllegalStateException("Interrupted while waiting for an order", exception)

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
        throw IllegalStateException("Interrupted while waiting for the demo threads", exception)
