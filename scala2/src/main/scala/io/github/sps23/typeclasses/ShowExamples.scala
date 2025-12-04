package io.github.sps23.typeclasses

/** Examples demonstrating the typeclass pattern in Scala 2.
  *
  * This corresponds to the blog post "Typeclasses in Java: Because Why Should Scala Have All the
  * Fun?"
  */
object ShowExamples extends App {

  import Show._

  println("=== Scala 2 Typeclass Examples ===\n")

  // ============================================================================
  // Basic usage with implicit resolution
  // ============================================================================

  println("--- Basic Usage ---")

  // Compiler finds the implicit Show[Int] instance
  Show.print(42) // Int(42)

  // Compiler finds the implicit Show[String] instance
  Show.print("hello") // "hello"

  // Compiler finds the implicit Show[Boolean] instance
  Show.print(true) // True

  // Using extension method syntax
  42.print      // Int(42)
  "world".print // "world"

  println()

  // ============================================================================
  // Collections with automatic derivation
  // ============================================================================

  println("--- Collections ---")

  // Compiler automatically derives Show[List[Int]]
  val numbers = List(1, 2, 3)
  Show.print(numbers) // List(Int(1), Int(2), Int(3))
  numbers.print       // List(Int(1), Int(2), Int(3))

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
  // Custom types with implicit instances
  // ============================================================================

  println("--- Custom Types ---")

  case class Person(name: String, age: Int)

  // Define Show instance for Person
  implicit val personShow: Show[Person] = new Show[Person] {
    def show(p: Person): String = s"Person(${p.name}, ${p.age})"
  }

  val alice = Person("Alice", 30)
  Show.print(alice) // Person(Alice, 30)
  alice.print       // Person(Alice, 30)

  // Show for List[Person] is automatically derived!
  val people = List(Person("Alice", 30), Person("Bob", 35))
  Show.print(people) // List(Person(Alice, 30), Person(Bob, 35))

  println()

  // ============================================================================
  // Context bounds syntax
  // ============================================================================

  println("--- Context Bounds ---")

  // Function using context bound (syntactic sugar)
  def printAll[A: Show](items: List[A]): Unit =
    items.foreach(item => Show.print(item))

  // Same as above but more explicit
  def printAllExplicit[A](items: List[A])(implicit show: Show[A]): Unit =
    items.foreach(item => println(show.show(item)))

  printAll(List(1, 2, 3))
  printAll(List("a", "b", "c"))
  printAll(List(Person("Charlie", 40), Person("Diana", 28)))

  println()

  // ============================================================================
  // Summoning instances
  // ============================================================================

  println("--- Summoning Instances ---")

  // Get the implicit Show instance
  val intShowInstance = implicitly[Show[Int]]
  println(intShowInstance.show(99)) // Int(99)

  // Or using Show.apply
  val stringShowInstance = Show[String]
  println(stringShowInstance.show("scala")) // "scala"
}
