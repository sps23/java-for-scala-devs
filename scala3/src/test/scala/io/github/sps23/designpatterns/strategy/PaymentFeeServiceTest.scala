package io.github.sps23.designpatterns.strategy

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class PaymentFeeServiceTest extends AnyFunSuite with Matchers:

  test("Strategy should calculate card fees with percentage and flat charge") {
    val service = PaymentFeeService.defaultService
    val quote = service.quote(
      PaymentRequest(PaymentMethod.CARD, BigDecimal("100.00"), "GBP", vipCustomer = false)
    )

    quote.fee shouldBe BigDecimal("3.20")
    quote.totalAmount shouldBe BigDecimal("103.20")
  }

  test("Strategy should cap bank transfer fees") {
    val service = PaymentFeeService.defaultService
    val quote = service.quote(
      PaymentRequest(PaymentMethod.BANK_TRANSFER, BigDecimal("1000.00"), "GBP", vipCustomer = false)
    )

    quote.fee shouldBe BigDecimal("7.50")
    quote.totalAmount shouldBe BigDecimal("1007.50")
  }

  test("Strategy should enforce a minimum wallet fee") {
    val service = PaymentFeeService.defaultService
    val quote = service.quote(
      PaymentRequest(PaymentMethod.DIGITAL_WALLET, BigDecimal("5.00"), "GBP", vipCustomer = false)
    )

    quote.fee shouldBe BigDecimal("0.25")
    quote.totalAmount shouldBe BigDecimal("5.25")
  }

  test("Strategy should allow lambda-based custom strategies") {
    val service = new PaymentFeeService(
      Map(PaymentMethod.BUY_NOW_PAY_LATER -> PaymentFeeStrategy(_ => BigDecimal("9.00")))
    )
    val quote = service.quote(
      PaymentRequest(
        PaymentMethod.BUY_NOW_PAY_LATER,
        BigDecimal("200.00"),
        "GBP",
        vipCustomer = false
      )
    )

    quote.fee shouldBe BigDecimal("9.00")
    quote.totalAmount shouldBe BigDecimal("209.00")
  }

  test("Strategy should reject unknown strategies") {
    val service = PaymentFeeService.defaultService
    val error = the[IllegalArgumentException] thrownBy service.quote(
      PaymentRequest(
        PaymentMethod.BUY_NOW_PAY_LATER,
        BigDecimal("200.00"),
        "GBP",
        vipCustomer = false
      )
    )

    error.getMessage shouldBe "No strategy configured for payment method: BUY_NOW_PAY_LATER"
  }
