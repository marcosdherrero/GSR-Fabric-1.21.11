package net.berkle.groupspeedrun.gui.runhistory;

import net.berkle.groupspeedrun.data.GSRRunRecord;
import net.berkle.groupspeedrun.data.GSRRunSaveState;
import net.berkle.groupspeedrun.gui.GSRStandardTooltip;
import net.berkle.groupspeedrun.gui.GSRTickerState;
import net.berkle.groupspeedrun.parameter.GSRRunHistoryParameters;
import net.berkle.groupspeedrun.util.GSRFormatUtil;
import net.berkle.groupspeedrun.util.GSRStatusText;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.ToDoubleFunction;

/**
 * Renders the "Stats vs Avg" bar chart for Run History.
 * Label at bottom, bars extend upward. Shows passed/failed and units.
 * Split Times: bar divided into 5 equal segments (N,B,F,E,D) with per-split times.
 */
public final class GSRRunHistoryChartRenderer {

    /** Full split names for tooltips. */
    private static final String[] SPLIT_NAMES = { "Nether", "Bastion", "Fortress", "The End", "Dragon" };
    /** Abbreviations for split key (N, B, F, E, D). */
    private static final String[] SPLIT_KEY_ABBREV = { "N", "B", "F", "E", "D" };
    private static final String CHART_DESCRIPTION = "Sel, Avg, 5 comparison runs.";

    private GSRRunHistoryChartRenderer() {}

    /**
     * Computes All time chart max horizontal scroll. Returns maxScrollX when run count exceeds
     * {@link GSRRunHistoryParameters#OVER_TIME_SCROLL_THRESHOLD}; 0 otherwise.
     */
    public static int computeOverTimeMaxScrollX(int chartWidth, int runCount) {
        if (runCount <= GSRRunHistoryParameters.OVER_TIME_SCROLL_THRESHOLD) return 0;
        int slotWidth = GSRRunHistoryParameters.OVER_TIME_SCROLLED_BAR_WIDTH;
        int totalContentWidth = runCount * slotWidth + (runCount - 1) * GSRRunHistoryParameters.BAR_GAP;
        return Math.max(0, totalContentWidth - chartWidth);
    }

    /**
     * Renders the Run Graphs bar chart: All time (viewMode 2) or Compare (viewMode 0/1).
     *
     * @param selectedRuns Runs to average for Sel bar; empty = all runs (Sel = Avg).
     * @param filteredRuns For viewMode 2 only: runs matching Runs filter (blue); null = use selectedRuns for blue.
     * @param viewMode 0=Recent (Sel, Avg, 5), 1=Best 5, 2=All time.
     * @param chartScrollX Horizontal scroll for All time view.
     */
    public static void render(DrawContext context, TextRenderer textRenderer,
                             GSRTickerState tickerState,
                             GSRRunHistoryStatRow row, List<GSRRunSaveState> selectedRuns,
                             List<GSRRunSaveState> runs, List<GSRRunSaveState> filteredRuns, int viewMode, String description,
                             int left, int top, int right, int bottom, int scrollY,
                             int mouseX, int mouseY, int screenWidth, int screenHeight,
                             int chartScrollX) {
        if (runs.isEmpty()) return;
        List<GSRRunSaveState> sel = selectedRuns == null || selectedRuns.isEmpty() ? runs : selectedRuns;

        context.enableScissor(left, top, right, bottom);
        var acc = row.runAccessor();

        if (viewMode == 2) {
            renderOverTimeChart(context, textRenderer, tickerState, row, sel, runs, filteredRuns, description,
                    left, top, right, bottom, scrollY, chartScrollX, mouseX, mouseY, screenWidth, screenHeight, acc);
        } else {
            renderCompareChart(context, textRenderer, tickerState, row, sel, runs, viewMode, description,
                    left, top, right, bottom, scrollY, mouseX, mouseY, screenWidth, screenHeight, acc);
        }
        context.disableScissor();
    }

