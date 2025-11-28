package io.github.sps23.interview.preparation.strings;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Demonstrates modern String API features introduced in Java 11-17+.
 *
 * <p>
 * This class showcases the evolution of string processing from verbose Java 8
 * approaches to modern fluent APIs available in Java 21.
 *
 * <h2>Key Features Demonstrated:</h2>
 * <ul>
 * <li>{@code String.isBlank()} - Check if string is empty or contains only
 * whitespace (Java 11)</li>
 * <li>{@code String.lines()} - Split string into lines as a Stream (Java
 * 11)</li>
 * <li>{@code String.strip()} - Remove leading/trailing whitespace
 * (Unicode-aware) (Java 11)</li>
 * <li>{@code String.indent()} - Adjust indentation of each line (Java 12)</li>
 * <li>{@code String.transform()} - Apply a function to a string (Java 12)</li>
 * <li>Text blocks - Multi-line string literals (Java 15)</li>
 * <li>{@code String.formatted()} - Instance method for formatting (Java
 * 15)</li>
 * </ul>
 */
public final class StringManipulation {

    private StringManipulation() {
        // Utility class
    }

    // ========================================================================
    // Sample multi-line text for processing
    // ========================================================================

    /**
     * Sample multi-line text using text blocks (Java 15+). Text blocks preserve the
     * formatting while allowing natural indentation in source code.
     */
    public static final String SAMPLE_TEXT = """
               Introduction to Java

               Java is a high-level, class-based, object-oriented programming language.

               Key Features:
                   - Platform independence
                   - Strong type system
                   - Automatic memory management

               Java was originally developed by James Gosling at Sun Microsystems.
            """;

    // ========================================================================
    // Java 8 Style - Verbose and manual
    // ========================================================================

    /**
     * Java 8 style: Process multi-line text using traditional methods.
     *
     * <p>
     * This approach requires:
     * <ul>
     * <li>Manual splitting by newline character</li>
     * <li>Manual trimming of each line</li>
     * <li>Manual filtering of empty lines</li>
     * <li>StringBuilder for output construction</li>
     * </ul>
     *
     * @param text
     *            the input multi-line text
     * @return processed text with blank lines removed and trimmed lines
     */
    public static String processJava8Style(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        String[] lines = text.split("\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty()) {
                result.append(trimmed).append("\n");
            }
        }

        // Remove trailing newline if present
        if (!result.isEmpty() && result.charAt(result.length() - 1) == '\n') {
            result.deleteCharAt(result.length() - 1);
        }

