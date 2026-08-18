package net.berkle.groupspeedrun.mixin.huds;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.DeltaTracker;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.berkle.groupspeedrun.util.GSRLocatorIconHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.berkle.groupspeedrun.GSRClient;
import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.mixin.accessors.GSRBossBarHudAccessor;
import net.berkle.groupspeedrun.parameter.GSRHudParameters;
import net.berkle.groupspeedrun.parameter.GSRLocatorParameters;
import net.berkle.groupspeedrun.util.GSRAlphaUtil;
import net.berkle.groupspeedrun.util.GSRColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
@SuppressWarnings("null")
public class GSRLocateHudMixin {

    /** Injects at end of Gui.render to draw GSR locator bar with structure icons. */
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void groupspeedrun$renderLocateHud(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui || client.level == null) return;

        GSRConfigWorld config = GSRClient.clientWorldConfig;
        if (config == null || config.startTime <= 0) return;

        GSRConfigPlayer pConfig = GSRClient.PLAYER_CONFIG;
        if (pConfig == null) return;

        boolean isFinished = config.isVictorious || config.isFailed;
        long currentTime = client.level.getGameTime();
        long ticksSinceSplit = currentTime - config.lastSplitTime;
        float fadeAlpha = GSRAlphaUtil.getFadeAlpha(client, config, isFinished, ticksSinceSplit);
        if (fadeAlpha <= GSRHudParameters.ALPHA_CUTOFF) return;

        ResourceKey<World> currentDim = client.level.dimension();
        boolean showFortress = config.fortressLocated && pConfig.fortressLocatorOn && currentDim == World.NETHER;
        boolean showBastion = config.bastionLocated && pConfig.bastionLocatorOn && currentDim == World.NETHER;
        boolean showStronghold = config.strongholdLocated && pConfig.strongholdLocatorOn && currentDim == World.OVERWORLD;
        boolean showShip = config.shipLocated && pConfig.shipLocatorOn && currentDim == World.END;

        boolean fading = config.locatorFadeStartTime > 0 && config.locatorFadeType != null && !config.locatorFadeType.isEmpty();
        long fadeElapsed = fading ? currentTime - config.locatorFadeStartTime : 0;
        boolean fadeInProgress = fading && fadeElapsed < GSRLocatorParameters.LOCATOR_FADE_TICKS;
        float fadeOutAlpha = fadeInProgress ? Math.max(0f, 1f - (float) fadeElapsed / GSRLocatorParameters.LOCATOR_FADE_TICKS) : 0f;

        boolean showFadeFortress = fadeInProgress && "fortress".equals(config.locatorFadeType) && currentDim == World.NETHER;
        boolean showFadeBastion = fadeInProgress && "bastion".equals(config.locatorFadeType) && currentDim == World.NETHER;
        boolean showFadeStronghold = fadeInProgress && "stronghold".equals(config.locatorFadeType) && currentDim == World.OVERWORLD;
        boolean showFadeShip = fadeInProgress && "ship".equals(config.locatorFadeType) && currentDim == World.END;

        if (!showFortress && !showBastion && !showStronghold && !showShip && !showFadeFortress && !showFadeBastion && !showFadeStronghold && !showFadeShip) return;

        int centerX = context.guiWidth() / 2;
        int y = pConfig.locateHudOnTop ? GSRLocatorParameters.LOCATE_TOP_Y : context.guiHeight() - GSRLocatorParameters.LOCATE_BOTTOM_OFFSET;

        if (pConfig.locateHudOnTop) {
            var bossBarHud = client.gui.getBossOverlay();
            var activeBars = ((GSRBossBarHudAccessor) bossBarHud).getBossBars();
            if (!activeBars.isEmpty()) {
                y += activeBars.size() * GSRLocatorParameters.BOSS_BAR_ROW_HEIGHT;
            }
        }

        context.pose().pushMatrix();
        context.pose().translate(centerX, y);
        context.pose().scale(pConfig.locateScale, pConfig.locateScale);
        context.pose().translate(-centerX, -y);

        float barAlpha = (showFortress || showBastion || showStronghold || showShip) ? fadeAlpha : fadeAlpha * fadeOutAlpha;
        renderTrackingBar(context, pConfig, centerX, y, barAlpha);

        if (showFortress) renderIcon(context, client, pConfig, centerX, y, config.fortressX, config.fortressZ, GSRLocatorIconHelper.getItemStack(pConfig.fortressItem, Items.BLAZE_ROD), pConfig.fortressColor, fadeAlpha);
        if (showBastion) renderIcon(context, client, pConfig, centerX, y, config.bastionX, config.bastionZ, GSRLocatorIconHelper.getItemStack(pConfig.bastionItem, Items.PIGLIN_HEAD), pConfig.bastionColor, fadeAlpha);
        if (showStronghold) renderIcon(context, client, pConfig, centerX, y, config.strongholdX, config.strongholdZ, GSRLocatorIconHelper.getItemStack(pConfig.strongholdItem, Items.ENDER_EYE), pConfig.strongholdColor, fadeAlpha);
        if (showShip) renderIcon(context, client, pConfig, centerX, y, config.shipX, config.shipZ, GSRLocatorIconHelper.getItemStack(pConfig.shipItem, Items.ELYTRA), pConfig.shipColor, fadeAlpha);

