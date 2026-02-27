package net.berkle.groupspeedrun.client;

import net.berkle.groupspeedrun.parameter.GSRButtonParameters;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;

import java.util.ArrayList;
import java.util.List;

/**
 * Main menu layout. Deterministic positioning from config parameters.
 * Layout: AA Singleplayer | BB Multiplayer | CD GSR Controls | Realms | EE Mods (optional) | Options | Quit
 * GSR Controls opens the GSR menu (Options, Config, New World). Language and accessibility buttons remain in vanilla positions.
 */
public final class GSRTitleScreenLayout {

    private GSRTitleScreenLayout() {}

    /** Create GSR Controls button for title screen. Caller must add the returned button.
     * If alone in row (Realms absent), uses full width and centers. */
    public static ButtonWidget createControlsButton(net.minecraft.client.MinecraftClient client, net.minecraft.client.gui.screen.Screen screen, int width, int height) {
        LayoutResult layout = computeLayout(screen, width, height);
        boolean aloneInRow = layout.realmsButton == null;
        int x = aloneInRow ? layout.fullX : layout.gsrX;
        int w = aloneInRow ? layout.fullW : layout.halfW;
        return ButtonWidget.builder(GSRButtonParameters.literal(GSRButtonParameters.TITLE_GSR_CONTROLS),
                        btn -> {
                            if (client != null) client.setScreen(new net.berkle.groupspeedrun.gui.GSRControlsScreen(screen));
                        })
                .dimensions(x, layout.row3Y, w, layout.btnH)
                .build();
    }

    /** Re-apply layout for all main menu buttons. Call after init and refreshWidgetPositions. */
    public static void applyRunHistoryLayout(net.minecraft.client.gui.screen.Screen screen) {
        LayoutResult layout = computeLayout(screen, screen.width, screen.height);

        if (layout.singleplayerButton != null) {
            layout.singleplayerButton.setPosition(layout.fullX, layout.row1Y);
            layout.singleplayerButton.setDimensions(layout.fullW, layout.btnH);
        }
        if (layout.multiplayerButton != null) {
            layout.multiplayerButton.setPosition(layout.fullX, layout.row2Y);
            layout.multiplayerButton.setDimensions(layout.fullW, layout.btnH);
        }
        ClickableWidget gsrBtn = findGsrMainMenuButton(screen);
        boolean row3HasGsr = gsrBtn != null;
        boolean row3HasRealms = layout.realmsButton != null;
        if (gsrBtn != null) {
            // If alone in row, center; else use left column
            int x = (!row3HasRealms) ? layout.fullX : layout.gsrX;
            int w = (!row3HasRealms) ? layout.fullW : layout.halfW;
            gsrBtn.setPosition(x, layout.row3Y);
            gsrBtn.setDimensions(w, layout.btnH);
        }
        if (layout.realmsButton != null) {
            // If alone in row, center; else use right column
            int x = (!row3HasGsr) ? layout.fullX : layout.realmsX;
            int w = (!row3HasGsr) ? layout.fullW : layout.halfW;
            layout.realmsButton.setPosition(x, layout.row3Y);
            layout.realmsButton.setDimensions(w, layout.btnH);
        }
        if (layout.modsButton != null) {
            layout.modsButton.setPosition(layout.fullX, layout.row4Y);
            layout.modsButton.setDimensions(layout.fullW, layout.btnH);
        }
        boolean optionsQuitHasOptions = layout.optionsButton != null;
        boolean optionsQuitHasQuit = layout.quitButton != null;
        if (layout.optionsButton != null) {
            // If alone in row, center; else use left column
            int x = (!optionsQuitHasQuit) ? layout.fullX : layout.optionsX;
            int w = (!optionsQuitHasQuit) ? layout.fullW : layout.halfW;
            layout.optionsButton.setPosition(x, layout.optionsQuitY);
            layout.optionsButton.setDimensions(w, layout.btnH);
        }
        if (layout.quitButton != null) {
            // If alone in row, center; else use right column
            int x = (!optionsQuitHasOptions) ? layout.fullX : layout.quitX;
            int w = (!optionsQuitHasOptions) ? layout.fullW : layout.halfW;
            layout.quitButton.setPosition(x, layout.optionsQuitY);
            layout.quitButton.setDimensions(w, layout.btnH);
        }
    }

    private static ClickableWidget findGsrMainMenuButton(net.minecraft.client.gui.screen.Screen screen) {
        return findButtonByLabel(screen, GSRButtonParameters.TITLE_GSR_CONTROLS);
    }

    private static ClickableWidget findButtonByLabel(net.minecraft.client.gui.screen.Screen screen, String label) {
        for (ClickableWidget b : collectClickableWidgets(screen)) {
            if (label.equals(b.getMessage().getString())) return b;
        }
        return null;
    }

