package com.github.anonfunc.vcidea.commands;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Optional;

public abstract class VcCommand {

    private static final Logger LOG = Logger.getInstance(VcCommand.class);

    public static Optional<VcCommand> fromRequestUri(URI requestURI) {

        String[] split;
        try {
            String decode = URLDecoder.decode(requestURI.toString().substring(1), "UTF-8");
            split = decode.split("/");
            // XXX For debugging
//            Notification notification =new Notification("vc-idea", "Voicecode Plugin", decode,
//                    NotificationType.INFORMATION);
//            Notifications.Bus.notify(notification);
            split = split[1].split(" ");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Failed to parse request URI", e);
            return Optional.empty();

        }

        String command = split[0];
        if (command.equals("goto")) {
            return Optional.of(new GotoCommand(Integer.parseInt(split[1]), Integer.parseInt(split[2])));
        }
        if (command.equals("range")) {
            return Optional.of(new RangeCommand(Integer.parseInt(split[1]), Integer.parseInt(split[2])));
        }
        if (command.equals("extend")) {
            return Optional.of(new ExtendCommand(Integer.parseInt(split[1])));
        }
        if (command.equals("clone")) {
            return Optional.of(new CloneLineCommand(Integer.parseInt(split[1])));
        }
        if (command.equals("action")) {
            return Optional.of(new GivenActionCommand(split[1]));
        }
        if (command.equals("location")) {
            return Optional.of(new LocationCommand());
        }
        if (command.equals("find")) {
            return Optional.of(new FindCommand(split[1], String.join(" ", Arrays.copyOfRange(split, 2, split.length))));
        }
        if (command.equals("psi")) {
            return Optional.of(new StructureCommand(split[1], String.join(" ", Arrays.copyOfRange(split, 2, split.length)).split(",")));
        }
        return Optional.empty();
    }

    static Editor getEditor() {
        Project currentProject = getProject();
        Editor e = FileEditorManager.getInstance(currentProject).getSelectedTextEditor();
        if (e == null) {
            LOG.debug("No selected editor?");
        }
        return e;
    }

    static ToolWindow getToolWindow() {
        Project currentProject = getProject();
        ToolWindowManager twm = ToolWindowManager.getInstance(currentProject);
        ToolWindow tw = twm.getToolWindow(twm.getActiveToolWindowId());
        if (tw == null) {
            LOG.debug("No selected tool window?");
        }
        return tw;
    }

    static PsiFile getPsiFile() {
        Project currentProject = getProject();
        Editor e = FileEditorManager.getInstance(currentProject).getSelectedTextEditor();
        final PsiFile psiFile = PsiDocumentManager.getInstance(currentProject)
            .getPsiFile(e.getDocument());
        return psiFile;
    }

    static Project getProject() {
        return IdeFocusManager.findInstance().getLastFocusedFrame().getProject();
    }

    public abstract String run();
}
