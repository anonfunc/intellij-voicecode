package com.github.anonfunc.vcidea.commands;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.IdeFocusManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;

public interface VcCommand {
    static VcCommand fromRequestUri(URI requestURI) {

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
            e.printStackTrace();
            return null;

        }

        String command = split[0];
        if (command.equals("goto")) {
            return new GotoCommand(Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        }
        if (command.equals("range")) {
            return new RangeCommand(Integer.parseInt(split[1]), Integer.parseInt(split[2]));
        }
        if (command.equals("extend")) {
            return new ExtendCommand(Integer.parseInt(split[1]));
        }
        if (command.equals("clone")) {
            return new CloneLineCommand(Integer.parseInt(split[1]));
        }
        if (command.equals("action")) {
            return new GivenActionCommand(split[1]);
        }
        if (command.equals("location")) {
            return new LocationCommand();
        }
        if (command.equals("find")) {
            return new FindCommand(split[1], String.join(" ", Arrays.copyOfRange(split, 2, split.length)));
        }
        if (command.equals("psi")) {
            return new StructureCommand(split[1], String.join(" ", Arrays.copyOfRange(split, 2, split.length)).split(","));
        }
        return null;
    }

    static Editor getEditor() {
        Project currentProject = getProject();
        Editor e = FileEditorManager.getInstance(currentProject).getSelectedTextEditor();
        if (e == null) {
            System.out.println("No selected editor?");
        }
        return e;
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

    String run();
}
