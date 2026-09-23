package io.github.sps23.interview.preparation.atomicity;

public record TicketSnapshot(int ticketsRemaining, boolean sellingOpen, String lastBuyer) {

    TicketSnapshot sellTo(String buyer) {
        var updatedRemaining = ticketsRemaining - 1;
        return new TicketSnapshot(updatedRemaining, updatedRemaining > 0, buyer);
    }
}
