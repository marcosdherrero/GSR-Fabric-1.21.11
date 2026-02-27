package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.util.Identifier;
import net.minecraft.client.gui.widget.ScrollableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes scrollbar textures and width so GSR can draw keybinds-style scrollbars. */
@Mixin(ScrollableWidget.class)
public interface GSRScrollableWidgetAccessor {
    @Accessor("SCROLLER_TEXTURE")
    static Identifier gsr$getScrollerTexture() {
        throw new AssertionError("Mixin failed to apply");
    }

    @Accessor("SCROLLER_BACKGROUND_TEXTURE")
    static Identifier gsr$getScrollerBackgroundTexture() {
        throw new AssertionError("Mixin failed to apply");
    }

    @Accessor("SCROLLBAR_WIDTH")
    static int gsr$getScrollbarWidth() {
        throw new AssertionError("Mixin failed to apply");
    }
}
