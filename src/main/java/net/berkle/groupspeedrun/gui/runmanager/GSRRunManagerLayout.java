package net.berkle.groupspeedrun.gui.runmanager;

import net.berkle.groupspeedrun.parameter.GSRRunHistoryParameters;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

/**
 * Computes Run Manager container bounds. Container fits content exactly:
 * closed = two sections with minimal gap; open = header + list + confirm button.
 */
public final class GSRRunManagerLayout {

    private GSRRunManagerLayout() {}

    /**
     * Height of the container when no dropdown is open (two sections stacked).
     */
    public static int closedContentHeight() {
        return 2 * GSRUiParameters.RUN_MANAGER_SECTION_HEIGHT + GSRUiParameters.RUN_MANAGER_SECTION_GAP;
    }

    /**
     * Computes container bounds when dropdown is open.
     *
     * @param textRenderer  For header wrap.
     * @param header        Dropdown header text.
     * @param itemCount     Number of list items.
     * @param listWidth     Width of list area (for header wrap).
     * @return [containerTop, containerBottom, listTop, listBottom, confirmButtonTop].
     */
    public static int[] openBounds(TextRenderer textRenderer, String header, int itemCount, int listWidth) {
        int containerTop = GSRUiParameters.RUN_MANAGER_CONTAINER_TOP + GSRRunHistoryParameters.SELECTION_CONTAINER_MARGIN;

        int headerMaxWidth = listWidth - 2 * GSRRunHistoryParameters.LIST_TEXT_INSET;
        int headerHeight = textRenderer.wrapLines(Text.literal(header), Math.max(1, headerMaxWidth)).size() * textRenderer.fontHeight;
        int listTop = containerTop + GSRRunHistoryParameters.LIST_VERTICAL_GAP + headerHeight + GSRRunHistoryParameters.LIST_VERTICAL_GAP;

        int listAreaHeight = Math.min(
                itemCount * GSRRunHistoryParameters.ROW_HEIGHT + GSRRunHistoryParameters.BOTTOM_SCROLL_PADDING,
                GSRUiParameters.RUN_MANAGER_MAX_LIST_ROWS * GSRRunHistoryParameters.ROW_HEIGHT
        );
        listAreaHeight = Math.max(listAreaHeight, 3 * GSRRunHistoryParameters.ROW_HEIGHT);

        int listBottom = listTop + listAreaHeight;
        int delimiterTop = listBottom + GSRRunHistoryParameters.SELECTION_SCROLL_BEHIND_HEIGHT;
        int confirmButtonTop = delimiterTop + GSRRunHistoryParameters.SELECTION_DELIMITER_BAR_HEIGHT
                + GSRRunHistoryParameters.SELECTION_DELIMITER_BUTTON_GAP;
        int containerBottom = confirmButtonTop + GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT;

        return new int[] { containerTop, containerBottom, listTop, listBottom, confirmButtonTop };
    }
}
