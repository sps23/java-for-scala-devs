package io.github.sps23.interview.preparation.atomicity;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

public final class AtomicTicketOffice {
    private final AtomicReference<TicketSnapshot> ticketState;
    private final AtomicInteger displayedQueueSize;
    private final AtomicLong totalRevenueInCents = new AtomicLong();
    private final AtomicBoolean soldOut = new AtomicBoolean();
    private final LongAdder claimAttempts = new LongAdder();
    private final long ticketPriceInCents;

    public AtomicTicketOffice(int initialTickets, long ticketPriceInCents) {
        if (initialTickets < 0) {
            throw new IllegalArgumentException("initialTickets cannot be negative");
        }
        if (ticketPriceInCents < 0) {
            throw new IllegalArgumentException("ticketPriceInCents cannot be negative");
        }

        this.ticketPriceInCents = ticketPriceInCents;
        this.ticketState = new AtomicReference<>(
                new TicketSnapshot(initialTickets, initialTickets > 0, null));
        this.displayedQueueSize = new AtomicInteger(initialTickets);
        this.soldOut.set(initialTickets == 0);
    }

    public boolean claimTicket(String buyer) {
        var normalizedBuyer = normalizeBuyer(buyer);
        claimAttempts.increment();

        while (true) {
            var observed = ticketState.get();
            if (!observed.sellingOpen() || observed.ticketsRemaining() == 0) {
                soldOut.set(observed.ticketsRemaining() == 0);
                return false;
            }

            var updated = observed.sellTo(normalizedBuyer);
            if (ticketState.compareAndSet(observed, updated)) {
                totalRevenueInCents.addAndGet(ticketPriceInCents);
                displayedQueueSize.set(updated.ticketsRemaining());
                soldOut.set(updated.ticketsRemaining() == 0);
                return true;
            }
        }
    }

    public TicketSnapshot snapshot() {
        return ticketState.get();
    }

    public int displayedQueueSize() {
        return displayedQueueSize.get();
    }

    public long totalRevenueInCents() {
        return totalRevenueInCents.get();
    }

    public long claimAttempts() {
        return claimAttempts.sum();
    }

    public boolean soldOutFlag() {
        return soldOut.get();
    }

    private static String normalizeBuyer(String buyer) {
        Objects.requireNonNull(buyer, "buyer cannot be null");
        var normalizedBuyer = buyer.trim();
        if (normalizedBuyer.isBlank()) {
            throw new IllegalArgumentException("buyer cannot be blank");
        }
        return normalizedBuyer;
    }
}
