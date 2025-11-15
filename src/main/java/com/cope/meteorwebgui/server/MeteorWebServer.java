package com.cope.meteorwebgui.server;

import com.cope.meteorwebgui.mapping.ModuleMapper;
import com.cope.meteorwebgui.mapping.SettingsReflector;
import com.cope.meteorwebgui.protocol.MessageType;
import com.cope.meteorwebgui.protocol.WSMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class MeteorWebServer extends WebSocketServer {
    private static final Logger LOG = LoggerFactory.getLogger("WebGUI Server");
    private static final Gson GSON = new Gson();

    private boolean running = false;

    public MeteorWebServer(String host, int port) {
        super(new InetSocketAddress(host, port));
        setReuseAddr(true);
    }

    @Override
    public void onStart() {
        running = true;
        LOG.info("WebGUI server started on {}:{}", getAddress().getHostString(), getPort());
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        LOG.info("New WebSocket connection from {}", conn.getRemoteSocketAddress());

        // Send initial state
        try {
            JsonObject initialData = new JsonObject();
            initialData.add("modules", ModuleMapper.mapAllModulesByCategory());

            WSMessage message = new WSMessage(MessageType.INITIAL_STATE, initialData);
            conn.send(GSON.toJson(message));

            LOG.info("Sent initial state to client");
        } catch (Exception e) {
            LOG.error("Failed to send initial state: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        LOG.info("WebSocket connection closed: {} (code: {})", reason, code);
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        try {
            WSMessage wsMessage = GSON.fromJson(message, WSMessage.class);
            MessageType type = wsMessage.getMessageType();

            if (type == null) {
                sendError(conn, "Unknown message type: " + wsMessage.getType());
                return;
            }

            LOG.debug("Received message type: {}", type);

            switch (type) {
                case MODULE_TOGGLE -> handleModuleToggle(conn, wsMessage);
                case MODULE_LIST -> handleModuleList(conn, wsMessage);
                case SETTING_UPDATE -> handleSettingUpdate(conn, wsMessage);
                case SETTING_GET -> handleSettingGet(conn, wsMessage);
                case PING -> handlePing(conn, wsMessage);
                default -> sendError(conn, "Unsupported message type: " + type);
            }

        } catch (Exception e) {
            LOG.error("Error handling message: {}", e.getMessage(), e);
            sendError(conn, "Failed to process message: " + e.getMessage());
        }
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOG.error("WebSocket error: {}", ex.getMessage(), ex);
    }

    private void handleModuleToggle(WebSocket conn, WSMessage message) {
        try {
            JsonObject data = message.getData().getAsJsonObject();
            String moduleName = data.get("moduleName").getAsString();

            Module module = Modules.get().get(moduleName);
            if (module == null) {
                sendError(conn, "Module not found: " + moduleName, message.getId());
                return;
            }

            module.toggle();

            // Send success response
            JsonObject response = new JsonObject();
            response.addProperty("success", true);
            response.addProperty("moduleName", moduleName);
            response.addProperty("active", module.isActive());

            conn.send(GSON.toJson(new WSMessage("response", response, message.getId())));

            LOG.info("Toggled module: {} -> {}", moduleName, module.isActive());

        } catch (Exception e) {
            LOG.error("Failed to toggle module: {}", e.getMessage(), e);
            sendError(conn, "Failed to toggle module: " + e.getMessage(), message.getId());
        }
    }

    private void handleModuleList(WebSocket conn, WSMessage message) {
        try {
            JsonObject data = ModuleMapper.mapModulesLightweight();

            JsonObject response = new JsonObject();
            response.add("modules", data);

            conn.send(GSON.toJson(new WSMessage("response", response, message.getId())));

        } catch (Exception e) {
            LOG.error("Failed to get module list: {}", e.getMessage(), e);
            sendError(conn, "Failed to get module list: " + e.getMessage(), message.getId());
        }
    }

    private void handleSettingUpdate(WebSocket conn, WSMessage message) {
        try {
            JsonObject data = message.getData().getAsJsonObject();
            String moduleName = data.get("moduleName").getAsString();
            String settingName = data.get("settingName").getAsString();
            JsonObject value = data.get("value").getAsJsonObject();

            Module module = Modules.get().get(moduleName);
            if (module == null) {
                sendError(conn, "Module not found: " + moduleName, message.getId());
                return;
            }

            Setting<?> setting = findSetting(module, settingName);
            if (setting == null) {
                sendError(conn, "Setting not found: " + settingName, message.getId());
                return;
            }

            boolean success = SettingsReflector.setSettingValue(setting, value);

            JsonObject response = new JsonObject();
            response.addProperty("success", success);
            response.addProperty("moduleName", moduleName);
            response.addProperty("settingName", settingName);

            conn.send(GSON.toJson(new WSMessage("response", response, message.getId())));

            if (success) {
                LOG.info("Updated setting: {}.{} = {}", moduleName, settingName, value);
            }

        } catch (Exception e) {
            LOG.error("Failed to update setting: {}", e.getMessage(), e);
            sendError(conn, "Failed to update setting: " + e.getMessage(), message.getId());
        }
    }

    private void handleSettingGet(WebSocket conn, WSMessage message) {
        try {
            JsonObject data = message.getData().getAsJsonObject();
            String moduleName = data.get("moduleName").getAsString();
            String settingName = data.get("settingName").getAsString();

            Module module = Modules.get().get(moduleName);
            if (module == null) {
                sendError(conn, "Module not found: " + moduleName, message.getId());
                return;
            }

            Setting<?> setting = findSetting(module, settingName);
            if (setting == null) {
                sendError(conn, "Setting not found: " + settingName, message.getId());
                return;
            }

            JsonObject response = new JsonObject();
            response.add("setting", SettingsReflector.getSettingMetadata(setting));

            conn.send(GSON.toJson(new WSMessage("response", response, message.getId())));

        } catch (Exception e) {
            LOG.error("Failed to get setting: {}", e.getMessage(), e);
            sendError(conn, "Failed to get setting: " + e.getMessage(), message.getId());
        }
    }

    private void handlePing(WebSocket conn, WSMessage message) {
        conn.send(GSON.toJson(new WSMessage(MessageType.PONG, new JsonObject(), message.getId())));
    }

    private Setting<?> findSetting(Module module, String settingName) {
        for (SettingGroup group : module.settings) {
            for (Setting<?> setting : group) {
                if (setting.name.equals(settingName)) {
                    return setting;
                }
            }
        }
        return null;
    }

    private void sendError(WebSocket conn, String error) {
        sendError(conn, error, null);
    }

    private void sendError(WebSocket conn, String error, String requestId) {
        JsonObject errorData = new JsonObject();
        errorData.addProperty("error", error);
        conn.send(GSON.toJson(new WSMessage(MessageType.ERROR, errorData, requestId)));
    }

    /**
     * Broadcast module state change to all connected clients
     */
    public void broadcastModuleStateChange(Module module) {
        if (!running) return;

        try {
            JsonObject data = ModuleMapper.createModuleStateMessage(module);
            WSMessage message = new WSMessage(MessageType.MODULE_STATE_CHANGED, data);
            broadcast(GSON.toJson(message));

            LOG.debug("Broadcast module state: {} -> {}", module.name, module.isActive());
        } catch (Exception e) {
            LOG.error("Failed to broadcast module state: {}", e.getMessage(), e);
        }
    }

    /**
     * Broadcast setting value change to all connected clients
     */
    public void broadcastSettingChange(Module module, Setting<?> setting) {
        if (!running) return;

        try {
            JsonObject data = ModuleMapper.createSettingChangeMessage(module, setting);
            WSMessage message = new WSMessage(MessageType.SETTING_VALUE_CHANGED, data);
            broadcast(GSON.toJson(message));

            LOG.debug("Broadcast setting change: {}.{}", module.name, setting.name);
        } catch (Exception e) {
            LOG.error("Failed to broadcast setting change: {}", e.getMessage(), e);
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void shutdown() {
        try {
            running = false;
            stop();
            LOG.info("WebGUI server stopped");
        } catch (Exception e) {
            LOG.error("Error stopping server: {}", e.getMessage(), e);
        }
    }
}
