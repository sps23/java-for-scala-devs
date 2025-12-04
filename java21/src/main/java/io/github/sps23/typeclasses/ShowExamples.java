package io.github.sps23.typeclasses;

import java.util.List;
import java.util.Map;

/**
 * Examples demonstrating different versions of the typeclass pattern in Java.
 *
 * This corresponds to the blog post "Typeclasses in Java: Because Why Should
 * Scala Have All the Fun?"
 */
public class ShowExamples {

    public static void main(String[] args) {
        version1BasicImplementation();
        System.out.println();

        version2RegistryBased();
        System.out.println();

        version4ExtensionMethods();
        System.out.println();

        version5AutoDerivation();
    }

    /**
     * Version 1: Basic typeclass implementation with explicit instance passing.
     */
    private static void version1BasicImplementation() {
        System.out.println("=== Version 1: Basic Implementation ===");

        // Create Show instances
        Show<Integer> intShow = Show.forInteger();
        Show<String> stringShow = Show.forString();

        // Use them explicitly
        Show.print(42, intShow); // Int(42)
        Show.print("hello", stringShow); // "hello"

        // Inline lambda
        Show.print(3.14, d -> "Double(" + d + ")"); // Double(3.14)

        // Show for collections
        Show<List<Integer>> listShow = Show.forList(intShow);
        Show.print(List.of(1, 2, 3), listShow); // List(Int(1), Int(2), Int(3))

        // Show for maps
        Show<Map<String, Integer>> mapShow = Show.forMap(stringShow, intShow);
        Show.print(Map.of("a", 1, "b", 2), mapShow); // Map("a" -> Int(1), "b" -> Int(2))
    }

    /**
     * Version 2: Registry-based lookup (simulates implicit resolution).
     */
    private static void version2RegistryBased() {
        System.out.println("=== Version 2: Registry-Based Lookup ===");

        // Use registered instances
        Show.print(42, Show.Registry.summon(Integer.class)); // Int(42)
        Show.print("hello", Show.Registry.summon(String.class)); // "hello"
        Show.print(true, Show.Registry.summon(Boolean.class)); // True

        // Register custom instance
        record Person(String name, int age) {
        }
        Show.Registry.register(Person.class, p -> "Person(" + p.name() + ", " + p.age() + ")");

        Person alice = new Person("Alice", 30);
        Show.print(alice, Show.Registry.summon(Person.class)); // Person(Alice, 30)
    }

    /**
     * Version 4: Extension-method-like syntax with Showable wrapper.
     */
    private static void version4ExtensionMethods() {
        System.out.println("=== Version 4: Extension Methods ===");

        // Fluent API
        Showable.of(42, Show.forInteger()).inspect() // Inspecting: Int(42)
                .map(i -> i * 2, Show.forInteger()).print(); // Int(84)

        // With registry lookup
        Showable.of("hello", String.class).inspect() // Inspecting: "hello"
                .map(String::toUpperCase, Show.forString()).print(); // "HELLO"
    }

    /**
     * Version 5: Auto-derivation for Record types.
     */
    private static void version5AutoDerivation() {
        System.out.println("=== Version 5: Auto-Derivation for Records ===");

        // Define record types
        record Person(String name, int age) {
        }
        record Address(String street, String city) {
        }
        record Employee(Person person, Address address, double salary) {
        }

        // Derive Show instances automatically
        Show<Person> personShow = ShowDerivation.deriveRecord();
        Show<Address> addressShow = ShowDerivation.deriveRecord();
        Show<Employee> employeeShow = ShowDerivation.deriveRecord();

        // Use them
        Person bob = new Person("Bob", 35);
        Address addr = new Address("123 Main St", "Springfield");
        Employee emp = new Employee(bob, addr, 75000.0);

        personShow.show(bob); // Person(name = "Bob", age = 35)
        Show.print(bob, personShow);

        addressShow.show(addr); // Address(street = "123 Main St", city = "Springfield")
        Show.print(addr, addressShow);

        employeeShow.show(emp); // Nested records work too!
        Show.print(emp, employeeShow);
    }
}
