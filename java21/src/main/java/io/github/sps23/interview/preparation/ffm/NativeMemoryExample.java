package io.github.sps23.interview.preparation.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * Demonstrates Java 21's Foreign Function and Memory (FFM) API for native
 * memory management.
 *
 * <p>
 * The FFM API provides safe, efficient access to native memory without JNI.
 * This example shows:
 * <ul>
 * <li>Arena for memory lifecycle management (automatic deallocation)</li>
 * <li>MemorySegment for type-safe native memory access</li>
 * <li>MemoryLayout for structured data layouts</li>
 * <li>Safety improvements over manual memory management</li>
 * </ul>
 *
 * <p>
 * For Scala developers: Think of Arena as a managed region similar to ZIO's
 * Scope or Cats Effect's Resource - it automatically cleans up memory when the
 * scope exits.
 *
 * <p>
 * Key FFM API concepts:
 * <ul>
 * <li><b>Arena</b>: Controls the lifecycle of memory segments. When an arena is
 * closed, all associated memory is freed.</li>
 * <li><b>MemorySegment</b>: A contiguous region of memory with bounds
 * checking.</li>
 * <li><b>MemoryLayout</b>: Describes the layout of data in memory
 * (structs/arrays).</li>
 * <li><b>ValueLayout</b>: Primitive type layouts (int, long, double,
 * etc.).</li>
 * </ul>
 *
 * @see ForeignFunctionExample for calling native functions
 */
public class NativeMemoryExample {

    /**
     * Demonstrates basic native memory allocation and access using Arena.
     *
     * <p>
     * Arena types:
     * <ul>
     * <li>{@code Arena.ofConfined()} - single-threaded, must close in same
     * thread</li>
     * <li>{@code Arena.ofShared()} - multi-threaded, can close from any thread</li>
     * <li>{@code Arena.ofAuto()} - automatically freed by GC (no explicit
     * close)</li>
     * <li>{@code Arena.global()} - never freed, for permanent allocations</li>
     * </ul>
     */
    public static void demonstrateArenaLifecycle() {
        System.out.println("=== Arena Lifecycle Management ===\n");

        // Confined arena - single-threaded, deterministic cleanup
        // Similar to try-with-resources for memory
        try (Arena arena = Arena.ofConfined()) {
            // Allocate 1024 bytes of native memory
            MemorySegment segment = arena.allocate(1024);

            System.out.println("Allocated segment: " + segment);
            System.out.println("Segment size: " + segment.byteSize() + " bytes");

            // Write data to the segment
            segment.set(ValueLayout.JAVA_INT, 0, 42);
            segment.set(ValueLayout.JAVA_INT, 4, 100);
            segment.set(ValueLayout.JAVA_LONG, 8, 123456789L);

            // Read data back
            int first = segment.get(ValueLayout.JAVA_INT, 0);
            int second = segment.get(ValueLayout.JAVA_INT, 4);
            long third = segment.get(ValueLayout.JAVA_LONG, 8);

            System.out.println("Read values: " + first + ", " + second + ", " + third);

            // Memory is automatically freed when arena exits
        }

        System.out.println("Arena closed - memory automatically freed\n");
    }

    /**
     * Demonstrates working with structured data using MemoryLayout.
     *
     * <p>
     * This is similar to defining a C struct and accessing its fields.
     */
    public static void demonstrateStructuredData() {
        System.out.println("=== Structured Data with MemoryLayout ===\n");

        // Define a struct layout for a "Point3D" structure:
        // struct Point3D { double x; double y; double z; }
        MemoryLayout point3DLayout = MemoryLayout.structLayout(
                ValueLayout.JAVA_DOUBLE.withName("x"), ValueLayout.JAVA_DOUBLE.withName("y"),
                ValueLayout.JAVA_DOUBLE.withName("z"));

        System.out.println("Point3D layout: " + point3DLayout);
        System.out.println("Point3D size: " + point3DLayout.byteSize() + " bytes");

        try (Arena arena = Arena.ofConfined()) {
            // Allocate memory for the struct
            MemorySegment point = arena.allocate(point3DLayout);

            // Get VarHandles for each field using path elements
            var xHandle = point3DLayout.varHandle(MemoryLayout.PathElement.groupElement("x"));
            var yHandle = point3DLayout.varHandle(MemoryLayout.PathElement.groupElement("y"));
            var zHandle = point3DLayout.varHandle(MemoryLayout.PathElement.groupElement("z"));

            // Set field values
            xHandle.set(point, 0L, 1.0);
            yHandle.set(point, 0L, 2.0);
            zHandle.set(point, 0L, 3.0);

            // Read field values
            double x = (double) xHandle.get(point, 0L);
            double y = (double) yHandle.get(point, 0L);
            double z = (double) zHandle.get(point, 0L);

            System.out.printf("Point3D values: (%.1f, %.1f, %.1f)%n", x, y, z);

            // Calculate distance from origin
            double distance = Math.sqrt(x * x + y * y + z * z);
            System.out.printf("Distance from origin: %.4f%n", distance);
        }

        System.out.println();
    }

