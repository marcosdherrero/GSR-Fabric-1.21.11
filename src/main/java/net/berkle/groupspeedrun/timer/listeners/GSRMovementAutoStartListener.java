package net.berkle.groupspeedrun.timer.listeners;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.GSRStats;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.parameter.GSRTrackerParameters;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

/**
 * Event handler for movement-based auto-start and auto-resume triggers.
 * When run is Primed and a group death participant moves, starts the timer.
 * When run is Paused by server stop and player moves, resumes the timer.
 * Manual pause persists until admin resumes; movement does not resume.
 */
public final class GSRMovementAutoStartListener {

    private static final double JITTER = GSRTrackerParameters.JITTER;
    private static final double TELEPORT_THRESHOLD = GSRTrackerParameters.TELEPORT_THRESHOLD;

    private GSRMovementAutoStartListener() {}

    /**
     * Called each tick when player is in ServerLevel. Evaluates movement and triggers start/resume if appropriate.
     *
     * @param server server instance
     * @param player the player
     * @param x current X
     * @param y current Y
     * @param z current Z
     * @param prevX previous X
     * @param prevY previous Y
     * @param prevZ previous Z
     * @param armedTicksInWorld ticks spent in world while armed (for warmup)
     * @param baselineFromServerWorld true if baseline was set in ServerLevel
     * @return true if caller should increment armedTicksInWorld (for Primed state)
     */
    public static boolean onTick(MinecraftServer server, ServerPlayer player,
            double x, double y, double z, double prevX, double prevY, double prevZ,
            int armedTicksInWorld, boolean baselineFromServerWorld) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.isVictorious || config.isFailed) return false;

        if (config.isRunNotStarted()) {
            if (!config.autoStartEnabled) return true;
            if (config.isInGroupDeath(player.getUUID()) && server != null) {
                double dx = x - prevX;
                double dy = y - prevY;
                double dz = z - prevZ;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (dist > JITTER && dist < TELEPORT_THRESHOLD) {
                    GSRMain.getTimer().start(server);
                }
            }
            return true;
        }

        double dx = x - prevX;
        double dy = y - prevY;
        double dz = z - prevZ;
        double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (dist <= JITTER || dist >= TELEPORT_THRESHOLD) return config.startTime <= 0;

        if (config.isTimerFrozen) {
            // Only resume on movement when frozen by server stop. Manual pause persists until admin resumes.
            if (config.startTime > 0 && config.frozenByServerStop && baselineFromServerWorld && server != null) {
                GSRMain.getTimer().resume(server);
            }
            return false;
        }

        // Active run: track distance for stats by travel type
        String travelType = resolveTravelType(player);
        GSRStats.addDistanceMoved(player.getUUID(), travelType, (float) dist);
        return false;
    }

    /** Returns true if player is in a ServerLevel (for movement evaluation). */
    public static boolean isInServerWorld(ServerPlayer player) {
        return player.level() instanceof ServerLevel;
    }

    /** Resolves travel type for distance stats. Keys: walk, sprint, swim, fly, climb, fall. */
    private static String resolveTravelType(ServerPlayer player) {
        if (player.getAbilities().flying) return "fly";
        if (player.isSwimming()) return "swim";
        if (player.isClimbing()) return "climb";
        if (!player.isOnGround()) {
            if (player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA)) return "fly";
            if (player.getVelocity().y < -0.08) return "fall";
        }
        if (player.isSprinting()) return "sprint";
        return "walk";
    }
}
