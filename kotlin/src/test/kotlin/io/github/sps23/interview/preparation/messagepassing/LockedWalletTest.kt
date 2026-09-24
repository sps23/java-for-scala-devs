package io.github.sps23.interview.preparation.messagepassing

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@DisplayName("Locked wallet Kotlin tests")
class LockedWalletTest {
    @Test
    @DisplayName("Should apply a single top up correctly")
    fun shouldApplyASingleTopUpCorrectly() {
        val wallet = LockedWallet()

        wallet.topUp(500)

        assertEquals(500, wallet.balance())
    }

    @Test
    @DisplayName("Should apply all concurrent top ups without lost updates")
    fun shouldApplyAllConcurrentTopUpsWithoutLostUpdates() {
        val wallet = LockedWallet()
        val producerCount = 8
        val topUpsPerProducer = 500
        val centsPerTopUp = 25
        val start = CountDownLatch(1)
        val producersDone = CountDownLatch(producerCount)
        val executor = Executors.newFixedThreadPool(producerCount)

        try {
            repeat(producerCount) {
                executor.submit {
                    start.await()
                    repeat(topUpsPerProducer) {
                        wallet.topUp(centsPerTopUp)
                    }
                    producersDone.countDown()
                }
            }
            start.countDown()
            producersDone.await()
        } finally {
            executor.shutdown()
            executor.awaitTermination(5, TimeUnit.SECONDS)
        }

        val expectedBalance = producerCount * topUpsPerProducer * centsPerTopUp
        assertEquals(expectedBalance, wallet.balance())
    }
}
