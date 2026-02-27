package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRHudParameters;
import net.minecraft.text.Text;

/**
 * HUD Look mode: Full (always show splits) or Condensed (splits only during event window).
 */
public enum GSRHudLookMode {
    FULL(GSRHudParameters.MODE_FULL, "Full"),
    CONDENSED(GSRHudParameters.MODE_CONDENSED, "Condensed");

    private final int value;
    private final String label;

    GSRHudLookMode(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public Text getDisplayName() {
        return Text.literal(label);
    }

    public static GSRHudLookMode from(int value) {
        return value == GSRHudParameters.MODE_CONDENSED ? CONDENSED : FULL;
    }
}
