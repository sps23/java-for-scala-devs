---
layout: post
title: "Foreign Function and Memory API in Java 21"
description: "Integrate with native C libraries using Java 21's FFM API - a modern JNI alternative with Arena memory management, MemorySegment, and Linker examples."
date: 2025-11-29 19:00:00 +0000
updated: 2026-08-29 14:00:00 +0000
categories: [interview]
tags: [java, java21, scala, kotlin, ffm, native, jni, interview-preparation]
---

Java 21 introduces the Foreign Function and Memory (FFM) API as a stable feature, providing a modern alternative to JNI for native code integration. In this post, we'll explore how to use the FFM API to integrate with native C libraries for tasks like compression or cryptography.

## The Problem: Native Library Integration

Many applications need to interact with native libraries for performance-critical operations, system-level functionality, or leveraging existing C/C++ codebases. Traditional approaches using JNI (Java Native Interface) are:

<div class="table-wrapper" markdown="1">

| Challenge | Impact |
|-----------|--------|
| Complexity | Requires writing C/C++ glue code |
| Build Process | Need native compilers and build tools |
| Safety | Manual memory management, no bounds checking |
| Portability | Platform-specific binaries |
| Debugging | Difficult to trace issues across JNI boundary |

</div>

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

### FFM API Example

<div class="code-tabs" data-tabs-id="ffm-api-example">
<div class="tab-buttons">
<button class="tab-button active" data-tab="java" data-lang="Java 21">Java 21</button>
<button class="tab-button" data-tab="kotlin" data-lang="Kotlin">Kotlin</button>
<button class="tab-button" data-tab="scala3" data-lang="Scala 3">Scala 3</button>
</div>
<div class="tab-content active" data-tab="java">
<div class="language-java highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="kd">public</span> <span class="kd">class</span> <span class="nc">NativeLibFFM</span> <span class="o">{</span>
    <span class="kd">private</span> <span class="kd">static</span> <span class="kd">final</span> <span class="nc">Linker</span> <span class="n">LINKER</span> <span class="o">=</span> <span class="nc">Linker</span><span class="o">.</span><span class="na">nativeLinker</span><span class="o">();</span>
    <span class="kd">private</span> <span class="kd">static</span> <span class="kd">final</span> <span class="nc">SymbolLookup</span> <span class="n">STDLIB</span> <span class="o">=</span> <span class="n">LINKER</span><span class="o">.</span><span class="na">defaultLookup</span><span class="o">();</span>

    <span class="kd">public</span> <span class="kd">static</span> <span class="kt">long</span> <span class="nf">strlen</span><span class="o">(</span><span class="nc">String</span> <span class="n">s</span><span class="o">)</span> <span class="kd">throws</span> <span class="nc">Throwable</span> <span class="o">{</span>
        <span class="nc">MemorySegment</span> <span class="n">strlenSymbol</span> <span class="o">=</span> <span class="n">STDLIB</span><span class="o">.</span><span class="na">find</span><span class="o">(</span><span class="s">"strlen"</span><span class="o">)</span>
                <span class="o">.</span><span class="na">orElseThrow</span><span class="o">(()</span> <span class="o">-&gt;</span> <span class="k">new</span> <span class="nc">RuntimeException</span><span class="o">(</span><span class="s">"strlen not found"</span><span class="o">));</span>

        <span class="nc">FunctionDescriptor</span> <span class="n">descriptor</span> <span class="o">=</span> <span class="nc">FunctionDescriptor</span><span class="o">.</span><span class="na">of</span><span class="o">(</span>
                <span class="nc">ValueLayout</span><span class="o">.</span><span class="na">JAVA_LONG</span><span class="o">,</span>
                <span class="nc">ValueLayout</span><span class="o">.</span><span class="na">ADDRESS</span>
        <span class="o">);</span>

        <span class="nc">MethodHandle</span> <span class="n">strlen</span> <span class="o">=</span> <span class="n">LINKER</span><span class="o">.</span><span class="na">downcallHandle</span><span class="o">(</span><span class="n">strlenSymbol</span><span class="o">,</span> <span class="n">descriptor</span><span class="o">);</span>

        <span class="k">try</span> <span class="o">(</span><span class="nc">Arena</span> <span class="n">arena</span> <span class="o">=</span> <span class="nc">Arena</span><span class="o">.</span><span class="na">ofConfined</span><span class="o">())</span> <span class="o">{</span>
            <span class="nc">MemorySegment</span> <span class="n">nativeString</span> <span class="o">=</span> <span class="n">arena</span><span class="o">.</span><span class="na">allocateUtf8String</span><span class="o">(</span><span class="n">s</span><span class="o">);</span>
            <span class="k">return</span> <span class="o">(</span><span class="kt">long</span><span class="o">)</span> <span class="n">strlen</span><span class="o">.</span><span class="na">invokeExact</span><span class="o">(</span><span class="n">nativeString</span><span class="o">);</span>
        <span class="o">}</span>
    <span class="o">}</span>
