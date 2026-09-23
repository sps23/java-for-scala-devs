package io.github.sps23.interview.preparation.concurrentcollections

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

object ConcurrentCollectionsExamples {
    fun loseOneUpdateWithHashMapRace(): Int {
        val sharedPot = HashMap<String, Int>()
        sharedPot["spoons"] = 0

        val start = CountDownLatch(1)
        val bothRead = CountDownLatch(2)
        val allowWrite = CountDownLatch(1)

        val first =
            Thread(
                { stagedHashMapIncrement(sharedPot, start, bothRead, allowWrite) },
                "hashmap-chef-1",
            )
        val second =
            Thread(
                { stagedHashMapIncrement(sharedPot, start, bothRead, allowWrite) },
                "hashmap-chef-2",
            )

        first.start()
        second.start()
        start.countDown()
        await(bothRead)
        allowWrite.countDown()
        join(first)
        join(second)
        return sharedPot.getValue("spoons")
    }

    fun incrementWithConcurrentHashMapMerge(): Int {
        val sharedPot = ConcurrentHashMap<String, Int>()
        sharedPot["spoons"] = 0

        val start = CountDownLatch(1)
        val done = CountDownLatch(2)

        val first =
            Thread(
                {
                    await(start)
                    sharedPot.merge("spoons", 1, Int::plus)
                    done.countDown()
                },
                "concurrent-map-chef-1",
            )
        val second =
            Thread(
                {
                    await(start)
                    sharedPot.merge("spoons", 1, Int::plus)
                    done.countDown()
                },
                "concurrent-map-chef-2",
            )

        first.start()
        second.start()
        start.countDown()
        await(done)
        return sharedPot.getValue("spoons")
    }

    fun drainOrdersWithConcurrentLinkedQueue(): List<String> {
        val orders = ConcurrentLinkedQueue<String>()
        orders.offer("ramen")
        orders.offer("udon")
        orders.offer("pho")

        val served = mutableListOf<String>()
        while (true) {
            val order = orders.poll() ?: return served
            served.add(order)
        }
    }

    fun drainOrdersWithBlockingQueue(): List<String> {
        val orders = LinkedBlockingQueue<String>()
        orders.offer("ramen")
        orders.offer("udon")
        orders.offer("pho")
        orders.offer("service-over")

        val served = mutableListOf<String>()
        while (true) {
            val order = take(orders)
            if (order == "service-over") {
                return served
            }
            served.add(order)
        }
    }

    fun copyOnWriteWaitersSnapshot(): CopyOnWriteSnapshot {
        val waiters = CopyOnWriteArrayList(listOf("Ana", "Ben"))
        val iterated = mutableListOf<String>()
        for (waiter in waiters) {
            iterated.add(waiter)
            if (waiter == "Ana") {
                waiters.add("Cara")
            }
        }
        return CopyOnWriteSnapshot(iterated = iterated, finalView = waiters.toList())
    }

    data class CopyOnWriteSnapshot(
        val iterated: List<String>,
        val finalView: List<String>,
    )

    private fun stagedHashMapIncrement(
        sharedPot: HashMap<String, Int>,
        start: CountDownLatch,
        bothRead: CountDownLatch,
        allowWrite: CountDownLatch,
    ) {
        await(start)
        val observed = sharedPot.getValue("spoons")
        bothRead.countDown()
        await(allowWrite)
        sharedPot["spoons"] = observed + 1
    }

    private fun take(orders: LinkedBlockingQueue<String>): String =
        try {
            val order = orders.poll(1, TimeUnit.SECONDS)
            order ?: throw IllegalStateException("Timed out while waiting for an order")
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            throw IllegalStateException("Interrupted while waiting for an order", exception)
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
            throw IllegalStateException("Interrupted while waiting for the demo threads", exception)
        }
    }
}