    /**
     * All time view: all runs in chronological order. Filtered runs blue, specifically selected yellow, others gray.
     */
    private static void renderOverTimeChart(DrawContext context, TextRenderer textRenderer,
                                            GSRTickerState tickerState,
                                            GSRRunHistoryStatRow row, List<GSRRunSaveState> selectedRuns,
                                            List<GSRRunSaveState> runs, List<GSRRunSaveState> filteredRuns, String description,
                                            int left, int top, int right, int bottom, int scrollY,
                                            int chartScrollX, int mouseX, int mouseY, int screenWidth, int screenHeight,
                                            java.util.function.ToDoubleFunction<GSRRunSaveState> acc) {
        List<GSRRunSaveState> chronological = new ArrayList<>(runs);
        chronological.sort(Comparator.comparingLong(r -> r.record().endMs()));
        int n = chronological.size();
        if (n == 0) return;

        int chartWidth = right - left - GSRRunHistoryParameters.CONTENT_PADDING;
        boolean overflow = n > GSRRunHistoryParameters.OVER_TIME_SCROLL_THRESHOLD;
        int slotWidth;
        int totalContentWidth;
        if (overflow) {
            slotWidth = GSRRunHistoryParameters.OVER_TIME_SCROLLED_BAR_WIDTH;
            totalContentWidth = n * slotWidth + (n - 1) * GSRRunHistoryParameters.BAR_GAP;
        } else {
            int maxBarWidth = (int) (chartWidth * GSRRunHistoryParameters.BAR_MAX_WIDTH_FRACTION);
            slotWidth = Math.min(maxBarWidth, Math.max(1, (chartWidth - GSRRunHistoryParameters.BAR_GAP * (n - 1)) / n));
            totalContentWidth = n * slotWidth + (n - 1) * GSRRunHistoryParameters.BAR_GAP;
        }
        int scrollX = overflow ? chartScrollX : 0;
        boolean showLabels = n <= GSRRunHistoryParameters.OVER_TIME_LABEL_HIDE_THRESHOLD;

        int contentHeight = bottom - top;
        // Use compact layout when container is small so chart fits without scroll.
        boolean compact = contentHeight < GSRRunHistoryParameters.CHART_COMPACT_THRESHOLD;
        int topOffset = compact ? 1 : 2;
        int axisTextHeight = compact ? GSRRunHistoryParameters.CHART_AXIS_TEXT_HEIGHT_COMPACT : GSRRunHistoryParameters.CHART_AXIS_TEXT_HEIGHT;
        int titleMaxWidth = Math.max(1, right - left);

        int titleY = top + topOffset - scrollY;
        String titleWithUnits = row.chartTitle() + row.unitSuffix();
        int titleHeight = drawWrappedText(context, textRenderer, titleWithUnits, left, titleY, titleMaxWidth, GSRRunHistoryParameters.CHART_LABEL_COLOR);
        int descY = titleY + titleHeight;
        String descText = (description != null && !description.isEmpty()) ? description : "All runs. Filtered blue, selected yellow.";
        int descHeight = drawWrappedText(context, textRenderer, descText, left, descY, titleMaxWidth, GSRRunHistoryParameters.BAR_LABEL_COLOR);

        int headerBottom = descY + descHeight;
        int margin = computeChartMargin(compact, right - left);
        int valueY = bottom - margin - axisTextHeight;
        int barBottom = valueY - GSRRunHistoryParameters.CHART_LABEL_Y_OFFSET;
        int barAreaTop = headerBottom + margin;
        int availableBarHeight = Math.max(GSRRunHistoryParameters.BAR_HEIGHT, barBottom - barAreaTop);
        int barTop = Math.max(barAreaTop, barBottom - availableBarHeight);

        boolean isSplitTimes = "Split Times".equals(row.label());
        long[][] splitTimes = null;
        double splitGlobalMax = 0.001;
        if (isSplitTimes) {
            splitTimes = new long[n][5];
            for (int i = 0; i < n; i++) {
                splitTimes[i] = getSplitTimes(chronological.get(i).record());
                for (int s = 0; s < 5; s++) splitGlobalMax = Math.max(splitGlobalMax, splitTimes[i][s]);
            }
            if (splitGlobalMax < 0.001) splitGlobalMax = 1;
        }
        double globalMax = 0.001;
        for (GSRRunSaveState r : chronological) globalMax = Math.max(globalMax, acc.applyAsDouble(r));
        if (globalMax < 0.001) globalMax = 1;

        int x = left - scrollX;
        int slotAreaBottom = valueY + 2 * textRenderer.fontHeight;
        int labelMargin = GSRRunHistoryParameters.BUTTON_TICKER_MARGIN;
        int maxLabelWidth = Math.max(1, slotWidth - 2 * labelMargin);
        float uniformScale = 1f;
        int segCount = GSRRunHistoryParameters.SPLIT_SEGMENT_COUNT;
        int segGap = GSRRunHistoryParameters.SPLIT_SEGMENT_GAP;
        int segWidth = isSplitTimes ? Math.max(1, (slotWidth - (segCount - 1) * segGap) / segCount) : slotWidth;
        Set<GSRRunSaveState> selSet = new HashSet<>(selectedRuns);
        Set<GSRRunSaveState> filteredSet = filteredRuns != null ? new HashSet<>(filteredRuns) : selSet;
        for (int i = 0; i < n; i++) {
            GSRRunSaveState r = chronological.get(i);
            boolean isSelected = selSet.contains(r);
            String lbl;
            if (isSplitTimes && splitTimes != null) {
                long lastT = getLastSplitTime(r.record());
                lbl = (isSelected ? "Sel" : ("#" + (i + 1))) + " " + GSRFormatUtil.formatTime(lastT);
            } else {
                lbl = (isSelected ? "Sel" : ("#" + (i + 1))) + " " + formatValue(row, acc.applyAsDouble(r));
            }
            int w = textRenderer.getWidth(lbl);
            if (w > maxLabelWidth) uniformScale = Math.min(uniformScale, (float) maxLabelWidth / w);
        }
        x = left - scrollX;

        boolean drawKey = false;
        if (isSplitTimes && splitTimes != null) {
            int maxFillH = 0;
            for (int i = 0; i < n; i++) {
                for (int s = 0; s < 5; s++) {
                    int h = (int) ((splitTimes[i][s] / splitGlobalMax) * availableBarHeight);
                    if (h > maxFillH) maxFillH = h;
                }
            }
            drawKey = maxFillH >= GSRRunHistoryParameters.SPLIT_KEY_HEIGHT;
        }

        for (int i = 0; i < n; i++) {
            if (x + slotWidth > left && x < right) {
                context.fill(x, barTop, x + slotWidth, barBottom, GSRRunHistoryParameters.BAR_SLOT_BG);
            }
            x += slotWidth + GSRRunHistoryParameters.BAR_GAP;
        }
        if (drawKey) {
            drawSplitKey(context, textRenderer, left - scrollX, barTop, totalContentWidth);
        }

        List<String> hoverTooltip = null;
        int hoveredBarIndex = -1;
        int hoveredSegmentIndex = -1;
        x = left - scrollX;

        for (int i = 0; i < n; i++) {
            GSRRunSaveState run = chronological.get(i);
            double val = acc.applyAsDouble(run);
            boolean isSelected = selSet.contains(run);
            boolean isFiltered = filteredSet.contains(run);
            int color = isSelected ? GSRRunHistoryParameters.BAR_COLOR_AVG
                    : (isFiltered ? GSRRunHistoryParameters.BAR_COLOR_SELECTED : GSRRunHistoryParameters.BAR_COLOR_RECENT);

            if (x + slotWidth > left && x < right) {
                String statusIcon = statusIconForRecord(run.record());
                if (isSplitTimes && splitTimes != null) {
                    for (int s = 0; s < segCount; s++) {
                        int segX = x + s * (segWidth + segGap);
                        long t = splitTimes[i][s];
                        int fillHeight = (int) ((t / splitGlobalMax) * availableBarHeight);
                        if (fillHeight < 0) fillHeight = 0;
                        int segColor = GSRRunHistoryParameters.SPLIT_SEGMENT_COLORS[s];
                        context.fill(segX, barBottom - fillHeight, segX + segWidth, barBottom, segColor);

                        boolean segHovered = mouseX >= segX && mouseX < segX + segWidth && mouseY >= barTop && mouseY < slotAreaBottom;
                        if (segHovered) {
                            hoveredBarIndex = i;
                            hoveredSegmentIndex = s;
                        }
                    }
                } else {
                    int fillHeight = barFillHeight(val, globalMax, availableBarHeight);
                    context.fill(x, barBottom - fillHeight, x + slotWidth, barBottom, color);
                }

                String barLabel;
                if (isSplitTimes && splitTimes != null) {
                    long lastT = getLastSplitTime(run.record());
                    barLabel = (isSelected ? "Sel" : ("#" + (i + 1))) + " " + GSRFormatUtil.formatTime(lastT);
                } else {
                    barLabel = (isSelected ? "Sel" : ("#" + (i + 1))) + " " + formatValue(row, val);
                }
                boolean labelHovered = mouseX >= x && mouseX < x + slotWidth && mouseY >= barTop && mouseY < slotAreaBottom;
                if (showLabels) {
                    drawBarLabelWithTicker(context, textRenderer, tickerState, "chart-overtime-val-" + i, barLabel, statusIcon, x, valueY, slotWidth, GSRRunHistoryParameters.BAR_VALUE_COLOR, labelHovered, left, top, right, bottom, uniformScale);
                }

                if (labelHovered) {
                    hoverTooltip = new ArrayList<>();
                    hoverTooltip.add(formatRunLabelForTooltip(run, 2));
                    if (hoveredSegmentIndex >= 0 && hoveredSegmentIndex < 5 && splitTimes != null) {
                        hoverTooltip.add(SPLIT_NAMES[hoveredSegmentIndex] + ": " + GSRFormatUtil.formatTime(splitTimes[i][hoveredSegmentIndex]));
                    }
                    hoverTooltip.addAll(Arrays.asList(GSRStatusText.buildForCompletedRun(run.record(), run.snapshots()).split("\n")));
                    hoveredBarIndex = i;
                }
            }
            x += slotWidth + GSRRunHistoryParameters.BAR_GAP;
        }

        if (hoverTooltip != null) {
            context.disableScissor();
            long elapsed = tickerState.getTooltipElapsedMs("tooltip-overtime-" + hoveredBarIndex + "-" + hoveredSegmentIndex, System.currentTimeMillis());
            GSRStandardTooltip.drawFromStrings(context, textRenderer, hoverTooltip, GSRStandardTooltip.CURSOR_OFFSET, mouseX, mouseY, screenWidth, screenHeight, elapsed);
            context.enableScissor(left, top, right, bottom);
        }
    }

