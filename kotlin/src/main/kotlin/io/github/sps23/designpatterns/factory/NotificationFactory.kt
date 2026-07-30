package io.github.sps23.designpatterns.factory

sealed interface Notification {
    fun send(
        recipient: String,
        message: String,
    ): String
}

object EmailNotification : Notification {
    override fun send(
        recipient: String,
        message: String,
    ): String = "EMAIL to $recipient: $message"
}

object SmsNotification : Notification {
    override fun send(
        recipient: String,
        message: String,
    ): String = "SMS to $recipient: $message"
}

object PushNotification : Notification {
    override fun send(
        recipient: String,
        message: String,
    ): String = "PUSH to $recipient: $message"
}

object NotificationFactory {
    fun create(channel: String?): Notification {
        val normalized =
            channel
                ?.trim()
                ?.lowercase()
                ?.takeIf { it.isNotEmpty() }
                ?: throw IllegalArgumentException("Notification channel must not be blank")

        return when (normalized) {
            "email" -> EmailNotification
            "sms" -> SmsNotification
            "push" -> PushNotification
            else -> throw IllegalArgumentException("Unsupported notification channel: $normalized")
        }
    }
}
