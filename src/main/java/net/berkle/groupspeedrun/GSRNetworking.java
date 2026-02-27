package net.berkle.groupspeedrun;

// Fabric: server-side networking
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

// Minecraft: NBT, commands, server, world
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

// GSR: config, managers, network payloads, parameters, server
import net.berkle.groupspeedrun.config.GSRConfigPayload;
import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.managers.GSRLocateHelper;
import net.berkle.groupspeedrun.managers.GSRLocatorGate;
import net.berkle.groupspeedrun.managers.GSRProfileManager;
import net.berkle.groupspeedrun.managers.GSRRunSyncManager;
import net.berkle.groupspeedrun.network.GSRLocatorActionPayload;
import net.berkle.groupspeedrun.network.GSRScreenTimePayload;
import net.berkle.groupspeedrun.network.GSROpenScreenPayload;
import net.berkle.groupspeedrun.network.GSRPlayerListPayload;
import net.berkle.groupspeedrun.network.GSRRunActionPayload;
import net.berkle.groupspeedrun.network.GSRRunDataPayload;
import net.berkle.groupspeedrun.network.GSRRunIdsPayload;
import net.berkle.groupspeedrun.network.GSRRunManagerRequestPayload;
import net.berkle.groupspeedrun.network.GSRRunManagerUpdatePayload;
import net.berkle.groupspeedrun.network.GSRRunRequestPayload;
import net.berkle.groupspeedrun.network.GSRWorldConfigPayload;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;
import net.berkle.groupspeedrun.parameter.GSRWorldConfigParameters;
import net.berkle.groupspeedrun.server.GSRConfigSync;
import net.berkle.groupspeedrun.server.GSRDesignatedConfigSource;
import net.berkle.groupspeedrun.server.GSRRunManagerNbt;

/**
 * GSRNetworking: S2C sync of world run state and per-player HUD config.
 */
@SuppressWarnings("null")
public final class GSRNetworking {

    private GSRNetworking() {}

    /** Sync world + player config to a single player. Delegates to {@link GSRConfigSync}. */
    public static void syncConfigWithPlayer(ServerPlayerEntity player) {
        GSRConfigSync.syncConfigWithPlayer(player);
    }

    /** No-op: group death uses exclusion list; everyone is in by default. Kept for API compatibility. */
    public static void addJoinerToGroupDeath(net.minecraft.server.MinecraftServer server, ServerPlayerEntity joiner) {}

    /** Sync world + player config to all players. Delegates to {@link GSRConfigSync}. */
    public static void syncConfigWithAll(net.minecraft.server.MinecraftServer server) {
        GSRConfigSync.syncConfigWithAll(server);
    }

