package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.AbstractButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the button textures so Run History tabs and dropdown triggers can use vanilla button style. */
@Mixin(AbstractButton.class)
public interface GSRPressableWidgetAccessor {
    @Accessor("SPRITES")
    static WidgetSprites gsr$getTextures() {
        throw new AssertionError("Mixin failed to apply");
    }
}
