package net.berkle.groupspeedrun.mixin.trackers;

import net.berkle.groupspeedrun.GSRStats;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.InteractionResult;
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
    @Inject(method = "place(Lnet/minecraft/world/item/context/BlockPlaceContext;)Lnet/minecraft/world/InteractionResult;", at = @At("RETURN"))
    @SuppressWarnings("deprecation")
    private void groupspeedrun$onPlace(BlockPlaceContext context, CallbackInfoReturnable<InteractionResult> cir) {
        var player = context.getPlayer();
        if (player != null && cir.getReturnValue().consumesAction() && !context.getLevel().isClientSide()) {
            BlockItem self = (BlockItem) (Object) this;
            String blockId = self.getBlock().builtInRegistryHolder().unwrapKey().map(k -> k.identifier().toString()).orElse(null);
            GSRStats.addBlockPlaced(player.getUUID(), blockId);
        }
    }
}
