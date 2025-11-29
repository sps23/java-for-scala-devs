package io.github.sps23.interview.preparation.ffm

import java.lang.foreign.{
  Arena,
  FunctionDescriptor,
  Linker,
  MemorySegment,
  SymbolLookup,
  ValueLayout
}
import java.lang.invoke.MethodHandle
import scala.util.Using

/** Demonstrates Java 21's Foreign Function and Memory (FFM) API for calling native C library
  * functions in Scala 3.
  *
  * The FFM API provides a modern alternative to JNI for native code integration. This example
  * demonstrates:
  *   - Linker for creating method handles to native functions
  *   - FunctionDescriptor for describing native function signatures
  *   - SymbolLookup for finding native functions in libraries
  *   - Calling standard C library functions (strlen, abs, etc.)
  *
  * For Scala developers: This is similar to using JNA or JNI, but with a pure Java API that's
  * type-safe and doesn't require native code compilation. The functional style of Scala makes the
  * code more concise.
  *
  * Key FFM API concepts for function calls:
  *   - '''Linker''': Creates method handles for native functions following the platform's calling
  *     convention.
  *   - '''FunctionDescriptor''': Describes the signature (return type and parameter types) of a
  *     native function.
  *   - '''SymbolLookup''': Locates native functions by name in loaded libraries.
  *   - '''MethodHandle''': Java representation of a callable native function.
  *
  * @see
  *   NativeMemoryExample for memory management examples
  * @see
  *   ForeignFunctionExample.java for Java version
  */
