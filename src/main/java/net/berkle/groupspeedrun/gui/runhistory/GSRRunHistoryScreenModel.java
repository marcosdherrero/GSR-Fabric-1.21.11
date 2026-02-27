package net.berkle.groupspeedrun.gui.runhistory;

import net.berkle.groupspeedrun.data.GSRRunRecord;
import net.berkle.groupspeedrun.data.GSRRunSaveState;
import net.berkle.groupspeedrun.parameter.GSRRunHistoryParameters;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mutable state for the Run History screen. Shared by screen and UI components.
 * Components read/write this model; the screen coordinates input and delegates to components.
 */
public class GSRRunHistoryScreenModel {

    /** All runs loaded from disk. */
    public List<GSRRunSaveState> allRuns = new ArrayList<>();
    /** Filtered runs (by selected players). */
    public List<GSRRunSaveState> runs = new ArrayList<>();
    /** All unique player names across runs. */
    public List<String> allPlayerNames = new ArrayList<>();
    /** Distinct participant counts across runs (sorted ascending). */
    public List<Integer> allPlayerCounts = new ArrayList<>();
    /** Confirmed selected players for filter. */
    public Set<String> selectedPlayerFilter = new HashSet<>();
    /** Confirmed selected player counts for filter (e.g. {1, 2, 4}); empty = all. */
    public Set<Integer> selectedPlayerCountFilter = new HashSet<>();
    /** Confirmed selected run (Run Info tab); derived from selectedRuns. */
    public GSRRunSaveState selectedRun = null;
    /** Multi-select runs for all tabs; empty = all runs. Used for Run Info, Run Graphs Sel bar, Player Graphs. */
    public Set<GSRRunSaveState> selectedRuns = new HashSet<>();
    /** Active tab: 0=Run Info, 1=Run Graphs, 2=Player Graphs. */
    public int selectedTab = 0;
    /** Confirmed compare category index (into ALL_SORTED; default Run Time). */
    public int selectedCategoryIndex = GSRRunHistoryStatRow.DEFAULT_COMPARE_INDEX;
    /** Chart view index (display index into sorted View options; default Recent 5). */
    public int selectedChartViewIndex = GSRRunHistoryLeftColumn.CHART_VIEW_DEFAULT_INDEX;
    /** Player type index (display index into sorted Type options; default Avg). */
    public int selectedPlayerTypeIndex = GSRRunHistoryLeftColumn.TYPE_DEFAULT_INDEX;

    /** Whether Player Filter dropdown is open. */
    public boolean filterDropdownOpen = false;
    public boolean playerCountDropdownOpen = false;
    public int playerCountDropdownScroll = 0;
    public int filterDropdownScroll = 0;
    public boolean runsDropdownOpen = false;
    public int runsDropdownScroll = 0;
    public boolean compareDropdownOpen = false;
    public int compareDropdownScroll = 0;
    public boolean chartViewDropdownOpen = false;
    public boolean typeDropdownOpen = false;
    public int viewDropdownScroll = 0;

    /** Detail panel vertical scroll. */
    public int detailScroll = 0;
    /** Horizontal scroll for All time chart. */
    public int chartScrollX = 0;

    /** Pending selection while dropdown open; discarded on click-off. */
    public Set<String> pendingPlayerFilter = new HashSet<>();
    public Set<Integer> pendingPlayerCountFilter = new HashSet<>();
    /** Pending run indices for multi-select; applies to all tabs. */
    public Set<Integer> pendingRunIndices = new HashSet<>();
    public int pendingCategoryIndex = GSRRunHistoryStatRow.DEFAULT_COMPARE_INDEX;
    public int pendingViewIndex = 0;

    /** Ticker activation times (ms) for "active 10s after select". */
    public long filterSelectionTimeMs = 0;
    public long playerCountSelectionTimeMs = 0;
    public long runsSelectionTimeMs = 0;
    public long compareSelectionTimeMs = 0;
    public long viewSelectionTimeMs = 0;

