package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRLocatorParameters;
import net.minecraft.text.Text;

/** Locator bar height options (2–6 px). Used for single-selection in GSR Preferences. */
public enum GSRBarHeightOption {
    H2(2), H3(3), H4(4), H5(5), H6(6);

    private final int value;

    GSRBarHeightOption(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public Text getDisplayName() {
        return Text.literal(String.valueOf(value));
    }

    public static GSRBarHeightOption from(int value) {
        int clamped = Math.max(GSRLocatorParameters.MIN_BAR_HEIGHT, Math.min(GSRLocatorParameters.MAX_BAR_HEIGHT, value));
        GSRBarHeightOption closest = H3;
        for (GSRBarHeightOption opt : values()) {
            if (Math.abs(opt.value - clamped) < Math.abs(closest.value - clamped)) closest = opt;
        }
        return closest;
    }
}