        return result.toString();
    }

    /**
     * Java 8 style: Format output with line numbers.
     *
     * @param text
     *            the input text
     * @return formatted text with line numbers
     */
    public static String formatWithLineNumbersJava8(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        String[] lines = text.split("\n");
        StringBuilder result = new StringBuilder();
        int lineNumber = 1;

        for (String line : lines) {
            result.append(String.format("%3d: %s%n", lineNumber++, line));
        }

        return result.toString().trim();
    }

    // ========================================================================
    // Modern Java 11+ Style - Fluent and expressive
    // ========================================================================

    /**
     * Modern Java 11+ style: Process multi-line text using fluent API.
     *
     * <p>
     * Key APIs used:
     * <ul>
     * <li>{@code lines()} - Splits into a stream of lines</li>
     * <li>{@code strip()} - Removes leading/trailing whitespace
     * (Unicode-aware)</li>
     * <li>{@code isBlank()} - Checks for empty or whitespace-only strings</li>
     * </ul>
     *
     * @param text
     *            the input multi-line text
     * @return processed text with blank lines removed and stripped lines
     */
    public static String processModernStyle(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return text.lines().map(String::strip).filter(line -> !line.isBlank())
                .collect(Collectors.joining("\n"));
    }

    /**
     * Modern Java 12+ style: Format output with line numbers using transform().
     *
     * <p>
     * The {@code transform()} method allows chaining arbitrary transformations on
     * strings, making code more fluent and readable.
     *
     * @param text
     *            the input text
     * @return formatted text with line numbers
     */
    public static String formatWithLineNumbersModern(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        int[] lineNumber = {1}; // Effectively final wrapper for use in lambda

        return text.lines().map(line -> "%3d: %s".formatted(lineNumber[0]++, line))
                .collect(Collectors.joining("\n"));
    }

    // ========================================================================
    // Advanced String Operations
    // ========================================================================

    /**
     * Demonstrates String.indent() for adjusting line indentation.
     *
     * <p>
     * {@code indent(n)} adds n spaces to the beginning of each line when n &gt; 0,
     * or removes up to |n| leading whitespace characters when n &lt; 0.
     *
     * @param text
     *            the input text
     * @param spaces
     *            number of spaces to indent (positive) or remove (negative)
     * @return text with adjusted indentation
     */
    public static String adjustIndentation(String text, int spaces) {
        if (text == null || text.isBlank()) {
            return "";
        }
        return text.indent(spaces).stripTrailing();
    }

    /**
     * Demonstrates String.transform() for fluent string processing.
     *
     * <p>
     * {@code transform()} applies a function and returns the result, enabling
     * fluent chaining of arbitrary string operations.
     *
     * @param text
     *            the input text
     * @return transformed text
     */
    public static String transformExample(String text) {
        if (text == null) {
            return "";
        }

        return text.transform(s -> s.strip()).transform(s -> s.toUpperCase())
                .transform(s -> "[%s]".formatted(s));
    }

    /**
     * Process text using a complete modern pipeline.
     *
     * <p>
     * This example combines multiple modern String APIs:
     * <ul>
     * <li>Text blocks for input</li>
     * <li>{@code lines()} for splitting</li>
     * <li>{@code strip()} for trimming</li>
     * <li>{@code isBlank()} for filtering</li>
     * <li>{@code formatted()} for output formatting</li>
     * <li>{@code transform()} for chaining</li>
     * </ul>
     *
     * @param text
     *            the input text
     * @return processed and formatted text
     */
    public static String processCompletePipeline(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        return text.lines().map(String::strip).filter(line -> !line.isBlank())
                .map(line -> "• %s".formatted(line)).collect(Collectors.joining("\n"))
                .transform(result -> "Processed Content:\n" + result)
                .transform(result -> result.indent(2).stripTrailing());
    }

    /**
     * Demonstrates String.repeat() for creating repeated patterns.
     *
     * @param pattern
     *            the pattern to repeat
     * @param count
     *            number of repetitions
     * @param separator
     *            separator between repetitions
     * @return repeated pattern string
     */
    public static String repeatPattern(String pattern, int count, String separator) {
        if (pattern == null || count <= 0) {
            return "";
        }

        return (pattern + separator).repeat(count).stripTrailing().transform(
                s -> s.endsWith(separator) ? s.substring(0, s.length() - separator.length()) : s);
    }

    /**
     * Extract non-blank lines as a list.
     *
     * @param text
     *            the input text
     * @return list of non-blank lines
     */
    public static List<String> extractNonBlankLines(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        return text.lines().map(String::strip).filter(line -> !line.isBlank()).toList();
    }

    // ========================================================================
    // Comparison Methods - isBlank() vs isEmpty()
    // ========================================================================

    /**
     * Demonstrates the difference between isEmpty() and isBlank().
     *
     * <ul>
     * <li>{@code isEmpty()} - Returns true only if length is 0</li>
     * <li>{@code isBlank()} - Returns true if empty or contains only whitespace
     * (Java 11+)</li>
     * </ul>
     *
     * @param text
     *            the text to check
     * @return description of blank/empty status
     */
    public static String describeBlankStatus(String text) {
        if (text == null) {
            return "null";
        }

        String isEmpty = text.isEmpty() ? "empty" : "not empty";
        String isBlank = text.isBlank() ? "blank" : "not blank";

        return "String '%s': %s, %s".formatted(
                text.replace("\n", "\\n").replace("\t", "\\t").replace(" ", "·"), isEmpty, isBlank);
    }

    // ========================================================================
    // strip() vs trim() comparison
    // ========================================================================

    /**
     * Demonstrates the difference between strip() and trim().
     *
     * <ul>
     * <li>{@code trim()} - Removes characters &lt;= U+0020 (space)</li>
     * <li>{@code strip()} - Removes all Unicode whitespace (Java 11+)</li>
     * </ul>
     *
     * @param text
     *            the text to process
     * @return comparison of trim() and strip() results
     */
    public static String compareStripVsTrim(String text) {
        if (text == null) {
            return "null input";
        }

        String trimmed = text.trim();
        String stripped = text.strip();

        return """
                Original: '%s'
                trim():   '%s'
                strip():  '%s'
                Same result: %s""".formatted(text.replace("\n", "\\n"),
                trimmed.replace("\n", "\\n"), stripped.replace("\n", "\\n"),
                trimmed.equals(stripped));
    }

    /**
     * Main method demonstrating all string manipulation features.
     *
     * @param args
     *            command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=== Modern String API Demo (Java 11-17+) ===\n");

        // Demo 1: Process text - Java 8 vs Modern
        System.out.println("--- Processing Multi-line Text ---\n");
        System.out.println("Java 8 Style Result:");
        System.out.println(processJava8Style(SAMPLE_TEXT));
        System.out.println("\nModern Style Result:");
        System.out.println(processModernStyle(SAMPLE_TEXT));

        // Demo 2: Line numbers
        System.out.println("\n--- Line Numbers Formatting ---\n");
        String simpleText = """
                First line
                Second line
                Third line
                """;
        System.out.println("With line numbers:");
        System.out.println(formatWithLineNumbersModern(simpleText));

        // Demo 3: Indentation
        System.out.println("\n--- Indentation Control ---\n");
        String codeBlock = """
                public void hello() {
                    System.out.println("Hello");
                }
                """;
        System.out.println("Original:");
        System.out.println(codeBlock);
        System.out.println("Indented by 4 spaces:");
        System.out.println(adjustIndentation(codeBlock, 4));

        // Demo 4: Transform chain
        System.out.println("\n--- Transform Example ---\n");
        System.out.println(transformExample("  hello world  "));

        // Demo 5: Complete pipeline
        System.out.println("\n--- Complete Processing Pipeline ---\n");
        System.out.println(processCompletePipeline(SAMPLE_TEXT));

        // Demo 6: isBlank() vs isEmpty()
        System.out.println("\n--- isBlank() vs isEmpty() ---\n");
        String[] testStrings = {"", " ", "  \t\n  ", "hello", null};
        for (String s : testStrings) {
            System.out.println(describeBlankStatus(s));
        }

        // Demo 7: strip() vs trim()
        System.out.println("\n--- strip() vs trim() ---\n");
        // Unicode non-breaking space (U+00A0)
        String withUnicodeSpace = "\u00A0 hello \u00A0";
        System.out.println(compareStripVsTrim(withUnicodeSpace));

        // Demo 8: String.formatted()
        System.out.println("\n--- String.formatted() ---\n");
        String template = "Name: %s, Age: %d, Balance: $%.2f";
        System.out.println(template.formatted("Alice", 30, 1234.56));

        // Demo 9: Text blocks with formatted
        System.out.println("\n--- Text Blocks with formatted() ---\n");
        String jsonTemplate = """
                {
                    "name": "%s",
                    "email": "%s",
                    "active": %b
                }
                """;
        System.out.println(jsonTemplate.formatted("Bob", "bob@example.com", true));
    }
}
