package net.berkle.groupspeedrun.server;

import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.managers.GSRProfileManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.ServerOpListEntry;

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
    public static ServerPlayer getDesignatedConfigPlayer(MinecraftServer server) {
        if (server == null) return null;
        ServerPlayer best = null;
        int bestLevel = -1;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player.level() instanceof ServerLevel sw) {
                CommandSourceStack src = server.createCommandSourceStack().withEntity(player).withLevel(sw);
                if (!Commands.LEVEL_ADMINS.check(src.permissions())) continue;
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
        ServerPlayer designated = getDesignatedConfigPlayer(server);
        if (designated == null) return false;
        GSRConfigPlayer pc = GSRProfileManager.getPlayerConfig(designated);
        return pc != null && pc.allowNewWorldBeforeRunEnd;
    }

    /** Returns true if the given player is the designated config source. */
    public static boolean isDesignatedConfigPlayer(MinecraftServer server, ServerPlayer player) {
        if (server == null || player == null) return false;
        return player.equals(getDesignatedConfigPlayer(server));
    }

    /** Returns numeric op level (2–4) from op list, or 0 if not op. */
    private static int getPermissionLevel(MinecraftServer server, ServerPlayer player) {
        ServerOpListEntry op = server.getPlayerList().getOps().get(player.nameAndId());
        if (op == null) return 0;
        return op.permissions().level().id();
    }
}
