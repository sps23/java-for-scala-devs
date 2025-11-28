package io.github.sps23.interview.preparation.payment

/** Calculates transaction fees for different payment methods.
  *
  * This object demonstrates exhaustive pattern matching with sealed traits in Scala 3. Because
  * `PaymentMethod` is sealed and all subtypes are defined in the same file, the compiler verifies
  * that all cases are handled.
  *
  * Fee structure:
  *   - Credit Card: 2.9% + $0.30 per transaction
  *   - Bank Transfer: flat $2.50 for amounts under $1000, $5.00 otherwise
  *   - Digital Wallet: 2.5% with $0.50 minimum fee
  */
object FeeCalculator:

  private val CreditCardPercentage    = BigDecimal("0.029")
  private val CreditCardFixedFee      = BigDecimal("0.30")
  private val BankTransferLowFee      = BigDecimal("2.50")
  private val BankTransferHighFee     = BigDecimal("5.00")
  private val BankTransferThreshold   = BigDecimal("1000")
  private val DigitalWalletPercentage = BigDecimal("0.025")
  private val DigitalWalletMinFee     = BigDecimal("0.50")

  /** Calculates the transaction fee for a payment method.
    *
    * Uses exhaustive pattern matching with case class destructuring. The compiler verifies that all
    * permitted subtypes of `PaymentMethod` are handled.
    *
    * @param payment
    *   the payment method to calculate fee for
    * @return
    *   the calculated fee amount, rounded to 2 decimal places
    */
  def calculateFee(payment: PaymentMethod): BigDecimal =
    payment match
      case CreditCard(_, _, amount)    => calculateCreditCardFee(amount)
      case BankTransfer(_, _, amount)  => calculateBankTransferFee(amount)
      case DigitalWallet(_, _, amount) => calculateDigitalWalletFee(amount)

  /** Calculates fee using pattern matching with guards.
    *
    * Demonstrates conditional pattern matching using the `if` keyword in patterns. This is similar
    * to Java's `when` clause in switch expressions.
    *
    * @param payment
    *   the payment method
    * @return
    *   a description of the fee calculation
    */
  def describeFee(payment: PaymentMethod): String =
    payment match
      case CreditCard(num, _, amt) if amt > BigDecimal("100") =>
        s"Credit card (high value): 2.9% + $$0.30 on ${num.takeRight(4)}"
      case CreditCard(num, _, _) =>
        s"Credit card (standard): 2.9% + $$0.30 on ${num.takeRight(4)}"
      case BankTransfer(_, code, amt) if amt >= BankTransferThreshold =>
        s"Bank transfer (high value): flat $$5.00 to $code"
      case BankTransfer(_, code, _) =>
        s"Bank transfer (standard): flat $$2.50 to $code"
      case DigitalWallet(provider, _, _) =>
        s"Digital wallet ($provider): 2.5% (min $$0.50)"

  /** Demonstrates simple type pattern matching (without destructuring).
    *
    * @param payment
    *   the payment method
    * @return
    *   the payment type name
    */
  def getPaymentTypeName(payment: PaymentMethod): String =
    payment match
      case _: CreditCard    => "Credit Card"
      case _: BankTransfer  => "Bank Transfer"
      case _: DigitalWallet => "Digital Wallet"

  private def calculateCreditCardFee(amount: BigDecimal): BigDecimal =
    (amount * CreditCardPercentage + CreditCardFixedFee)
      .setScale(2, BigDecimal.RoundingMode.HALF_UP)

  private def calculateBankTransferFee(amount: BigDecimal): BigDecimal =
    if amount < BankTransferThreshold then BankTransferLowFee else BankTransferHighFee

  private def calculateDigitalWalletFee(amount: BigDecimal): BigDecimal =
    (amount * DigitalWalletPercentage)
      .max(DigitalWalletMinFee)
      .setScale(2, BigDecimal.RoundingMode.HALF_UP)

@main def runPaymentExample(): Unit =
  import java.time.YearMonth

  val creditCard   = CreditCard("1234567890123456", YearMonth.now().plusYears(2), BigDecimal("100"))
  val bankTransfer = BankTransfer("DE89370400440532013000", "COBADEFFXXX", BigDecimal("500"))
  val digitalWallet = DigitalWallet("PayPal", "user@example.com", BigDecimal("50"))

  val payments = List(creditCard, bankTransfer, digitalWallet)

  payments.foreach { payment =>
    println(s"Payment: ${FeeCalculator.getPaymentTypeName(payment)}")
    println(s"  Amount: $$${payment.amount}")
    println(s"  Fee: $$${FeeCalculator.calculateFee(payment)}")
    println(s"  Description: ${FeeCalculator.describeFee(payment)}")
    println()
  }
