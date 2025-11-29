---
layout: post
title: "Foreign Function and Memory API in Java 21"
date: 2025-11-29 19:00:00 +0000
categories: interview
tags: java java21 scala kotlin ffm native jni interview-preparation
---

Java 21 introduces the Foreign Function and Memory (FFM) API as a stable feature, providing a modern alternative to JNI for native code integration. In this post, we'll explore how to use the FFM API to integrate with native C libraries for tasks like compression or cryptography.

## The Problem: Native Library Integration

Many applications need to interact with native libraries for performance-critical operations, system-level functionality, or leveraging existing C/C++ codebases. Traditional approaches using JNI (Java Native Interface) are:

| Challenge | Impact |
|-----------|--------|
| Complexity | Requires writing C/C++ glue code |
| Build Process | Need native compilers and build tools |
| Safety | Manual memory management, no bounds checking |
| Portability | Platform-specific binaries |
| Debugging | Difficult to trace issues across JNI boundary |

## Before: Traditional JNI Approach

Here's what JNI typically required for calling a simple C function:

```java
// Step 1: Declare native method
public class NativeLib {
    static { System.loadLibrary("mylib"); }
    public native int strlen(String s);
}

// Step 2: Generate header with javah
// Step 3: Implement in C:
// JNIEXPORT jint JNICALL Java_NativeLib_strlen(JNIEnv *env, jobject obj, jstring s) {
//     const char *str = (*env)->GetStringUTFChars(env, s, 0);
//     int len = strlen(str);
//     (*env)->ReleaseStringUTFChars(env, s, str);
//     return len;
// }

// Step 4: Compile with native compiler
// Step 5: Package and distribute native library
```

**Problems**: Multiple languages, complex build, manual memory management, platform-specific binaries.

## After: FFM API Approach

With the FFM API, the same functionality is pure Java:

### Java 21

```java
public class NativeLibFFM {
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup STDLIB = LINKER.defaultLookup();

    public static long strlen(String s) throws Throwable {
        // Find the native strlen function
        MemorySegment strlenSymbol = STDLIB.find("strlen")
                .orElseThrow(() -> new RuntimeException("strlen not found"));

        // Define the function signature: size_t strlen(const char*)
        FunctionDescriptor descriptor = FunctionDescriptor.of(
                ValueLayout.JAVA_LONG,  // return type (size_t)
                ValueLayout.ADDRESS     // parameter (const char*)
        );

        // Create a method handle for the native function
        MethodHandle strlen = LINKER.downcallHandle(strlenSymbol, descriptor);

        // Call the native function with an Arena for memory management
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment nativeString = arena.allocateUtf8String(s);
            return (long) strlen.invokeExact(nativeString);
        }
    }
}
```

### Scala 3

```scala
object NativeLibFFM:
  private val linker: Linker = Linker.nativeLinker()
  private val stdlib: SymbolLookup = linker.defaultLookup()

  def strlen(s: String): Long =
    val strlenSymbol = stdlib.find("strlen")
      .orElseThrow(() => RuntimeException("strlen not found"))

    // Function signature: size_t strlen(const char*)
    val descriptor = FunctionDescriptor.of(
      ValueLayout.JAVA_LONG,  // return type
      ValueLayout.ADDRESS     // parameter
    )

    val strlenHandle = linker.downcallHandle(strlenSymbol, descriptor)

    Using.resource(Arena.ofConfined()) { arena =>
      val nativeString = arena.allocateUtf8String(s)
      strlenHandle.invokeExact(nativeString).asInstanceOf[Long]
    }
```

### Kotlin

```kotlin
object NativeLibFFM {
    private val linker: Linker = Linker.nativeLinker()
    private val stdlib: SymbolLookup = linker.defaultLookup()

    fun strlen(s: String): Long {
        val strlenSymbol = stdlib.find("strlen")
            .orElseThrow { RuntimeException("strlen not found") }

        // Function signature: size_t strlen(const char*)
        val descriptor = FunctionDescriptor.of(
            ValueLayout.JAVA_LONG,
            ValueLayout.ADDRESS
        )

        val strlenHandle = linker.downcallHandle(strlenSymbol, descriptor)

        return Arena.ofConfined().use { arena ->
            val nativeString = arena.allocateUtf8String(s)
            strlenHandle.invokeExact(nativeString) as Long
        }
    }
}
```

## Key FFM API Concepts

### Arena for Memory Lifecycle Management

An Arena controls the lifecycle of native memory allocations. When an arena is closed, all associated memory is automatically freed.

