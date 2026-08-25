package io.github.sps23.designpatterns.prototype

/**
 * Prototype pattern in Kotlin.
 *
 * Data classes provide a generated `copy()` method, which is Kotlin's idiomatic
 * prototype mechanism. Because the generated copy keeps the same mutable list
 * reference by default, the example also shows a `deepCopy()` helper for cases
 * where independent nested state is required.
 */
data class Document(
    val title: String,
    val author: String,
    val sections: MutableList<String> = mutableListOf(),
) {
    /** Creates a copy with an independent sections list. */
    fun deepCopy(): Document = copy(sections = sections.toMutableList())
}