    /**
     * Recent/Best view: Sel (average of selected runs), Avg, 5 comparison runs.
     */
    private static void renderCompareChart(DrawContext context, TextRenderer textRenderer,
                                           GSRTickerState tickerState,
                                           GSRRunHistoryStatRow row, List<GSRRunSaveState> selectedRuns,
                                           List<GSRRunSaveState> runs, int viewMode, String description,
                                           int left, int top, int right, int bottom, int scrollY,
                                           int mouseX, int mouseY, int screenWidth, int screenHeight,
                                           ToDoubleFunction<GSRRunSaveState> acc) {
        double selectedVal = selectedRuns.stream().mapToDouble(acc::applyAsDouble).average().orElse(0);
        double avgVal = runs.stream().mapToDouble(acc::applyAsDouble).average().orElse(0);

        List<GSRRunSaveState> comparisonRuns = new ArrayList<>(runs);
        if (viewMode == 1) {
            boolean timeBased = "Run Time".equals(row.label()) || "Split Times".equals(row.label());
            Comparator<GSRRunSaveState> cmp = Comparator.comparingDouble(acc::applyAsDouble);
            if (!timeBased) cmp = cmp.reversed();
            comparisonRuns.sort(cmp);
        } else if (viewMode == 3) {
            boolean timeBased = "Run Time".equals(row.label()) || "Split Times".equals(row.label());
            Comparator<GSRRunSaveState> cmp = Comparator.comparingDouble(acc::applyAsDouble);
            if (timeBased) cmp = cmp.reversed();
            comparisonRuns.sort(cmp);
        }

        double[] recent5 = new double[5];
        GSRRunSaveState[] comparisonRefs = new GSRRunSaveState[5];
        int n = comparisonRuns.size();
        int count = Math.min(5, n);
        for (int i = 0; i < count; i++) {
            GSRRunSaveState r = comparisonRuns.get(i);
            recent5[i] = acc.applyAsDouble(r);
            comparisonRefs[i] = r;
        }

        double globalMax = selectedVal;
        globalMax = Math.max(globalMax, avgVal);
        for (double v : recent5) globalMax = Math.max(globalMax, v);
        if (globalMax < 0.001) globalMax = 1;

        int chartWidth = right - left - GSRRunHistoryParameters.CONTENT_PADDING;
        int maxBarWidth = (int) (chartWidth * GSRRunHistoryParameters.BAR_MAX_WIDTH_FRACTION);
        int slotWidth = (chartWidth - GSRRunHistoryParameters.BAR_GAP * (GSRRunHistoryParameters.NUM_COMPARE_BARS - 1)) / GSRRunHistoryParameters.NUM_COMPARE_BARS;
        slotWidth = Math.min(slotWidth, maxBarWidth);

        int contentHeight = bottom - top;
        // Use compact layout when container is small so chart fits without scroll.
        boolean compact = contentHeight < GSRRunHistoryParameters.CHART_COMPACT_THRESHOLD;
        int topOffset = compact ? 1 : 2;
        int axisTextHeight = compact ? GSRRunHistoryParameters.CHART_AXIS_TEXT_HEIGHT_COMPACT : GSRRunHistoryParameters.CHART_AXIS_TEXT_HEIGHT;
        int titleMaxWidth = Math.max(1, right - left);

        int titleY = top + topOffset - scrollY;
        String titleWithUnits = row.chartTitle() + row.unitSuffix();
        int titleHeight = drawWrappedText(context, textRenderer, titleWithUnits, left, titleY, titleMaxWidth, GSRRunHistoryParameters.CHART_LABEL_COLOR);
        int descY = titleY + titleHeight;
        String descText = (description != null && !description.isEmpty()) ? description : CHART_DESCRIPTION;
        int descHeight = drawWrappedText(context, textRenderer, descText, left, descY, titleMaxWidth, GSRRunHistoryParameters.BAR_LABEL_COLOR);

        int headerBottom = descY + descHeight;
        int margin = computeChartMargin(compact, right - left);
        int valueY = bottom - margin - axisTextHeight;
        int barBottom = valueY - GSRRunHistoryParameters.CHART_LABEL_Y_OFFSET;
        int barAreaTop = headerBottom + margin;
        int availableBarHeight = Math.max(GSRRunHistoryParameters.BAR_HEIGHT, barBottom - barAreaTop);
        int barTop = Math.max(barAreaTop, barBottom - availableBarHeight);

        GSRRunSaveState selRunForIcon = selectedRuns.size() == 1 ? selectedRuns.get(0) : null;
        double[] values = { selectedVal, avgVal, recent5[0], recent5[1], recent5[2], recent5[3], recent5[4] };
        GSRRunSaveState[] runRefs = { selRunForIcon, null, comparisonRefs[0], comparisonRefs[1], comparisonRefs[2], comparisonRefs[3], comparisonRefs[4] };
        int[] colors = {
            GSRRunHistoryParameters.BAR_COLOR_SELECTED,
            GSRRunHistoryParameters.BAR_COLOR_AVG,
            GSRRunHistoryParameters.BAR_COLOR_RECENT,
            GSRRunHistoryParameters.BAR_COLOR_RECENT,
            GSRRunHistoryParameters.BAR_COLOR_RECENT,
            GSRRunHistoryParameters.BAR_COLOR_RECENT,
            GSRRunHistoryParameters.BAR_COLOR_RECENT
        };
        String[] slotLabels = { "Sel", "Avg", "#1", "#2", "#3", "#4", "#5" };

        boolean isSplitTimes = "Split Times".equals(row.label());
        long[][] splitTimes = null;
        double splitGlobalMax = globalMax;
        if (isSplitTimes) {
            splitTimes = new long[7][5];
            splitTimes[0] = getAvgSplitTimes(selectedRuns);
            splitTimes[1] = getAvgSplitTimes(runs);
            for (int j = 2; j < 7; j++) {
                splitTimes[j] = runRefs[j] != null ? getSplitTimes(runRefs[j].record()) : new long[5];
            }
            splitGlobalMax = 0.001;
            for (int j = 0; j < 7; j++) {
                for (int s = 0; s < 5; s++) splitGlobalMax = Math.max(splitGlobalMax, splitTimes[j][s]);
            }
        }

        long[] lastSplitTimes = null;
        if (isSplitTimes) {
            lastSplitTimes = new long[7];
            lastSplitTimes[0] = getAvgLastSplitTime(selectedRuns);
            lastSplitTimes[1] = getAvgLastSplitTime(runs);
            for (int j = 2; j < 7; j++) {
                lastSplitTimes[j] = runRefs[j] != null ? getLastSplitTime(runRefs[j].record()) : 0;
            }
        }

        int labelMargin = GSRRunHistoryParameters.BUTTON_TICKER_MARGIN;
        int maxLabelWidth = Math.max(1, slotWidth - 2 * labelMargin);
        float uniformScale = 1f;
        for (int i = 0; i < GSRRunHistoryParameters.NUM_COMPARE_BARS; i++) {
            String lbl;
            if (isSplitTimes && lastSplitTimes != null) {
                lbl = slotLabels[i] + " " + GSRFormatUtil.formatTime(lastSplitTimes[i]);
            } else {
                lbl = slotLabels[i] + " " + formatValue(row, values[i]);
            }
            int w = textRenderer.getWidth(lbl);
            if (w > maxLabelWidth) uniformScale = Math.min(uniformScale, (float) maxLabelWidth / w);
        }

        int x = left;
        int slotAreaBottom = valueY + 2 * textRenderer.fontHeight;
        List<String> hoverTooltip = null;
        int hoveredBarIndex = -1;
        int hoveredSegmentIndex = -1;

        int segCount = GSRRunHistoryParameters.SPLIT_SEGMENT_COUNT;
        int segGap = GSRRunHistoryParameters.SPLIT_SEGMENT_GAP;
        int segWidth = isSplitTimes ? Math.max(1, (slotWidth - (segCount - 1) * segGap) / segCount) : slotWidth;

        boolean drawKey = false;
        if (isSplitTimes && splitTimes != null) {
            int maxFillH = 0;
            for (int j = 0; j < 7; j++) {
                for (int s = 0; s < 5; s++) {
                    int h = (int) ((splitTimes[j][s] / splitGlobalMax) * availableBarHeight);
                    if (h > maxFillH) maxFillH = h;
                }
            }
            drawKey = maxFillH >= GSRRunHistoryParameters.SPLIT_KEY_HEIGHT;
        }

        for (int i = 0; i < GSRRunHistoryParameters.NUM_COMPARE_BARS; i++) {
            context.fill(x, barTop, x + slotWidth, barBottom, GSRRunHistoryParameters.BAR_SLOT_BG);
            x += slotWidth + GSRRunHistoryParameters.BAR_GAP;
        }
        if (drawKey) {
            drawSplitKey(context, textRenderer, left, barTop, chartWidth);
        }
        x = left;
        for (int i = 0; i < GSRRunHistoryParameters.NUM_COMPARE_BARS; i++) {
            if (isSplitTimes && splitTimes != null) {
                for (int s = 0; s < segCount; s++) {
                    int segX = x + s * (segWidth + segGap);
                    long t = splitTimes[i][s];
                    int fillHeight = (int) ((t / splitGlobalMax) * availableBarHeight);
                    if (fillHeight < 0) fillHeight = 0;
                    int segColor = GSRRunHistoryParameters.SPLIT_SEGMENT_COLORS[s];
                    context.fill(segX, barBottom - fillHeight, segX + segWidth, barBottom, segColor);

                    boolean segHovered = mouseX >= segX && mouseX < segX + segWidth && mouseY >= barTop && mouseY < slotAreaBottom;
                    if (segHovered) {
                        hoveredBarIndex = i;
                        hoveredSegmentIndex = s;
                    }
                }
            } else {
                int fillHeight = barFillHeight(values[i], globalMax, availableBarHeight);
                context.fill(x, barBottom - fillHeight, x + slotWidth, barBottom, colors[i]);
            }

            String statusIcon = "";
            if (!isSplitTimes) {
                if (i == 0 && runRefs[0] != null) {
                    statusIcon = statusIconForRecord(runRefs[0].record());
                } else if (i >= 2 && runRefs[i] != null) {
                    statusIcon = statusIconForRecord(runRefs[i].record());
                }
            }

            String barLabel;
            if (isSplitTimes && lastSplitTimes != null) {
                barLabel = slotLabels[i] + " " + GSRFormatUtil.formatTime(lastSplitTimes[i]);
            } else {
                barLabel = slotLabels[i] + " " + formatValue(row, values[i]);
            }
            boolean barHovered = mouseX >= x && mouseX < x + slotWidth && mouseY >= barTop && mouseY < slotAreaBottom;
            drawBarLabelWithTicker(context, textRenderer, tickerState, "chart-compare-val-" + i, barLabel, statusIcon, x, valueY, slotWidth, GSRRunHistoryParameters.BAR_VALUE_COLOR, barHovered, left, top, right, bottom, uniformScale);

            if (barHovered && (runRefs[i] != null || i == 0 || i == 1)) {
                hoverTooltip = new ArrayList<>();
                if (runRefs[i] != null) {
                    String runName = formatRunLabelForTooltip(runRefs[i], viewMode);
                    hoverTooltip.add(runName);
                    if (hoveredSegmentIndex >= 0 && hoveredSegmentIndex < 5 && splitTimes != null) {
                        hoverTooltip.add(SPLIT_NAMES[hoveredSegmentIndex] + ": " + GSRFormatUtil.formatTime(splitTimes[i][hoveredSegmentIndex]));
                    }
                    hoverTooltip.addAll(Arrays.asList(GSRStatusText.buildForCompletedRun(runRefs[i].record(), runRefs[i].snapshots()).split("\n")));
                } else if (i == 0 && selectedRuns.size() > 1) {
                    hoverTooltip.add("Average of " + selectedRuns.size() + " selected runs");
                    hoverTooltip.addAll(buildAvgBarTooltip(row, selectedRuns, acc, isSplitTimes, splitTimes, hoveredSegmentIndex));
                } else if (i == 1) {
                    hoverTooltip.addAll(buildAvgBarTooltip(row, runs, acc, isSplitTimes, splitTimes, hoveredSegmentIndex));
                }
                hoveredBarIndex = i;
            }
            x += slotWidth + GSRRunHistoryParameters.BAR_GAP;
        }
        if (hoverTooltip != null) {
            context.disableScissor();
            long elapsed = tickerState.getTooltipElapsedMs("tooltip-compare-" + hoveredBarIndex + "-" + hoveredSegmentIndex, System.currentTimeMillis());
            GSRStandardTooltip.drawFromStrings(context, textRenderer, hoverTooltip, GSRStandardTooltip.CURSOR_OFFSET, mouseX, mouseY, screenWidth, screenHeight, elapsed);
            context.enableScissor(left, top, right, bottom);
        }
    }

