package net.berkle.groupspeedrun.mixin;

import net.berkle.groupspeedrun.GSRClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Re-applies GSR Run History + Realms layout after Screen.refreshWidgetPositions.
 * Targets Screen (not TitleScreen) because TitleScreen inherits this method.
 */
@Mixin(Screen.class)
public abstract class GSRScreenMixin {

    /** Injects at end of refreshWidgetPositions to re-apply GSR Run History layout on TitleScreen. */
    @Inject(method = "refreshWidgetPositions", at = @At("TAIL"))
    private void gsr$onRefreshWidgetPositions(CallbackInfo ci) {
        Screen self = (Screen) (Object) this;
        if (self instanceof TitleScreen && (GSRClient.nextGsrWorldName == null || GSRClient.nextGsrWorldName.isEmpty())) {
            GSRClient.applyRunHistoryLayout(self);
        }
    }
}
