package io.github.sps23.interview.preparation.stringtemplates

import java.time.LocalDateTime

/**
 * Demonstrates Kotlin String Templates, comparing with Java 21 String Templates.
 *
 * Kotlin provides built-in string templates that are similar to Java 21's String Templates
 * but have been part of the language since its inception. This class showcases a SQL query
 * builder using safe interpolation patterns.
 *
 * Key Concepts:
 * - Simple templates: $variable
 * - Expression templates: ${expression}
 * - Multi-line strings with trimIndent/trimMargin
 * - Extension functions for custom string operations
 * - Safety benefits of parameterized queries
 */
object StringTemplates {
    // ========================================================================
    // Basic String Templates
    // ========================================================================

    /**
     * Demonstrates basic string template usage.
     *
     * Kotlin's string templates perform simple string interpolation, embedding
     * expression values directly into the string using $ prefix.
     *
     * @param name the name to greet
     * @param age the age to display
     * @return a formatted greeting string
     */
    fun basicInterpolation(
        name: String,
        age: Int,
    ): String = "Hello, $name! You are $age years old."

    /**
     * Demonstrates expressions within template placeholders.
     *
     * @param x first operand
     * @param y second operand
     * @return string showing arithmetic operations
     */
    fun expressionInterpolation(
        x: Int,
        y: Int,
    ): String = "Sum: $x + $y = ${x + y}, Product: ${x * y}"

    /**
     * Demonstrates multi-line string templates with trimIndent.
     *
     * @param name user name
     * @param email user email
     * @param active active status
     * @return formatted JSON string
     */
    fun multiLineTemplate(
        name: String,
        email: String,
        active: Boolean,
    ): String =
        """
        {
            "name": "$name",
            "email": "$email",
            "active": $active
        }
        """.trimIndent()

    // ========================================================================
    // Formatted Output
    // ========================================================================

    /**
     * Demonstrates formatted output using String.format() with templates.
     *
     * Kotlin combines string templates with format() for printf-style formatting,
     * allowing precise control over number formatting, alignment, and padding.
     *
     * @param item item name
     * @param quantity quantity
     * @param unitPrice price per unit
     * @return formatted receipt line
     */
    fun formattedOutput(
        item: String,
        quantity: Int,
        unitPrice: Double,
    ): String {
        val total = quantity * unitPrice
        return "Item: $item, Qty: $quantity, Price: $%.2f, Total: $%.2f".format(unitPrice, total)
    }

    /**
     * Creates a formatted table row.
     *
     * @param id employee ID
     * @param name employee name
     * @param salary employee salary
     * @return formatted table row
     */
    fun tableRow(
        id: Int,
        name: String,
        salary: Double,
    ): String = "| %5d | %-20s | $%10.2f |".format(id, name, salary)

    // ========================================================================
    // SQL Query Builder - Safe Parameter Interpolation
    // ========================================================================

    /**
     * A safe SQL query builder that prevents SQL injection by using
     * parameterized queries.
     *
     * This class demonstrates how to build domain-specific query builders
     * that are safer than string concatenation.
     *
     * @property query the parameterized SQL query
     * @property parameters the list of parameter values
     */
    data class SafeQueryBuilder(
        val query: String = "",
        val parameters: List<Any> = emptyList(),
    ) {
        /**
         * Sets the SELECT clause.
         *
         * @param columns columns to select
         * @return new builder with SELECT clause
         */
        fun select(vararg columns: String): SafeQueryBuilder = copy(query = "SELECT ${columns.joinToString(", ")}")

        /**
         * Sets the FROM clause.
         *
         * @param table table name
         * @return new builder with FROM clause
         */
        fun from(table: String): SafeQueryBuilder = copy(query = "$query FROM $table")

        /**
         * Adds a WHERE condition with a parameterized value.
         *
         * @param column column name
         * @param operator comparison operator
         * @param value parameter value (safely parameterized)
         * @return new builder with WHERE condition
         */
        fun where(
            column: String,
            operator: String,
            value: Any,
        ): SafeQueryBuilder {
            val prefix = if (query.contains("WHERE")) " AND" else " WHERE"
            return copy(
                query = "$query$prefix $column $operator ?",
                parameters = parameters + value,
            )
        }

        /**
         * Adds an ORDER BY clause.
         *
         * @param column column to order by
         * @param direction ASC or DESC
         * @return new builder with ORDER BY clause
         */
        fun orderBy(
            column: String,
            direction: String,
        ): SafeQueryBuilder = copy(query = "$query ORDER BY $column $direction")

        /**
         * Adds a LIMIT clause.
         *
         * @param limit maximum number of rows
         * @return new builder with LIMIT clause
         */
        fun limit(limit: Int): SafeQueryBuilder =
            copy(
                query = "$query LIMIT ?",
                parameters = parameters + limit,
            )

        /**
         * Returns a debug representation showing query and parameters.
         *
         * @return debug string
         */
        fun toDebugString(): String =
            """
            Query: $query
            Parameters: $parameters
            """.trimIndent()
    }