    /**
     * Draws bar label beneath a bar, with optional pass/fail icon on a second row.
     * Uses uniformScale so all labels match the smallest size.
     * Text is drawn inset by margin so it is not clipped by the scissor.
     *
     * @param statusIcon Optional status icon (e.g. dragon/skull) to draw centered on second row; null or empty to skip.
     */
    private static void drawBarLabelWithTicker(DrawContext context, TextRenderer textRenderer,
                                               GSRTickerState tickerState, String tickerKey,
                                               String label, String statusIcon, int x, int y, int slotWidth, int color, boolean hovered,
                                               int clipLeft, int clipTop, int clipRight, int clipBottom,
                                               float uniformScale) {
        int margin = GSRRunHistoryParameters.BUTTON_TICKER_MARGIN;
        int textX = x + margin;
        int rowHeight = textRenderer.fontHeight;
        int contentHeight = rowHeight + (statusIcon != null && !statusIcon.isEmpty() ? rowHeight : 0);
        int scissorLeft = Math.max(clipLeft, x + margin);
        int scissorTop = Math.max(clipTop, y - 2);
        int scissorRight = Math.min(clipRight, x + slotWidth - margin);
        int scissorBottom = Math.min(clipBottom, y + contentHeight + 2);
        boolean scissorValid = scissorRight > scissorLeft && scissorBottom > scissorTop;
        if (scissorValid) {
            context.enableScissor(scissorLeft, scissorTop, scissorRight, scissorBottom);
        }
        if (uniformScale >= 1f) {
            context.drawText(textRenderer, label, textX, y, color, false);
        } else {
            var matrices = context.getMatrices();
            matrices.pushMatrix();
            matrices.translate(textX, y);
            matrices.scale(uniformScale, uniformScale);
            context.drawText(textRenderer, label, 0, 0, color, false);
            matrices.popMatrix();
        }
        if (statusIcon != null && !statusIcon.isEmpty()) {
            int iconW = textRenderer.getWidth(statusIcon);
            int iconX = x + (slotWidth - iconW) / 2;
            int iconY = y + rowHeight;
            context.drawText(textRenderer, statusIcon, iconX, iconY, GSRRunHistoryParameters.STATUS_ICON_ON_BAR_COLOR, false);
        }
        if (scissorValid) {
            context.disableScissor();
        }
    }

