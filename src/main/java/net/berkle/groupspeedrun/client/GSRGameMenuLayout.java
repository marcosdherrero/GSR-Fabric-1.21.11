package net.berkle.groupspeedrun.client;

// Minecraft: GUI
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;

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
     * Caller (mixin) must add the returned button via addRenderableWidget.
     */
    public static Button applyLayout(net.minecraft.client.gui.screens.PauseScreen screen) {
        Button exitBtn = ((GSRGameMenuScreenAccessor) screen).gsr$getExitButton();
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
        exitBtn.setSize(halfW, btnH);

        Minecraft client = Minecraft.getInstance();
        return Button.builder(GSRButtonParameters.literal(GSRButtonParameters.TITLE_GSR_CONTROLS),
                        b -> {
                            if (client != null) {
                                client.gui.setScreen(new GSRControlsScreen(screen));
                            }
                        })
                .bounds(rightX, rowY, halfW, btnH)
                .build();
    }

    /**
     * Re-applies layout positions after repositionElements. Use when vanilla layout has run and button positions are final.
     * Repositions exit button and GSR Controls button to match the measured grid gap.
     */
    public static void reapplyLayout(net.minecraft.client.gui.screens.PauseScreen screen) {
        Button exitBtn = ((GSRGameMenuScreenAccessor) screen).gsr$getExitButton();
        if (exitBtn == null) return;

        Button gsrBtn = findGsrControlsButton(screen);
        if (gsrBtn == null) return;

        int leftX = exitBtn.getX();
        int totalW = exitBtn.getWidth();
        int rowY = exitBtn.getY();
        int btnH = exitBtn.getHeight();
        int gap = measureGridColumnGap(screen);
        int halfW = (totalW - gap) / 2;
        int rightX = leftX + halfW + gap;

        exitBtn.setPosition(leftX, rowY);
        exitBtn.setSize(halfW, btnH);
        gsrBtn.setPosition(rightX, rowY);
        gsrBtn.setSize(halfW, btnH);
    }

    private static Button findGsrControlsButton(net.minecraft.client.gui.screens.Screen screen) {
        for (AbstractWidget cw : collectClickableWidgets(screen)) {
            if (cw instanceof Button bw
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
    private static int measureGridColumnGap(net.minecraft.client.gui.screens.PauseScreen screen) {
        List<AbstractWidget> buttons = collectClickableWidgets(screen);
        List<AbstractWidget> buttonList = buttons.stream()
                .filter(Button.class::isInstance)
                .sorted(Comparator.comparingInt(AbstractWidget::getY).thenComparingInt(AbstractWidget::getX))
                .toList();

        int lastY = Integer.MIN_VALUE;
        AbstractWidget prev = null;
        for (AbstractWidget btn : buttonList) {
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

    private static List<AbstractWidget> collectClickableWidgets(GuiEventListener parent) {
        List<AbstractWidget> out = new ArrayList<>();
        collectClickableWidgetsRecursive(parent, out);
        return out;
    }

    private static void collectClickableWidgetsRecursive(GuiEventListener e, List<AbstractWidget> out) {
        if (e instanceof AbstractWidget cw) out.add(cw);
        if (e instanceof ContainerEventHandler pe) {
            for (GuiEventListener child : pe.children()) {
                collectClickableWidgetsRecursive(child, out);
            }
        }
    }
}
