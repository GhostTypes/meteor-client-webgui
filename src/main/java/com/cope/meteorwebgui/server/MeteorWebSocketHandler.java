package com.cope.meteorwebgui.server;

import fi.iki.elonen.NanoWSD;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages WebSocket connections and provides broadcasting capabilities.
 */
public class MeteorWebSocketHandler {
    private final List<MeteorWebSocket> connections = new ArrayList<>();

    /**
     * Creates a new WebSocket connection.
     */
    public NanoWSD.WebSocket createWebSocket(NanoWSD.IHTTPSession handshake) {
        MeteorWebSocket socket = new MeteorWebSocket(handshake) {
            @Override
            protected void onOpen() {
                super.onOpen();
                synchronized (connections) {
                    connections.add(this);
                }
            }

            @Override
            protected void onClose(NanoWSD.WebSocketFrame.CloseCode code, String reason, boolean initiatedByRemote) {
                super.onClose(code, reason, initiatedByRemote);
                synchronized (connections) {
                    connections.remove(this);
                }
            }
        };
        return socket;
    }

    /**
     * Broadcasts a message to all connected clients.
     */
    public void broadcast(String message) {
        synchronized (connections) {
            List<MeteorWebSocket> deadConnections = new ArrayList<>();

            for (MeteorWebSocket socket : connections) {
                try {
                    socket.send(message);
                } catch (Exception e) {
                    deadConnections.add(socket);
                }
            }

            // Remove dead connections
            connections.removeAll(deadConnections);
        }
    }

    /**
     * Returns the number of active connections.
     */
    public int getConnectionCount() {
        synchronized (connections) {
            return connections.size();
        }
    }
}
