package io.github.sps23.interview.preparation.atomicity

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.atomic.LongAdder

class AtomicTicketOffice(initialTickets: Int, private val ticketPriceInCents: Long) {
    private val ticketState =
        AtomicReference(TicketSnapshot(initialTickets, initialTickets > 0, null))
    private val displayedQueueSize = AtomicInteger(initialTickets)
    private val totalRevenueInCents = AtomicLong(0L)
    private val soldOut = AtomicBoolean(initialTickets == 0)
    private val claimAttempts = LongAdder()

    init {
        require(initialTickets >= 0) { "initialTickets cannot be negative" }
        require(ticketPriceInCents >= 0) { "ticketPriceInCents cannot be negative" }
    }

    fun claimTicket(buyer: String): Boolean {
        val normalizedBuyer = normalizeBuyer(buyer)
        claimAttempts.increment()

        while (true) {
            val observed = ticketState.get()
            if (!observed.sellingOpen || observed.ticketsRemaining == 0) {
                soldOut.set(observed.ticketsRemaining == 0)
                return false
            }

            val updated = observed.sellTo(normalizedBuyer)
            if (ticketState.compareAndSet(observed, updated)) {
                totalRevenueInCents.addAndGet(ticketPriceInCents)
                displayedQueueSize.set(updated.ticketsRemaining)
                soldOut.set(updated.ticketsRemaining == 0)
                return true
            }
        }
    }

    fun snapshot(): TicketSnapshot = ticketState.get()

    fun displayedQueueSize(): Int = displayedQueueSize.get()

    fun totalRevenueInCents(): Long = totalRevenueInCents.get()

    fun claimAttempts(): Long = claimAttempts.sum()

    fun soldOutFlag(): Boolean = soldOut.get()

    private fun normalizeBuyer(buyer: String): String {
        val normalizedBuyer = buyer.trim()
        require(normalizedBuyer.isNotBlank()) { "buyer cannot be blank" }
        return normalizedBuyer
    }
}
