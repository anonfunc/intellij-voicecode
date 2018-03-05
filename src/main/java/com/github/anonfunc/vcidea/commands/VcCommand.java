package com.github.anonfunc.vcidea.commands;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;

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
        if (command.equals("action")) {
            return new GivenActionCommand(split[1]);
        }
        if (command.equals("location")) {
            return new LocationCommand();
        }
        return null;
    }

    String run();
}
