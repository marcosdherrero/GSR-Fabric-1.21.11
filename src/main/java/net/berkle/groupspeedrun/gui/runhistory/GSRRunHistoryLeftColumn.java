package net.berkle.groupspeedrun.gui.runhistory;

import net.berkle.groupspeedrun.data.GSRRunRecord;
import net.berkle.groupspeedrun.data.GSRRunSaveState;
import net.berkle.groupspeedrun.gui.GSRTickerState;
import net.berkle.groupspeedrun.gui.components.GSRMultiSelectDropdown;
import net.berkle.groupspeedrun.gui.components.GSRMultiSelectDropdownBehavior;
import net.berkle.groupspeedrun.mixin.accessors.GSRPressableWidgetAccessor;
import net.berkle.groupspeedrun.parameter.GSRRunHistoryParameters;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;
import net.berkle.groupspeedrun.util.GSRColorHelper;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Renders the left column: 6 dropdown triggers stacked vertically (Player Filter, Player Count, Runs,
 * Compare, View, Type), and the dropdown overlay in the right panel when any is open.
 */
public final class GSRRunHistoryLeftColumn {

    /** Number of preset items at top of Runs dropdown (Last 5, Last 10, Last 20). */
    public static final int RUNS_PRESET_COUNT = 3;

    /** View options sorted alphabetically. Default = Recent 5 runs. */
    private static final String[] CHART_VIEW_OPTIONS_SORTED = sortedCopy("All time", "Best 5 runs", "Recent (Sel, Avg, 5)", "Worst 5 runs");
    /** Index of "Recent (Sel, Avg, 5)" in CHART_VIEW_OPTIONS_SORTED; default View selection. */
    public static final int CHART_VIEW_DEFAULT_INDEX = indexOf(CHART_VIEW_OPTIONS_SORTED, "Recent (Sel, Avg, 5)");
    /** Maps display index -> chart view mode: 0=Recent, 1=Best 5, 2=All, 3=Worst 5. */
    private static final int[] VIEW_DISPLAY_TO_MODE = { 2, 1, 0, 3 };

    /** Type options sorted alphabetically. Default = Avg. */
    private static final String[] PLAYER_TYPE_OPTIONS_SORTED = sortedCopy("Avg", "Avg Fail", "Avg Success", "Best Fail", "Best Success", "Worst Fail", "Worst Success");
    /** Index of "Avg" in PLAYER_TYPE_OPTIONS_SORTED; default Type selection. */
    public static final int TYPE_DEFAULT_INDEX = 0;
    /** Maps display index (alphabetical) -> type mode: 0=Avg, 1=Best Success, 2=Best Fail, 3=Worst Success, 4=Worst Fail, 5=Avg Success, 6=Avg Fail. */
    private static final int[] TYPE_DISPLAY_TO_MODE = { 0, 6, 5, 2, 1, 4, 3 };

    private static String[] sortedCopy(String... arr) {
        String[] copy = arr.clone();
        Arrays.sort(copy, String.CASE_INSENSITIVE_ORDER);
        return copy;
    }

    private static int indexOf(String[] arr, String s) {
        for (int i = 0; i < arr.length; i++) {
            if (s.equals(arr[i])) return i;
        }
        return 0;
    }

    /** Returns chart view mode (0=Recent, 1=Best 5, 2=All, 3=Worst 5) for display index. */
    public static int getViewModeForDisplayIndex(int displayIndex) {
        return displayIndex >= 0 && displayIndex < VIEW_DISPLAY_TO_MODE.length ? VIEW_DISPLAY_TO_MODE[displayIndex] : 0;
    }

    /** Returns type mode (0=Avg, 1=Best/worst, 2=Avg Success, 3=Avg Fail) for display index. */
    public static int getTypeModeForDisplayIndex(int displayIndex) {
        return displayIndex >= 0 && displayIndex < TYPE_DISPLAY_TO_MODE.length ? TYPE_DISPLAY_TO_MODE[displayIndex] : 0;
    }

