package io.github.sps23.interview.preparation.concurrentcollections

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Concurrent collections Kotlin tests")
class ConcurrentCollectionsExamplesTest {
    @Test
    @DisplayName("Should lose one HashMap update when two threads race")
    fun shouldLoseOneHashMapUpdateWhenTwoThreadsRace() {
        val finalCount = ConcurrentCollectionsExamples.loseOneUpdateWithHashMapRace()

        assertEquals(1, finalCount)
    }

    @Test
    @DisplayName("Should atomically update ConcurrentHashMap with merge")
    fun shouldAtomicallyUpdateConcurrentHashMapWithMerge() {
        val finalCount = ConcurrentCollectionsExamples.incrementWithConcurrentHashMapMerge()

        assertEquals(2, finalCount)
    }

    @Test
    @DisplayName("Should drain orders in insertion order with ConcurrentLinkedQueue")
    fun shouldDrainOrdersInInsertionOrderWithConcurrentLinkedQueue() {
        val served = ConcurrentCollectionsExamples.drainOrdersWithConcurrentLinkedQueue()

        assertEquals(listOf("ramen", "udon", "pho"), served)
    }

    @Test
    @DisplayName("Should stop consumer when blocking queue receives sentinel")
    fun shouldStopConsumerWhenBlockingQueueReceivesSentinel() {
        val served = ConcurrentCollectionsExamples.drainOrdersWithBlockingQueue()

        assertEquals(listOf("ramen", "udon", "pho"), served)
    }

    @Test
    @DisplayName("Should iterate stable snapshot with CopyOnWriteArrayList")
    fun shouldIterateStableSnapshotWithCopyOnWriteArrayList() {
        val snapshot = ConcurrentCollectionsExamples.copyOnWriteWaitersSnapshot()

        assertEquals(listOf("Ana", "Ben"), snapshot.iterated)
        assertEquals(listOf("Ana", "Ben", "Cara"), snapshot.finalView)
    }
}
