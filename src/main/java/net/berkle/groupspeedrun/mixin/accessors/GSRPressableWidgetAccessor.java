package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.gui.widget.PressableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the button textures so Run History tabs and dropdown triggers can use vanilla button style. */
@Mixin(PressableWidget.class)
public interface GSRPressableWidgetAccessor {
    @Accessor("TEXTURES")
    static ButtonTextures gsr$getTextures() {
        throw new AssertionError("Mixin failed to apply");
    }
}
