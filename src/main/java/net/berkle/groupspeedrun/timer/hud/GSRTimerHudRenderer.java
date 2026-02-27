package net.berkle.groupspeedrun.timer.hud;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.berkle.groupspeedrun.GSRClient;
import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.parameter.GSRHudParameters;
import net.berkle.groupspeedrun.parameter.GSRTimerConfig;
import net.berkle.groupspeedrun.util.GSRColorHelper;
import net.berkle.groupspeedrun.util.GSRFormatUtil;

/**
 * Rendering logic for the dynamic GSR timer HUD, including conditional symbols and colors.
 * Used by InGameHud mixin and GSR Controls screen.
 */
public final class GSRTimerHudRenderer {

    public static final int EDGE_MARGIN = GSRHudParameters.EDGE_MARGIN;
    public static final float VERTICAL_OFFSET_FACTOR = GSRHudParameters.VERTICAL_OFFSET_FACTOR;

    private GSRTimerHudRenderer() {}

    /**
     * Compute the scaled size of the timer box (same logic as drawTimerBox) so callers can position it.
     * Returns int[2] = { scaledWidth, scaledHeight }.
     */
    public static int[] getTimerBoxScaledSize(TextRenderer tr, GSRConfigWorld worldConfig,
            GSRConfigPlayer playerConfig, boolean showSplits) {
        if (worldConfig == null || playerConfig == null) return new int[]{0, 0};
        long displayElapsed = (worldConfig == GSRClient.clientWorldConfig) ? GSRClient.getClientElapsedMs() : worldConfig.getElapsedTime();
        boolean menuPaused = (worldConfig == GSRClient.clientWorldConfig) && GSRClient.isClientTimerPaused();
        // Run not started: show "GSR Timer:" with 00.00; menuPaused shows "GSR Freeze"
        boolean showPaused = menuPaused || (worldConfig.startTime > 0 && worldConfig.isTimerFrozen && !worldConfig.isVictorious && !worldConfig.isFailed);
        boolean isFreeze = showPaused && (worldConfig.frozenByServerStop || menuPaused);
        String titleLabel = buildTitleLabel(worldConfig, showPaused, isFreeze);
        String titleTime = GSRFormatUtil.formatTime(displayElapsed);
        int nameColWidth = tr.getWidth(titleLabel);
        int timeColWidth = tr.getWidth(titleTime);
        if (showSplits) {
            long latest = Math.max(worldConfig.timeNether, Math.max(worldConfig.timeBastion,
                    Math.max(worldConfig.timeFortress, Math.max(worldConfig.timeEnd, worldConfig.timeDragon))));
            String[][] splitData = {
                    prepareLine(GSRTimerConfig.SPLIT_NETHER, worldConfig.timeNether, latest, worldConfig.isVictorious, false),
                    prepareLine(GSRTimerConfig.SPLIT_BASTION, worldConfig.timeBastion, latest, worldConfig.isVictorious, false),
                    prepareLine(GSRTimerConfig.SPLIT_FORTRESS, worldConfig.timeFortress, latest, worldConfig.isVictorious, false),
                    prepareLine(GSRTimerConfig.SPLIT_END, worldConfig.timeEnd, latest, worldConfig.isVictorious, false),
                    prepareLine(GSRTimerConfig.SPLIT_DRAGON, worldConfig.timeDragon, latest, worldConfig.isVictorious, true)
            };
            for (String[] split : splitData) {
                nameColWidth = Math.max(nameColWidth, tr.getWidth(split[0]));
                timeColWidth = Math.max(timeColWidth, tr.getWidth(split[1]));
            }
        }
        int padding = playerConfig.hudPadding;
        int rowHeight = playerConfig.hudRowHeight;
        int totalBoxWidth = nameColWidth + playerConfig.hudSplitGap + timeColWidth + (padding * 2);
        int boxHeight = showSplits
                ? (((5 + 1) * rowHeight) + (padding * 2) + GSRHudParameters.TITLE_SPLIT_GAP)
                : (rowHeight + (padding * 2));
        float scale = playerConfig.timerScale;
        return new int[]{(int) (totalBoxWidth * scale), (int) (boxHeight * scale)};
    }

    /**
     * Draw the timer box. When forceShowInMenu is true, always show with full opacity and all splits
     * (for use on GSR Controls screen). Otherwise use fade/showSplits from caller.
     *
     * @param context draw context
     * @param tr text renderer
     * @param boxRight if true, align box to right edge; else left
     * @param anchorX x position of the box (left edge if !boxRight, right edge if boxRight)
     * @param anchorY y position of top of box
     * @param worldConfig run state
     * @param playerConfig HUD config
     * @param forceShowInMenu true when drawing on Controls screen (always show, show splits)
     * @param fadeAlpha 0–1 when not forceShowInMenu
     * @param showSplits whether to draw split rows
     */
    public static void drawTimerBox(DrawContext context, TextRenderer tr,
            boolean boxRight, int anchorX, int anchorY,
            GSRConfigWorld worldConfig, GSRConfigPlayer playerConfig,
            boolean forceShowInMenu, float fadeAlpha, boolean showSplits) {
        drawTimerBox(context, tr, boxRight, anchorX, anchorY, worldConfig, playerConfig, forceShowInMenu, fadeAlpha, showSplits, 1f);
    }