object ForeignFunctionExample:

  // Native linker for the current platform (e.g., x86_64 Linux)
  private val linker: Linker = Linker.nativeLinker()

  // Symbol lookup for standard C library functions
  private val stdlib: SymbolLookup = linker.defaultLookup()

  /** Helper to find a symbol or throw an exception. */
  private def findSymbol(name: String): MemorySegment =
    stdlib
      .find(name)
      .orElseThrow(() => new RuntimeException(s"$name not found"))

  /** Demonstrates calling the C strlen function.
    *
    * C signature: `size_t strlen(const char *s)`
    */
  def demonstrateStrlen(): Unit =
    println("=== Calling C strlen() ===\n")

    // Find the strlen symbol and create function descriptor
    val strlenSymbol = findSymbol("strlen")
    val strlenDescriptor = FunctionDescriptor.of(
      ValueLayout.JAVA_LONG, // return type (size_t)
      ValueLayout.ADDRESS    // parameter: const char*
    )

    // Create a method handle for strlen
    val strlen: MethodHandle = linker.downcallHandle(strlenSymbol, strlenDescriptor)

    Using.resource(Arena.ofConfined()) { arena =>
      // Create a null-terminated string in native memory (UTF-8)
      val testString   = "Hello, FFM API from Scala!"
      val nativeString = arena.allocateUtf8String(testString)

      // Call strlen using invokeExact
      val length = strlen.invokeExact(nativeString).asInstanceOf[Long]

      println(s"""String: "$testString"""")
      println(s"Scala length: ${testString.length}")
      println(s"Native strlen result: $length")
    }

    println()

  /** Demonstrates calling the C abs function.
    *
    * C signature: `int abs(int n)`
    */
  def demonstrateAbs(): Unit =
    println("=== Calling C abs() ===\n")

    val absSymbol = findSymbol("abs")
    val absDescriptor = FunctionDescriptor.of(
      ValueLayout.JAVA_INT, // return type
      ValueLayout.JAVA_INT  // parameter
    )

    val abs: MethodHandle = linker.downcallHandle(absSymbol, absDescriptor)

    // Test with various values - functional style
    val testValues = List(-42, 0, 42, Int.MinValue + 1)

    testValues.foreach { value =>
      val result = abs.invokeExact(value).asInstanceOf[Int]
      println(s"abs($value) = $result")
    }

    println()

  /** Demonstrates calling the C sqrt function from math library.
    *
    * C signature: `double sqrt(double x)`
    */
  def demonstrateSqrt(): Unit =
    println("=== Calling C sqrt() ===\n")

    val sqrtSymbol = findSymbol("sqrt")
    val sqrtDescriptor = FunctionDescriptor.of(
      ValueLayout.JAVA_DOUBLE, // return type
      ValueLayout.JAVA_DOUBLE  // parameter
    )

    val sqrt: MethodHandle = linker.downcallHandle(sqrtSymbol, sqrtDescriptor)

    // Test with various values - functional style
    val testValues = List(4.0, 9.0, 16.0, 2.0, 100.0)

    testValues.foreach { value =>
      val result = sqrt.invokeExact(value).asInstanceOf[Double]
      println(f"sqrt($value%.1f) = $result%.6f")
    }

    println()

  /** Demonstrates calling the C time function.
    *
    * C signature: `time_t time(time_t *tloc)`
    */
  def demonstrateTime(): Unit =
    println("=== Calling C time() ===\n")

    val timeSymbol = findSymbol("time")
    val timeDescriptor = FunctionDescriptor.of(
      ValueLayout.JAVA_LONG, // return type (time_t)
      ValueLayout.ADDRESS    // parameter (time_t *tloc)
    )

    val time: MethodHandle = linker.downcallHandle(timeSymbol, timeDescriptor)

    // Call time(NULL) to get current time
    val timestamp = time.invokeExact(MemorySegment.NULL).asInstanceOf[Long]

    println(s"Current Unix timestamp: $timestamp")
    println(s"Scala System.currentTimeMillis()/1000: ${System.currentTimeMillis() / 1000}")

    println()

  /** Demonstrates calling C memcpy function.
    *
    * C signature: `void *memcpy(void *dest, const void *src, size_t n)`
    */
  def demonstrateMemcpy(): Unit =
    println("=== Calling C memcpy() ===\n")

    val memcpySymbol = findSymbol("memcpy")
    val memcpyDescriptor = FunctionDescriptor.of(
      ValueLayout.ADDRESS,  // return type
      ValueLayout.ADDRESS,  // dest
      ValueLayout.ADDRESS,  // src
      ValueLayout.JAVA_LONG // size_t n
    )

    val memcpy: MethodHandle = linker.downcallHandle(memcpySymbol, memcpyDescriptor)

    Using.resource(Arena.ofConfined()) { arena =>
      // Allocate source and destination buffers
      val src  = arena.allocate(32)
      val dest = arena.allocate(32)

      // Fill source with data - functional style
      (0 until 8).foreach(i => src.setAtIndex(ValueLayout.JAVA_INT, i, i * 10))

      val srcData = (0 until 8).map(i => src.getAtIndex(ValueLayout.JAVA_INT, i))
      println(s"Source data: [${srcData.mkString(", ")}]")

      // Copy 32 bytes (8 integers) - note: invokeExact requires exact types
      memcpy.invokeExact(dest, src, 32L)

      val destData = (0 until 8).map(i => dest.getAtIndex(ValueLayout.JAVA_INT, i))
      println(s"Copied data: [${destData.mkString(", ")}]")
    }

    println()

  /** Demonstrates the advantages of FFM API over JNI. */
  def demonstrateAdvantagesOverJNI(): Unit =
    println("=== Advantages over JNI ===\n")

    println("FFM API improvements over traditional JNI:")
    println()
    println("| Aspect              | JNI                  | FFM API            |")
    println("|---------------------|----------------------|--------------------|")
    println("| Native code         | Requires C code      | Pure Java/Scala    |")
    println("| Compilation         | javah + C compiler   | None required      |")
    println("| Memory safety       | Manual management    | Arena-based        |")
    println("| Type safety         | Weak                 | Strong             |")
    println("| Bounds checking     | None                 | Built-in           |")
    println("| Thread safety       | Manual               | Confined arenas    |")
    println("| Performance         | Good                 | Comparable         |")
    println("| Debugging           | Difficult            | Better tooling     |")
    println()
    println("Key benefits for Scala developers:")
    println("1. No native code to write - define signatures in Scala")
    println("2. Functional style with Using for resource management")
    println("3. Safer - bounds checking prevents buffer overflows")
    println("4. Method handles integrate well with Scala's FP style")
    println("5. Better lifecycle management with Arena + Using")
    println()

/** Main entry point for foreign function examples. */
@main def runForeignFunctionExample(): Unit =
  println("========================================")
  println("  FFM API - Foreign Function Calls")
  println("  Scala 3 with Java 21 FFM API")
  println("========================================\n")

  try
    ForeignFunctionExample.demonstrateStrlen()
    ForeignFunctionExample.demonstrateAbs()
    ForeignFunctionExample.demonstrateSqrt()
    ForeignFunctionExample.demonstrateTime()
    ForeignFunctionExample.demonstrateMemcpy()
    ForeignFunctionExample.demonstrateAdvantagesOverJNI()
  catch
    case e: Throwable =>
      System.err.println(s"Error calling native function: ${e.getMessage}")
      e.printStackTrace()

  println("========================================")
  println("  Key Takeaways:")
  println("  - Use Linker for native calling convention")
  println("  - FunctionDescriptor defines signatures")
  println("  - SymbolLookup finds native functions")
  println("  - MethodHandle invokes native code")
  println("  - Scala's functional style is natural fit")
  println("========================================")
