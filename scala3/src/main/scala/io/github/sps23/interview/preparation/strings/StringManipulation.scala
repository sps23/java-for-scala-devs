package io.github.sps23.interview.preparation.strings

/** Demonstrates string manipulation in Scala 3, comparing with Java's modern String APIs.
  *
  * Scala 3 provides idiomatic ways to process strings using:
  *   - String interpolation (s"...", f"...", raw"...")
  *   - Extension methods
  *   - Collection operations on strings
  *   - Multi-line strings with stripMargin
  */
object StringManipulation:

  // ========================================================================
  // Sample multi-line text for processing
  // ========================================================================

  /** Sample multi-line text using Scala's stripMargin for clean formatting. The pipe character
    * indicates where the margin should be stripped.
    */
  val sampleText: String =
    """
      |   Introduction to Java
      |
      |   Java is a high-level, class-based, object-oriented programming language.
      |
      |   Key Features:
      |       - Platform independence
      |       - Strong type system
      |       - Automatic memory management
      |
      |   Java was originally developed by James Gosling at Sun Microsystems.
      |""".stripMargin

  // ========================================================================
  // Idiomatic Scala 3 string processing
  // ========================================================================

  /** Process multi-line text: strip indentation, filter blank lines, and format output.
    *
    * Uses Scala's idiomatic approach with:
    *   - `linesIterator` for splitting lines
    *   - `trim` or `strip` for whitespace removal
    *   - Collection operations for filtering
    *
    * @param text
    *   the input multi-line text
    * @return
    *   processed text with blank lines removed
    */
  def processText(text: String): String =
    Option(text)
      .filter(_.trim.nonEmpty)
      .map(
        _.linesIterator
          .map(_.strip)
          .filter(_.nonEmpty)
          .mkString("\n")
      )
      .getOrElse("")

  /** Format output with line numbers using Scala idioms.
    *
    * Uses zipWithIndex for natural line numbering.
    *
    * @param text
    *   the input text
    * @return
    *   formatted text with line numbers
    */
  def formatWithLineNumbers(text: String): String =
    Option(text)
      .filter(_.trim.nonEmpty)
      .map(
        _.linesIterator.zipWithIndex
          .map { case (line, idx) => f"${idx + 1}%3d: $line" }
          .mkString("\n")
      )
      .getOrElse("")

  /** Adjust indentation of each line.
    *
    * @param text
    *   the input text
    * @param spaces
    *   number of spaces to add (positive) or remove (negative)
    * @return
    *   text with adjusted indentation
    */
  def adjustIndentation(text: String, spaces: Int): String =
    Option(text)
      .filter(_.trim.nonEmpty)
      .map { t =>
        if spaces >= 0 then
          val indent = " " * spaces
          t.linesIterator.map(line => indent + line).mkString("\n")
        else
          val removeCount = -spaces
          t.linesIterator
            .map { line =>
              val leadingSpaces = line.takeWhile(_ == ' ').length
              line.drop(leadingSpaces.min(removeCount))
            }
            .mkString("\n")
      }
      .getOrElse("")

  /** Transform text through a pipeline of operations.
    *
    * Demonstrates Scala's pipe operator and chained transformations.
    *
    * @param text
    *   the input text
    * @return
    *   transformed text
    */
  def transformExample(text: String): String =
    Option(text)
      .map(_.strip)
      .map(_.toUpperCase)
      .map(s => s"[$s]")
      .getOrElse("")

  /** Process text using a complete pipeline with bullet points.
    *
    * @param text
    *   the input text
    * @return
    *   processed and formatted text
    */
  def processCompletePipeline(text: String): String =
    Option(text)
      .filter(_.trim.nonEmpty)
      .map { t =>
        val processed = t.linesIterator
          .map(_.strip)
          .filter(_.nonEmpty)
          .map(line => s"• $line")
          .mkString("\n")

        s"Processed Content:\n$processed".linesIterator
          .map(line => s"  $line")
          .mkString("\n")
      }
      .getOrElse("")

  /** Extract non-blank lines as a list.
    *
    * @param text
    *   the input text
    * @return
    *   list of non-blank lines
    */
  def extractNonBlankLines(text: String): List[String] =
    Option(text)
      .filter(_.trim.nonEmpty)
      .map(_.linesIterator.map(_.strip).filter(_.nonEmpty).toList)
      .getOrElse(List.empty)

  /** Repeat a pattern with separator.
    *
    * @param pattern
    *   the pattern to repeat
    * @param count
    *   number of repetitions
    * @param separator
    *   separator between repetitions
    * @return
    *   repeated pattern string
    */
  def repeatPattern(pattern: String, count: Int, separator: String): String =
    if pattern == null || count <= 0 then ""
    else List.fill(count)(pattern).mkString(separator)

  // ========================================================================
  // String interpolation examples
  // ========================================================================

  /** Demonstrate Scala's string interpolation features.
    *
    * Scala provides three built-in interpolators:
    *   - s"..." - Simple string interpolation
    *   - f"..." - Printf-style formatting
    *   - raw"..." - No escape processing
    */
  def demonstrateInterpolation(): Unit =
    val name  = "Alice"
    val age   = 30
    val price = 1234.56

    // s-interpolator: simple string interpolation
    println(s"Name: $name, Age: $age")
    println(s"Next year: ${age + 1}")

    // f-interpolator: printf-style formatting
    println(f"Balance: $$$price%.2f")
    println(f"Padded: $age%05d")

    // raw-interpolator: no escape processing
    println(raw"Path: C:\Users\$name")

  // ========================================================================
  // Extension methods for enhanced string operations
  // ========================================================================

  /** Extension methods add functionality to String type. These are similar to Java's instance
    * methods but defined externally.
    */
  extension (s: String)

    /** Check if string is null or blank (similar to Java's isBlank but null-safe). */
    def isNullOrBlank: Boolean =
      s == null || s.isBlank

    /** Convert to Option, treating blank strings as None. */
    def toNonBlankOption: Option[String] =
      Option(s).filter(_.trim.nonEmpty)

    /** Apply multiple transformations in sequence. */
    def transformWith(fs: (String => String)*): String =
      fs.foldLeft(s)((acc, f) => f(acc))

    /** Wrap string in a delimiter. */
    def wrapWith(delimiter: String): String =
      s"$delimiter$s$delimiter"

    /** Truncate to max length with ellipsis. */
    def truncate(maxLength: Int): String =
      if s.length <= maxLength then s
      else s.take(maxLength - 3) + "..."

  // ========================================================================
  // Comparison with Java's isBlank() and isEmpty()
  // ========================================================================

  /** Describe blank/empty status of a string.
    *
    * @param text
    *   the text to check
    * @return
    *   description of status
    */
  def describeBlankStatus(text: String): String =
    if text == null then "null"
    else
      val isEmpty = if text.isEmpty then "empty" else "not empty"
      val isBlank = if text.isBlank then "blank" else "not blank"
      val display = text.replace("\n", "\\n").replace("\t", "\\t").replace(" ", "·")
      s"String '$display': $isEmpty, $isBlank"

  // ========================================================================
  // Multi-line string examples
  // ========================================================================

  /** Create formatted JSON using string interpolation and multi-line strings. */
  def createJson(name: String, email: String, active: Boolean): String =
    s"""{
       |  "name": "$name",
       |  "email": "$email",
       |  "active": $active
       |}""".stripMargin

  /** Create SQL query with parameters. */
  def createQuery(table: String, columns: List[String], whereClause: String): String =
    val cols = columns.mkString(", ")
    s"""SELECT $cols
       |FROM $table
       |WHERE $whereClause""".stripMargin

