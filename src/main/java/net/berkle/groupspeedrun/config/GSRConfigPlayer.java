package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRHudParameters;
import net.berkle.groupspeedrun.parameter.GSRLocatorParameters;
import net.berkle.groupspeedrun.parameter.GSRPlayerConfigParameters;
import net.berkle.groupspeedrun.parameter.GSRTimerConfig;
import net.minecraft.nbt.NbtCompound;

/**
 * Per-player HUD config (stored per UUID on server). Timer and styling fields.
 * Bounds and defaults are defined in {@link GSRHudParameters}.
 */
public class GSRConfigPlayer {

    // Re-export for backward compatibility; values from GSRHudParameters
    public static final int MODE_FULL = GSRHudParameters.MODE_FULL;
    public static final int MODE_CONDENSED = GSRHudParameters.MODE_CONDENSED;

    /** HUD visibility: 0 = Toggle (stays on until toggled), 1 = Pressed (hold to show, hidden when released, alpha fade). */
    public static final int VISIBILITY_TOGGLE = 0;
    public static final int VISIBILITY_PRESSED = 1;

    public static final float MIN_OVERALL_SCALE = GSRHudParameters.MIN_OVERALL_SCALE;
    public static final float MAX_OVERALL_SCALE = GSRHudParameters.MAX_OVERALL_SCALE;
    public static final float DEFAULT_SCALE = GSRHudParameters.DEFAULT_SCALE;

    public static final int MIN_HUD_PADDING = GSRHudParameters.MIN_HUD_PADDING;
    public static final int MAX_HUD_PADDING = GSRHudParameters.MAX_HUD_PADDING;
    public static final int MIN_HUD_ROW_HEIGHT = GSRHudParameters.MIN_HUD_ROW_HEIGHT;
    public static final int MAX_HUD_ROW_HEIGHT = GSRHudParameters.MAX_HUD_ROW_HEIGHT;
    public static final int MIN_HUD_SPLIT_GAP = GSRHudParameters.MIN_HUD_SPLIT_GAP;
    public static final int MAX_HUD_SPLIT_GAP = GSRHudParameters.MAX_HUD_SPLIT_GAP;
    public static final float MIN_SEPARATOR_ALPHA = GSRHudParameters.MIN_SEPARATOR_ALPHA;
    public static final float MAX_SEPARATOR_ALPHA = GSRHudParameters.MAX_SEPARATOR_ALPHA;
    public static final int MIN_SPLIT_SHOW_TICKS = GSRHudParameters.MIN_SPLIT_SHOW_TICKS;
    public static final int MAX_SPLIT_SHOW_TICKS = GSRHudParameters.MAX_SPLIT_SHOW_TICKS;
    public static final int MIN_END_SHOW_TICKS = GSRHudParameters.MIN_END_SHOW_TICKS;
    public static final int MAX_END_SHOW_TICKS = GSRHudParameters.MAX_END_SHOW_TICKS;

    public static final int MIN_BAR_WIDTH = GSRLocatorParameters.MIN_BAR_WIDTH;
    public static final int MAX_BAR_WIDTH = GSRLocatorParameters.MAX_BAR_WIDTH;
    public static final int MIN_BAR_HEIGHT = GSRLocatorParameters.MIN_BAR_HEIGHT;
    public static final int MAX_BAR_HEIGHT = GSRLocatorParameters.MAX_BAR_HEIGHT;
    public static final float MIN_MAX_SCALE_DIST = GSRLocatorParameters.MIN_MAX_SCALE_DIST;
    public static final float MAX_MAX_SCALE_DIST = GSRLocatorParameters.MAX_MAX_SCALE_DIST;
    public static final float MIN_ICON_SCALE = GSRLocatorParameters.MIN_ICON_SCALE;
    public static final float MAX_ICON_SCALE = GSRLocatorParameters.MAX_ICON_SCALE;

    public float hudOverallScale = GSRHudParameters.DEFAULT_SCALE;
    public float timerScale = GSRHudParameters.DEFAULT_SCALE;
    public int hudMode = GSRHudParameters.MODE_FULL;
    /** 0 = Toggle (stays on until toggled), 1 = Pressed (hold to show). */
    public int hudVisibility = VISIBILITY_PRESSED;
    /** Timer side; NBT key "timerRight" for sync. */
    public boolean timerHudOnRight = true;

