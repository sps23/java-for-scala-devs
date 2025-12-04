package io.github.sps23.typeclasses;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Show Typeclass Tests")
class ShowTest {

    @Nested
    @DisplayName("Version 1: Basic Implementation")
    class BasicImplementationTests {

        @Test
        @DisplayName("Should show integers correctly")
        void testIntegerShow() {
            Show<Integer> intShow = Show.forInteger();
            assertEquals("Int(42)", intShow.show(42));
            assertEquals("Int(-5)", intShow.show(-5));
        }

        @Test
        @DisplayName("Should show strings correctly")
        void testStringShow() {
            Show<String> stringShow = Show.forString();
            assertEquals("\"hello\"", stringShow.show("hello"));
            assertEquals("\"\"", stringShow.show(""));
        }

        @Test
        @DisplayName("Should show booleans correctly")
        void testBooleanShow() {
            Show<Boolean> boolShow = Show.forBoolean();
            assertEquals("True", boolShow.show(true));
            assertEquals("False", boolShow.show(false));
        }

        @Test
        @DisplayName("Should show lists correctly")
        void testListShow() {
            Show<List<Integer>> listShow = Show.forList(Show.forInteger());
            assertEquals("List(Int(1), Int(2), Int(3))", listShow.show(List.of(1, 2, 3)));
            assertEquals("List()", listShow.show(List.of()));
        }

        @Test
        @DisplayName("Should show maps correctly")
        void testMapShow() {
            Show<Map<String, Integer>> mapShow = Show.forMap(Show.forString(), Show.forInteger());
            String result = mapShow.show(Map.of("a", 1));
            assertEquals("Map(\"a\" -> Int(1))", result);
        }

        @Test
        @DisplayName("Should handle null values")
        void testNullHandling() {
            Show<String> show = Show.forAny();
            assertEquals("null", show.show(null));
        }
    }

    @Nested
    @DisplayName("Version 2: Registry-Based Lookup")
    class RegistryTests {

        @Test
        @DisplayName("Should retrieve registered instances")
        void testRegistryLookup() {
            Show<Integer> intShow = Show.Registry.summon(Integer.class);
            assertEquals("Int(42)", intShow.show(42));

            Show<String> stringShow = Show.Registry.summon(String.class);
            assertEquals("\"hello\"", stringShow.show("hello"));
        }

        @Test
        @DisplayName("Should register custom instances")
        void testCustomRegistration() {
            record Person(String name, int age) {
            }

            Show.Registry.register(Person.class, p -> "Person(" + p.name() + ", " + p.age() + ")");

            Show<Person> personShow = Show.Registry.summon(Person.class);
            assertEquals("Person(Alice, 30)", personShow.show(new Person("Alice", 30)));
        }

        @Test
        @DisplayName("Should fallback to toString for unregistered types")
        void testFallbackBehavior() {
            record Unknown(int x) {
            }

            Show<Unknown> show = Show.Registry.summon(Unknown.class);
            Unknown u = new Unknown(42);
            assertEquals(u.toString(), show.show(u));
        }
    }

    @Nested
    @DisplayName("Version 4: Extension Methods")
    class ShowableTests {

        @Test
        @DisplayName("Should wrap and show values")
        void testShowableBasic() {
            String result = Showable.of(42, Show.forInteger()).show();
            assertEquals("Int(42)", result);
        }

        @Test
        @DisplayName("Should support mapping")
        void testShowableMap() {
            String result = Showable.of(21, Show.forInteger()).map(i -> i * 2, Show.forInteger())
                    .show();
            assertEquals("Int(42)", result);
        }

        @Test
        @DisplayName("Should work with registry lookup")
        void testShowableWithRegistry() {
            String result = Showable.of(42, Integer.class).show();
            assertEquals("Int(42)", result);
        }
    }

    @Nested
    @DisplayName("Version 5: Auto-Derivation")
    class DerivationTests {

        @Test
        @DisplayName("Should derive Show for simple records")
        void testSimpleRecordDerivation() {
            record Person(String name, int age) {
            }

            Show<Person> personShow = ShowDerivation.deriveRecord();
            Person alice = new Person("Alice", 30);

            String result = personShow.show(alice);
            assertTrue(result.contains("Person"));
            assertTrue(result.contains("name = \"Alice\""));
            assertTrue(result.contains("age = 30"));
        }

        @Test
        @DisplayName("Should derive Show for nested records")
        void testNestedRecordDerivation() {
            record Address(String street, String city) {
            }
            record Person(String name, Address address) {
            }

            Show<Person> personShow = ShowDerivation.deriveRecord();
            Person bob = new Person("Bob", new Address("123 Main St", "Springfield"));

            String result = personShow.show(bob);
            assertTrue(result.contains("Person"));
            assertTrue(result.contains("name = \"Bob\""));
            assertTrue(result.contains("Address"));
            assertTrue(result.contains("street = \"123 Main St\""));
            assertTrue(result.contains("city = \"Springfield\""));
        }

        @Test
        @DisplayName("Should handle null in record fields")
        void testNullInRecordFields() {
            record Person(String name, Integer age) {
            }

            Show<Person> personShow = ShowDerivation.deriveRecord();
            Person person = new Person(null, null);

            String result = personShow.show(person);
            assertTrue(result.contains("name = null"));
            assertTrue(result.contains("age = null"));
        }
    }
}
