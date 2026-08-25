package io.github.sps23.designpatterns.prototype

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Document Prototype Kotlin Tests")
class DocumentTest {
    @Test
    @DisplayName("Deep copy should create an independent document")
    fun `deep copy should be independent`() {
        val original = Document("Annual Report", "Ada", mutableListOf("Introduction", "Market Analysis"))
        val deepCopy = original.deepCopy()
        deepCopy.sections.add("Conclusion")

        assertNotSame(original, deepCopy, "Deep copy should be a new object")
        assertEquals(2, original.sections.size, "Original should keep its original sections")
        assertEquals(3, deepCopy.sections.size, "Deep copy should be mutable independently")
    }

    @Test
    @DisplayName("Shallow copy should share mutable nested state")
    fun `shallow copy should share nested state`() {
        val original = Document("Annual Report", "Ada", mutableListOf("Introduction"))
        val shallowCopy = original.copy()
        shallowCopy.sections.add("Conclusion")

        assertNotSame(original, shallowCopy, "Shallow copy should be a new object")
        assertEquals(2, original.sections.size, "Original sections are shared with the shallow copy")
    }
}