    /** Locator HUD: bar at top (true) or bottom (false). */
    public boolean locateHudOnTop = true;
    public float locateScale = GSRHudParameters.DEFAULT_SCALE;
    public int barWidth = GSRLocatorParameters.DEFAULT_BAR_WIDTH;
    public int barHeight = GSRLocatorParameters.DEFAULT_BAR_HEIGHT;
    public float maxScaleDistance = GSRLocatorParameters.DEFAULT_MAX_SCALE_DIST;
    public float minIconScale = GSRLocatorParameters.DEFAULT_MIN_ICON_SCALE;
    public float maxIconScale = GSRLocatorParameters.DEFAULT_MAX_ICON_SCALE;

    /** Per-structure icon items (registry IDs, e.g. minecraft:blaze_rod). */
    public String fortressItem = GSRLocatorParameters.DEFAULT_FORTRESS_ITEM;
    public String bastionItem = GSRLocatorParameters.DEFAULT_BASTION_ITEM;
    public String strongholdItem = GSRLocatorParameters.DEFAULT_STRONGHOLD_ITEM;
    public String shipItem = GSRLocatorParameters.DEFAULT_SHIP_ITEM;

    /** Per-structure theme colors (0xRRGGBB). */
    public int fortressColor = GSRLocatorParameters.DEFAULT_FORTRESS_COLOR;
    public int bastionColor = GSRLocatorParameters.DEFAULT_BASTION_COLOR;
    public int strongholdColor = GSRLocatorParameters.DEFAULT_STRONGHOLD_COLOR;
    public int shipColor = GSRLocatorParameters.DEFAULT_SHIP_COLOR;

    /** Timer HUD state colors (ARGB). Defaults from GSRTimerConfig; customizable via Default + ink options. */
    public int timerColorRunning = GSRTimerConfig.COLOR_RUNNING_ARGB;
    public int timerColorDeranked = GSRTimerConfig.COLOR_DERANKED_ARGB;
    public int timerColorFreeze = GSRTimerConfig.COLOR_FREEZE_ARGB;
    public int timerColorPaused = GSRTimerConfig.COLOR_PAUSED_ARGB;
    public int timerColorFail = GSRTimerConfig.COLOR_FAIL_ARGB;
    public int timerColorVictory = GSRTimerConfig.COLOR_VICTORY_ARGB;

    /** Per-player locator toggles (per-world; stored in world's players.json). */
    public boolean fortressLocatorOn = false;
    public boolean bastionLocatorOn = false;
    public boolean strongholdLocatorOn = false;
    public boolean shipLocatorOn = false;

    public int hudPadding = GSRHudParameters.DEFAULT_HUD_PADDING;
    public int hudRowHeight = GSRHudParameters.DEFAULT_HUD_ROW_HEIGHT;
    public int hudSplitGap = GSRHudParameters.DEFAULT_HUD_SPLIT_GAP;
    public float hudSeparatorAlpha = GSRHudParameters.DEFAULT_SEPARATOR_ALPHA;
    public int splitShowTicks = GSRHudParameters.DEFAULT_SPLIT_SHOW_TICKS;
    public int endShowTicks = GSRHudParameters.DEFAULT_END_SHOW_TICKS;

    /** When true (host only): allow New World key before run has ended. Default false. */
    public boolean allowNewWorldBeforeRunEnd = false;

    /** When true: player has admin permission (ADMINS_CHECK). Server-sent; not written by client. */
    public boolean canUseAdmin = false;

    public static int clampHudMode(int value) {
        if (value < MODE_FULL) return MODE_FULL;
        if (value > MODE_CONDENSED) return MODE_CONDENSED;
        return value;
    }

    public static int clampHudVisibility(int value) {
        return (value == VISIBILITY_PRESSED) ? VISIBILITY_PRESSED : VISIBILITY_TOGGLE;
    }

    public static float clampOverallScale(float value) {
        return Math.max(MIN_OVERALL_SCALE, Math.min(MAX_OVERALL_SCALE, value));
    }

