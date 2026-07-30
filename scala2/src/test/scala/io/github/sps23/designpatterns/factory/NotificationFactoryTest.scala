package io.github.sps23.designpatterns.factory

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class NotificationFactoryTest extends AnyFunSuite with Matchers {

  test("Factory should create email notification") {
    val notification = NotificationFactory.create("email")
    notification shouldBe EmailNotification
    notification.send(
      "alice@example.com",
      "Welcome!"
    ) shouldBe "EMAIL to alice@example.com: Welcome!"
  }

  test("Factory should create SMS notification") {
    val notification = NotificationFactory.create("sms")
    notification shouldBe SmsNotification
    notification.send("+441234567890", "Code 1234") shouldBe "SMS to +441234567890: Code 1234"
  }

  test("Factory should create push notification") {
    val notification = NotificationFactory.create("push")
    notification shouldBe PushNotification
    notification.send("user-42", "Build completed") shouldBe "PUSH to user-42: Build completed"
  }

  test("Factory should normalize channel names") {
    NotificationFactory.create("  EmAiL ") shouldBe EmailNotification
  }

  test("Factory should reject unsupported channels") {
    val error = the[IllegalArgumentException] thrownBy NotificationFactory.create("fax")
    error.getMessage shouldBe "Unsupported notification channel: fax"
  }

  test("Factory should reject blank channels") {
    val error = the[IllegalArgumentException] thrownBy NotificationFactory.create("  ")
    error.getMessage shouldBe "Notification channel must not be blank"
  }
}
