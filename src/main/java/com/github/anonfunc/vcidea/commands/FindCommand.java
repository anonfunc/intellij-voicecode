package com.github.anonfunc.vcidea.commands;

import com.intellij.find.FindManager;
import com.intellij.find.FindModel;
import com.intellij.find.FindResult;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.wm.IdeFocusManager;

public class FindCommand extends VcCommand {
    private String direction;
    private String searchTerm;

    public FindCommand(final String direction, final String searchTerm) {
        this.direction = direction;
        this.searchTerm = searchTerm;
    }

    @Override

    public String run() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            final Editor e = VcCommand.getEditor();
            final Document document = e.getDocument();
            final SelectionModel selection = e.getSelectionModel();
            final Project project = ProjectManager.getInstance().getOpenProjects()[0];
            FindManager findManager = FindManager.getInstance(project);
            final FindModel findModel = new FindModel();
            findModel.setStringToFind(searchTerm);
            findModel.setCaseSensitive(false);
            findModel.setRegularExpressions(true);
            findModel.setForward(direction.equals("next"));
            final FindResult result = findManager.findString(
                    document.getCharsSequence(),
                    e.getCaretModel().getOffset(),
                    findModel);

            if (result.isStringFound()) {
                if (direction.equals("next")) {
                    e.getCaretModel().moveToOffset(result.getEndOffset());
                } else {
                    e.getCaretModel().moveToOffset(result.getStartOffset());
                }
                selection.setSelection(result.getStartOffset(), result.getEndOffset());
                e.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                IdeFocusManager.getGlobalInstance().requestFocus(e.getContentComponent(), true);
            }
        });
        return "OK";
    }
}
