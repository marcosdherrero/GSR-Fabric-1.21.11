package net.berkle.groupspeedrun.mixin.trackers;

import net.berkle.groupspeedrun.server.GSRSharedHealthEatAllowance;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects at end of addExhaustion to record activity for shared health eat allowance.
 */
@Mixin(Player.class)
public abstract class GSRPlayerExhaustionTracker {

    /** Injects at end of addExhaustion to credit eat allowance for shared health players. */
    @Inject(method = "causeFoodExhaustion", at = @At("TAIL"))
    private void groupspeedrun$onAddExhaustion(float exhaustion, CallbackInfo ci) {
        Player self = (Player) (Object) this;
        if (self instanceof ServerPlayer player) {
            GSRSharedHealthEatAllowance.recordExhaustion(player, exhaustion);
        }
    }
}
