package net.berkle.groupspeedrun.mixin.trackers;

import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.timer.listeners.GSRBlockBreakAutoStartListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Tracks blocks broken. Auto-starts the timer on first block break when armed (startTime == -1).
 * Delegates auto-start/resume logic to {@link GSRBlockBreakAutoStartListener}.
 */
@Mixin(ServerPlayerInteractionManager.class)
public class GSRServerPlayerInterTracker {

    @Shadow @Final protected ServerPlayerEntity player;

    @Unique private static final ThreadLocal<Block> gsr$brokenBlock = new ThreadLocal<>();

    /** Capture block state before break (at HEAD, block still in world). */
    @Inject(method = "tryBreakBlock", at = @At("HEAD"))
    private void groupspeedrun$captureBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (player != null && player.getEntityWorld() instanceof ServerWorld sw) {
            gsr$brokenBlock.set(sw.getBlockState(pos).getBlock());
        } else {
            gsr$brokenBlock.set(null);
        }
    }

    /** Injects at return of tryBreakBlock to auto-start timer or track blocks broken. */
    @Inject(method = "tryBreakBlock", at = @At("RETURN"))
    @SuppressWarnings("deprecation")
    private void groupspeedrun$afterBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Block block = gsr$brokenBlock.get();
        gsr$brokenBlock.remove();
        if (!cir.getReturnValue() || player == null || !(player.getEntityWorld() instanceof ServerWorld sw)) return;
        MinecraftServer server = sw.getServer();
        if (server == null) return;
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.isVictorious || config.isFailed) return;
        if (GSRBlockBreakAutoStartListener.onBlockBroken(server, player, block)) return;
        // If not handled, listener already tracked block for stats
    }
}