    public void clampAll() {
        hudOverallScale = clampOverallScale(hudOverallScale);
        timerScale = clampOverallScale(timerScale);
        hudMode = clampHudMode(hudMode);
        hudVisibility = clampHudVisibility(hudVisibility);
        locateScale = clampOverallScale(locateScale);
        barWidth = Math.max(MIN_BAR_WIDTH, Math.min(MAX_BAR_WIDTH, barWidth));
        barHeight = Math.max(MIN_BAR_HEIGHT, Math.min(MAX_BAR_HEIGHT, barHeight));
        maxScaleDistance = Math.max(MIN_MAX_SCALE_DIST, Math.min(MAX_MAX_SCALE_DIST, maxScaleDistance));
        minIconScale = Math.max(MIN_ICON_SCALE, Math.min(MAX_ICON_SCALE, minIconScale));
        maxIconScale = Math.max(MIN_ICON_SCALE, Math.min(MAX_ICON_SCALE, maxIconScale));
        hudPadding = Math.max(MIN_HUD_PADDING, Math.min(MAX_HUD_PADDING, hudPadding));
        hudRowHeight = Math.max(MIN_HUD_ROW_HEIGHT, Math.min(MAX_HUD_ROW_HEIGHT, hudRowHeight));
        hudSplitGap = Math.max(MIN_HUD_SPLIT_GAP, Math.min(MAX_HUD_SPLIT_GAP, hudSplitGap));
        hudSeparatorAlpha = Math.max(MIN_SEPARATOR_ALPHA, Math.min(MAX_SEPARATOR_ALPHA, hudSeparatorAlpha));
        splitShowTicks = Math.max(MIN_SPLIT_SHOW_TICKS, Math.min(MAX_SPLIT_SHOW_TICKS, splitShowTicks));
        endShowTicks = Math.max(MIN_END_SHOW_TICKS, Math.min(MAX_END_SHOW_TICKS, endShowTicks));
    }

    public void writeNbt(NbtCompound nbt) {
        nbt.putFloat(GSRPlayerConfigParameters.K_HUD_SCALE, hudOverallScale);
        nbt.putFloat(GSRPlayerConfigParameters.K_TIMER_SCALE, timerScale);
        nbt.putBoolean(GSRPlayerConfigParameters.K_TIMER_RIGHT, timerHudOnRight);
        nbt.putInt(GSRPlayerConfigParameters.K_HUD_MODE, hudMode);
        nbt.putInt(GSRPlayerConfigParameters.K_HUD_VISIBILITY, hudVisibility);
        nbt.putBoolean(GSRPlayerConfigParameters.K_LOCATE_HUD_ON_TOP, locateHudOnTop);
        nbt.putFloat(GSRPlayerConfigParameters.K_LOCATE_SCALE, locateScale);
        nbt.putInt(GSRPlayerConfigParameters.K_BAR_WIDTH, barWidth);
        nbt.putInt(GSRPlayerConfigParameters.K_BAR_HEIGHT, barHeight);
        nbt.putFloat(GSRPlayerConfigParameters.K_MAX_SCALE_DIST, maxScaleDistance);
        nbt.putFloat(GSRPlayerConfigParameters.K_MIN_ICON_SCALE, minIconScale);
        nbt.putFloat(GSRPlayerConfigParameters.K_MAX_ICON_SCALE, maxIconScale);
        nbt.putString(GSRPlayerConfigParameters.K_FORTRESS_ITEM, fortressItem != null ? fortressItem : GSRLocatorParameters.DEFAULT_FORTRESS_ITEM);
        nbt.putString(GSRPlayerConfigParameters.K_BASTION_ITEM, bastionItem != null ? bastionItem : GSRLocatorParameters.DEFAULT_BASTION_ITEM);
        nbt.putString(GSRPlayerConfigParameters.K_STRONGHOLD_ITEM, strongholdItem != null ? strongholdItem : GSRLocatorParameters.DEFAULT_STRONGHOLD_ITEM);
        nbt.putString(GSRPlayerConfigParameters.K_SHIP_ITEM, shipItem != null ? shipItem : GSRLocatorParameters.DEFAULT_SHIP_ITEM);
        nbt.putInt(GSRPlayerConfigParameters.K_FORTRESS_COLOR, fortressColor);
        nbt.putInt(GSRPlayerConfigParameters.K_BASTION_COLOR, bastionColor);
        nbt.putInt(GSRPlayerConfigParameters.K_STRONGHOLD_COLOR, strongholdColor);
        nbt.putInt(GSRPlayerConfigParameters.K_SHIP_COLOR, shipColor);
        nbt.putInt(GSRPlayerConfigParameters.K_TIMER_COLOR_RUNNING, timerColorRunning);
        nbt.putInt(GSRPlayerConfigParameters.K_TIMER_COLOR_DERANKED, timerColorDeranked);
        nbt.putInt(GSRPlayerConfigParameters.K_TIMER_COLOR_FREEZE, timerColorFreeze);
        nbt.putInt(GSRPlayerConfigParameters.K_TIMER_COLOR_PAUSED, timerColorPaused);
        nbt.putInt(GSRPlayerConfigParameters.K_TIMER_COLOR_FAIL, timerColorFail);
        nbt.putInt(GSRPlayerConfigParameters.K_TIMER_COLOR_VICTORY, timerColorVictory);
        nbt.putBoolean(GSRPlayerConfigParameters.K_FORTRESS_LOCATOR_ON, fortressLocatorOn);
        nbt.putBoolean(GSRPlayerConfigParameters.K_BASTION_LOCATOR_ON, bastionLocatorOn);
        nbt.putBoolean(GSRPlayerConfigParameters.K_STRONGHOLD_LOCATOR_ON, strongholdLocatorOn);
        nbt.putBoolean(GSRPlayerConfigParameters.K_SHIP_LOCATOR_ON, shipLocatorOn);
        nbt.putInt(GSRPlayerConfigParameters.K_HUD_PADDING, hudPadding);
        nbt.putInt(GSRPlayerConfigParameters.K_HUD_ROW_HEIGHT, hudRowHeight);
        nbt.putInt(GSRPlayerConfigParameters.K_HUD_SPLIT_GAP, hudSplitGap);
        nbt.putFloat(GSRPlayerConfigParameters.K_HUD_SEPARATOR_ALPHA, hudSeparatorAlpha);
        nbt.putInt(GSRPlayerConfigParameters.K_SPLIT_SHOW_TICKS, splitShowTicks);
        nbt.putInt(GSRPlayerConfigParameters.K_END_SHOW_TICKS, endShowTicks);
        nbt.putBoolean(GSRPlayerConfigParameters.K_ALLOW_NEW_WORLD_BEFORE_RUN_END, allowNewWorldBeforeRunEnd);
    }

