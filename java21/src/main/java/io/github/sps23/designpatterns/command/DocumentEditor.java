package io.github.sps23.designpatterns.command;

import java.util.ArrayDeque;
import java.util.Deque;

public final class DocumentEditor {
    private final StringBuilder content = new StringBuilder();

    public void insert(int index, String value) {
        if (index < 0 || index > content.length()) {
            throw new IndexOutOfBoundsException("Index out of range: " + index);
        }
        content.insert(index, value);
    }

    public void delete(int start, int end) {
        if (start < 0 || end < start || end > content.length()) {
            throw new IndexOutOfBoundsException("Range is invalid: [" + start + ", " + end + "]");
        }
        content.delete(start, end);
    }

    public String text() {
        return content.toString();
    }
}

interface Command {
    void execute();

    void undo();
}

final class InsertTextCommand implements Command {
    private final DocumentEditor editor;
    private final int index;
    private final String value;

    InsertTextCommand(DocumentEditor editor, int index, String value) {
        this.editor = editor;
        this.index = index;
        this.value = value;
    }

    @Override
    public void execute() {
        editor.insert(index, value);
    }

    @Override
    public void undo() {
        editor.delete(index, index + value.length());
    }
}

final class CommandHistory {
    private final Deque<Command> undoStack = new ArrayDeque<>();
    private final Deque<Command> redoStack = new ArrayDeque<>();

    public void execute(Command command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    public void undo() {
        if (undoStack.isEmpty()) {
            return;
        }
        var command = undoStack.pop();
        command.undo();
        redoStack.push(command);
    }

    public void redo() {
        if (redoStack.isEmpty()) {
            return;
        }
        var command = redoStack.pop();
        command.execute();
        undoStack.push(command);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
