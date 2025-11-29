package io.github.sps23.interview.preparation.stringtemplates;

import static java.lang.StringTemplate.STR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Demonstrates Java 21 String Templates (Preview Feature).
 *
 * <p>
 * String templates provide a safer and more expressive way to create strings
 * with embedded expressions, compared to string concatenation or format
 * methods. This class showcases a SQL query builder that safely handles
 * parameter interpolation.
 *
 * <h2>Key Concepts:</h2>
 * <ul>
 * <li>STR template processor - standard string interpolation</li>
 * <li>FMT template processor - printf-style formatting</li>
 * <li>Custom template processors - domain-specific string handling</li>
 * <li>Safety benefits over string concatenation</li>
 * </ul>
 *
 * <p>
 * Note: String Templates are a preview feature in Java 21 and require
 * --enable-preview flag.
 */
public final class StringTemplates {

    private StringTemplates() {
        // Utility class
    }

    // ========================================================================
    // STR Template Processor - Basic String Interpolation
    // ========================================================================

    /**
     * Demonstrates basic STR template processor usage.
     *
     * <p>
     * The STR processor performs simple string interpolation, embedding
     * expression values directly into the string.
     *
     * @param name
     *            the name to greet
     * @param age
     *            the age to display
     * @return a formatted greeting string
     */
    public static String basicInterpolation(String name, int age) {
        return STR."Hello, \{name}! You are \{age} years old.";
    }

    /**
     * Demonstrates expressions within template placeholders.
     *
     * @param x
     *            first operand
     * @param y
     *            second operand
     * @return string showing arithmetic operations
     */
    public static String expressionInterpolation(int x, int y) {
        return STR."Sum: \{x} + \{y} = \{x + y}, Product: \{x * y}";
    }

    /**
     * Demonstrates multi-line string templates.
     *
     * @param name
     *            user name
     * @param email
     *            user email
     * @param active
     *            active status
     * @return formatted JSON string
     */
    public static String multiLineTemplate(String name, String email, boolean active) {
        return STR."""
                {
                    "name": "\{name}",
                    "email": "\{email}",
                    "active": \{active}
                }
                """;
    }

    // ========================================================================
    // FMT Template Processor - Printf-style Formatting
    // ========================================================================

    /**
     * Demonstrates FMT template processor for formatted output.
     *
     * <p>
     * FMT combines string interpolation with printf-style format specifiers,
     * allowing precise control over number formatting, alignment, and padding.
     *
     * @param item
     *            item name
     * @param quantity
     *            quantity
     * @param unitPrice
     *            price per unit
     * @return formatted receipt line
     */
    public static String formattedOutput(String item, int quantity, double unitPrice) {
        double total = quantity * unitPrice;
        return STR."Item: \{item}, Qty: \{quantity}, Price: $\{String.format("%.2f", unitPrice)}, Total: $\{String.format("%.2f", total)}";
    }

    /**
     * Creates a formatted table row.
     *
     * @param id
     *            employee ID
     * @param name
     *            employee name
     * @param salary
     *            employee salary
     * @return formatted table row
     */
    public static String tableRow(int id, String name, double salary) {
        return STR."| \{String.format("%5d", id)} | \{String.format("%-20s", name)} | $\{String.format("%10.2f", salary)} |";
    }

    // ========================================================================
    // SQL Query Builder - Safe Parameter Interpolation
    // ========================================================================

    /**
     * A safe SQL query builder that prevents SQL injection by using parameterized
     * queries.
     *
     * <p>
     * This class demonstrates how string templates can be used to create
     * domain-specific builders that are safer than string concatenation.
     */
    public static final class SafeQueryBuilder {

        private final StringBuilder query;
        private final List<Object> parameters;
        private boolean hasWhereClause;

        /**
         * Creates a new safe query builder.
         */
        public SafeQueryBuilder() {
            this.query = new StringBuilder();
            this.parameters = new ArrayList<>();
            this.hasWhereClause = false;
        }

        /**
         * Sets the SELECT clause.
         *
         * @param columns
         *            columns to select
         * @return this builder
         */
        public SafeQueryBuilder select(String... columns) {
            query.append("SELECT ");
            query.append(String.join(", ", columns));
            return this;
        }

        /**
         * Sets the FROM clause.
         *
         * @param table
         *            table name
         * @return this builder
         */
        public SafeQueryBuilder from(String table) {
            query.append(" FROM ").append(table);
            return this;
        }

        /**
         * Adds a WHERE condition with a parameterized value.
         *
         * @param column
         *            column name
         * @param operator
         *            comparison operator
         * @param value
         *            parameter value (safely parameterized)
         * @return this builder
         */
        public SafeQueryBuilder where(String column, String operator, Object value) {
            if (!hasWhereClause) {
                query.append(" WHERE ");
                hasWhereClause = true;
            } else {
                query.append(" AND ");
            }
            query.append(column).append(" ").append(operator).append(" ?");
            parameters.add(value);
            return this;
        }

        /**
         * Adds an ORDER BY clause.
         *
         * @param column
         *            column to order by
         * @param direction
         *            ASC or DESC
         * @return this builder
         */
        public SafeQueryBuilder orderBy(String column, String direction) {
            query.append(" ORDER BY ").append(column).append(" ").append(direction);
            return this;
        }

        /**
         * Adds a LIMIT clause.
         *
         * @param limit
         *            maximum number of rows
         * @return this builder
         */
        public SafeQueryBuilder limit(int limit) {
            query.append(" LIMIT ?");
            parameters.add(limit);
            return this;
        }

