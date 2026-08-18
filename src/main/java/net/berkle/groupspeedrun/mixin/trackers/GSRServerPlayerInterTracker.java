package net.berkle.groupspeedrun.mixin.trackers;

import net.minecraft.world.level.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.core.BlockPos;
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
@Mixin(ServerPlayerGameMode.class)
public class GSRServerPlayerInterTracker {

    @Shadow @Final protected ServerPlayer player;

    @Unique private static final ThreadLocal<Block> gsr$brokenBlock = new ThreadLocal<>();

    /** Capture block state before break (at HEAD, block still in world). */
    @Inject(method = "destroyBlock", at = @At("HEAD"))
    private void groupspeedrun$captureBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (player != null && player.level() instanceof ServerLevel sw) {
            gsr$brokenBlock.set(sw.getBlockState(pos).getBlock());
        } else {
            gsr$brokenBlock.set(null);
        }
    }

    /** Injects at return of tryBreakBlock to auto-start timer or track blocks broken. */
    @Inject(method = "destroyBlock", at = @At("RETURN"))
    @SuppressWarnings("deprecation")
    private void groupspeedrun$afterBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        Block block = gsr$brokenBlock.get();
        gsr$brokenBlock.remove();
        if (!cir.getReturnValue() || player == null || !(player.level() instanceof ServerLevel sw)) return;
        MinecraftServer server = sw.getServer();
        if (server == null) return;
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.isVictorious || config.isFailed) return;
        if (GSRBlockBreakAutoStartListener.onBlockBroken(server, player, block)) return;
        // If not handled, listener already tracked block for stats
    }
}
