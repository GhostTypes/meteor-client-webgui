package com.cope.meteorwebgui.hud;

/**
 * Represents a single text line emitted by a HUD element.
 */
public class HudTextLine {
    private final String text;
    private final double x;
    private final double y;
    private final int color;
    private final boolean shadow;
    private final double scale;

    public HudTextLine(String text, double x, double y, int color, boolean shadow, double scale) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.color = color;
        this.shadow = shadow;
        this.scale = scale;
    }

    public String getText() {
        return text;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getColor() {
        return color;
    }

    public boolean hasShadow() {
        return shadow;
    }

    public double getScale() {
        return scale;
    }
}