```java
// Confined arena - single-threaded, deterministic cleanup
try (Arena arena = Arena.ofConfined()) {
    MemorySegment segment = arena.allocate(1024);
    // Use the segment...
} // Memory automatically freed here

// Arena types:
// - Arena.ofConfined()  - single-threaded, must close in same thread
// - Arena.ofShared()    - multi-threaded, can close from any thread
// - Arena.ofAuto()      - automatically freed by GC
// - Arena.global()      - never freed, for permanent allocations
```

For Scala developers: Think of Arena as similar to ZIO's Scope or Cats Effect's Resource - it provides automatic resource cleanup.

### MemorySegment for Native Memory Access

MemorySegment represents a contiguous region of native memory with bounds checking:

```java
try (Arena arena = Arena.ofConfined()) {
    // Allocate 1024 bytes
    MemorySegment segment = arena.allocate(1024);

    // Write data with type safety
    segment.set(ValueLayout.JAVA_INT, 0, 42);
    segment.set(ValueLayout.JAVA_LONG, 4, 123456789L);

    // Read data back
    int first = segment.get(ValueLayout.JAVA_INT, 0);
    long second = segment.get(ValueLayout.JAVA_LONG, 4);

    // Bounds checking prevents buffer overflows
    // segment.get(ValueLayout.JAVA_LONG, 1020); // Would throw!
}
```

### Linker and FunctionDescriptor for Native Calls

The Linker creates method handles for native functions, while FunctionDescriptor describes function signatures:

```java
// Get the native linker for the current platform
Linker linker = Linker.nativeLinker();

// Define function signature: double sqrt(double)
FunctionDescriptor sqrtDescriptor = FunctionDescriptor.of(
    ValueLayout.JAVA_DOUBLE,  // return type
    ValueLayout.JAVA_DOUBLE   // parameter
);

// Create method handle
MethodHandle sqrt = linker.downcallHandle(
    linker.defaultLookup().find("sqrt").orElseThrow(),
    sqrtDescriptor
);

// Call the native function
double result = (double) sqrt.invokeExact(16.0);  // Returns 4.0
```

### SymbolLookup for Finding Native Functions

SymbolLookup locates native functions in loaded libraries:

```java
// Default lookup includes standard C library
SymbolLookup stdlib = Linker.nativeLinker().defaultLookup();

// Find common C functions
MemorySegment strlen = stdlib.find("strlen").orElseThrow();
MemorySegment abs = stdlib.find("abs").orElseThrow();
MemorySegment sqrt = stdlib.find("sqrt").orElseThrow();

// Load a specific library
// SymbolLookup customLib = SymbolLookup.libraryLookup("libcustom.so", Arena.global());
```

## Working with Structured Data

Define C-like structs using MemoryLayout:

### Java 21

```java
// Define: struct Point3D { double x; double y; double z; }
MemoryLayout point3DLayout = MemoryLayout.structLayout(
    ValueLayout.JAVA_DOUBLE.withName("x"),
    ValueLayout.JAVA_DOUBLE.withName("y"),
    ValueLayout.JAVA_DOUBLE.withName("z")
);

try (Arena arena = Arena.ofConfined()) {
    MemorySegment point = arena.allocate(point3DLayout);

    // Get VarHandles for field access
    var xHandle = point3DLayout.varHandle(
        MemoryLayout.PathElement.groupElement("x"));
    var yHandle = point3DLayout.varHandle(
        MemoryLayout.PathElement.groupElement("y"));
    var zHandle = point3DLayout.varHandle(
        MemoryLayout.PathElement.groupElement("z"));

    // Set and get field values
    xHandle.set(point, 0L, 1.0);
    yHandle.set(point, 0L, 2.0);
    zHandle.set(point, 0L, 3.0);

    double x = (double) xHandle.get(point, 0L);  // 1.0
}
```

## Calling Multiple Native Functions

Here's a complete example calling several C library functions:

```java
public class FFMDemo {
    private static final Linker LINKER = Linker.nativeLinker();
    private static final SymbolLookup STDLIB = LINKER.defaultLookup();

    public static void main(String[] args) throws Throwable {
        // abs(int) -> int
        var abs = LINKER.downcallHandle(
            STDLIB.find("abs").orElseThrow(),
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT)
        );
        System.out.println("abs(-42) = " + (int) abs.invokeExact(-42));

        // sqrt(double) -> double
        var sqrt = LINKER.downcallHandle(
            STDLIB.find("sqrt").orElseThrow(),
            FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_DOUBLE)
        );
        System.out.println("sqrt(16.0) = " + (double) sqrt.invokeExact(16.0));

        // time(time_t*) -> time_t
        var time = LINKER.downcallHandle(
            STDLIB.find("time").orElseThrow(),
            FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
        );
        long timestamp = (long) time.invokeExact(MemorySegment.NULL);
        System.out.println("Current Unix timestamp: " + timestamp);

        // strlen with string handling
        try (Arena arena = Arena.ofConfined()) {
            var strlen = LINKER.downcallHandle(
                STDLIB.find("strlen").orElseThrow(),
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS)
            );
            MemorySegment str = arena.allocateUtf8String("Hello, FFM!");
            System.out.println("strlen result: " + (long) strlen.invokeExact(str));
        }
    }
}
```

