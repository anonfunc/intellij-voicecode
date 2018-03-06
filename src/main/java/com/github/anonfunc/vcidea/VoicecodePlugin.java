package com.github.anonfunc.vcidea;

import com.github.anonfunc.vcidea.commands.VcCommand;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.util.PlatformUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class VoicecodePlugin implements ApplicationComponent, HttpHandler {

    public static final int DEFAULT_PORT = 8652;

    private static final Map<String, Integer> PLATFORM_TO_PORT = new HashMap<>();

    static {
        PLATFORM_TO_PORT.put(PlatformUtils.IDEA_PREFIX, 8653);
        PLATFORM_TO_PORT.put(PlatformUtils.IDEA_CE_PREFIX, 8654);
        PLATFORM_TO_PORT.put(PlatformUtils.APPCODE_PREFIX, 8655);
        PLATFORM_TO_PORT.put(PlatformUtils.CLION_PREFIX, 8657);
        PLATFORM_TO_PORT.put(PlatformUtils.PYCHARM_PREFIX, 8658);
        PLATFORM_TO_PORT.put(PlatformUtils.PYCHARM_CE_PREFIX, 8658);
        PLATFORM_TO_PORT.put(PlatformUtils.PYCHARM_EDU_PREFIX, 8658);
        PLATFORM_TO_PORT.put(PlatformUtils.RUBY_PREFIX, 8661);
        PLATFORM_TO_PORT.put(PlatformUtils.PHP_PREFIX, 8662);
        PLATFORM_TO_PORT.put(PlatformUtils.WEB_PREFIX, 8663);
        PLATFORM_TO_PORT.put(PlatformUtils.DBE_PREFIX, 8664);
        PLATFORM_TO_PORT.put(PlatformUtils.RIDER_PREFIX, 8660);
        PLATFORM_TO_PORT.put(PlatformUtils.GOIDE_PREFIX, 8659);
    }

    @Override
    public void initComponent() {
        System.out.println("Starting Voicecode plugin...");
        Integer port = PLATFORM_TO_PORT.getOrDefault(PlatformUtils.getPlatformPrefix(), DEFAULT_PORT);

        Notification notification =new Notification("vc-idea", "Voicecode Plugin","Listening on " + port,
                NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);

        // https://stackoverflow.com/questions/3732109/simple-http-server-in-java-using-only-java-se-api#3732328
        final InetSocketAddress loopbackSocket = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
        final HttpServer server;
        try {
            server = HttpServer.create(loopbackSocket, -1);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        server.createContext("/", this);
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    @Override
    public void disposeComponent() {
        System.out.println("Disposing of Voicecode plugin");
    }

    @NotNull
    @Override
    public String getComponentName() {
        return "VoiceCode IDEA";
    }

    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        try {
            System.out.println("Handling " + httpExchange.getRequestURI().toString() + httpExchange.getRequestMethod());
            final InputStream is = httpExchange.getRequestBody();
            final Scanner s = new Scanner(is).useDelimiter("\\A");
            String response = VcCommand.fromRequestUri(httpExchange.getRequestURI()).run();
            if (response == null) {
                response = "BAD";
                httpExchange.sendResponseHeaders(502, response.length());
            } else {
                httpExchange.sendResponseHeaders(200, response.length());
            }
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
