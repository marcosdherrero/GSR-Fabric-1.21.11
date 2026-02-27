package net.berkle.groupspeedrun.timer.state;

import net.berkle.groupspeedrun.config.GSRConfigWorld;

/**
 * Explicit state machine for the GSR timer lifecycle.
 * States are derived from {@link GSRConfigWorld}; transitions occur in
 * {@link GSRRunLifecycle} and run action handlers.
 */
public enum GSRTimerState {

    /** Run created but not active. Awaiting movement or block break by group death participant. */
    PRIMED,

    /** Timer running. Transitions from Primed on first movement/block break or admin Start. */
    ACTIVE,

    /** Timer frozen by admin manual pause, world/server pause, or last player disconnect. */
    PAUSED,

    /** Dragon killed; run completed successfully. */
    COMPLETED,

    /** Run failed (group death participant died). */
    FAILED;

    /**
     * Derives the current timer state from world config.
     * Order matters: Completed and Failed are terminal and checked first.
     */
    public static GSRTimerState fromConfig(GSRConfigWorld config) {
        if (config == null) return PRIMED;
        if (config.isVictorious) return COMPLETED;
        if (config.isFailed) return FAILED;
        if (config.startTime <= 0) return PRIMED;
        if (config.isTimerFrozen) return PAUSED;
        return ACTIVE;
    }

    /** True if run has ended (victory or fail); timer is frozen at completion time. */
    public boolean isTerminal() {
        return this == COMPLETED || this == FAILED;
    }

    /** True if timer can be started (Primed) or resumed (Paused with frozenByServerStop). */
    public boolean canTransitionToActive() {
        return this == PRIMED || this == PAUSED;
    }
}