    /** Computes bar fill height; ensures at least 1px for small positive values so they remain visible. */
    static int barFillHeight(double value, double globalMax, int availableBarHeight) {
        int h = (int) ((value / globalMax) * availableBarHeight);
        if (h < 0) return 0;
        if (h == 0 && value > 0.001) return 1;
        return h;
    }

    static int computeChartMargin(boolean compact, int panelWidth) {
        int margin = compact ? GSRRunHistoryParameters.CHART_BAR_Y_OFFSET_COMPACT : (int) (panelWidth * GSRRunHistoryParameters.PANEL_GAP_FRACTION);
        return margin <= 0 ? (compact ? GSRRunHistoryParameters.CHART_BAR_Y_OFFSET_COMPACT : GSRRunHistoryParameters.CHART_BAR_Y_OFFSET) : margin;
    }

    /**
     * Draws the split times color key across the top of the bar area (N, B, F, E, D).
     * Drawn in background before bars; bars that reach the top overlap it.
     */
    private static void drawSplitKey(DrawContext context, TextRenderer textRenderer,
                                     int left, int keyTop, int chartWidth) {
        int keyH = GSRRunHistoryParameters.SPLIT_KEY_HEIGHT;
        int segCount = GSRRunHistoryParameters.SPLIT_SEGMENT_COUNT;
        int segW = Math.max(1, chartWidth / segCount);
        for (int s = 0; s < segCount; s++) {
            int segX = left + s * segW;
            int color = GSRRunHistoryParameters.SPLIT_SEGMENT_COLORS[s];
            context.fill(segX, keyTop, segX + segW, keyTop + keyH, color);
            String abbrev = SPLIT_KEY_ABBREV[s];
            int textW = textRenderer.getWidth(abbrev);
            int textX = segX + (segW - textW) / 2;
            int textY = keyTop + (keyH - textRenderer.fontHeight) / 2;
            context.drawText(textRenderer, abbrev, textX, textY, GSRRunHistoryParameters.SPLIT_KEY_TEXT_COLOR, false);
        }
    }

