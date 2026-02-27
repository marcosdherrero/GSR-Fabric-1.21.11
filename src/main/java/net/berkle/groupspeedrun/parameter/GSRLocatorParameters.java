package net.berkle.groupspeedrun.parameter;

/**
 * Parameters for the GSR locator HUD: structure compass bar scale, size, icon scaling, and per-structure icon items/colors.
 * Used by {@link net.berkle.groupspeedrun.config.GSRConfigPlayer} and the locator HUD mixin.
 */
public final class GSRLocatorParameters {

    private GSRLocatorParameters() {}

    // --- Per-structure icon items (registry IDs) ---
    public static final String DEFAULT_FORTRESS_ITEM = "minecraft:blaze_rod";
    public static final String DEFAULT_BASTION_ITEM = "minecraft:piglin_head";
    public static final String DEFAULT_STRONGHOLD_ITEM = "minecraft:ender_eye";
    public static final String DEFAULT_SHIP_ITEM = "minecraft:elytra";

    // --- Per-structure theme colors (0xRRGGBB, alpha added when rendering) ---
    public static final int DEFAULT_FORTRESS_COLOR = 0xFFFF5555;
    public static final int DEFAULT_BASTION_COLOR = 0xFFFFAA00;
    public static final int DEFAULT_STRONGHOLD_COLOR = 0xFF55FFFF;
    public static final int DEFAULT_SHIP_COLOR = 0xFFAA00AA;

    /** Vertical offset in pixels when the locate bar is at the top. */
    public static final int LOCATE_TOP_Y = 15;
    /** Vertical offset in pixels from bottom when the bar is at the bottom. */
    public static final int LOCATE_BOTTOM_OFFSET = 70;
    /** Height in pixels per boss bar row (for offset when bar is on top). */
    public static final int BOSS_BAR_ROW_HEIGHT = 19;

    public static final int MIN_BAR_WIDTH = 40;
    public static final int MAX_BAR_WIDTH = 200;
    public static final int DEFAULT_BAR_WIDTH = 120;
    public static final int MIN_BAR_HEIGHT = 2;
    public static final int MAX_BAR_HEIGHT = 6;
    public static final int DEFAULT_BAR_HEIGHT = 3;

    /** Min value for minimum scale distance (blocks). Icon stays at min scale until within this range. */
    public static final float MIN_MAX_SCALE_DIST = 100f;
    /** Max value for minimum scale distance (blocks). */
    public static final float MAX_MAX_SCALE_DIST = 5000f;
    /** Default minimum scale distance (blocks). Icon scales up when within this range. */
    public static final float DEFAULT_MAX_SCALE_DIST = 1000f;
    /** Distance (blocks) at or below which icon reaches maximum scale. */
    public static final float ICON_SCALE_NEAR_THRESHOLD = 25f;
    /** Max icon scale so item stays within box (9/8 = 1.125, from ICON_MARGIN/ICON_INNER_RADIUS). */
    public static final float ICON_MAX_SCALE_FIT_BOX = 9f / 8f;
    public static final float MIN_ICON_SCALE = 0.2f;
    public static final float MAX_ICON_SCALE = 3.0f;
    public static final float DEFAULT_MIN_ICON_SCALE = 0.3f;
    public static final float DEFAULT_MAX_ICON_SCALE = 1.0f;

    /** Ticks for locator bar fade-out when entering structure (3 seconds). */
    public static final int LOCATOR_FADE_TICKS = 60;

    // --- Locate logic (server) ---
    /** Chunk radius for structure locate search. */
    public static final int LOCATE_RADIUS_CHUNKS = 100;
    /** Horizontal radius (blocks) from ship position to trigger locator turn-off. Explicitly checks distance to ship. */
    public static final int SHIP_LOCATOR_TRIGGER_RADIUS_BLOCKS = 100;
    /** Chunk radius for ship search (end cities with ship). Outer islands are ~1000 blocks from center; need ~64+ chunks. */
    public static final int SHIP_LOCATE_SEARCH_RADIUS_CHUNKS = 80;
    /** Max chunks to check when searching for nearest ship. Prevents excessive iteration. */
    public static final int SHIP_LOCATE_MAX_CHUNKS = 2000;
    /** Locator gate: minutes after relevant split before locator unlocks (non-admin). */
    public static final long LOCATE_GATE_MINUTES = 30;
    public static final long LOCATE_GATE_MS = LOCATE_GATE_MINUTES * 60 * 1000;

    // --- Bar rendering (internal) ---
    /** Bar Y offset from anchor (pixels). */
    public static final int BAR_Y_OFFSET = 7;
    /** Returns the center Y of the locator bar for icon placement. Icons are centered on the bar. */
    public static float computeBarCenterY(float anchorY, float barHeight, float scale) {
        return anchorY + BAR_Y_OFFSET + (barHeight * scale) / 2f;
    }
    /** Icon margin from bar center for direction mapping (pixels). */
    public static final int ICON_MARGIN = 9;
    /** Angle (degrees) for icon direction normalization. */
    public static final float ICON_ANGLE_RANGE = 90.0f;
    /** Bar fill gradient start (0xRRGGBB). */
    public static final int BAR_GRADIENT_START = 0xFF333333;
    /** Bar fill gradient end (0xRRGGBB). */
    public static final int BAR_GRADIENT_END = 0xFF444444;
    /** Bar background (0xRRGGBB, alpha applied). */
    public static final int BAR_BG = 0xFF000000;
    public static final float BAR_BG_ALPHA = 0.5f;
    /** Bar top border (0xRRGGBB). */
    public static final int BAR_TOP_BORDER = 0xFFAAAAAA;
    /** Bar bottom border (0xRRGGBB). */
    public static final int BAR_BOTTOM_BORDER = 0xFF444444;
    /** Tick marks on bar (0xRRGGBB, alpha applied). */
    public static final int BAR_TICK_COLOR = 0xFFFFFFFF;
    public static final float BAR_TICK_ALPHA = 0.4f;
    /** Icon gray fill when looking away (0xRRGGBB). */
    public static final int ICON_GRAY_FILL = 0xFF555555;
    /** Icon inner size (half-width, pixels). */
    public static final int ICON_INNER_RADIUS = 8;
}
