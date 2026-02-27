package net.berkle.groupspeedrun;

// Fabric: client mod init, lifecycle, networking
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

// Minecraft: screens, widgets
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.widget.ButtonWidget;

// GSR: client, config, data, gui, network, util
import net.berkle.groupspeedrun.client.GSRCelebrationHandler;
import net.berkle.groupspeedrun.client.GSRKeyBindings;
import net.berkle.groupspeedrun.client.GSRSharedRunLoader;
import net.berkle.groupspeedrun.client.GSRTitleScreenLayout;
import net.berkle.groupspeedrun.config.GSRConfigPayload;
import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.data.GSRRunSaveStateNbt;
import net.berkle.groupspeedrun.gui.preferences.GSRPreferencesScreen;
import net.berkle.groupspeedrun.gui.GSRControlsScreen;
import net.berkle.groupspeedrun.gui.GSRLocatorsScreen;
import net.berkle.groupspeedrun.gui.GSRNewWorldConfirmScreen;
import net.berkle.groupspeedrun.gui.GSRRunManagerScreen;
import net.berkle.groupspeedrun.network.GSRRunActionPayload;
import net.berkle.groupspeedrun.network.GSRScreenTimePayload;
import net.berkle.groupspeedrun.network.GSROpenScreenPayload;
import net.berkle.groupspeedrun.network.GSRPlayerListPayload;
import net.berkle.groupspeedrun.network.GSRRunCompletePayload;
import net.berkle.groupspeedrun.network.GSRRunDataPayload;
import net.berkle.groupspeedrun.network.GSRRunIdsPayload;
import net.berkle.groupspeedrun.network.GSRRunIdsRequestPayload;
import net.berkle.groupspeedrun.network.GSRSplitAchievedPayload;
import net.berkle.groupspeedrun.network.GSRVictoryCelebrationPayload;
import net.berkle.groupspeedrun.network.GSRRunRequestBroadcastPayload;
import net.berkle.groupspeedrun.network.GSRRunRequestPayload;
import net.berkle.groupspeedrun.util.GSRAlphaUtil;
import net.berkle.groupspeedrun.util.GSRQuickNewWorld;

/**
 * Client entrypoint. Holds client-side run state and player HUD config for mixin.
 */
@SuppressWarnings("null")
public class GSRClient implements ClientModInitializer {

    /** Client-side copy of run state (updated from server sync). */
    public static final GSRConfigWorld clientWorldConfig = new GSRConfigWorld();

    /** Local player HUD config (updated from server sync). */
    public static final GSRConfigPlayer PLAYER_CONFIG = new GSRConfigPlayer();

    /** When set, the next opened CreateWorldScreen will have this name pre-filled. Cleared when used. */
    public static volatile String nextGsrWorldName = null;

    /** When >= 0, single-player pause screen is open: display this elapsed ms and auto-resume when closed. */
    private static long clientPausedElapsedMs = -1;

    /** When HUD visibility is Toggle, this is the current visibility. Default true so HUD starts visible. */
    private static boolean hudToggledVisible = true;
    /** Previous hudVisibility from last config receive; -1 = never received. Used to detect mode switch. */
    private static int previousHudVisibility = -1;

    /** G key was held last tick. Used to detect release (open menu only when G is let go by itself). */
    private static boolean gKeyHeldLastTick = false;
    /** True if we opened config (G+C) during this G hold; don't open menu on release. */
    private static boolean openedConfigDuringGHold = false;
    /** Previous single-player pause state; used to detect transitions for server timer freeze. */
    private static boolean wasSinglePlayerPaused = false;
    /** True when pause menu was opened while run was running (we froze it); false when run was already manually paused. */
    private static boolean wasRunRunningWhenMenuOpened = false;

    /**
     * True when the given screen should freeze the GSR timer in single player.
     * Includes GSR Preferences, GSR Controls, Options menu, and its children (Controls, Video, etc.).
     */
    private static boolean shouldFreezeTimerForScreen(Screen screen) {
        if (screen == null) return false;
        return screen instanceof GSRPreferencesScreen || screen instanceof GSRControlsScreen || screen instanceof GameOptionsScreen;
    }

