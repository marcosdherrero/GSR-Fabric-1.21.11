package net.berkle.groupspeedrun;

import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.managers.GSRSplitManager;
import net.berkle.groupspeedrun.parameter.GSRServerParameters;
import net.berkle.groupspeedrun.server.GSRDeathHandler;
import net.berkle.groupspeedrun.server.GSRLocatorTickHandler;
import net.berkle.groupspeedrun.timer.GSRTimer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.Difficulty;

/**
 * Server tick and run lifecycle facade. Delegates to server handlers.
 */
public final class GSREvents {

    private GSREvents() {}

    /**
     * Called every server tick. If run is active and not frozen, check dimension splits and locator enter/fade.
     */
    public static void onTick(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null) return;
        if (config.isTimerFrozen || config.isVictorious || config.isFailed) return;
        if (config.startTime <= 0) return;

        var overworld = server.getOverworld();
        if (overworld != null) {
            Difficulty d = overworld.getDifficulty();
            if (d != null) {
                int ord = d.ordinal();
                config.lowestDifficultyOrdinal = config.lowestDifficultyOrdinal < 0 ? ord : Math.min(config.lowestDifficultyOrdinal, ord);
            }
        }

        if (server.getTicks() % GSRServerParameters.SPLIT_CHECK_INTERVAL == 0) {
            GSRSplitManager.checkSplits(server);
        }

        GSRLocatorTickHandler.checkLocatorEnterAndFade(server);
    }

    /** Starts the timer from now. Delegates to {@link GSRTimer}. */
    public static void startTimerNow(MinecraftServer server) {
        GSRMain.getTimer().start(server);
    }

    /** Unfreeze the timer. Delegates to {@link GSRTimer}. */
    public static void resumeTimer(MinecraftServer server) {
        GSRMain.getTimer().resume(server);
    }

    /** Hard reset: save completed run, clear data, teleport players. Delegates to {@link GSRTimer}. */
    public static void resetRun(MinecraftServer server) {
        GSRMain.getTimer().reset(server);
    }

    /** Handle group death: mark failed, spectator all, broadcast. Delegates to {@link GSRDeathHandler}. */
    public static void handlePlayerDeath(ServerPlayerEntity deadPlayer, MinecraftServer server) {
        GSRDeathHandler.handlePlayerDeath(deadPlayer, server);
    }
}
