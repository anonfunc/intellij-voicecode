package com.github.anonfunc.vcidea.commands;

import com.intellij.find.FindManager;
import com.intellij.find.FindModel;
import com.intellij.find.FindResult;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.playback.commands.ActionCommand;
import com.intellij.openapi.wm.IdeFocusManager;

import java.awt.*;
import java.awt.event.InputEvent;

public class FindCommand implements VcCommand {
    private String direction;
    private String searchTerm;

    public FindCommand(final String direction, final String searchTerm) {
        this.direction = direction;
        this.searchTerm = searchTerm;
    }

    @Override

    public String run() {
        final EditorEx e = VcCommand.getEditorEx();
        final DocumentEx document = e.getDocument();
        final SelectionModel selection = e.getSelectionModel();
        final Project project = ProjectManager.getInstance().getOpenProjects()[0];
        FindManager findManager = FindManager.getInstance(project);
        final FindModel findModel = new FindModel();
        findModel.setStringToFind(searchTerm);
        findModel.setCaseSensitive(false);
        findModel.setForward(direction.equals("next"));

        ApplicationManager.getApplication().invokeAndWait(() -> {
            final FindResult result = findManager.findString(
                    document.getCharsSequence(),
                    e.getCaretModel().getOffset(),
                    findModel);

            if (result.isStringFound()) {
                e.getCaretModel().moveToOffset(result.getStartOffset());
                selection.setSelection(result.getStartOffset(), result.getEndOffset());
                e.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                IdeFocusManager.getGlobalInstance().requestFocus(e.getContentComponent(), true);
            }
        });
        return "OK";
    }
}
