package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRHudParameters;
import net.minecraft.text.Text;

/** HUD padding options (0–24 px). Used for single-selection in GSR Preferences. */
public enum GSRHudPaddingOption {
    P0(0), P4(4), P6(6), P8(8), P12(12), P16(16), P20(20), P24(24);

    private final int value;

    GSRHudPaddingOption(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public Text getDisplayName() {
        return Text.literal(String.valueOf(value));
    }

    public static GSRHudPaddingOption from(int value) {
        int clamped = Math.max(GSRHudParameters.MIN_HUD_PADDING, Math.min(GSRHudParameters.MAX_HUD_PADDING, value));
        GSRHudPaddingOption closest = P0;
        for (GSRHudPaddingOption opt : values()) {
            if (Math.abs(opt.value - clamped) < Math.abs(closest.value - clamped)) closest = opt;
        }
        return closest;
    }
}
