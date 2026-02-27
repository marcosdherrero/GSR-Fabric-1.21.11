package net.berkle.groupspeedrun.gui.preferences;

/**
 * Model for GSR Preferences screen. Tracks which dropdown is open and pending selection.
 * Config values are read from {@link net.berkle.groupspeedrun.GSRClient} and applied on confirm.
 */
public final class GSRPreferencesScreenModel {

    /** No dropdown open. */
    public static final int DROPDOWN_NONE = 0;

    /** Which dropdown is open (1..N). 0 = none. */
    public int openDropdownId;

    /** Pending selection index for the open dropdown. */
    public int pendingIndex;

    /** Scroll offset for the open dropdown list (pixels). */
    public int dropdownScroll;

    /** Scroll offset for the main content list (pixels). */
    public int contentScroll;

    /** Last time a click was handled (ms). Used for anti-spam cooldown. */
    public long lastClickHandledTimeMs;
    /** Last hovered element: [rowType, rowId]. Used to require stable hover before accepting click. */
    public int[] lastHoveredElement;
    /** When we started hovering over lastHoveredElement (ms). */
    public long lastHoveredTimeMs;
}
