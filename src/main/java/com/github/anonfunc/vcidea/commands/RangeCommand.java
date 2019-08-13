package com.github.anonfunc.vcidea.commands;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.wm.IdeFocusManager;

public class RangeCommand implements VcCommand {

    private int startLine;
    private int endLine;

    public RangeCommand(int startLine, int endLine) {
        this.startLine = startLine - 1;
        this.endLine = endLine - 1;
    }

    @Override
    public String run() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            final Editor e = VcCommand.getEditor();
            final SelectionModel selection = e.getSelectionModel();
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
