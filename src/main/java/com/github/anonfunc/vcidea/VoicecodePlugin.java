package com.github.anonfunc.vcidea;

import com.github.anonfunc.vcidea.commands.VcCommand;
import com.intellij.ide.AppLifecycleListener;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.PlatformUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.*;


public class VoicecodePlugin implements HttpHandler, AppLifecycleListener {

    public static final int DEFAULT_PORT = 8652;

    private static final Map<String, Integer> PLATFORM_TO_PORT = new HashMap<>();
    private static final Logger LOG = Logger.getInstance(VoicecodePlugin.class);

    private Path pathToNonce;
    private HttpServer server;

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
    public void appStarted() {
        LOG.info("Starting Voicecode plugin...");
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        String nonce = new String(Base64.getUrlEncoder().encode(bytes));
//        String nonce = "localdev";
        Integer port = PLATFORM_TO_PORT.getOrDefault(PlatformUtils.getPlatformPrefix(), DEFAULT_PORT);
        try {
            pathToNonce = FileSystems.getDefault().getPath(System.getProperty("java.io.tmpdir"), "vcidea_" + port.toString());
            Files.write(pathToNonce, nonce.getBytes());
        } catch (IOException e) {
            LOG.error("Failed to write nonce file", e);
        }

        Notification notification = new Notification("vc-idea",
                "Voicecode Plugin","Listening on http://localhost:" + port + "/" + nonce,
                NotificationType.INFORMATION);
        Notifications.Bus.notify(notification);

        // https://stackoverflow.com/questions/3732109/simple-http-server-in-java-using-only-java-se-api#3732328
        final InetSocketAddress loopbackSocket = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
        final HttpServer server;
        try {
            server = HttpServer.create(loopbackSocket, -1);
        } catch (IOException e) {
            LOG.error("Failed to start server to listen for commands", e);
            return;
        }
        server.createContext("/" + nonce, this);
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    @Override
    public void appWillBeClosed(boolean isRestart) {
        try {
            Files.delete(pathToNonce);
        } catch (IOException e) {
            LOG.error("Failed to cleanup nonce file", e);
        }
        server.stop(1);
        LOG.info("Completed cleanup of Voicecode plugin");
    }

    @Override
    public void handle(final HttpExchange httpExchange) throws IOException {
        try {
            LOG.debug("Handling " + httpExchange.getRequestURI().toString() + httpExchange.getRequestMethod());
            final InputStream is = httpExchange.getRequestBody();
            final Scanner s = new Scanner(is).useDelimiter("\\A");
            var response = VcCommand.fromRequestUri(httpExchange.getRequestURI())
                    .map(VcCommand::run)
                    .map(resp -> new VoicePluginResponse(200, resp))
                    .orElse(new VoicePluginResponse(502, "BAD"));
            httpExchange.sendResponseHeaders(response.responseCode(), response.response().length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.response().getBytes());
            os.close();
        } catch (Exception e) {
            LOG.error("Failed to process command... ", e);
            final String response = e.toString();
            httpExchange.sendResponseHeaders(500, response.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    record VoicePluginResponse(int responseCode, String response) {
    }
}
