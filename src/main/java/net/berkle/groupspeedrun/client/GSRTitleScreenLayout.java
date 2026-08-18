package net.berkle.groupspeedrun.client;

import net.berkle.groupspeedrun.parameter.GSRButtonParameters;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;

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
    public static Button createControlsButton(net.minecraft.client.Minecraft client, net.minecraft.client.gui.screens.Screen screen, int width, int height) {
        LayoutResult layout = computeLayout(screen, width, height);
        boolean aloneInRow = layout.realmsButton == null;
        int x = aloneInRow ? layout.fullX : layout.gsrX;
        int w = aloneInRow ? layout.fullW : layout.halfW;
        return Button.builder(GSRButtonParameters.literal(GSRButtonParameters.TITLE_GSR_CONTROLS),
                        btn -> {
                            if (client != null) client.setScreen(new net.berkle.groupspeedrun.gui.GSRControlsScreen(screen));
                        })
                .bounds(x, layout.row3Y, w, layout.btnH)
                .build();
    }

    /** Re-apply layout for all main menu buttons. Call after init and repositionElements. */
    public static void applyRunHistoryLayout(net.minecraft.client.gui.screens.Screen screen) {
        LayoutResult layout = computeLayout(screen, screen.width, screen.height);

        if (layout.singleplayerButton != null) {
            layout.singleplayerButton.setPosition(layout.fullX, layout.row1Y);
            layout.singleplayerButton.setSize(layout.fullW, layout.btnH);
        }
        if (layout.multiplayerButton != null) {
            layout.multiplayerButton.setPosition(layout.fullX, layout.row2Y);
            layout.multiplayerButton.setSize(layout.fullW, layout.btnH);
        }
        AbstractWidget gsrBtn = findGsrMainMenuButton(screen);
        boolean row3HasGsr = gsrBtn != null;
        boolean row3HasRealms = layout.realmsButton != null;
        if (gsrBtn != null) {
            // If alone in row, center; else use left column
            int x = (!row3HasRealms) ? layout.fullX : layout.gsrX;
            int w = (!row3HasRealms) ? layout.fullW : layout.halfW;
            gsrBtn.setPosition(x, layout.row3Y);
            gsrBtn.setSize(w, layout.btnH);
        }
        if (layout.realmsButton != null) {
            // If alone in row, center; else use right column
            int x = (!row3HasGsr) ? layout.fullX : layout.realmsX;
            int w = (!row3HasGsr) ? layout.fullW : layout.halfW;
            layout.realmsButton.setPosition(x, layout.row3Y);
            layout.realmsButton.setSize(w, layout.btnH);
        }
        if (layout.modsButton != null) {
            layout.modsButton.setPosition(layout.fullX, layout.row4Y);
            layout.modsButton.setSize(layout.fullW, layout.btnH);
        }
        boolean optionsQuitHasOptions = layout.optionsButton != null;
        boolean optionsQuitHasQuit = layout.quitButton != null;
        if (layout.optionsButton != null) {
            // If alone in row, center; else use left column
            int x = (!optionsQuitHasQuit) ? layout.fullX : layout.optionsX;
            int w = (!optionsQuitHasQuit) ? layout.fullW : layout.halfW;
            layout.optionsButton.setPosition(x, layout.optionsQuitY);
            layout.optionsButton.setSize(w, layout.btnH);
        }
        if (layout.quitButton != null) {
            // If alone in row, center; else use right column
            int x = (!optionsQuitHasOptions) ? layout.fullX : layout.quitX;
            int w = (!optionsQuitHasOptions) ? layout.fullW : layout.halfW;
            layout.quitButton.setPosition(x, layout.optionsQuitY);
            layout.quitButton.setSize(w, layout.btnH);
        }
    }

    private static AbstractWidget findGsrMainMenuButton(net.minecraft.client.gui.screens.Screen screen) {
        return findButtonByLabel(screen, GSRButtonParameters.TITLE_GSR_CONTROLS);
    }

    private static AbstractWidget findButtonByLabel(net.minecraft.client.gui.screens.Screen screen, String label) {
        for (AbstractWidget b : collectClickableWidgets(screen)) {
            if (label.equals(b.getMessage().getString())) return b;
        }
        return null;
    }

    private static LayoutResult computeLayout(net.minecraft.client.gui.screens.Screen screen, int width, int height) {
        List<AbstractWidget> allButtons = collectClickableWidgets(screen);
        AbstractWidget singleplayerButton = findSingleplayerButton(allButtons);
        AbstractWidget multiplayerButton = findMultiplayerButton(allButtons);
        AbstractWidget realmsButton = findRealmsButton(allButtons);
        AbstractWidget modsButton = findModsButton(allButtons);
        AbstractWidget optionsButton = findOptionsButton(allButtons);
        AbstractWidget quitButton = findQuitButton(allButtons);

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

    private static AbstractWidget findSingleplayerButton(List<AbstractWidget> buttons) {
        for (AbstractWidget b : buttons) {
            if (b.getMessage().getString().toLowerCase().contains("single")) return b;
        }
        return null;
    }

    private static AbstractWidget findMultiplayerButton(List<AbstractWidget> buttons) {
        for (AbstractWidget b : buttons) {
            if (b.getMessage().getString().toLowerCase().contains("multi")) return b;
        }
        return null;
    }

    private static AbstractWidget findRealmsButton(List<AbstractWidget> buttons) {
        for (AbstractWidget b : buttons) {
            if (b.getMessage().getString().toLowerCase().contains("realms")) return b;
        }
        return null;
    }

    private static AbstractWidget findModsButton(List<AbstractWidget> buttons) {
        for (AbstractWidget b : buttons) {
            String msg = b.getMessage().getString().toLowerCase();
            if ("mods".equals(msg) || msg.contains("mod menu")) return b;
        }
        return null;
    }

    private static AbstractWidget findOptionsButton(List<AbstractWidget> buttons) {
        for (AbstractWidget b : buttons) {
            if (b.getMessage().getString().toLowerCase().contains("options")) return b;
        }
        return null;
    }

    private static AbstractWidget findQuitButton(List<AbstractWidget> buttons) {
        for (AbstractWidget b : buttons) {
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
            AbstractWidget singleplayerButton,
            AbstractWidget multiplayerButton,
            AbstractWidget realmsButton,
            AbstractWidget modsButton,
            AbstractWidget optionsButton,
            AbstractWidget quitButton
    ) {}

    private static List<AbstractWidget> collectClickableWidgets(net.minecraft.client.gui.screens.Screen screen) {
        List<AbstractWidget> out = new ArrayList<>();
        collectClickableWidgetsRecursive(screen, out);
        return out;
    }

    private static void collectClickableWidgetsRecursive(GuiEventListener parent, List<AbstractWidget> out) {
        if (parent instanceof AbstractWidget cw) {
            out.add(cw);
        }
        if (parent instanceof ContainerEventHandler pe) {
            for (GuiEventListener child : pe.children()) {
                collectClickableWidgetsRecursive(child, out);
            }
        }
    }
}