    /**
     * Draws text wrapped to maxWidth, one line per row. Returns total height in pixels.
     */
    static int drawWrappedText(DrawContext context, TextRenderer textRenderer, String text,
                              int left, int y, int maxWidth, int color) {
        if (text == null || text.isEmpty()) return 0;
        List<OrderedText> lines = textRenderer.wrapLines(Text.literal(text), Math.max(1, maxWidth));
        int fontHeight = textRenderer.fontHeight;
        for (int i = 0; i < lines.size(); i++) {
            context.drawText(textRenderer, lines.get(i), left, y + i * fontHeight, color, false);
        }
        return lines.size() * fontHeight;
    }

    private static String formatValue(GSRRunHistoryStatRow row, double value) {
        if ("Run Time".equals(row.label()) || "Split Times".equals(row.label())) {
            return GSRFormatUtil.formatTime((long) value);
        }
        if (row.displayAsHearts()) {
            return String.format("%.1f", value / 2.0);
        }
        return row.isInt() ? String.valueOf((int) value) : String.format("%.1f", value);
    }

    /** Returns [timeNether, timeBastion, timeFortress, timeEnd, timeDragon] for a run. */
    private static long[] getSplitTimes(GSRRunRecord r) {
        return new long[] { r.timeNether(), r.timeBastion(), r.timeFortress(), r.timeEnd(), r.timeDragon() };
    }

