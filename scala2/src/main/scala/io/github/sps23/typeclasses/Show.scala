package io.github.sps23.typeclasses

/** The Show typeclass in Scala 2.
  *
  * This demonstrates the typeclass pattern using Scala 2's implicit system.
  */
trait Show[A] {
  def show(a: A): String
}

object Show {

  // ============================================================================
  // Syntax for using Show instances
  // ============================================================================

  /** Extension methods for types that have a Show instance. */
  implicit class ShowOps[A](val a: A) extends AnyVal {
    def show(implicit s: Show[A]): String = s.show(a)
    def print(implicit s: Show[A]): Unit  = println(s.show(a))
  }

  // ============================================================================
  // Type class instances for common types
  // ============================================================================

  implicit val intShow: Show[Int] = new Show[Int] {
    def show(a: Int): String = s"Int($a)"
  }

  implicit val stringShow: Show[String] = new Show[String] {
    def show(a: String): String = s""""$a""""
  }

  implicit val doubleShow: Show[Double] = new Show[Double] {
    def show(a: Double): String = s"Double($a)"
  }

  implicit val booleanShow: Show[Boolean] = new Show[Boolean] {
    def show(a: Boolean): String = if (a) "True" else "False"
  }

  // Generic Show for any type (fallback)
  def forAny[A]: Show[A] = new Show[A] {
    def show(a: A): String = if (a == null) "null" else a.toString
  }

  // Show for Lists
  implicit def listShow[A](implicit elementShow: Show[A]): Show[List[A]] =
    new Show[List[A]] {
      def show(list: List[A]): String =
        list.map(elementShow.show).mkString("List(", ", ", ")")
    }

  // Show for Options
  implicit def optionShow[A](implicit elementShow: Show[A]): Show[Option[A]] =
    new Show[Option[A]] {
      def show(opt: Option[A]): String = opt match {
        case Some(value) => s"Some(${elementShow.show(value)})"
        case None        => "None"
      }
    }

  // Show for Maps
  implicit def mapShow[K, V](implicit keyShow: Show[K], valueShow: Show[V]): Show[Map[K, V]] =
    new Show[Map[K, V]] {
      def show(map: Map[K, V]): String =
        map
          .map { case (k, v) => s"${keyShow.show(k)} -> ${valueShow.show(v)}" }
          .mkString("Map(", ", ", ")")
    }

  // ============================================================================
  // Utility methods
  // ============================================================================

  /** Print a value using its Show instance. */
  def print[A](a: A)(implicit s: Show[A]): Unit =
    println(s.show(a))

  /** Get string representation using Show instance. */
  def showValue[A](a: A)(implicit s: Show[A]): String =
    s.show(a)

  /** Summon a Show instance (like Scala 3's summon). */
  def apply[A](implicit s: Show[A]): Show[A] = s
}
