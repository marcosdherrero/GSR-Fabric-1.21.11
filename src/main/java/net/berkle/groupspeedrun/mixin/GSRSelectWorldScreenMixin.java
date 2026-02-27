package net.berkle.groupspeedrun.mixin;

import net.berkle.groupspeedrun.GSRClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * When SelectWorldScreen opens and nextGsrWorldName is set (e.g. after "New GSR World" → Save & Quit),
 * immediately open CreateWorldScreen with that name pre-filled so the user can create a new world
 * and reinvite friends.
 */
@Mixin(SelectWorldScreen.class)
public abstract class GSRSelectWorldScreenMixin extends Screen {

    protected GSRSelectWorldScreenMixin() {
        super(null);
    }

    /** Injects at end of init to open CreateWorldScreen when nextGsrWorldName is set. */
    @Inject(method = "init", at = @At("TAIL"))
    private void gsr$openCreateWorldIfNeeded(CallbackInfo ci) {
        if (GSRClient.nextGsrWorldName != null && !GSRClient.nextGsrWorldName.isEmpty() && client != null) {
            SelectWorldScreen self = (SelectWorldScreen) (Object) this;
            CreateWorldScreen.show(client, () -> client.setScreen(self));
        }
    }
}
