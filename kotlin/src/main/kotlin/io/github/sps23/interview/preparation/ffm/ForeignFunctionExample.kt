package io.github.sps23.interview.preparation.ffm

import java.lang.foreign.Arena
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.MemorySegment
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandle

/**
 * Demonstrates Java 21's Foreign Function and Memory (FFM) API for calling
 * native C library functions in Kotlin.
 *
 * The FFM API provides a modern alternative to JNI for native code integration.
 * This example demonstrates:
 * - Linker for creating method handles to native functions
 * - FunctionDescriptor for describing native function signatures
 * - SymbolLookup for finding native functions in libraries
 * - Calling standard C library functions (strlen, abs, etc.)
 *
 * For Kotlin developers: This is similar to using JNA or JNI, but with a pure
 * Java API that's type-safe and doesn't require native code compilation.
 * Kotlin's extension functions and null-safety make the code more idiomatic.
 *
 * Key FFM API concepts for function calls:
 * - **Linker**: Creates method handles for native functions following the
 *   platform's calling convention.
 * - **FunctionDescriptor**: Describes the signature (return type and parameter
 *   types) of a native function.
 * - **SymbolLookup**: Locates native functions by name in loaded libraries.
 * - **MethodHandle**: Java representation of a callable native function.
 *
 * @see NativeMemoryExample for memory management examples
 * @see ForeignFunctionExample.java for Java version
 */
object ForeignFunctionExample {
    // Native linker for the current platform (e.g., x86_64 Linux)
    private val linker: Linker = Linker.nativeLinker()

    // Symbol lookup for standard C library functions
    private val stdlib: SymbolLookup = linker.defaultLookup()

    /**
     * Helper extension function to find a symbol or throw an exception.
     */
    private fun SymbolLookup.findOrThrow(name: String): MemorySegment = find(name).orElseThrow { RuntimeException("$name not found") }

    /**
     * Demonstrates calling the C strlen function.
     *
     * C signature: `size_t strlen(const char *s)`
     */
    fun demonstrateStrlen() {
        println("=== Calling C strlen() ===\n")

        // Find the strlen symbol and create function descriptor
        val strlenSymbol = stdlib.findOrThrow("strlen")
        // Function signature: size_t strlen(const char*)
        val strlenDescriptor =
            FunctionDescriptor.of(
                ValueLayout.JAVA_LONG,
                ValueLayout.ADDRESS,
            )

        // Create a method handle for strlen
        val strlen: MethodHandle = linker.downcallHandle(strlenSymbol, strlenDescriptor)

        Arena.ofConfined().use { arena ->
            // Create a null-terminated string in native memory (UTF-8)
            val testString = "Hello, FFM API from Kotlin!"
            val nativeString = arena.allocateUtf8String(testString)

            // Call strlen using invokeExact
            val length = strlen.invokeExact(nativeString) as Long

            println("String: \"$testString\"")
            println("Kotlin length: ${testString.length}")
            println("Native strlen result: $length")
        }

        println()
    }

    /**
     * Demonstrates calling the C abs function.
     *
     * C signature: `int abs(int n)`
     */
    fun demonstrateAbs() {
        println("=== Calling C abs() ===\n")

        val absSymbol = stdlib.findOrThrow("abs")
        // Function signature: int abs(int)
        val absDescriptor =
            FunctionDescriptor.of(
                ValueLayout.JAVA_INT,
                ValueLayout.JAVA_INT,
            )

        val abs: MethodHandle = linker.downcallHandle(absSymbol, absDescriptor)

        // Test with various values - idiomatic Kotlin style
        val testValues = listOf(-42, 0, 42, Int.MIN_VALUE + 1)

        testValues.forEach { value ->
            val result = abs.invokeExact(value) as Int
            println("abs($value) = $result")
        }

        println()
    }

    /**
     * Demonstrates calling the C sqrt function from math library.
     *
     * C signature: `double sqrt(double x)`
     */
    fun demonstrateSqrt() {
        println("=== Calling C sqrt() ===\n")

        val sqrtSymbol = stdlib.findOrThrow("sqrt")
        // Function signature: double sqrt(double)
        val sqrtDescriptor =
            FunctionDescriptor.of(
                ValueLayout.JAVA_DOUBLE,
                ValueLayout.JAVA_DOUBLE,
            )

        val sqrt: MethodHandle = linker.downcallHandle(sqrtSymbol, sqrtDescriptor)

        // Test with various values - idiomatic Kotlin style
        val testValues = listOf(4.0, 9.0, 16.0, 2.0, 100.0)

        testValues.forEach { value ->
            val result = sqrt.invokeExact(value) as Double
            println("sqrt(%.1f) = %.6f".format(value, result))
        }

        println()
    }

