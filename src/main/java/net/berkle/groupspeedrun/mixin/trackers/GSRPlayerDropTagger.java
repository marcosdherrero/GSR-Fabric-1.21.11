package net.berkle.groupspeedrun.mixin.trackers;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tags item entities spawned by the player (dropStack) so pickup tracking
 * can ignore them and only count pearls/rods from loot/mobs.
 */
@Mixin(PlayerEntity.class)
public class GSRPlayerDropTagger {

    /** Tags dropped items so pickup tracker ignores player-dropped pearls/rods. */
    @Inject(method = "dropStack(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/ItemEntity;", at = @At("RETURN"))
    private void groupspeedrun$tagDroppedItem2(ServerWorld world, ItemStack stack, CallbackInfoReturnable<ItemEntity> cir) {
        ItemEntity entity = cir.getReturnValue();
        if (entity != null) entity.addCommandTag("GSR_PLAYER_DROPPED");
    }

    /** Tags dropped items (yOffset overload) so pickup tracker ignores player-dropped pearls/rods. */
    @Inject(method = "dropStack(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;", at = @At("RETURN"))
    private void groupspeedrun$tagDroppedItem3(ServerWorld world, ItemStack stack, float yOffset, CallbackInfoReturnable<ItemEntity> cir) {
        ItemEntity entity = cir.getReturnValue();
        if (entity != null) entity.addCommandTag("GSR_PLAYER_DROPPED");
    }
}
