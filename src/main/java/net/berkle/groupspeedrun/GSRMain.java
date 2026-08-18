package net.berkle.groupspeedrun;

// Fabric: mod init, commands, lifecycle, networking
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

// Minecraft: server, world, damage
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;

// GSR: config, managers, payloads, parameters, server
import net.berkle.groupspeedrun.config.GSRConfigPayload;
import net.berkle.groupspeedrun.server.GSRSharedHealthBroadcast;
import net.berkle.groupspeedrun.server.GSRSharedHealthEatAllowance;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.managers.GSRProfileManager;
import net.berkle.groupspeedrun.managers.GSRDataStore;
import net.berkle.groupspeedrun.managers.GSRRunSyncManager;
import net.berkle.groupspeedrun.timer.GSRTimer;
import net.berkle.groupspeedrun.managers.GSRWorldSnapshotManager;
import net.berkle.groupspeedrun.network.GSRLocatorActionPayload;
import net.berkle.groupspeedrun.network.GSRLocatorFeedbackPayload;
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
    /** True after this server's world config has been loaded from disk. Prevents primed defaults from overwriting saves. */
    public static boolean worldConfigLoaded = false;
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

        PayloadTypeRegistry.clientboundPlay().register(GSRConfigPayload.ID, GSRConfigPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GSRConfigPayload.ID, GSRConfigPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GSROpenScreenPayload.ID, GSROpenScreenPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GSRRunActionPayload.ID, GSRRunActionPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GSRLocatorActionPayload.ID, GSRLocatorActionPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GSRLocatorFeedbackPayload.ID, GSRLocatorFeedbackPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GSRScreenTimePayload.ID, GSRScreenTimePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GSRPlayerListPayload.ID, GSRPlayerListPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GSRRunCompletePayload.ID, GSRRunCompletePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GSRSplitAchievedPayload.ID, GSRSplitAchievedPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GSRVictoryCelebrationPayload.ID, GSRVictoryCelebrationPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GSRRunIdsPayload.ID, GSRRunIdsPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GSRRunIdsPayload.ID, GSRRunIdsPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GSRRunIdsRequestPayload.ID, GSRRunIdsRequestPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GSRRunRequestPayload.ID, GSRRunRequestPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GSRRunRequestBroadcastPayload.ID, GSRRunRequestBroadcastPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GSRRunDataPayload.ID, GSRRunDataPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(GSRRunDataPayload.ID, GSRRunDataPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GSRRunManagerRequestPayload.ID, GSRRunManagerRequestPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GSRRunManagerUpdatePayload.ID, GSRRunManagerUpdatePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(GSRWorldConfigPayload.ID, GSRWorldConfigPayload.CODEC);
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
            if (entity instanceof ServerPlayer player && damageTaken > 0) {
                GSRSharedHealthBroadcast.onSharedHealthPlayerDamaged(player, source, damageTaken);
                // Track damage taken using actual amount (post-armor); covers all damage types
                if (GSRMain.CONFIG != null && GSRMain.CONFIG.startTime > 0 && !GSRMain.CONFIG.isTimerFrozen
                        && entity.level() instanceof ServerLevel world) {
                    String typeId = GSRStats.getDamageTypeId(world, source);
                    GSRStats.addDamageTakenByType(player.getUUID(), typeId, damageTaken);
                    if (source.is(DamageTypes.FALL) || "minecraft:fall".equals(typeId)) {
                        GSRStats.addFloat(GSRStats.FALL_DAMAGE_TAKEN, player.getUUID(), damageTaken);
                    }
                }
            }
        });

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof net.minecraft.server.level.ServerPlayer) return;
            var attacker = damageSource.getEntity();
            if (attacker instanceof ServerPlayer player && GSRStats.shouldRecordForPlayer(player.getUUID())) {
                if (GSRMain.CONFIG != null && GSRMain.CONFIG.startTime > 0 && !GSRMain.CONFIG.isTimerFrozen) {
                    String entityTypeId = entity.getType().builtInRegistryHolder().unwrapKey().map(k -> k.identifier().toString()).orElse(null);
                    if (entityTypeId != null) GSRStats.addEntityKill(player.getUUID(), entityTypeId);
                }
            }
        });

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.getPlayer();
            ensureWorldConfigLoaded(server);
            getTimer().primeRunIfArmed(server);
            // Resume runs frozen by server stop before syncing so client gets running state, not frozen
            getTimer().tryAutoStartOrResumeOnJoin(server);
            GSRNetworking.syncConfigWithPlayer(player);
            GSRRunSyncManager.onPlayerJoin(server, player);
            // Auto-add new joiners to group death (default: all players in). Group health stays empty.
            GSRNetworking.addJoinerToGroupDeath(server, player);
        });
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            GSRSharedHealthEatAllowance.onPlayerDisconnect(handler.getPlayer().getUUID());
            GSRRunSyncManager.onPlayerDisconnect(handler.getPlayer().getUUID());
            getTimer().tryFreezeOnLastPlayerDisconnect(server);
        });

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            GSRWorldSnapshotManager.checkAndRestoreIfNeeded(server);
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // Overworld is available here; Fabric 26.1 dropped ServerWorldEvents.LOAD
            ensureWorldConfigLoaded(server);
            getTimer().primeRunIfArmed(server);
            // Auto-resume runs frozen by server stop; manual pause stays paused until manual resume
            getTimer().tryAutoStartOrResumeOnJoin(server);
        });

        ServerLifecycleEvents.BEFORE_SAVE.register((server, flush, force) -> {
            // Persist run state as part of world save (Save and Quit, autosave). Ensures run data survives restart.
            if (worldConfigLoaded && CONFIG != null) CONFIG.save(server);
            if (worldConfigLoaded) {
                GSRStats.save(server);
                GSRProfileManager.save(server);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            frozenByClientPause = false;
            if (CONFIG != null && worldConfigLoaded) {
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
            if (worldConfigLoaded) {
                GSRStats.save(server);
                GSRProfileManager.save(server);
            }
            worldConfigLoaded = false;
        });

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (!worldConfigLoaded) return;
            if (server.getTickCount() == GSRServerParameters.SNAPSHOT_DEFER_TICKS) {
                GSRWorldSnapshotManager.takeSnapshotIfNeeded(server);
            }
            GSREvents.onTick(server);
            if (server.getTickCount() % GSRServerParameters.SAVE_INTERVAL_TICKS == 0) {
                if (CONFIG != null) {
                    CONFIG.save(server);
                    GSRNetworking.syncConfigWithAll(server);
                }
                GSRStats.save(server);
                GSRProfileManager.save(server);
            }
        });
    }

    /**
     * Loads world config/stats once per server start. Safe to call from JOIN if it races SERVER_STARTED.
     * Recovers a completed HUD from run history when the world file looks primed but a later completed run exists.
     */
    public static void ensureWorldConfigLoaded(MinecraftServer server) {
        if (worldConfigLoaded || server == null) return;
        CONFIG = GSRConfigWorld.load(server);
        GSRStats.load(server);
        GSRProfileManager.load(server);
        if (GSRDataStore.restoreHudFromLatestCompletedRun(server, CONFIG)) {
            LOGGER.info("[GSR] Restored completed run HUD from history (startTime={}, victory={}, fail={})",
                    CONFIG.startTime, CONFIG.isVictorious, CONFIG.isFailed);
        }
        worldConfigLoaded = true;
    }
}
