package io.github.sps23.designpatterns.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("NotificationFactory Java 21 Tests")
class NotificationFactoryTest {

    @Test
    @DisplayName("Should create an email notification")
    void shouldCreateEmailNotification() {
        Notification notification = NotificationFactory.create("email");
        assertInstanceOf(EmailNotification.class, notification);
        assertEquals("EMAIL to alice@example.com: Welcome!",
                notification.send("alice@example.com", "Welcome!"));
    }

    @Test
    @DisplayName("Should create an SMS notification")
    void shouldCreateSmsNotification() {
        Notification notification = NotificationFactory.create("sms");
        assertInstanceOf(SmsNotification.class, notification);
        assertEquals("SMS to +441234567890: Code 1234",
                notification.send("+441234567890", "Code 1234"));
    }

    @Test
    @DisplayName("Should create a push notification")
    void shouldCreatePushNotification() {
        Notification notification = NotificationFactory.create("push");
        assertInstanceOf(PushNotification.class, notification);
        assertEquals("PUSH to user-42: Build completed",
                notification.send("user-42", "Build completed"));
    }

    @Test
    @DisplayName("Should handle mixed case and extra whitespace")
    void shouldNormalizeChannelName() {
        Notification notification = NotificationFactory.create("  EmAiL ");
        assertInstanceOf(EmailNotification.class, notification);
    }

    @Test
    @DisplayName("Should reject unsupported channels")
    void shouldRejectUnsupportedChannels() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> NotificationFactory.create("fax"));
        assertEquals("Unsupported notification channel: fax", error.getMessage());
    }

    @Test
    @DisplayName("Should reject blank channels")
    void shouldRejectBlankChannels() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> NotificationFactory.create("  "));
        assertEquals("Notification channel must not be blank", error.getMessage());
    }
}
