package net.berkle.groupspeedrun.mixin.trackers;

import net.berkle.groupspeedrun.GSRStats;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tracks blocks placed. Phase 2.
 */
@Mixin(BlockItem.class)
public abstract class GSRBlockItemTracker {

    /** Injects at return of place to record blocks placed for stats (overall + per-type for most-placed). */
    @Inject(method = "place(Lnet/minecraft/item/ItemPlacementContext;)Lnet/minecraft/util/ActionResult;", at = @At("RETURN"))
    @SuppressWarnings("deprecation")
    private void groupspeedrun$onPlace(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> cir) {
        var player = context.getPlayer();
        if (player != null && cir.getReturnValue().isAccepted() && !context.getWorld().isClient()) {
            BlockItem self = (BlockItem) (Object) this;
            String blockId = self.getBlock().getRegistryEntry().getKey().map(k -> k.getValue().toString()).orElse(null);
            GSRStats.addBlockPlaced(player.getUuid(), blockId);
        }
    }
}
