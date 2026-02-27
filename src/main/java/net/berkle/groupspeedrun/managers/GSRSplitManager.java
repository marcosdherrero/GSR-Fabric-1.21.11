package net.berkle.groupspeedrun.managers;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.data.GSRRunPlayerSnapshot;
import net.berkle.groupspeedrun.network.GSRSplitAchievedPayload;
import net.berkle.groupspeedrun.network.GSRVictoryCelebrationPayload;
import net.berkle.groupspeedrun.parameter.GSRBroadcastParameters;
import net.berkle.groupspeedrun.util.GSRFormatUtil;

/**
 * GSRSplitManager: Dimension-based split detection and dragon completion.
 * Phase 1: checkSplits (Nether, Bastion, Fortress, End), completeDragon.
 * Broadcasts chat messages and sends client payloads for split/victory feedback.
 */
public final class GSRSplitManager {

    private GSRSplitManager() {}

    /**
     * Called from tick. For each online player, set dimension/structure splits if not yet set.
     */
    public static void checkSplits(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.startTime <= 0 || config.isTimerFrozen) return;
        if (config.isVictorious || config.isFailed) return;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (config.excludedFromRun.contains(player.getUuid())) continue;
            ServerWorld world = (ServerWorld) player.getEntityWorld();
            if (world.getRegistryKey() == World.NETHER) {
                if (config.timeNether <= 0) {
                    setNetherSplit(server);
                    return;
                }
                if (config.timeBastion <= 0 && GSRLocateHelper.isInStructure(world, player.getBlockPos(), "bastion")) {
                    setBastionSplit(server);
                    return;
                }
                if (config.timeFortress <= 0 && GSRLocateHelper.isInStructure(world, player.getBlockPos(), "fortress")) {
                    setFortressSplit(server);
                    return;
                }
            }
            if (world.getRegistryKey() == World.END && config.timeEnd <= 0) {
                setEndSplit(server);
                return;
            }
            if (world.getRegistryKey() == World.OVERWORLD && config.timeFirstOverworldReturnAfterNether <= 0
                    && config.timeFortress > 0 && config.timeBastion > 0) {
                setOverworldReturnAfterNether(server);
                return;
            }
        }
    }

    private static void setOverworldReturnAfterNether(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.timeFirstOverworldReturnAfterNether > 0) return;
        config.timeFirstOverworldReturnAfterNether = config.getElapsedTime();
        GSRMain.CONFIG.save(server);
    }

    private static void onSplitAchieved(MinecraftServer server, String splitName, long timeMs) {
        GSRBroadcastManager.broadcastToRunParticipants(server, Text.literal("§b" + splitName + " §7split achieved! §f" + GSRFormatUtil.formatTime(timeMs)));
        var payload = new GSRSplitAchievedPayload(splitName, timeMs);
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config != null) {
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                if (!config.excludedFromRun.contains(p.getUuid())) {
                    ServerPlayNetworking.send(p, payload);
                }
            }
        }
    }

    private static void setNetherSplit(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.timeNether > 0) return;
        long elapsed = config.getElapsedTime();
        config.timeNether = elapsed;
        config.lastSplitTime = server.getOverworld().getTime();
        GSRMain.CONFIG.save(server);
        onSplitAchieved(server, "Nether", elapsed);
    }

    private static void setEndSplit(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.timeEnd > 0) return;
        long elapsed = config.getElapsedTime();
        config.timeEnd = elapsed;
        config.lastSplitTime = server.getOverworld().getTime();
        GSRMain.CONFIG.save(server);
        onSplitAchieved(server, "End", elapsed);
    }

    private static void setBastionSplit(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.timeBastion > 0) return;
        long elapsed = config.getElapsedTime();
        config.timeBastion = elapsed;
        config.lastSplitTime = server.getOverworld().getTime();
        GSRMain.CONFIG.save(server);
        onSplitAchieved(server, "Bastion", elapsed);
    }

    private static void setFortressSplit(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.timeFortress > 0) return;
        long elapsed = config.getElapsedTime();
        config.timeFortress = elapsed;
        config.lastSplitTime = server.getOverworld().getTime();
        GSRMain.CONFIG.save(server);
        onSplitAchieved(server, "Fortress", elapsed);
    }

    /**
     * Called when dragon is killed. Sets timeDragon, marks victorious, freezes timer,
     * sets runParticipantCount, broadcasts run-end summary, and records run to database.
     * Sends Dragon split achieved message and victory celebration payload to run participants.
     */
    public static void completeDragon(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.startTime <= 0 || config.isVictorious || config.isFailed) return;
        long elapsed = config.getElapsedTime();
        config.timeDragon = elapsed;
        config.isVictorious = true;
        config.isTimerFrozen = true;
        config.frozenTime = elapsed;
        config.runParticipantCount = (int) server.getPlayerManager().getPlayerList().stream()
                .filter(p -> !config.excludedFromRun.contains(p.getUuid())).count();
        config.lastSplitTime = server.getOverworld().getTime();
        GSRMain.CONFIG.save(server);
        onSplitAchieved(server, "Dragon", elapsed);
        var state = GSRDataStore.recordCurrentRun(server);
        if (state != null) {
            GSRRunSyncManager.addServerRunId(state.record().runId());
            GSRRunPlayerSnapshot dragon = GSRBroadcastManager.getDragonWarriorSnapshot(state.snapshots());
            if (dragon != null && dragon.dragonDamage() > GSRBroadcastParameters.STAT_EPSILON) {
                config.dragonWarriorName = dragon.playerName() != null ? dragon.playerName() : "";
                config.dragonWarriorDamage = dragon.dragonDamage();
                config.save(server);
            }
            GSRBroadcastManager.broadcastRunEnd(server, state);
            var victoryPayload = new GSRVictoryCelebrationPayload();
            for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                if (!config.excludedFromRun.contains(p.getUuid())) {
                    ServerPlayNetworking.send(p, victoryPayload);
                }
            }
        }
    }
}
