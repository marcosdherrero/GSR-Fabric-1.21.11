package net.berkle.groupspeedrun.mixin;

// Minecraft: screen
import net.minecraft.client.gui.screen.GameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// GSR: layout
import net.berkle.groupspeedrun.client.GSRGameMenuLayout;

/**
 * Splits the Save and Quit to Title row into two columns: Save and Quit to Title | GSR Controls.
 * Row width matches the rest of the pause menu.
 */
@Mixin(GameMenuScreen.class)
public abstract class GSRGameMenuScreenMixin extends Screen {

    protected GSRGameMenuScreenMixin() {
        super(null);
    }

    /** Splits exit row into Save and Quit | GSR Controls; adds GSR button. */
    @Inject(method = "init", at = @At("TAIL"))
    private void gsr$onInit(CallbackInfo ci) {
        GameMenuScreen self = (GameMenuScreen) (Object) this;
        var gsrBtn = GSRGameMenuLayout.applyLayout(self);
        if (gsrBtn != null) {
            addDrawableChild(gsrBtn);
        }
    }

    /** Re-applies Save and Quit | GSR Controls layout after vanilla layout runs, so gap matches Advancements | Statistics. */
    @Inject(method = "refreshWidgetPositions", at = @At("TAIL"))
    private void gsr$onRefreshWidgetPositions(CallbackInfo ci) {
        GSRGameMenuLayout.reapplyLayout((GameMenuScreen) (Object) this);
    }
}
