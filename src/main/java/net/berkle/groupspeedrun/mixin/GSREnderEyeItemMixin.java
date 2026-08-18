package net.berkle.groupspeedrun.mixin;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnderEyeItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Records the first time any player uses (throws) an ender eye this run, for the stronghold locator 30 min gate.
 */
@Mixin(EnderEyeItem.class)
public class GSREnderEyeItemMixin {

    /** Injects at head of use to record first ender eye throw time for stronghold locator gate. */
    @Inject(method = "use(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", at = @At("HEAD"))
    private void groupspeedrun$recordFirstEnderEye(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (world.isClientSide()) return;
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.startTime <= 0 || config.isVictorious || config.isFailed) return;
        if (config.timeFirstEnderEye > 0) return;
        config.timeFirstEnderEye = config.getElapsedTime();
        if (world instanceof net.minecraft.server.level.ServerLevel serverWorld && serverWorld.getServer() != null) {
            config.save(serverWorld.getServer());
        }
    }
}
