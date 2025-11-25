package io.github.sps23;

import java.util.List;

/**
 * Example Java 21 class demonstrating new language features.
 */
public class HelloJava21 {

    public static void main(String[] args) {
        System.out.println(greeting());
        
        // Pattern matching example
        Object obj = "Hello";
        String result = switch (obj) {
            case String s -> "String: " + s;
            case Integer i -> "Integer: " + i;
            default -> "Unknown type";
        };
        System.out.println(result);
    }

    public static String greeting() {
        return "Hello from Java 21!";
    }

    // Record example
    public record Person(String name, int age) {}

    // Sealed interface example
    public sealed interface Shape permits Circle, Rectangle {}
    
    public record Circle(double radius) implements Shape {}
    
    public record Rectangle(double width, double height) implements Shape {}

    // Pattern matching with records
    public static double area(Shape shape) {
        return switch (shape) {
            case Circle(double r) -> Math.PI * r * r;
            case Rectangle(double w, double h) -> w * h;
        };
    }

    // Example with virtual threads
    public static void virtualThreadExample() throws InterruptedException {
        Thread.startVirtualThread(() -> {
            System.out.println("Running in virtual thread!");
        }).join();
    }
}
