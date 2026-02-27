package net.berkle.groupspeedrun;

// Fabric: mod init, commands, lifecycle, networking
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

// Minecraft: server, world, damage
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

// GSR: config, managers, payloads, parameters, server
import net.berkle.groupspeedrun.config.GSRConfigPayload;
import net.berkle.groupspeedrun.server.GSRSharedHealthBroadcast;
import net.berkle.groupspeedrun.server.GSRSharedHealthEatAllowance;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.managers.GSRProfileManager;
import net.berkle.groupspeedrun.managers.GSRRunSyncManager;
import net.berkle.groupspeedrun.timer.GSRTimer;
import net.berkle.groupspeedrun.managers.GSRWorldSnapshotManager;
import net.berkle.groupspeedrun.network.GSRLocatorActionPayload;
import net.berkle.groupspeedrun.network.GSRScreenTimePayload;
import net.berkle.groupspeedrun.network.GSROpenScreenPayload;
import net.berkle.groupspeedrun.network.GSRSplitAchievedPayload;
import net.berkle.groupspeedrun.network.GSRVictoryCelebrationPayload;
import net.berkle.groupspeedrun.network.GSRPlayerListPayload;
import net.berkle.groupspeedrun.network.GSRRunActionPayload;
import net.berkle.groupspeedrun.network.GSRRunCompletePayload;
import net.berkle.groupspeedrun.network.GSRRunDataPayload;
import net.berkle.groupspeedrun.network.GSRRunIdsPayload;
import net.berkle.groupspeedrun.network.GSRRunIdsRequestPayload;
import net.berkle.groupspeedrun.network.GSRRunManagerRequestPayload;
import net.berkle.groupspeedrun.network.GSRRunManagerUpdatePayload;
import net.berkle.groupspeedrun.network.GSRRunRequestBroadcastPayload;
import net.berkle.groupspeedrun.network.GSRRunRequestPayload;
import net.berkle.groupspeedrun.network.GSRWorldConfigPayload;
import net.berkle.groupspeedrun.parameter.GSRServerParameters;

// Logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for Group Speed Run. Handles config load/save, commands, server tick, and payload registration.
 */
public class GSRMain implements ModInitializer {

    public static final String MOD_ID = "gsr";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Global world run configuration. */
    public static GSRConfigWorld CONFIG = new GSRConfigWorld();
    /** True when timer is frozen because single-player pause menu is open; cleared on admin pause or server stop. */
    public static boolean frozenByClientPause = false;

    /** Returns the root timer object for start/resume/reset and state queries. OOP-first design per spec. */
    public static GSRTimer getTimer() {
        return GSRTimer.getInstance();
    }

    @Override
    @SuppressWarnings("null")
    public void onInitialize() {
        LOGGER.info("[GSR] Initializing Group Speed Run...");

        PayloadTypeRegistry.playS2C().register(GSRConfigPayload.ID, GSRConfigPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GSRConfigPayload.ID, GSRConfigPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GSROpenScreenPayload.ID, GSROpenScreenPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GSRRunActionPayload.ID, GSRRunActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GSRLocatorActionPayload.ID, GSRLocatorActionPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GSRScreenTimePayload.ID, GSRScreenTimePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GSRPlayerListPayload.ID, GSRPlayerListPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GSRRunCompletePayload.ID, GSRRunCompletePayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GSRSplitAchievedPayload.ID, GSRSplitAchievedPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GSRVictoryCelebrationPayload.ID, GSRVictoryCelebrationPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GSRRunIdsPayload.ID, GSRRunIdsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GSRRunIdsPayload.ID, GSRRunIdsPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GSRRunIdsRequestPayload.ID, GSRRunIdsRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GSRRunRequestPayload.ID, GSRRunRequestPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GSRRunRequestBroadcastPayload.ID, GSRRunRequestBroadcastPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GSRRunDataPayload.ID, GSRRunDataPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(GSRRunDataPayload.ID, GSRRunDataPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GSRRunManagerRequestPayload.ID, GSRRunManagerRequestPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GSRRunManagerUpdatePayload.ID, GSRRunManagerUpdatePayload.CODEC);
        PayloadTypeRegistry.playC2S().register(GSRWorldConfigPayload.ID, GSRWorldConfigPayload.CODEC);
        GSRNetworking.registerC2SReceiver();
        GSRNetworking.registerWorldConfigReceiver();
        GSRNetworking.registerRunActionReceiver();
        GSRNetworking.registerLocatorActionReceiver();
        GSRNetworking.registerScreenTimeReceiver();
        GSRNetworking.registerRunManagerReceivers();
        GSRNetworking.registerRunSyncReceivers();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            GSRCommands.register(dispatcher);
        });

