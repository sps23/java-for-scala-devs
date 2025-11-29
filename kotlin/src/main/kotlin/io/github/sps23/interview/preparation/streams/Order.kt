package io.github.sps23.interview.preparation.streams

import java.time.LocalDate

/**
 * An immutable Order data class demonstrating Kotlin idioms.
 *
 * Data classes provide:
 * - Automatically generated constructor
 * - Accessor methods (properties)
 * - equals(), hashCode(), and toString() implementations
 * - Copy method for creating modified copies
 * - Destructuring declarations
 *
 * @property id unique order identifier, must be positive
 * @property category order category for grouping, must not be blank
 * @property amount order total amount, must be non-negative
 * @property customer customer name, must not be blank
 * @property date order date
 * @property items list of items in the order
 */
data class Order(
    val id: Long,
    val category: String,
    val amount: Double,
    val customer: String,
    val date: LocalDate,
    val items: List<String>,
) {
    init {
        require(id > 0) { "Order ID must be positive, got: $id" }
        require(category.isNotBlank()) { "Category cannot be blank" }
        require(amount >= 0) { "Amount cannot be negative, got: $amount" }
        require(customer.isNotBlank()) { "Customer cannot be blank" }
    }

    /**
     * Returns the number of items in this order.
     */
    val itemCount: Int get() = items.size

    /**
     * Returns a formatted string representation of the order.
     */
    fun toFormattedString(): String = "[$id] $category: $%.2f by $customer ($date) - $itemCount items".format(amount)

    companion object {
        /**
         * Creates an Order with today's date.
         *
         * @param id order ID
         * @param category order category
         * @param amount order amount
         * @param customer customer name
         * @param items list of items
         * @return new Order instance with current date
         */
        fun today(
            id: Long,
            category: String,
            amount: Double,
            customer: String,
            items: List<String>,
        ): Order = Order(id, category, amount, customer, LocalDate.now(), items)
    }
}
