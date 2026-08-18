package net.berkle.groupspeedrun.client;

import net.berkle.groupspeedrun.gui.widget.GSRSquareMenuButton;
import net.berkle.groupspeedrun.parameter.GSRButtonParameters;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.text.Text;

/**
 * Title-screen layout. Adds a square GSR button to the vanilla icon row
 * (language / accessibility) and re-centers that row. Does not resize the main menu buttons.
 */
public final class GSRTitleScreenLayout {

    private GSRTitleScreenLayout() {}

    /** Create square GSR button for title screen. Caller must add the returned button, then apply layout. */
    public static ButtonWidget createControlsButton(net.minecraft.client.MinecraftClient client, Screen screen, int width, int height) {
        ButtonWidget button = new GSRSquareMenuButton(0, 0,
                GSRButtonParameters.literal(GSRButtonParameters.TITLE_GSR_SQUARE),
                btn -> GSRScreens.openControls(screen));
        button.setTooltip(Tooltip.of(Text.literal(GSRButtonParameters.TITLE_GSR_CONTROLS)));
        return button;
    }

    /** Re-center the square icon row after init / refreshWidgetPositions. */
    public static void applyRunHistoryLayout(Screen screen) {
        GSRGameMenuLayout.recenterSquareRow(screen);
    }
}