    /** Last achieved split time, or run duration if none (time of fail). */
    private static long getLastSplitTime(GSRRunRecord r) {
        long dragon = r.timeDragon();
        if (dragon > 0) return dragon;
        long end = r.timeEnd();
        if (end > 0) return end;
        long fortress = r.timeFortress();
        if (fortress > 0) return fortress;
        long bastion = r.timeBastion();
        if (bastion > 0) return bastion;
        long nether = r.timeNether();
        if (nether > 0) return nether;
        return r.endMs() - r.startMs();
    }

    /**
     * Returns average split times across runs (for Avg slot bar segments).
     * Excludes 0 from each segment's average: 0 means the split was not achieved, not a fast time.
     */
    private static long[] getAvgSplitTimes(List<GSRRunSaveState> runs) {
        long[] sum = new long[5];
        int[] count = new int[5];
        for (GSRRunSaveState r : runs) {
            long[] t = getSplitTimes(r.record());
            for (int i = 0; i < 5; i++) {
                if (t[i] > 0) {
                    sum[i] += t[i];
                    count[i]++;
                }
            }
        }
        return new long[] {
                count[0] > 0 ? sum[0] / count[0] : 0,
                count[1] > 0 ? sum[1] / count[1] : 0,
                count[2] > 0 ? sum[2] / count[2] : 0,
                count[3] > 0 ? sum[3] / count[3] : 0,
                count[4] > 0 ? sum[4] / count[4] : 0
        };
    }

    /**
     * Computes count and average for runs filtered by status. Returns {count, avg} or {0, 0}.
     */
    private static double[] getCountAndAvg(List<GSRRunSaveState> runs, boolean victoryOnly,
                                          ToDoubleFunction<GSRRunSaveState> acc) {
        List<GSRRunSaveState> filtered = runs.stream()
                .filter(r -> victoryOnly == GSRRunRecord.STATUS_VICTORY.equals(r.record().status()))
                .toList();
        if (filtered.isEmpty()) return new double[] { 0, 0 };
        double sum = filtered.stream().mapToDouble(acc::applyAsDouble).sum();
        return new double[] { filtered.size(), sum / filtered.size() };
    }

    /**
     * For Split Times segment s: returns {count, avg} for runs that achieved that segment.
     * victoryOnly=true filters to victory runs; false to fail runs; null for all runs.
     */
    private static long[] getSplitSegmentCountAndAvg(List<GSRRunSaveState> runs, int segIndex,
                                                     Boolean victoryOnly) {
        long sum = 0;
        int count = 0;
        for (GSRRunSaveState r : runs) {
            if (victoryOnly != null && victoryOnly != GSRRunRecord.STATUS_VICTORY.equals(r.record().status())) continue;
            long[] times = getSplitTimes(r.record());
            if (times[segIndex] > 0) {
                sum += times[segIndex];
                count++;
            }
        }
        return new long[] { count, count > 0 ? sum / count : 0 };
    }

    /**
     * Average of last split times across runs (for Avg slot label).
     * Excludes 0: 0 means no split achieved (not a valid time for averaging).
     */
    private static long getAvgLastSplitTime(List<GSRRunSaveState> runs) {
        long sum = 0;
        int count = 0;
        for (GSRRunSaveState r : runs) {
            long t = getLastSplitTime(r.record());
            if (t > 0) {
                sum += t;
                count++;
            }
        }
        return count > 0 ? sum / count : 0;
    }

    /**
     * Returns {count, avg} for last split time, filtered by status. victoryOnly=null for all runs.
     */
    private static long[] getCountAndAvgLastSplit(List<GSRRunSaveState> runs, Boolean victoryOnly) {
        long sum = 0;
        int count = 0;
        for (GSRRunSaveState r : runs) {
            if (victoryOnly != null && victoryOnly != GSRRunRecord.STATUS_VICTORY.equals(r.record().status())) continue;
            long t = getLastSplitTime(r.record());
            if (t > 0) {
                sum += t;
                count++;
            }
        }
        return new long[] { count, count > 0 ? sum / count : 0 };
    }

