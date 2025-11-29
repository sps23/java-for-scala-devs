package io.github.sps23.interview.preparation.ffm

import java.lang.foreign.Arena
import java.lang.foreign.MemoryLayout
import java.lang.foreign.ValueLayout

/**
 * Demonstrates Java 21's Foreign Function and Memory (FFM) API for native
 * memory management in Kotlin.
 *
 * The FFM API provides safe, efficient access to native memory without JNI.
 * This example shows:
 * - Arena for memory lifecycle management (automatic deallocation)
 * - MemorySegment for type-safe native memory access
 * - MemoryLayout for structured data layouts
 * - Safety improvements over manual memory management
 *
 * For Kotlin developers: Think of Arena as similar to Kotlin's `use` extension
 * for Closeable - it automatically cleans up memory when the scope exits.
 *
 * Key FFM API concepts:
 * - **Arena**: Controls the lifecycle of memory segments. When closed, all memory is freed.
 * - **MemorySegment**: A contiguous region of memory with bounds checking.
 * - **MemoryLayout**: Describes the layout of data in memory (structs/arrays).
 * - **ValueLayout**: Primitive type layouts (int, long, double, etc.).
 *
 * @see ForeignFunctionExample for calling native functions
 * @see NativeMemoryExample.java for Java version
 */
object NativeMemoryExample {
    /**
     * Demonstrates basic native memory allocation and access using Arena.
     *
     * Arena types:
     * - `Arena.ofConfined()` - single-threaded, must close in same thread
     * - `Arena.ofShared()` - multi-threaded, can close from any thread
     * - `Arena.ofAuto()` - automatically freed by GC (no explicit close)
     * - `Arena.global()` - never freed, for permanent allocations
     */
    fun demonstrateArenaLifecycle() {
        println("=== Arena Lifecycle Management ===\n")

        // Using Kotlin's use extension for automatic resource cleanup
        Arena.ofConfined().use { arena ->
            // Allocate 1024 bytes of native memory
            val segment = arena.allocate(1024)

            println("Allocated segment: $segment")
            println("Segment size: ${segment.byteSize()} bytes")

            // Write data to the segment
            segment.set(ValueLayout.JAVA_INT, 0, 42)
            segment.set(ValueLayout.JAVA_INT, 4, 100)
            segment.set(ValueLayout.JAVA_LONG, 8, 123456789L)

            // Read data back
            val first = segment.get(ValueLayout.JAVA_INT, 0)
            val second = segment.get(ValueLayout.JAVA_INT, 4)
            val third = segment.get(ValueLayout.JAVA_LONG, 8)

            println("Read values: $first, $second, $third")
        }

        println("Arena closed - memory automatically freed\n")
    }

    /**
     * Demonstrates working with structured data using MemoryLayout.
     *
     * This is similar to defining a C struct and accessing its fields.
     */
    fun demonstrateStructuredData() {
        println("=== Structured Data with MemoryLayout ===\n")

        // Define a struct layout for a "Point3D" structure:
        // struct Point3D { double x; double y; double z; }
        val point3DLayout =
            MemoryLayout.structLayout(
                ValueLayout.JAVA_DOUBLE.withName("x"),
                ValueLayout.JAVA_DOUBLE.withName("y"),
                ValueLayout.JAVA_DOUBLE.withName("z"),
            )

        println("Point3D layout: $point3DLayout")
        println("Point3D size: ${point3DLayout.byteSize()} bytes")

        Arena.ofConfined().use { arena ->
            // Allocate memory for the struct
            val point = arena.allocate(point3DLayout)

            // Get VarHandles for each field using path elements
            val xHandle = point3DLayout.varHandle(MemoryLayout.PathElement.groupElement("x"))
            val yHandle = point3DLayout.varHandle(MemoryLayout.PathElement.groupElement("y"))
            val zHandle = point3DLayout.varHandle(MemoryLayout.PathElement.groupElement("z"))

            // Set field values
            xHandle.set(point, 0L, 1.0)
            yHandle.set(point, 0L, 2.0)
            zHandle.set(point, 0L, 3.0)

            // Read field values
            val x = xHandle.get(point, 0L) as Double
            val y = yHandle.get(point, 0L) as Double
            val z = zHandle.get(point, 0L) as Double

            println("Point3D values: (%.1f, %.1f, %.1f)".format(x, y, z))

            // Calculate distance from origin
            val distance = kotlin.math.sqrt(x * x + y * y + z * z)
            println("Distance from origin: %.4f".format(distance))
        }

        println()
    }

