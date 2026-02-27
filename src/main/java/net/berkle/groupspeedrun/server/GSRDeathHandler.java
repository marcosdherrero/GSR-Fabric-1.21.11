package net.berkle.groupspeedrun.server;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.managers.GSRBroadcastManager;
import net.berkle.groupspeedrun.managers.GSRDataStore;
import net.berkle.groupspeedrun.managers.GSRRunSyncManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;

/**
 * Handles player death when group death is enabled: mark run failed, store death info,
 * put all players in spectator, broadcast run-end, and record to database.
 */
public final class GSRDeathHandler {

    private GSRDeathHandler() {}

    public static void handlePlayerDeath(ServerPlayerEntity deadPlayer, MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.startTime <= 0 || config.isVictorious || config.isFailed) return;
        if (!config.groupDeathEnabled) return;
        if (!config.isInGroupDeath(deadPlayer.getUuid())) return;

        long elapsed = config.getElapsedTime();
        config.isFailed = true;
        config.isTimerFrozen = true;
        config.frozenTime = elapsed;
        config.lastSplitTime = server.getOverworld().getTime();
        config.failedByPlayerName = deadPlayer.getName().getString();
        config.failedByDeathMessage = deadPlayer.getDamageTracker().getDeathMessage().getString();
        config.runParticipantCount = (int) server.getPlayerManager().getPlayerList().stream()
                .filter(p -> !config.excludedFromRun.contains(p.getUuid())).count();
        config.save(server);
        GSRConfigSync.syncConfigWithAll(server);

        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            p.changeGameMode(GameMode.SPECTATOR);
        }
        var state = GSRDataStore.recordCurrentRun(server);
        if (state != null) {
            GSRRunSyncManager.addServerRunId(state.record().runId());
            GSRBroadcastManager.broadcastRunEnd(server, state);
        }
    }
}