    @Override
    public void onInitializeClient() {
        GSRKeyBindings.register();
        ClientPlayNetworking.registerGlobalReceiver(GSRConfigPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                var client = context.client();
                clientWorldConfig.readNbt(payload.nbt());
                PLAYER_CONFIG.readNbt(payload.nbt());
                // When opening a world, config may arrive while paused; ensure paused time shows current run time
                if (client.getServer() != null && (client.isPaused() || shouldFreezeTimerForScreen(client.currentScreen)) && client.world != null) {
                    clientPausedElapsedMs = clientWorldConfig.getElapsedTime();
                }
                int newVisibility = PLAYER_CONFIG.hudVisibility;
                // Only reset toggle when switching FROM Pressed TO Toggle (so HUD isn't stuck off)
                if (newVisibility == GSRConfigPlayer.VISIBILITY_TOGGLE
                        && (previousHudVisibility == GSRConfigPlayer.VISIBILITY_PRESSED || previousHudVisibility == -1)) {
                    hudToggledVisible = true;
                }
                previousHudVisibility = newVisibility;
                // Refresh controls screen so button labels and timer reflect new state
                if (client.currentScreen instanceof GSRControlsScreen gsr) {
                    client.setScreen(new GSRControlsScreen(gsr.getParent()));
                }
                // Refresh locators screen so toggles and preview reflect new state
                if (client.currentScreen instanceof GSRLocatorsScreen loc) {
                    client.setScreen(new GSRLocatorsScreen(loc.getParent()));
                }
                // Refresh preferences screen so dropdowns reflect new state; preserve scroll position
                if (client.currentScreen instanceof net.berkle.groupspeedrun.gui.preferences.GSRPreferencesScreen prefs) {
                    client.setScreen(new net.berkle.groupspeedrun.gui.preferences.GSRPreferencesScreen(prefs.getParent(), prefs.getContentScroll()));
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(GSROpenScreenPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                var client = context.client();
                if (payload.screenType() == GSROpenScreenPayload.TYPE_CONFIG) {
                    client.setScreen(new GSRPreferencesScreen(client.currentScreen));
                } else if (payload.screenType() == GSROpenScreenPayload.TYPE_CONTROLS) {
                    client.setScreen(new GSRControlsScreen(client.currentScreen));
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(GSRRunCompletePayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                var state = GSRRunSaveStateNbt.fromNbt(payload.nbt());
                if (state != null) GSRSharedRunLoader.saveToPersonal(state);
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(GSRRunIdsRequestPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                var runIds = GSRSharedRunLoader.getRunIds();
                if (!runIds.isEmpty()) {
                    ClientPlayNetworking.send(new GSRRunIdsPayload(GSRRunIdsPayload.toNbt(runIds)));
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(GSRRunIdsPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                var othersRunIds = new java.util.HashSet<>(GSRRunIdsPayload.fromNbt(payload.nbt()));
                var myRunIds = new java.util.HashSet<>(GSRSharedRunLoader.getRunIds());
                othersRunIds.removeAll(myRunIds);
                if (!othersRunIds.isEmpty()) {
                    ClientPlayNetworking.send(new GSRRunRequestPayload(GSRRunIdsPayload.toNbt(new java.util.ArrayList<>(othersRunIds))));
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(GSRRunRequestBroadcastPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                var runIds = GSRRunRequestBroadcastPayload.getRunIds(payload.nbt());
                for (String runId : runIds) {
                    var state = GSRSharedRunLoader.loadByRunId(runId);
                    if (state != null) {
                        var runNbt = net.berkle.groupspeedrun.data.GSRRunSaveStateNbt.toNbt(state);
                        ClientPlayNetworking.send(new GSRRunDataPayload(GSRRunDataPayload.toNbt(runId, runNbt)));
                    }
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(GSRRunDataPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                if (!GSRRunDataPayload.isValid(payload.nbt())) return;
                var runNbt = payload.nbt().getCompound(GSRRunDataPayload.KEY_RUN_NBT).orElse(null);
                if (runNbt != null) {
                    var state = GSRRunSaveStateNbt.fromNbt(runNbt);
                    if (state != null) GSRSharedRunLoader.saveToShared(state);
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(GSRPlayerListPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                var client = context.client();
                if (client.currentScreen instanceof GSRRunManagerScreen runManager) {
                    runManager.setRunManagerData(payload.nbt());
                }
            });
        });
        ClientPlayNetworking.registerGlobalReceiver(GSRSplitAchievedPayload.ID, (payload, context) -> {
            context.client().execute(() -> GSRCelebrationHandler.onSplitAchieved());
        });
        ClientPlayNetworking.registerGlobalReceiver(GSRVictoryCelebrationPayload.ID, (payload, context) -> {
            context.client().execute(() -> GSRCelebrationHandler.onVictoryCelebration());
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            // Clear completion state only so fail/victory does not carry over between worlds.
            // Preserve startTime/frozenTime; server sends full config on join.
            clientWorldConfig.clearCompletionStateOnly();
        });
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (client.world == null) {
                clientPausedElapsedMs = -1;
                gKeyHeldLastTick = false;
                openedConfigDuringGHold = false;
                wasSinglePlayerPaused = false;
                wasRunRunningWhenMenuOpened = false;
                return;
            }
            GSRAlphaUtil.tick();
            boolean singlePlayerPaused = client.getServer() != null && (client.isPaused() || shouldFreezeTimerForScreen(client.currentScreen));
            if (singlePlayerPaused != wasSinglePlayerPaused) {
                wasSinglePlayerPaused = singlePlayerPaused;
                if (singlePlayerPaused) {
                    wasRunRunningWhenMenuOpened = !clientWorldConfig.isTimerFrozen;
                } else {
                    GSRAlphaUtil.startMenuCloseFade();
                }
                if (ClientPlayNetworking.canSend(GSRRunActionPayload.ID)) {
                    ClientPlayNetworking.send(new GSRRunActionPayload(
                            singlePlayerPaused ? GSRRunActionPayload.ACTION_CLIENT_PAUSE : GSRRunActionPayload.ACTION_CLIENT_RESUME));
                }
            }
            clientPausedElapsedMs = singlePlayerPaused ? (clientPausedElapsedMs < 0 ? clientWorldConfig.getElapsedTime() : clientPausedElapsedMs) : -1;
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            GSRCelebrationHandler.tick(client);
            if (client.world == null) {
                return;
            }
            // Report player/creative inventory screen time (server cannot detect these)
            if (client.player != null && ClientPlayNetworking.canSend(GSRScreenTimePayload.ID)
                    && clientWorldConfig.startTime > 0 && !clientWorldConfig.isTimerFrozen
                    && !clientWorldConfig.isVictorious && !clientWorldConfig.isFailed) {
                Screen screen = client.currentScreen;
                if (screen instanceof InventoryScreen) {
                    ClientPlayNetworking.send(new GSRScreenTimePayload(GSRScreenTimePayload.PLAYER_INVENTORY));
                } else if (screen instanceof CreativeInventoryScreen) {
                    ClientPlayNetworking.send(new GSRScreenTimePayload(GSRScreenTimePayload.CREATIVE_INVENTORY));
                }
            }
            if (client.player != null) {
                boolean gPressed = GSRKeyBindings.openGsrOptionsKey != null && GSRKeyBindings.openGsrOptionsKey.isPressed();
                if (GSRKeyBindings.openGsrConfigKey != null && GSRKeyBindings.openGsrConfigKey.wasPressed() && gPressed) {
                    client.setScreen(new GSRPreferencesScreen(client.currentScreen));
                    openedConfigDuringGHold = true;
                    gKeyHeldLastTick = true;
                    return;
                }
                if (gPressed) {
                    gKeyHeldLastTick = true;
                } else {
                    if (gKeyHeldLastTick && !openedConfigDuringGHold) client.setScreen(new GSRControlsScreen(client.currentScreen));
                    gKeyHeldLastTick = false;
                    openedConfigDuringGHold = false;
                }
                if (client.currentScreen == null && GSRKeyBindings.newGsrWorldKey.wasPressed() && client.getServer() != null
                        && (clientWorldConfig.effectiveAllowNewWorldBeforeRunEnd || clientWorldConfig.isFailed || clientWorldConfig.isVictorious)) {
                    int runNum = GSRQuickNewWorld.getAndIncrementRunCount();
                    String hostName = client.getSession() != null ? client.getSession().getUsername() : "Host";
                    client.setScreen(new GSRNewWorldConfirmScreen(null, GSRQuickNewWorld.suggestedWorldName(runNum, hostName)));
                }
            } else {
                gKeyHeldLastTick = false;
                openedConfigDuringGHold = false;
            }

            if (client.currentScreen == null && PLAYER_CONFIG != null && PLAYER_CONFIG.hudVisibility == GSRConfigPlayer.VISIBILITY_TOGGLE
                    && GSRKeyBindings.toggleGsrHudKey != null && GSRKeyBindings.toggleGsrHudKey.wasPressed()) {
                hudToggledVisible = !hudToggledVisible;
            }
        });
    }

    /** Called when config is saved/synced with Toggle mode so HUD defaults to visible. */
    public static void setHudToggledVisible(boolean visible) {
        hudToggledVisible = visible;
    }

    /** Called when config is saved locally so next config receive doesn't incorrectly reset toggle. */
    public static void setPreviousHudVisibility(int visibility) {
        previousHudVisibility = visibility;
    }

    /** True when the HUD should be shown (hold pressed or toggle on). Used for render and fade. */
    public static boolean isGsrHudShowActive() {
        if (PLAYER_CONFIG == null) return isGsrHudPressActive() || isGsrHudToggleActive();
        if (PLAYER_CONFIG.hudVisibility == GSRConfigPlayer.VISIBILITY_PRESSED) return isGsrHudPressActive();
        return isGsrHudToggleActive();
    }

    /** True when hold-to-show key (Tab) is pressed. */
    public static boolean isGsrHudPressActive() {
        return GSRKeyBindings.pressToShowGsrHudKey != null && GSRKeyBindings.pressToShowGsrHudKey.isPressed();
    }

    /** True when HUD is toggled on (V key). */
    public static boolean isGsrHudToggleActive() {
        return hudToggledVisible;
    }

    /** Creates GSR Controls button for main menu. Caller must add the returned button. */
    public static ButtonWidget createControlsButton(net.minecraft.client.MinecraftClient client, net.minecraft.client.gui.screen.Screen screen, int width, int height) {
        return GSRTitleScreenLayout.createControlsButton(client, screen, width, height);
    }

    /** Re-apply layout after refreshWidgetPositions. Delegates to {@link GSRTitleScreenLayout}. */
    public static void applyRunHistoryLayout(net.minecraft.client.gui.screen.Screen screen) {
        GSRTitleScreenLayout.applyRunHistoryLayout(screen);
    }

    /** True when the client is in single player with the pause screen open (timer display frozen). */
    public static boolean isClientTimerPaused() {
        return clientPausedElapsedMs >= 0;
    }

    /** True when pause menu was opened while run was running (we froze it). False when run was already manually paused. */
    public static boolean wasRunRunningWhenMenuOpened() {
        return wasRunRunningWhenMenuOpened;
    }

    /** Elapsed ms for HUD display. When no active run, returns 0. When paused, returns frozen time; otherwise live time.
     * In singleplayer, when timer is frozen (manual pause or server freeze), always use frozenTime so display stays
     * frozen. In multiplayer, pause menu does not stop the server so time advances; manual pause still freezes. */
    public static long getClientElapsedMs() {
        if (clientWorldConfig == null || clientWorldConfig.startTime <= 0) return 0;
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client != null && client.getServer() != null) {
            // Singleplayer: when timer is frozen by server/manual pause, use frozenTime so display stays frozen
            if (clientWorldConfig.isTimerFrozen && clientWorldConfig.frozenTime > 0) {
                return clientWorldConfig.frozenTime;
            }
            if (client.isPaused() || shouldFreezeTimerForScreen(client.currentScreen)) {
                if (clientPausedElapsedMs < 0) clientPausedElapsedMs = clientWorldConfig.getElapsedTime();
                return clientPausedElapsedMs;
            }
        }
        return clientWorldConfig.getElapsedTime();
    }
}
