package net.berkle.groupspeedrun.gui.runhistory;

import net.berkle.groupspeedrun.util.GSRSectionCodeUtil;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Renders run info text with Minecraft section sign (§) color codes.
 * Delegates to {@link GSRSectionCodeUtil}.
 */
public final class GSRRunHistoryTextRenderer {

    private GSRRunHistoryTextRenderer() {}

    public static void drawLineWithSectionColors(DrawContext context, TextRenderer textRenderer, String line, int x, int y) {
        GSRSectionCodeUtil.drawLineWithSectionColors(context, textRenderer, line, x, y);
    }
}