    private final GSRMultiSelectDropdown<GSRRunHistoryScreenModel> filterDropdown;
    private final GSRMultiSelectDropdown<GSRRunHistoryScreenModel> playerCountDropdown;
    private final GSRMultiSelectDropdown<GSRRunHistoryScreenModel> runsDropdownMulti;
    private final GSRMultiSelectDropdown<GSRRunHistoryScreenModel> compareDropdown;
    private final GSRMultiSelectDropdown<GSRRunHistoryScreenModel> viewDropdown;
    private final GSRMultiSelectDropdown<GSRRunHistoryScreenModel> typeDropdown;

    public GSRRunHistoryLeftColumn() {
        filterDropdown = new GSRMultiSelectDropdown<>("Player Filter:", "Player Filter – select players", "dd-filter", true,
                new GSRMultiSelectDropdownBehavior<>() {
                    @Override
                    public List<String> getItems(GSRRunHistoryScreenModel model) {
                        return model.allPlayerNames;
                    }

                    @Override
                    public Set<Integer> getSelectedIndices(GSRRunHistoryScreenModel model) {
                        Set<Integer> indices = new HashSet<>();
                        for (int i = 0; i < model.allPlayerNames.size(); i++) {
                            if (model.pendingPlayerFilter.contains(model.allPlayerNames.get(i))) indices.add(i);
                        }
                        return indices;
                    }

                    @Override
                    public String getDisplayLabel(GSRRunHistoryScreenModel model) {
                        return model.selectedPlayerFilter.isEmpty() ? "All (" + model.allPlayerNames.size() + ")" : model.selectedPlayerFilter.size() + " selected";
                    }

                    @Override
                    public long getSelectionTimeMs(GSRRunHistoryScreenModel model) {
                        return model.filterSelectionTimeMs;
                    }
                });
        playerCountDropdown = new GSRMultiSelectDropdown<>("Player Count:", "Player Count – filter by party size", "dd-playercount", true,
                new GSRMultiSelectDropdownBehavior<>() {
                    @Override
                    public List<String> getItems(GSRRunHistoryScreenModel model) {
                        return model.allPlayerCounts.stream()
                                .map(c -> c == 1 ? "1 player" : c + " players")
                                .toList();
                    }

                    @Override
                    public Set<Integer> getSelectedIndices(GSRRunHistoryScreenModel model) {
                        Set<Integer> indices = new HashSet<>();
                        for (int i = 0; i < model.allPlayerCounts.size(); i++) {
                            if (model.pendingPlayerCountFilter.contains(model.allPlayerCounts.get(i))) indices.add(i);
                        }
                        return indices;
                    }

                    @Override
                    public String getDisplayLabel(GSRRunHistoryScreenModel model) {
                        return model.selectedPlayerCountFilter.isEmpty() ? "All (" + model.allPlayerCounts.size() + ")" : model.selectedPlayerCountFilter.size() + " selected";
                    }

                    @Override
                    public long getSelectionTimeMs(GSRRunHistoryScreenModel model) {
                        return model.playerCountSelectionTimeMs;
                    }
                });
        runsDropdownMulti = new GSRMultiSelectDropdown<>("Runs:", "Runs – select runs to include", "dd-runs", true,
                new GSRMultiSelectDropdownBehavior<>() {
                    @Override
                    public List<String> getItems(GSRRunHistoryScreenModel model) {
                        List<String> items = new ArrayList<>();
                        items.add(GSRRunHistoryParameters.RUNS_PRESET_LAST_5_LABEL);
                        items.add(GSRRunHistoryParameters.RUNS_PRESET_LAST_10_LABEL);
                        items.add(GSRRunHistoryParameters.RUNS_PRESET_LAST_20_LABEL);
                        items.addAll(model.runs.stream().map(GSRRunHistoryLeftColumn::formatRunLabel).toList());
                        return items;
                    }

                    @Override
                    public Set<Integer> getSelectedIndices(GSRRunHistoryScreenModel model) {
                        return model.pendingRunIndices.stream()
                                .map(i -> i + RUNS_PRESET_COUNT)
                                .collect(Collectors.toSet());
                    }

                    @Override
                    public String getDisplayLabel(GSRRunHistoryScreenModel model) {
                        if (model.selectedRuns.isEmpty()) return "All (" + model.runs.size() + ")";
                        return model.selectedRuns.size() + " run(s) selected";
                    }

                    @Override
                    public long getSelectionTimeMs(GSRRunHistoryScreenModel model) {
                        return model.runsSelectionTimeMs;
                    }
                });
        compareDropdown = new GSRMultiSelectDropdown<>("Compare:", "Compare – select a stat", "dd-compare", false,
                new GSRMultiSelectDropdownBehavior<>() {
                    @Override
                    public List<String> getItems(GSRRunHistoryScreenModel model) {
                        return GSRRunHistoryStatRow.ALL_SORTED.stream().map(GSRRunHistoryStatRow::label).toList();
                    }

                    @Override
                    public Set<Integer> getSelectedIndices(GSRRunHistoryScreenModel model) {
                        return Set.of(model.pendingCategoryIndex);
                    }

                    @Override
                    public String getDisplayLabel(GSRRunHistoryScreenModel model) {
                        return GSRRunHistoryStatRow.ALL_SORTED.get(model.selectedCategoryIndex).label();
                    }

                    @Override
                    public long getSelectionTimeMs(GSRRunHistoryScreenModel model) {
                        return model.compareSelectionTimeMs;
                    }
                });
        viewDropdown = new GSRMultiSelectDropdown<>("View:", "View – chart mode", "dd-view", false,
                new GSRMultiSelectDropdownBehavior<>() {
                    @Override
                    public List<String> getItems(GSRRunHistoryScreenModel model) {
                        return List.of(CHART_VIEW_OPTIONS_SORTED);
                    }

                    @Override
                    public Set<Integer> getSelectedIndices(GSRRunHistoryScreenModel model) {
                        return Set.of(model.pendingViewIndex);
                    }

                    @Override
                    public String getDisplayLabel(GSRRunHistoryScreenModel model) {
                        return CHART_VIEW_OPTIONS_SORTED[model.selectedChartViewIndex];
                    }

                    @Override
                    public long getSelectionTimeMs(GSRRunHistoryScreenModel model) {
                        return model.viewSelectionTimeMs;
                    }
                });
        typeDropdown = new GSRMultiSelectDropdown<>("Type:", "Type – aggregation", "dd-type", false,
                new GSRMultiSelectDropdownBehavior<>() {
                    @Override
                    public List<String> getItems(GSRRunHistoryScreenModel model) {
                        return List.of(PLAYER_TYPE_OPTIONS_SORTED);
                    }

                    @Override
                    public Set<Integer> getSelectedIndices(GSRRunHistoryScreenModel model) {
                        return Set.of(model.pendingViewIndex);
                    }

                    @Override
                    public String getDisplayLabel(GSRRunHistoryScreenModel model) {
                        return PLAYER_TYPE_OPTIONS_SORTED[model.selectedPlayerTypeIndex];
                    }

                    @Override
                    public long getSelectionTimeMs(GSRRunHistoryScreenModel model) {
                        return model.viewSelectionTimeMs;
                    }
                });
    }