        /**
         * Returns the parameterized query string.
         *
         * @return the SQL query with ? placeholders
         */
        public String getQuery() {
            return query.toString();
        }

        /**
         * Returns the parameter values in order.
         *
         * @return unmodifiable list of parameters
         */
        public List<Object> getParameters() {
            return Collections.unmodifiableList(parameters);
        }

        /**
         * Returns a debug representation showing query and parameters.
         *
         * @return debug string
         */
        public String toDebugString() {
            return STR."""
                    Query: \{query}
                    Parameters: \{parameters}
                    """;
        }
    }

    /**
     * Demonstrates building a safe SELECT query.
     *
     * @param tableName
     *            table to query
     * @param minAge
     *            minimum age filter
     * @param status
     *            status filter
     * @return the safe query builder with configured query
     */
    public static SafeQueryBuilder buildUserQuery(String tableName, int minAge, String status) {
        return new SafeQueryBuilder().select("id", "name", "email", "age").from(tableName)
                .where("age", ">=", minAge).where("status", "=", status).orderBy("name", "ASC")
                .limit(100);
    }

    // ========================================================================
    // Unsafe vs Safe Comparison
    // ========================================================================

    /**
     * Demonstrates UNSAFE string concatenation (vulnerable to SQL injection).
     *
     * <p>
     * WARNING: This is an example of what NOT to do. String concatenation with user
     * input can lead to SQL injection attacks.
     *
     * @param name
     *            user-provided name (potentially malicious)
     * @return UNSAFE query string
     */
    public static String unsafeQuery(String name) {
        // DANGEROUS: Never do this in production!
        return "SELECT * FROM users WHERE name = '" + name + "'";
    }

    /**
     * Demonstrates safe parameterized query building.
     *
     * @param name
     *            user-provided name
     * @return safe query builder with parameterized value
     */
    public static SafeQueryBuilder safeQuery(String name) {
        return new SafeQueryBuilder().select("*").from("users").where("name", "=", name);
    }

    // ========================================================================
    // Complex Template Examples
    // ========================================================================

    /**
     * Creates an HTML template with interpolated values.
     *
     * @param title
     *            page title
     * @param heading
     *            main heading
     * @param content
     *            page content
     * @return HTML string
     */
    public static String htmlTemplate(String title, String heading, String content) {
        return STR."""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>\{escapeHtml(title)}</title>
                </head>
                <body>
                    <h1>\{escapeHtml(heading)}</h1>
                    <div class="content">
                        \{escapeHtml(content)}
                    </div>
                </body>
                </html>
                """;
    }

    /**
     * Escapes HTML special characters to prevent XSS attacks.
     *
     * @param input
     *            raw input string
     * @return HTML-escaped string
     */
    public static String escapeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    /**
     * Creates a log message template.
     *
     * @param level
     *            log level
     * @param component
     *            component name
     * @param message
     *            log message
     * @return formatted log string
     */
    public static String logMessage(String level, String component, String message) {
        return STR."[\{java.time.LocalDateTime.now()}] [\{level}] [\{component}] \{message}";
    }

    /**
     * Main method demonstrating all String Template features.
     *
     * @param args
     *            command-line arguments (not used)
     */
    public static void main(String[] args) {
        System.out.println("=== Java 21 String Templates Demo ===\n");

        // Demo 1: Basic STR interpolation
        System.out.println("--- Basic STR Interpolation ---\n");
        System.out.println(basicInterpolation("Alice", 30));
        System.out.println(expressionInterpolation(5, 3));

        // Demo 2: Multi-line templates
        System.out.println("\n--- Multi-line Templates ---\n");
        System.out.println(multiLineTemplate("Bob", "bob@example.com", true));

        // Demo 3: Formatted output
        System.out.println("--- Formatted Output ---\n");
        System.out.println(formattedOutput("Widget", 5, 19.99));
        System.out.println("\nTable format:");
        System.out.println(tableRow(1, "Alice Johnson", 75000.00));
        System.out.println(tableRow(2, "Bob Smith", 82500.50));

        // Demo 4: Safe SQL Query Builder
        System.out.println("\n--- Safe SQL Query Builder ---\n");
        SafeQueryBuilder query = buildUserQuery("users", 18, "active");
        System.out.println(query.toDebugString());

        // Demo 5: Unsafe vs Safe comparison
        System.out.println("--- Unsafe vs Safe Queries ---\n");
        String maliciousInput = "'; DROP TABLE users; --";
        System.out.println("UNSAFE (vulnerable to injection):");
        System.out.println(unsafeQuery(maliciousInput));
        System.out.println("\nSAFE (parameterized):");
        SafeQueryBuilder safeQry = safeQuery(maliciousInput);
        System.out.println(STR."Query: \{safeQry.getQuery()}");
        System.out.println(STR."Parameters: \{safeQry.getParameters()}");

        // Demo 6: HTML template
        System.out.println("\n--- HTML Template ---\n");
        System.out.println(
                htmlTemplate("My Page", "Welcome!", "This is <script>alert('xss')</script> content"));

        // Demo 7: Log message
        System.out.println("\n--- Log Message Template ---\n");
        System.out.println(logMessage("INFO", "UserService", "User logged in successfully"));
        System.out.println(logMessage("ERROR", "Database", "Connection timeout"));
    }
}
