package net.berkle.groupspeedrun.mixin;

import net.berkle.groupspeedrun.GSRClient;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When TitleScreen opens after "New GSR World" save & quit, and nextGsrWorldName is set,
 * automatically open SelectWorldScreen (which triggers CreateWorldScreen with GSR name prefilled).
 * Adds GSR Controls button via addRenderableWidget. GSR Options, Config, and New World are in the Controls screen.
 */
@Mixin(TitleScreen.class)
public abstract class GSRTitleScreenMixin extends Screen {

    protected GSRTitleScreenMixin() {
        super(null);
    }

    /** Injects at end of init to open SelectWorld if nextGsrWorldName set, else add GSR buttons. */
    @Inject(method = "init", at = @At("TAIL"))
    private void gsr$onInit(CallbackInfo ci) {
        if (GSRClient.nextGsrWorldName != null && !GSRClient.nextGsrWorldName.isEmpty() && minecraft != null) {
            TitleScreen self = (TitleScreen) (Object) this;
            minecraft.gui.setScreen(new SelectWorldScreen(self));
        } else {
            TitleScreen self = (TitleScreen) (Object) this;
            Button gsrControlsBtn = GSRClient.createControlsButton(minecraft, self, width, height);
            addRenderableWidget(gsrControlsBtn);
            GSRClient.applyRunHistoryLayout(this);
            if (minecraft != null) {
                minecraft.execute(() -> {
                    GSRClient.applyRunHistoryLayout(this);
                    minecraft.execute(() -> GSRClient.applyRunHistoryLayout(this));
                });
            }
        }
    }
}
