package io.github.sps23.interview.preparation.stringtemplates

/** Demonstrates Scala 3 String Interpolation, comparing with Java 21 String Templates.
  *
  * Scala 3 provides built-in string interpolation that is similar to Java 21's String Templates but
  * has been part of the language since Scala 2.10. This class showcases a SQL query builder using
  * safe interpolation patterns.
  *
  * Key Concepts:
  *   - s"..." interpolator - simple string interpolation
  *   - f"..." interpolator - printf-style formatting
  *   - raw"..." interpolator - no escape processing
  *   - Custom string interpolators
  *   - Safety benefits of parameterized queries
  */
object StringTemplates:

  // ========================================================================
  // Basic String Interpolation (s-interpolator)
  // ========================================================================

  /** Demonstrates basic s-interpolator usage.
    *
    * The s-interpolator performs simple string interpolation, embedding expression values directly
    * into the string.
    *
    * @param name
    *   the name to greet
    * @param age
    *   the age to display
    * @return
    *   a formatted greeting string
    */
  def basicInterpolation(name: String, age: Int): String =
    s"Hello, $name! You are $age years old."

  /** Demonstrates expressions within interpolation placeholders.
    *
    * @param x
    *   first operand
    * @param y
    *   second operand
    * @return
    *   string showing arithmetic operations
    */
  def expressionInterpolation(x: Int, y: Int): String =
    s"Sum: $x + $y = ${x + y}, Product: ${x * y}"

  /** Demonstrates multi-line string interpolation with stripMargin.
    *
    * @param name
    *   user name
    * @param email
    *   user email
    * @param active
    *   active status
    * @return
    *   formatted JSON string
    */
  def multiLineTemplate(name: String, email: String, active: Boolean): String =
    s"""{
       |    "name": "$name",
       |    "email": "$email",
       |    "active": $active
       |}""".stripMargin

  // ========================================================================
  // Printf-style Formatting (f-interpolator)
  // ========================================================================

  /** Demonstrates f-interpolator for formatted output.
    *
    * The f-interpolator combines string interpolation with printf-style format specifiers, allowing
    * precise control over number formatting, alignment, and padding.
    *
    * @param item
    *   item name
    * @param quantity
    *   quantity
    * @param unitPrice
    *   price per unit
    * @return
    *   formatted receipt line
    */
  def formattedOutput(item: String, quantity: Int, unitPrice: Double): String =
    val total = quantity * unitPrice
    f"Item: $item, Qty: $quantity, Price: $$$unitPrice%.2f, Total: $$$total%.2f"

  /** Creates a formatted table row using f-interpolator.
    *
    * @param id
    *   employee ID
    * @param name
    *   employee name
    * @param salary
    *   employee salary
    * @return
    *   formatted table row
    */
  def tableRow(id: Int, name: String, salary: Double): String =
    f"| $id%5d | $name%-20s | $$$salary%10.2f |"

  // ========================================================================
  // SQL Query Builder - Safe Parameter Interpolation
  // ========================================================================

  /** A safe SQL query builder that prevents SQL injection by using parameterized queries.
    *
    * This class demonstrates how to build domain-specific query builders that are safer than string
    * concatenation.
    *
    * @param query
    *   the parameterized SQL query
    * @param parameters
    *   the list of parameter values
    */
  final case class SafeQueryBuilder(
      query: String         = "",
      parameters: List[Any] = List.empty
  ):

    /** Sets the SELECT clause.
      *
      * @param columns
      *   columns to select
      * @return
      *   new builder with SELECT clause
      */
    def select(columns: String*): SafeQueryBuilder =
      copy(query = s"SELECT ${columns.mkString(", ")}")

    /** Sets the FROM clause.
      *
      * @param table
      *   table name
      * @return
      *   new builder with FROM clause
      */
    def from(table: String): SafeQueryBuilder =
      copy(query = s"$query FROM $table")

    /** Adds a WHERE condition with a parameterized value.
      *
      * @param column
      *   column name
      * @param operator
      *   comparison operator
      * @param value
      *   parameter value (safely parameterized)
      * @return
      *   new builder with WHERE condition
      */
    def where(column: String, operator: String, value: Any): SafeQueryBuilder =
      val prefix = if query.contains("WHERE") then " AND" else " WHERE"
      copy(
        query      = s"$query$prefix $column $operator ?",
        parameters = parameters :+ value
      )

    /** Adds an ORDER BY clause.
      *
      * @param column
      *   column to order by
      * @param direction
      *   ASC or DESC
      * @return
      *   new builder with ORDER BY clause
      */
    def orderBy(column: String, direction: String): SafeQueryBuilder =
      copy(query = s"$query ORDER BY $column $direction")

    /** Adds a LIMIT clause.
      *
      * @param limit
      *   maximum number of rows
      * @return
      *   new builder with LIMIT clause
      */
    def limit(limit: Int): SafeQueryBuilder =
      copy(
        query      = s"$query LIMIT ?",
        parameters = parameters :+ limit
      )

    /** Returns a debug representation showing query and parameters.
      *
      * @return
      *   debug string
      */
    def toDebugString: String =
      s"""Query: $query
         |Parameters: $parameters""".stripMargin

  /** Demonstrates building a safe SELECT query.
    *
    * @param tableName
    *   table to query
    * @param minAge
    *   minimum age filter
    * @param status
    *   status filter
    * @return
    *   the safe query builder with configured query
    */
  def buildUserQuery(tableName: String, minAge: Int, status: String): SafeQueryBuilder =
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

  /** Demonstrates UNSAFE string concatenation (vulnerable to SQL injection).
    *
    * WARNING: This is an example of what NOT to do. String concatenation with user input can lead
    * to SQL injection attacks.
    *
    * @param name
    *   user-provided name (potentially malicious)
    * @return
    *   UNSAFE query string
    */
  def unsafeQuery(name: String): String =
    // DANGEROUS: Never do this in production!
    s"SELECT * FROM users WHERE name = '$name'"

  /** Demonstrates safe parameterized query building.
    *
    * @param name
    *   user-provided name
    * @return
    *   safe query builder with parameterized value
    */
  def safeQuery(name: String): SafeQueryBuilder =
    SafeQueryBuilder().select("*").from("users").where("name", "=", name)

  // ========================================================================
  // Complex Template Examples
  // ========================================================================

  /** Creates an HTML template with interpolated values.
    *
    * @param title
    *   page title
    * @param heading
    *   main heading
    * @param content
    *   page content
    * @return
    *   HTML string
    */
  def htmlTemplate(title: String, heading: String, content: String): String =
    s"""<!DOCTYPE html>
       |<html>
       |<head>
       |    <title>${escapeHtml(title)}</title>
       |</head>
       |<body>
       |    <h1>${escapeHtml(heading)}</h1>
       |    <div class="content">
       |        ${escapeHtml(content)}
       |    </div>
       |</body>
       |</html>""".stripMargin

  /** Escapes HTML special characters to prevent XSS attacks.
    *
    * @param input
    *   raw input string
    * @return
    *   HTML-escaped string
    */
  def escapeHtml(input: String): String =
    Option(input)
      .map(
        _.replace("&", "&amp;")
          .replace("<", "&lt;")
          .replace(">", "&gt;")
          .replace("\"", "&quot;")
          .replace("'", "&#39;")
      )
      .getOrElse("")

  /** Creates a log message template.
    *
    * @param level
    *   log level
    * @param component
    *   component name
    * @param message
    *   log message
    * @return
    *   formatted log string
    */
  def logMessage(level: String, component: String, message: String): String =
    s"[${java.time.LocalDateTime.now()}] [$level] [$component] $message"

  // ========================================================================
  // Custom String Interpolator
  // ========================================================================

  /** Custom string interpolator for safe SQL query building.
    *
    * Demonstrates how to create domain-specific string interpolators in Scala.
    */
  extension (sc: StringContext)
    /** Safe SQL interpolator that collects parameters.
      *
      * @param args
      *   interpolated values
      * @return
      *   SafeQueryBuilder with parameterized query
      */
    def sql(args: Any*): SafeQueryBuilder =
      val parts   = sc.parts.iterator
      val builder = new StringBuilder(parts.next())

      args.foreach { arg =>
        builder.append("?")
        if parts.hasNext then builder.append(parts.next())
      }

      SafeQueryBuilder(builder.toString(), args.toList)

