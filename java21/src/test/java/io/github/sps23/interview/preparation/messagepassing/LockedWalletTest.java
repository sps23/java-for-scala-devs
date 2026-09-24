package io.github.sps23.interview.preparation.messagepassing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Locked wallet Java 21 tests")
class LockedWalletTest {

    @Test
    @DisplayName("Should apply a single top up correctly")
    void shouldApplyASingleTopUpCorrectly() {
        var wallet = new LockedWallet();

        wallet.topUp(500);

        assertEquals(500, wallet.balance());
    }

    @Test
    @DisplayName("Should apply all concurrent top ups without lost updates")
    void shouldApplyAllConcurrentTopUpsWithoutLostUpdates() throws InterruptedException {
        var wallet = new LockedWallet();
        var producerCount = 8;
        var topUpsPerProducer = 500;
        var centsPerTopUp = 25;
        var start = new CountDownLatch(1);
        var producersDone = new CountDownLatch(producerCount);
        ExecutorService executor = Executors.newFixedThreadPool(producerCount);

        try {
            for (var producerIndex = 0; producerIndex < producerCount; producerIndex++) {
                executor.submit(() -> {
                    await(start);
                    for (var i = 0; i < topUpsPerProducer; i++) {
                        wallet.topUp(centsPerTopUp);
                    }
                    producersDone.countDown();
                });
            }
            start.countDown();
            producersDone.await();
        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

        var expectedBalance = producerCount * topUpsPerProducer * centsPerTopUp;
        assertEquals(expectedBalance, wallet.balance());
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while coordinating the test", exception);
        }
    }
}
