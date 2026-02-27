package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRHudParameters;
import net.minecraft.text.Text;

/** Separator alpha options (0–1). Used for single-selection in GSR Preferences. */
public enum GSRSeparatorAlphaOption {
    A0(0f), A25(0.25f), A31(0.31f), A50(0.5f), A75(0.75f), A100(1f);

    private final float value;

    GSRSeparatorAlphaOption(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public Text getDisplayName() {
        return Text.literal(String.format("%.2f", value));
    }

    public static GSRSeparatorAlphaOption from(float value) {
        float clamped = Math.max(GSRHudParameters.MIN_SEPARATOR_ALPHA, Math.min(GSRHudParameters.MAX_SEPARATOR_ALPHA, value));
        GSRSeparatorAlphaOption closest = A31;
        for (GSRSeparatorAlphaOption opt : values()) {
            if (Math.abs(opt.value - clamped) < Math.abs(closest.value - clamped)) closest = opt;
        }
        return closest;
    }
}
