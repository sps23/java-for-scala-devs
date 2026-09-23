package io.github.sps23.interview.preparation.atomicity;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class SplitAtomicTicketOffice {
    private final AtomicInteger remainingTickets;
    private final AtomicBoolean soldOut = new AtomicBoolean();

    public SplitAtomicTicketOffice(int initialTickets) {
        if (initialTickets < 0) {
            throw new IllegalArgumentException("initialTickets cannot be negative");
        }

        this.remainingTickets = new AtomicInteger(initialTickets);
        this.soldOut.set(initialTickets == 0);
    }

    public boolean claimLastTicket() {
        return claimLastTicket(() -> {
        });
    }

    boolean claimLastTicket(Runnable beforeSoldOutFlagUpdate) {
        Objects.requireNonNull(beforeSoldOutFlagUpdate, "beforeSoldOutFlagUpdate cannot be null");

        while (true) {
            var observed = remainingTickets.get();
            if (observed == 0) {
                soldOut.set(true);
                return false;
            }

            if (remainingTickets.compareAndSet(observed, observed - 1)) {
                beforeSoldOutFlagUpdate.run();
                if (observed - 1 == 0) {
                    soldOut.set(true);
                }
                return true;
            }
        }
    }

    public int remainingTickets() {
        return remainingTickets.get();
    }

    public boolean soldOutFlag() {
        return soldOut.get();
    }
}
