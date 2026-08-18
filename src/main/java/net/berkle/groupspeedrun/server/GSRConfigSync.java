package net.berkle.groupspeedrun.server;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.config.GSRConfigPayload;
import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.managers.GSRProfileManager;
import net.berkle.groupspeedrun.parameter.GSRPlayerConfigParameters;
import net.berkle.groupspeedrun.parameter.GSRWorldConfigParameters;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

/**
 * Server-side config sync: send world run state and per-player HUD config to clients.
 */
@SuppressWarnings("null")
public final class GSRConfigSync {

    private GSRConfigSync() {}

    public static void syncConfigWithPlayer(ServerPlayer player) {
        if (player == null) return;
        CompoundTag nbt = new CompoundTag();
        if (GSRMain.CONFIG != null) GSRMain.CONFIG.writeNbt(nbt);
        var world = player.level();
        MinecraftServer server = world instanceof ServerLevel sw ? sw.getServer() : null;
        addEffectiveWorldConfig(server, nbt);
        GSRConfigPlayer playerConfig = GSRProfileManager.getPlayerConfig(player);
        if (playerConfig != null) playerConfig.writeNbt(nbt);
        addPermissionFlags(player, nbt);
        ServerPlayNetworking.send(player, new GSRConfigPayload(nbt));
    }

    public static void syncConfigWithAll(MinecraftServer server) {
        if (server == null || GSRMain.CONFIG == null) return;
        CompoundTag worldBase = new CompoundTag();
        GSRMain.CONFIG.writeNbt(worldBase);
        addEffectiveWorldConfig(server, worldBase);
        for (ServerPlayer player : PlayerLookup.all(server)) {
            CompoundTag playerNbt = worldBase.copy();
            GSRConfigPlayer pc = GSRProfileManager.getPlayerConfig(player);
            if (pc != null) pc.writeNbt(playerNbt);
            addPermissionFlags(player, playerNbt);
            ServerPlayNetworking.send(player, new GSRConfigPayload(playerNbt));
        }
    }

    /** Adds effective world config values (designated admin source) to NBT; not persisted in writeNbt. */
    private static void addEffectiveWorldConfig(MinecraftServer server, CompoundTag nbt) {
        if (server == null || nbt == null) return;
        nbt.putBoolean(GSRWorldConfigParameters.K_EFFECTIVE_ALLOW_NEW_WORLD_BEFORE_RUN_END,
                GSRDesignatedConfigSource.getEffectiveAllowNewWorldBeforeRunEnd(server));
    }

    /** Adds server-computed permission flags for client UI (e.g. gray out admin-only buttons). */
    private static void addPermissionFlags(ServerPlayer player, CompoundTag nbt) {
        var world = player.level();
        MinecraftServer server = world instanceof ServerLevel sw ? sw.getServer() : null;
        CommandSourceStack src = server != null
                ? server.createCommandSourceStack().withEntity(player).withLevel((ServerLevel) world)
                : null;
        boolean canUseAdmin = src != null && Commands.LEVEL_ADMINS.check(src.permissions());
        nbt.putBoolean(GSRPlayerConfigParameters.K_CAN_USE_ADMIN, canUseAdmin);
    }
}