        ServerLivingEntityEvents.AFTER_DAMAGE.register((entity, source, baseDamageTaken, damageTaken, blocked) -> {
            if (entity instanceof ServerPlayerEntity player && damageTaken > 0) {
                GSRSharedHealthBroadcast.onSharedHealthPlayerDamaged(player, source, damageTaken);
                // Track damage taken using actual amount (post-armor); covers all damage types
                if (GSRMain.CONFIG != null && GSRMain.CONFIG.startTime > 0 && !GSRMain.CONFIG.isTimerFrozen
                        && entity.getEntityWorld() instanceof ServerWorld world) {
                    String typeId = GSRStats.getDamageTypeId(world, source);
                    GSRStats.addDamageTakenByType(player.getUuid(), typeId, damageTaken);
                    if (source.isOf(DamageTypes.FALL) || "minecraft:fall".equals(typeId)) {
                        GSRStats.addFloat(GSRStats.FALL_DAMAGE_TAKEN, player.getUuid(), damageTaken);
                    }
                }
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof net.minecraft.server.network.ServerPlayerEntity) return;
            var attacker = damageSource.getAttacker();
            if (attacker instanceof ServerPlayerEntity player && GSRStats.shouldRecordForPlayer(player.getUuid())) {
                if (GSRMain.CONFIG != null && GSRMain.CONFIG.startTime > 0 && !GSRMain.CONFIG.isTimerFrozen) {
                    String entityTypeId = entity.getType().getRegistryEntry().getKey().map(k -> k.getValue().toString()).orElse(null);
                    if (entityTypeId != null) GSRStats.addEntityKill(player.getUuid(), entityTypeId);
                }
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            getTimer().primeRunIfArmed(server);
            // Resume runs frozen by server stop before syncing so client gets running state, not frozen
            getTimer().tryAutoStartOrResumeOnJoin(server);
            GSRNetworking.syncConfigWithPlayer(player);
            GSRRunSyncManager.onPlayerJoin(server, player);
            // Auto-add new joiners to group death (default: all players in). Group health stays empty.
            GSRNetworking.addJoinerToGroupDeath(server, player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            GSRSharedHealthEatAllowance.onPlayerDisconnect(handler.getPlayer().getUuid());
            GSRRunSyncManager.onPlayerDisconnect(handler.getPlayer().getUuid());
            getTimer().tryFreezeOnLastPlayerDisconnect(server);
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            GSRWorldSnapshotManager.checkAndRestoreIfNeeded(server);
        });
        ServerWorldEvents.LOAD.register((server, world) -> {
            // Load config when overworld loads so save path is definitely available
            if (world.getRegistryKey() == World.OVERWORLD) {
                CONFIG = GSRConfigWorld.load(server);
                // Prime armed state immediately so auto-start works even if LOAD fires after SERVER_STARTED
                getTimer().primeRunIfArmed(server);
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            GSRStats.load(server);
            GSRProfileManager.load(server);
            getTimer().primeRunIfArmed(server);
            // Auto-resume runs frozen by server stop; manual pause stays paused until manual resume
            getTimer().tryAutoStartOrResumeOnJoin(server);
        });

        ServerLifecycleEvents.BEFORE_SAVE.register((server, flush, force) -> {
            // Persist run state as part of world save (Save and Quit, autosave). Ensures run data survives restart.
            if (CONFIG != null) CONFIG.save(server);
            GSRStats.save(server);
            GSRProfileManager.save(server);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            frozenByClientPause = false;
            if (CONFIG != null) {
                if (CONFIG.startTime > 0 && !CONFIG.isVictorious && !CONFIG.isFailed) {
                    if (!CONFIG.isTimerFrozen) {
                        CONFIG.frozenTime = CONFIG.getElapsedTime();
                        CONFIG.isTimerFrozen = true;
                        CONFIG.frozenByServerStop = true;
                    } else if (!CONFIG.manualPause) {
                        // Already frozen by pause menu or last player leave; mark for auto-resume on reload
                        CONFIG.frozenByServerStop = true;
                    }
                    // manualPause: leave frozenByServerStop=false so timer stays paused on reload
                }
                CONFIG.save(server);
            }
            GSRStats.save(server);
            GSRProfileManager.save(server);
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (server.getTicks() == GSRServerParameters.SNAPSHOT_DEFER_TICKS) {
                GSRWorldSnapshotManager.takeSnapshotIfNeeded(server);
            }
            GSREvents.onTick(server);
            if (server.getTicks() % GSRServerParameters.SAVE_INTERVAL_TICKS == 0) {
                if (CONFIG != null) {
                    CONFIG.save(server);
                    GSRNetworking.syncConfigWithAll(server);
                }
                GSRStats.save(server);
                GSRProfileManager.save(server);
            }
        });
    }
}
