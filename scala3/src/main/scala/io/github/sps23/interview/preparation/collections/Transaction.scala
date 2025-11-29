package io.github.sps23.interview.preparation.collections

import java.time.LocalDate

/** An immutable Transaction case class demonstrating Scala 3 idioms.
  *
  * Case classes provide:
  *   - Automatically generated constructor
  *   - Accessor methods
  *   - equals(), hashCode(), and toString() implementations
  *   - Copy method for creating modified copies
  *   - Pattern matching support
  *
  * @param id
  *   unique transaction identifier, must be positive
  * @param category
  *   transaction category for grouping, must not be null or blank
  * @param amount
  *   transaction amount, must be positive
  * @param description
  *   transaction description
  * @param date
  *   transaction date
  */
case class Transaction(
    id: Long,
    category: String,
    amount: Double,
    description: String,
    date: LocalDate
):
  // Validation using require (throws IllegalArgumentException on failure)
  require(id > 0, s"Transaction ID must be positive, got: $id")
  require(category != null && category.nonEmpty, "Category cannot be null or blank")
  require(amount > 0, s"Amount must be positive, got: $amount")
  require(description != null, "Description cannot be null")
  require(date != null, "Date cannot be null")

  /** Returns a formatted string representation of the transaction. */
  def toFormattedString: String =
    f"[$id] $category: $$$amount%.2f - $description ($date)"

object Transaction:
  /** Creates a Transaction with today's date.
    *
    * @param id
    *   transaction ID
    * @param category
    *   transaction category
    * @param amount
    *   transaction amount
    * @param description
    *   transaction description
    * @return
    *   new Transaction instance with current date
    */
  def today(id: Long, category: String, amount: Double, description: String): Transaction =
    Transaction(id, category, amount, description, LocalDate.now())
