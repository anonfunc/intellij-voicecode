package com.github.anonfunc.vcidea.commands;

import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ex.EditorEx;

public class LocationCommand implements VcCommand {

    public LocationCommand() {
    }

    @Override
    public String run() {
        final EditorEx e = VcCommand.getEditorEx();
        LogicalPosition logicalPosition = e.getCaretModel().getLogicalPosition();
        return String.format("%d %d", logicalPosition.line + 1, logicalPosition.column + 1);
    }
}
