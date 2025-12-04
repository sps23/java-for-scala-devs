package io.github.sps23.typeclasses

/** The Show typeclass in Scala 3.
  *
  * This demonstrates the typeclass pattern using Scala 3's new given/using syntax.
  */
trait Show[A]:
  def show(a: A): String

object Show:

  // ============================================================================
  // Extension methods for types that have a Show instance
  // ============================================================================

  extension [A](a: A)(using s: Show[A])
    def show: String    = s.show(a)
    def printShow: Unit = println(s.show(a))

  // ============================================================================
  // Type class instances for common types (using 'given')
  // ============================================================================

  given Show[Int] with
    def show(a: Int): String = s"Int($a)"

  given Show[String] with
    def show(a: String): String = s""""$a""""

  given Show[Double] with
    def show(a: Double): String = s"Double($a)"

  given Show[Boolean] with
    def show(a: Boolean): String = if a then "True" else "False"

  // Generic Show for any type (fallback)
  def forAny[A]: Show[A] = new Show[A]:
    def show(a: A): String = if a == null then "null" else a.toString

  // Show for Lists - compiler can derive this automatically
  given [A](using elementShow: Show[A]): Show[List[A]] with
    def show(list: List[A]): String =
      list.map(elementShow.show).mkString("List(", ", ", ")")

  // Show for Options
  given [A](using elementShow: Show[A]): Show[Option[A]] with
    def show(opt: Option[A]): String = opt match
      case Some(value) => s"Some(${elementShow.show(value)})"
      case None        => "None"

  // Show for Maps
  given [K, V](using keyShow: Show[K], valueShow: Show[V]): Show[Map[K, V]] with
    def show(map: Map[K, V]): String =
      map
        .map { case (k, v) => s"${keyShow.show(k)} -> ${valueShow.show(v)}" }
        .mkString("Map(", ", ", ")")

  // ============================================================================
  // Utility methods with context parameters
  // ============================================================================

  /** Print a value using its Show instance. */
  def print[A](a: A)(using s: Show[A]): Unit =
    println(s.show(a))

  /** Get string representation using Show instance. */
  def showValue[A](a: A)(using s: Show[A]): String =
    s.show(a)

  /** Summon a Show instance explicitly. */
  def apply[A](using s: Show[A]): Show[A] = s
