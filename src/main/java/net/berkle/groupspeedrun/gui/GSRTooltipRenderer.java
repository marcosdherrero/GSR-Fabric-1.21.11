package net.berkle.groupspeedrun.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipPositioner;
import net.minecraft.text.OrderedText;

import java.util.List;

/**
 * Facade for standardized tooltips. Delegates to {@link GSRStandardTooltip}.
 * Used by the DrawContext mixin for options menus, config screens, and buttons.
 */
public final class GSRTooltipRenderer {

    private GSRTooltipRenderer() {}

    /**
     * Draws a tooltip with standardized 35% max size, 2.5s scroll delay, cyclical scroll, and divider bar.
     */
    public static void drawTooltipWithMaxSizeAndScroll(DrawContext context, TextRenderer textRenderer,
                                                       List<OrderedText> lines,
                                                       TooltipPositioner positioner,
                                                       int mouseX, int mouseY,
                                                       int screenWidth, int screenHeight) {
        String key = GSRStandardTooltip.buildTooltipKey(lines);
        long elapsed = GSRGlobalTooltipState.getTooltipElapsedMs(key, System.currentTimeMillis());
        GSRStandardTooltip.draw(context, textRenderer, lines, positioner, mouseX, mouseY, screenWidth, screenHeight, elapsed);
    }
}
