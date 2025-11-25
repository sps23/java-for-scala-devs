package io.github.sps23

/** Example Scala 2 class demonstrating basic functionality. */
object HelloScala2 {
  def main(args: Array[String]): Unit =
    println(greeting)

  def greeting: String = "Hello from Scala 2.13!"

  // Example of Scala 2 specific features
  def processItems(items: List[String]): List[String] =
    items.map(_.toUpperCase).filter(_.nonEmpty)
}
