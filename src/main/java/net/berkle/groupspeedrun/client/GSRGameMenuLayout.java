package net.berkle.groupspeedrun.client;

// Minecraft: GUI
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;

// GSR: GUI, mixin accessors, parameters
import net.berkle.groupspeedrun.gui.GSRControlsScreen;
import net.berkle.groupspeedrun.mixin.accessors.GSRGameMenuScreenAccessor;
import net.berkle.groupspeedrun.parameter.GSRButtonParameters;

// Java collections
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pause menu layout. Splits the Save and Quit to Title row into two columns:
 * Save and Quit to Title | GSR Controls. Row width matches the rest of the page (200px).
 * Gap between buttons matches the Advancements | Statistics row.
 */
public final class GSRGameMenuLayout {

    private GSRGameMenuLayout() {}

    /**
     * Applies two-column layout to the exit row. Repositions exit button left; creates and returns GSR Controls button for right column.
     * Uses vanilla grid position and dimensions so Save and Quit | GSR Controls match the buttons above exactly.
     * Gap matches the Advancements | Statistics row by measuring adjacent buttons in the same row.
     * Caller (mixin) must add the returned button via addDrawableChild.
     */
    public static ButtonWidget applyLayout(net.minecraft.client.gui.screen.GameMenuScreen screen) {
        ButtonWidget exitBtn = ((GSRGameMenuScreenAccessor) screen).gsr$getExitButton();
        if (exitBtn == null) return null;

        int leftX = exitBtn.getX();
        int totalW = exitBtn.getWidth();
        int rowY = exitBtn.getY();
        int btnH = exitBtn.getHeight();
        int gap = measureGridColumnGap(screen);
        int halfW = (totalW - gap) / 2;
        int rightX = leftX + halfW + gap;

        exitBtn.setMessage(GSRButtonParameters.literal(GSRButtonParameters.GAME_MENU_SAVE_QUIT));
        exitBtn.setPosition(leftX, rowY);
        exitBtn.setDimensions(halfW, btnH);

        MinecraftClient client = MinecraftClient.getInstance();
        return ButtonWidget.builder(GSRButtonParameters.literal(GSRButtonParameters.TITLE_GSR_CONTROLS),
                        b -> {
                            if (client != null) {
                                client.setScreen(new GSRControlsScreen(screen));
                            }
                        })
                .dimensions(rightX, rowY, halfW, btnH)
                .build();
    }

    /**
     * Re-applies layout positions after refreshWidgetPositions. Use when vanilla layout has run and button positions are final.
     * Repositions exit button and GSR Controls button to match the measured grid gap.
     */
    public static void reapplyLayout(net.minecraft.client.gui.screen.GameMenuScreen screen) {
        ButtonWidget exitBtn = ((GSRGameMenuScreenAccessor) screen).gsr$getExitButton();
        if (exitBtn == null) return;

        ButtonWidget gsrBtn = findGsrControlsButton(screen);
        if (gsrBtn == null) return;

        int leftX = exitBtn.getX();
        int totalW = exitBtn.getWidth();
        int rowY = exitBtn.getY();
        int btnH = exitBtn.getHeight();
        int gap = measureGridColumnGap(screen);
        int halfW = (totalW - gap) / 2;
        int rightX = leftX + halfW + gap;

        exitBtn.setPosition(leftX, rowY);
        exitBtn.setDimensions(halfW, btnH);
        gsrBtn.setPosition(rightX, rowY);
        gsrBtn.setDimensions(halfW, btnH);
    }

    private static ButtonWidget findGsrControlsButton(net.minecraft.client.gui.screen.Screen screen) {
        for (ClickableWidget cw : collectClickableWidgets(screen)) {
            if (cw instanceof ButtonWidget bw
                    && bw.getMessage().getString().equals(GSRButtonParameters.TITLE_GSR_CONTROLS)) {
                return bw;
            }
        }
        return null;
    }

    /**
     * Measures the gap between adjacent buttons in the same row (e.g. Advancements | Statistics).
     * Uses the first such pair found; falls back to GRID_MARGIN if none found.
     */
    private static int measureGridColumnGap(net.minecraft.client.gui.screen.GameMenuScreen screen) {
        List<ClickableWidget> buttons = collectClickableWidgets(screen);
        List<ClickableWidget> buttonList = buttons.stream()
                .filter(ButtonWidget.class::isInstance)
                .sorted(Comparator.comparingInt(ClickableWidget::getY).thenComparingInt(ClickableWidget::getX))
                .toList();

        int lastY = Integer.MIN_VALUE;
        ClickableWidget prev = null;
        for (ClickableWidget btn : buttonList) {
            int y = btn.getY();
            if (y == lastY && prev != null) {
                int gap = btn.getX() - (prev.getX() + prev.getWidth());
                if (gap >= 0) return gap;
            }
            lastY = y;
            prev = btn;
        }
        return ((GSRGameMenuScreenAccessor) screen).gsr$getGridMargin();
    }

    private static List<ClickableWidget> collectClickableWidgets(Element parent) {
        List<ClickableWidget> out = new ArrayList<>();
        collectClickableWidgetsRecursive(parent, out);
        return out;
    }

    private static void collectClickableWidgetsRecursive(Element e, List<ClickableWidget> out) {
        if (e instanceof ClickableWidget cw) out.add(cw);
        if (e instanceof ParentElement pe) {
            for (Element child : pe.children()) {
                collectClickableWidgetsRecursive(child, out);
            }
        }
    }
}
