package io.github.sps23.designpatterns.prototype

import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Document Prototype Scala 2 Tests")
class DocumentTest {

  @Test
  @DisplayName("copy() should create an independent document")
  def copyShouldCreateIndependentDocument(): Unit = {
    val original = Document("Annual Report", "Ada", List("Introduction", "Market Analysis"))
    val updated  = original.addSection("Conclusion")

    assertNotSame(original, updated, "copy() should produce a new object")
    assertEquals(2, original.sections.size, "Original should be unchanged")
    assertEquals(3, updated.sections.size, "Updated document should contain the new section")
  }
}
