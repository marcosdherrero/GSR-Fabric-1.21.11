package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRLocatorParameters;
import net.minecraft.text.Text;

/** Max scale distance options (500–5000 blocks). Used for single-selection in GSR Preferences. */
public enum GSRMaxScaleDistOption {
    D500(500f), D1000(1000f), D1500(1500f), D2000(2000f), D2500(2500f), D3000(3000f), D3500(3500f), D4000(4000f), D5000(5000f);

    private final float value;

    GSRMaxScaleDistOption(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public Text getDisplayName() {
        return Text.literal(String.valueOf((int) value));
    }

    public static GSRMaxScaleDistOption from(float value) {
        float clamped = Math.max(GSRLocatorParameters.MIN_MAX_SCALE_DIST, Math.min(GSRLocatorParameters.MAX_MAX_SCALE_DIST, value));
        GSRMaxScaleDistOption closest = D500;
        for (GSRMaxScaleDistOption opt : values()) {
            if (Math.abs(opt.value - clamped) < Math.abs(closest.value - clamped)) closest = opt;
        }
        return closest;
    }
}
