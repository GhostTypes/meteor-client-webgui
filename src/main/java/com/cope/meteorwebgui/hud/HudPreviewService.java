package com.cope.meteorwebgui.hud;

import com.cope.meteorwebgui.protocol.MessageType;
import com.cope.meteorwebgui.protocol.WSMessage;
import com.cope.meteorwebgui.server.MeteorWebServer;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Periodically publishes HUD preview snapshots to connected WebGUI clients.
 */
public class HudPreviewService {
    private static final Logger LOG = LoggerFactory.getLogger("WebGUI HudPreviewService");
    private static final long DEFAULT_INTERVAL_MS = 200L;

    private final MeteorWebServer server;
    private final Map<String, Integer> snapshotHashes = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();

    private ScheduledExecutorService scheduler;

    public HudPreviewService(MeteorWebServer server) {
        this.server = server;
    }

    public synchronized void start() {
        if (scheduler != null) {
            return;
        }

        HudPreviewCapture.setEnabled(true);
        ThreadFactory factory = runnable -> {
            Thread thread = new Thread(runnable, "meteor-webgui-hud-preview");
            thread.setDaemon(true);
            return thread;
        };
        scheduler = Executors.newSingleThreadScheduledExecutor(factory);
        scheduler.scheduleAtFixedRate(this::publishSnapshots, 0L, DEFAULT_INTERVAL_MS, TimeUnit.MILLISECONDS);
        LOG.info("HUD preview service started ({} ms interval)", DEFAULT_INTERVAL_MS);
    }

    public synchronized void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        snapshotHashes.clear();
        HudPreviewCapture.setEnabled(false);
        LOG.info("HUD preview service stopped");
    }

    private void publishSnapshots() {
        Collection<HudPreviewSnapshot> snapshots = HudPreviewCapture.copySnapshots();
        JsonArray changedElements = new JsonArray();

        for (HudPreviewSnapshot snapshot : snapshots) {
            int hash = snapshot.contentHash();
            Integer previous = snapshotHashes.put(snapshot.getName(), hash);
            if (previous != null && previous == hash) {
                continue;
            }
            changedElements.add(snapshot.toJson());
        }

        if (changedElements.size() == 0) {
            return;
        }

        JsonObject data = new JsonObject();
        data.add("elements", changedElements);

        try {
            WSMessage message = new WSMessage(MessageType.HUD_PREVIEW_UPDATE, data);
            server.broadcast(gson.toJson(message));
        } catch (Exception e) {
            LOG.error("Failed to broadcast HUD preview update: {}", e.getMessage(), e);
        }
    }
}
