package io.github.sps23.interview.preparation.concurrentcollections

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class ConcurrentCollectionsExamplesTest extends AnyFunSuite with Matchers:

  test("Should lose one HashMap update when two threads race") {
    val finalCount = ConcurrentCollectionsExamples.loseOneUpdateWithHashMapRace()

    finalCount shouldBe 1
  }

  test("Should atomically update ConcurrentHashMap with merge") {
    val finalCount = ConcurrentCollectionsExamples.incrementWithConcurrentHashMapMerge()

    finalCount shouldBe 2
  }

  test("Should drain orders in insertion order with ConcurrentLinkedQueue") {
    val served = ConcurrentCollectionsExamples.drainOrdersWithConcurrentLinkedQueue()

    served shouldBe List("ramen", "udon", "pho")
  }

  test("Should stop consumer when blocking queue receives sentinel") {
    val served = ConcurrentCollectionsExamples.drainOrdersWithBlockingQueue()

    served shouldBe List("ramen", "udon", "pho")
  }

  test("Should iterate stable snapshot with CopyOnWriteArrayList") {
    val snapshot = ConcurrentCollectionsExamples.copyOnWriteWaitersSnapshot()

    snapshot.iterated shouldBe List("Ana", "Ben")
    snapshot.finalView shouldBe List("Ana", "Ben", "Cara")
  }
