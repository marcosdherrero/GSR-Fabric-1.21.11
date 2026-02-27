package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes parent so GSR can check if KeybindsScreen was opened from GSR preferences. */
@Mixin(GameOptionsScreen.class)
public interface GSRGameOptionsScreenAccessor {
    @Accessor("parent")
    Screen gsr$getParent();
}