@main def runStringManipulation(): Unit =
  import StringManipulation.*
  import StringManipulation.{truncate, wrapWith, transformWith}

  println("=== Scala 3 String Manipulation Demo ===\n")

  // Demo 1: Process text
  println("--- Processing Multi-line Text ---\n")
  println("Processed Result:")
  println(processText(sampleText))

  // Demo 2: Line numbers
  println("\n--- Line Numbers Formatting ---\n")
  val simpleText = """First line
                     |Second line
                     |Third line""".stripMargin
  println("With line numbers:")
  println(formatWithLineNumbers(simpleText))

  // Demo 3: Indentation
  println("\n--- Indentation Control ---\n")
  val codeBlock = """def hello(): Unit =
                    |  println("Hello")""".stripMargin
  println("Original:")
  println(codeBlock)
  println("\nIndented by 4 spaces:")
  println(adjustIndentation(codeBlock, 4))

  // Demo 4: Transform chain
  println("\n--- Transform Example ---\n")
  println(transformExample("  hello world  "))

  // Demo 5: Complete pipeline
  println("\n--- Complete Processing Pipeline ---\n")
  println(processCompletePipeline(sampleText))

  // Demo 6: Blank status
  println("\n--- isBlank() vs isEmpty() ---\n")
  val testStrings: List[Option[String]] =
    List(Some(""), Some(" "), Some("  \t\n  "), Some("hello"), None)
  testStrings.foreach(s => println(describeBlankStatus(s.orNull)))

  // Demo 7: String interpolation
  println("\n--- String Interpolation ---\n")
  demonstrateInterpolation()

  // Demo 8: JSON creation
  println("\n--- JSON with Interpolation ---\n")
  println(createJson("Bob", "bob@example.com", true))

  // Demo 9: Extension methods
  println("\n--- Extension Methods ---\n")
  val text = "Hello, World!"
  println(s"Original: $text")
  println(s"Wrapped: ${text.wrapWith("***")}")
  println(s"Truncated: ${text.truncate(8)}")
  println(
    s"Transformed: ${"  hello  ".transformWith(_.strip, _.toUpperCase, s => s"[$s]")}"
  )

  // Demo 10: Repeat pattern
  println("\n--- Repeat Pattern ---\n")
  println(repeatPattern("*", 5, "-"))
