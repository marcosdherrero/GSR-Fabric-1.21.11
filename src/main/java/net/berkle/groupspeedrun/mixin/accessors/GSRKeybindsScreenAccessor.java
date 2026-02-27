package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes controlsList so GSR can scroll to the GSR keybind category when opened from preferences. */
@Mixin(KeybindsScreen.class)
public interface GSRKeybindsScreenAccessor {
    @Accessor("controlsList")
    ControlsListWidget gsr$getControlsList();
}
