package io.github.sps23.interview.preparation.concurrentcollections;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Concurrent collections Java 21 tests")
class ConcurrentCollectionsExamplesTest {

    @Test
    @DisplayName("Should lose one HashMap update when two threads race")
    void shouldLoseOneHashMapUpdateWhenTwoThreadsRace() {
        var finalCount = ConcurrentCollectionsExamples.loseOneUpdateWithHashMapRace();

        assertEquals(1, finalCount);
    }

    @Test
    @DisplayName("Should atomically update ConcurrentHashMap with merge")
    void shouldAtomicallyUpdateConcurrentHashMapWithMerge() {
        var finalCount = ConcurrentCollectionsExamples.incrementWithConcurrentHashMapMerge();

        assertEquals(2, finalCount);
    }

    @Test
    @DisplayName("Should drain orders in insertion order with ConcurrentLinkedQueue")
    void shouldDrainOrdersInInsertionOrderWithConcurrentLinkedQueue() {
        var served = ConcurrentCollectionsExamples.drainOrdersWithConcurrentLinkedQueue();

        assertEquals(List.of("ramen", "udon", "pho"), served);
    }

    @Test
    @DisplayName("Should stop consumer when blocking queue receives sentinel")
    void shouldStopConsumerWhenBlockingQueueReceivesSentinel() {
        var served = ConcurrentCollectionsExamples.drainOrdersWithBlockingQueue();

        assertEquals(List.of("ramen", "udon", "pho"), served);
    }

    @Test
    @DisplayName("Should iterate stable snapshot with CopyOnWriteArrayList")
    void shouldIterateStableSnapshotWithCopyOnWriteArrayList() {
        var snapshot = ConcurrentCollectionsExamples.copyOnWriteWaitersSnapshot();

        assertEquals(List.of("Ana", "Ben"), snapshot.iterated());
        assertEquals(List.of("Ana", "Ben", "Cara"), snapshot.finalView());
    }
}
