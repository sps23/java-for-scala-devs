package io.github.sps23.designpatterns.prototype

/** Prototype pattern in Scala 2.
  *
  * Scala's case classes already provide a built-in `copy()` method, which is the idiomatic way to
  * create a modified clone. Because the default `List` is immutable, shallow versus deep copying is
  * rarely a concern: `copy()` returns a new value and the original cannot be mutated through its
  * reference.
  */
case class Document(
    title: String,
    author: String,
    sections: List[String] = Nil
) {

  /** Returns a new document with an additional section. */
  def addSection(section: String): Document =
    copy(sections = sections :+ section)
}
