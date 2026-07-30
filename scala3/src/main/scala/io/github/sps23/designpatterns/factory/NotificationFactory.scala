package io.github.sps23.designpatterns.factory

sealed trait Notification:
  def send(recipient: String, message: String): String

case object EmailNotification extends Notification:
  override def send(recipient: String, message: String): String =
    s"EMAIL to $recipient: $message"

case object SmsNotification extends Notification:
  override def send(recipient: String, message: String): String =
    s"SMS to $recipient: $message"

case object PushNotification extends Notification:
  override def send(recipient: String, message: String): String =
    s"PUSH to $recipient: $message"

object NotificationFactory:
  def create(channel: String): Notification =
    Option(channel)
      .map(_.trim.toLowerCase)
      .filter(_.nonEmpty)
      .map:
        case "email" => EmailNotification
        case "sms"   => SmsNotification
        case "push"  => PushNotification
        case other =>
          throw new IllegalArgumentException(s"Unsupported notification channel: $other")
      .getOrElse(throw new IllegalArgumentException("Notification channel must not be blank"))
