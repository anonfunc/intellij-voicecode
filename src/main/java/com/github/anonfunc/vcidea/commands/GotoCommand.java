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

public class GotoCommand implements VcCommand {
    private int line;
    private int column;

    public GotoCommand(final int line, final int column) {
        // Both count from 0, so adjust.
        this.line = Math.max(line - 1, 0);
        this.column = Math.max(column - 1, 0);
    }

    @Override
    public String run() {
        System.out.println("Running goto command... " + line + " " + column);
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        EditorEx e = null;
        System.out.println("Open projects: " + openProjects.length);
        for (Project p : openProjects) {
            FileEditor[] selectedEditors = FileEditorManager.getInstance(openProjects[0]).getSelectedEditors();
            System.out.println("Selected editors: " + selectedEditors.length);
            if (selectedEditors.length >= 1) {
                e = EditorUtil.getEditorEx(selectedEditors[0]);
                break;
            }
        }
        if (e == null) {
            System.out.println("No selected editor?");
        }

        System.out.println(e.getCaretModel().getLogicalPosition());

        LogicalPosition position = new LogicalPosition(line, column);

        final EditorEx finalE = e;
        ApplicationManager.getApplication().invokeAndWait(() -> {
            finalE.getCaretModel().removeSecondaryCarets();
            finalE.getCaretModel().moveToLogicalPosition(position);
            finalE.getScrollingModel().scrollToCaret(ScrollType.CENTER);
            finalE.getSelectionModel().removeSelection();
            IdeFocusManager.getGlobalInstance().requestFocus(finalE.getContentComponent(), true);
        });


        System.out.println(e.getCaretModel().getLogicalPosition());
        return "OK";
    }
}