    /**
     * Demonstrates working with native arrays.
     */
    fun demonstrateNativeArrays() {
        println("=== Native Arrays ===\n")

        Arena.ofConfined().use { arena ->
            // Allocate an array of 10 integers
            val arraySize = 10L
            val intArray = arena.allocateArray(ValueLayout.JAVA_INT, arraySize)

            println("Allocated int array of size: $arraySize")

            // Fill the array with squares - idiomatic Kotlin style
            (0 until arraySize.toInt()).forEach { i ->
                intArray.setAtIndex(ValueLayout.JAVA_INT, i.toLong(), i * i)
            }

            // Read back and print - idiomatic Kotlin style
            val contents =
                (0 until arraySize.toInt()).map { i ->
                    intArray.getAtIndex(ValueLayout.JAVA_INT, i.toLong())
                }
            println("Array contents: [${contents.joinToString(", ")}]")

            // Calculate sum
            val sum = contents.sum()
            println("Sum of squares: $sum")
        }

        println()
    }

    /**
     * Demonstrates string handling in native memory.
     */
    fun demonstrateStringHandling() {
        println("=== String Handling ===\n")

        Arena.ofConfined().use { arena ->
            // Allocate a C-style string (null-terminated UTF-8)
            val kotlinString = "Hello from FFM API in Kotlin!"
            val nativeString = arena.allocateUtf8String(kotlinString)

            println("Original Kotlin string: $kotlinString")
            println("Native segment size: ${nativeString.byteSize()} bytes")

            // Read back as Java/Kotlin string
            val readBack = nativeString.getUtf8String(0)
            println("Read back string: $readBack")

            // Manual null-terminated string handling
            val manualString = arena.allocate(32)
            val bytes = "Manual string".toByteArray()
            bytes.forEachIndexed { i, b ->
                manualString.set(ValueLayout.JAVA_BYTE, i.toLong(), b)
            }
            manualString.set(ValueLayout.JAVA_BYTE, bytes.size.toLong(), 0.toByte()) // null terminator

            println("Manual string: ${manualString.getUtf8String(0)}")
        }

        println()
    }

    /**
     * Demonstrates memory safety features compared to JNI.
     */
    fun demonstrateSafetyFeatures() {
        println("=== Safety Features ===\n")

        println("FFM API Safety Improvements over JNI:")
        println("1. Bounds checking - access outside segment throws exception")
        println("2. Lifetime management - Arena ensures memory is freed")
        println("3. Thread confinement - prevents data races")
        println("4. No manual pointer arithmetic in Java/Kotlin code")
        println()

        Arena.ofConfined().use { arena ->
            val small = arena.allocate(8)

            // This would throw IndexOutOfBoundsException
            // small.get(ValueLayout.JAVA_LONG, 8) // Attempt to read beyond bounds

            println("Bounds checking prevents buffer overflows!")
        }

        // Demonstrating confined arena thread safety
        println("\nThread confinement example:")
        Arena.ofConfined().use { confined ->
            val segment = confined.allocate(16)
            println("Confined arena can only be accessed from creating thread")
            // Accessing from another thread would throw WrongThreadException
        }

        println()
    }
}

/**
 * Main entry point for native memory examples.
 */
fun main() {
    println("========================================")
    println("  FFM API - Native Memory Management")
    println("  Kotlin with Java 21 FFM API")
    println("========================================\n")

    NativeMemoryExample.demonstrateArenaLifecycle()
    NativeMemoryExample.demonstrateStructuredData()
    NativeMemoryExample.demonstrateNativeArrays()
    NativeMemoryExample.demonstrateStringHandling()
    NativeMemoryExample.demonstrateSafetyFeatures()

    println("========================================")
    println("  Key Takeaways:")
    println("  - Use Arena for deterministic memory management")
    println("  - MemorySegment provides safe memory access")
    println("  - MemoryLayout defines structured data")
    println("  - Much safer than JNI with bounds checking")
    println("  - Kotlin's use extension integrates nicely with Arena")
    println("========================================")
}