    private static LayoutResult computeLayout(net.minecraft.client.gui.screen.Screen screen, int width, int height) {
        List<ClickableWidget> allButtons = collectClickableWidgets(screen);
        ClickableWidget singleplayerButton = findSingleplayerButton(allButtons);
        ClickableWidget multiplayerButton = findMultiplayerButton(allButtons);
        ClickableWidget realmsButton = findRealmsButton(allButtons);
        ClickableWidget modsButton = findModsButton(allButtons);
        ClickableWidget optionsButton = findOptionsButton(allButtons);
        ClickableWidget quitButton = findQuitButton(allButtons);

        int fullW = GSRUiParameters.TITLE_FULL_BUTTON_WIDTH;
        int halfW = GSRUiParameters.TITLE_HALF_BUTTON_WIDTH;
        int btnH = GSRUiParameters.TITLE_BUTTON_HEIGHT;
        int gap = GSRUiParameters.TITLE_BUTTON_GAP;
        int rowGap = GSRUiParameters.TITLE_BUTTON_ROW_GAP;
        int centerX = width / 2;
        int fallbackBaseY = height / 2 + GSRUiParameters.TITLE_SCREEN_BUTTON_Y_OFFSET;

        int fullX = centerX - fullW / 2;
        int gsrX = centerX - gap / 2 - halfW;
        int realmsX = centerX + gap / 2;
        int optionsX = gsrX;
        int quitX = realmsX;

        // Singleplayer keeps vanilla Y; remaining buttons in block are stacked below it
        int row1Y = singleplayerButton != null ? singleplayerButton.getY() : fallbackBaseY;
        int row2Y = row1Y + btnH + rowGap;
        int row3Y = row2Y + btnH + rowGap;
        int row4Y = row3Y + btnH + rowGap;
        // Options and Quit keep vanilla Y position (from bottom); only X and width are adjusted
        int optionsQuitY = optionsButton != null ? optionsButton.getY() : (height - GSRUiParameters.TITLE_OPTIONS_QUIT_Y_OFFSET);

        return new LayoutResult(
                fullW, halfW, btnH, fullX, gsrX, realmsX, optionsX, quitX,
                row1Y, row2Y, row3Y, row4Y, optionsQuitY,
                singleplayerButton, multiplayerButton, realmsButton, modsButton, optionsButton, quitButton
        );
    }

    private static ClickableWidget findSingleplayerButton(List<ClickableWidget> buttons) {
        for (ClickableWidget b : buttons) {
            if (b.getMessage().getString().toLowerCase().contains("single")) return b;
        }
        return null;
    }

    private static ClickableWidget findMultiplayerButton(List<ClickableWidget> buttons) {
        for (ClickableWidget b : buttons) {
            if (b.getMessage().getString().toLowerCase().contains("multi")) return b;
        }
        return null;
    }

    private static ClickableWidget findRealmsButton(List<ClickableWidget> buttons) {
        for (ClickableWidget b : buttons) {
            if (b.getMessage().getString().toLowerCase().contains("realms")) return b;
        }
        return null;
    }

    private static ClickableWidget findModsButton(List<ClickableWidget> buttons) {
        for (ClickableWidget b : buttons) {
            String msg = b.getMessage().getString().toLowerCase();
            if ("mods".equals(msg) || msg.contains("mod menu")) return b;
        }
        return null;
    }

    private static ClickableWidget findOptionsButton(List<ClickableWidget> buttons) {
        for (ClickableWidget b : buttons) {
            if (b.getMessage().getString().toLowerCase().contains("options")) return b;
        }
        return null;
    }

    private static ClickableWidget findQuitButton(List<ClickableWidget> buttons) {
        for (ClickableWidget b : buttons) {
            if (b.getMessage().getString().toLowerCase().contains("quit")) return b;
        }
        return null;
    }

    private record LayoutResult(
            int fullW,
            int halfW,
            int btnH,
            int fullX,
            int gsrX,
            int realmsX,
            int optionsX,
            int quitX,
            int row1Y,
            int row2Y,
            int row3Y,
            int row4Y,
            int optionsQuitY,
            ClickableWidget singleplayerButton,
            ClickableWidget multiplayerButton,
            ClickableWidget realmsButton,
            ClickableWidget modsButton,
            ClickableWidget optionsButton,
            ClickableWidget quitButton
    ) {}

    private static List<ClickableWidget> collectClickableWidgets(net.minecraft.client.gui.screen.Screen screen) {
        List<ClickableWidget> out = new ArrayList<>();
        collectClickableWidgetsRecursive(screen, out);
        return out;
    }

    private static void collectClickableWidgetsRecursive(Element parent, List<ClickableWidget> out) {
        if (parent instanceof ClickableWidget cw) {
            out.add(cw);
        }
        if (parent instanceof ParentElement pe) {
            for (Element child : pe.children()) {
                collectClickableWidgetsRecursive(child, out);
            }
        }
    }
}
