package io.github.sps23.trickypatterns;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Demonstrates the confusing Stream.of() vs Arrays.stream() behavior for
 * primitives.
 *
 * The tricky part: Stream.of(primitiveArray) treats the array as a single
 * element, while Arrays.stream(primitiveArray) creates a stream of the
 * elements.
 */
public class StreamPrimitiveArrayPattern {

    public static void main(String[] args) {
        demonstrateTrap();
        demonstrateCorrectWay();
        demonstrateObjectArrays();
        explainTheReason();
    }

    /**
     * Shows the trap that catches many developers
     */
    private static void demonstrateTrap() {
        System.out.println("=== THE TRAP: Stream.of() with primitive array ===");

        int[] numbers = {1, 2, 3, 4, 5};

        // ❌ This looks right but is WRONG!
        // Stream.of() treats the entire array as ONE element
        Stream<int[]> wrongStream = Stream.of(numbers);
        System.out.println("Stream.of(numbers).count() = " + wrongStream.count());
        System.out.println("Expected: 5, Got: 1 (array is single element!)");
        System.out.println();

        // Try to use it - compilation error or wrong result
        // wrongStream.forEach(System.out::println); // Prints: [I@hashcode
    }

    /**
     * The correct ways to stream primitive arrays
     */
    private static void demonstrateCorrectWay() {
        System.out.println("=== CORRECT WAYS ===");

        int[] numbers = {1, 2, 3, 4, 5};

        // ✅ Method 1: Use Arrays.stream()
        System.out.println("Arrays.stream(numbers):");
        IntStream intStream1 = Arrays.stream(numbers);
        intStream1.forEach(n -> System.out.print(n + " "));
        System.out.println("\n");

        // ✅ Method 2: Use IntStream.of()
        System.out.println("IntStream.of(numbers):");
        IntStream intStream2 = IntStream.of(numbers);
        intStream2.forEach(n -> System.out.print(n + " "));
        System.out.println("\n");

        // Note: Both return IntStream, not Stream<Integer>
        System.out.println("Both return IntStream (specialized for int primitives)");
        System.out.println();
    }

    /**
     * Shows that object arrays work differently
     */
    private static void demonstrateObjectArrays() {
        System.out.println("=== OBJECT ARRAYS (Different Behavior) ===");

        String[] words = {"Java", "Scala", "Kotlin"};

        // ✅ Stream.of() works fine with object arrays!
        Stream<String> stream1 = Stream.of(words);
        System.out.println("Stream.of(words).count() = " + stream1.count());

        // ✅ Arrays.stream() also works
        Stream<String> stream2 = Arrays.stream(words);
        System.out.println("Arrays.stream(words).count() = " + stream2.count());

        // Both are equivalent for object arrays
        System.out.println();
        System.out.println("For object arrays: Stream.of() and Arrays.stream() are equivalent");
        System.out.println("For primitive arrays: They behave DIFFERENTLY!");
        System.out.println();
    }

    /**
     * Explains why this happens
     */
    private static void explainTheReason() {
        System.out.println("=== WHY THIS HAPPENS ===");
        System.out.println();
        System.out.println("Stream.of() signature:");
        System.out.println("  static <T> Stream<T> of(T... values)");
        System.out.println("  For int[], T = int[] (array is one value)");
        System.out.println();
        System.out.println("Arrays.stream() has overloads:");
        System.out.println("  static IntStream stream(int[] array)");
        System.out.println("  static <T> Stream<T> stream(T[] array)");
        System.out.println("  Correctly handles both primitive and object arrays");
        System.out.println();
        System.out.println("Rule of thumb:");
        System.out.println("  Primitive arrays → Use Arrays.stream() or IntStream.of()");
        System.out.println("  Object arrays → Either Stream.of() or Arrays.stream()");
        System.out.println();
    }

    /**
     * Shows the complete picture with all primitive types
     */
    public static void demonstrateAllPrimitiveTypes() {
        System.out.println("=== ALL PRIMITIVE STREAM TYPES ===");

        // int[] → IntStream
        int[] ints = {1, 2, 3};
        IntStream intStream = Arrays.stream(ints);
        System.out.println("int[] → IntStream");

        // long[] → LongStream
        long[] longs = {1L, 2L, 3L};
        java.util.stream.LongStream longStream = Arrays.stream(longs);
        System.out.println("long[] → LongStream");

        // double[] → DoubleStream
        double[] doubles = {1.0, 2.0, 3.0};
        java.util.stream.DoubleStream doubleStream = Arrays.stream(doubles);
        System.out.println("double[] → DoubleStream");

        // Other primitives need boxing
        byte[] bytes = {1, 2, 3};
        // No ByteStream! Need to box or cast
        IntStream byteStream = IntStream.range(0, bytes.length).map(i -> bytes[i]);
        System.out.println("byte[] → Need to convert to IntStream");

        System.out.println();
        System.out.println("Only int, long, and double have specialized streams");
        System.out.println("Others need boxing or conversion to IntStream");
    }

    /**
     * Shows common mistake in practice
     */
    public static void demonstrateRealWorldMistake() {
        int[] scores = {85, 92, 78, 90, 88};

        // ❌ Wrong - trying to get average
        // Stream.of(scores).mapToInt(???).average(); // Can't compile!

        // ✅ Correct way
        double average = Arrays.stream(scores).average().orElse(0.0);
        System.out.println("Average score: " + average);

        // ✅ Also correct
        double average2 = IntStream.of(scores).average().orElse(0.0);
        System.out.println("Average score: " + average2);
    }
}
