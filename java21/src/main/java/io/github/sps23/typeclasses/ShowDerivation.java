package io.github.sps23.typeclasses;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Auto-derivation utilities for Record types (Version 5 from blog post).
 *
 * This uses reflection to automatically generate Show instances for any Record
 * type.
 */
public class ShowDerivation {

    /**
     * Derive a Show instance for any Record type.
     *
     * Example: record Person(String name, int age) {} Show<Person> show =
     * ShowDerivation.deriveRecord(); show.show(new Person("Alice", 30)); //
     * "Person(name = "Alice", age = 30)"
     */
    public static <T extends Record> Show<T> deriveRecord() {
        return value -> {
            if (value == null)
                return "null";

            Class<?> recordClass = value.getClass();
            RecordComponent[] components = recordClass.getRecordComponents();

            String fields = Arrays.stream(components).map(comp -> {
                try {
                    Object fieldValue = comp.getAccessor().invoke(value);
                    return comp.getName() + " = " + showField(fieldValue);
                } catch (Exception e) {
                    return comp.getName() + " = <error>";
                }
            }).collect(Collectors.joining(", "));

            return recordClass.getSimpleName() + "(" + fields + ")";
        };
    }

    /**
     * Helper to show individual field values, with special handling for common
     * types.
     */
    private static String showField(Object value) {
        if (value == null)
            return "null";
        if (value instanceof String)
            return "\"" + value + "\"";
        if (value instanceof Record) {
            Show<Record> recordShow = deriveRecord();
            return recordShow.show((Record) value);
        }
        return value.toString();
    }

    /**
     * Derive Show instances for nested records automatically.
     */
    public static <T extends Record> Show<T> deriveRecordDeep() {
        return deriveRecord();
    }
}
