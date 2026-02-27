package net.berkle.groupspeedrun.mixin.huds;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.berkle.groupspeedrun.GSRClient;
import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.util.GSRAlphaUtil;
import net.berkle.groupspeedrun.parameter.GSRHudParameters;
import net.berkle.groupspeedrun.timer.hud.GSRTimerHudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Renders GSR timer and split list. Uses only GSRClient (clientWorldConfig, PLAYER_CONFIG).
 */
@Mixin(InGameHud.class)
@SuppressWarnings("null")
public class GSRTimerHudMixin {

    /** Injects at end of InGameHud.render to draw GSR timer and split list. */
    @Inject(method = "render", at = @At("TAIL"))
    private void groupspeedrun$renderTimer(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden || client.world == null) return;

        GSRConfigWorld worldConfig = GSRClient.clientWorldConfig;
        GSRConfigPlayer playerConfig = GSRClient.PLAYER_CONFIG;
        if (worldConfig == null || playerConfig == null) return;

        boolean isFinished = worldConfig.isVictorious || worldConfig.isFailed;
        long currentTime = client.world.getTime();
        long ticksSinceSplit = currentTime - worldConfig.lastSplitTime;
        int activeWindow = isFinished ? playerConfig.endShowTicks : playerConfig.splitShowTicks;
        boolean isPriorityWindow = (ticksSinceSplit >= 0 && ticksSinceSplit < activeWindow);

        boolean isLocatorActive = ((worldConfig.fortressLocated && playerConfig.fortressLocatorOn) || (worldConfig.bastionLocated && playerConfig.bastionLocatorOn)) && client.world.getRegistryKey() == net.minecraft.world.World.NETHER
                || (worldConfig.strongholdLocated && playerConfig.strongholdLocatorOn) && client.world.getRegistryKey() == net.minecraft.world.World.OVERWORLD
                || (worldConfig.shipLocated && playerConfig.shipLocatorOn) && client.world.getRegistryKey() == net.minecraft.world.World.END;

        boolean menuPaused = GSRClient.isClientTimerPaused();
        float fadeAlpha = GSRAlphaUtil.getFadeAlpha(client, worldConfig, isFinished, ticksSinceSplit);
        boolean shouldRender = GSRClient.isGsrHudShowActive() || isPriorityWindow || isLocatorActive || menuPaused
                || fadeAlpha > GSRHudParameters.ALPHA_CUTOFF;
        boolean showSplits = (playerConfig.hudMode == GSRConfigPlayer.MODE_FULL) ? true : isPriorityWindow;
        if (isPriorityWindow || isLocatorActive || menuPaused) showSplits = true;

        if (!shouldRender) return;
        if (menuPaused) fadeAlpha = 1.0f;
        if (fadeAlpha <= GSRHudParameters.ALPHA_CUTOFF) return;

        TextRenderer tr = client.textRenderer;
        int screenW = context.getScaledWindowWidth();
        int screenH = context.getScaledWindowHeight();
        int[] size = GSRTimerHudRenderer.getTimerBoxScaledSize(tr, worldConfig, playerConfig, showSplits);
        int scaledH = size[1];
        int anchorX = playerConfig.timerHudOnRight ? (screenW - GSRTimerHudRenderer.EDGE_MARGIN) : GSRTimerHudRenderer.EDGE_MARGIN;
        int anchorY = (int) ((screenH / 2f) - (scaledH / 2f) - (screenH * GSRTimerHudRenderer.VERTICAL_OFFSET_FACTOR));

        GSRTimerHudRenderer.drawTimerBox(context, tr, playerConfig.timerHudOnRight, anchorX, anchorY,
                worldConfig, playerConfig, false, fadeAlpha, showSplits);
    }
}
