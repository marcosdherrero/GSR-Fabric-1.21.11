package net.berkle.groupspeedrun.timer.listeners;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.GSRStats;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Event handler for block-break auto-start and auto-resume triggers.
 * When run is Primed and a group death participant breaks a block, starts the timer.
 * When run is Paused by server stop and player breaks block, resumes the timer.
 * Manual pause persists until admin resumes; block break does not resume.
 */
public final class GSRBlockBreakAutoStartListener {

    private GSRBlockBreakAutoStartListener() {}

    /**
     * Called after a block is successfully broken. Triggers start or resume if appropriate.
     *
     * @param server server instance
     * @param player the player who broke the block
     * @param block the block that was broken (may be null)
     * @return true if handled (start or resume); false if run is active and block should be tracked for stats
     */
    public static boolean onBlockBroken(MinecraftServer server, ServerPlayerEntity player, Block block) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.isVictorious || config.isFailed) return false;

        if (config.startTime <= 0) {
            if (!config.autoStartEnabled) return false;
            if (config.isInGroupDeath(player.getUuid())) {
                GSRMain.getTimer().start(server);
                return true;
            }
            return false;
        }

        if (config.isTimerFrozen && config.frozenByServerStop) {
            GSRMain.getTimer().resume(server);
            return true;
        }

        String blockId = block != null ? block.getRegistryEntry().getKey().map(k -> k.getValue().toString()).orElse(null) : null;
        GSRStats.addBlockBroken(player.getUuid(), blockId);
        return false;
    }
}
