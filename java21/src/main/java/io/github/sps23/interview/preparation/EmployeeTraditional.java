package io.github.sps23.interview.preparation;

import java.util.Objects;

/**
 * Traditional Java immutable class (pre-Java 16 style).
 *
 * <p>
 * This class demonstrates the verbose boilerplate required before Java Records.
 * Compare this ~150 lines of code with the concise Employee record (~30 lines).
 *
 * <p>
 * To create an immutable class in Java 8, you needed to:
 * <ol>
 * <li>Declare the class as final</li>
 * <li>Make all fields private and final</li>
 * <li>Provide a constructor with validation</li>
 * <li>Provide getter methods (no setters)</li>
 * <li>Override equals(), hashCode(), and toString()</li>
 * </ol>
 */
public final class EmployeeTraditional {

    private final long id;
    private final String name;
    private final String email;
    private final String department;
    private final double salary;

    /**
     * Constructs a new EmployeeTraditional with validation.
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
     * @throws IllegalArgumentException
     *             if any validation fails
     * @throws NullPointerException
     *             if name, email, or department is null
     */
    public EmployeeTraditional(long id, String name, String email, String department,
            double salary) {
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

        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.salary = salary;
    }

    // Getter methods (required for immutable class)

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public double getSalary() {
        return salary;
    }

    // equals(), hashCode(), and toString() must be manually implemented

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EmployeeTraditional that = (EmployeeTraditional) o;
        return id == that.id && Double.compare(salary, that.salary) == 0
                && Objects.equals(name, that.name) && Objects.equals(email, that.email)
                && Objects.equals(department, that.department);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, email, department, salary);
    }

    @Override
    public String toString() {
        return "EmployeeTraditional[" + "id=" + id + ", name=" + name + ", email=" + email
                + ", department=" + department + ", salary=" + salary + ']';
    }

    /**
     * Example of adding a custom method.
     *
     * @return formatted employee information
     */
    public String toFormattedString() {
        return String.format("Employee #%d: %s (%s) - %s - $%.2f", id, name, email, department,
                salary);
    }

    /**
     * Static factory method.
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
     * @return new EmployeeTraditional instance
     */
    public static EmployeeTraditional of(long id, String name, String email, String department,
            double salary) {
        return new EmployeeTraditional(id, name, email, department, salary);
    }
}
