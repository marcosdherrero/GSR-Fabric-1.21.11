package net.berkle.groupspeedrun.config;

import net.minecraft.text.Text;

/**
 * HUD Visibility mode: Toggle (V to toggle) or Pressed (Tab to hold).
 */
public enum GSRHudVisibilityMode {
    TOGGLE(GSRConfigPlayer.VISIBILITY_TOGGLE, "Toggle (V)"),
    PRESSED(GSRConfigPlayer.VISIBILITY_PRESSED, "Hold (Tab)");

    private final int value;
    private final String label;

    GSRHudVisibilityMode(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return value;
    }

    public Text getDisplayName() {
        return Text.literal(label);
    }

    public static GSRHudVisibilityMode from(int value) {
        return value == GSRConfigPlayer.VISIBILITY_PRESSED ? PRESSED : TOGGLE;
    }
}
