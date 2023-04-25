package com.github.anonfunc.vcidea.commands;

import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;



public class CloneLineCommand extends VcCommand {

    private static final Logger LOG = Logger.getInstance(StructureCommand.class);
    private int sourceLine;

    public CloneLineCommand(final int sourceLine) {
        this.sourceLine = sourceLine - 1;
    }

    @Override
    public String run() {
        final Application application = ApplicationManager.getApplication();
        final CommandProcessor cp = CommandProcessor.getInstance();
        final Project p = VcCommand.getProject();
        try {
            application.invokeAndWait(() -> {
                final Editor e = VcCommand.getEditor();
                final Document document = e.getDocument();
                document.setReadOnly(false);
                final int startOffset = document.getLineStartOffset(sourceLine);
                final int endOffset = document.getLineEndOffset(sourceLine);
                final String text = document.getText(new TextRange(startOffset, endOffset)).trim();

                application.runWriteAction(() -> {
                    final int originalOffset = e.getCaretModel().getOffset();
                    cp.executeCommand(p, () -> document.insertString(originalOffset, text), "clone", "cloneGroup");
                });
            });
        } catch (Exception ex) {
            LOG.error("Failed to run clone line command", ex);
            return null;
        }
        return "OK";
    }
}
