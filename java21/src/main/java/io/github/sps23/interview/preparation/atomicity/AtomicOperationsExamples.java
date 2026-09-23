package io.github.sps23.interview.preparation.atomicity;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/**
 * Atomicity examples built around claiming the last available ticket.
 */
public final class AtomicOperationsExamples {

    private AtomicOperationsExamples() {
    }

    public static final class VolatileCounter {
        private volatile int counter;

        public int loseOneIncrementDeterministically() {
            counter = 0;

            var start = new CountDownLatch(1);
            var bothRead = new CountDownLatch(2);
            var allowWrite = new CountDownLatch(1);

            var first = new Thread(() -> stagedIncrement(start, bothRead, allowWrite), "counter-reader-1");
            var second = new Thread(() -> stagedIncrement(start, bothRead, allowWrite),
                    "counter-reader-2");

            first.start();
            second.start();

            start.countDown();
            await(bothRead);
            allowWrite.countDown();
            join(first);
            join(second);
            return counter;
        }

        public int currentValue() {
            return counter;
        }

        private void stagedIncrement(CountDownLatch start, CountDownLatch bothRead,
                CountDownLatch allowWrite) {
            await(start);
            var observed = counter;
            bothRead.countDown();
            await(allowWrite);
            counter = observed + 1;
        }
    }

    public record TicketSnapshot(int ticketsRemaining, boolean sellingOpen, String lastBuyer) {

        TicketSnapshot sellTo(String buyer) {
            var updatedRemaining = ticketsRemaining - 1;
            return new TicketSnapshot(updatedRemaining, updatedRemaining > 0, buyer);
        }
    }

    public static final class AtomicTicketOffice {
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

    public static final class SplitAtomicTicketOffice {
        private final AtomicInteger remainingTickets;
        private final AtomicBoolean soldOut = new AtomicBoolean();

        public SplitAtomicTicketOffice(int initialTickets) {
            if (initialTickets < 0) {
                throw new IllegalArgumentException("initialTickets cannot be negative");
            }

            this.remainingTickets = new AtomicInteger(initialTickets);
            this.soldOut.set(initialTickets == 0);
        }

        public boolean claimLastTicket(Runnable beforeSoldOutFlagUpdate) {
            while (true) {
                var observed = remainingTickets.get();
                if (observed == 0) {
                    soldOut.set(true);
                    return false;
                }

                if (remainingTickets.compareAndSet(observed, observed - 1)) {
                    if (beforeSoldOutFlagUpdate != null) {
                        beforeSoldOutFlagUpdate.run();
                    }
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

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while coordinating the demo", exception);
        }
    }

    private static void join(Thread thread) {
        try {
            thread.join();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for the demo threads", exception);
        }
    }
}