    /**
     * Renders the left column: 6 dropdown triggers stacked vertically, or overlay in right panel when open.
     *
     * @param model        Screen model with state.
     * @param context      Draw context.
     * @param textRenderer Text renderer.
     * @param tickerState  Ticker state.
     * @param bounds       Two-column layout bounds.
     * @param mouseX       Mouse X.
     * @param mouseY       Mouse Y.
     */
    public void render(GSRRunHistoryScreenModel model, DrawContext context, TextRenderer textRenderer,
                      GSRTickerState tickerState, GSRRunHistoryLayout.TwoColumnBounds bounds, int mouseX, int mouseY) {
        int overlayLeft = bounds.overlayLeft();
        int overlayWidth = bounds.overlayWidth();
        int barHeight = bounds.barHeight();
        int barWidth = bounds.barWidth();
        float labelScale = computeLabelScale(barWidth);
        boolean dropdownOpen = model.filterDropdownOpen || model.playerCountDropdownOpen || model.runsDropdownOpen || model.compareDropdownOpen || model.chartViewDropdownOpen || model.typeDropdownOpen;

        if (dropdownOpen) {
            int listTop = bounds.leftPanelTop() + GSRRunHistoryParameters.SELECTION_CONTAINER_MARGIN;
            int selectionListBottom = bounds.selectionListBottom();
            int confirmButtonTop = bounds.makeSelectionContainerTop();
            int delimiterTop = selectionListBottom + GSRRunHistoryParameters.SELECTION_SCROLL_BEHIND_HEIGHT;
            context.fill(bounds.leftPanelLeft(), bounds.leftPanelTop(), bounds.leftPanelLeft() + bounds.leftPanelWidth(), bounds.leftPanelBottom(), GSRUiParameters.CONTENT_BOX_BG);
            int listBottom = delimiterTop + GSRRunHistoryParameters.SELECTION_DELIMITER_BAR_HEIGHT;
            if (model.filterDropdownOpen && !model.allPlayerNames.isEmpty()) {
                filterDropdown.renderOverlay(model, context, textRenderer, tickerState, overlayLeft, listTop, listBottom, overlayWidth,
                        model.filterDropdownScroll, mouseX, mouseY);
            } else if (model.playerCountDropdownOpen && !model.allPlayerCounts.isEmpty()) {
                playerCountDropdown.renderOverlay(model, context, textRenderer, tickerState, overlayLeft, listTop, listBottom, overlayWidth,
                        model.playerCountDropdownScroll, mouseX, mouseY);
            } else if (model.runsDropdownOpen && (model.runs.size() + RUNS_PRESET_COUNT > 0)) {
                runsDropdownMulti.renderOverlay(model, context, textRenderer, tickerState, overlayLeft, listTop, listBottom, overlayWidth,
                        model.runsDropdownScroll, mouseX, mouseY);
            } else if (model.compareDropdownOpen) {
                compareDropdown.renderOverlay(model, context, textRenderer, tickerState, overlayLeft, listTop, listBottom, overlayWidth,
                        model.compareDropdownScroll, mouseX, mouseY);
            } else if (model.chartViewDropdownOpen) {
                viewDropdown.renderOverlay(model, context, textRenderer, tickerState, overlayLeft, listTop, listBottom, overlayWidth,
                        model.viewDropdownScroll, mouseX, mouseY);
            } else if (model.typeDropdownOpen) {
                typeDropdown.renderOverlay(model, context, textRenderer, tickerState, overlayLeft, listTop, listBottom, overlayWidth,
                        model.viewDropdownScroll, mouseX, mouseY);
            }
            context.fill(overlayLeft, delimiterTop, overlayLeft + overlayWidth,
                    delimiterTop + GSRRunHistoryParameters.SELECTION_DELIMITER_BAR_HEIGHT,
                    GSRRunHistoryParameters.SELECTION_DELIMITER_BAR_COLOR);
            boolean confirmHover = mouseX >= overlayLeft && mouseX < overlayLeft + overlayWidth
                    && mouseY >= confirmButtonTop && mouseY < confirmButtonTop + GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT;
            boolean hasPendingChanges = hasPendingSelectionChanges(model);
            var textures = GSRPressableWidgetAccessor.gsr$getTextures();
            var confirmTexture = textures.get(true, confirmHover);
            int confirmWidth = overlayWidth - 2 * GSRRunHistoryParameters.CONTAINER_INSET;
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, confirmTexture, overlayLeft + GSRRunHistoryParameters.CONTAINER_INSET, confirmButtonTop, confirmWidth,
                    GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT);
            if (hasPendingChanges) {
                float breath = (float) ((1 + Math.sin(System.currentTimeMillis() * Math.PI * 2 / GSRRunHistoryParameters.MAKE_SELECTION_BREATHE_PERIOD_MS)) / 2);
                float alpha = GSRRunHistoryParameters.MAKE_SELECTION_BREATHE_ALPHA_MIN
                        + (GSRRunHistoryParameters.MAKE_SELECTION_BREATHE_ALPHA_MAX - GSRRunHistoryParameters.MAKE_SELECTION_BREATHE_ALPHA_MIN) * breath;
                int glowColor = GSRColorHelper.applyAlpha(GSRRunHistoryParameters.MAKE_SELECTION_BREATHE_GLOW_COLOR, alpha);
                int border = GSRRunHistoryParameters.MAKE_SELECTION_GLOW_BORDER;
                int confirmLeft = overlayLeft + GSRRunHistoryParameters.CONTAINER_INSET;
                context.fill(confirmLeft - border, confirmButtonTop - border, confirmLeft + confirmWidth + border, confirmButtonTop, glowColor);
                context.fill(confirmLeft - border, confirmButtonTop + GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT, confirmLeft + confirmWidth + border, confirmButtonTop + GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT + border, glowColor);
                context.fill(confirmLeft - border, confirmButtonTop, confirmLeft, confirmButtonTop + GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT, glowColor);
                context.fill(confirmLeft + confirmWidth, confirmButtonTop, confirmLeft + confirmWidth + border, confirmButtonTop + GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT, glowColor);
            }
            int confirmCenterX = overlayLeft + overlayWidth / 2;
            int confirmTextY = confirmButtonTop + (GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT - textRenderer.fontHeight) / 2;
            context.drawCenteredTextWithShadow(textRenderer, GSRMultiSelectDropdown.CONFIRM_BUTTON_TEXT, confirmCenterX, confirmTextY, GSRRunHistoryParameters.TEXT_COLOR);
        } else {
            context.fill(bounds.leftPanelLeft(), bounds.leftPanelTop(), bounds.leftPanelLeft() + bounds.leftPanelWidth(), bounds.leftPanelBottom(), GSRUiParameters.CONTENT_BOX_BG);
            int[] st = bounds.sectionTops();
            int row0 = bounds.barTop(0);
            int row1 = bounds.barTop(1);
            int row2 = bounds.barTop(2);
            int row3 = bounds.barTop(3);
            int row4 = bounds.barTop(4);
            int row5 = bounds.barTop(5);
            int sectionLeft = bounds.leftPanelLeft();
            int fullBarWidth = bounds.leftPanelWidth();
            int barLeft = sectionLeft + GSRRunHistoryParameters.LEFT_COLUMN_INTERIOR_MARGIN;
            int barDrawWidth = fullBarWidth - 2 * GSRRunHistoryParameters.LEFT_COLUMN_INTERIOR_MARGIN;
            filterDropdown.renderTrigger(model, context, textRenderer, tickerState, sectionLeft, st[0], row0,
                    fullBarWidth, barHeight, labelScale, false, filterDropdown.isBarHovered(barLeft, row0, barDrawWidth, barHeight, mouseX, mouseY));
            if (!model.allPlayerCounts.isEmpty()) {
                playerCountDropdown.renderTrigger(model, context, textRenderer, tickerState, sectionLeft, st[1], row1,
                        fullBarWidth, barHeight, labelScale, false, playerCountDropdown.isBarHovered(barLeft, row1, barDrawWidth, barHeight, mouseX, mouseY));
            }
            runsDropdownMulti.renderTrigger(model, context, textRenderer, tickerState, sectionLeft, st[2], row2,
                    fullBarWidth, barHeight, labelScale, false, runsDropdownMulti.isBarHovered(barLeft, row2, barDrawWidth, barHeight, mouseX, mouseY));
            compareDropdown.renderTrigger(model, context, textRenderer, tickerState, sectionLeft, st[3], row3,
                    fullBarWidth, barHeight, labelScale, false, compareDropdown.isBarHovered(barLeft, row3, barDrawWidth, barHeight, mouseX, mouseY));
            viewDropdown.renderTrigger(model, context, textRenderer, tickerState, sectionLeft, st[4], row4,
                    fullBarWidth, barHeight, labelScale, false, viewDropdown.isBarHovered(barLeft, row4, barDrawWidth, barHeight, mouseX, mouseY));
            typeDropdown.renderTrigger(model, context, textRenderer, tickerState, sectionLeft, st[5], row5,
                    fullBarWidth, barHeight, labelScale, false, typeDropdown.isBarHovered(barLeft, row5, barDrawWidth, barHeight, mouseX, mouseY));
        }
    }

    /** Scale for labels to fit bar width; clamps between min and full scale. */
    private static float computeLabelScale(int barWidth) {
        float fitScale = barWidth / (float) GSRRunHistoryParameters.LEFT_COLUMN_REFERENCE_BAR_WIDTH;
        return Math.max(GSRRunHistoryParameters.LEFT_COLUMN_MIN_FIT_SCALE,
                Math.min(GSRRunHistoryParameters.LEFT_COLUMN_LABEL_SCALE, fitScale));
    }

    /** True when the open dropdown has unconfirmed changes (pending != selected). */
    private static boolean hasPendingSelectionChanges(GSRRunHistoryScreenModel model) {
        if (model.filterDropdownOpen) {
            return !model.pendingPlayerFilter.equals(model.selectedPlayerFilter);
        }
        if (model.playerCountDropdownOpen) {
            return !model.pendingPlayerCountFilter.equals(model.selectedPlayerCountFilter);
        }
        if (model.runsDropdownOpen) {
            Set<Integer> selectedIndices = new HashSet<>();
            for (int i = 0; i < model.runs.size(); i++) {
                if (model.selectedRuns.contains(model.runs.get(i))) selectedIndices.add(i);
            }
            return !model.pendingRunIndices.equals(selectedIndices);
        }
        if (model.compareDropdownOpen) {
            return model.pendingCategoryIndex != model.selectedCategoryIndex;
        }
        if (model.chartViewDropdownOpen) {
            return model.pendingViewIndex != model.selectedChartViewIndex;
        }
        if (model.typeDropdownOpen) {
            return model.pendingViewIndex != model.selectedPlayerTypeIndex;
        }
        return false;
    }


    private static String formatRunLabel(GSRRunSaveState state) {
        GSRRunRecord r = state.record();
        String date = r.endDateIso();
        if (date != null && date.length() >= 10) date = date.substring(0, 10);
        else date = "?";
        String status;
        if (GSRRunRecord.STATUS_ACTIVE.equals(r.status())) {
            status = GSRRunHistoryParameters.STATUS_ICON_ACTIVE;
        } else if (GSRRunRecord.STATUS_VICTORY.equals(r.status())) {
            status = GSRRunHistoryParameters.STATUS_ICON_VICTORY;
        } else {
            status = GSRRunHistoryParameters.STATUS_ICON_FAIL;
        }
        return status + " " + (r.worldName() != null ? r.worldName() : "?") + " " + date;
    }

    /** Returns Runs dropdown: multi-select on all tabs. */
    public GSRMultiSelectDropdown<GSRRunHistoryScreenModel> getRunsDropdown(GSRRunHistoryScreenModel model) {
        return runsDropdownMulti;
    }

    /** True if item index is a preset (Last 5, Last 10, Last 20); presets apply immediately on click. */
    public static boolean isRunsPresetIndex(int itemIndex) {
        return itemIndex >= 0 && itemIndex < RUNS_PRESET_COUNT;
    }

    /** Run count for preset at index 0=5, 1=10, 2=20. */
    public static int getRunsPresetCount(int presetIndex) {
        return switch (presetIndex) {
            case 0 -> 5;
            case 1 -> 10;
            case 2 -> 20;
            default -> 0;
        };
    }

    /** Dropdowns for screen mouse handling. */
    public GSRMultiSelectDropdown<GSRRunHistoryScreenModel> getFilterDropdown() { return filterDropdown; }
    public GSRMultiSelectDropdown<GSRRunHistoryScreenModel> getPlayerCountDropdown() { return playerCountDropdown; }
    public GSRMultiSelectDropdown<GSRRunHistoryScreenModel> getCompareDropdown() { return compareDropdown; }
    public GSRMultiSelectDropdown<GSRRunHistoryScreenModel> getViewDropdown() { return viewDropdown; }
    public GSRMultiSelectDropdown<GSRRunHistoryScreenModel> getTypeDropdown() { return typeDropdown; }
}
