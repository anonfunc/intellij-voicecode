package com.github.anonfunc.vcidea.commands;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.util.TextRange;



public class CloneLineCommand implements VcCommand {
    private int sourceLine;

    public CloneLineCommand(final int sourceLine) {
        this.sourceLine = sourceLine - 1;
    }

    @Override
    public String run() {
        Project[] openProjects = ProjectManager.getInstance().getOpenProjects();
        Project project = null;
        EditorEx e1 = null;
        System.out.println("Open projects: " + openProjects.length);
        for (Project p : openProjects) {
            FileEditor[] selectedEditors = FileEditorManager.getInstance(openProjects[0]).getSelectedEditors();
            System.out.println("Selected editors: " + selectedEditors.length);
            if (selectedEditors.length >= 1) {
                project = p;
                e1 = EditorUtil.getEditorEx(selectedEditors[0]);
                break;
            }
        }
        if (e1 == null) {
            System.out.println("No selected editor?");

        }
        final EditorEx e = e1;
        final DocumentEx document = e.getDocument();
        document.setReadOnly(false);
        final int startOffset = document.getLineStartOffset(sourceLine);
        final int endOffset = document.getLineEndOffset(sourceLine);
        final String text = document.getText(new TextRange(startOffset, endOffset)).trim();

        final Application application = ApplicationManager.getApplication();
        final CommandProcessor cp = CommandProcessor.getInstance();
        final Project p = project;
        try {
            application.invokeAndWait(() -> {
                application.runWriteAction(() -> {
                    final int originalOffset = e.getCaretModel().getOffset();
                    cp.executeCommand(p, () -> document.insertString(originalOffset, text), "clone", "cloneGroup");
                });
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return "OK";
    }
}
