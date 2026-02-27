package net.berkle.groupspeedrun.gui.components;

import net.berkle.groupspeedrun.parameter.GSRButtonParameters;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;
import net.minecraft.client.gui.widget.ButtonWidget;

/**
 * Shared factory for menus and buttons.
 * Ensures consistent styling across GSR screens.
 */
public final class GSRMenuComponents {

    private GSRMenuComponents() {}

    /**
     * Creates a standard menu button with consistent dimensions.
     *
     * @param label   Button label (use GSRButtonParameters constants).
     * @param action  Runnable on click.
     * @param x       Left edge.
     * @param y       Top edge.
     * @param width   Button width.
     * @param height  Button height.
     */
    public static ButtonWidget button(String label, Runnable action, int x, int y, int width, int height) {
        return ButtonWidget.builder(GSRButtonParameters.literal(label), b -> action.run())
                .dimensions(x, y, width, height)
                .build();
    }

    /**
     * Computes footer layout: Back (left) and optional second button (right).
     * Uses same dimensions as main menu Options/Quit.
     */
    public static FooterLayout footerLayout(int screenWidth, int screenHeight) {
        return footerLayout(screenWidth, screenHeight, GSRUiParameters.CONTROLS_FOOTER_Y_OFFSET);
    }

    /**
     * Computes footer layout with custom Y offset from bottom.
     * @param footerYOffset Pixels from bottom of screen to footer row (smaller = buttons closer to bottom).
     */
    public static FooterLayout footerLayout(int screenWidth, int screenHeight, int footerYOffset) {
        int halfW = GSRUiParameters.CONTROLS_HALF_BUTTON_WIDTH;
        int btnH = GSRUiParameters.CONTROLS_BUTTON_HEIGHT;
        int gap = GSRUiParameters.CONTROLS_COL_GAP;
        int footerY = screenHeight - footerYOffset;
        int centerX = screenWidth / 2;
        int leftX = centerX - gap / 2 - halfW;
        int rightX = centerX + gap / 2;
        return new FooterLayout(leftX, rightX, footerY, halfW, btnH);
    }

    /**
     * Computes footer layout for a single centered button (e.g. Back when it is the only footer button).
     */
    public static SingleButtonFooterLayout singleButtonFooterLayout(int screenWidth, int screenHeight) {
        int btnW = GSRUiParameters.FOOTER_BUTTON_WIDTH;
        int btnH = GSRUiParameters.FOOTER_BUTTON_HEIGHT;
        int footerY = screenHeight - GSRUiParameters.CONTROLS_FOOTER_Y_OFFSET;
        int centerX = screenWidth / 2;
        int buttonX = centerX - btnW / 2;
        return new SingleButtonFooterLayout(buttonX, footerY, btnW, btnH);
    }

    public record FooterLayout(int leftX, int rightX, int footerY, int buttonWidth, int buttonHeight) {}
    public record SingleButtonFooterLayout(int buttonX, int footerY, int buttonWidth, int buttonHeight) {}
}
