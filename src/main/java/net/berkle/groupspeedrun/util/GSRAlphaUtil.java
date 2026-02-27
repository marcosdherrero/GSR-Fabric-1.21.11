package net.berkle.groupspeedrun.util;

import net.minecraft.client.MinecraftClient;
import net.berkle.groupspeedrun.GSRClient;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.parameter.GSRHudParameters;

/**
 * Alpha transitions for GSR HUD: hold-to-show fade (symmetric in/out), toggle fade (symmetric in/out),
 * menu-close fade (same duration as hold release), and event (split/victory) overlay.
 */
public final class GSRAlphaUtil {

    /** Ticks since hold key was pressed (fade in) or released (fade out). Symmetric: +1 when held, -1 when released. */
    private static long holdFadeTicks = 0;
    /** Ticks for toggle mode fade. +1 when toggled on, -1 when toggled off. Symmetric in/out. */
    private static long toggleFadeTicks = 0;
    /** Ticks remaining for menu-close fade. Starts at HOLD_FADE_TICKS when menu closes, decrements each tick. */
    private static int menuCloseFadeTicks = 0;

    private GSRAlphaUtil() {}

    /** Starts the menu-close fade (same duration as hold release). Called when GSR Controls or pause menu closes. */
    public static void startMenuCloseFade() {
        menuCloseFadeTicks = GSRHudParameters.HOLD_FADE_TICKS;
    }

    /** Decrements menu-close fade each client tick. Call from GSRClient tick handler. */
    public static void tick() {
        if (menuCloseFadeTicks > 0) menuCloseFadeTicks--;
    }

    public static float getFadeAlpha(MinecraftClient client, GSRConfigWorld worldConfig, boolean isFinished, long ticksSinceSplit) {
        GSRConfigPlayer playerConfig = GSRClient.PLAYER_CONFIG;
        if (playerConfig == null) return 0.0f;

        int activeWindow = isFinished ? playerConfig.endShowTicks : playerConfig.splitShowTicks;
        float overlayAlpha = 0.0f;
        if (ticksSinceSplit >= 0 && ticksSinceSplit < activeWindow) {
            overlayAlpha = calculateLinearFade(ticksSinceSplit, activeWindow, GSRHudParameters.ANIMATION_FADE_TICKS);
        }

        float baseAlpha;
        if (playerConfig.hudVisibility == GSRConfigPlayer.VISIBILITY_PRESSED) {
            boolean keyHeld = GSRClient.isGsrHudPressActive();
            if (keyHeld) {
                holdFadeTicks = Math.min(holdFadeTicks + 1, GSRHudParameters.HOLD_FADE_TICKS);
            } else {
                holdFadeTicks = Math.max(holdFadeTicks - 1, 0);
            }
            baseAlpha = holdFadeTicks / (float) GSRHudParameters.HOLD_FADE_TICKS;
        } else {
            boolean toggleOn = GSRClient.isGsrHudToggleActive();
            if (toggleOn) {
                toggleFadeTicks = Math.min(toggleFadeTicks + 1, GSRHudParameters.HOLD_FADE_TICKS);
            } else {
                toggleFadeTicks = Math.max(toggleFadeTicks - 1, 0);
            }
            baseAlpha = toggleFadeTicks / (float) GSRHudParameters.HOLD_FADE_TICKS;
        }
        float menuCloseAlpha = menuCloseFadeTicks > 0 ? menuCloseFadeTicks / (float) GSRHudParameters.HOLD_FADE_TICKS : 0f;
        return Math.max(Math.max(baseAlpha, menuCloseAlpha), overlayAlpha);
    }

    private static float calculateLinearFade(long current, int total, int fade) {
        if (current < 0) return 0f;
        if (current < fade) return current / (float) fade;
        if (current > (total - fade)) return Math.max(0f, (total - current) / (float) fade);
        return 1.0f;
    }
}
