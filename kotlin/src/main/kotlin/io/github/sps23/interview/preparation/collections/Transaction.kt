package io.github.sps23.interview.preparation.collections

import java.time.LocalDate

/**
 * An immutable Transaction data class demonstrating Kotlin idioms.
 *
 * Data classes provide:
 * - Automatically generated constructor
 * - Accessor methods (properties)
 * - equals(), hashCode(), and toString() implementations
 * - Copy method for creating modified copies
 * - Destructuring declarations
 *
 * @property id unique transaction identifier, must be positive
 * @property category transaction category for grouping, must not be blank
 * @property amount transaction amount, must be positive
 * @property description transaction description
 * @property date transaction date
 */
data class Transaction(
    val id: Long,
    val category: String,
    val amount: Double,
    val description: String,
    val date: LocalDate,
) {
    init {
        require(id > 0) { "Transaction ID must be positive, got: $id" }
        require(category.isNotBlank()) { "Category cannot be blank" }
        require(amount > 0) { "Amount must be positive, got: $amount" }
    }

    /**
     * Returns a formatted string representation of the transaction.
     */
    fun toFormattedString(): String = "[$id] $category: $%.2f - $description ($date)".format(amount)

    companion object {
        /**
         * Creates a Transaction with today's date.
         *
         * @param id transaction ID
         * @param category transaction category
         * @param amount transaction amount
         * @param description transaction description
         * @return new Transaction instance with current date
         */
        fun today(
            id: Long,
            category: String,
            amount: Double,
            description: String,
        ): Transaction = Transaction(id, category, amount, description, LocalDate.now())
    }
}