<span class="o">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="kotlin">
<div class="language-kotlin highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">object</span> <span class="nc">NativeLibFFM</span> <span class="p">{</span>
    <span class="k">private</span> <span class="k">val</span> <span class="py">linker</span><span class="p">:</span> <span class="nc">Linker</span> <span class="p">=</span> <span class="nc">Linker</span><span class="p">.</span><span class="n">nativeLinker</span><span class="p">()</span>
    <span class="k">private</span> <span class="k">val</span> <span class="py">stdlib</span><span class="p">:</span> <span class="nc">SymbolLookup</span> <span class="p">=</span> <span class="n">linker</span><span class="p">.</span><span class="n">defaultLookup</span><span class="p">()</span>

    <span class="k">fun</span> <span class="nf">strlen</span><span class="p">(</span><span class="n">s</span><span class="p">:</span> <span class="nc">String</span><span class="p">):</span> <span class="nc">Long</span> <span class="p">{</span>
        <span class="k">val</span> <span class="py">strlenSymbol</span> <span class="p">=</span> <span class="n">stdlib</span><span class="p">.</span><span class="nf">find</span><span class="p">(</span><span class="s">"strlen"</span><span class="p">)</span>
            <span class="p">.</span><span class="nf">orElseThrow</span> <span class="p">{</span> <span class="nc">RuntimeException</span><span class="p">(</span><span class="s">"strlen not found"</span><span class="p">)</span> <span class="p">}</span>

        <span class="k">val</span> <span class="py">descriptor</span> <span class="p">=</span> <span class="nc">FunctionDescriptor</span><span class="p">.</span><span class="nf">of</span><span class="p">(</span>
            <span class="nc">ValueLayout</span><span class="p">.</span><span class="n">JAVA_LONG</span><span class="p">,</span>
            <span class="nc">ValueLayout</span><span class="p">.</span><span class="n">ADDRESS</span>
        <span class="p">)</span>

        <span class="k">val</span> <span class="py">strlenHandle</span> <span class="p">=</span> <span class="n">linker</span><span class="p">.</span><span class="nf">downcallHandle</span><span class="p">(</span><span class="n">strlenSymbol</span><span class="p">,</span> <span class="n">descriptor</span><span class="p">)</span>

        <span class="k">return</span> <span class="nc">Arena</span><span class="p">.</span><span class="nf">ofConfined</span><span class="p">().</span><span class="nf">use</span> <span class="p">{</span> <span class="n">arena</span> <span class="p">-&gt;</span>
            <span class="k">val</span> <span class="py">nativeString</span> <span class="p">=</span> <span class="n">arena</span><span class="p">.</span><span class="nf">allocateUtf8String</span><span class="p">(</span><span class="n">s</span><span class="p">)</span>
            <span class="n">strlenHandle</span><span class="p">.</span><span class="nf">invokeExact</span><span class="p">(</span><span class="n">nativeString</span><span class="p">)</span> <span class="k">as</span> <span class="nc">Long</span>
        <span class="p">}</span>
    <span class="p">}</span>
<span class="p">}</span>
</code></pre></div></div>
</div>
<div class="tab-content" data-tab="scala3">
<div class="language-scala highlighter-rouge"><div class="highlight"><pre class="highlight"><code><span class="k">object</span> <span class="nc">NativeLibFFM</span><span class="k">:</span>
  <span class="k">private</span> <span class="k">val</span> <span class="nv">linker</span><span class="k">:</span> <span class="kt">Linker</span> <span class="o">=</span> <span class="nc">Linker</span><span class="o">.</span><span class="py">nativeLinker</span><span class="o">()</span>
  <span class="k">private</span> <span class="k">val</span> <span class="nv">stdlib</span><span class="k">:</span> <span class="kt">SymbolLookup</span> <span class="o">=</span> <span class="n">linker</span><span class="o">.</span><span class="py">defaultLookup</span><span class="o">()</span>

  <span class="k">def</span> <span class="nf">strlen</span><span class="o">(</span><span class="n">s</span><span class="k">:</span> <span class="kt">String</span><span class="o">)</span><span class="k">:</span> <span class="kt">Long</span> <span class="o">=</span>
    <span class="k">val</span> <span class="nv">strlenSymbol</span> <span class="k">=</span> <span class="n">stdlib</span><span class="o">.</span><span class="py">find</span><span class="o">(</span><span class="s">"strlen"</span><span class="o">)</span>
      <span class="o">.</span><span class="py">orElseThrow</span><span class="o">(()</span> <span class="k">=&gt;</span> <span class="nc">RuntimeException</span><span class="o">(</span><span class="s">"strlen not found"</span><span class="o">))</span>

    <span class="k">val</span> <span class="nv">descriptor</span> <span class="k">=</span> <span class="nc">FunctionDescriptor</span><span class="o">.</span><span class="py">of</span><span class="o">(</span>
      <span class="nc">ValueLayout</span><span class="o">.</span><span class="py">JAVA_LONG</span><span class="o">,</span>
      <span class="nc">ValueLayout</span><span class="o">.</span><span class="py">ADDRESS</span>
    <span class="o">)</span>

    <span class="k">val</span> <span class="nv">strlenHandle</span> <span class="k">=</span> <span class="n">linker</span><span class="o">.</span><span class="py">downcallHandle</span><span class="o">(</span><span class="n">strlenSymbol</span><span class="o">,</span> <span class="n">descriptor</span><span class="o">)</span>

    <span class="nc">Using</span><span class="o">.</span><span class="py">resource</span><span class="o">(</span><span class="nc">Arena</span><span class="o">.</span><span class="py">ofConfined</span><span class="o">())</span> <span class="o">{</span> <span class="n">arena</span> <span class="k">=&gt;</span>
      <span class="k">val</span> <span class="nv">nativeString</span> <span class="k">=</span> <span class="n">arena</span><span class="o">.</span><span class="py">allocateUtf8String</span><span class="o">(</span><span class="n">s</span><span class="o">)</span>
      <span class="n">strlenHandle</span><span class="o">.</span><span class="py">invokeExact</span><span class="o">(</span><span class="n">nativeString</span><span class="o">).</span><span class="py">asInstanceOf</span><span class="o">[</span><span class="kt">Long</span><span class="o">]</span>
    <span class="o">}</span>
