package com.github.anonfunc.vcidea.commands;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ex.EditorEx;

public class LocationCommand implements VcCommand {

    public LocationCommand() {
    }

    @Override
    public String run() {
        final Editor e[] = new Editor[1];
        ApplicationManager.getApplication().invokeAndWait(() -> {
            e[0] = VcCommand.getEditor();
        });
        LogicalPosition logicalPosition = e[0].getCaretModel().getLogicalPosition();
        return String.format("%d %d", logicalPosition.line + 1, logicalPosition.column + 1);
    }
}