    /**
     * Demonstrates working with native arrays.
     */
    public static void demonstrateNativeArrays() {
        System.out.println("=== Native Arrays ===\n");

        try (Arena arena = Arena.ofConfined()) {
            // Allocate an array of 10 integers using allocateArray
            long arraySize = 10;
            MemorySegment intArray = arena.allocateArray(ValueLayout.JAVA_INT, arraySize);

            System.out.println("Allocated int array of size: " + arraySize);

            // Fill the array with squares
            for (int i = 0; i < arraySize; i++) {
                intArray.setAtIndex(ValueLayout.JAVA_INT, i, i * i);
            }

            // Read back and print
            System.out.print("Array contents: [");
            for (int i = 0; i < arraySize; i++) {
                int value = intArray.getAtIndex(ValueLayout.JAVA_INT, i);
                System.out.print(value);
                if (i < arraySize - 1)
                    System.out.print(", ");
            }
            System.out.println("]");

            // Calculate sum
            int sum = 0;
            for (int i = 0; i < arraySize; i++) {
                sum += intArray.getAtIndex(ValueLayout.JAVA_INT, i);
            }
            System.out.println("Sum of squares: " + sum);
        }

        System.out.println();
    }

    /**
     * Demonstrates string handling in native memory.
     */
    public static void demonstrateStringHandling() {
        System.out.println("=== String Handling ===\n");

        try (Arena arena = Arena.ofConfined()) {
            // Allocate a C-style string (null-terminated UTF-8)
            String javaString = "Hello from FFM API!";
            MemorySegment nativeString = arena.allocateUtf8String(javaString);

            System.out.println("Original Java string: " + javaString);
            System.out.println("Native segment size: " + nativeString.byteSize() + " bytes");

            // Read back as Java string
            String readBack = nativeString.getUtf8String(0);
            System.out.println("Read back string: " + readBack);

            // Manual null-terminated string handling
            MemorySegment manualString = arena.allocate(32);
            byte[] bytes = "Manual string".getBytes();
            for (int i = 0; i < bytes.length; i++) {
                manualString.set(ValueLayout.JAVA_BYTE, i, bytes[i]);
            }
            manualString.set(ValueLayout.JAVA_BYTE, bytes.length, (byte) 0); // null terminator

            System.out.println("Manual string: " + manualString.getUtf8String(0));
        }

        System.out.println();
    }

    /**
     * Demonstrates memory safety features compared to JNI.
     */
    public static void demonstrateSafetyFeatures() {
        System.out.println("=== Safety Features ===\n");

        System.out.println("FFM API Safety Improvements over JNI:");
        System.out.println("1. Bounds checking - access outside segment throws exception");
        System.out.println("2. Lifetime management - Arena ensures memory is freed");
        System.out.println("3. Thread confinement - prevents data races");
        System.out.println("4. No manual pointer arithmetic in Java code");
        System.out.println();

        try (Arena arena = Arena.ofConfined()) {
            MemorySegment small = arena.allocate(8);

            // This would throw IndexOutOfBoundsException
            // small.get(ValueLayout.JAVA_LONG, 8); // Attempt to read beyond bounds

            System.out.println("Bounds checking prevents buffer overflows!");
        }

        // Demonstrating confined arena thread safety
        System.out.println("\nThread confinement example:");
        try (Arena confined = Arena.ofConfined()) {
            MemorySegment segment = confined.allocate(16);
            System.out.println("Confined arena can only be accessed from creating thread");
            // Accessing from another thread would throw WrongThreadException
        }

        System.out.println();
    }

    /**
     * Example usage demonstrating FFM API memory management.
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  FFM API - Native Memory Management");
        System.out.println("  Java 21 Foreign Function & Memory API");
        System.out.println("========================================\n");

        demonstrateArenaLifecycle();
        demonstrateStructuredData();
        demonstrateNativeArrays();
        demonstrateStringHandling();
        demonstrateSafetyFeatures();

        System.out.println("========================================");
        System.out.println("  Key Takeaways:");
        System.out.println("  - Use Arena for deterministic memory management");
        System.out.println("  - MemorySegment provides safe memory access");
        System.out.println("  - MemoryLayout defines structured data");
        System.out.println("  - Much safer than JNI with bounds checking");
        System.out.println("========================================");
    }
}
