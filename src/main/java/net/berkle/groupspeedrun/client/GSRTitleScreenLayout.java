package net.berkle.groupspeedrun.client;

import net.berkle.groupspeedrun.gui.widget.GSRSquareMenuButton;
import net.berkle.groupspeedrun.parameter.GSRButtonParameters;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Title-screen layout. Adds a square GSR button to the vanilla icon row
 * (friends / language / accessibility) and re-centers that row. Does not resize the main menu buttons.
 */
public final class GSRTitleScreenLayout {

    private GSRTitleScreenLayout() {}

    /** Create square GSR button for title screen. Caller must add the returned button, then apply layout. */
    public static Button createControlsButton(net.minecraft.client.Minecraft client, Screen screen, int width, int height) {
        Button button = new GSRSquareMenuButton(0, 0,
                GSRButtonParameters.literal(GSRButtonParameters.TITLE_GSR_SQUARE),
                btn -> GSRScreens.openConfig(screen));
        button.setTooltip(Tooltip.create(Component.literal(GSRButtonParameters.TITLE_GSR_CONFIG)));
        return button;
    }

    /** Re-center the square icon row after init / repositionElements. */
    public static void applyRunHistoryLayout(Screen screen) {
        GSRGameMenuLayout.recenterSquareRow(screen);
    }
}
