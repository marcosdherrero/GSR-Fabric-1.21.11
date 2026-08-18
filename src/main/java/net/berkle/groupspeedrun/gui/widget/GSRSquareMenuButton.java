package net.berkle.groupspeedrun.gui.widget;

import net.berkle.groupspeedrun.parameter.GSRButtonParameters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/**
 * Square menu button with the letters GSR scaled to fit, matching 26.2 pause/title icon-row size (20×20).
 */
public class GSRSquareMenuButton extends Button {

    public static final int SIZE = 20;

    public GSRSquareMenuButton(int x, int y, Component message, Button.OnPress onPress) {
        super(x, y, SIZE, SIZE, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        extractDefaultSprite(context);
        Font font = Minecraft.getInstance().font;
        String label = getMessage().getString();
        int padding = GSRButtonParameters.SQUARE_BUTTON_LABEL_PADDING;
        int maxW = Math.max(1, getWidth() - padding * 2);
        int maxH = Math.max(1, getHeight() - padding * 2);
        float scale = Math.min(maxW / (float) Math.max(1, font.width(label)), maxH / (float) font.lineHeight);
        scale = Math.min(scale, 1.0f);

        int centerX = getX() + getWidth() / 2;
        int centerY = getY() + getHeight() / 2;
        var matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.scale(scale, scale);
        int color = active ? GSRButtonParameters.TITLE_BUTTON_TEXT_ACTIVE : GSRButtonParameters.TITLE_BUTTON_TEXT_INACTIVE;
        context.centeredText(font, getMessage(), 0, -font.lineHeight / 2, color);
        matrices.popMatrix();
    }
}
