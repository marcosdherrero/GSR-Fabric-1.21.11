package net.berkle.groupspeedrun.gui;

/**
 * Model for GSRLocatorsScreen. Tracks click cooldown for toggle rows.
 */
public final class GSRLocatorsScreenModel {

    /** Last time a click was handled (ms). Used for anti-spam cooldown. */
    public long lastClickHandledTimeMs;
}
