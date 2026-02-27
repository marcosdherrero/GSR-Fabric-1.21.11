package net.berkle.groupspeedrun.server;

import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.managers.GSRProfileManager;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Determines the designated config source for world-level settings that depend on a player.
 * Uses default states when no player; when players are in the run, uses the highest-level
 * admin who logged in first.
 */
public final class GSRDesignatedConfigSource {

    private GSRDesignatedConfigSource() {}

    /**
     * Returns the designated config player: highest permission-level admin who joined first.
     * Returns null when no admin is online.
     */
    public static ServerPlayerEntity getDesignatedConfigPlayer(MinecraftServer server) {
        if (server == null) return null;
        ServerPlayerEntity best = null;
        int bestLevel = -1;
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (player.getEntityWorld() instanceof ServerWorld sw) {
                ServerCommandSource src = server.getCommandSource().withEntity(player).withWorld(sw);
                if (!CommandManager.ADMINS_CHECK.allows(src.getPermissions())) continue;
                int level = getPermissionLevel(server, player);
                if (level > bestLevel) {
                    bestLevel = level;
                    best = player;
                }
            }
        }
        return best;
    }

    /** Returns effective allowNewWorldBeforeRunEnd: designated admin's value or default (false) when none. */
    public static boolean getEffectiveAllowNewWorldBeforeRunEnd(MinecraftServer server) {
        ServerPlayerEntity designated = getDesignatedConfigPlayer(server);
        if (designated == null) return false;
        GSRConfigPlayer pc = GSRProfileManager.getPlayerConfig(designated);
        return pc != null && pc.allowNewWorldBeforeRunEnd;
    }

    /** Returns true if the given player is the designated config source. */
    public static boolean isDesignatedConfigPlayer(MinecraftServer server, ServerPlayerEntity player) {
        if (server == null || player == null) return false;
        return player.equals(getDesignatedConfigPlayer(server));
    }

    /** Returns numeric op level (2–4) from op list, or 0 if not op. */
    private static int getPermissionLevel(MinecraftServer server, ServerPlayerEntity player) {
        PlayerConfigEntry entry = player.getPlayerConfigEntry();
        OperatorEntry op = server.getPlayerManager().getOpList().get(entry);
        if (op == null) return 0;
        LeveledPermissionPredicate levelPred = op.getLevel();
        if (levelPred == null) return 0;
        PermissionLevel pl = levelPred.getLevel();
        return pl != null ? pl.getLevel() : 0;
    }
}
