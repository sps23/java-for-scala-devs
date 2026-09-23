package io.github.sps23.interview.preparation.atomicity

data class TicketSnapshot(
    val ticketsRemaining: Int,
    val sellingOpen: Boolean,
    val lastBuyer: String?,
) {
    fun sellTo(buyer: String): TicketSnapshot {
        val updatedRemaining = ticketsRemaining - 1
        return TicketSnapshot(updatedRemaining, updatedRemaining > 0, buyer)
    }
}