</code></pre></div></div>
</div>
</div>

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

<div class="table-wrapper" markdown="1">

| Feature | JNI | FFM API |
|---------|-----|---------|
| Bounds Checking | None | Built-in |
| Memory Management | Manual | Arena-based |
| Type Safety | Weak | Strong |
| Null Safety | Manual checks | Integrated |
| Thread Safety | Manual | Confined arenas |

</div>

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

<div class="table-wrapper" markdown="1">

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

</div>

## For Scala Developers

The FFM API provides similar benefits to what you get from effect systems:

<div class="table-wrapper" markdown="1">

| Feature | FFM API | ZIO/Cats Effect |
|---------|---------|-----------------|
| Resource Management | Arena | Scope/Resource |
| Memory Safety | Built-in | Not applicable |
| Error Handling | Exceptions | Effect types |
| Composability | Method handles | Monadic |

</div>

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

## Interview Q&A: Foreign Function and Memory API in Practice

<div class="faq-list">
  <details class="faq-item" open>
    <summary>
      <span>What is the Foreign Function and Memory API?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      It is Java's modern way to call native code and work with memory outside the JVM heap. It is a safer and cleaner replacement for older tools like JNI, which were harder to use and easier to get wrong. The goal is to make native interop more controlled and less fragile.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>Why is it better than JNI?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      JNI is powerful but often difficult to manage. The FFM API gives you a more explicit model for native memory and safer lifetimes. It reduces the risk of crashes caused by incorrect memory handling and makes the code easier to reason about.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>What is <code>MemorySegment</code> used for?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      It represents a region of memory that can be accessed safely from Java. Think of it as a controlled handle to native memory. That makes the data flow more explicit and helps keep memory operations under the program's control instead of letting them drift into unsafe territory.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>When would I use it in production?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      You would use it when you need to call a native library or work with data that lives outside the JVM heap, such as a C library or a system API. It is not a replacement for normal Java code; it is for the cases where Java needs to work with native systems directly.
    </div>
  </details>

  <details class="faq-item" open>
    <summary>
      <span>How should I explain it in an interview?</span>
      <span class="faq-toggle" aria-hidden="true"></span>
    </summary>
    <div class="faq-answer">
      A good answer is: “The FFM API is Java's modern native interop story. It lets Java call native code and manage external memory more safely than JNI, while keeping native access under clearer ownership and lifetime rules.” That shows both practical understanding and the reason the feature exists.
    </div>
  </details>
</div>


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

*This post is part of the [Java 21 Interview Preparation Guide - Your Roadmap to Success]({{ site.baseurl }}{% link _posts/2025-11-25-java21-interview-preparation-plan.md %}). Next related post: [Tricky Java Patterns That Everyone Uses]({{ site.baseurl }}{% link _posts/2025-12-03-tricky-java-patterns-everyone-uses.md %}).*
