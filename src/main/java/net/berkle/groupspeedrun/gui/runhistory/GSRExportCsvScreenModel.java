package net.berkle.groupspeedrun.gui.runhistory;

import java.util.HashSet;
import java.util.Set;

/**
 * Model for the Export CSV popup. Extends {@link GSRRunHistoryScreenModel} with
 * multi-select run indices for export. Uses pendingPlayerFilter for player filter
 * and runs (filtered by player) for the runs list.
 */
public final class GSRExportCsvScreenModel extends GSRRunHistoryScreenModel {

    /** Indices of runs to export (into {@link #runs}). Empty = export all runs. */
    public final Set<Integer> exportSelectedRunIndices = new HashSet<>();

    /** Whether Runs dropdown is open. */
    public boolean runsDropdownOpen = false;
    /** Scroll offset for runs dropdown list. */
    public int runsDropdownScroll = 0;
    /** Whether Player Filter dropdown is open. */
    public boolean filterDropdownOpen = false;
    /** Scroll offset for filter dropdown list. */
    public int filterDropdownScroll = 0;
    /** Vertical scroll for right-panel average run info (pixels). */
    public int exportDetailScroll = 0;
}
