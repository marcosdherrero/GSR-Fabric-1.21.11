package net.berkle.groupspeedrun.timer;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.timer.state.GSRRunLifecycle;
import net.berkle.groupspeedrun.timer.state.GSRTimerState;
import net.minecraft.server.MinecraftServer;

/**
 * Root object for the GSR timer. Replaces deprecated static utilities with an OOP-first design.
 * Owns state transitions (start, pause, resume, reset) and delegates to {@link GSRRunLifecycle}.
 */
public final class GSRTimer {

    private static final GSRTimer INSTANCE = new GSRTimer();

    private GSRTimer() {}

    /** Returns the singleton timer instance. */
    public static GSRTimer getInstance() {
        return INSTANCE;
    }

    /** Starts the timer from now. Used by auto-start or admin Start. */
    public void start(MinecraftServer server) {
        GSRRunLifecycle.startTimerNow(server);
    }

    /** Unfreezes the timer. Used when resuming from server stop or admin Resume. */
    public void resume(MinecraftServer server) {
        GSRRunLifecycle.resumeTimer(server);
    }

    /** Hard reset: save completed run if any, clear data, teleport players. */
    public void reset(MinecraftServer server) {
        GSRRunLifecycle.resetRun(server);
    }

    /** Ensures run is armed for auto-start. Called on player join. */
    public void primeRunIfArmed(MinecraftServer server) {
        GSRRunLifecycle.primeRunIfArmed(server);
    }

    /** Auto-resume on join if frozen by server stop. */
    public void tryAutoStartOrResumeOnJoin(MinecraftServer server) {
        GSRRunLifecycle.tryAutoStartOrResumeOnJoin(server);
    }

    /** Freeze timer when last player disconnects so it auto-resumes on rejoin. */
    public void tryFreezeOnLastPlayerDisconnect(MinecraftServer server) {
        GSRRunLifecycle.tryFreezeOnLastPlayerDisconnect(server);
    }

    /** Returns the current timer state derived from world config. */
    public GSRTimerState getState() {
        return GSRTimerState.fromConfig(GSRMain.CONFIG);
    }
}
