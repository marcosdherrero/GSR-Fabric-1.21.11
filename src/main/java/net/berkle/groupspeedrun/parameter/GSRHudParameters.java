package net.berkle.groupspeedrun.parameter;

/**
 * Parameters for the GSR timer HUD: display modes, scales, layout, visibility windows, and fade.
 * Used by {@link net.berkle.groupspeedrun.config.GSRConfigPlayer}, {@link net.berkle.groupspeedrun.timer.hud.GSRTimerHudRenderer},
 * and {@link net.berkle.groupspeedrun.util.GSRAlphaUtil}.
 */
public final class GSRHudParameters {

    private GSRHudParameters() {}

    // --- HUD Look (Full / Condensed) ---
    /** Always show timer and splits. */
    public static final int MODE_FULL = 0;
    /** Timer always visible; splits only during event window (start, stop, split). */
    public static final int MODE_CONDENSED = 1;

    // --- Scale bounds and default ---
    public static final float MIN_OVERALL_SCALE = 0.5f;
    public static final float MAX_OVERALL_SCALE = 2.5f;
    public static final float DEFAULT_SCALE = 1.0f;

    // --- Layout bounds and defaults ---
    public static final int MIN_HUD_PADDING = 0;
    public static final int MAX_HUD_PADDING = 24;
    public static final int DEFAULT_HUD_PADDING = 6;

    public static final int MIN_HUD_ROW_HEIGHT = 6;
    public static final int MAX_HUD_ROW_HEIGHT = 24;
    public static final int DEFAULT_HUD_ROW_HEIGHT = 10;

    /** Gap between name column and time column (pixels). Based on visible content; kept minimal. */
    public static final int MIN_HUD_SPLIT_GAP = 0;
    public static final int MAX_HUD_SPLIT_GAP = 60;
    public static final int DEFAULT_HUD_SPLIT_GAP = 2;

    public static final float MIN_SEPARATOR_ALPHA = 0f;
    public static final float MAX_SEPARATOR_ALPHA = 1f;
    public static final float DEFAULT_SEPARATOR_ALPHA = 0.31f;

    // --- Visibility window (ticks; 20 ticks = 1 second) ---
    /** How long split list stays visible after a split. Default 10 seconds. */
    public static final int MIN_SPLIT_SHOW_TICKS = 20;
    public static final int MAX_SPLIT_SHOW_TICKS = 1200;
    public static final int DEFAULT_SPLIT_SHOW_TICKS = 200;

    /** How long HUD stays visible after victory/fail. Default 30 seconds. */
    public static final int MIN_END_SHOW_TICKS = 60;
    public static final int MAX_END_SHOW_TICKS = 1800;
    public static final int DEFAULT_END_SHOW_TICKS = 600;

    // --- Fade (hold-to-show) ---
    /** Ticks over which hold-to-show fades in and out (symmetric: fade out = fade in reversed). */
    public static final int HOLD_FADE_TICKS = 20;
    /** Ticks over which event overlay fades in/out. */
    public static final int ANIMATION_FADE_TICKS = 20;

    // --- Timer box positioning (in-game HUD) ---
    /** Pixels from screen edge to timer box. */
    public static final int EDGE_MARGIN = 10;
    /** Vertical offset: box center is (screenHeight * VERTICAL_OFFSET_FACTOR) above center. */
    public static final float VERTICAL_OFFSET_FACTOR = 0.15f;

    /** Alpha below this is treated as not visible. */
    public static final float ALPHA_CUTOFF = 0.001f;

    // --- Timer box rendering (internal) ---
    /** Background opacity when paused (0x00–0xFF). */
    public static final int BG_OPACITY_PAUSED = 0x60;
    /** Background opacity when running (0x00–0xFF). */
    public static final int BG_OPACITY_RUNNING = 0x90;
    /** Text color when paused (ARGB). Used with GSRColorHelper.applyAlpha. */
    public static final int TEXT_COLOR_PAUSED = 0xFFB0B0B0;
    /** Text color when freeze (soft blue, ARGB). Used with GSRColorHelper.applyAlpha. */
    public static final int TEXT_COLOR_FREEZE = 0xFFB0D4E8;
    /** Text color when running (ARGB). Used with GSRColorHelper.applyAlpha. */
    public static final int TEXT_COLOR_RUNNING = 0xFFFFFFFF;
    /** Gap between title row and split rows (pixels). */
    public static final int TITLE_SPLIT_GAP = 4;
    /** Separator line thickness (pixels). */
    public static final int SEPARATOR_THICKNESS = 1;
    /** Separator inset from box edges (pixels). */
    public static final int SEPARATOR_INSET = 2;
    /** Gap between separator and first split row (pixels). */
    public static final int SEPARATOR_SPLIT_GAP = 3;
}
