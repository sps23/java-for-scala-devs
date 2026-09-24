package io.github.sps23.interview.preparation.messagepassing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Message passing Java 21 tests")
class MessagePassingExamplesTest {

    @Test
    @DisplayName("Should process orders in order with blocking queue handoff")
    void shouldProcessOrdersInOrderWithBlockingQueueHandoff() {
        var prepared = MessagePassingExamples.handOffOrdersWithBlockingQueue(List.of("ramen", "udon", "pho"));

        assertEquals(List.of("prepared:ramen", "prepared:udon", "prepared:pho"), prepared);
    }

    @Test
    @DisplayName("Should drain emails without blocking when queue is empty")
    void shouldDrainEmailsWithoutBlockingWhenQueueIsEmpty() {
        var sent = MessagePassingExamples.handOffEmailsWithNonBlockingQueue(List.of("invoice", "welcome"));

        assertEquals(List.of("sent:invoice", "sent:welcome"), sent);
    }

    @Test
    @DisplayName("Should apply all top ups with single owner queue processing")
    void shouldApplyAllTopUpsWithSingleOwnerQueueProcessing() {
        var finalBalance = MessagePassingExamples.processWalletTopUpsWithSingleOwner(4, 25, 100);

        assertEquals(10_000, finalBalance);
    }
}
