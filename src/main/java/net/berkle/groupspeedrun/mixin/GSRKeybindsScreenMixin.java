package net.berkle.groupspeedrun.mixin;

/**
 * Placeholder for KeyBindsScreen. Scroll-to-GSR-category was removed because the scrollTo accessor
 * broke in Minecraft 1.21.11 (KeyBindsList/EntryListWidget API change). Keybinds screen opens normally.
 */
@org.spongepowered.asm.mixin.Mixin(net.minecraft.client.gui.screens.options.controls.KeyBindsScreen.class)
public abstract class GSRKeybindsScreenMixin {
}
