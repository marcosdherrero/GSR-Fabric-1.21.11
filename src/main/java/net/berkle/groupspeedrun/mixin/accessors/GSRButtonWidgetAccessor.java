package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the PressAction so we can invoke it to simulate a button click. */
@Mixin(ButtonWidget.class)
public interface GSRButtonWidgetAccessor {
    @Accessor("onPress")
    ButtonWidget.PressAction gsr$getOnPress();
}
