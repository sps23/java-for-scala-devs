package io.github.sps23.interview.preparation.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * Demonstrates Java 21's Foreign Function and Memory (FFM) API for calling
 * native C library functions.
 *
 * <p>
 * The FFM API provides a modern alternative to JNI for native code integration.
 * This example demonstrates:
 * <ul>
 * <li>Linker for creating method handles to native functions</li>
 * <li>FunctionDescriptor for describing native function signatures</li>
 * <li>SymbolLookup for finding native functions in libraries</li>
 * <li>Calling standard C library functions (strlen, abs, etc.)</li>
 * </ul>
 *
 * <p>
 * For Scala developers: This is similar to using JNA or JNI, but with a pure
 * Java API that's type-safe and doesn't require native code compilation.
 *
 * <p>
 * Key FFM API concepts for function calls:
 * <ul>
 * <li><b>Linker</b>: Creates method handles for native functions following the
 * platform's calling convention.</li>
 * <li><b>FunctionDescriptor</b>: Describes the signature (return type and
 * parameter types) of a native function.</li>
 * <li><b>SymbolLookup</b>: Locates native functions by name in loaded
 * libraries.</li>
 * <li><b>MethodHandle</b>: Java representation of a callable native
 * function.</li>
 * </ul>
 *
 * @see NativeMemoryExample for memory management examples
 */
public class ForeignFunctionExample {

    // Native linker for the current platform (e.g., x86_64 Linux)
    private static final Linker LINKER = Linker.nativeLinker();

    // Symbol lookup for standard C library functions
    private static final SymbolLookup STDLIB = LINKER.defaultLookup();

    /**
     * Demonstrates calling the C strlen function.
     *
     * <p>
     * C signature: {@code size_t strlen(const char *s)}
     */
    public static void demonstrateStrlen() throws Throwable {
        System.out.println("=== Calling C strlen() ===\n");

        // Find the strlen symbol in the C library
        MemorySegment strlenSymbol = STDLIB.find("strlen")
                .orElseThrow(() -> new RuntimeException("strlen not found"));

        // Define the function signature: returns long (size_t), takes pointer
        // (ADDRESS)
        FunctionDescriptor strlenDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_LONG, // return
                                                                                           // type
                ValueLayout.ADDRESS // parameter: const char*
        );

        // Create a method handle for strlen
        MethodHandle strlen = LINKER.downcallHandle(strlenSymbol, strlenDescriptor);

        try (Arena arena = Arena.ofConfined()) {
            // Create a null-terminated string in native memory (UTF-8)
            String testString = "Hello, FFM API!";
            MemorySegment nativeString = arena.allocateUtf8String(testString);

            // Call strlen
            long length = (long) strlen.invokeExact(nativeString);

            System.out.println("String: \"" + testString + "\"");
            System.out.println("Java length: " + testString.length());
            System.out.println("Native strlen result: " + length);
        }