        float iconFadeAlpha = fadeAlpha * fadeOutAlpha;
        if (showFadeFortress) renderIcon(context, client, pConfig, centerX, y, config.fortressX, config.fortressZ, GSRLocatorIconHelper.getItemStack(pConfig.fortressItem, Items.BLAZE_ROD), pConfig.fortressColor, iconFadeAlpha);
        if (showFadeBastion) renderIcon(context, client, pConfig, centerX, y, config.bastionX, config.bastionZ, GSRLocatorIconHelper.getItemStack(pConfig.bastionItem, Items.PIGLIN_HEAD), pConfig.bastionColor, iconFadeAlpha);
        if (showFadeStronghold) renderIcon(context, client, pConfig, centerX, y, config.strongholdX, config.strongholdZ, GSRLocatorIconHelper.getItemStack(pConfig.strongholdItem, Items.ENDER_EYE), pConfig.strongholdColor, iconFadeAlpha);
        if (showFadeShip) renderIcon(context, client, pConfig, centerX, y, config.shipX, config.shipZ, GSRLocatorIconHelper.getItemStack(pConfig.shipItem, Items.ELYTRA), pConfig.shipColor, iconFadeAlpha);

        context.pose().popMatrix();
    }

    @Unique
    private void renderTrackingBar(GuiGraphicsExtractor context, GSRConfigPlayer pConfig, int centerX, int y, float alpha) {
        float scale = pConfig.locateScale;
        int halfW = (int) ((GSRLocatorParameters.DEFAULT_BAR_WIDTH / 2.0) * scale);
        int barH = Math.max(1, (int) (GSRLocatorParameters.DEFAULT_BAR_HEIGHT * scale));
        int x1 = centerX - halfW;
        int x2 = centerX + halfW;
        int y1 = y + GSRLocatorParameters.BAR_Y_OFFSET;

        context.fill(x1, y1, x2, y1 + barH, GSRColorHelper.applyAlpha(GSRLocatorParameters.BAR_BG, GSRLocatorParameters.BAR_BG_ALPHA * alpha));
        renderHorizontalGradient(context, x1, y1, x2, y1 + barH, GSRColorHelper.applyAlpha(GSRLocatorParameters.BAR_GRADIENT_START, alpha), GSRColorHelper.applyAlpha(GSRLocatorParameters.BAR_GRADIENT_END, alpha));

        for (int i = 0; i < 5; i++) {
            float rel = (i / 4.0f) * 2 - 1;
            int tx = centerX + (int) (rel * halfW);
            context.fill(tx, y1, tx + 1, y1 + barH, GSRColorHelper.applyAlpha(GSRLocatorParameters.BAR_TICK_COLOR, alpha * GSRLocatorParameters.BAR_TICK_ALPHA));
        }

        context.fill(x1, y1 - 1, x2, y1, GSRColorHelper.applyAlpha(GSRLocatorParameters.BAR_TOP_BORDER, alpha));
        context.fill(x1, y1 + barH, x2, y1 + barH + 1, GSRColorHelper.applyAlpha(GSRLocatorParameters.BAR_BOTTOM_BORDER, alpha));
    }

    @Unique
    private void renderIcon(GuiGraphicsExtractor context, Minecraft client, GSRConfigPlayer pConfig, int centerX, int y, int tX, int tZ, ItemStack stack, int themeColor, float alpha) {
        float locateScale = pConfig.locateScale;
        float maxOff = ((GSRLocatorParameters.DEFAULT_BAR_WIDTH / 2.0f) * locateScale) - ((float) GSRLocatorParameters.ICON_MARGIN * locateScale);
        int halfW = (int) ((GSRLocatorParameters.DEFAULT_BAR_WIDTH / 2.0) * locateScale);
        float barCenterY = GSRLocatorParameters.computeBarCenterY(y, GSRLocatorParameters.DEFAULT_BAR_HEIGHT, locateScale);

        // Use block center for direction; player uses entity center (getX/Z are fractional)
        double targetX = tX + 0.5;
        double targetZ = tZ + 0.5;
        double dX = targetX - client.player.getX();
        double dZ = targetZ - client.player.getZ();
        double distance = Math.sqrt(dX * dX + dZ * dZ);

        // Forward direction: yaw 0 = South (+Z), 90 = West (-X)
        float yawRad = (float) Math.toRadians(client.player.getYaw());
        double dirX = -Math.sin(yawRad);
        double dirZ = Math.cos(yawRad);
        double dot = dX * dirX + dZ * dirZ;
        double det = dX * dirZ - dZ * dirX;
        float angle = (float) Math.toDegrees(Math.atan2(det, dot));

        // Map angle to bar: positive angle = structure to player's left = icon on left side of bar (negative xOff)
        float normalized = angle / GSRLocatorParameters.ICON_ANGLE_RANGE;
        boolean offScreen = Math.abs(normalized) > 1.0f;

        if (offScreen) {
            // Structure off-screen: show arrow (→/←) at bar edge pointing toward center, same as timer side buttons
            int edgeX = normalized > 0 ? centerX - halfW : centerX + halfW;
            int arrowColor = GSRColorHelper.applyAlpha(themeColor & 0x00FFFFFF, alpha);
            renderOffScreenArrow(context, client, edgeX, (int) barCenterY, normalized > 0, locateScale, arrowColor);
            return;
        }

        float xOff = -Mth.clamp(normalized, -1.0f, 1.0f) * maxOff;
        float drawX = centerX + xOff;
        float drawY = barCenterY;

        // Gradual fade: when looking directly at structure, theme color at full opacity;
        // as you look away, theme fades and gray fill shows through until fully gray when 90° off
        float angleDeg = Math.abs(angle);
        float themeAlpha = Mth.clamp(1.0f - angleDeg / GSRLocatorParameters.ICON_ANGLE_RANGE, 0.0f, 1.0f);
        float grayAlpha = 1.0f - themeAlpha;
        int r = GSRLocatorParameters.ICON_MARGIN;
        int inner = GSRLocatorParameters.ICON_INNER_RADIUS;

        float farThreshold = pConfig.maxScaleDistance;
        float distFactor;
        if (distance >= farThreshold) {
            distFactor = 0f;
        } else if (distance <= GSRLocatorParameters.ICON_SCALE_NEAR_THRESHOLD) {
            distFactor = 1f;
        } else {
            float range = farThreshold - GSRLocatorParameters.ICON_SCALE_NEAR_THRESHOLD;
            distFactor = (float) ((farThreshold - distance) / range);
        }
        float rawScale = Mth.lerp(distFactor, pConfig.minIconScale, 1.0f) * locateScale;
        float dynamicIconScale = Math.min(rawScale, GSRLocatorParameters.ICON_MAX_SCALE_FIT_BOX * locateScale);

        context.pose().pushMatrix();
        context.pose().translate(drawX, drawY);
        context.pose().scale(locateScale, locateScale);

        // Outer box: theme color when looking at structure, gray when looking away
        int rgbTheme = themeColor & 0x00FFFFFF;
        context.fill(-r, -r, r, r, GSRColorHelper.applyAlpha(GSRLocatorParameters.ICON_GRAY_FILL, alpha * grayAlpha));
        context.fill(-r, -r, r, r, GSRColorHelper.applyAlpha(rgbTheme, alpha * themeAlpha));
        context.fill(-inner, -inner, inner, inner, GSRColorHelper.applyAlpha(GSRLocatorParameters.BAR_BG, 1.0f));

        // Item centered in inner box
        context.pose().scale(dynamicIconScale / locateScale, dynamicIconScale / locateScale);
        context.drawItem(stack, -inner, -inner);
        context.pose().popMatrix();
    }

    /** Draws arrow (→/←) at bar edge pointing toward structure when off-screen. Left edge → ←; right edge → →. */
    @Unique
    private void renderOffScreenArrow(GuiGraphicsExtractor context, Minecraft client, int centerX, int centerY, boolean onLeftEdge, float scale, int color) {
        String arrow = onLeftEdge ? "\u2190" : "\u2192"; // ← when structure left, → when structure right
        var tr = client.font;
        int h = tr.lineHeight;
        context.pose().pushMatrix();
        context.pose().translate(centerX, centerY);
        context.pose().scale(scale, scale);
        context.centeredText(tr, arrow, 0, -h / 2, color);
        context.pose().popMatrix();
    }

    @Unique
    private void renderHorizontalGradient(GuiGraphicsExtractor context, int x1, int y1, int x2, int y2, int colorStart, int colorEnd) {
        for (int i = x1; i < x2; i++) {
            float ratio = (float) (i - x1) / (x2 - x1);
            context.fill(i, y1, i + 1, y2, interpolateColor(colorStart, colorEnd, ratio));
        }
    }

    @Unique
    private int interpolateColor(int color1, int color2, float ratio) {
        int a = (int) Mth.lerp(ratio, (color1 >> 24) & 0xFF, (color2 >> 24) & 0xFF);
        int r = (int) Mth.lerp(ratio, (color1 >> 16) & 0xFF, (color2 >> 16) & 0xFF);
        int g = (int) Mth.lerp(ratio, (color1 >> 8) & 0xFF, (color2 >> 8) & 0xFF);
        int b = (int) Mth.lerp(ratio, color1 & 0xFF, color2 & 0xFF);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
