package net.berkle.groupspeedrun.parameter;

/**
 * Centralized configuration for GSR timer: HUD labels, icons, default values, and split names.
 * All timer display strings and symbols are mapped here per spec (config-driven approach).
 * Used by {@link net.berkle.groupspeedrun.timer.hud.GSRTimerHudRenderer} and timer state logic.
 */
public final class GSRTimerConfig {

    private GSRTimerConfig() {}

    // --- HUD labels (state-specific) ---
    /** Primed state: run created but not active. Awaiting trigger. */
    public static final String LABEL_PRIMED = "Timer:";
    /** Active state: run in progress. Shown with stopwatch icon. */
    public static final String LABEL_ACTIVE = "Time:";
    /** Paused state: timer frozen by admin manual pause. */
    public static final String LABEL_PAUSED = "Paused:";
    /** Freeze state: timer frozen by server stop or world/pause menu (not manual). */
    public static final String LABEL_FREEZE = "Freeze:";
    /** Victory state: dragon killed. */
    public static final String LABEL_VICTORY = "Victory!";
    /** Fail state: run death. */
    public static final String LABEL_FAIL = "FAIL:";

    // --- Icons (Unicode; Minecraft default font supports these) ---
    /** Dragon icon for victory label. U+1F409. */
    public static final String ICON_DRAGON = "\uD83D\uDC09";
    /** Skull icon for fail label. U+1F480. */
    public static final String ICON_SKULL = "\uD83D\uDC80";
    /** Tight snowflake icon for freeze label. U+2745. */
    public static final String ICON_ICE = "\u2745";
    /** Stopwatch icon for running timer. U+23F1. */
    public static final String ICON_CLOCK = "\u23F1";
    /** White flag when run is deranked (invalid for ranking). U+1F3F3. */
    public static final String ICON_DERANKED = "\uD83C\uDFF3";
    /** Hourglass icon for manual pause. U+231B. */
    public static final String ICON_PAUSE = "\u231B";

    // --- Default primed time display ---
    /** Formatted time shown when run is primed (not started). */
    public static final String DEFAULT_PRIMED_TIME = "00.00";

    // --- Split names (for HUD split rows) ---
    public static final String SPLIT_NETHER = "Nether";
    public static final String SPLIT_BASTION = "Bastion";
    public static final String SPLIT_FORTRESS = "Fortress";
    public static final String SPLIT_END = "The End";
    public static final String SPLIT_DRAGON = "Dragon";

    // --- Format codes (Minecraft color codes) ---
    /** Gold for victory, dragon label, gold star. §6 fallback when ARGB not used. */
    public static final String COLOR_GOLD = "§6";
    /** Brown for deranked run (invalid for ranking). §6 fallback when ARGB not used. */
    public static final String COLOR_DERANKED = "§6";

    // --- ARGB colors (custom hex for timer title/time; used with DrawContext) ---
    /** Running (ranked). #DDD605. */
    public static final int COLOR_RUNNING_ARGB = 0xFFDDD605;
    /** Deranked run. #B4684D. */
    public static final int COLOR_DERANKED_ARGB = 0xFFB4684D;
    /** Freeze (server/world pause). #2CBAA8. */
    public static final int COLOR_FREEZE_ARGB = 0xFF2CBAA8;
    /** Paused (manual). #CECACA. */
    public static final int COLOR_PAUSED_ARGB = 0xFFCECACA;
    /** Fail. #FF5555. */
    public static final int COLOR_FAIL_ARGB = 0xFFFF5555;
    /** Victory. #FFAA00. */
    public static final int COLOR_VICTORY_ARGB = 0xFFFFAA00;

    /** Red for fail. §c fallback. */
    public static final String COLOR_RED = "§c";
    /** Gray for paused. §7 fallback. */
    public static final String COLOR_PAUSED = "§7";
    /** Soft blue for freeze. §b fallback. */
    public static final String COLOR_FREEZE = "§b";
    /** Gray for incomplete splits. */
    public static final String COLOR_INCOMPLETE = "§7";
    /** Green for completed splits (checkmark). */
    public static final String COLOR_GREEN = "§a";
    /** White for active timer. */
    public static final String COLOR_ACTIVE = "§f";
    /** Bold for title labels. */
    public static final String STYLE_BOLD = "§l";
}
