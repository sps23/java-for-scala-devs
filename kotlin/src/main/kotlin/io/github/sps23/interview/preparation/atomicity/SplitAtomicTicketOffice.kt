package io.github.sps23.interview.preparation.atomicity

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class SplitAtomicTicketOffice(initialTickets: Int) {
    private val remainingTickets = AtomicInteger(initialTickets)
    private val soldOut = AtomicBoolean(initialTickets == 0)

    init {
        require(initialTickets >= 0) { "initialTickets cannot be negative" }
    }

    fun claimLastTicket(): Boolean = claimLastTicket { }

    internal fun claimLastTicket(beforeSoldOutFlagUpdate: () -> Unit): Boolean {
        while (true) {
            val observed = remainingTickets.get()
            if (observed == 0) {
                soldOut.set(true)
                return false
            }

            if (remainingTickets.compareAndSet(observed, observed - 1)) {
                beforeSoldOutFlagUpdate()
                if (observed - 1 == 0) {
                    soldOut.set(true)
                }
                return true
            }
        }
    }

    fun remainingTickets(): Int = remainingTickets.get()

    fun soldOutFlag(): Boolean = soldOut.get()
}
