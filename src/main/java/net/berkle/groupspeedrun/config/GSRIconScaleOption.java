package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRLocatorParameters;
import net.minecraft.text.Text;

/** Icon scale options (0.2–3.0) for Min/Max icon scale. Used for single-selection in GSR Preferences. */
public enum GSRIconScaleOption {
    I20(0.2f), I30(0.3f), I50(0.5f), I75(0.75f), I100(1.0f), I125(1.25f), I150(1.5f), I200(2.0f), I250(2.5f), I300(3.0f);

    private final float value;

    GSRIconScaleOption(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public Text getDisplayName() {
        return Text.literal(String.format("%.2f", value));
    }

    public static GSRIconScaleOption from(float value) {
        float clamped = Math.max(GSRLocatorParameters.MIN_ICON_SCALE, Math.min(GSRLocatorParameters.MAX_ICON_SCALE, value));
        GSRIconScaleOption closest = I100;
        for (GSRIconScaleOption opt : values()) {
            if (Math.abs(opt.value - clamped) < Math.abs(closest.value - clamped)) closest = opt;
        }
        return closest;
    }
}
