package io.github.sps23.designpatterns.strategy

sealed trait PaymentMethod
case object CARD              extends PaymentMethod
case object BANK_TRANSFER     extends PaymentMethod
case object DIGITAL_WALLET    extends PaymentMethod
case object BUY_NOW_PAY_LATER extends PaymentMethod

final case class PaymentRequest(
    paymentMethod: PaymentMethod,
    amount: BigDecimal,
    currency: String,
    vipCustomer: Boolean
) {
  require(amount > BigDecimal(0), "Amount must be greater than zero")
  require(currency.trim.nonEmpty, "Currency must not be blank")
}

final case class FeeQuote(
    paymentMethod: PaymentMethod,
    baseAmount: BigDecimal,
    fee: BigDecimal,
    totalAmount: BigDecimal
)

trait PaymentFeeStrategy {
  def calculateFee(request: PaymentRequest): BigDecimal

  def withMinimumFee(minimumFee: BigDecimal): PaymentFeeStrategy =
    PaymentFeeStrategy(request => PaymentFeeService.scale(calculateFee(request).max(minimumFee)))

  def withCap(maximumFee: BigDecimal): PaymentFeeStrategy =
    PaymentFeeStrategy(request => PaymentFeeService.scale(calculateFee(request).min(maximumFee)))
}

object PaymentFeeStrategy {
  def apply(run: PaymentRequest => BigDecimal): PaymentFeeStrategy =
    new PaymentFeeStrategy {
      override def calculateFee(request: PaymentRequest): BigDecimal = run(request)
    }
}

final class PaymentFeeService(
    private val strategies: Map[PaymentMethod, PaymentFeeStrategy]
) {
  def quote(request: PaymentRequest): FeeQuote = {
    val strategy = strategies.getOrElse(
      request.paymentMethod,
      throw new IllegalArgumentException(
        s"No strategy configured for payment method: ${request.paymentMethod}"
      )
    )

    val baseAmount = PaymentFeeService.scale(request.amount)
    val fee        = PaymentFeeService.scale(strategy.calculateFee(request))
    FeeQuote(request.paymentMethod, baseAmount, fee, PaymentFeeService.scale(baseAmount + fee))
  }
}

object PaymentFeeService {
  def defaultService: PaymentFeeService = {
    val card =
      PaymentFeeStrategy(request =>
        scale(request.amount * BigDecimal("0.029") + BigDecimal("0.30"))
      )
    val bankTransfer =
      PaymentFeeStrategy(request => scale(request.amount * BigDecimal("0.008")))
    val digitalWallet = PaymentFeeStrategy { request =>
      val baseFee = request.amount * BigDecimal("0.017")
      val adjusted =
        if (request.vipCustomer) baseFee * BigDecimal("0.50")
        else baseFee
      scale(adjusted)
    }

    new PaymentFeeService(
      Map(
        CARD           -> card,
        BANK_TRANSFER  -> bankTransfer.withCap(BigDecimal("7.50")),
        DIGITAL_WALLET -> digitalWallet.withMinimumFee(BigDecimal("0.25"))
      )
    )
  }

  def scale(amount: BigDecimal): BigDecimal =
    amount.setScale(2, BigDecimal.RoundingMode.HALF_UP)
}
