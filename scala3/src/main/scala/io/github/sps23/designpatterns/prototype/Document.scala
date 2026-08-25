package io.github.sps23.designpatterns.prototype

/** Prototype pattern in Scala 3.
  *
  * Scala 3 keeps the case-class `copy()` idiom. Immutable collections mean that creating a
  * prototype is both shallow (it reuses the existing list reference) and safe (the list can never
  * be mutated in place).
  */
case class Document(
    title: String,
    author: String,
    sections: List[String] = Nil
):

  /** Returns a new document with an additional section. */
  def addSection(section: String): Document =
    copy(sections = sections :+ section)
