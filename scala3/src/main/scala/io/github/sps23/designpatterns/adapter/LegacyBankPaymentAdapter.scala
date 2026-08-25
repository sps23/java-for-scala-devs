package io.github.sps23.designpatterns.adapter

trait PaymentGateway:
  def charge(request: PaymentRequest): PaymentResult

case class PaymentRequest(customerId: String, amountInCents: Int, currency: String)
case class PaymentResult(approved: Boolean, transactionId: String, message: String)
case class LegacyBankResponse(statusCode: String, reference: String, detail: String)

class LegacyBankApi:
  def submitPayment(clientCode: String, minorUnits: Long, isoCurrency: String): LegacyBankResponse =
    if minorUnits <= 0 then LegacyBankResponse("12", "N/A", "Amount must be greater than zero")
    else if !Set("USD", "EUR", "GBP").contains(isoCurrency) then
      LegacyBankResponse("14", "N/A", s"Unsupported currency: $isoCurrency")
    else LegacyBankResponse("00", s"TX-${clientCode.toUpperCase}-$minorUnits", "Approved")

/** Adapter pattern in Scala 3.
  *
  * Wraps a legacy banking API and exposes a modern payment-gateway interface for checkout services.
  */
class LegacyBankPaymentAdapter(legacyBankApi: LegacyBankApi) extends PaymentGateway:
  override def charge(request: PaymentRequest): PaymentResult =
    if request == null then throw new IllegalArgumentException("Payment request must not be null")
    if Option(request.customerId).forall(_.trim.isEmpty) then
      throw new IllegalArgumentException("Customer id must not be blank")
    if request.amountInCents <= 0 then
      throw new IllegalArgumentException("Amount must be greater than zero")

    val currency = normalizeCurrency(request.currency)
    val response = legacyBankApi.submitPayment(
      clientCode  = request.customerId.trim,
      minorUnits  = request.amountInCents,
      isoCurrency = currency
    )

    val approved = response.statusCode == "00"
    PaymentResult(
      approved      = approved,
      transactionId = response.reference,
      message       = if approved then "Payment approved" else response.detail
    )

  private def normalizeCurrency(currency: String): String =
    Option(currency)
      .map(_.trim.toUpperCase)
      .filter(_.nonEmpty)
      .getOrElse(throw new IllegalArgumentException("Currency must not be blank"))
