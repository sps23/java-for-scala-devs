package io.github.sps23.designpatterns.adapter

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class LegacyBankPaymentAdapterTest extends AnyFunSuite with Matchers:

  private val paymentGateway: PaymentGateway = new LegacyBankPaymentAdapter(new LegacyBankApi)

  test("Adapter should approve checkout payment through legacy bank API"):
    val checkoutService = CheckoutService(paymentGateway)

    val confirmation = checkoutService.checkout("cust-42", 1599, "eur")

    confirmation should startWith("CONFIRMED:TX-CUST-42-1599")

  test("Adapter should reject unsupported currencies from legacy API"):
    val result = paymentGateway.charge(PaymentRequest("cust-42", 1599, "pln"))

    result.approved shouldBe false
    result.message shouldBe "Unsupported currency: PLN"

  test("Adapter should fail fast when amount is not positive"):
    val error = the[IllegalArgumentException] thrownBy paymentGateway.charge(
      PaymentRequest("cust-42", 0, "EUR")
    )
    error.getMessage shouldBe "Amount must be greater than zero"

case class CheckoutService(paymentGateway: PaymentGateway):
  def checkout(customerId: String, amountInCents: Int, currency: String): String =
    val result = paymentGateway.charge(PaymentRequest(customerId, amountInCents, currency))
    if result.approved then s"CONFIRMED:${result.transactionId}"
    else s"REJECTED:${result.message}"
