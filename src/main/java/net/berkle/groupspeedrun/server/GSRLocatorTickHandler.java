package net.berkle.groupspeedrun.server;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.managers.GSRLocateHelper;
import net.berkle.groupspeedrun.managers.GSRProfileManager;
import net.berkle.groupspeedrun.parameter.GSRLocatorParameters;
import net.berkle.groupspeedrun.parameter.GSRServerParameters;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * Server tick: locator enter detection and fade. When a player enters a tracked
 * structure, turn off the locator and optionally start a fade animation.
 */
public final class GSRLocatorTickHandler {

    private GSRLocatorTickHandler() {}

    public static void checkLocatorEnterAndFade(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null) return;
        long worldTime = server.getOverworld().getTime();

        if (config.locatorFadeStartTime > 0) {
            long elapsed = worldTime - config.locatorFadeStartTime;
            if (elapsed >= GSRLocatorParameters.LOCATOR_FADE_TICKS) {
                config.locatorFadeStartTime = 0;
                config.locatorFadeType = "";
                config.save(server);
                GSRConfigSync.syncConfigWithAll(server);
                return;
            }
        }

        if (server.getTicks() % GSRServerParameters.LOCATOR_CHECK_INTERVAL != 0) return;

        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            if (!(player.getEntityWorld() instanceof ServerWorld sw)) continue;
            BlockPos pos = player.getBlockPos();
            GSRConfigPlayer pc = GSRProfileManager.getPlayerConfig(player);
            if (pc == null) continue;

            if (sw.getRegistryKey() == World.OVERWORLD && config.strongholdLocated && pc.strongholdLocatorOn) {
                if (GSRLocateHelper.isInTrackedStructure(sw, pos, "stronghold", config.strongholdX, config.strongholdZ)) {
                    turnOffLocator(config, pc, "stronghold", worldTime, server);
                    return;
                }
            }
            if (sw.getRegistryKey() == World.NETHER) {
                if (config.fortressLocated && pc.fortressLocatorOn && GSRLocateHelper.isInTrackedStructure(sw, pos, "fortress", config.fortressX, config.fortressZ)) {
                    turnOffLocator(config, pc, "fortress", worldTime, server);
                    return;
                }
                if (config.bastionLocated && pc.bastionLocatorOn && GSRLocateHelper.isInTrackedStructure(sw, pos, "bastion", config.bastionX, config.bastionZ)) {
                    turnOffLocator(config, pc, "bastion", worldTime, server);
                    return;
                }
            }
            if (sw.getRegistryKey() == World.END && config.shipLocated && pc.shipLocatorOn) {
                if (GSRLocateHelper.isInTrackedStructure(sw, pos, "ship", config.shipX, config.shipY, config.shipZ)) {
                    turnOffLocator(config, pc, "ship", worldTime, server);
                    return;
                }
            }
        }
    }

    private static void turnOffLocator(GSRConfigWorld config, GSRConfigPlayer pc, String type, long worldTime, MinecraftServer server) {
        boolean onlyOne = switch (type) {
            case "stronghold", "ship" -> true;
            case "fortress" -> !pc.bastionLocatorOn;
            case "bastion" -> !pc.fortressLocatorOn;
            default -> false;
        };

        switch (type) {
            case "fortress" -> pc.fortressLocatorOn = false;
            case "bastion" -> pc.bastionLocatorOn = false;
            case "stronghold" -> pc.strongholdLocatorOn = false;
            case "ship" -> pc.shipLocatorOn = false;
        }

        if (onlyOne) {
            config.locatorFadeStartTime = worldTime;
            config.locatorFadeType = type;
        } else {
            config.locatorFadeStartTime = 0;
            config.locatorFadeType = "";
        }
        config.save(server);
        GSRProfileManager.save(server);
        GSRConfigSync.syncConfigWithAll(server);
    }
}
