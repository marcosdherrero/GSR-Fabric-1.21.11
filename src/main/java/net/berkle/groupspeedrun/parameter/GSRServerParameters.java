package net.berkle.groupspeedrun.parameter;

/**
 * Parameters for server-side run logic: save interval and split check interval.
 * Used by {@link net.berkle.groupspeedrun.GSRMain} and {@link net.berkle.groupspeedrun.GSREvents}.
 */
public final class GSRServerParameters {

    private GSRServerParameters() {}

    /** Save world config and stats every this many ticks. */
    public static final int SAVE_INTERVAL_TICKS = 100;
    /** Defer snapshot to this tick so early ticks (movement auto-start) are not blocked. */
    public static final int SNAPSHOT_DEFER_TICKS = 20;
    /** Check dimension splits every this many ticks. */
    public static final int SPLIT_CHECK_INTERVAL = 10;
    /** Check locator enter/fade (structure entry) every this many ticks. */
    public static final int LOCATOR_CHECK_INTERVAL = 10;
    /** Blocks beyond structure piece bounds that still trigger split (e.g. 5 = within 5 blocks). */
    public static final int SPLIT_STRUCTURE_PROXIMITY_BLOCKS = 5;
}
