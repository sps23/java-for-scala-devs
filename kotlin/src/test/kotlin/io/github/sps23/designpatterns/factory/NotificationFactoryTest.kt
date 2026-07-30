package io.github.sps23.designpatterns.factory

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("NotificationFactory Kotlin Tests")
class NotificationFactoryTest {
    @Test
    @DisplayName("Should create an email notification")
    fun shouldCreateEmailNotification() {
        val notification = NotificationFactory.create("email")
        assertTrue(notification is EmailNotification)
        assertEquals("EMAIL to alice@example.com: Welcome!", notification.send("alice@example.com", "Welcome!"))
    }

    @Test
    @DisplayName("Should create an SMS notification")
    fun shouldCreateSmsNotification() {
        val notification = NotificationFactory.create("sms")
        assertTrue(notification is SmsNotification)
        assertEquals("SMS to +441234567890: Code 1234", notification.send("+441234567890", "Code 1234"))
    }

    @Test
    @DisplayName("Should create a push notification")
    fun shouldCreatePushNotification() {
        val notification = NotificationFactory.create("push")
        assertTrue(notification is PushNotification)
        assertEquals("PUSH to user-42: Build completed", notification.send("user-42", "Build completed"))
    }

    @Test
    @DisplayName("Should normalize channel names")
    fun shouldNormalizeChannelNames() {
        val notification = NotificationFactory.create("  EmAiL ")
        assertTrue(notification is EmailNotification)
    }

    @Test
    @DisplayName("Should reject unsupported channels")
    fun shouldRejectUnsupportedChannels() {
        val error =
            assertThrows(IllegalArgumentException::class.java) {
                NotificationFactory.create("fax")
            }
        assertEquals("Unsupported notification channel: fax", error.message)
    }

    @Test
    @DisplayName("Should reject blank channels")
    fun shouldRejectBlankChannels() {
        val error =
            assertThrows(IllegalArgumentException::class.java) {
                NotificationFactory.create("  ")
            }
        assertEquals("Notification channel must not be blank", error.message)
    }

    @Test
    @DisplayName("Should reject null channels")
    fun shouldRejectNullChannels() {
        val error =
            assertThrows(IllegalArgumentException::class.java) {
                NotificationFactory.create(null)
            }
        assertEquals("Notification channel must not be blank", error.message)
    }
}
