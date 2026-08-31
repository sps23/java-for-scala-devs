package io.github.sps23.designpatterns.observer;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class MarketTicker {
    public record PriceUpdate(String symbol, BigDecimal price, Instant timestamp) {
    }

    @FunctionalInterface
    public interface PriceObserver {
        void onPriceUpdate(PriceUpdate update);
    }

    private final Set<PriceObserver> observers = ConcurrentHashMap.newKeySet();
    private final Map<String, BigDecimal> latestPrices = new ConcurrentHashMap<>();

    public void subscribe(PriceObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(PriceObserver observer) {
        observers.remove(observer);
    }

    public PriceUpdate publishPrice(String symbol, BigDecimal price) {
        var update = new PriceUpdate(symbol, price, Instant.now());
        latestPrices.put(symbol, price);
        observers.forEach(observer -> observer.onPriceUpdate(update));
        return update;
    }

    public Optional<BigDecimal> latestPrice(String symbol) {
        return Optional.ofNullable(latestPrices.get(symbol));
    }
}
