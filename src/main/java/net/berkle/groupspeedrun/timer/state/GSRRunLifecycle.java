package net.berkle.groupspeedrun.timer.state;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.GSRStats;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.managers.GSRBroadcastManager;
import net.berkle.groupspeedrun.managers.GSRDataStore;
import net.berkle.groupspeedrun.managers.GSRWorldSnapshotManager;
import net.berkle.groupspeedrun.network.GSRSplitAchievedPayload;
import net.berkle.groupspeedrun.server.GSRConfigSync;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

import java.util.Collections;

/**
 * Run lifecycle: start, resume, pause, and reset. Handles timer state transitions,
 * player reset (teleport, clear inv, revoke advancements), and world snapshot restore.
 * Core state machine logic for the GSR timer.
 */
public final class GSRRunLifecycle {

    private GSRRunLifecycle() {}

    /** Starts the timer from now. Used by auto-start (first movement or block break) or admin Start. */
    public static void startTimerNow(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null) return;
        if (config.isVictorious || config.isFailed) return;
        config.startTime = System.currentTimeMillis();
        config.isTimerFrozen = false;
        config.manualPause = false;
        config.frozenTime = 0;
        config.lowestDifficultyOrdinal = -1;
        config.clearSplitsOnly();
        config.lastSplitTime = server.getOverworld().getTime();
        Difficulty prevDifficulty = server.getSaveProperties().getDifficulty();
        server.getSaveProperties().setDifficulty(Difficulty.HARD);
        config.save(server);
        GSRConfigSync.syncConfigWithAll(server);
        sendTimerStartEffect(server);
        String msg = "§6§l[GSR] Run started!";
        if (prevDifficulty != Difficulty.HARD) {
            msg += " §7(Difficulty set to Hard)";
        }
        Text message = Text.literal(msg);
        server.execute(() -> GSRBroadcastManager.broadcastToRunParticipants(server, message));
    }

    /**
     * Ensures run is armed (startTime = -1) when not started; primes fresh world for movement/block-break auto-start.
     */
    public static void primeRunIfArmed(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null) return;
        if (config.isVictorious || config.isFailed) return;
        if (config.startTime <= 0) {
            config.startTime = -1;
            config.save(server);
        }
    }

    /**
     * On player join: auto-resume only if the run was already started and frozen by server stop/last player leave.
     * If the run has not started yet (startTime <= 0), do nothing; it will auto-start on first movement or block break.
     */
    public static void tryAutoStartOrResumeOnJoin(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null) return;
        if (config.isVictorious || config.isFailed) return;
        if (config.startTime <= 0) return; // Run not started yet; wait for movement or block break
        if (config.isTimerFrozen && config.frozenByServerStop) {
            resumeTimer(server);
        }
    }

    /** When the last player disconnects, freeze the timer so it auto-resumes when someone rejoins. */
    public static void tryFreezeOnLastPlayerDisconnect(MinecraftServer server) {
        if (server.getPlayerManager().getCurrentPlayerCount() > 0) return;
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null) return;
        if (config.startTime <= 0 || config.isVictorious || config.isFailed) return;
        config.frozenTime = config.getElapsedTime();
        config.isTimerFrozen = true;
        config.frozenByServerStop = true;
        config.save(server);
    }

    /** Unfreeze the timer (e.g. after world was closed, last player left, or player moves/breaks block). */
    public static void resumeTimer(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null) return;
        config.manualPause = false;
        config.isTimerFrozen = false;
        config.frozenByServerStop = false;
        // Adjust startTime so elapsed stays at frozenTime; otherwise (now - startTime) would add the frozen duration
        if (config.frozenTime > 0) {
            config.startTime = System.currentTimeMillis() - config.frozenTime;
        }
        config.lastSplitTime = server.getOverworld().getTime();
        config.save(server);
        GSRConfigSync.syncConfigWithAll(server);
        sendTimerStartEffect(server);
        GSRBroadcastManager.broadcastToRunParticipants(server, Text.literal("§6§l[GSR] Run resumed!"));
    }

    /** Sends split-achieved payload to run participants so timer HUD shows split effect (sound + priority window). */
    private static void sendTimerStartEffect(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null) return;
        var payload = new GSRSplitAchievedPayload("Start", 0L);
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (!config.excludedFromRun.contains(p.getUuid())) {
                ServerPlayNetworking.send(p, payload);
            }
        }
    }

    /**
     * Hard reset: if the run was completed (victory/fail), save it to per-player folders,
     * then clear run data and stats. Teleport players to spawn, clear inventory, revoke advancements.
     */
    public static void resetRun(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null) return;
        if (config.isVictorious || config.isFailed) {
            GSRDataStore.saveCompletedRunToPlayerFolders(server);
        }
        config.resetRunData();
        GSRStats.reset();

        ServerWorld overworld = server.getOverworld();
        BlockPos spawnPos = overworld.getSpawnPoint().getPos();

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            player.stopRiding();
            player.changeGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.setHealth(player.getMaxHealth());
            player.getHungerManager().setFoodLevel(20);
            player.getHungerManager().setSaturationLevel(5.0f);
            player.setExperienceLevel(0);
            player.setExperiencePoints(0);
            player.clearStatusEffects();
            revokeAllAdvancements(player, server);
            player.teleport(
                    overworld,
                    spawnPos.getX() + 0.5,
                    spawnPos.getY(),
                    spawnPos.getZ() + 0.5,
                    Collections.emptySet(),
                    0.0f,
                    0.0f,
                    true
            );
        }
        GSRWorldSnapshotManager.setRestoreFromSnapshotOnNextLoad(server);
        config.save(server);
        GSRConfigSync.syncConfigWithAll(server);
        GSRBroadcastManager.broadcastToRunParticipants(server, Text.literal("§6§l[GSR] Run reset."));
    }

    private static void revokeAllAdvancements(ServerPlayerEntity player, MinecraftServer server) {
        for (AdvancementEntry advancement : server.getAdvancementLoader().getAdvancements()) {
            AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
            if (progress.isAnyObtained()) {
                for (String criterion : progress.getObtainedCriteria()) {
                    player.getAdvancementTracker().revokeCriterion(advancement, criterion);
                }
            }
        }
    }
}
