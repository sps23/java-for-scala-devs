package io.github.sps23.interview.preparation;

import java.util.Objects;

/**
 * An immutable Employee record demonstrating Java Records (Java 16+).
 *
 * <p>
 * Records provide a compact way to declare immutable data classes with:
 * <ul>
 * <li>Automatically generated constructor</li>
 * <li>Accessor methods (name(), email(), department(), salary())</li>
 * <li>equals(), hashCode(), and toString() implementations</li>
 * </ul>
 *
 * <p>
 * This example showcases the use of a compact constructor for validation, which
 * is a key feature distinguishing records from simple data holders.
 *
 * @param id
 *            unique employee identifier, must be positive
 * @param name
 *            employee's full name, must not be null or blank
 * @param email
 *            employee's email address, must contain '@'
 * @param department
 *            department name, must not be null or blank
 * @param salary
 *            employee's salary, must be non-negative
 */
public record Employee(long id, String name, String email, String department, double salary) {

    /**
     * Compact constructor for validation.
     *
     * <p>
     * In a compact constructor, parameter assignments to record components happen
     * automatically at the end. We only need to write validation logic.
     *
     * <p>
     * Compared to Scala case classes, which typically use require() for validation,
     * Java records use compact constructors that throw exceptions for invalid data.
     */
    public Employee {
        // Validate id
        if (id <= 0) {
            throw new IllegalArgumentException("Employee ID must be positive, got: " + id);
        }

        // Validate name
        Objects.requireNonNull(name, "Employee name cannot be null");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Employee name cannot be blank");
        }

        // Validate email
        Objects.requireNonNull(email, "Employee email cannot be null");
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }

        // Validate department
        Objects.requireNonNull(department, "Department cannot be null");
        if (department.isBlank()) {
            throw new IllegalArgumentException("Department cannot be blank");
        }

        // Validate salary
        if (salary < 0) {
            throw new IllegalArgumentException("Salary cannot be negative, got: " + salary);
        }
    }

    /**
     * Example of adding a custom method to a record. Records can have instance
     * methods just like regular classes.
     *
     * @return formatted employee information
     */
    public String toFormattedString() {
        return String.format("Employee #%d: %s (%s) - %s - $%.2f", id, name, email, department,
                salary);
    }

    /**
     * Static factory method demonstrating alternative construction pattern.
     *
     * @param id
     *            employee ID
     * @param name
     *            employee name
     * @param email
     *            employee email
     * @param department
     *            employee department
     * @param salary
     *            employee salary
     * @return new Employee instance
     */
    public static Employee of(long id, String name, String email, String department,
            double salary) {
        return new Employee(id, name, email, department, salary);
    }
}
