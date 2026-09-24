package io.github.sps23.interview.preparation.messagepassing

import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

object MessagePassingExamples {
    fun handOffOrdersWithBlockingQueue(incomingOrders: List<String>): List<String> {
        val orders = LinkedBlockingQueue<String>()
        val prepared = mutableListOf<String>()

        val kitchenWorker =
            Thread(
                {
                    repeat(incomingOrders.size) {
                        val order = takeStringMessage(orders)
                        prepared.add("prepared:$order")
                    }
                },
                "kitchen-worker",
            )
        kitchenWorker.start()
        incomingOrders.forEach(orders::offer)
        join(kitchenWorker)
        return prepared.toList()
    }

    fun handOffEmailsWithNonBlockingQueue(outgoingEmails: List<String>): List<String> {
        val mailbox = ConcurrentLinkedQueue<String>()
        outgoingEmails.forEach(mailbox::offer)

        val sent = mutableListOf<String>()
        while (true) {
            val email = mailbox.poll() ?: return sent.toList()
            sent.add("sent:$email")
        }
    }

    fun processWalletTopUpsWithSingleOwner(
        producerCount: Int,
        topUpsPerProducer: Int,
        centsPerTopUp: Int,
    ): Int {
        require(producerCount >= 1) { "producerCount must be at least one" }
        require(topUpsPerProducer >= 0) { "topUpsPerProducer cannot be negative" }
        require(centsPerTopUp >= 0) { "centsPerTopUp cannot be negative" }
        val expectedTopUps = Math.multiplyExact(producerCount, topUpsPerProducer)

        val topUpMessages = LinkedBlockingQueue<Int>()
        val start = CountDownLatch(1)
        val producersDone = CountDownLatch(producerCount)
        val finalBalanceInCents = AtomicInteger(0)

        val walletOwner =
            Thread(
                {
                    var localBalance = 0
                    repeat(expectedTopUps) {
                        val message = takeIntMessage(topUpMessages)
                        localBalance += message
                    }
                    finalBalanceInCents.set(localBalance)
                },
                "wallet-owner",
            )
        walletOwner.start()

        for (producerIndex in 0 until producerCount) {
            val producer =
                Thread(
                    {
                        await(start)
                        repeat(topUpsPerProducer) {
                            put(topUpMessages, centsPerTopUp)
                        }
                        producersDone.countDown()
                    },
                    "top-up-producer-$producerIndex",
                )
            producer.start()
        }

        start.countDown()
        await(producersDone)
        join(walletOwner)
        return finalBalanceInCents.get()
    }

    private fun takeStringMessage(queue: LinkedBlockingQueue<String>): String = try {
        val value = queue.poll(1, TimeUnit.SECONDS)
        value ?: throw IllegalStateException("Timed out while waiting for a message")
    } catch (exception: InterruptedException) {
        Thread.currentThread().interrupt()
        throw IllegalStateException("Interrupted while waiting for a message", exception)
    }

    private fun takeIntMessage(queue: LinkedBlockingQueue<Int>): Int = try {
        val value = queue.poll(1, TimeUnit.SECONDS)
        value ?: throw IllegalStateException("Timed out while waiting for a message")
    } catch (exception: InterruptedException) {
        Thread.currentThread().interrupt()
        throw IllegalStateException("Interrupted while waiting for a message", exception)
    }

    private fun put(queue: LinkedBlockingQueue<Int>, message: Int) {
        try {
            queue.put(message)
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("Interrupted while handing off a message", exception)
        }
    }

    private fun await(latch: CountDownLatch) {
        try {
            latch.await()
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("Interrupted while coordinating the demo", exception)
        }
    }

    private fun join(thread: Thread) {
        try {
            thread.join()
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("Interrupted while waiting for the demo thread", exception)
        }
    }
}
