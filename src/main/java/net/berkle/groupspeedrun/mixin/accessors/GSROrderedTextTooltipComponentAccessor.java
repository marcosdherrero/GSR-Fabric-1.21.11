package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.gui.screens.inventory.tooltip.ClientTextTooltip;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the FormattedCharSequence from ClientTextTooltip for GSR tooltip wrapping. */
@Mixin(ClientTextTooltip.class)
public interface GSROrderedTextTooltipComponentAccessor {
    @Accessor("text")
    FormattedCharSequence gsr$getText();
}
