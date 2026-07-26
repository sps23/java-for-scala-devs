package io.github.sps23.spring.configuration;

import java.util.Locale;

/**
 * Simulates a bean that still lives in XML because it comes from older
 * configuration shared with legacy modules.
 */
public final class LegacySupportPolicy {

    private final String partnerQueue;
    private final String fallbackQueue;

    public LegacySupportPolicy(String partnerQueue, String fallbackQueue) {
        this.partnerQueue = partnerQueue;
        this.fallbackQueue = fallbackQueue;
    }

    public String supportQueueFor(String salesChannel) {
        return switch (salesChannel.toLowerCase(Locale.ROOT)) {
            case "partner" -> partnerQueue;
            default -> fallbackQueue;
        };
    }
}
