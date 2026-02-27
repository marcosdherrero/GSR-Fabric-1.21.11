package net.berkle.groupspeedrun.gui;

import net.berkle.groupspeedrun.parameter.GSRTooltipParameters;

/**
 * Static state for global tooltip scroll timing. Used by the DrawContext mixin when
 * rendering tooltips in options menus, config screens, and buttons. Resets scroll
 * when tooltip content changes (different widget/element hovered).
 */
public final class GSRGlobalTooltipState {

    private static String lastKey = null;
    private static long startTimeMs = 0;

    private GSRGlobalTooltipState() {}

    /**
     * Returns elapsed ms within the tooltip scroll cycle for the given key.
     * Resets when the key changes (e.g. user hovers a different element).
     *
     * @param key Unique identifier for this tooltip (e.g. content hash).
     * @param now Current time in ms (e.g. System.currentTimeMillis()).
     * @return Elapsed ms in [0, SCROLL_CYCLE_MS) for vertical scroll offset.
     */
    public static long getTooltipElapsedMs(String key, long now) {
        if (key == null) return 0;
        if (!key.equals(lastKey)) {
            lastKey = key;
            startTimeMs = now;
        }
        return (now - startTimeMs) % GSRTooltipParameters.SCROLL_CYCLE_MS;
    }
}
