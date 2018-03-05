package com.github.anonfunc.vcidea.commands;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.IdeFocusManager;

public class LocationCommand implements VcCommand {

    public LocationCommand() {
    }

    @Override
    public String run() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        EditorEx e = null;
        for (Project p : openProjects) {
            FileEditor[] selectedEditors = FileEditorManager.getInstance(openProjects[0]).getSelectedEditors();
            if (selectedEditors.length >= 1) {
                e = EditorUtil.getEditorEx(selectedEditors[0]);
                break;
            }
        }
        if (e == null) {
            System.out.println("No selected editor?");
            return null;
        }

        LogicalPosition logicalPosition = e.getCaretModel().getLogicalPosition();
        return String.format("%d %d", logicalPosition.line + 1, logicalPosition.column + 1);
    }
}
