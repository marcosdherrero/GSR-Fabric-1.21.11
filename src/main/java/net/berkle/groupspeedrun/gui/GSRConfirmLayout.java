package net.berkle.groupspeedrun.gui;

import net.berkle.groupspeedrun.parameter.GSRUiParameters;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/**
 * Centered, wrapping confirmation copy. Width tracks ~2/3 of the screen (or the remaining
 * padded width), never shrinking below the vanilla dialog line length.
 */
public final class GSRConfirmLayout {

    private GSRConfirmLayout() {}

    /** Max wrap width: ~2/3 of the screen, capped to a side margin, at least vanilla dialog width. */
    public static int wrapWidth(int screenWidth) {
        int twoThirds = Math.max(1, (screenWidth * 2) / 3);
        int padded = Math.max(1, screenWidth - GSRUiParameters.CONFIRM_MESSAGE_SIDE_MARGIN * 2);
        int minReadable = Math.min(padded, GSRUiParameters.RESET_CONFIRM_MESSAGE_MAX_WIDTH);
        return Math.min(padded, Math.max(minReadable, twoThirds));
    }

    public static List<FormattedCharSequence> wrap(Font font, String message, int screenWidth) {
        return font.split(Component.literal(message), wrapWidth(screenWidth));
    }

    /**
     * Draws wrapped lines centered on {@code centerX}. Returns the Y of the last line's baseline
     * plus spacing so buttons can sit below the block.
     */
    public static int drawCenteredWrapped(GuiGraphicsExtractor context, Font font, String message,
            int centerX, int topY, int screenWidth, int color) {
        List<FormattedCharSequence> lines = wrap(font, message, screenWidth);
        int lineHeight = font.lineHeight + GSRUiParameters.CONFIRM_MESSAGE_LINE_GAP;
        for (int i = 0; i < lines.size(); i++) {
            context.centeredText(font, lines.get(i), centerX, topY + i * lineHeight, color);
        }
        return topY + lines.size() * lineHeight;
    }

    /** Vertical space used by the wrapped message. */
    public static int messageHeight(Font font, String message, int screenWidth) {
        int lines = Math.max(1, wrap(font, message, screenWidth).size());
        return lines * (font.lineHeight + GSRUiParameters.CONFIRM_MESSAGE_LINE_GAP);
    }

    /** Button row Y: below the wrapped message, still near vertical center. */
    public static int buttonY(Font font, String message, int screenWidth, int screenHeight) {
        int msgH = messageHeight(font, message, screenWidth);
        int msgTop = screenHeight / 2 - GSRUiParameters.RESET_CONFIRM_MESSAGE_OFFSET - msgH / 2;
        int belowMsg = msgTop + msgH + GSRUiParameters.CONFIRM_BUTTON_GAP_ABOVE;
        int fallback = screenHeight / 2 + GSRUiParameters.CONTROLS_PADDING;
        return Math.max(fallback, belowMsg);
    }

    /** Title above a centered wrapped message. */
    public static void drawTitleAndMessage(GuiGraphicsExtractor context, Font font, Component title, String message,
            int screenWidth, int screenHeight) {
        int centerX = screenWidth / 2;
        int msgH = messageHeight(font, message, screenWidth);
        int msgTop = screenHeight / 2 - GSRUiParameters.RESET_CONFIRM_MESSAGE_OFFSET - msgH / 2;
        int titleY = Math.min(screenHeight / 2 - GSRUiParameters.RESET_CONFIRM_TITLE_OFFSET, msgTop - font.lineHeight - 6);
        context.centeredText(font, title, centerX, titleY, GSRUiParameters.NEW_WORLD_TITLE_COLOR);
        drawCenteredWrapped(context, font, message, centerX, msgTop, screenWidth, GSRUiParameters.NEW_WORLD_LINE1_COLOR);
    }
}
