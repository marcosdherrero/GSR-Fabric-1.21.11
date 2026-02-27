package net.berkle.groupspeedrun.parameter;

/**
 * Standardized parameters for all GSR tooltips project-wide.
 * Ensures 35% max screen size, 2.5s scroll delay, cyclical scroll, and divider bar.
 */
public final class GSRTooltipParameters {

    private GSRTooltipParameters() {}

    /** Max width/height as fraction of screen (0.35 = 35%). */
    public static final float MAX_SCREEN_FRACTION = 0.35f;
    /** Player tooltip max width fraction (wider for more readable text). */
    public static final float PLAYER_TOOLTIP_MAX_WIDTH_FRACTION = 0.45f;
    /** When true, player chart tooltip swaps width/height (rotated aspect) for more vertical space. */
    public static final boolean PLAYER_TOOLTIP_SWAP_ASPECT = false;
    /** Minimum tooltip box width in pixels. */
    public static final int MIN_BOX_WIDTH = 80;
    /** Minimum tooltip box height in pixels. */
    public static final int MIN_BOX_HEIGHT = 60;
    /** Pause at top (ms) before starting scroll, and again each time content returns to top. */
    public static final int SCROLL_START_DELAY_MS = 2500;
    /** Vertical scroll cycle duration in ms (cyclical loop). */
    public static final int SCROLL_CYCLE_MS = 16000;
    /** Inner padding in pixels. */
    public static final int PADDING = 8;
    /** Cursor-offset positioner: pixels from cursor to tooltip top-left. */
    public static final int CURSOR_OFFSET = 12;
    /** Size of player face in pixels for player tooltip header (40% of original 32px). */
    public static final int PLAYER_FACE_SIZE = 13;
    /** Gap between player face and name in player tooltip header (pixels). */
    public static final int PLAYER_FACE_NAME_GAP = 6;
    /** Margin on all sides of player face and name container in tooltip (pixels). */
    public static final int PLAYER_HEADER_MARGIN = 4;
    /** Height of mini chart in player tooltip (zoomed-in bar, pixels). */
    public static final int PLAYER_TOOLTIP_CHART_HEIGHT = 48;
    /** Vertical gap between mini chart and player header in tooltip (pixels). */
    public static final int PLAYER_TOOLTIP_CHART_HEADER_GAP = 6;
    /** Vertical gap between player name and stats row (pixels). */
    public static final int PLAYER_STATS_ROW_GAP = 4;
    /** Vertical gap between stats row and score lines (pixels). */
    public static final int PLAYER_SCORES_GAP = 6;
    /** Separator between repeating ticker segments (space-pipe-space). */
    public static final String TICKER_SEPARATOR = " | ";
    /** Horizontal ticker scroll speed in pixels per ms. Same for both rows. */
    public static final float TICKER_SPEED_PX_PER_MS = 0.015f;
    /** Horizontal margin (px) on each side of the cyclical-scroll divider bar. */
    public static final int SCROLL_DIVIDER_MARGIN = 4;
    /** Minimum distance from screen edge (pixels). Used when flipping tooltip and clamping. */
    public static final int EDGE_MARGIN = 4;

    /** Background color (ARGB). */
    public static final int BG = 0xF0101010;
    /** Border color (ARGB). */
    public static final int BORDER = 0xFF505050;
    /** Text color (ARGB). */
    public static final int TEXT_COLOR = 0xFFFFFFFF;
}
