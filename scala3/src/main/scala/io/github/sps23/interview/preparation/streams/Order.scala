package io.github.sps23.interview.preparation.streams

import java.time.LocalDate

/** An immutable Order case class demonstrating Scala 3 idioms.
  *
  * Case classes provide:
  *   - Automatically generated constructor
  *   - Accessor methods
  *   - equals(), hashCode(), and toString() implementations
  *   - Copy method for creating modified copies
  *   - Pattern matching support
  *
  * @param id
  *   unique order identifier, must be positive
  * @param category
  *   order category for grouping, must not be null or blank
  * @param amount
  *   order total amount, must be non-negative
  * @param customer
  *   customer name, must not be null or blank
  * @param date
  *   order date
  * @param items
  *   list of items in the order
  */
case class Order(
    id: Long,
    category: String,
    amount: Double,
    customer: String,
    date: LocalDate,
    items: List[String]
):
  // Validation using require (throws IllegalArgumentException on failure)
  require(id > 0, s"Order ID must be positive, got: $id")
  require(category != null && category.nonEmpty, "Category cannot be null or blank")
  require(amount >= 0, s"Amount cannot be negative, got: $amount")
  require(customer != null && customer.nonEmpty, "Customer cannot be null or blank")
  require(date != null, "Date cannot be null")
  require(items != null, "Items cannot be null")

  /** Returns the number of items in this order. */
  def itemCount: Int = items.size

  /** Returns a formatted string representation of the order. */
  def toFormattedString: String =
    f"[$id] $category: $$$amount%.2f by $customer ($date) - $itemCount items"

object Order:
  /** Creates an Order with today's date.
    *
    * @param id
    *   order ID
    * @param category
    *   order category
    * @param amount
    *   order amount
    * @param customer
    *   customer name
    * @param items
    *   list of items
    * @return
    *   new Order instance with current date
    */
  def today(
      id: Long,
      category: String,
      amount: Double,
      customer: String,
      items: List[String]
  ): Order =
    Order(id, category, amount, customer, LocalDate.now(), items)
