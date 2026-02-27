package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRHudParameters;
import net.minecraft.text.Text;

/** Split gap options (0–60 px). Used for single-selection in GSR Preferences. */
public enum GSRSplitGapOption {
    G0(0), G2(2), G5(5), G10(10), G15(15), G20(20), G30(30), G40(40), G50(50), G60(60);

    private final int value;

    GSRSplitGapOption(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public Text getDisplayName() {
        return Text.literal(String.valueOf(value));
    }

    public static GSRSplitGapOption from(int value) {
        int clamped = Math.max(GSRHudParameters.MIN_HUD_SPLIT_GAP, Math.min(GSRHudParameters.MAX_HUD_SPLIT_GAP, value));
        GSRSplitGapOption closest = G0;
        for (GSRSplitGapOption opt : values()) {
            if (Math.abs(opt.value - clamped) < Math.abs(closest.value - clamped)) closest = opt;
        }
        return closest;
    }
}
