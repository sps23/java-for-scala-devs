package io.github.sps23.designpatterns.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

@DisplayName("Document editor command history tests")
class DocumentEditorTest {
    @Test
    @DisplayName("Should execute and undo an insertion command")
    fun shouldExecuteAndUndoInsertion() {
        val editor = DocumentEditor()
        val history = CommandHistory()

        history.execute(InsertTextCommand(editor, 0, "Hello"))
        assertEquals("Hello", editor.text())

        history.undo()
        assertEquals("", editor.text())
    }

    @Test
    @DisplayName("Should redo a command after undo")
    fun shouldRedoAfterUndo() {
        val editor = DocumentEditor()
        val history = CommandHistory()

        history.execute(InsertTextCommand(editor, 0, "Hello"))
        history.undo()
        history.redo()

        assertEquals("Hello", editor.text())
    }
}
