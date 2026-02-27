package net.berkle.groupspeedrun.mixin;

/**
 * Placeholder for KeybindsScreen. Scroll-to-GSR-category was removed because the scrollTo accessor
 * broke in Minecraft 1.21.11 (ControlsListWidget/EntryListWidget API change). Keybinds screen opens normally.
 */
@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.gui.screen.option.KeybindsScreen.class)
public abstract class GSRKeybindsScreenMixin {
}
