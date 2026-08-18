package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes tooltipDrawer so GSR can defer tooltip rendering for correct z-order. */
@Mixin(GuiGraphicsExtractor.class)
public interface GSRDrawContextAccessor {
    @Accessor("tooltipDrawer")
    void gsr$setTooltipDrawer(Runnable runnable);
}
