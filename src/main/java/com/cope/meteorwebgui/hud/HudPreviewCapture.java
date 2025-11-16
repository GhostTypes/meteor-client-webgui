package com.cope.meteorwebgui.hud;

import com.cope.meteorwebgui.mapping.HudMapper;
import com.google.gson.JsonArray;
import meteordevelopment.meteorclient.systems.hud.HudElement;
import meteordevelopment.meteorclient.systems.hud.HudElementInfo;
import meteordevelopment.meteorclient.utils.render.color.Color;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lightweight capture helper that records text draw calls emitted by HUD elements.
 * Heavy processing happens elsewhere; this class stays on the render thread hot path only.
 */
public final class HudPreviewCapture {
    private static final ThreadLocal<CaptureBuffer> ACTIVE_BUFFER = new ThreadLocal<>();
    private static final Map<String, HudPreviewSnapshot> SNAPSHOTS = new ConcurrentHashMap<>();
    private static final AtomicBoolean ENABLED = new AtomicBoolean(false);

    private HudPreviewCapture() {}

    public static void setEnabled(boolean enabled) {
        ENABLED.set(enabled);
        if (!enabled) {
            ACTIVE_BUFFER.remove();
            SNAPSHOTS.clear();
        }
    }

    public static boolean isEnabled() {
        return ENABLED.get();
    }

    public static void begin(HudElement element) {
        if (!ENABLED.get() || element == null) return;
        ACTIVE_BUFFER.set(new CaptureBuffer(element));
    }

    public static void end() {
        CaptureBuffer buffer = ACTIVE_BUFFER.get();
        ACTIVE_BUFFER.remove();
        if (!ENABLED.get() || buffer == null) return;

        HudPreviewSnapshot snapshot = buffer.build();
        if (snapshot != null) {
            SNAPSHOTS.put(snapshot.getName(), snapshot);
        }
    }

    public static void recordText(String text, double x, double y, Color color, boolean shadow, double scale) {
        CaptureBuffer buffer = ACTIVE_BUFFER.get();
        if (buffer == null) return;
        buffer.addLine(text, x, y, color, shadow, scale);
    }

    public static void markNonText() {
        CaptureBuffer buffer = ACTIVE_BUFFER.get();
        if (buffer == null) return;
        buffer.markNonText();
    }

    public static Collection<HudPreviewSnapshot> copySnapshots() {
        return List.copyOf(SNAPSHOTS.values());
    }

    public static JsonArray serializeSnapshots() {
        JsonArray array = new JsonArray();
        for (HudPreviewSnapshot snapshot : SNAPSHOTS.values()) {
            array.add(snapshot.toJson());
        }
        return array;
    }

    private static final class CaptureBuffer {
        private final HudElement element;
        private final List<HudTextLine> lines = new ArrayList<>();
        private boolean hasNonText;

        private CaptureBuffer(HudElement element) {
            this.element = element;
        }

        private void addLine(String text, double x, double y, Color color, boolean shadow, double scale) {
            if (text == null || text.isEmpty()) return;
            lines.add(new HudTextLine(text, x, y, color != null ? color.getPacked() : Color.WHITE.getPacked(), shadow, scale));
        }

        private void markNonText() {
            hasNonText = true;
        }

        private HudPreviewSnapshot build() {
            if (element == null) return null;
            HudElementInfo<?> info = element.info;
            String name = HudMapper.getElementIdentifier(element);
            String title = info != null ? info.title : name;
            String description = info != null ? info.description : "";
            String group = info != null && info.group != null ? info.group.title() : "HUD";

            return new HudPreviewSnapshot(
                name,
                title,
                description,
                group,
                element.isActive(),
                element.getX(),
                element.getY(),
                element.getWidth(),
                element.getHeight(),
                hasNonText,
                lines,
                System.currentTimeMillis()
            );
        }
    }
}
