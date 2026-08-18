package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the Save and Quit (exit) button and GRID_MARGIN so we can simulate clicking it and match vanilla layout. */
@Mixin(PauseScreen.class)
public interface GSRGameMenuScreenAccessor {
    @Accessor("exitButton")
    Button gsr$getExitButton();

    @Accessor("GRID_MARGIN")
    int gsr$getGridMargin();
}