    /**
     * Demonstrates building a safe SELECT query.
     *
     * @param tableName table to query
     * @param minAge minimum age filter
     * @param status status filter
     * @return the safe query builder with configured query
     */
    fun buildUserQuery(
        tableName: String,
        minAge: Int,
        status: String,
    ): SafeQueryBuilder =
        SafeQueryBuilder()
            .select("id", "name", "email", "age")
            .from(tableName)
            .where("age", ">=", minAge)
            .where("status", "=", status)
            .orderBy("name", "ASC")
            .limit(100)

    // ========================================================================
    // Unsafe vs Safe Comparison
    // ========================================================================

    /**
     * Demonstrates UNSAFE string concatenation (vulnerable to SQL injection).
     *
     * WARNING: This is an example of what NOT to do. String concatenation with
     * user input can lead to SQL injection attacks.
     *
     * @param name user-provided name (potentially malicious)
     * @return UNSAFE query string
     */
    fun unsafeQuery(name: String): String =
        // DANGEROUS: Never do this in production!
        "SELECT * FROM users WHERE name = '$name'"

    /**
     * Demonstrates safe parameterized query building.
     *
     * @param name user-provided name
     * @return safe query builder with parameterized value
     */
    fun safeQuery(name: String): SafeQueryBuilder = SafeQueryBuilder().select("*").from("users").where("name", "=", name)

    // ========================================================================
    // Complex Template Examples
    // ========================================================================

    /**
     * Creates an HTML template with interpolated values.
     *
     * @param title page title
     * @param heading main heading
     * @param content page content
     * @return HTML string
     */
    fun htmlTemplate(
        title: String,
        heading: String,
        content: String,
    ): String =
        """
        <!DOCTYPE html>
        <html>
        <head>
            <title>${escapeHtml(title)}</title>
        </head>
        <body>
            <h1>${escapeHtml(heading)}</h1>
            <div class="content">
                ${escapeHtml(content)}
            </div>
        </body>
        </html>
        """.trimIndent()

    /**
     * Escapes HTML special characters to prevent XSS attacks.
     *
     * @param input raw input string
     * @return HTML-escaped string
     */
    fun escapeHtml(input: String?): String =
        input
            ?.replace("&", "&amp;")
            ?.replace("<", "&lt;")
            ?.replace(">", "&gt;")
            ?.replace("\"", "&quot;")
            ?.replace("'", "&#39;")
            ?: ""

    /**
     * Creates a log message template.
     *
     * @param level log level
     * @param component component name
     * @param message log message
     * @return formatted log string
     */
    fun logMessage(
        level: String,
        component: String,
        message: String,
    ): String = "[${LocalDateTime.now()}] [$level] [$component] $message"
}

// ========================================================================
// Extension Functions for Enhanced String Operations
// ========================================================================

/**
 * Creates a safe SQL query from a template string.
 *
 * This extension function demonstrates how to create domain-specific
 * string operations in Kotlin.
 */
