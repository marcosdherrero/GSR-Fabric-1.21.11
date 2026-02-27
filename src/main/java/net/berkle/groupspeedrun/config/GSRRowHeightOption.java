package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRHudParameters;
import net.minecraft.text.Text;

/** HUD row height options (6–24 px). Used for single-selection in GSR Preferences. */
public enum GSRRowHeightOption {
    R6(6), R8(8), R10(10), R12(12), R14(14), R16(16), R18(18), R20(20), R22(22), R24(24);

    private final int value;

    GSRRowHeightOption(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public Text getDisplayName() {
        return Text.literal(String.valueOf(value));
    }

    public static GSRRowHeightOption from(int value) {
        int clamped = Math.max(GSRHudParameters.MIN_HUD_ROW_HEIGHT, Math.min(GSRHudParameters.MAX_HUD_ROW_HEIGHT, value));
        GSRRowHeightOption closest = R10;
        for (GSRRowHeightOption opt : values()) {
            if (Math.abs(opt.value - clamped) < Math.abs(closest.value - clamped)) closest = opt;
        }
        return closest;
    }
}
