package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRHudParameters;
import net.minecraft.text.Text;

/** Scale options (0.5–2.5) for Overall/Timer/Locate scale. Used for single-selection in GSR Preferences. */
public enum GSRScaleOption {
    S050(0.5f), S075(0.75f), S100(1.0f), S125(1.25f), S150(1.5f), S175(1.75f), S200(2.0f), S225(2.25f), S250(2.5f);

    private final float value;

    GSRScaleOption(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public Text getDisplayName() {
        return Text.literal(String.format("%.2f", value));
    }

    public static GSRScaleOption from(float value) {
        float clamped = Math.max(GSRHudParameters.MIN_OVERALL_SCALE, Math.min(GSRHudParameters.MAX_OVERALL_SCALE, value));
        GSRScaleOption closest = S100;
        for (GSRScaleOption opt : values()) {
            if (Math.abs(opt.value - clamped) < Math.abs(closest.value - clamped)) closest = opt;
        }
        return closest;
    }
}
