package io.github.sps23.interview.preparation.ffm

import java.lang.foreign.{Arena, MemoryLayout, MemorySegment, ValueLayout}

/** Demonstrates Java 21's Foreign Function and Memory (FFM) API for native memory management in
  * Scala 3.
  *
  * The FFM API provides safe, efficient access to native memory without JNI. This example shows:
  *   - Arena for memory lifecycle management (automatic deallocation)
  *   - MemorySegment for type-safe native memory access
  *   - MemoryLayout for structured data layouts
  *   - Safety improvements over manual memory management
  *
  * For Scala developers: Think of Arena as a managed region similar to ZIO's Scope or Cats Effect's
  * Resource - it automatically cleans up memory when the scope exits.
  *
  * Key FFM API concepts:
  *   - '''Arena''': Controls the lifecycle of memory segments. When closed, all memory is freed.
  *   - '''MemorySegment''': A contiguous region of memory with bounds checking.
  *   - '''MemoryLayout''': Describes the layout of data in memory (structs/arrays).
  *   - '''ValueLayout''': Primitive type layouts (int, long, double, etc.).
  *
  * @see
  *   ForeignFunctionExample for calling native functions
  * @see
  *   NativeMemoryExample.java for Java version
  */
object NativeMemoryExample:

  /** Demonstrates basic native memory allocation and access using Arena.
    *
    * Arena types:
    *   - `Arena.ofConfined()` - single-threaded, must close in same thread
    *   - `Arena.ofShared()` - multi-threaded, can close from any thread
    *   - `Arena.ofAuto()` - automatically freed by GC (no explicit close)
    *   - `Arena.global()` - never freed, for permanent allocations
    */
  def demonstrateArenaLifecycle(): Unit =
    println("=== Arena Lifecycle Management ===\n")

    // Using Scala's Using for automatic resource cleanup (similar to try-with-resources)
    import scala.util.Using

    Using.resource(Arena.ofConfined()) { arena =>
      // Allocate 1024 bytes of native memory
      val segment = arena.allocate(1024)

      println(s"Allocated segment: $segment")
      println(s"Segment size: ${segment.byteSize()} bytes")

      // Write data to the segment
      segment.set(ValueLayout.JAVA_INT, 0, 42)
      segment.set(ValueLayout.JAVA_INT, 4, 100)
      segment.set(ValueLayout.JAVA_LONG, 8, 123456789L)

      // Read data back
      val first  = segment.get(ValueLayout.JAVA_INT, 0)
      val second = segment.get(ValueLayout.JAVA_INT, 4)
      val third  = segment.get(ValueLayout.JAVA_LONG, 8)

      println(s"Read values: $first, $second, $third")
    }

    println("Arena closed - memory automatically freed\n")

  /** Demonstrates working with structured data using MemoryLayout.
    *
    * This is similar to defining a C struct and accessing its fields.
    */
  def demonstrateStructuredData(): Unit =
    println("=== Structured Data with MemoryLayout ===\n")

    import scala.util.Using

    // Define a struct layout for a "Point3D" structure:
    // struct Point3D { double x; double y; double z; }
    val point3DLayout = MemoryLayout.structLayout(
      ValueLayout.JAVA_DOUBLE.withName("x"),
      ValueLayout.JAVA_DOUBLE.withName("y"),
      ValueLayout.JAVA_DOUBLE.withName("z")
    )

    println(s"Point3D layout: $point3DLayout")
    println(s"Point3D size: ${point3DLayout.byteSize()} bytes")

    Using.resource(Arena.ofConfined()) { arena =>
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
      val x = xHandle.get(point, 0L).asInstanceOf[Double]
      val y = yHandle.get(point, 0L).asInstanceOf[Double]
      val z = zHandle.get(point, 0L).asInstanceOf[Double]

      println(f"Point3D values: ($x%.1f, $y%.1f, $z%.1f)")

      // Calculate distance from origin
      val distance = math.sqrt(x * x + y * y + z * z)
      println(f"Distance from origin: $distance%.4f")
    }

    println()

  /** Demonstrates working with native arrays. */
  def demonstrateNativeArrays(): Unit =
    println("=== Native Arrays ===\n")

    import scala.util.Using

    Using.resource(Arena.ofConfined()) { arena =>
      // Allocate an array of 10 integers using allocateArray
      val arraySize = 10L
      val intArray  = arena.allocateArray(ValueLayout.JAVA_INT, arraySize)

      println(s"Allocated int array of size: $arraySize")

      // Fill the array with squares - functional style
      (0 until arraySize.toInt).foreach(i => intArray.setAtIndex(ValueLayout.JAVA_INT, i, i * i))

      // Read back and print - functional style
      val contents = (0 until arraySize.toInt).map { i =>
        intArray.getAtIndex(ValueLayout.JAVA_INT, i)
      }
      println(s"Array contents: [${contents.mkString(", ")}]")

      // Calculate sum using fold
      val sum = contents.sum
      println(s"Sum of squares: $sum")
    }

    println()

  /** Demonstrates string handling in native memory. */
  def demonstrateStringHandling(): Unit =
    println("=== String Handling ===\n")

    import scala.util.Using

    Using.resource(Arena.ofConfined()) { arena =>
      // Allocate a C-style string (null-terminated UTF-8)
      val javaString   = "Hello from FFM API in Scala!"
      val nativeString = arena.allocateUtf8String(javaString)

      println(s"Original Scala string: $javaString")
      println(s"Native segment size: ${nativeString.byteSize()} bytes")

      // Read back as Java/Scala string
      val readBack = nativeString.getUtf8String(0)
      println(s"Read back string: $readBack")

      // Manual null-terminated string handling
      val manualString = arena.allocate(32)
      val bytes        = "Manual string".getBytes
      bytes.zipWithIndex.foreach { case (b, i) =>
        manualString.set(ValueLayout.JAVA_BYTE, i, b)
      }
      manualString.set(ValueLayout.JAVA_BYTE, bytes.length, 0.toByte) // null terminator

      println(s"Manual string: ${manualString.getUtf8String(0)}")
    }

    println()

  /** Demonstrates memory safety features compared to JNI. */
  def demonstrateSafetyFeatures(): Unit =
    println("=== Safety Features ===\n")

    println("FFM API Safety Improvements over JNI:")
    println("1. Bounds checking - access outside segment throws exception")
    println("2. Lifetime management - Arena ensures memory is freed")
    println("3. Thread confinement - prevents data races")
    println("4. No manual pointer arithmetic in Java/Scala code")
    println()

    import scala.util.Using

    Using.resource(Arena.ofConfined()) { arena =>
      val small = arena.allocate(8)

      // This would throw IndexOutOfBoundsException
      // small.get(ValueLayout.JAVA_LONG, 8) // Attempt to read beyond bounds

      println("Bounds checking prevents buffer overflows!")
    }

    // Demonstrating confined arena thread safety
    println("\nThread confinement example:")
    Using.resource(Arena.ofConfined()) { confined =>
      val segment = confined.allocate(16)
      println("Confined arena can only be accessed from creating thread")
      // Accessing from another thread would throw WrongThreadException
    }

    println()

/** Main entry point for native memory examples. */
@main def runNativeMemoryExample(): Unit =
  println("========================================")
  println("  FFM API - Native Memory Management")
  println("  Scala 3 with Java 21 FFM API")
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
  println("  - Scala's Using integrates nicely with Arena")
  println("========================================")
