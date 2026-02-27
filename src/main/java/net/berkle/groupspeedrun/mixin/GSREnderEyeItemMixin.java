package net.berkle.groupspeedrun.mixin;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderEyeItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
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
    @Inject(method = "use(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at = @At("HEAD"))
    private void groupspeedrun$recordFirstEnderEye(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (world.isClient()) return;
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.startTime <= 0 || config.isVictorious || config.isFailed) return;
        if (config.timeFirstEnderEye > 0) return;
        config.timeFirstEnderEye = config.getElapsedTime();
        if (world instanceof net.minecraft.server.world.ServerWorld serverWorld && serverWorld.getServer() != null) {
            config.save(serverWorld.getServer());
        }
    }
}
