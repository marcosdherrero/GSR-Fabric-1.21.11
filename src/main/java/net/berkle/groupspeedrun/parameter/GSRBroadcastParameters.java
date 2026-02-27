package net.berkle.groupspeedrun.parameter;

/**
 * Parameters for run-end broadcast messages: separator bar length and stat comparison epsilon.
 * Also run start/split chat messages and victory celebration duration.
 * Used by {@link net.berkle.groupspeedrun.managers.GSRBroadcastManager} and celebration handlers.
 */
public final class GSRBroadcastParameters {

    private GSRBroadcastParameters() {}

    /** Length of the success/fail separator bar (character count). */
    public static final int BAR_LENGTH = 28;
    /** Victory quip shown at start of run-end broadcast. */
    public static final String VICTORY_QUIP = "§aVictory! Great run!";
    /** Fail quip shown at start of run-end broadcast. */
    public static final String FAIL_QUIP = "§cBetter luck next time!";
    /** Epsilon for floating-point stat comparisons (e.g. leaderboard ties). */
    public static final double STAT_EPSILON = 0.001;
    /** Threshold above which numbers use scientific notation for run info and broadcasts. */
    public static final int NUMBER_SCIENTIFIC_THRESHOLD = 10_000;
    /** Victory celebration duration in ticks (5 seconds at 20 tps). */
    public static final int VICTORY_CELEBRATION_TICKS = 100;
    /** Interval between firework particle bursts during victory (ticks). */
    public static final int VICTORY_FIREWORK_INTERVAL_TICKS = 8;
}
