package io.github.sps23.designpatterns.factory;

import java.util.Locale;

sealed interface Notification permits EmailNotification, SmsNotification, PushNotification {
    String send(String recipient, String message);
}

final class EmailNotification implements Notification {
    @Override
    public String send(String recipient, String message) {
        return "EMAIL to " + recipient + ": " + message;
    }
}

final class SmsNotification implements Notification {
    @Override
    public String send(String recipient, String message) {
        return "SMS to " + recipient + ": " + message;
    }
}

final class PushNotification implements Notification {
    @Override
    public String send(String recipient, String message) {
        return "PUSH to " + recipient + ": " + message;
    }
}

/**
 * Factory pattern in Java 21.
 *
 * Centralizes object creation so calling code does not depend on concrete
 * implementations.
 */
public final class NotificationFactory {
    private NotificationFactory() {
    }

    public static Notification create(String channel) {
        if (channel == null || channel.isBlank()) {
            throw new IllegalArgumentException("Notification channel must not be blank");
        }

        return switch (channel.trim().toLowerCase(Locale.ROOT)) {
            case "email" -> new EmailNotification();
            case "sms" -> new SmsNotification();
            case "push" -> new PushNotification();
            default ->
                throw new IllegalArgumentException("Unsupported notification channel: " + channel);
        };
    }
}