@main def runStringTemplates(): Unit =
  import StringTemplates.*

  println("=== Scala 3 String Interpolation Demo ===\n")

  // Demo 1: Basic s-interpolator
  println("--- Basic s-Interpolator ---\n")
  println(basicInterpolation("Alice", 30))
  println(expressionInterpolation(5, 3))

  // Demo 2: Multi-line templates
  println("\n--- Multi-line Templates ---\n")
  println(multiLineTemplate("Bob", "bob@example.com", true))

  // Demo 3: f-interpolator formatted output
  println("\n--- f-Interpolator Formatted Output ---\n")
  println(formattedOutput("Widget", 5, 19.99))
  println("\nTable format:")
  println(tableRow(1, "Alice Johnson", 75000.00))
  println(tableRow(2, "Bob Smith", 82500.50))

  // Demo 4: Safe SQL Query Builder
  println("\n--- Safe SQL Query Builder ---\n")
  val query = buildUserQuery("users", 18, "active")
  println(query.toDebugString)

  // Demo 5: Unsafe vs Safe comparison
  println("\n--- Unsafe vs Safe Queries ---\n")
  val maliciousInput = "'; DROP TABLE users; --"
  println("UNSAFE (vulnerable to injection):")
  println(unsafeQuery(maliciousInput))
  println("\nSAFE (parameterized):")
  val safeQry = safeQuery(maliciousInput)
  println(s"Query: ${safeQry.query}")
  println(s"Parameters: ${safeQry.parameters}")

  // Demo 6: HTML template
  println("\n--- HTML Template ---\n")
  println(htmlTemplate("My Page", "Welcome!", "This is <script>alert('xss')</script> content"))

  // Demo 7: Log message
  println("\n--- Log Message Template ---\n")
  println(logMessage("INFO", "UserService", "User logged in successfully"))
  println(logMessage("ERROR", "Database", "Connection timeout"))

  // Demo 8: Custom sql interpolator
  println("\n--- Custom SQL Interpolator ---\n")
  val tableName = "products"
  val minPrice  = 10.0
  val category  = "electronics"
  val sqlQuery  = sql"SELECT * FROM $tableName WHERE price > $minPrice AND category = $category"
  println(sqlQuery.toDebugString)

  // Demo 9: Raw interpolator
  println("\n--- Raw Interpolator ---\n")
  val path = raw"C:\Users\Alice\Documents"
  println(s"Raw path: $path")
