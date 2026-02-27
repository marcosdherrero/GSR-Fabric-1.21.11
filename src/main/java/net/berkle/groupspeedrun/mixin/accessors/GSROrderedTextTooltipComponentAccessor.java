package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the OrderedText from OrderedTextTooltipComponent for GSR tooltip wrapping. */
@Mixin(OrderedTextTooltipComponent.class)
public interface GSROrderedTextTooltipComponentAccessor {
    @Accessor("text")
    OrderedText gsr$getText();
}
