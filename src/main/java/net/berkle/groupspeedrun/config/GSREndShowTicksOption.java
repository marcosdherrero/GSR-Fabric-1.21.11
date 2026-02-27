package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRHudParameters;
import net.minecraft.text.Text;

/** End show duration options (3–90 seconds). Displayed in seconds; stored as ticks internally. */
public enum GSREndShowTicksOption {
    S3(60), S10(200), S20(400), S30(600), S45(900), S60(1200), S75(1500), S90(1800);

    private final int ticks;

    GSREndShowTicksOption(int ticks) {
        this.ticks = ticks;
    }

    /** Returns ticks for internal use. */
    public int getValue() {
        return ticks;
    }

    public Text getDisplayName() {
        return Text.literal((ticks / 20) + " s");
    }

    public static GSREndShowTicksOption from(int ticks) {
        int clamped = Math.max(GSRHudParameters.MIN_END_SHOW_TICKS, Math.min(GSRHudParameters.MAX_END_SHOW_TICKS, ticks));
        GSREndShowTicksOption closest = S3;
        for (GSREndShowTicksOption opt : values()) {
            if (Math.abs(opt.ticks - clamped) < Math.abs(closest.ticks - clamped)) closest = opt;
        }
        return closest;
    }
}