    /**
     * Builds tooltip lines for the Avg bar: Overall, Success, Fail with counts and averages.
     */
    private static List<String> buildAvgBarTooltip(GSRRunHistoryStatRow row, List<GSRRunSaveState> runs,
                                                    ToDoubleFunction<GSRRunSaveState> acc,
                                                    boolean isSplitTimes, long[][] splitTimes,
                                                    int hoveredSegmentIndex) {
        List<String> lines = new ArrayList<>();
        String ov = GSRRunHistoryParameters.TOOLTIP_OVERALL_COLOR;
        String su = GSRRunHistoryParameters.TOOLTIP_SUCCESS_COLOR;
        String fa = GSRRunHistoryParameters.TOOLTIP_FAIL_COLOR;
        String wh = GSRRunHistoryParameters.TOOLTIP_WHITE;
        String av = GSRRunHistoryParameters.TOOLTIP_AVG_VALUE_COLOR;
        String dragon = GSRRunHistoryParameters.STATUS_ICON_VICTORY;
        String skull = GSRRunHistoryParameters.STATUS_ICON_FAIL;

        if (isSplitTimes && splitTimes != null && hoveredSegmentIndex >= 0 && hoveredSegmentIndex < 5) {
            long[] all = getSplitSegmentCountAndAvg(runs, hoveredSegmentIndex, null);
            long[] suc = getSplitSegmentCountAndAvg(runs, hoveredSegmentIndex, true);
            long[] fl = getSplitSegmentCountAndAvg(runs, hoveredSegmentIndex, false);
            lines.add(ov + "Overall: " + wh + all[0] + ov + " | Avg: " + av + GSRFormatUtil.formatTime(all[1]));
            lines.add(su + "Success " + wh + dragon + su + ": " + wh + suc[0] + su + " | Avg: " + av + GSRFormatUtil.formatTime(suc[1]));
            lines.add(fa + "Fail " + wh + skull + fa + ": " + wh + fl[0] + fa + " | Avg: " + av + GSRFormatUtil.formatTime(fl[1]));
        } else if (isSplitTimes) {
            long[] all = getCountAndAvgLastSplit(runs, null);
            long[] suc = getCountAndAvgLastSplit(runs, true);
            long[] fl = getCountAndAvgLastSplit(runs, false);
            lines.add(ov + "Overall: " + wh + all[0] + ov + " | Avg: " + av + GSRFormatUtil.formatTime(all[1]));
            lines.add(su + "Success " + wh + dragon + su + ": " + wh + suc[0] + su + " | Avg: " + av + GSRFormatUtil.formatTime(suc[1]));
            lines.add(fa + "Fail " + wh + skull + fa + ": " + wh + fl[0] + fa + " | Avg: " + av + GSRFormatUtil.formatTime(fl[1]));
        } else {
            double[] suc = getCountAndAvg(runs, true, acc);
            double[] fl = getCountAndAvg(runs, false, acc);
            int totalCount = runs.size();
            double overallAvg = runs.stream().mapToDouble(acc::applyAsDouble).average().orElse(0);
            lines.add(ov + "Overall: " + wh + totalCount + ov + " | Avg: " + av + formatValue(row, overallAvg));
            lines.add(su + "Success " + wh + dragon + su + ": " + wh + (int) suc[0] + su + " | Avg: " + av + formatValue(row, suc[1]));
            lines.add(fa + "Fail " + wh + skull + fa + ": " + wh + (int) fl[0] + fa + " | Avg: " + av + formatValue(row, fl[1]));
        }
        return lines;
    }

    /** Returns status icon for a run record (Active, Victory, or Fail). */
    private static String statusIconForRecord(GSRRunRecord rec) {
        return GSRRunRecord.STATUS_ACTIVE.equals(rec.status()) ? GSRRunHistoryParameters.STATUS_ICON_ACTIVE
                : (GSRRunRecord.STATUS_VICTORY.equals(rec.status()) ? GSRRunHistoryParameters.STATUS_ICON_VICTORY : GSRRunHistoryParameters.STATUS_ICON_FAIL);
    }

    /** Formats run label for tooltip: status icon + date + run time (world/date lines removed from body). */
    private static String formatRunLabelForTooltip(GSRRunSaveState run, int viewMode) {
        GSRRunRecord rec = run.record();
        String status = statusIconForRecord(rec);
        String dateIso = rec.endDateIso();
        String date = (dateIso != null && dateIso.length() >= 10) ? dateIso.substring(0, 10) : "?";
        if (viewMode == 2 && dateIso != null && dateIso.length() >= 16) {
            date = dateIso.substring(0, 16).replace('T', ' ');
        }
        long elapsed = rec.endMs() - rec.startMs();
        String timeStr = GSRFormatUtil.formatTime(elapsed);
        return status + " " + date + " §7| §f" + timeStr;
    }

    public static int computeContentHeight() {
        int margin = GSRRunHistoryParameters.CHART_BAR_Y_OFFSET;
        return GSRRunHistoryParameters.CHART_TITLE_HEIGHT
                + GSRRunHistoryParameters.CHART_DESCRIPTION_HEIGHT
                + margin
                + GSRRunHistoryParameters.CHART_APPROX_MAX_BAR_HEIGHT
                + GSRRunHistoryParameters.CHART_LABEL_Y_OFFSET
                + GSRRunHistoryParameters.CHART_AXIS_TEXT_HEIGHT
                + margin;
    }
}
