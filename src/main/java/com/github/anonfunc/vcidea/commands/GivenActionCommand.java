package com.github.anonfunc.vcidea.commands;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.playback.commands.ActionCommand;
import com.intellij.openapi.wm.ToolWindow;

import java.awt.Component;
import java.awt.event.InputEvent;

// Not sure if this works.  XXX
public class GivenActionCommand implements VcCommand {
    private String actionId;

    public GivenActionCommand(final String actionId) {
        this.actionId = actionId;
    }

    @Override
    public String run() {
        ApplicationManager.getApplication().invokeAndWait(() -> {
            AnAction action = ActionManager.getInstance().getAction(actionId);
            InputEvent event = ActionCommand.getInputEvent(actionId);
            final ToolWindow e = VcCommand.getToolWindow();
            Component component = null;
            if (e != null) {
                component = e.getComponent();
            }
            ActionManager.getInstance().tryToExecute(action, event, component, null, true);
        });
        return "OK";

    }
}
