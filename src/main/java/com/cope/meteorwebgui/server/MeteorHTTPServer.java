package com.cope.meteorwebgui.server;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoWSD;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * HTTP server that serves the bundled WebUI static files from JAR resources
 * and handles WebSocket upgrade requests for the /ws endpoint.
 */
public class MeteorHTTPServer extends NanoWSD {
    private final MeteorWebSocketHandler webSocketHandler;

    public MeteorHTTPServer(String hostname, int port, MeteorWebSocketHandler webSocketHandler) {
        super(hostname, port);
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    protected WebSocket openWebSocket(IHTTPSession handshake) {
        String uri = handshake.getUri();

        // Only accept WebSocket connections on /ws path
        if ("/ws".equals(uri)) {
            return webSocketHandler.createWebSocket(handshake);
        }

        return null; // Reject WebSocket connection
    }

    @Override
    protected Response serveHttp(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        // Only handle GET requests for static files
        if (method != Method.GET) {
            return newFixedLengthResponse(Response.Status.METHOD_NOT_ALLOWED, MIME_PLAINTEXT, "Method Not Allowed");
        }

        // Serve static files from bundled resources
        return serveStaticFile(uri);
    }

    /**
     * Serves static files from the bundled WebUI resources in the JAR.
     */
    private Response serveStaticFile(String uri) {
        // Map root to index.html
        if ("/".equals(uri)) {
            uri = "/index.html";
        }

        // Build resource path (all WebUI files are in /webui/ in JAR resources)
        String resourcePath = "/webui" + uri;

        // Try to load resource from JAR
        try (InputStream resourceStream = getClass().getResourceAsStream(resourcePath)) {
            if (resourceStream == null) {
                // Resource not found
                return newNotFoundResponse(uri);
            }

            // Read the entire resource into memory
            byte[] data = resourceStream.readAllBytes();

            // Determine MIME type based on file extension
            String mimeType = detectMimeType(uri);

            // Return the resource with appropriate MIME type
            return newFixedLengthResponse(Response.Status.OK, mimeType, new ByteArrayInputStream(data), data.length);

        } catch (IOException e) {
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Internal Server Error");
        }
    }

    /**
     * Determines MIME type based on file extension.
     */
    private String detectMimeType(String filename) {
        if (filename.endsWith(".html")) {
            return "text/html";
        } else if (filename.endsWith(".js")) {
            return "application/javascript";
        } else if (filename.endsWith(".css")) {
            return "text/css";
        } else if (filename.endsWith(".json")) {
            return "application/json";
        } else if (filename.endsWith(".png")) {
            return "image/png";
        } else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (filename.endsWith(".svg")) {
            return "image/svg+xml";
        } else if (filename.endsWith(".ico")) {
            return "image/x-icon";
        } else if (filename.endsWith(".woff")) {
            return "font/woff";
        } else if (filename.endsWith(".woff2")) {
            return "font/woff2";
        } else if (filename.endsWith(".ttf")) {
            return "font/ttf";
        } else if (filename.endsWith(".eot")) {
            return "application/vnd.ms-fontobject";
        } else {
            return "application/octet-stream";
        }
    }

    /**
     * Returns a 404 Not Found response.
     */
    private Response newNotFoundResponse(String uri) {
        String html = "<!DOCTYPE html>" +
                "<html><head><title>404 Not Found</title></head>" +
                "<body><h1>404 Not Found</h1><p>The requested resource '" + uri + "' was not found.</p></body>" +
                "</html>";
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", html);
    }
}