## Safety Improvements Over JNI

The FFM API provides significant safety improvements:

| Feature | JNI | FFM API |
|---------|-----|---------|
| Bounds Checking | None | Built-in |
| Memory Management | Manual | Arena-based |
| Type Safety | Weak | Strong |
| Null Safety | Manual checks | Integrated |
| Thread Safety | Manual | Confined arenas |

### Example: Bounds Checking

```java
try (Arena arena = Arena.ofConfined()) {
    MemorySegment small = arena.allocate(8);

    // This is safe
    small.set(ValueLayout.JAVA_LONG, 0, 42L);

    // This throws IndexOutOfBoundsException - prevented!
    // small.set(ValueLayout.JAVA_LONG, 8, 999L);
}
```

### Example: Thread Confinement

```java
try (Arena confined = Arena.ofConfined()) {
    MemorySegment segment = confined.allocate(16);

    // Safe: same thread
    segment.set(ValueLayout.JAVA_INT, 0, 42);

    // Would throw WrongThreadException if accessed from another thread
    // new Thread(() -> segment.get(ValueLayout.JAVA_INT, 0)).start();
}
```

## FFM API vs JNI Comparison

| Aspect | JNI | FFM API |
|--------|-----|---------|
| Native Code | Requires C/C++ glue code | Pure Java |
| Build Process | javah + C compiler | None needed |
| Memory Safety | Manual, error-prone | Arena-managed |
| Type Safety | Weak | Strong with layouts |
| Bounds Checking | None | Built-in |
| Thread Safety | Manual synchronization | Confined arenas |
| Performance | Excellent | Comparable |
| Debugging | Difficult | Better tooling |
| Learning Curve | Steep | Moderate |

## For Scala Developers

The FFM API provides similar benefits to what you get from effect systems:

| Feature | FFM API | ZIO/Cats Effect |
|---------|---------|-----------------|
| Resource Management | Arena | Scope/Resource |
| Memory Safety | Built-in | Not applicable |
| Error Handling | Exceptions | Effect types |
| Composability | Method handles | Monadic |

Scala's `Using.resource` integrates naturally with Arena:

```scala
import scala.util.Using

Using.resource(Arena.ofConfined()) { arena =>
  val segment = arena.allocate(1024)
  // Use segment safely
} // Automatic cleanup
```

## For Kotlin Developers

Kotlin's `use` extension works seamlessly with Arena:

```kotlin
Arena.ofConfined().use { arena ->
    val segment = arena.allocate(1024)
    // Use segment safely
} // Automatic cleanup
```

Kotlin's null-safety complements FFM's safety features.

## When to Use FFM API

### Use FFM API For:

✅ Calling standard C library functions  
✅ Integrating with existing native libraries  
✅ Performance-critical native code  
✅ System-level operations  
✅ Replacing existing JNI code  

### Consider Alternatives For:

❌ Simple tasks that don't need native code  
❌ When pure Java solutions exist  
❌ Cross-platform portability is critical  

## Migration from JNI

1. **Identify native calls** in your JNI code
2. **Define FunctionDescriptors** for each native function signature
3. **Replace** JNI calls with FFM method handle invocations
4. **Use Arenas** for memory management instead of manual allocation
5. **Remove** native C/C++ glue code
6. **Simplify build** by removing native compilation steps

## Conclusion

The FFM API in Java 21 represents a major improvement in native code integration:

- **Simpler**: Pure Java, no native code required
- **Safer**: Built-in bounds checking, arena-based memory management
- **Cleaner**: Method handles instead of JNI functions
- **Modern**: Designed for contemporary Java development

For applications requiring native library integration, the FFM API provides a much more developer-friendly experience while maintaining the performance characteristics needed for production use.

## Code Samples

See the complete implementations in our repository:
- [Java 21 FFM Examples](https://github.com/sps23/java-for-scala-devs/tree/main/java21/src/main/java/io/github/sps23/interview/preparation/ffm)
- [Scala 3 FFM Examples](https://github.com/sps23/java-for-scala-devs/tree/main/scala3/src/main/scala/io/github/sps23/interview/preparation/ffm)
- [Kotlin FFM Examples](https://github.com/sps23/java-for-scala-devs/tree/main/kotlin/src/main/kotlin/io/github/sps23/interview/preparation/ffm)

---

*This is part of our Java 21 Interview Preparation series. Check out the [full preparation plan](/interview/2025/11/25/java21-interview-preparation-plan.html) for more topics.*
