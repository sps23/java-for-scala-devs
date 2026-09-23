package io.github.sps23.interview.preparation.atomicity;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Ticket office that updates the complete ticket state atomically as one
 * immutable snapshot.
 */
public final class AtomicReferenceTicketOffice {

    private final AtomicReference<State> state;

    public AtomicReferenceTicketOffice(int initialTickets) {
        if (initialTickets < 0) {
            throw new IllegalArgumentException("initialTickets cannot be negative");
        }
        state = new AtomicReference<>(new State(initialTickets, initialTickets > 0, null));
    }

    public boolean claimTicket(String buyer) {
        var normalizedBuyer = normalizeBuyer(buyer);

        while (true) {
            var observed = state.get();
            if (!observed.sellingOpen()) {
                return false;
            }

            var updatedTickets = observed.ticketsRemaining() - 1;
            var updated = new State(updatedTickets, updatedTickets > 0, normalizedBuyer);
            if (state.compareAndSet(observed, updated)) {
                return true;
            }
        }
    }

    public State snapshot() {
        return state.get();
    }

    private static String normalizeBuyer(String buyer) {
        Objects.requireNonNull(buyer, "buyer cannot be null");
        var normalizedBuyer = buyer.trim();
        if (normalizedBuyer.isBlank()) {
            throw new IllegalArgumentException("buyer cannot be blank");
        }
        return normalizedBuyer;
    }

    public record State(int ticketsRemaining, boolean sellingOpen, String lastBuyer) {
    }
}
