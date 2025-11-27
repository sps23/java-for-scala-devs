package io.github.sps23.interview.preparation

/** Immutable Employee case class in Scala 3. Demonstrates idiomatic Scala for data modeling, with
  * validation in the body.
  */
case class Employee(
    id: Long,
    name: String,
    email: String,
    department: String,
    salary: Double
):
  require(id > 0, "Employee ID must be positive")
  require(name.nonEmpty, "Employee name cannot be empty")
  require(email.contains("@"), "Invalid email format")
  require(department.nonEmpty, "Department cannot be empty")
  require(salary >= 0, "Salary cannot be negative")

@main def runEmployeeExample(): Unit =
  val employee = Employee(1L, "Alice", "alice@example.com", "Engineering", 75000.0)
  println(employee.name)       // Alice
  println(employee.department) // Engineering
  println(employee)
