package net.berkle.groupspeedrun.mixin.huds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
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
import net.minecraft.world.level.Level;

/**
 * Renders GSR timer and split list. Uses only GSRClient (clientWorldConfig, PLAYER_CONFIG).
 */
@Mixin(Gui.class)
@SuppressWarnings("null")
public class GSRTimerHudMixin {

    /** Injects at end of Gui.render to draw GSR timer and split list. */
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void groupspeedrun$renderTimer(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui || client.level == null) return;

        GSRConfigWorld worldConfig = GSRClient.clientWorldConfig;
        GSRConfigPlayer playerConfig = GSRClient.PLAYER_CONFIG;
        if (worldConfig == null || playerConfig == null) return;

        boolean isFinished = worldConfig.isVictorious || worldConfig.isFailed;
        long currentTime = client.level.getGameTime();
        long ticksSinceSplit = currentTime - worldConfig.lastSplitTime;
        int activeWindow = isFinished ? playerConfig.endShowTicks : playerConfig.splitShowTicks;
        boolean isPriorityWindow = (ticksSinceSplit >= 0 && ticksSinceSplit < activeWindow);

        boolean isLocatorActive = ((worldConfig.fortressLocated && playerConfig.fortressLocatorOn) || (worldConfig.bastionLocated && playerConfig.bastionLocatorOn)) && client.level.dimension() == net.minecraft.world.level.Level.NETHER
                || (worldConfig.strongholdLocated && playerConfig.strongholdLocatorOn) && client.level.dimension() == net.minecraft.world.level.Level.OVERWORLD
                || (worldConfig.shipLocated && playerConfig.shipLocatorOn) && client.level.dimension() == net.minecraft.world.level.Level.END;

        boolean menuPaused = GSRClient.isClientTimerPaused();
        float fadeAlpha = GSRAlphaUtil.getFadeAlpha(client, worldConfig, isFinished, ticksSinceSplit);
        boolean shouldRender = GSRClient.isGsrHudShowActive() || isPriorityWindow || isLocatorActive || menuPaused
                || fadeAlpha > GSRHudParameters.ALPHA_CUTOFF;
        boolean showSplits = (playerConfig.hudMode == GSRConfigPlayer.MODE_FULL) ? true : isPriorityWindow;
        if (isPriorityWindow || isLocatorActive || menuPaused) showSplits = true;

        if (!shouldRender) return;
        if (menuPaused) fadeAlpha = 1.0f;
        if (fadeAlpha <= GSRHudParameters.ALPHA_CUTOFF) return;

        Font tr = client.font;
        int screenW = context.guiWidth();
        int screenH = context.guiHeight();
        int[] size = GSRTimerHudRenderer.getTimerBoxScaledSize(tr, worldConfig, playerConfig, showSplits);
        int scaledH = size[1];
        int anchorX = playerConfig.timerHudOnRight ? (screenW - GSRTimerHudRenderer.EDGE_MARGIN) : GSRTimerHudRenderer.EDGE_MARGIN;
        int anchorY = (int) ((screenH / 2f) - (scaledH / 2f) - (screenH * GSRTimerHudRenderer.VERTICAL_OFFSET_FACTOR));

        GSRTimerHudRenderer.drawTimerBox(context, tr, playerConfig.timerHudOnRight, anchorX, anchorY,
                worldConfig, playerConfig, false, fadeAlpha, showSplits);
    }
}
