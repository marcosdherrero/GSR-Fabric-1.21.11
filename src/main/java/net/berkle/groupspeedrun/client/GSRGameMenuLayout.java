package net.berkle.groupspeedrun.client;

// Minecraft: GUI
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

// GSR: GUI, mixin accessors, parameters
import net.berkle.groupspeedrun.gui.widget.GSRSquareMenuButton;
import net.berkle.groupspeedrun.parameter.GSRButtonParameters;

// Java collections
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pause menu layout. Adds a square GSR button to the vanilla 20×20 icon row
 * (report / accessibility / friends / etc.) and re-centers the whole row with equal gaps.
 */
public final class GSRGameMenuLayout {

    private static final int SQUARE_SIZE = GSRSquareMenuButton.SIZE;
    private static final int SQUARE_GAP = 4;

    private GSRGameMenuLayout() {}

    /**
     * Creates the square GSR button (caller adds it via addRenderableWidget, then calls reapplyLayout).
     */
    public static Button createSquareButton(PauseScreen screen) {
        Button button = new GSRSquareMenuButton(0, 0,
                GSRButtonParameters.literal(GSRButtonParameters.TITLE_GSR_SQUARE),
                b -> GSRScreens.openControls(screen));
        button.setTooltip(Tooltip.create(Component.literal(GSRButtonParameters.TITLE_GSR_CONTROLS)));
        return button;
    }

    /**
     * 26.2 {@code getChildAt} returns the first overlapping child, so a vanilla icon or
     * full-width layout cell can steal the square GSR hit. Prefer the GSR button when the
     * cursor is on it.
     */
    public static boolean handleSquareClick(net.minecraft.client.gui.screens.Screen screen, MouseButtonEvent click, boolean captured) {
        AbstractWidget gsr = findGsrSquareButton(screen);
        if (gsr == null || !gsr.isMouseOver(click.x(), click.y())) return false;
        return gsr.mouseClicked(click, captured);
    }

    /**
     * Inserts GSR into the existing square-button row and re-centers the group with vanilla spacing.
     */
    public static void reapplyLayout(PauseScreen screen) {
        recenterSquareRow(screen);
    }

    static void recenterSquareRow(net.minecraft.client.gui.screens.Screen screen) {
        List<AbstractWidget> vanilla = collectSquareRow(screen);
        if (vanilla.isEmpty()) return;
        vanilla.sort(Comparator.comparingInt(AbstractWidget::getX));

        AbstractWidget gsr = findGsrSquareButton(screen);
        int size = SQUARE_SIZE;
        int gap = measureSquareGap(vanilla);
        int minX = vanilla.get(0).getX();
        int maxX = vanilla.get(vanilla.size() - 1).getX() + vanilla.get(vanilla.size() - 1).getWidth();
        int centerX = (minX + maxX) / 2;
        int y = vanilla.get(0).getY();

        List<AbstractWidget> row = new ArrayList<>(vanilla);
        if (gsr != null && !row.contains(gsr)) {
            row.add(gsr);
        }
        int n = row.size();
        int totalW = n * size + Math.max(0, n - 1) * gap;
        int startX = centerX - totalW / 2;

        for (int i = 0; i < n; i++) {
            AbstractWidget w = row.get(i);
            w.setPosition(startX + i * (size + gap), y);
            w.setSize(size, size);
        }
    }

    private static int measureSquareGap(List<AbstractWidget> squares) {
        if (squares.size() < 2) return SQUARE_GAP;
        List<AbstractWidget> ordered = squares.stream()
                .sorted(Comparator.comparingInt(AbstractWidget::getX))
                .toList();
        for (int i = 1; i < ordered.size(); i++) {
            int gap = ordered.get(i).getX() - (ordered.get(i - 1).getX() + ordered.get(i - 1).getWidth());
            if (gap > 0 && gap <= 8) return gap;
        }
        return SQUARE_GAP;
    }

    private static List<AbstractWidget> collectSquareRow(net.minecraft.client.gui.screens.Screen screen) {
        List<AbstractWidget> all = collectClickableWidgets(screen);
        List<AbstractWidget> candidates = new ArrayList<>();
        for (AbstractWidget w : all) {
            if (isSquareIconButton(w)) candidates.add(w);
        }
        if (candidates.isEmpty()) return candidates;

        candidates.sort(Comparator.comparingInt(AbstractWidget::getY).thenComparingInt(AbstractWidget::getX));
        int bestY = candidates.get(0).getY();
        int bestCount = 0;
        int currentY = candidates.get(0).getY();
        int currentCount = 0;
        for (AbstractWidget w : candidates) {
            if (w.getY() == currentY) {
                currentCount++;
            } else {
                if (currentCount > bestCount) {
                    bestCount = currentCount;
                    bestY = currentY;
                }
                currentY = w.getY();
                currentCount = 1;
            }
        }
        if (currentCount > bestCount) {
            bestY = currentY;
        }

        List<AbstractWidget> row = new ArrayList<>();
        for (AbstractWidget w : candidates) {
            if (w.getY() == bestY) row.add(w);
        }
        return row;
    }

    private static boolean isSquareIconButton(AbstractWidget w) {
        if (w.getWidth() != w.getHeight()) return false;
        if (w.getWidth() < 16 || w.getWidth() > 24) return false;
        return w instanceof SpriteIconButton || w instanceof GSRSquareMenuButton;
    }

    private static AbstractWidget findGsrSquareButton(net.minecraft.client.gui.screens.Screen screen) {
        for (AbstractWidget cw : collectClickableWidgets(screen)) {
            if (cw instanceof GSRSquareMenuButton
                    || (cw instanceof Button bw
                    && bw.getMessage().getString().equals(GSRButtonParameters.TITLE_GSR_SQUARE))) {
                return cw;
            }
        }
        return null;
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