    /** Last time a click was handled (ms). Used for anti-spam cooldown. */
    public long lastClickHandledTimeMs = 0;

    /** Run Info auto-scroll state: 0=idle, 1=hover_wait, 2=scroll_down, 3=pause_at_bottom, 4=scroll_up, 5=pause_at_top. */
    public int runInfoAutoScrollState = 0;
    /** When hover started (ms) for 2.5s delay. */
    public long runInfoAutoScrollHoverStartMs = 0;
    /** When current pause at top/bottom started (ms). */
    public long runInfoAutoScrollPauseStartMs = 0;
    /** Sub-pixel scroll offset during auto-scroll for smooth gradual motion. */
    public float runInfoAutoScrollOffset = 0f;

    /**
     * Returns runs for the detail panel. Uses selectedRuns (empty = all runs).
     */
    public List<GSRRunSaveState> getFilteredRunsForTab(int tab) {
        if (selectedRuns.isEmpty()) return runs;
        List<GSRRunSaveState> list = new ArrayList<>(selectedRuns);
        list.retainAll(new HashSet<>(runs));
        return list.isEmpty() ? runs : list;
    }

    /**
     * Returns runs for Run Graphs tab. Applies type filter: Success types (Best/Worst Success, Avg Success)
     * show only victory runs; Fail types (Best/Worst Fail, Avg Fail) show only fail runs.
     */
    public List<GSRRunSaveState> getRunsForRunGraphs() {
        List<GSRRunSaveState> base = getFilteredRunsForTab(GSRRunHistoryParameters.TAB_RUN_GRAPHS);
        return filterRunsByType(base, GSRRunHistoryLeftColumn.getTypeModeForDisplayIndex(selectedPlayerTypeIndex));
    }

    /**
     * Returns all runs for Run Graphs "All time" view. Applies type filter (Success/Fail) but not
     * Runs selection. Used when chart view index is 2 to display every run; filtered runs highlighted
     * in blue, selected runs in yellow.
     */
    public List<GSRRunSaveState> getRunsForRunGraphsAllTime() {
        return filterRunsByType(new ArrayList<>(runs), GSRRunHistoryLeftColumn.getTypeModeForDisplayIndex(selectedPlayerTypeIndex));
    }

    /** Filters runs by type mode: 1,3,5 = victory only; 2,4,6 = fail only; 0 = all. */
    private static List<GSRRunSaveState> filterRunsByType(List<GSRRunSaveState> runs, int typeMode) {
        if (typeMode == 1 || typeMode == 3 || typeMode == 5) { // Best Success, Worst Success, Avg Success
            return runs.stream().filter(r -> GSRRunRecord.STATUS_VICTORY.equals(r.record().status())).toList();
        }
        if (typeMode == 2 || typeMode == 4 || typeMode == 6) { // Best Fail, Worst Fail, Avg Fail
            return runs.stream().filter(r -> GSRRunRecord.STATUS_FAIL.equals(r.record().status())).toList();
        }
        return runs;
    }

    /**
     * Returns runs for Player Graphs tab. No type-based run filtering here; type controls aggregation
     * (Avg, Best/worst, Avg Success, Avg Fail) inside the player chart renderer.
     */
    public List<GSRRunSaveState> getRunsForPlayerGraphs() {
        return getFilteredRunsForTab(GSRRunHistoryParameters.TAB_PLAYER_GRAPHS);
    }

    /**
     * Derives selectedRun from selectedRuns; empty means all runs.
     * Sets selectedRun to the most recent (by endMs) of the valid filtered runs.
     */
    public void deriveSelectedRunForTab0() {
        Set<GSRRunSaveState> source = selectedRuns.isEmpty() ? new HashSet<>(runs) : new HashSet<>(selectedRuns);
        source.retainAll(new HashSet<>(runs));
        selectedRun = source.isEmpty() ? (runs.isEmpty() ? null : runs.get(0))
                : source.stream().max(Comparator.comparingLong(r -> r.record().endMs())).orElse(runs.isEmpty() ? null : runs.get(0));
    }
}