    /**
     * Same as {@link #drawTimerBox(DrawContext, TextRenderer, boolean, int, int, GSRConfigWorld, GSRConfigPlayer, boolean, float, boolean)}
     * but with explicit menu alpha for de-emphasized display (e.g. GSR Controls screen).
     *
     * @param menuAlpha when forceShowInMenu, alpha (0–1) for faded/blurred look. Ignored otherwise.
     */
    public static void drawTimerBox(DrawContext context, TextRenderer tr,
            boolean boxRight, int anchorX, int anchorY,
            GSRConfigWorld worldConfig, GSRConfigPlayer playerConfig,
            boolean forceShowInMenu, float fadeAlpha, boolean showSplits, float menuAlpha) {
        if (worldConfig == null || playerConfig == null) return;
        float alpha = forceShowInMenu ? Math.max(0f, Math.min(1f, menuAlpha)) : fadeAlpha;
        if (alpha <= GSRHudParameters.ALPHA_CUTOFF && !forceShowInMenu) return;

        boolean isFinished = worldConfig.isVictorious || worldConfig.isFailed;
        boolean menuPaused = (worldConfig == GSRClient.clientWorldConfig) && GSRClient.isClientTimerPaused();
        // Run not started (startTime <= 0) shows "GSR Timer: 00.00", not "Paused"
        boolean isPaused = (worldConfig.startTime > 0 && worldConfig.isTimerFrozen && !isFinished) || menuPaused;
        boolean isFreeze = isPaused && (worldConfig.frozenByServerStop || menuPaused);
        long elapsedMs = (worldConfig == GSRClient.clientWorldConfig) ? GSRClient.getClientElapsedMs() : worldConfig.getElapsedTime();
        String titleLabel = buildTitleLabel(worldConfig, isPaused, isFreeze);
        int stateColor = gsr$stateColor(worldConfig, playerConfig, isPaused, isFreeze, isFinished);
        String titleTime = GSRFormatUtil.formatTime(elapsedMs);

        long latestTime = Math.max(worldConfig.timeNether, Math.max(worldConfig.timeBastion,
                Math.max(worldConfig.timeFortress, Math.max(worldConfig.timeEnd, worldConfig.timeDragon))));
        String[][] splitData = {
                prepareLine(GSRTimerConfig.SPLIT_NETHER, worldConfig.timeNether, latestTime, worldConfig.isVictorious, false),
                prepareLine(GSRTimerConfig.SPLIT_BASTION, worldConfig.timeBastion, latestTime, worldConfig.isVictorious, false),
                prepareLine(GSRTimerConfig.SPLIT_FORTRESS, worldConfig.timeFortress, latestTime, worldConfig.isVictorious, false),
                prepareLine(GSRTimerConfig.SPLIT_END, worldConfig.timeEnd, latestTime, worldConfig.isVictorious, false),
                prepareLine(GSRTimerConfig.SPLIT_DRAGON, worldConfig.timeDragon, latestTime, worldConfig.isVictorious, true)
        };

        int nameColWidth = tr.getWidth(titleLabel);
        int timeColWidth = tr.getWidth(titleTime);
        if (showSplits) {
            for (String[] split : splitData) {
                nameColWidth = Math.max(nameColWidth, tr.getWidth(split[0]));
                timeColWidth = Math.max(timeColWidth, tr.getWidth(split[1]));
            }
        }

        int padding = playerConfig.hudPadding;
        int rowHeight = playerConfig.hudRowHeight;
        int totalBoxWidth = nameColWidth + playerConfig.hudSplitGap + timeColWidth + (padding * 2);
        int boxHeight = showSplits
                ? (((splitData.length + 1) * rowHeight) + (padding * 2) + GSRHudParameters.TITLE_SPLIT_GAP)
                : (rowHeight + (padding * 2));

        float scale = playerConfig.timerScale;
        int scaledW = (int) (totalBoxWidth * scale);
        int x = boxRight ? (anchorX - scaledW) : anchorX;
        int y = anchorY;

        context.getMatrices().pushMatrix();
        context.getMatrices().translate(x, y);
        context.getMatrices().scale(scale, scale);

        // Slightly gray out the box when paused (lower background opacity); soft blue when freeze
        int bgOpacity = isPaused ? GSRHudParameters.BG_OPACITY_PAUSED : GSRHudParameters.BG_OPACITY_RUNNING;
        context.fill(0, 0, totalBoxWidth, boxHeight, GSRColorHelper.getBackgroundWithAlpha(bgOpacity, alpha));
        int mainTextColor = GSRColorHelper.applyAlpha(stateColor, alpha);
        context.drawTextWithShadow(tr, titleLabel, padding, padding, mainTextColor);
        context.drawTextWithShadow(tr, titleTime, totalBoxWidth - padding - tr.getWidth(titleTime), padding, mainTextColor);

        if (showSplits) {
            int sepCol = GSRColorHelper.applyAlpha(stateColor, playerConfig.hudSeparatorAlpha * alpha);
            context.fill(GSRHudParameters.SEPARATOR_INSET, padding + rowHeight + GSRHudParameters.SEPARATOR_INSET, totalBoxWidth - GSRHudParameters.SEPARATOR_INSET, padding + rowHeight + GSRHudParameters.SEPARATOR_INSET + GSRHudParameters.SEPARATOR_THICKNESS, sepCol);
            int currentY = padding + rowHeight + GSRHudParameters.SEPARATOR_INSET + GSRHudParameters.SEPARATOR_THICKNESS + GSRHudParameters.SEPARATOR_SPLIT_GAP;
            for (String[] split : splitData) {
                context.drawTextWithShadow(tr, split[0], padding, currentY, mainTextColor);
                context.drawTextWithShadow(tr, split[1], totalBoxWidth - padding - tr.getWidth(split[1]), currentY, mainTextColor);
                currentY += rowHeight;
            }
        }
        context.getMatrices().popMatrix();
    }

