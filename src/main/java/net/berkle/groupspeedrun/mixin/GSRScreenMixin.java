package net.berkle.groupspeedrun.mixin;

import net.berkle.groupspeedrun.GSRClient;
import net.berkle.groupspeedrun.client.GSRGameMenuLayout;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Re-centers the title-screen square GSR icon row after Screen.repositionElements.
 * Prefer the square GSR button on pause/title clicks (26.2 getChildAt is first-match).
 */
@Mixin(Screen.class)
public abstract class GSRScreenMixin {

    /** Injects at end of repositionElements to re-apply GSR Run History layout on TitleScreen. */
    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void gsr$onRefreshWidgetPositions(CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (self instanceof TitleScreen && (GSRClient.nextGsrWorldName == null || GSRClient.nextGsrWorldName.isEmpty())) {
            GSRClient.applyRunHistoryLayout(self);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void gsr$preferSquareButton(MouseButtonEvent click, boolean captured, CallbackInfoReturnable<Boolean> cir) {
        Screen self = (Screen) (Object) this;
        if (!(self instanceof PauseScreen) && !(self instanceof TitleScreen)) return;
        if (GSRGameMenuLayout.handleSquareClick(self, click, captured)) {
            cir.setReturnValue(true);
        }
    }
}
