package net.berkle.groupspeedrun.util;

/**
 * Utility for ARGB color integers (alpha in high bits) for HUD DrawContext.
 */
public final class GSRColorHelper {

    public static final int ALPHA_BIT_SHIFT = 24;
    public static final int RGB_MASK = 0x00FFFFFF;
    public static final int MAX_CHANNEL_VALUE = 255;

    private GSRColorHelper() {}

    /** Injects alpha (0–1) into an RGB color; returns 32-bit ARGB. */
    public static int applyAlpha(int hexColor, float alpha) {
        int alphaInt = (int) (Math.max(0.0f, Math.min(1.0f, alpha)) * MAX_CHANNEL_VALUE);
        return (hexColor & RGB_MASK) | (alphaInt << ALPHA_BIT_SHIFT);
    }

    /** Returns true if the color (ARGB) is too dark for readable text on dark UI backgrounds.
     * Threshold 300 allows magenta (0xAA00AA, sum 340) for preference color labels. */
    public static boolean isTooDarkForText(int argb) {
        int r = (argb >> 16) & 0xFF;
        int g = (argb >> 8) & 0xFF;
        int b = argb & 0xFF;
        return (r + g + b) < 300;
    }

    /** Transparent black for HUD background; baseOpacity e.g. 0x90, fadeProgress 0–1. */
    public static int getBackgroundWithAlpha(int baseOpacity, float fadeProgress) {
        int alpha = (int) (baseOpacity * Math.max(0.0f, Math.min(1.0f, fadeProgress)));
        return (alpha << ALPHA_BIT_SHIFT);
    }
}
