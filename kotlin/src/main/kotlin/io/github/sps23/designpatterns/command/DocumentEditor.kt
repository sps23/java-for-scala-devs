package io.github.sps23.designpatterns.command

import java.util.ArrayDeque
import java.util.Deque

class DocumentEditor {
    private val content = StringBuilder()

    fun insert(index: Int, value: String) {
        require(index in 0..content.length) { "Index out of range: $index" }
        content.insert(index, value)
    }

    fun delete(start: Int, end: Int) {
        require(start in 0..end && end <= content.length) { "Range is invalid: [$start, $end]" }
        content.delete(start, end)
    }

    fun text(): String = content.toString()
}

interface Command {
    fun execute()
    fun undo()
}

class InsertTextCommand(
    private val editor: DocumentEditor,
    private val index: Int,
    private val value: String,
) : Command {
    override fun execute() {
        editor.insert(index, value)
    }

    override fun undo() {
        editor.delete(index, index + value.length)
    }
}

class CommandHistory {
    private val undoStack: Deque<Command> = ArrayDeque()
    private val redoStack: Deque<Command> = ArrayDeque()

    fun execute(command: Command) {
        command.execute()
        undoStack.push(command)
        redoStack.clear()
    }

    fun undo() {
        if (undoStack.isEmpty()) return
        val command = undoStack.pop()
        command.undo()
        redoStack.push(command)
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val command = redoStack.pop()
        command.execute()
        undoStack.push(command)
    }

    fun canUndo(): Boolean = undoStack.isNotEmpty()
    fun canRedo(): Boolean = redoStack.isNotEmpty()
}