    /**
     * Demonstrates calling the C time function.
     *
     * C signature: `time_t time(time_t *tloc)`
     */
    fun demonstrateTime() {
        println("=== Calling C time() ===\n")

        val timeSymbol = stdlib.findOrThrow("time")
        // Function signature: time_t time(time_t*)
        val timeDescriptor =
            FunctionDescriptor.of(
                ValueLayout.JAVA_LONG,
                ValueLayout.ADDRESS,
            )

        val time: MethodHandle = linker.downcallHandle(timeSymbol, timeDescriptor)

        // Call time(NULL) to get current time
        val timestamp = time.invokeExact(MemorySegment.NULL) as Long

        println("Current Unix timestamp: $timestamp")
        println("Kotlin System.currentTimeMillis()/1000: ${System.currentTimeMillis() / 1000}")

        println()
    }

    /**
     * Demonstrates calling C memcpy function.
     *
     * C signature: `void *memcpy(void *dest, const void *src, size_t n)`
     */
    fun demonstrateMemcpy() {
        println("=== Calling C memcpy() ===\n")

        val memcpySymbol = stdlib.findOrThrow("memcpy")
        // Function signature: void* memcpy(void* dest, const void* src, size_t n)
        val memcpyDescriptor =
            FunctionDescriptor.of(
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS,
                ValueLayout.ADDRESS,
                ValueLayout.JAVA_LONG,
            )

        val memcpy: MethodHandle = linker.downcallHandle(memcpySymbol, memcpyDescriptor)

        Arena.ofConfined().use { arena ->
            // Allocate source and destination buffers
            val src = arena.allocate(32)
            val dest = arena.allocate(32)

            // Fill source with data - idiomatic Kotlin style
            (0 until 8).forEach { i ->
                src.setAtIndex(ValueLayout.JAVA_INT, i.toLong(), i * 10)
            }

            val srcData =
                (0 until 8).map { i ->
                    src.getAtIndex(ValueLayout.JAVA_INT, i.toLong())
                }
            println("Source data: [${srcData.joinToString(", ")}]")

            // Copy 32 bytes (8 integers)
            memcpy.invokeExact(dest, src, 32L)

            val destData =
                (0 until 8).map { i ->
                    dest.getAtIndex(ValueLayout.JAVA_INT, i.toLong())
                }
            println("Copied data: [${destData.joinToString(", ")}]")
        }

        println()
    }

    /**
     * Demonstrates the advantages of FFM API over JNI.
     */
    fun demonstrateAdvantagesOverJNI() {
        println("=== Advantages over JNI ===\n")

        println("FFM API improvements over traditional JNI:")
        println()
        println("| Aspect              | JNI                  | FFM API            |")
        println("|---------------------|----------------------|--------------------|")
        println("| Native code         | Requires C code      | Pure Java/Kotlin   |")
        println("| Compilation         | javah + C compiler   | None required      |")
        println("| Memory safety       | Manual management    | Arena-based        |")
        println("| Type safety         | Weak                 | Strong             |")
        println("| Bounds checking     | None                 | Built-in           |")
        println("| Thread safety       | Manual               | Confined arenas    |")
        println("| Performance         | Good                 | Comparable         |")
        println("| Debugging           | Difficult            | Better tooling     |")
        println()
        println("Key benefits for Kotlin developers:")
        println("1. No native code to write - define signatures in Kotlin")
        println("2. Extension functions make API more idiomatic")
        println("3. Safer - bounds checking prevents buffer overflows")
        println("4. Kotlin's use extension integrates with Arena lifecycle")
        println("5. Null-safety helps avoid common FFM pitfalls")
        println()
    }
}

/**
 * Main entry point for foreign function examples.
 */
fun main() {
    println("========================================")
    println("  FFM API - Foreign Function Calls")
    println("  Kotlin with Java 21 FFM API")
    println("========================================\n")

    try {
        ForeignFunctionExample.demonstrateStrlen()
        ForeignFunctionExample.demonstrateAbs()
        ForeignFunctionExample.demonstrateSqrt()
        ForeignFunctionExample.demonstrateTime()
        ForeignFunctionExample.demonstrateMemcpy()
        ForeignFunctionExample.demonstrateAdvantagesOverJNI()
    } catch (e: Throwable) {
        System.err.println("Error calling native function: ${e.message}")
        e.printStackTrace()
    }

    println("========================================")
    println("  Key Takeaways:")
    println("  - Use Linker for native calling convention")
    println("  - FunctionDescriptor defines signatures")
    println("  - SymbolLookup finds native functions")
    println("  - MethodHandle invokes native code")
    println("  - Kotlin extensions make API idiomatic")
    println("========================================")
}
