package io.github.sps23.interview.preparation.strings

/**
 * Demonstrates string manipulation in Kotlin, comparing with Java's modern String APIs.
 *
 * Kotlin provides idiomatic ways to process strings using:
 * - String templates ($variable, ${expression})
 * - Extension functions
 * - Collection operations on strings
 * - Multi-line strings (trimIndent, trimMargin)
 */
object StringManipulation {
    // ========================================================================
    // Sample multi-line text for processing
    // ========================================================================

    /**
     * Sample multi-line text using Kotlin's trimIndent for clean formatting.
     * trimIndent() detects and removes common indentation from all lines.
     */
    val sampleText =
        """
        Introduction to Java

        Java is a high-level, class-based, object-oriented programming language.

        Key Features:
            - Platform independence
            - Strong type system
            - Automatic memory management

        Java was originally developed by James Gosling at Sun Microsystems.
        """.trimIndent()

    // ========================================================================
    // Idiomatic Kotlin string processing
    // ========================================================================

    /**
     * Process multi-line text: strip indentation, filter blank lines, and format output.
     *
     * Uses Kotlin's idiomatic approach with:
     * - `lines()` for splitting lines
     * - `trim()` or extension functions for whitespace removal
     * - Collection operations for filtering
     *
     * @param text the input multi-line text
     * @return processed text with blank lines removed
     */
    fun processText(text: String?): String {
        if (text.isNullOrBlank()) return ""

        return text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n")
    }

    /**
     * Format output with line numbers using Kotlin idioms.
     *
     * Uses mapIndexed for natural line numbering.
     *
     * @param text the input text
     * @return formatted text with line numbers
     */
    fun formatWithLineNumbers(text: String?): String {
        if (text.isNullOrBlank()) return ""

        return text.lines()
            .mapIndexed { index, line -> "%3d: %s".format(index + 1, line) }
            .joinToString("\n")
    }

    /**
     * Adjust indentation of each line.
     *
     * @param text the input text
     * @param spaces number of spaces to add (positive) or remove (negative)
     * @return text with adjusted indentation
     */
    fun adjustIndentation(
        text: String?,
        spaces: Int,
    ): String {
        if (text.isNullOrBlank()) return ""

        return if (spaces >= 0) {
            val indent = " ".repeat(spaces)
            text.lines().joinToString("\n") { indent + it }
        } else {
            val removeCount = -spaces
            text.lines().joinToString("\n") { line ->
                val leadingSpaces = line.takeWhile { it == ' ' }.length
                line.drop(minOf(leadingSpaces, removeCount))
            }
        }
    }

    /**
     * Transform text through a pipeline of operations.
     *
     * Demonstrates Kotlin's let, run, and chained transformations.
     *
     * @param text the input text
     * @return transformed text
     */
    fun transformExample(text: String?): String =
        text?.trim()
            ?.uppercase()
            ?.let { "[$it]" }
            ?: ""

    /**
     * Process text using a complete pipeline with bullet points.
     *
     * @param text the input text
     * @return processed and formatted text
     */
    fun processCompletePipeline(text: String?): String {
        if (text.isNullOrBlank()) return ""

        val processed =
            text.lines()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .joinToString("\n") { "• $it" }

        return "Processed Content:\n$processed"
            .lines()
            .joinToString("\n") { "  $it" }
    }

