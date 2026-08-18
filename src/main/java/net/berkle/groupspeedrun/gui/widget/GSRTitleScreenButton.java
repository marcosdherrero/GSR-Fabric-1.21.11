package net.berkle.groupspeedrun.gui.widget;

import net.berkle.groupspeedrun.parameter.GSRButtonParameters;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;

/**
 * Button for title screen with internal padding and scaled text so labels fit without clipping.
 * Used for GSR Run History and aligned with the shrunk Realms button.
 */
public class GSRTitleScreenButton extends Button.Text {

    public GSRTitleScreenButton(int x, int y, int width, int height, net.minecraft.network.chat.Component message, PressAction onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION_SUPPLIER);
    }

    @Override
    protected void drawIcon(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        drawLabelWithPaddingAndScale(context);
    }

    /** Draw label with padding and reduced scale so text fits. */
    private void drawLabelWithPaddingAndScale(GuiGraphicsExtractor context) {
        float scale = GSRButtonParameters.TITLE_BUTTON_TEXT_SCALE;
        int centerX = getX() + getWidth() / 2;
        int centerY = getY() + getHeight() / 2;

        var matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(centerX, centerY);
        matrices.scale(scale, scale);
        matrices.translate(-centerX, -centerY);

        int color = active ? GSRButtonParameters.TITLE_BUTTON_TEXT_ACTIVE : GSRButtonParameters.TITLE_BUTTON_TEXT_INACTIVE;
        context.centeredText(
            Minecraft.getInstance().font,
            getMessage(),
            centerX,
            centerY - 4,
            color
        );
        matrices.popMatrix();
    }
}
