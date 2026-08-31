package io.github.sps23.designpatterns.command

import scala.collection.mutable.ArrayDeque

final class DocumentEditor {
  private val content = new StringBuilder

  def insert(index: Int, value: String): Unit = {
    require(index >= 0 && index <= content.length, s"Index out of range: $index")
    content.insert(index, value)
  }

  def delete(start: Int, end: Int): Unit = {
    require(start >= 0 && end >= start && end <= content.length, s"Range is invalid: [$start, $end]")
    content.delete(start, end)
  }

  def text: String = content.toString
}

trait Command {
  def execute(): Unit
  def undo(): Unit
}

final case class InsertTextCommand(editor: DocumentEditor, index: Int, value: String) extends Command {
  override def execute(): Unit = editor.insert(index, value)

  override def undo(): Unit = editor.delete(index, index + value.length)
}

final class CommandHistory {
  private val undoStack = ArrayDeque.empty[Command]
  private val redoStack = ArrayDeque.empty[Command]

  def execute(command: Command): Unit = {
    command.execute()
    undoStack.prepend(command)
    redoStack.clear()
  }

  def undo(): Unit = {
    if (undoStack.isEmpty) return
    val command = undoStack.removeHead()
    command.undo()
    redoStack.prepend(command)
  }

  def redo(): Unit = {
    if (redoStack.isEmpty) return
    val command = redoStack.removeHead()
    command.execute()
    undoStack.prepend(command)
  }

  def canUndo: Boolean = undoStack.nonEmpty
  def canRedo: Boolean = redoStack.nonEmpty
}
