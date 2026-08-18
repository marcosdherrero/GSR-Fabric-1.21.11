package net.berkle.groupspeedrun.mixin;

// Minecraft: screen
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// GSR: layout
import net.berkle.groupspeedrun.client.GSRGameMenuLayout;

/**
 * Adds a square GSR button to the pause-menu icon row and re-centers the row.
 */
@Mixin(PauseScreen.class)
public abstract class GSRGameMenuScreenMixin extends Screen {

    protected GSRGameMenuScreenMixin() {
        super(null);
    }

    /** Adds the square GSR button after vanilla icon buttons exist. */
    @Inject(method = "init", at = @At("TAIL"))
    private void gsr$onInit(CallbackInfo ci) {
        PauseScreen self = (PauseScreen) (Object) this;
        var gsrBtn = GSRGameMenuLayout.createSquareButton(self);
        if (gsrBtn != null) {
            addRenderableWidget(gsrBtn);
            GSRGameMenuLayout.reapplyLayout(self);
        }
    }

    /** Re-centers the icon row after vanilla layout runs. */
    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void gsr$onRefreshWidgetPositions(CallbackInfo ci) {
        GSRGameMenuLayout.reapplyLayout((PauseScreen) (Object) this);
    }
}