fun String.toSafeQuery(vararg params: Any): StringTemplates.SafeQueryBuilder {
    var parameterizedQuery = this
    params.forEach { _ ->
        val firstPlaceholder = parameterizedQuery.indexOf("\${")
        if (firstPlaceholder >= 0) {
            val endPlaceholder = parameterizedQuery.indexOf("}", firstPlaceholder)
            if (endPlaceholder >= 0) {
                parameterizedQuery =
                    parameterizedQuery.substring(0, firstPlaceholder) +
                    "?" +
                    parameterizedQuery.substring(endPlaceholder + 1)
            }
        }
    }
    return StringTemplates.SafeQueryBuilder(parameterizedQuery, params.toList())
}

/**
 * Wraps the string with a delimiter on both sides.
 */
fun String.wrapWith(delimiter: String): String = "$delimiter$this$delimiter"

/**
 * Truncates the string to maximum length with ellipsis.
 */
fun String.truncate(maxLength: Int): String =
    if (length <= maxLength) {
        this
    } else {
        take(maxLength - 3) + "..."
    }

/**
 * Escapes special characters for use in SQL LIKE patterns.
 */
fun String.escapeSqlLike(): String =
    this.replace("\\", "\\\\")
        .replace("%", "\\%")
        .replace("_", "\\_")

/**
 * Main function demonstrating all String Template features.
 */
fun main() {
    println("=== Kotlin String Templates Demo ===\n")

    // Demo 1: Basic templates
    println("--- Basic String Templates ---\n")
    println(StringTemplates.basicInterpolation("Alice", 30))
    println(StringTemplates.expressionInterpolation(5, 3))

    // Demo 2: Multi-line templates
    println("\n--- Multi-line Templates ---\n")
    println(StringTemplates.multiLineTemplate("Bob", "bob@example.com", true))

    // Demo 3: Formatted output
    println("\n--- Formatted Output ---\n")
    println(StringTemplates.formattedOutput("Widget", 5, 19.99))
    println("\nTable format:")
    println(StringTemplates.tableRow(1, "Alice Johnson", 75000.00))
    println(StringTemplates.tableRow(2, "Bob Smith", 82500.50))

    // Demo 4: Safe SQL Query Builder
    println("\n--- Safe SQL Query Builder ---\n")
    val query = StringTemplates.buildUserQuery("users", 18, "active")
    println(query.toDebugString())

    // Demo 5: Unsafe vs Safe comparison
    println("\n--- Unsafe vs Safe Queries ---\n")
    val maliciousInput = "'; DROP TABLE users; --"
    println("UNSAFE (vulnerable to injection):")
    println(StringTemplates.unsafeQuery(maliciousInput))
    println("\nSAFE (parameterized):")
    val safeQry = StringTemplates.safeQuery(maliciousInput)
    println("Query: ${safeQry.query}")
    println("Parameters: ${safeQry.parameters}")

    // Demo 6: HTML template
    println("\n--- HTML Template ---\n")
    println(
        StringTemplates.htmlTemplate(
            "My Page",
            "Welcome!",
            "This is <script>alert('xss')</script> content",
        ),
    )

    // Demo 7: Log message
    println("\n--- Log Message Template ---\n")
    println(StringTemplates.logMessage("INFO", "UserService", "User logged in successfully"))
    println(StringTemplates.logMessage("ERROR", "Database", "Connection timeout"))

    // Demo 8: Extension functions
    println("\n--- Extension Functions ---\n")
    println("Wrapped: ${"Hello".wrapWith("***")}")
    println("Truncated: ${"Hello, World!".truncate(8)}")
    println("SQL LIKE escape: ${"100% complete_task".escapeSqlLike()}")

    // Demo 9: Dollar sign escaping
    println("\n--- Dollar Sign in Templates ---\n")
    val price = 19.99
    println("Price: ${'$'}$price") // Escaping dollar sign
    println("Price: \$$price") // Alternative escaping

    // Demo 10: trimMargin example
    println("\n--- trimMargin Example ---\n")
    val margined =
        """
        |First line
        |  Second line (indented)
        |Third line
        """.trimMargin()
    println(margined)
}
