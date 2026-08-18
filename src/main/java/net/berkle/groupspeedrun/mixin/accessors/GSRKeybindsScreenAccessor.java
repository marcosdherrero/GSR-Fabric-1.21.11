package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes controlsList so GSR can scroll to the GSR keybind category when opened from preferences. */
@Mixin(KeyBindsScreen.class)
public interface GSRKeybindsScreenAccessor {
    @Accessor("controlsList")
    KeyBindsList gsr$getControlsList();
}
