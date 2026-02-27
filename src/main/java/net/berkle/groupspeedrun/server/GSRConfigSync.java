package net.berkle.groupspeedrun.server;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.config.GSRConfigPayload;
import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.managers.GSRProfileManager;
import net.berkle.groupspeedrun.parameter.GSRPlayerConfigParameters;
import net.berkle.groupspeedrun.parameter.GSRWorldConfigParameters;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

/**
 * Server-side config sync: send world run state and per-player HUD config to clients.
 */
@SuppressWarnings("null")
public final class GSRConfigSync {

    private GSRConfigSync() {}

    public static void syncConfigWithPlayer(ServerPlayerEntity player) {
        if (player == null) return;
        NbtCompound nbt = new NbtCompound();
        if (GSRMain.CONFIG != null) GSRMain.CONFIG.writeNbt(nbt);
        var world = player.getEntityWorld();
        MinecraftServer server = world instanceof ServerWorld sw ? sw.getServer() : null;
        addEffectiveWorldConfig(server, nbt);
        GSRConfigPlayer playerConfig = GSRProfileManager.getPlayerConfig(player);
        if (playerConfig != null) playerConfig.writeNbt(nbt);
        addPermissionFlags(player, nbt);
        ServerPlayNetworking.send(player, new GSRConfigPayload(nbt));
    }

    public static void syncConfigWithAll(MinecraftServer server) {
        if (server == null || GSRMain.CONFIG == null) return;
        NbtCompound worldBase = new NbtCompound();
        GSRMain.CONFIG.writeNbt(worldBase);
        addEffectiveWorldConfig(server, worldBase);
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            NbtCompound playerNbt = worldBase.copy();
            GSRConfigPlayer pc = GSRProfileManager.getPlayerConfig(player);
            if (pc != null) pc.writeNbt(playerNbt);
            addPermissionFlags(player, playerNbt);
            ServerPlayNetworking.send(player, new GSRConfigPayload(playerNbt));
        }
    }

    /** Adds effective world config values (designated admin source) to NBT; not persisted in writeNbt. */
    private static void addEffectiveWorldConfig(MinecraftServer server, NbtCompound nbt) {
        if (server == null || nbt == null) return;
        nbt.putBoolean(GSRWorldConfigParameters.K_EFFECTIVE_ALLOW_NEW_WORLD_BEFORE_RUN_END,
                GSRDesignatedConfigSource.getEffectiveAllowNewWorldBeforeRunEnd(server));
    }

    /** Adds server-computed permission flags for client UI (e.g. gray out admin-only buttons). */
    private static void addPermissionFlags(ServerPlayerEntity player, NbtCompound nbt) {
        var world = player.getEntityWorld();
        MinecraftServer server = world instanceof ServerWorld sw ? sw.getServer() : null;
        ServerCommandSource src = server != null
                ? server.getCommandSource().withEntity(player).withWorld((ServerWorld) world)
                : null;
        boolean canUseAdmin = src != null && CommandManager.ADMINS_CHECK.allows(src.getPermissions());
        nbt.putBoolean(GSRPlayerConfigParameters.K_CAN_USE_ADMIN, canUseAdmin);
    }
}
