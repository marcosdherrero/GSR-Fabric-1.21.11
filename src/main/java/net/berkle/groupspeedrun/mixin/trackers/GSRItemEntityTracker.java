package net.berkle.groupspeedrun.mixin.trackers;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.GSRStats;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class GSRItemEntityTracker {

    @Shadow
    public abstract ItemStack getStack();

    /** Injects at insertStack to record pearl/rod pickups for stats; ignores GSR_PLAYER_DROPPED items. */
    @Inject(method = "onPlayerCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerInventory;insertStack(Lnet/minecraft/item/ItemStack;)Z"))
    private void groupspeedrun$onPickup(PlayerEntity player, CallbackInfo ci) {
        if (player.getEntityWorld().isClient() || GSRMain.CONFIG == null || GSRMain.CONFIG.startTime <= 0 || GSRMain.CONFIG.isTimerFrozen) return;
        ItemEntity self = (ItemEntity) (Object) this;
        if (self.getCommandTags().contains("GSR_PLAYER_DROPPED")) return;
        ItemStack stack = getStack();
        if (stack.isEmpty() || self.isRemoved()) return;
        if (stack.isOf(Items.ENDER_PEARL)) GSRStats.addInt(GSRStats.ENDER_PEARLS_COLLECTED, player.getUuid(), stack.getCount());
        if (stack.isOf(Items.BLAZE_ROD)) GSRStats.addInt(GSRStats.BLAZE_RODS_COLLECTED, player.getUuid(), stack.getCount());
    }
}
