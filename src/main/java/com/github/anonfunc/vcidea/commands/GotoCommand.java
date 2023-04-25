package com.github.anonfunc.vcidea.commands;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.wm.IdeFocusManager;

public class GotoCommand extends VcCommand {
    private int line;
    private int column;

    public GotoCommand(final int line, final int column) {
        // Both count from 0, so adjust.
        this.line = Math.max(line - 1, 0);
        this.column = Math.max(column - 1, 0);
    }

    @Override
    public String run() {
        final LogicalPosition pos = new LogicalPosition(line, column);
        ApplicationManager.getApplication().invokeAndWait(() -> {
            final Editor e = VcCommand.getEditor();
            e.getCaretModel().removeSecondaryCarets();
            e.getCaretModel().moveToLogicalPosition(pos);
            e.getScrollingModel().scrollToCaret(ScrollType.CENTER);
            e.getSelectionModel().removeSelection();
            IdeFocusManager.getGlobalInstance().requestFocus(e.getContentComponent(), true);
        });
        return "OK";
    }
}