    /** Returns ARGB color for current timer state (title and time use same color). Uses player config when available. */
    private static int gsr$stateColor(GSRConfigWorld worldConfig, GSRConfigPlayer playerConfig, boolean isPaused, boolean isFreeze, boolean isFinished) {
        if (worldConfig.isVictorious) return playerConfig != null ? playerConfig.timerColorVictory : GSRTimerConfig.COLOR_VICTORY_ARGB;
        if (worldConfig.isFailed) return playerConfig != null ? playerConfig.timerColorFail : GSRTimerConfig.COLOR_FAIL_ARGB;
        if (isFreeze) return playerConfig != null ? playerConfig.timerColorFreeze : GSRTimerConfig.COLOR_FREEZE_ARGB;
        if (isPaused) return playerConfig != null ? playerConfig.timerColorPaused : GSRTimerConfig.COLOR_PAUSED_ARGB;
        if (worldConfig.antiCheatEnabled && worldConfig.locatorDeranked) return playerConfig != null ? playerConfig.timerColorDeranked : GSRTimerConfig.COLOR_DERANKED_ARGB;
        return playerConfig != null ? playerConfig.timerColorRunning : GSRTimerConfig.COLOR_RUNNING_ARGB;
    }

    /**
     * Builds the title label for the timer box based on run state. Plain text with §l bold only;
     * color is applied via DrawContext ARGB.
     * Victory: GSR {dragon} Victory!. Fail: GSR {skull} FAIL:.
     * Freeze: GSR {snowflake} Freeze:. Paused: GSR {hourglass} Paused:.
     * Running: GSR {stopwatch} Time: when ranked; GSR {white-flag} Time: when deranked.
     */
    private static String buildTitleLabel(GSRConfigWorld worldConfig, boolean isPaused, boolean isFreeze) {
        String prefix = "GSR ";
        if (worldConfig.isVictorious) {
            return GSRTimerConfig.STYLE_BOLD + prefix + GSRTimerConfig.ICON_DRAGON + " " + GSRTimerConfig.LABEL_VICTORY;
        }
        if (worldConfig.isFailed) {
            return GSRTimerConfig.STYLE_BOLD + prefix + GSRTimerConfig.ICON_SKULL + " " + GSRTimerConfig.LABEL_FAIL;
        }
        if (isPaused) {
            if (isFreeze) {
                return GSRTimerConfig.STYLE_BOLD + prefix + GSRTimerConfig.ICON_ICE + " " + GSRTimerConfig.LABEL_FREEZE;
            }
            return GSRTimerConfig.STYLE_BOLD + prefix + GSRTimerConfig.ICON_PAUSE + " " + GSRTimerConfig.LABEL_PAUSED;
        }
        boolean deranked = worldConfig.antiCheatEnabled && worldConfig.locatorDeranked;
        String icon = deranked ? GSRTimerConfig.ICON_DERANKED : GSRTimerConfig.ICON_CLOCK;
        return GSRTimerConfig.STYLE_BOLD + prefix + icon + " " + GSRTimerConfig.LABEL_ACTIVE;
    }

    /** @param isDragon true for Dragon split (all gold when victorious). */
    private static String[] prepareLine(String name, long timeMs, long latestMs, boolean isVictorious, boolean isDragon) {
        if (timeMs <= 0) return new String[]{GSRTimerConfig.COLOR_INCOMPLETE + "○ " + GSRTimerConfig.COLOR_INCOMPLETE + name, GSRTimerConfig.COLOR_INCOMPLETE + "--:--"};
        boolean isLatest = timeMs == latestMs;
        boolean dragonVictory = isVictorious && isDragon;
        String icon = isLatest ? GSRTimerConfig.COLOR_GOLD + "★ " : GSRTimerConfig.COLOR_GREEN + "✔ ";
        String labelColor = dragonVictory ? GSRTimerConfig.COLOR_GOLD : GSRTimerConfig.COLOR_GREEN;
        String timeColor = dragonVictory ? GSRTimerConfig.COLOR_GOLD : GSRTimerConfig.COLOR_GREEN;
        return new String[]{icon + labelColor + name, timeColor + GSRFormatUtil.formatTime(timeMs)};
    }
}