    /**
     * Extract non-blank lines as a list.
     *
     * @param text the input text
     * @return list of non-blank lines
     */
    fun extractNonBlankLines(text: String?): List<String> {
        if (text.isNullOrBlank()) return emptyList()

        return text.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    /**
     * Repeat a pattern with separator.
     *
     * @param pattern the pattern to repeat
     * @param count number of repetitions
     * @param separator separator between repetitions
     * @return repeated pattern string
     */
    fun repeatPattern(
        pattern: String?,
        count: Int,
        separator: String,
    ): String {
        if (pattern == null || count <= 0) return ""
        return List(count) { pattern }.joinToString(separator)
    }

    // ========================================================================
    // String template examples
    // ========================================================================

    /**
     * Demonstrate Kotlin's string template features.
     *
     * Kotlin provides string templates with:
     * - Simple references: $variable
     * - Expressions: ${expression}
     * - Format specifiers with format()
     */
    fun demonstrateTemplates() {
        val name = "Alice"
        val age = 30
        val price = 1234.56

        // Simple templates
        println("Name: $name, Age: $age")
        println("Next year: ${age + 1}")

        // Formatted output
        println("Balance: $%.2f".format(price))
        println("Padded: %05d".format(age))

        // Raw string with dollar sign (escape with ${'$'})
        println("Price: ${'$'}$price")
    }

    // ========================================================================
    // Comparison with Java's isBlank() and isEmpty()
    // ========================================================================

    /**
     * Describe blank/empty status of a string.
     *
     * Kotlin provides:
     * - isEmpty() - true if length is 0
     * - isBlank() - true if empty or only whitespace
     * - isNullOrEmpty() - null-safe empty check
     * - isNullOrBlank() - null-safe blank check
     *
     * @param text the text to check
     * @return description of status
     */
    fun describeBlankStatus(text: String?): String {
        if (text == null) return "null"

        val isEmptyStr = if (text.isEmpty()) "empty" else "not empty"
        val isBlankStr = if (text.isBlank()) "blank" else "not blank"
        val display =
            text.replace("\n", "\\n")
                .replace("\t", "\\t")
                .replace(" ", "·")
        return "String '$display': $isEmptyStr, $isBlankStr"
    }

    // ========================================================================
    // Multi-line string examples
    // ========================================================================

    /**
     * Create formatted JSON using string templates and multi-line strings.
     */
    fun createJson(
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

    /**
     * Create SQL query with parameters.
     */
    fun createQuery(
        table: String,
        columns: List<String>,
        whereClause: String,
    ): String {
        val cols = columns.joinToString(", ")
        return """
            SELECT $cols
            FROM $table
            WHERE $whereClause
            """.trimIndent()
    }

    /**
     * Demonstrates trimMargin with custom margin prefix.
     */
    fun trimMarginExample(): String =
        """
        |First line
        |Second line
        |  Indented third line
        """.trimMargin()
}

// ========================================================================
// Extension functions for enhanced string operations
// ========================================================================

/**
 * Check if string is null or blank.
 * Note: Kotlin already provides isNullOrBlank(), this is for demonstration.
 */
fun String?.isNullOrBlankCustom(): Boolean = this == null || this.isBlank()

/**
 * Convert to non-blank value or null.
 */
fun String?.toNonBlankOrNull(): String? = this?.takeIf { it.isNotBlank() }

/**
 * Apply multiple transformations in sequence.
 */
fun String.transformWith(vararg transformations: (String) -> String): String = transformations.fold(this) { acc, f -> f(acc) }

/**
 * Wrap string in a delimiter.
 */
fun String.wrapWith(delimiter: String): String = "$delimiter$this$delimiter"

/**
 * Truncate to max length with ellipsis.
 */
fun String.truncate(maxLength: Int): String =
    if (length <= maxLength) {
        this
    } else {
        take(maxLength - 3) + "..."
    }

/**
 * Convert to title case.
 */
fun String.toTitleCase(): String =
    split(" ")
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { it.uppercase() }
        }

// ========================================================================
// Main demonstration
// ========================================================================

fun main() {
    println("=== Kotlin String Manipulation Demo ===\n")

    // Demo 1: Process text
    println("--- Processing Multi-line Text ---\n")
    println("Processed Result:")
    println(StringManipulation.processText(StringManipulation.sampleText))

    // Demo 2: Line numbers
    println("\n--- Line Numbers Formatting ---\n")
    val simpleText =
        """
        First line
        Second line
        Third line
        """.trimIndent()
    println("With line numbers:")
    println(StringManipulation.formatWithLineNumbers(simpleText))

    // Demo 3: Indentation
    println("\n--- Indentation Control ---\n")
    val codeBlock =
        """
        fun hello() {
            println("Hello")
        }
        """.trimIndent()
    println("Original:")
    println(codeBlock)
    println("\nIndented by 4 spaces:")
    println(StringManipulation.adjustIndentation(codeBlock, 4))

    // Demo 4: Transform chain
    println("\n--- Transform Example ---\n")
    println(StringManipulation.transformExample("  hello world  "))

    // Demo 5: Complete pipeline
    println("\n--- Complete Processing Pipeline ---\n")
    println(StringManipulation.processCompletePipeline(StringManipulation.sampleText))

    // Demo 6: Blank status
    println("\n--- isBlank() vs isEmpty() ---\n")
    val testStrings: List<String?> = listOf("", " ", "  \t\n  ", "hello", null)
    testStrings.forEach { println(StringManipulation.describeBlankStatus(it)) }

    // Demo 7: String templates
    println("\n--- String Templates ---\n")
    StringManipulation.demonstrateTemplates()

    // Demo 8: JSON creation
    println("\n--- JSON with Templates ---\n")
    println(StringManipulation.createJson("Bob", "bob@example.com", true))

    // Demo 9: Extension functions
    println("\n--- Extension Functions ---\n")
    val text = "Hello, World!"
    println("Original: $text")
    println("Wrapped: ${text.wrapWith("***")}")
    println("Truncated: ${text.truncate(8)}")
    println("Title Case: ${"hello world".toTitleCase()}")
    println(
        "Transformed: ${
            "  hello  ".transformWith(
                { it.trim() },
                { it.uppercase() },
                { "[$it]" },
            )
        }",
    )

    // Demo 10: Repeat pattern
    println("\n--- Repeat Pattern ---\n")
    println(StringManipulation.repeatPattern("*", 5, "-"))

    // Demo 11: trimMargin
    println("\n--- trimMargin Example ---\n")
    println(StringManipulation.trimMarginExample())
}
