package com.cope.meteorwebgui;

import com.cope.meteorwebgui.events.EventMonitor;
import com.cope.meteorwebgui.gui.WebGUITab;
import com.cope.meteorwebgui.server.MeteorWebServer;
import com.cope.meteorwebgui.systems.WebGUIConfig;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.gui.tabs.Tabs;
import meteordevelopment.meteorclient.systems.Systems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Meteor WebGUI Addon - Entry Point
 *
 * Provides a web-based GUI for Meteor Client with bi-directional control.
 * Features real-time module/settings synchronization via WebSockets.
 *
 * @author Cope
 */
public class MeteorWebGUIAddon extends MeteorAddon {
    public static final Logger LOG = LoggerFactory.getLogger("Meteor WebGUI");

    private static MeteorWebServer server;
    private static EventMonitor eventMonitor;

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor WebGUI Addon");

        // Register config system
        Systems.add(new WebGUIConfig());
        LOG.info("WebGUI config system registered");

        // Register GUI tab
        Tabs.add(new WebGUITab());
        LOG.info("WebGUI tab registered");

        // Auto-start server if enabled
        if (WebGUIConfig.get().autoStart.get()) {
            LOG.info("Auto-start enabled, starting server...");
            startServer();
        }

        LOG.info("Meteor WebGUI Addon initialized successfully");
    }

    @Override
    public void onRegisterCategories() {
        // No custom module categories needed for this addon
    }

    @Override
    public String getPackage() {
        return "com.cope.meteorwebgui";
    }

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("cope", "meteor-webgui");
    }

    /**
     * Start the WebGUI server
     */
    public static void startServer() {
        if (server != null && server.isRunning()) {
            LOG.warn("Server is already running");
            return;
        }

        try {
            String host = WebGUIConfig.get().host.get();
            int port = WebGUIConfig.get().port.get();

            LOG.info("Starting WebGUI server on {}:{}", host, port);

            server = new MeteorWebServer(host, port);
            server.start();

            // Initialize event monitoring
            eventMonitor = new EventMonitor(server);
            MeteorClient.EVENT_BUS.subscribe(eventMonitor);
            eventMonitor.startMonitoring();

            LOG.info("WebGUI server started successfully");
            LOG.info("Access the WebGUI at: http://{}:{}", host, port);

        } catch (Exception e) {
            LOG.error("Failed to start WebGUI server: {}", e.getMessage(), e);
        }
    }

    /**
     * Stop the WebGUI server
     */
    public static void stopServer() {
        if (server == null || !server.isRunning()) {
            LOG.warn("Server is not running");
            return;
        }

        try {
            LOG.info("Stopping WebGUI server");

            // Stop event monitoring
            if (eventMonitor != null) {
                MeteorClient.EVENT_BUS.unsubscribe(eventMonitor);
                eventMonitor.stopMonitoring();
                eventMonitor = null;
            }

            // Stop server
            server.shutdown();
            server = null;

            LOG.info("WebGUI server stopped successfully");

        } catch (Exception e) {
            LOG.error("Failed to stop WebGUI server: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if server is running
     */
    public static boolean isServerRunning() {
        return server != null && server.isRunning();
    }

    /**
     * Get the server instance
     */
    public static MeteorWebServer getServer() {
        return server;
    }
}

