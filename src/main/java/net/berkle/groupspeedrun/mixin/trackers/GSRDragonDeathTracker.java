package net.berkle.groupspeedrun.mixin.trackers;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.server.MinecraftServer;
import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.managers.GSRSplitManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * GSRDragonDeathTracker: On dragon mechanical death (1HP), call completeDragon and save.
 */
@Mixin(EnderDragonEntity.class)
public abstract class GSRDragonDeathTracker {

    @Unique
    private static final float MECHANICAL_DEATH_THRESHOLD = 1.0f;

    /** Injects at head of tickMovement to detect dragon mechanical death (1HP) and complete run. */
    @Inject(method = "tickMovement", at = @At("HEAD"))
    private void groupspeedrun$onDragonTick(CallbackInfo ci) {
        EnderDragonEntity dragon = (EnderDragonEntity) (Object) this;
        if (dragon.getEntityWorld().isClient() || GSRMain.CONFIG == null) return;
        if (dragon.getHealth() > MECHANICAL_DEATH_THRESHOLD) return;
        if (GSRMain.CONFIG.isVictorious || GSRMain.CONFIG.isFailed || GSRMain.CONFIG.startTime <= 0) return;

        MinecraftServer server = dragon.getEntityWorld().getServer();
        if (server != null) {
            GSRSplitManager.completeDragon(server);
        }
    }
}
