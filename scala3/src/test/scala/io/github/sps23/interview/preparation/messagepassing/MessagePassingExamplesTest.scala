package io.github.sps23.interview.preparation.messagepassing

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class MessagePassingExamplesTest extends AnyFunSuite with Matchers:

  test("Should process orders in order with blocking queue handoff") {
    val prepared = MessagePassingExamples.handOffOrdersWithBlockingQueue(List("ramen", "udon", "pho"))

    prepared shouldBe List("prepared:ramen", "prepared:udon", "prepared:pho")
  }

  test("Should drain emails without blocking when queue is empty") {
    val sent = MessagePassingExamples.handOffEmailsWithNonBlockingQueue(List("invoice", "welcome"))

    sent shouldBe List("sent:invoice", "sent:welcome")
  }

  test("Should apply all top ups with single owner queue processing") {
    val finalBalance = MessagePassingExamples.processWalletTopUpsWithSingleOwner(4, 25, 100)

    finalBalance shouldBe 10000
  }
