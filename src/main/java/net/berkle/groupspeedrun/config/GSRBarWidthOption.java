package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRLocatorParameters;
import net.minecraft.text.Text;

/** Locator bar width options (40–200 px). Used for single-selection in GSR Preferences. */
public enum GSRBarWidthOption {
    W40(40), W80(80), W120(120), W160(160), W200(200);

    private final int value;

    GSRBarWidthOption(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public Text getDisplayName() {
        return Text.literal(String.valueOf(value));
    }

    public static GSRBarWidthOption from(int value) {
        int clamped = Math.max(GSRLocatorParameters.MIN_BAR_WIDTH, Math.min(GSRLocatorParameters.MAX_BAR_WIDTH, value));
        GSRBarWidthOption closest = W40;
        for (GSRBarWidthOption opt : values()) {
            if (Math.abs(opt.value - clamped) < Math.abs(closest.value - clamped)) closest = opt;
        }
        return closest;
    }
}
