package net.berkle.groupspeedrun.gui;

import net.berkle.groupspeedrun.parameter.GSRRunHistoryParameters;
import net.berkle.groupspeedrun.parameter.GSRTooltipParameters;

/**
 * Tracks ticker and tooltip animation state so animations start at the beginning
 * when first activated and play through one full cycle back to the start.
 */
public final class GSRTickerState {

    private String lastKey = null;
    private long startTimeMs = 0;
    private boolean drewTicker = false;

    private String tooltipLastKey = null;
    private long tooltipStartTimeMs = 0;
    private boolean drewTooltip = false;

    /**
     * Returns elapsed ms within the current cycle for the given ticker key.
     * Resets start time when the key changes (e.g. user hovers a different ticker).
     *
     * @param key Unique identifier for this ticker instance (e.g. "sel-filter", "chart-0").
     * @param now Current time in ms (e.g. System.currentTimeMillis()).
     * @return Elapsed ms in [0, TICKER_CYCLE_MS) for scroll offset calculation.
     */
    public long getElapsedMs(String key, long now) {
        if (key == null) return 0;
        drewTicker = true;
        if (!key.equals(lastKey)) {
            lastKey = key;
            startTimeMs = now;
        }
        return (now - startTimeMs) % GSRRunHistoryParameters.TICKER_CYCLE_MS;
    }

    /**
     * Returns elapsed ms within the tooltip scroll cycle for the given key.
     * Resets when the key changes (e.g. user hovers a different bar). Used for chart bar hover tooltip.
     *
     * @param key Unique identifier for this tooltip (e.g. "tooltip-overtime-0", "tooltip-compare-3").
     * @param now Current time in ms (e.g. System.currentTimeMillis()).
     * @return Elapsed ms in [0, SCROLL_CYCLE_MS) for vertical scroll offset.
     */
    public long getTooltipElapsedMs(String key, long now) {
        if (key == null) return 0;
        drewTooltip = true;
        if (!key.equals(tooltipLastKey)) {
            tooltipLastKey = key;
            tooltipStartTimeMs = now;
        }
        return (now - tooltipStartTimeMs) % GSRTooltipParameters.SCROLL_CYCLE_MS;
    }

    /**
     * Returns unbounded elapsed ms for the given tooltip key. Resets only when the key changes.
     * Use for horizontal tickers that loop via modulo in the scroll offset, not in elapsed time.
     *
     * @param key Unique identifier for this tooltip (e.g. "tooltip-player-0-PlayerName").
     * @param now Current time in ms (e.g. System.currentTimeMillis()).
     * @return Elapsed ms since first activation (unbounded).
     */
    public long getTooltipElapsedMsUnbounded(String key, long now) {
        if (key == null) return 0;
        drewTooltip = true;
        if (!key.equals(tooltipLastKey)) {
            tooltipLastKey = key;
            tooltipStartTimeMs = now;
        }
        return now - tooltipStartTimeMs;
    }

    /**
     * Clears state when no ticker or tooltip was drawn this frame so the next animation starts from the beginning.
     */
    public void clearIfNotDrawn() {
        if (!drewTicker) lastKey = null;
        drewTicker = false;
        if (!drewTooltip) tooltipLastKey = null;
        drewTooltip = false;
    }
}
