package net.berkle.groupspeedrun.gui.runhistory;

import net.berkle.groupspeedrun.data.GSRRunSaveState;

import java.util.List;
import net.berkle.groupspeedrun.gui.GSRTickerState;
import net.berkle.groupspeedrun.mixin.accessors.GSRPressableWidgetAccessor;
import net.berkle.groupspeedrun.parameter.GSRButtonParameters;
import net.berkle.groupspeedrun.parameter.GSRRunHistoryParameters;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;
import net.berkle.groupspeedrun.util.GSRScrollbarHelper;
import net.berkle.groupspeedrun.util.GSRStatusText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;

import java.util.Set;

/**
 * Renders the detail panel content: Run Info (scrollable text), Run Graphs (bar chart), or Player Graphs.
 */
public final class GSRRunHistoryDetailPanel {

    public GSRRunHistoryDetailPanel() {}

    /**
     * Renders the selected tab content.
     *
     * @param context             Draw context.
     * @param textRenderer        Text renderer.
     * @param tickerState         Ticker for chart tooltips.
     * @param contentLeft         Left edge of content area.
     * @param contentTop          Top edge of content area.
     * @param contentRight        Right edge of content area.
     * @param contentBottom       Bottom edge of content area.
     * @param selectedTab         {@link GSRRunHistoryParameters#TAB_RUN_INFO}, {@link GSRRunHistoryParameters#TAB_RUN_GRAPHS}, or {@link GSRRunHistoryParameters#TAB_PLAYER_GRAPHS}.
     * @param selectedRun         Selected run for Run Info tab (null shows "Select a run").
     * @param selectedRunsForCharts Runs to average for Sel bar on Run Graphs tab; empty = all runs.
     * @param runs                Runs list (for All time view: all runs; for other views: filtered runs).
     * @param filteredRunsForCharts For All time view only: runs matching Runs filter (blue); null otherwise.
     * @param selectedPlayerFilter Selected players for filter.
     * @param selectedCategoryIndex Compare category index.
     * @param selectedChartViewIndex Chart view index.
     * @param selectedPlayerTypeIndex Player type index.
     * @param activeRunLeaderboardLines Optional live leaderboard lines for active run (Run Info tab); null otherwise.
     * @param activeRunFreshState  Optional freshly built state for active run; when non-null, used for live Run Info updates.
     * @param detailScroll        Vertical scroll offset (float for smooth sub-pixel during auto-scroll).
     * @param chartScrollX        Horizontal scroll for All time chart.
     * @param mouseX              Mouse X for hover.
     * @param mouseY              Mouse Y for hover.
     * @param screenWidth         Screen width.
     * @param screenHeight         Screen height.
     */
    public void render(DrawContext context, TextRenderer textRenderer, GSRTickerState tickerState,
                      int contentLeft, int contentTop, int contentRight, int contentBottom,
                      int selectedTab, GSRRunSaveState selectedRun, List<GSRRunSaveState> selectedRunsForCharts,
                      List<GSRRunSaveState> runs, List<GSRRunSaveState> filteredRunsForCharts, Set<String> selectedPlayerFilter,
                      int selectedCategoryIndex, int selectedChartViewIndex, int selectedPlayerTypeIndex,
                      List<String> activeRunLeaderboardLines, GSRRunSaveState activeRunFreshState,
                      float detailScroll, int chartScrollX,
                      int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (selectedTab == GSRRunHistoryParameters.TAB_RUN_INFO && selectedRun == null) {
            context.drawCenteredTextWithShadow(textRenderer, "Select a run",
                    (contentLeft + contentRight) / 2, contentTop + (contentBottom - contentTop) / 2 - 6,
                    GSRRunHistoryParameters.EMPTY_MESSAGE_COLOR);
            return;
        }
        if (selectedTab == GSRRunHistoryParameters.TAB_RUN_GRAPHS && runs.isEmpty()) {
            context.drawCenteredTextWithShadow(textRenderer, "No run data.",
                    (contentLeft + contentRight) / 2, contentTop + (contentBottom - contentTop) / 2 - 6,
                    GSRRunHistoryParameters.EMPTY_MESSAGE_COLOR);
            return;
        }

        int textLeft = contentLeft;
        int contentHeight = contentBottom - contentTop;
        if (selectedTab == GSRRunHistoryParameters.TAB_RUN_INFO) {
            int runInfoContentHeight = computeRunInfoContentHeight(selectedRun, activeRunLeaderboardLines, activeRunFreshState);
            int maxDetailScroll = Math.max(0, runInfoContentHeight - contentHeight + GSRRunHistoryParameters.BOTTOM_SCROLL_PADDING);
            if (maxDetailScroll > 0) {
                int sbWidth = GSRScrollbarHelper.getScrollbarWidth();
                GSRScrollbarHelper.drawScrollbar(context, contentLeft, contentTop, contentHeight,
                        (int) detailScroll, maxDetailScroll, GSRRunHistoryParameters.SCROLLBAR_MIN_THUMB_HEIGHT);
                textLeft = contentLeft + sbWidth + GSRRunHistoryParameters.LIST_TEXT_INSET;
            }
            GSRRunSaveState runForDisplay = activeRunFreshState != null ? activeRunFreshState : selectedRun;
            List<String> leaderboard = (selectedRun != null && "current".equals(selectedRun.record().runId()))
                    ? activeRunLeaderboardLines : null;
            renderRunInfo(context, textRenderer, runForDisplay, leaderboard, textLeft, contentTop, contentRight, contentBottom, detailScroll);
        } else if (selectedTab == GSRRunHistoryParameters.TAB_RUN_GRAPHS) {
            int viewMode = GSRRunHistoryLeftColumn.getViewModeForDisplayIndex(selectedChartViewIndex);
            String description = switch (viewMode) {
                case 1 -> "Sel, Avg, 5 best runs.";
                case 2 -> "All runs. Filtered blue, selected yellow.";
                case 3 -> "Sel, Avg, 5 worst runs.";
                default -> "Sel, Avg, 5 most recent runs.";
            };
            List<GSRRunSaveState> selRuns = selectedRunsForCharts == null || selectedRunsForCharts.isEmpty() ? runs : selectedRunsForCharts;
            int chartWidth = contentRight - contentLeft - GSRRunHistoryParameters.CONTENT_PADDING;
            int maxScrollX = (viewMode == 2 && !runs.isEmpty())
                    ? GSRRunHistoryChartRenderer.computeOverTimeMaxScrollX(chartWidth, runs.size()) : 0;
            int sbHeight = GSRScrollbarHelper.getScrollbarWidth();
            int chartBottom = (maxScrollX > 0) ? contentBottom - sbHeight : contentBottom;
            List<GSRRunSaveState> filteredForChart = (viewMode == 2 && filteredRunsForCharts != null) ? filteredRunsForCharts : null;
            GSRRunHistoryChartRenderer.render(context, textRenderer, tickerState,
                    GSRRunHistoryStatRow.ALL_SORTED.get(selectedCategoryIndex), selRuns, runs, filteredForChart, viewMode,
                    description, contentLeft, contentTop, contentRight, chartBottom, (int) detailScroll,
                    mouseX, mouseY, screenWidth, screenHeight, chartScrollX);
            if (maxScrollX > 0) {
                GSRScrollbarHelper.drawHorizontalScrollbar(context, contentLeft, chartBottom, chartWidth,
                        chartScrollX, maxScrollX, GSRRunHistoryParameters.CHART_HORIZONTAL_SCROLLBAR_MIN_THUMB);
            }
        } else {
            try {
                int typeMode = GSRRunHistoryLeftColumn.getTypeModeForDisplayIndex(selectedPlayerTypeIndex);
                int viewMode = GSRRunHistoryLeftColumn.getViewModeForDisplayIndex(selectedChartViewIndex);
                boolean rendered = GSRRunHistoryPlayerChartRenderer.render(context, textRenderer, tickerState,
                        GSRRunHistoryStatRow.ALL_SORTED.get(selectedCategoryIndex), selectedPlayerFilter, runs,
                        typeMode, viewMode, contentLeft, contentTop, contentRight, contentBottom, (int) detailScroll,
                        mouseX, mouseY);
                if (!rendered) {
                    String msg = selectedPlayerFilter.isEmpty()
                            ? "No run data."
                            : "No runs for selected players.";
                    context.drawCenteredTextWithShadow(textRenderer, msg, (contentLeft + contentRight) / 2,
                            contentTop + (contentBottom - contentTop) / 2 - 6, GSRRunHistoryParameters.EMPTY_MESSAGE_COLOR);
                }
            } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
                context.drawCenteredTextWithShadow(textRenderer, "Player chart unavailable. Restart the game.",
                        (contentLeft + contentRight) / 2, contentTop + (contentBottom - contentTop) / 2 - 6,
                        GSRRunHistoryParameters.EMPTY_MESSAGE_COLOR);
            }
        }
    }

    private void renderRunInfo(DrawContext context, TextRenderer textRenderer, GSRRunSaveState selectedRun,
                              List<String> activeRunLeaderboardLines,
                              int left, int top, int right, int bottom, float scrollY) {
        String text = GSRStatusText.buildForCompletedRun(selectedRun.record(), selectedRun.snapshots(), activeRunLeaderboardLines);
        String[] lines = text.split("\n", -1);
        context.enableScissor(left, top, right, bottom);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0, -scrollY);
        int y = top + GSRRunHistoryParameters.CONTENT_PADDING;
        for (String line : lines) {
            if (y + GSRUiParameters.STATUS_LINE_HEIGHT > top + scrollY && y < bottom + scrollY) {
                GSRRunHistoryTextRenderer.drawLineWithSectionColors(context, textRenderer, line, left, y);
            }
            y += line.isEmpty() ? GSRUiParameters.STATUS_LINE_HEIGHT / 2 : GSRUiParameters.STATUS_LINE_HEIGHT;
        }
        int textBottom = y + GSRRunHistoryParameters.CONTENT_PADDING;
        int buttonTop = textBottom + GSRRunHistoryParameters.DELETE_RUN_BUTTON_GAP;
        int buttonWidth = GSRRunHistoryParameters.DELETE_RUN_BUTTON_WIDTH;
        int buttonHeight = GSRRunHistoryParameters.DELETE_RUN_BUTTON_HEIGHT;
        int buttonLeft = right - GSRRunHistoryParameters.CONTENT_PADDING - buttonWidth;
        if (buttonTop + buttonHeight > top + scrollY && buttonTop < bottom + scrollY) {
            var textures = GSRPressableWidgetAccessor.gsr$getTextures();
            var tex = textures.get(true, false);
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, tex, buttonLeft, buttonTop, buttonWidth, buttonHeight);
            int textY = buttonTop + (buttonHeight - textRenderer.fontHeight) / 2;
            context.drawCenteredTextWithShadow(textRenderer, GSRButtonParameters.RUN_HISTORY_DELETE_RUN,
                    buttonLeft + buttonWidth / 2, textY, GSRRunHistoryParameters.TEXT_COLOR);
        }
        context.getMatrices().popMatrix();
        context.disableScissor();
    }

    /** Total height of Run Info content in pixels (text + gap + Delete Run button). */
    public static int computeRunInfoContentHeight(GSRRunSaveState selectedRun) {
        return computeRunInfoContentHeight(selectedRun, null, null);
    }

    /** Total height of Run Info content; activeRunLeaderboardLines used when selectedRun is current run. */
    public static int computeRunInfoContentHeight(GSRRunSaveState selectedRun, List<String> activeRunLeaderboardLines) {
        return computeRunInfoContentHeight(selectedRun, activeRunLeaderboardLines, null);
    }

    /** Total height of Run Info content; when activeRunFreshState is non-null, used for display (live updates). */
    public static int computeRunInfoContentHeight(GSRRunSaveState selectedRun, List<String> activeRunLeaderboardLines, GSRRunSaveState activeRunFreshState) {
        if (selectedRun == null) return 0;
        GSRRunSaveState runForDisplay = activeRunFreshState != null ? activeRunFreshState : selectedRun;
        List<String> leaderboard = (selectedRun != null && "current".equals(selectedRun.record().runId()))
                ? activeRunLeaderboardLines : null;
        String text = GSRStatusText.buildForCompletedRun(runForDisplay.record(), runForDisplay.snapshots(), leaderboard);
        String[] lines = text.split("\n", -1);
        int h = GSRRunHistoryParameters.CONTENT_PADDING;
        for (String line : lines) {
            h += line.isEmpty() ? GSRUiParameters.STATUS_LINE_HEIGHT / 2 : GSRUiParameters.STATUS_LINE_HEIGHT;
        }
        h += GSRRunHistoryParameters.CONTENT_PADDING;
        h += GSRRunHistoryParameters.DELETE_RUN_BUTTON_GAP;
        h += GSRRunHistoryParameters.DELETE_RUN_BUTTON_HEIGHT;
        return h;
    }

    /**
     * Returns true if (mouseX, mouseY) is within the Delete Run button bounds.
     * Button is at bottom-right of Run Info content, scrolls with content.
     */
    public static boolean isDeleteRunButtonAt(GSRRunSaveState selectedRun, int contentLeft, int contentTop,
                                             int contentRight, int contentBottom, float scrollY, int mouseX, int mouseY) {
        return isDeleteRunButtonAt(selectedRun, null, null, contentLeft, contentTop, contentRight, contentBottom, scrollY, mouseX, mouseY);
    }

    /** Overload with activeRunLeaderboardLines for content height when selectedRun is current run. */
    public static boolean isDeleteRunButtonAt(GSRRunSaveState selectedRun, List<String> activeRunLeaderboardLines,
                                             int contentLeft, int contentTop, int contentRight, int contentBottom, float scrollY, int mouseX, int mouseY) {
        return isDeleteRunButtonAt(selectedRun, activeRunLeaderboardLines, null, contentLeft, contentTop, contentRight, contentBottom, scrollY, mouseX, mouseY);
    }

    /** Overload with activeRunFreshState for live Run Info content height. */
    public static boolean isDeleteRunButtonAt(GSRRunSaveState selectedRun, List<String> activeRunLeaderboardLines, GSRRunSaveState activeRunFreshState,
                                             int contentLeft, int contentTop, int contentRight, int contentBottom, float scrollY, int mouseX, int mouseY) {
        if (selectedRun == null || "current".equals(selectedRun.record().runId())) return false;
        int textHeight = computeTextHeight(selectedRun, activeRunLeaderboardLines, activeRunFreshState);
        int buttonTop = contentTop + textHeight + GSRRunHistoryParameters.DELETE_RUN_BUTTON_GAP - (int) scrollY;
        int buttonHeight = GSRRunHistoryParameters.DELETE_RUN_BUTTON_HEIGHT;
        int buttonWidth = GSRRunHistoryParameters.DELETE_RUN_BUTTON_WIDTH;
        int buttonLeft = contentRight - GSRRunHistoryParameters.CONTENT_PADDING - buttonWidth;
        return mouseX >= buttonLeft && mouseX < buttonLeft + buttonWidth
                && mouseY >= buttonTop && mouseY < buttonTop + buttonHeight
                && mouseY >= contentTop && mouseY < contentBottom;
    }

    private static int computeTextHeight(GSRRunSaveState selectedRun, List<String> activeRunLeaderboardLines) {
        return computeTextHeight(selectedRun, activeRunLeaderboardLines, null);
    }

    private static int computeTextHeight(GSRRunSaveState selectedRun, List<String> activeRunLeaderboardLines, GSRRunSaveState activeRunFreshState) {
        if (selectedRun == null) return 0;
        GSRRunSaveState runForDisplay = activeRunFreshState != null ? activeRunFreshState : selectedRun;
        List<String> leaderboard = (selectedRun != null && "current".equals(selectedRun.record().runId()))
                ? activeRunLeaderboardLines : null;
        String text = GSRStatusText.buildForCompletedRun(runForDisplay.record(), runForDisplay.snapshots(), leaderboard);
        String[] lines = text.split("\n", -1);
        int h = GSRRunHistoryParameters.CONTENT_PADDING;
        for (String line : lines) {
            h += line.isEmpty() ? GSRUiParameters.STATUS_LINE_HEIGHT / 2 : GSRUiParameters.STATUS_LINE_HEIGHT;
        }
        return h + GSRRunHistoryParameters.CONTENT_PADDING;
    }

    /**
     * Renders pre-built run info text (e.g. average run info) with scroll. No Delete Run button.
     */
    public static void renderRunInfoFromText(DrawContext context, TextRenderer textRenderer, String text,
                                             int left, int top, int right, int bottom, float scrollY) {
        if (text == null || text.isEmpty()) return;
        String[] lines = text.split("\n", -1);
        context.enableScissor(left, top, right, bottom);
        context.getMatrices().pushMatrix();
        context.getMatrices().translate(0, -scrollY);
        int y = top + GSRRunHistoryParameters.CONTENT_PADDING;
        for (String line : lines) {
            if (y + GSRUiParameters.STATUS_LINE_HEIGHT > top + scrollY && y < bottom + scrollY) {
                GSRRunHistoryTextRenderer.drawLineWithSectionColors(context, textRenderer, line, left, y);
            }
            y += line.isEmpty() ? GSRUiParameters.STATUS_LINE_HEIGHT / 2 : GSRUiParameters.STATUS_LINE_HEIGHT;
        }
        context.getMatrices().popMatrix();
        context.disableScissor();
    }

    /** Content height in pixels for pre-built run info text (no Delete Run button). */
    public static int computeRunInfoContentHeightFromText(String text) {
        if (text == null || text.isEmpty()) return 0;
        String[] lines = text.split("\n", -1);
        int h = GSRRunHistoryParameters.CONTENT_PADDING;
        for (String line : lines) {
            h += line.isEmpty() ? GSRUiParameters.STATUS_LINE_HEIGHT / 2 : GSRUiParameters.STATUS_LINE_HEIGHT;
        }
        return h + GSRRunHistoryParameters.CONTENT_PADDING;
    }
}
