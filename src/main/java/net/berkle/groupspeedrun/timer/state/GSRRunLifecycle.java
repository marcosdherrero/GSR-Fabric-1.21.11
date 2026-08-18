package net.berkle.groupspeedrun.timer.state;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.GSRStats;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.managers.GSRBroadcastManager;
import net.berkle.groupspeedrun.managers.GSRDataStore;
import net.berkle.groupspeedrun.managers.GSRWorldSnapshotManager;
import net.berkle.groupspeedrun.network.GSRReloadWorldPayload;
import net.berkle.groupspeedrun.network.GSRSplitAchievedPayload;
import net.berkle.groupspeedrun.server.GSRConfigSync;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;

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
        if (!config.isRunNotStarted()) return;
        GSRWorldSnapshotManager.takeSnapshotIfNeeded(server);
        config.startTime = System.currentTimeMillis();
        config.isTimerFrozen = false;
        config.manualPause = false;
        config.frozenTime = 0;
        config.lowestDifficultyOrdinal = -1;
        config.clearSplitsOnly();
        config.lastSplitTime = server.overworld().getGameTime();
        Difficulty prevDifficulty = server.getWorldData().getDifficulty();
        server.getWorldData().setDifficulty(Difficulty.HARD);
        config.save(server);
        GSRConfigSync.syncConfigWithAll(server);
        sendTimerStartEffect(server);
        String msg = "§6§l[GSR] Run started!";
        if (prevDifficulty != Difficulty.HARD) {
            msg += " §7(Difficulty set to Hard)";
        }
        Component message = Component.literal(msg);
        server.execute(() -> GSRBroadcastManager.broadcastToRunParticipants(server, message));
    }

    /**
     * Ensures run is armed (startTime = -1) when not started. Does not write to disk:
     * saving a primed default would wipe a completed HUD if load has not finished.
     */
    public static void primeRunIfArmed(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null) return;
        if (config.isVictorious || config.isFailed || config.hasSplitTimes()) return;
        if (config.startTime <= 0) {
            config.startTime = -1;
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
        if (server.getPlayerCount() > 0) return;
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
        config.lastSplitTime = server.overworld().getGameTime();
        config.save(server);
        GSRConfigSync.syncConfigWithAll(server);
        sendTimerStartEffect(server);
        GSRBroadcastManager.broadcastToRunParticipants(server, Component.literal("§6§l[GSR] Run resumed!"));
    }

    /** Sends split-achieved payload to run participants so timer HUD shows split effect (sound + priority window). */
    private static void sendTimerStartEffect(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null) return;
        var payload = new GSRSplitAchievedPayload("Start", 0L);
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            if (!config.excludedFromRun.contains(p.getUUID())) {
                ServerPlayNetworking.send(p, payload);
            }
        }
    }

    /**
     * Hard reset: restore the original world snapshot (region/entities/poi/level.dat), then apply
     * GSR player state. Aborts clearly if no backup exists instead of only teleporting to spawn.
     */
    public static void resetRun(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null) return;
        if (!GSRWorldSnapshotManager.hasValidSnapshot(server)) {
            GSRBroadcastManager.broadcastToRunParticipants(server, Component.literal(
                    "§c§l[GSR] Reset aborted: no original-world backup. Rejoin this world (or start a new GSR world) so a snapshot can be saved."));
            return;
        }
        if (config.isVictorious || config.isFailed) {
            GSRDataStore.saveCompletedRunToPlayerFolders(server);
        }
        config.resetRunData();
        GSRStats.reset();
        applyPostResetPlayerState(server);
        GSRWorldSnapshotManager.setRestoreFromSnapshotOnNextLoad(server);
        config.save(server);
        GSRConfigSync.syncConfigWithAll(server);
        GSRBroadcastManager.broadcastToRunParticipants(server, Component.literal(
                "§6§l[GSR] Run reset. Reloading original world backup…"));
        String levelId = GSRWorldSnapshotManager.getLevelId(server);
        server.execute(() -> requestWorldReload(server, levelId));
    }

    /** Spawn, empty inventory, survival, revoke advancements. Applied in-memory; snapshot restore reloads files. */
    private static void applyPostResetPlayerState(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        BlockPos spawnPos = server.getRespawnData().pos();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.stopRiding();
            player.setGameMode(GameType.SURVIVAL);
            player.getInventory().clearContent();
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(5.0f);
            player.setExperienceLevels(0);
            player.totalExperience = 0;
            player.experienceProgress = 0.0f;
            player.removeAllEffects();
            revokeAllAdvancements(player, server);
            player.teleportTo(
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
    }

    private static void requestWorldReload(MinecraftServer server, String levelId) {
        if (!server.isDedicatedServer()) {
            var payload = new GSRReloadWorldPayload(levelId);
            for (ServerPlayer p : server.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(p, payload);
            }
        } else {
            GSRBroadcastManager.broadcastToRunParticipants(server, Component.literal(
                    "§e[GSR] Restart the server to finish restoring the original world."));
        }
    }

    private static void revokeAllAdvancements(ServerPlayer player, MinecraftServer server) {
        for (AdvancementHolder advancement : server.getAdvancements().getAllAdvancements()) {
            AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
            if (progress.hasProgress()) {
                for (String criterion : progress.getCompletedCriteria()) {
                    player.getAdvancements().revoke(advancement, criterion);
                }
            }
        }
    }
}
