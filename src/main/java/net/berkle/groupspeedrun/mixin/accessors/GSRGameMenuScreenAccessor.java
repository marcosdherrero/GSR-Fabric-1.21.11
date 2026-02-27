package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.screen.GameMenuScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the Save and Quit (exit) button and GRID_MARGIN so we can simulate clicking it and match vanilla layout. */
@Mixin(GameMenuScreen.class)
public interface GSRGameMenuScreenAccessor {
    @Accessor("exitButton")
    ButtonWidget gsr$getExitButton();

    @Accessor("GRID_MARGIN")
    int gsr$getGridMargin();
}
