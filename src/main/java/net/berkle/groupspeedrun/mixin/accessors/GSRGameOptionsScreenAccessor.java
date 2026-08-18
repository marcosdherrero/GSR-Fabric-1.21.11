package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes parent so GSR can check if KeyBindsScreen was opened from GSR preferences. */
@Mixin(OptionsSubScreen.class)
public interface GSRGameOptionsScreenAccessor {
    @Accessor("lastScreen")
    Screen gsr$getParent();
}