        System.out.println();
    }

    /**
     * Demonstrates calling the C abs function.
     *
     * <p>
     * C signature: {@code int abs(int n)}
     */
    public static void demonstrateAbs() throws Throwable {
        System.out.println("=== Calling C abs() ===\n");

        MemorySegment absSymbol = STDLIB.find("abs")
                .orElseThrow(() -> new RuntimeException("abs not found"));

        // Function signature: int abs(int)
        FunctionDescriptor absDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_INT, // return
                                                                                       // type
                ValueLayout.JAVA_INT // parameter
        );

        MethodHandle abs = LINKER.downcallHandle(absSymbol, absDescriptor);

        // Test with various values
        int[] testValues = {-42, 0, 42, Integer.MIN_VALUE + 1};

        for (int value : testValues) {
            int result = (int) abs.invokeExact(value);
            System.out.printf("abs(%d) = %d%n", value, result);
        }

        System.out.println();
    }

    /**
     * Demonstrates calling the C sqrt function from math library.
     *
     * <p>
     * C signature: {@code double sqrt(double x)}
     */
    public static void demonstrateSqrt() throws Throwable {
        System.out.println("=== Calling C sqrt() ===\n");

        MemorySegment sqrtSymbol = STDLIB.find("sqrt")
                .orElseThrow(() -> new RuntimeException("sqrt not found"));

        // Function signature: double sqrt(double)
        FunctionDescriptor sqrtDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, // return
                                                                                           // type
                ValueLayout.JAVA_DOUBLE // parameter
        );

        MethodHandle sqrt = LINKER.downcallHandle(sqrtSymbol, sqrtDescriptor);

        // Test with various values
        double[] testValues = {4.0, 9.0, 16.0, 2.0, 100.0};

        for (double value : testValues) {
            double result = (double) sqrt.invokeExact(value);
            System.out.printf("sqrt(%.1f) = %.6f%n", value, result);
        }

        System.out.println();
    }

    /**
     * Demonstrates calling the C time function.
     *
     * <p>
     * C signature: {@code time_t time(time_t *tloc)}
     */
    public static void demonstrateTime() throws Throwable {
        System.out.println("=== Calling C time() ===\n");

        MemorySegment timeSymbol = STDLIB.find("time")
                .orElseThrow(() -> new RuntimeException("time not found"));

        // Function signature: long time(pointer)
        // We pass NULL to just get the return value
        FunctionDescriptor timeDescriptor = FunctionDescriptor.of(ValueLayout.JAVA_LONG, // return
                                                                                         // type
                                                                                         // (time_t)
                ValueLayout.ADDRESS // parameter (time_t *tloc)
        );

        MethodHandle time = LINKER.downcallHandle(timeSymbol, timeDescriptor);

        // Call time(NULL) to get current time
        long timestamp = (long) time.invokeExact(MemorySegment.NULL);

        System.out.println("Current Unix timestamp: " + timestamp);
        System.out.println(
                "Java System.currentTimeMillis()/1000: " + System.currentTimeMillis() / 1000);

        System.out.println();
    }

    /**
     * Demonstrates calling C memcpy function.
     *
     * <p>
     * C signature: {@code void *memcpy(void *dest, const void *src, size_t n)}
     */
    public static void demonstrateMemcpy() throws Throwable {
        System.out.println("=== Calling C memcpy() ===\n");

        MemorySegment memcpySymbol = STDLIB.find("memcpy")
                .orElseThrow(() -> new RuntimeException("memcpy not found"));

        // Function signature: pointer memcpy(pointer, pointer, long)
        FunctionDescriptor memcpyDescriptor = FunctionDescriptor.of(ValueLayout.ADDRESS, // return
                                                                                         // type
                ValueLayout.ADDRESS, // dest
                ValueLayout.ADDRESS, // src
                ValueLayout.JAVA_LONG // size_t n
        );

        MethodHandle memcpy = LINKER.downcallHandle(memcpySymbol, memcpyDescriptor);

        try (Arena arena = Arena.ofConfined()) {
            // Allocate source and destination buffers
            MemorySegment src = arena.allocate(32);
            MemorySegment dest = arena.allocate(32);

            // Fill source with data
            for (int i = 0; i < 8; i++) {
                src.setAtIndex(ValueLayout.JAVA_INT, i, i * 10);
            }

            System.out.print("Source data: [");
            for (int i = 0; i < 8; i++) {
                System.out.print(src.getAtIndex(ValueLayout.JAVA_INT, i));
                if (i < 7)
                    System.out.print(", ");
            }
            System.out.println("]");

            // Copy 32 bytes (8 integers)
            memcpy.invokeExact(dest, src, 32L);

            System.out.print("Copied data: [");
            for (int i = 0; i < 8; i++) {
                System.out.print(dest.getAtIndex(ValueLayout.JAVA_INT, i));
                if (i < 7)
                    System.out.print(", ");
            }
            System.out.println("]");
        }

        System.out.println();
    }

    /**
     * Demonstrates the advantages of FFM API over JNI.
     */
    public static void demonstrateAdvantagesOverJNI() {
        System.out.println("=== Advantages over JNI ===\n");

        System.out.println("FFM API improvements over traditional JNI:");
        System.out.println();
        System.out.println("| Aspect              | JNI                  | FFM API            |");
        System.out.println("|---------------------|----------------------|--------------------|");
        System.out.println("| Native code         | Requires C code      | Pure Java          |");
        System.out.println("| Compilation         | javah + C compiler   | None required      |");
        System.out.println("| Memory safety       | Manual management    | Arena-based        |");
        System.out.println("| Type safety         | Weak                 | Strong             |");
        System.out.println("| Bounds checking     | None                 | Built-in           |");
        System.out.println("| Thread safety       | Manual               | Confined arenas    |");
        System.out.println("| Performance         | Good                 | Comparable         |");
        System.out.println("| Debugging           | Difficult            | Better tooling     |");
        System.out.println();
        System.out.println("Key benefits:");
        System.out.println("1. No native code to write - define signatures in Java");
        System.out.println("2. No compilation step - no javah, no C compiler needed");
        System.out.println("3. Safer - bounds checking prevents buffer overflows");
        System.out.println("4. Cleaner API - method handles instead of JNI functions");
        System.out.println("5. Better lifecycle management with Arena");
        System.out.println();
    }

    /**
     * Example usage demonstrating FFM API for native function calls.
     */
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("  FFM API - Foreign Function Calls");
        System.out.println("  Java 21 Foreign Function & Memory API");
        System.out.println("========================================\n");

        try {
            demonstrateStrlen();
            demonstrateAbs();
            demonstrateSqrt();
            demonstrateTime();
            demonstrateMemcpy();
            demonstrateAdvantagesOverJNI();
        } catch (Throwable e) {
            System.err.println("Error calling native function: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("========================================");
        System.out.println("  Key Takeaways:");
        System.out.println("  - Use Linker for native calling convention");
        System.out.println("  - FunctionDescriptor defines signatures");
        System.out.println("  - SymbolLookup finds native functions");
        System.out.println("  - MethodHandle invokes native code");
        System.out.println("  - Much simpler than JNI!");
        System.out.println("========================================");
    }
}
