package io.github.sps23

/**
 * Example Scala 3 class demonstrating new syntax features.
 */
@main def runHelloScala3(): Unit =
  println(HelloScala3.greeting)

object HelloScala3:
  def greeting: String = "Hello from Scala 3!"

  // Example of Scala 3 specific features
  enum Color:
    case Red, Green, Blue

  // Extension methods
  extension (s: String)
    def exclaim: String = s + "!"

  // Given instances (replacing implicits)
  given Ordering[Color] = Ordering.by(_.ordinal)
