package net.berkle.groupspeedrun.gui.widget;

// Minecraft: GUI, text
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
// GSR: parameters
import net.berkle.groupspeedrun.gui.GSRTickerState;
import net.berkle.groupspeedrun.parameter.GSRRunHistoryParameters;

/**
 * Button that draws sideways-scrolling text when the label overflows and the button is hovered.
 * Used for the pause menu Options / Open to LAN / GSR Options row.
 */
public class GSRTickerButtonWidget extends Button {

    private static final GSRTickerState TICKER = new GSRTickerState();
    private final String tickerKey;

    public GSRTickerButtonWidget(int x, int y, int width, int height, net.minecraft.network.chat.Component message, Button.OnPress onPress, String tickerKey) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        this.tickerKey = tickerKey != null ? tickerKey : "ticker-btn";
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        extractDefaultSprite(context);
        var textRenderer = Minecraft.getInstance().font;
        String label = getMessage().getString();
        int margin = GSRRunHistoryParameters.BUTTON_TICKER_MARGIN;
        int innerLeft = getX() + margin;
        int innerRight = getX() + getWidth() - margin;
        int maxWidth = getWidth() - 2 * margin;
        int textWidth = textRenderer.width(label);
        int textY = getY() + (getHeight() - textRenderer.lineHeight) / 2;

        context.enableScissor(innerLeft, getY(), innerRight, getY() + getHeight());
        int color = active ? GSRRunHistoryParameters.TICKER_ACTIVE_COLOR : GSRRunHistoryParameters.TICKER_INACTIVE_COLOR;
        if (textWidth > maxWidth) {
            if (isHovered) {
                String looping = label + "    " + label;
                int loopWidth = textRenderer.width(looping);
                int maxScroll = loopWidth / 2;
                long elapsed = TICKER.getElapsedMs(tickerKey, System.currentTimeMillis());
                int scrollOffset = (int) ((elapsed / (double) GSRRunHistoryParameters.TICKER_CYCLE_MS) * maxScroll);
                context.text(textRenderer, net.minecraft.network.chat.Component.literal(looping), getX() + margin - scrollOffset, textY, color, true);
            } else {
                String truncated = truncateWithPeriod(textRenderer, label, maxWidth);
                context.text(textRenderer, net.minecraft.network.chat.Component.literal(truncated), getX() + margin, textY, color, true);
            }
        } else {
            context.centeredText(textRenderer, getMessage(), getX() + getWidth() / 2, textY, color);
        }
        context.disableScissor();
    }

    private static String truncateWithPeriod(net.minecraft.client.gui.Font tr, String text, int maxWidth) {
        if (tr.width(text) <= maxWidth) return text;
        int periodW = tr.width(".");
        int available = maxWidth - periodW;
        for (int len = text.length(); len > 0; len--) {
            String s = text.substring(0, len);
            if (tr.width(s) <= available) return s + ".";
        }
        return ".";
    }

    /** Call from PauseScreen render (end of frame) so ticker resets when no scrolling button was drawn. */
    public static void clearTickerIfNotDrawn() {
        TICKER.clearIfNotDrawn();
    }
}
