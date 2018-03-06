package com.github.anonfunc.vcidea.commands;

import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Arrays;

public interface VcCommand {
    static VcCommand fromRequestUri(URI requestURI) {

        final String[] split;
        try {
            String decode = URLDecoder.decode(requestURI.toString().substring(1), "UTF-8");
            // XXX For debugging
//            Notification notification =new Notification("vc-idea", "Voicecode Plugin", decode,
//                    NotificationType.INFORMATION);
//            Notifications.Bus.notify(notification);
            split = decode.split(" ");
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
        return null;
    }

    static EditorEx getEditorEx() {
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
        return e;
    }

    String run();
}
