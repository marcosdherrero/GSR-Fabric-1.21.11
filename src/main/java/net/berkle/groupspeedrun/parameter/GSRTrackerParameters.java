package net.berkle.groupspeedrun.parameter;

/**
 * Parameters for tracker mixins: movement jitter tolerance and teleport threshold.
 * Used by {@link net.berkle.groupspeedrun.mixin.trackers.GSRServerPlayerEntityTracker}.
 */
public final class GSRTrackerParameters {

    private GSRTrackerParameters() {}

    /** Distance below which movement is considered jitter (ignored for stat tracking). */
    public static final double JITTER = 0.00001;
    /** Distance above which movement is treated as a teleport (e.g. dimension change, spawn). */
    public static final double TELEPORT_THRESHOLD = 20.0;
    /** Ticks in world before movement can trigger auto-start; allows spawn settling on fresh world load. */
    public static final int ARMED_WARMUP_TICKS = 40;
}