    /** Server: receive C2S world config from host (antiCheatEnabled). Only designated config player can change. */
    public static void registerWorldConfigReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(GSRWorldConfigPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                ServerCommandSource src = context.server().getCommandSource().withEntity(player).withWorld((ServerWorld) player.getEntityWorld());
                if (!CommandManager.ADMINS_CHECK.allows(src.getPermissions())) return;
                if (!GSRDesignatedConfigSource.isDesignatedConfigPlayer(context.server(), player)) return;
                GSRConfigWorld config = GSRMain.CONFIG;
                if (config == null) return;
                payload.nbt().getBoolean(GSRWorldConfigPayload.KEY_ANTI_CHEAT_ENABLED).ifPresent(v -> config.antiCheatEnabled = v);
                payload.nbt().getBoolean(GSRWorldConfigParameters.K_AUTO_START_ENABLED).ifPresent(v -> config.autoStartEnabled = v);
                payload.nbt().getInt(GSRWorldConfigParameters.K_LOCATOR_NON_ADMIN_MODE).ifPresent(v -> config.locatorNonAdminMode = Math.max(0, Math.min(2, v)));
                config.save(context.server());
                GSRConfigSync.syncConfigWithAll(context.server());
            });
        });
    }

    /** Server: receive C2S player config from client (e.g. Mod Menu save); persist by UUID. */
    public static void registerC2SReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(GSRConfigPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                GSRConfigPlayer config = GSRProfileManager.getPlayerConfig(player);
                config.readNbt(payload.nbt());
                GSRProfileManager.updatePlayerSettings(player, config);
                GSRProfileManager.save(context.server());
            });
        });
    }

    /** Tell the client to open a GSR screen (config or controls). Call from /gsr config or /gsr controls. */
    public static void sendOpenScreen(ServerPlayerEntity player, byte screenType) {
        if (player == null) return;
        ServerPlayNetworking.send(player, new GSROpenScreenPayload(screenType));
    }

    /** Server: receive C2S run action from Controls GUI; run same logic as /gsr start|pause|resume|reset. */
    public static void registerRunActionReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(GSRRunActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                var config = GSRMain.CONFIG;
                if (config == null) return;
                byte action = payload.action();
                ServerCommandSource src = context.server().getCommandSource().withEntity(player).withWorld((ServerWorld) player.getEntityWorld());
                switch (action) {
                    case GSRRunActionPayload.ACTION_START -> {
                        if (!CommandManager.ADMINS_CHECK.allows(src.getPermissions())) return;
                        if (config.startTime > 0 || config.isVictorious || config.isFailed) return;
                        GSREvents.startTimerNow(context.server());
                        GSRConfigSync.syncConfigWithAll(context.server());
                    }
                    case GSRRunActionPayload.ACTION_PAUSE -> {
                        if (!CommandManager.ADMINS_CHECK.allows(src.getPermissions())) return;
                        GSRMain.frozenByClientPause = false;
                        config.frozenByServerStop = false;
                        config.manualPause = true;
                        config.frozenTime = config.getElapsedTime();
                        config.isTimerFrozen = true;
                        if (config.antiCheatEnabled && config.startTime > 0 && !config.isVictorious && !config.isFailed) {
                            config.locatorDeranked = true;
                        }
                        config.save(context.server());
                        GSRConfigSync.syncConfigWithAll(context.server());
                    }
                    case GSRRunActionPayload.ACTION_RESUME -> {
                        if (!CommandManager.ADMINS_CHECK.allows(src.getPermissions())) return;
                        if (config.isVictorious || config.isFailed) return;
                        config.manualPause = false;
                        config.isTimerFrozen = false;
                        if (config.antiCheatEnabled && config.startTime > 0 && !config.isVictorious && !config.isFailed) {
                            config.locatorDeranked = true;
                        }
                        config.save(context.server());
                        GSRConfigSync.syncConfigWithAll(context.server());
                    }
                    case GSRRunActionPayload.ACTION_CLIENT_PAUSE -> {
                        if (config.startTime <= 0 || config.isVictorious || config.isFailed) return;
                        // Only set frozenByClientPause when we actually freeze; if already manually paused, preserve that state
                        if (!config.isTimerFrozen) {
                            GSRMain.frozenByClientPause = true;
                            config.frozenTime = config.getElapsedTime();
                            config.isTimerFrozen = true;
                        }
                        config.save(context.server());
                        GSRConfigSync.syncConfigWithAll(context.server());
                    }
                    case GSRRunActionPayload.ACTION_CLIENT_RESUME -> {
                        if (!GSRMain.frozenByClientPause) return;
                        GSRMain.frozenByClientPause = false;
                        // Adjust startTime so elapsed stays at frozenTime; otherwise (now - startTime) would add the frozen duration
                        if (config.frozenTime > 0) {
                            config.startTime = System.currentTimeMillis() - config.frozenTime;
                        }
                        config.isTimerFrozen = false;
                        config.save(context.server());
                        GSRConfigSync.syncConfigWithAll(context.server());
                    }
                    case GSRRunActionPayload.ACTION_RESET -> {
                        if (!CommandManager.ADMINS_CHECK.allows(src.getPermissions())) return;
                        GSREvents.resetRun(context.server());
                        GSRConfigSync.syncConfigWithAll(context.server());
                    }
                    default -> {}
                }
            });
        });
    }

    /** Server: receive C2S screen time from client for screens server cannot detect (player/creative inventory). */
    public static void registerScreenTimeReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(GSRScreenTimePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                GSRConfigWorld config = GSRMain.CONFIG;
                if (config == null || config.startTime <= 0 || config.isTimerFrozen || config.isVictorious || config.isFailed) return;
                String id = payload.screenTypeId();
                if (id == null || id.isEmpty()) return;
                if (!id.equals(GSRScreenTimePayload.PLAYER_INVENTORY) && !id.equals(GSRScreenTimePayload.CREATIVE_INVENTORY)) return;
                GSRStats.addScreenTime(player.getUuid(), id);
            });
        });
    }

    /** Server: on locator menu action (clear or toggle). Enforces time gate and admin checks, sets derank, syncs config. */
    public static void registerLocatorActionReceiver() {
        ServerPlayNetworking.registerGlobalReceiver(GSRLocatorActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                GSRConfigWorld config = GSRMain.CONFIG;
                if (config == null) return;
                var server = context.server();
                ServerCommandSource src = server.getCommandSource().withEntity(player).withWorld((ServerWorld) player.getEntityWorld());
                boolean isAdmin = GSRLocatorGate.isAdmin(src);
                byte action = payload.action();

                if (action == GSRLocatorActionPayload.ACTION_CLEAR) {
                    if (config.startTime <= 0) return;
                    config.fortressLocated = false;
                    config.bastionLocated = false;
                    config.strongholdLocated = false;
                    config.shipLocated = false;
                    config.fortressX = 0;
                    config.fortressZ = 0;
                    config.bastionX = 0;
                    config.bastionZ = 0;
                    config.strongholdX = 0;
                    config.strongholdZ = 0;
                    config.shipX = 0;
                    config.shipY = 0;
                    config.shipZ = 0;
                    config.locatorFadeStartTime = 0;
                    config.locatorFadeType = "";
                    for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
                        GSRConfigPlayer pc = GSRProfileManager.getPlayerConfig(p);
                        if (pc != null) {
                            pc.fortressLocatorOn = false;
                            pc.bastionLocatorOn = false;
                            pc.strongholdLocatorOn = false;
                            pc.shipLocatorOn = false;
                        }
                    }
                    config.locatorDeranked = true;
                    config.save(server);
                    GSRProfileManager.save(server);
                    GSRConfigSync.syncConfigWithAll(server);
                    String derankMsg = config.antiCheatEnabled ? " Run is deranked for ranking." : "";
                    player.sendMessage(net.minecraft.text.Text.literal(GSRUiParameters.MSG_PREFIX + "Locators cleared." + derankMsg), false);
                    return;
                }

                String type = switch (action) {
                    case GSRLocatorActionPayload.ACTION_TOGGLE_FORTRESS -> "fortress";
                    case GSRLocatorActionPayload.ACTION_TOGGLE_BASTION -> "bastion";
                    case GSRLocatorActionPayload.ACTION_TOGGLE_STRONGHOLD -> "stronghold";
                    case GSRLocatorActionPayload.ACTION_TOGGLE_SHIP -> "ship";
                    default -> null;
                };
                if (type == null) return;

                if (!GSRLocatorGate.canUseLocator(config, type, isAdmin)) {
                    String reason = GSRLocatorGate.getLockReason(config, type, isAdmin);
                    player.sendMessage(net.minecraft.text.Text.literal(GSRUiParameters.MSG_PREFIX + (reason.isEmpty() ? "Locator locked." : reason)), false);
                    return;
                }

                ServerWorld world = switch (type) {
                    case "fortress", "bastion" -> server.getWorld(World.NETHER);
                    case "stronghold" -> server.getOverworld();
                    case "ship" -> server.getWorld(World.END);
                    default -> null;
                };
                if (world == null) {
                    player.sendMessage(net.minecraft.text.Text.literal(GSRUiParameters.MSG_PREFIX + "Dimension not loaded."), false);
                    return;
                }
                BlockPos from = player.getBlockPos();
                BlockPos found = GSRLocateHelper.locate(world, type, from);
                if (found == null) {
                    GSRConfigPlayer pc = GSRProfileManager.getPlayerConfig(player);
                    if (pc != null) {
                        switch (type) {
                            case "fortress" -> pc.fortressLocatorOn = false;
                            case "bastion" -> pc.bastionLocatorOn = false;
                            case "stronghold" -> pc.strongholdLocatorOn = false;
                            case "ship" -> pc.shipLocatorOn = false;
                            default -> {}
                        }
                        GSRProfileManager.save(server);
                        GSRConfigSync.syncConfigWithPlayer(player);
                    }
                    player.sendMessage(net.minecraft.text.Text.literal(GSRUiParameters.MSG_PREFIX + String.format(GSRUiParameters.MSG_LOCATOR_NOT_FOUND, type)), false);
                    return;
                }
                int x = found.getX();
                int z = found.getZ();
                GSRConfigPlayer pc = GSRProfileManager.getPlayerConfig(player);
                if (pc == null) return;
                switch (type) {
                    case "fortress" -> {
                        pc.fortressLocatorOn = !pc.fortressLocatorOn;
                        if (pc.fortressLocatorOn) {
                            config.fortressX = x;
                            config.fortressZ = z;
                            config.fortressLocated = true;
                        }
                    }
                    case "bastion" -> {
                        pc.bastionLocatorOn = !pc.bastionLocatorOn;
                        if (pc.bastionLocatorOn) {
                            config.bastionX = x;
                            config.bastionZ = z;
                            config.bastionLocated = true;
                        }
                    }
                    case "stronghold" -> {
                        pc.strongholdLocatorOn = !pc.strongholdLocatorOn;
                        if (pc.strongholdLocatorOn) {
                            config.strongholdX = x;
                            config.strongholdZ = z;
                            config.strongholdLocated = true;
                        }
                    }
                    case "ship" -> {
                        pc.shipLocatorOn = !pc.shipLocatorOn;
                        if (pc.shipLocatorOn) {
                            config.shipX = x;
                            config.shipY = found.getY();
                            config.shipZ = z;
                            config.shipLocated = true;
                        }
                    }
                }
                config.locatorDeranked = true;
                config.save(server);
                GSRProfileManager.save(server);
                GSRConfigSync.syncConfigWithAll(server);
                boolean nowActive = switch (type) {
                    case "fortress" -> pc.fortressLocatorOn;
                    case "bastion" -> pc.bastionLocatorOn;
                    case "stronghold" -> pc.strongholdLocatorOn;
                    default -> pc.shipLocatorOn;
                };
                String derankMsg = config.antiCheatEnabled ? " Run is deranked for ranking." : "";
                player.sendMessage(net.minecraft.text.Text.literal(GSRUiParameters.MSG_PREFIX + "Locator " + type + ": " + (nowActive ? "ON at " + x + ", " + z : "OFF") + "." + derankMsg), false);
            });
        });
    }

    /** Server: Run Manager request and update handlers. */
    public static void registerRunManagerReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(GSRRunManagerRequestPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                GSRConfigWorld config = GSRMain.CONFIG;
                if (config == null) return;
                NbtCompound nbt = new NbtCompound();
                NbtList playersList = new NbtList();
                for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
                    if (p == null) continue;
                    NbtCompound entry = new NbtCompound();
                    entry.putString("uuid", p.getUuid().toString());
                    entry.putString("name", p.getName().getString());
                    playersList.add(entry);
                }
                nbt.put("players", playersList);
                GSRRunManagerNbt.writeUuidSet(nbt, GSRWorldConfigParameters.K_GROUP_DEATH_PARTICIPANTS, config.groupDeathParticipants);
                GSRRunManagerNbt.writeUuidSet(nbt, GSRWorldConfigParameters.K_SHARED_HEALTH_PARTICIPANTS, config.sharedHealthParticipants);
                GSRRunManagerNbt.writeUuidSet(nbt, GSRWorldConfigParameters.K_EXCLUDED_FROM_RUN, config.excludedFromRun);
                ServerPlayNetworking.send(player, new GSRPlayerListPayload(nbt));
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(GSRRunManagerUpdatePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                ServerCommandSource src = context.server().getCommandSource().withEntity(player).withWorld((ServerWorld) player.getEntityWorld());
                if (!CommandManager.ADMINS_CHECK.allows(src.getPermissions())) return;
                GSRConfigWorld config = GSRMain.CONFIG;
                if (config == null) return;
                NbtCompound nbt = payload.nbt();
                GSRRunManagerNbt.readUuidSet(nbt, GSRWorldConfigParameters.K_GROUP_DEATH_PARTICIPANTS, config.groupDeathParticipants);
                GSRRunManagerNbt.readUuidSet(nbt, GSRWorldConfigParameters.K_SHARED_HEALTH_PARTICIPANTS, config.sharedHealthParticipants);
                GSRRunManagerNbt.readUuidSet(nbt, GSRWorldConfigParameters.K_EXCLUDED_FROM_RUN, config.excludedFromRun);
                if (config.antiCheatEnabled && config.startTime > 0 && !config.isVictorious && !config.isFailed) {
                    config.locatorDeranked = true;
                }
                config.save(context.server());
                GSRConfigSync.syncConfigWithAll(context.server());
                String derankMsg = config.antiCheatEnabled && config.locatorDeranked ? " Run is deranked for ranking." : "";
                player.sendMessage(net.minecraft.text.Text.literal(GSRUiParameters.MSG_PREFIX + "Run Manager settings saved." + derankMsg), false);
            });
        });
    }

    /** Server: Run sync handlers (run IDs, run request, run data). */
    public static void registerRunSyncReceivers() {
        ServerPlayNetworking.registerGlobalReceiver(GSRRunIdsPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                java.util.List<String> runIds = GSRRunIdsPayload.fromNbt(payload.nbt());
                GSRRunSyncManager.handleRunIds(context.server(), player, runIds);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(GSRRunRequestPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerPlayerEntity player = context.player();
                java.util.List<String> runIds = GSRRunIdsPayload.fromNbt(payload.nbt());
                GSRRunSyncManager.handleRunRequest(context.server(), player, runIds);
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(GSRRunDataPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                if (!GSRRunDataPayload.isValid(payload.nbt())) return;
                ServerPlayerEntity player = context.player();
                GSRRunSyncManager.handleRunData(context.server(), player, payload.nbt());
            });
        });
    }
}
