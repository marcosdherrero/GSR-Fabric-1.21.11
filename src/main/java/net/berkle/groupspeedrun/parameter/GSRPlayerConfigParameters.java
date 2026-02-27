package net.berkle.groupspeedrun.parameter;

/**
 * NBT keys for per-player HUD config (GSRConfigPlayer).
 * Used by {@link net.berkle.groupspeedrun.config.GSRConfigPlayer} and {@link net.berkle.groupspeedrun.config.GSRConfigPayload}.
 */
public final class GSRPlayerConfigParameters {

    private GSRPlayerConfigParameters() {}

    // --- Timer HUD ---
    public static final String K_HUD_SCALE = "hudScale";
    public static final String K_TIMER_SCALE = "timerScale";
    public static final String K_TIMER_RIGHT = "timerRight";
    public static final String K_HUD_MODE = "hudMode";
    public static final String K_HUD_VISIBILITY = "hudVisibility";
    /** Legacy: hudHoldToShow (boolean) mapped to hudVisibility. */
    public static final String K_HUD_HOLD_TO_SHOW = "hudHoldToShow";

    // --- Locator HUD ---
    public static final String K_LOCATE_HUD_ON_TOP = "locateHudOnTop";
    public static final String K_LOCATE_SCALE = "locateScale";
    public static final String K_BAR_WIDTH = "barWidth";
    public static final String K_BAR_HEIGHT = "barHeight";
    public static final String K_MAX_SCALE_DIST = "maxScaleDistance";
    public static final String K_MIN_ICON_SCALE = "minIconScale";
    public static final String K_MAX_ICON_SCALE = "maxIconScale";
    public static final String K_FORTRESS_ITEM = "fortressItem";
    public static final String K_BASTION_ITEM = "bastionItem";
    public static final String K_STRONGHOLD_ITEM = "strongholdItem";
    public static final String K_SHIP_ITEM = "shipItem";
    public static final String K_FORTRESS_COLOR = "fortressColor";
    public static final String K_BASTION_COLOR = "bastionColor";
    public static final String K_STRONGHOLD_COLOR = "strongholdColor";
    public static final String K_SHIP_COLOR = "shipColor";

    // --- Timer HUD colors (ARGB; Default + ink options like locator) ---
    public static final String K_TIMER_COLOR_RUNNING = "timerColorRunning";
    public static final String K_TIMER_COLOR_DERANKED = "timerColorDeranked";
    public static final String K_TIMER_COLOR_FREEZE = "timerColorFreeze";
    public static final String K_TIMER_COLOR_PAUSED = "timerColorPaused";
    public static final String K_TIMER_COLOR_FAIL = "timerColorFail";
    public static final String K_TIMER_COLOR_VICTORY = "timerColorVictory";
    /** Per-player-per-world locator toggles. */
    public static final String K_FORTRESS_LOCATOR_ON = "fortressLocatorOn";
    public static final String K_BASTION_LOCATOR_ON = "bastionLocatorOn";
    public static final String K_STRONGHOLD_LOCATOR_ON = "strongholdLocatorOn";
    public static final String K_SHIP_LOCATOR_ON = "shipLocatorOn";

    // --- Styling ---
    public static final String K_HUD_PADDING = "hudPadding";
    public static final String K_HUD_ROW_HEIGHT = "hudRowHeight";
    public static final String K_HUD_SPLIT_GAP = "hudSplitGap";
    public static final String K_HUD_SEPARATOR_ALPHA = "hudSeparatorAlpha";
    public static final String K_SPLIT_SHOW_TICKS = "splitShowTicks";
    public static final String K_END_SHOW_TICKS = "endShowTicks";

    // --- New World ---
    /** When true (host only): allow New World key before run has ended. Default false. */
    public static final String K_ALLOW_NEW_WORLD_BEFORE_RUN_END = "allowNewWorldBeforeRunEnd";

    // --- Permissions (server-sent, read-only on client) ---
    /** When true: player has admin permission (ADMINS_CHECK). Used to gray out Pause/Resume/Reset. */
    public static final String K_CAN_USE_ADMIN = "canUseAdmin";
}
