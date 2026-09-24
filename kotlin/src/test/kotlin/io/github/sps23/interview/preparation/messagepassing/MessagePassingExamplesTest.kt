package io.github.sps23.interview.preparation.messagepassing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Message passing Kotlin tests")
class MessagePassingExamplesTest {
    @Test
    @DisplayName("Should process orders in order with blocking queue handoff")
    fun shouldProcessOrdersInOrderWithBlockingQueueHandoff() {
        val prepared = MessagePassingExamples.handOffOrdersWithBlockingQueue(listOf("ramen", "udon", "pho"))

        assertEquals(listOf("prepared:ramen", "prepared:udon", "prepared:pho"), prepared)
    }

    @Test
    @DisplayName("Should drain emails without blocking when queue is empty")
    fun shouldDrainEmailsWithoutBlockingWhenQueueIsEmpty() {
        val sent = MessagePassingExamples.handOffEmailsWithNonBlockingQueue(listOf("invoice", "welcome"))

        assertEquals(listOf("sent:invoice", "sent:welcome"), sent)
    }

    @Test
    @DisplayName("Should apply all top ups with single owner queue processing")
    fun shouldApplyAllTopUpsWithSingleOwnerQueueProcessing() {
        val finalBalance = MessagePassingExamples.processWalletTopUpsWithSingleOwner(4, 25, 100)

        assertEquals(10_000, finalBalance)
    }
}
