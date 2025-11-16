package com.cope.meteorwebgui.hud;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Immutable snapshot describing a HUD element preview payload.
 */
public class HudPreviewSnapshot {
    private final String name;
    private final String title;
    private final String description;
    private final String group;
    private final boolean active;
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final boolean hasNonText;
    private final List<HudTextLine> lines;
    private final long updatedAt;

    public HudPreviewSnapshot(String name,
                              String title,
                              String description,
                              String group,
                              boolean active,
                              int x,
                              int y,
                              int width,
                              int height,
                              boolean hasNonText,
                              List<HudTextLine> lines,
                              long updatedAt) {
        this.name = name;
        this.title = title;
        this.description = description;
        this.group = group;
        this.active = active;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.hasNonText = hasNonText;
        this.lines = List.copyOf(lines);
        this.updatedAt = updatedAt;
    }

    public String getName() {
        return name;
    }

    public boolean hasNonText() {
        return hasNonText;
    }

    public List<HudTextLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", name);
        obj.addProperty("title", title);
        obj.addProperty("description", description);
        obj.addProperty("group", group);
        obj.addProperty("active", active);
        obj.addProperty("x", x);
        obj.addProperty("y", y);
        obj.addProperty("width", width);
        obj.addProperty("height", height);
        obj.addProperty("hasNonText", hasNonText);
        obj.addProperty("updatedAt", updatedAt);

        JsonArray linesArray = new JsonArray();
        for (HudTextLine line : lines) {
            JsonObject lineObj = new JsonObject();
            lineObj.addProperty("text", line.getText());
            lineObj.addProperty("x", line.getX());
            lineObj.addProperty("y", line.getY());
            lineObj.addProperty("color", String.format("#%08X", line.getColor()));
            lineObj.addProperty("shadow", line.hasShadow());
            lineObj.addProperty("scale", line.getScale());
            linesArray.add(lineObj);
        }
        obj.add("lines", linesArray);
        return obj;
    }

    public int contentHash() {
        return Objects.hash(name, title, description, group, active, x, y, width, height, hasNonText, linesHash());
    }

    private int linesHash() {
        int hash = 7;
        for (HudTextLine line : lines) {
            hash = 31 * hash + Objects.hash(line.getText(), line.getX(), line.getY(), line.getColor(), line.hasShadow(), line.getScale());
        }
        return hash;
    }
}
