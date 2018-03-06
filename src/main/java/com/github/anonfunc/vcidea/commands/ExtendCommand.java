package com.github.anonfunc.vcidea.commands;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.wm.IdeFocusManager;

public class ExtendCommand implements VcCommand {
    private int targetLine;

    public ExtendCommand(int line) {
        this.targetLine = line - 1;
    }

    @Override
    public String run() {
        final EditorEx e = VcCommand.getEditorEx();

        final SelectionModel selection = e.getSelectionModel();
        final LogicalPosition current = e.getCaretModel().getLogicalPosition();
        int startLine = Math.min(current.line, targetLine);
        int endLine = Math.max(current.line, targetLine);
        ApplicationManager.getApplication().invokeAndWait(() -> {
          e.getCaretModel().moveToLogicalPosition(new LogicalPosition(startLine, 0));
          int startOffset = e.getCaretModel().getOffset();
          e.getCaretModel().moveToLogicalPosition(new LogicalPosition(endLine + 1, 0));
          int endOffset = e.getCaretModel().getOffset() - 1;
            selection.setSelection(startOffset, endOffset);
            e.getScrollingModel().scrollToCaret(ScrollType.CENTER);
            IdeFocusManager.getGlobalInstance().requestFocus(e.getContentComponent(), true);
        });

        return "OK";
    }
}
