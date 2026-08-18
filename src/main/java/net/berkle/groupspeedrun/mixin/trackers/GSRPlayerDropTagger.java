package net.berkle.groupspeedrun.mixin.trackers;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tags item entities spawned by the player (dropStack) so pickup tracking
 * can ignore them and only count pearls/rods from loot/mobs.
 */
@Mixin(Player.class)
public class GSRPlayerDropTagger {

    /** Tags dropped items so pickup tracker ignores player-dropped pearls/rods. */
    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("RETURN"))
    private void groupspeedrun$tagDroppedItem(ItemStack stack, boolean dropAround, CallbackInfoReturnable<ItemEntity> cir) {
        ItemEntity entity = cir.getReturnValue();
        if (entity != null) entity.addTag("GSR_PLAYER_DROPPED");
    }
}
