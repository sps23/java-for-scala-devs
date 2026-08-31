package io.github.sps23.designpatterns.command

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class DocumentEditorTest extends AnyFunSuite with Matchers:

  test("Command history should execute and undo an insertion command") {
    val editor  = new DocumentEditor
    val history = new CommandHistory

    history.execute(InsertTextCommand(editor, 0, "Hello"))
    editor.text shouldBe "Hello"

    history.undo()
    editor.text shouldBe ""
  }

  test("Command history should redo a command after undo") {
    val editor  = new DocumentEditor
    val history = new CommandHistory

    history.execute(InsertTextCommand(editor, 0, "Hello"))
    history.undo()
    history.redo()

    editor.text shouldBe "Hello"
  }