    public void readNbt(NbtCompound nbt) {
        if (nbt == null) return;
        nbt.getFloat(GSRPlayerConfigParameters.K_HUD_SCALE).ifPresent(v -> this.hudOverallScale = v);
        nbt.getFloat(GSRPlayerConfigParameters.K_TIMER_SCALE).ifPresent(v -> this.timerScale = v);
        nbt.getBoolean(GSRPlayerConfigParameters.K_TIMER_RIGHT).ifPresent(v -> this.timerHudOnRight = v);
        nbt.getInt(GSRPlayerConfigParameters.K_HUD_MODE).ifPresent(v -> this.hudMode = v);
        nbt.getInt(GSRPlayerConfigParameters.K_HUD_VISIBILITY).ifPresent(v -> this.hudVisibility = clampHudVisibility(v));
        nbt.getBoolean(GSRPlayerConfigParameters.K_HUD_HOLD_TO_SHOW).ifPresent(v -> this.hudVisibility = v ? VISIBILITY_PRESSED : VISIBILITY_TOGGLE);
        nbt.getBoolean(GSRPlayerConfigParameters.K_LOCATE_HUD_ON_TOP).ifPresent(v -> this.locateHudOnTop = v);
        nbt.getFloat(GSRPlayerConfigParameters.K_LOCATE_SCALE).ifPresent(v -> this.locateScale = v);
        nbt.getInt(GSRPlayerConfigParameters.K_BAR_WIDTH).ifPresent(v -> this.barWidth = v);
        nbt.getInt(GSRPlayerConfigParameters.K_BAR_HEIGHT).ifPresent(v -> this.barHeight = v);
        nbt.getFloat(GSRPlayerConfigParameters.K_MAX_SCALE_DIST).ifPresent(v -> this.maxScaleDistance = v);
        nbt.getFloat(GSRPlayerConfigParameters.K_MIN_ICON_SCALE).ifPresent(v -> this.minIconScale = v);
        nbt.getFloat(GSRPlayerConfigParameters.K_MAX_ICON_SCALE).ifPresent(v -> this.maxIconScale = v);
        nbt.getString(GSRPlayerConfigParameters.K_FORTRESS_ITEM).ifPresent(v -> this.fortressItem = v);
        nbt.getString(GSRPlayerConfigParameters.K_BASTION_ITEM).ifPresent(v -> this.bastionItem = v);
        nbt.getString(GSRPlayerConfigParameters.K_STRONGHOLD_ITEM).ifPresent(v -> this.strongholdItem = v);
        nbt.getString(GSRPlayerConfigParameters.K_SHIP_ITEM).ifPresent(v -> this.shipItem = v);
        nbt.getInt(GSRPlayerConfigParameters.K_FORTRESS_COLOR).ifPresent(v -> this.fortressColor = v);
        nbt.getInt(GSRPlayerConfigParameters.K_BASTION_COLOR).ifPresent(v -> this.bastionColor = v);
        nbt.getInt(GSRPlayerConfigParameters.K_STRONGHOLD_COLOR).ifPresent(v -> this.strongholdColor = v);
        nbt.getInt(GSRPlayerConfigParameters.K_SHIP_COLOR).ifPresent(v -> this.shipColor = v);
        nbt.getInt(GSRPlayerConfigParameters.K_TIMER_COLOR_RUNNING).ifPresent(v -> this.timerColorRunning = v);
        nbt.getInt(GSRPlayerConfigParameters.K_TIMER_COLOR_DERANKED).ifPresent(v -> this.timerColorDeranked = v);
        nbt.getInt(GSRPlayerConfigParameters.K_TIMER_COLOR_FREEZE).ifPresent(v -> this.timerColorFreeze = v);
        nbt.getInt(GSRPlayerConfigParameters.K_TIMER_COLOR_PAUSED).ifPresent(v -> this.timerColorPaused = v);
        nbt.getInt(GSRPlayerConfigParameters.K_TIMER_COLOR_FAIL).ifPresent(v -> this.timerColorFail = v);
        nbt.getInt(GSRPlayerConfigParameters.K_TIMER_COLOR_VICTORY).ifPresent(v -> this.timerColorVictory = v);
        nbt.getBoolean(GSRPlayerConfigParameters.K_FORTRESS_LOCATOR_ON).ifPresent(v -> this.fortressLocatorOn = v);
        nbt.getBoolean(GSRPlayerConfigParameters.K_BASTION_LOCATOR_ON).ifPresent(v -> this.bastionLocatorOn = v);
        nbt.getBoolean(GSRPlayerConfigParameters.K_STRONGHOLD_LOCATOR_ON).ifPresent(v -> this.strongholdLocatorOn = v);
        nbt.getBoolean(GSRPlayerConfigParameters.K_SHIP_LOCATOR_ON).ifPresent(v -> this.shipLocatorOn = v);
        nbt.getInt(GSRPlayerConfigParameters.K_HUD_PADDING).ifPresent(v -> this.hudPadding = v);
        nbt.getInt(GSRPlayerConfigParameters.K_HUD_ROW_HEIGHT).ifPresent(v -> this.hudRowHeight = v);
        nbt.getInt(GSRPlayerConfigParameters.K_HUD_SPLIT_GAP).ifPresent(v -> this.hudSplitGap = v);
        nbt.getFloat(GSRPlayerConfigParameters.K_HUD_SEPARATOR_ALPHA).ifPresent(v -> this.hudSeparatorAlpha = v);
        nbt.getInt(GSRPlayerConfigParameters.K_SPLIT_SHOW_TICKS).ifPresent(v -> this.splitShowTicks = v);
        nbt.getInt(GSRPlayerConfigParameters.K_END_SHOW_TICKS).ifPresent(v -> this.endShowTicks = v);
        nbt.getBoolean(GSRPlayerConfigParameters.K_ALLOW_NEW_WORLD_BEFORE_RUN_END).ifPresent(v -> this.allowNewWorldBeforeRunEnd = v);
        nbt.getBoolean(GSRPlayerConfigParameters.K_CAN_USE_ADMIN).ifPresent(v -> this.canUseAdmin = v);
        clampAll();
    }
}
