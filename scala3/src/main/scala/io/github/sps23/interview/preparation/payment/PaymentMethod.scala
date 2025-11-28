package io.github.sps23.interview.preparation.payment

import java.time.YearMonth

/** A sealed trait representing different payment methods in a payment system.
  *
  * In Scala, sealed traits provide the same functionality as Java's sealed interfaces: they
  * restrict which classes can extend them to those defined in the same file, enabling exhaustive
  * pattern matching.
  *
  * Key concepts demonstrated:
  *   - `sealed` trait - restricts subtypes to the same compilation unit
  *   - Exhaustive pattern matching - compiler warns if cases are missing
  *   - ADT (Algebraic Data Type) pattern - common in functional programming
  */
sealed trait PaymentMethod:
  def amount: BigDecimal

/** Represents a credit card payment method.
  *
  * @param cardNumber
  *   the 16-digit card number
  * @param expiryDate
  *   the card expiry date
  * @param amount
  *   the transaction amount
  */
case class CreditCard(cardNumber: String, expiryDate: YearMonth, amount: BigDecimal)
    extends PaymentMethod:
  require(
    cardNumber != null && cardNumber.length == 16 && cardNumber.forall(_.isDigit),
    "Card number must be 16 digits"
  )
  require(expiryDate != null && !expiryDate.isBefore(YearMonth.now()), "Card has expired")
  require(amount != null && amount > 0, "Amount must be positive")

  /** Returns a masked card number showing only the last 4 digits. */
  def maskedCardNumber: String = s"****-****-****-${cardNumber.takeRight(4)}"

/** Represents a bank transfer payment method.
  *
  * @param iban
  *   the International Bank Account Number
  * @param bankCode
  *   the bank's identification code (e.g., BIC/SWIFT)
  * @param amount
  *   the transfer amount
  */
case class BankTransfer(iban: String, bankCode: String, amount: BigDecimal) extends PaymentMethod:
  require(
    iban != null && iban.length >= 15 && iban.length <= 34,
    "IBAN must be between 15 and 34 characters"
  )
  require(bankCode != null && bankCode.nonEmpty, "Bank code cannot be blank")
  require(amount != null && amount > 0, "Amount must be positive")

  /** Returns a masked IBAN showing only the last 4 characters. */
  def maskedIban: String = s"****${iban.takeRight(4)}"

/** Represents a digital wallet payment method (e.g., PayPal, Apple Pay, Google Pay).
  *
  * @param provider
  *   the wallet provider name
  * @param accountId
  *   the user's account identifier
  * @param amount
  *   the payment amount
  */
case class DigitalWallet(provider: String, accountId: String, amount: BigDecimal)
    extends PaymentMethod:
  require(provider != null && provider.nonEmpty, "Provider cannot be blank")
  require(accountId != null && accountId.nonEmpty, "Account ID cannot be blank")
  require(amount != null && amount > 0, "Amount must be positive")

  /** Returns a masked account ID for display purposes. */
  def maskedAccountId: String =
    val atIndex = accountId.indexOf('@')
    if atIndex > 0 then s"${accountId.take(math.min(4, atIndex))}***${accountId.drop(atIndex)}"
    else s"${accountId.take(4)}***"
