package com.github.anonfunc.vcidea.commands;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.util.EditorUtil;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.openapi.ui.playback.commands.ActionCommand;

import java.awt.*;
import java.awt.event.InputEvent;

// Not sure if this works.  XXX
public class GivenActionCommand implements VcCommand {
    private String actionId;

    public GivenActionCommand(final String actionId) {
        this.actionId = actionId;
    }

    @Override
    public String run() {
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

        final EditorEx finalE = e;
        ApplicationManager.getApplication().invokeAndWait(() -> {
            AnAction action = ActionManager.getInstance().getAction(actionId);
            InputEvent event = ActionCommand.getInputEvent(actionId);
            Component component = finalE.getComponent();
            ActionManager.getInstance().tryToExecute(action, event, component, null, true);
        });
        return "OK";

    }
}
