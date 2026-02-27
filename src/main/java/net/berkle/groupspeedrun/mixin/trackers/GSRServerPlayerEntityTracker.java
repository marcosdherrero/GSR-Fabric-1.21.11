package net.berkle.groupspeedrun.mixin.trackers;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.berkle.groupspeedrun.GSREvents;
import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.GSRStats;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.timer.listeners.GSRMovementAutoStartListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tracks distance moved per player when run is active. Auto-starts the timer on first movement when armed (startTime == -1).
 * Delegates auto-start/resume logic to {@link GSRMovementAutoStartListener}.
 */
@Mixin(ServerPlayerEntity.class)
public abstract class GSRServerPlayerEntityTracker {

    @Unique private double gsrPrevX, gsrPrevY, gsrPrevZ;
    @Unique private boolean gsrInitialized = false;
    /** True only when baseline was set while player was in ServerWorld (avoids false trigger on spawn/teleport). */
    @Unique private boolean gsrBaselineFromServerWorld = false;
    /** Ticks spent in ServerWorld while armed; need warmup before movement can trigger auto-start. */
    @Unique private int gsrArmedTicksInWorld = 0;

    /** Injects at end of tick to track distance moved, auto-start timer, or resume when frozen. */
    @Inject(method = "tick", at = @At("TAIL"))
    private void groupspeedrun$onTick(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (!GSRMovementAutoStartListener.isInServerWorld(player)) {
            gsrBaselineFromServerWorld = false;
            gsrArmedTicksInWorld = 0;
            updateCache(player.getX(), player.getY(), player.getZ());
            return;
        }
        ServerWorld sw = (ServerWorld) player.getEntityWorld();
        MinecraftServer server = sw.getServer();
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.isVictorious || config.isFailed) {
            gsrArmedTicksInWorld = 0;
            updateCacheInServerWorld(player.getX(), player.getY(), player.getZ());
            return;
        }
        double x = player.getX(), y = player.getY(), z = player.getZ();
        boolean incrementArmed = GSRMovementAutoStartListener.onTick(server, player, x, y, z,
                gsrPrevX, gsrPrevY, gsrPrevZ, gsrArmedTicksInWorld, gsrBaselineFromServerWorld);
        if (incrementArmed) {
            gsrArmedTicksInWorld++;
        } else {
            gsrArmedTicksInWorld = 0;
        }
        updateCacheInServerWorld(x, y, z);

        if (config.startTime > 0 && !config.isVictorious && !config.isFailed && !config.isTimerFrozen) {
            ScreenHandler sh = player.currentScreenHandler;
            if (sh != player.playerScreenHandler && sh != null) {
                try {
                    var id = sw.getRegistryManager().getOrThrow(RegistryKeys.SCREEN_HANDLER).getId(sh.getType());
                    if (id != null) GSRStats.addScreenTime(player.getUuid(), id.toString());
                } catch (Exception ignored) {}
            }
        }
    }

    /** Updates position cache and marks baseline as valid (we are in ServerWorld). */
    @Unique
    private void updateCacheInServerWorld(double x, double y, double z) {
        gsrPrevX = x;
        gsrPrevY = y;
        gsrPrevZ = z;
        gsrInitialized = true;
        gsrBaselineFromServerWorld = true;
    }

    /** Injects at head of onDeath to trigger GSR run death handling. */
    @Inject(method = "onDeath", at = @At("HEAD"))
    private void groupspeedrun$onDeath(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        if (player.getEntityWorld() instanceof ServerWorld sw) {
            MinecraftServer server = sw.getServer();
            if (server != null) GSREvents.handlePlayerDeath(player, server);
        }
    }

    @Unique
    private void updateCache(double x, double y, double z) {
        gsrPrevX = x;
        gsrPrevY = y;
        gsrPrevZ = z;
        gsrInitialized = true;
    }
}
