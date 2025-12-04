package io.github.sps23.typeclasses

/** Examples demonstrating the typeclass pattern in Scala 3.
  *
  * This corresponds to the blog post "Typeclasses in Java: Because Why Should Scala Have All the
  * Fun?"
  */
@main def showExamples(): Unit =

  import Show.*
  import Show.given // Import all given instances

  println("=== Scala 3 Typeclass Examples ===\n")

  // ============================================================================
  // Basic usage with context parameters (using)
  // ============================================================================

  println("--- Basic Usage ---")

  // Compiler finds the given Show[Int] instance
  Show.print(42) // Int(42)

  // Compiler finds the given Show[String] instance
  Show.print("hello") // "hello"

  // Compiler finds the given Show[Boolean] instance
  Show.print(true) // True

  // Using extension method syntax (Scala 3's cleaner syntax)
  42.printShow      // Int(42)
  "world".printShow // "world"

  println()

  // ============================================================================
  // Collections with automatic derivation
  // ============================================================================

  println("--- Collections ---")

  // Compiler automatically derives Show[List[Int]]
  val numbers = List(1, 2, 3)
  Show.print(numbers) // List(Int(1), Int(2), Int(3))
  numbers.printShow   // List(Int(1), Int(2), Int(3))

  // Nested collections work too!
  val nested = List(List(1, 2), List(3, 4))
  Show.print(nested) // List(List(Int(1), Int(2)), List(Int(3), Int(4)))

  // Maps
  val scores = Map("Alice" -> 95, "Bob" -> 87)
  Show.print(scores) // Map("Alice" -> Int(95), "Bob" -> Int(87))

  // Options
  val maybeValue: Option[Int] = Some(42)
  Show.print(maybeValue)        // Some(Int(42))
  Show.print(None: Option[Int]) // None

  println()

  // ============================================================================
  // Custom types with given instances
  // ============================================================================

  println("--- Custom Types ---")

  case class Person(name: String, age: Int)

  // Define Show instance for Person using 'given'
  given Show[Person] with
    def show(p: Person): String = s"Person(${p.name}, ${p.age})"

  val alice = Person("Alice", 30)
  Show.print(alice) // Person(Alice, 30)
  alice.printShow   // Person(Alice, 30)

  // Show for List[Person] is automatically derived!
  val people = List(Person("Alice", 30), Person("Bob", 35))
  Show.print(people) // List(Person(Alice, 30), Person(Bob, 35))

  println()

  // ============================================================================
  // Context bounds with 'using' (cleaner than Scala 2)
  // ============================================================================

  println("--- Context Bounds ---")

  // Function using context bound
  def printAll[A: Show](items: List[A]): Unit =
    items.foreach(item => Show.print(item))

  // Same as above but more explicit with 'using'
  def printAllExplicit[A](items: List[A])(using show: Show[A]): Unit =
    items.foreach(item => println(show.show(item)))

  printAll(List(1, 2, 3))
  printAll(List("a", "b", "c"))
  printAll(List(Person("Charlie", 40), Person("Diana", 28)))

  println()

  // ============================================================================
  // Summoning instances with 'summon'
  // ============================================================================

  println("--- Summoning Instances ---")

  // Scala 3's way to get a context instance (cleaner than implicitly)
  val intShowInstance = summon[Show[Int]]
  println(intShowInstance.show(99)) // Int(99)

  // Or using Show.apply
  val stringShowInstance = Show[String]
  println(stringShowInstance.show("scala")) // "scala"

  println()

  // ============================================================================
  // Automatic derivation for case classes (Scala 3 feature)
  // ============================================================================

  println("--- Automatic Derivation ---")

  // Note: Full automatic derivation requires deriving clauses and Mirror
  // This is a simplified example showing the concept

  case class Employee(id: Int, name: String, salary: Double)

  // Manually defining for demonstration (automatic derivation would use derives)
  given Show[Employee] with
    def show(e: Employee): String =
      s"Employee(id = ${e.id}, name = ${e.name.show}, salary = ${e.salary})"

  val emp = Employee(1, "Alice", 75000.0)
  emp.printShow // Employee(id = 1, name = "Alice", salary = 75000.0)
