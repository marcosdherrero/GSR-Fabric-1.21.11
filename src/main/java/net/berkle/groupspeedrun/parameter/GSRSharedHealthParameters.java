package net.berkle.groupspeedrun.parameter;

/**
 * Parameters for shared health eat limit: activity-based allowance so passive players
 * (hiding, not moving) cannot eat without contributing exhaustion (sprint, mine, jump, etc.).
 * Used by {@link net.berkle.groupspeedrun.server.GSRSharedHealthEatAllowance}.
 */
public final class GSRSharedHealthParameters {

    private GSRSharedHealthParameters() {}

    /** Ticks after taking damage during which eating is unrestricted (player is in combat). 20 ticks = 1 second. */
    public static final int DAMAGE_EXEMPT_TICKS = 600;
    /** Exhaustion cost per nutrition point to eat. Player must have allowance >= nutrition * this. */
    public static final float EXHAUSTION_PER_NUTRITION = 0.5f;
    /** Max eat allowance (exhaustion units). Prevents stockpiling from long activity. */
    public static final float MAX_EAT_ALLOWANCE = 20f;
    /** Message when eating is blocked due to insufficient activity. */
    public static final String MSG_NEED_ACTIVITY = "You need to be more active to eat (sprint, mine, jump, attack).";
}
