package io.github.sps23.interview.preparation

/**
 * Immutable Employee data class in Kotlin.
 * Demonstrates idiomatic Kotlin for data modeling, with validation in init block.
 */
data class EmployeeDataClass(
    val id: Long,
    val name: String,
    val email: String,
    val department: String,
    val salary: Double,
) {
    init {
        require(id > 0) { "Employee ID must be positive" }
        require(name.isNotBlank()) { "Employee name cannot be blank" }
        require(email.contains("@")) { "Invalid email format: $email" }
        require(department.isNotBlank()) { "Department cannot be blank" }
        require(salary >= 0) { "Salary cannot be negative" }
    }

    /**
     * Custom formatted string for display.
     */
    fun toFormattedString(): String = "Employee #$id: $name ($email) - $department - $%.2f".format(salary)

    companion object {
        fun of(
            id: Long,
            name: String,
            email: String,
            department: String,
            salary: Double,
        ) = EmployeeDataClass(id, name, email, department, salary)
    }
}

// Example usage
fun main() {
    val employee = EmployeeDataClass.of(1L, "Alice", "alice@example.com", "Engineering", 75000.0)
    println(employee.name) // Alice
    println(employee.department) // Engineering
    println(employee.toFormattedString())
}
