package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRHudParameters;
import net.minecraft.text.Text;

/** Split show duration options (1–60 seconds). Displayed in seconds; stored as ticks internally. */
public enum GSRSplitShowTicksOption {
    S1(20), S5(100), S10(200), S15(300), S20(400), S30(600), S40(800), S50(1000), S60(1200);

    private final int ticks;

    GSRSplitShowTicksOption(int ticks) {
        this.ticks = ticks;
    }

    /** Returns ticks for internal use. */
    public int getValue() {
        return ticks;
    }

    public Text getDisplayName() {
        return Text.literal((ticks / 20) + " s");
    }

    public static GSRSplitShowTicksOption from(int ticks) {
        int clamped = Math.max(GSRHudParameters.MIN_SPLIT_SHOW_TICKS, Math.min(GSRHudParameters.MAX_SPLIT_SHOW_TICKS, ticks));
        GSRSplitShowTicksOption closest = S1;
        for (GSRSplitShowTicksOption opt : values()) {
            if (Math.abs(opt.ticks - clamped) < Math.abs(closest.ticks - clamped)) closest = opt;
        }
        return closest;
    }
}
