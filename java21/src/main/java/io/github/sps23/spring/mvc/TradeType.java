package io.github.sps23.spring.mvc;

/**
 * The type of a stock trade.
 *
 * <p>
 * Used as part of a {@link TradeRequest} to indicate whether the client is
 * buying or selling. In a real Spring MVC application this enum is
 * automatically deserialized from JSON by Jackson.
 */
public enum TradeType {
    BUY, SELL
}
