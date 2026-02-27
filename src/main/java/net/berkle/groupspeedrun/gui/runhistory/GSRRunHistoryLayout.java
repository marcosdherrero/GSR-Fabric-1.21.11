package net.berkle.groupspeedrun.gui.runhistory;

import net.berkle.groupspeedrun.parameter.GSRRunHistoryParameters;

/**
 * Layout for Run History screen: 2-column split with left filter panel and right detail panel.
 */
public final class GSRRunHistoryLayout {

    private GSRRunHistoryLayout() {}

    /** Full content area bounds (margin to margin). Uses 4px horizontal margin. */
    public record Layout(int contentLeft, int contentRight, int contentWidth) {
        public static Layout compute(int screenWidth) {
            int margin = GSRRunHistoryParameters.CONTENT_HORIZONTAL_MARGIN;
            int contentWidth = screenWidth - 2 * margin;
            int contentLeft = margin;
            int contentRight = contentLeft + contentWidth;
            return new Layout(contentLeft, contentRight, contentWidth);
        }
    }

    /**
     * Two-column layout: left panel (filters), right panel (detail with tabs).
     * Left: 25% width. Gap: 2%. Right: 73% width. Margins 4px all around.
     */
    public record TwoColumnBounds(
            int leftPanelLeft, int leftPanelWidth, int leftPanelTop, int leftPanelBottom,
            int rightPanelLeft, int rightPanelWidth, int rightPanelTop, int rightPanelBottom,
            int[] sectionTops, int sectionHeight,
            int barHeight, int barWidth,
            int overlayLeft, int overlayWidth,
            int selectionListBottom, int makeSelectionContainerTop, int makeSelectionContainerBottom,
            int ddHeaderMaxWidth) {

        /** Number of filter sections (Player Filter, Player Count, Runs, Compare, View, Type). */
        public static final int FILTER_SECTION_COUNT = 6;

        public static TwoColumnBounds compute(int screenWidth, int screenHeight) {
            Layout layout = Layout.compute(screenWidth);
            int contentLeft = layout.contentLeft();
            int contentWidth = layout.contentWidth();
            int leftPanelWidth = (int) (contentWidth * GSRRunHistoryParameters.LEFT_PANEL_WIDTH_FRACTION);
            int gapWidth = (int) (contentWidth * GSRRunHistoryParameters.PANEL_GAP_FRACTION);
            int rightPanelWidth = (int) (contentWidth * GSRRunHistoryParameters.RIGHT_PANEL_WIDTH_FRACTION);

            int leftPanelLeft = contentLeft;
            int rightPanelLeft = leftPanelLeft + leftPanelWidth + gapWidth;

            int topY = GSRRunHistoryParameters.RUN_HISTORY_TITLE_Y
                    + GSRRunHistoryParameters.RUN_HISTORY_TITLE_HEIGHT
                    + GSRRunHistoryParameters.RUN_HISTORY_CONTENT_MARGIN;
            int footerTop = screenHeight - GSRRunHistoryParameters.RUN_HISTORY_FOOTER_Y_OFFSET;
            int bottomY = footerTop - GSRRunHistoryParameters.RUN_HISTORY_CONTENT_MARGIN;
            int leftPanelTop = topY;
            int leftPanelBottom = bottomY;
            int rightPanelTop = topY;
            int rightPanelBottom = bottomY;

            int leftPanelHeight = leftPanelBottom - leftPanelTop;
            int sectionHeight = Math.max(GSRRunHistoryParameters.SELECTION_SECTION_MIN_HEIGHT,
                    leftPanelHeight / FILTER_SECTION_COUNT);
            int barHeight = Math.max(GSRRunHistoryParameters.FILTER_BAR_HEIGHT / 2,
                    sectionHeight - GSRRunHistoryParameters.CONTAINER_HEADER_GAP);
            int[] sectionTops = new int[FILTER_SECTION_COUNT];
            for (int i = 0; i < FILTER_SECTION_COUNT; i++) {
                sectionTops[i] = leftPanelTop + i * sectionHeight;
            }

            /* Dropdown overlay fills left panel when open. 4px margin on all sides of selection container. */
            int overlayWidth = leftPanelWidth - 2 * GSRRunHistoryParameters.LEFT_COLUMN_INTERIOR_MARGIN;
            int overlayLeft = leftPanelLeft + GSRRunHistoryParameters.LEFT_COLUMN_INTERIOR_MARGIN;
            int selectionListBottom = leftPanelBottom - GSRRunHistoryParameters.SELECTION_CONTAINER_MARGIN
                    - GSRRunHistoryParameters.MAKE_SELECTION_CONTAINER_HEIGHT;
            int makeSelectionContainerTop = selectionListBottom + GSRRunHistoryParameters.SELECTION_SCROLL_BEHIND_HEIGHT
                    + GSRRunHistoryParameters.SELECTION_DELIMITER_BAR_HEIGHT + GSRRunHistoryParameters.SELECTION_DELIMITER_BUTTON_GAP;
            int makeSelectionContainerBottom = makeSelectionContainerTop + GSRRunHistoryParameters.FILTER_MAKE_SELECTION_BUTTON_HEIGHT;
            int ddHeaderMaxWidth = Math.max(overlayWidth - 2 * GSRRunHistoryParameters.LIST_TEXT_INSET,
                    GSRRunHistoryParameters.FILTER_BAR_DROPDOWN_MIN_WIDTH - 2 * GSRRunHistoryParameters.LIST_TEXT_INSET);

            return new TwoColumnBounds(
                    leftPanelLeft, leftPanelWidth, leftPanelTop, leftPanelBottom,
                    rightPanelLeft, rightPanelWidth, rightPanelTop, rightPanelBottom,
                    sectionTops, sectionHeight, barHeight, leftPanelWidth,
                    overlayLeft, overlayWidth,
                    selectionListBottom, makeSelectionContainerTop, makeSelectionContainerBottom,
                    ddHeaderMaxWidth);
        }

        /** Top of filter bar for section index (0–5). */
        public int barTop(int sectionIndex) {
            int headerGap = Math.min(GSRRunHistoryParameters.CONTAINER_HEADER_GAP, sectionHeight - barHeight);
            return sectionTops[sectionIndex] + headerGap;
        }
    }

    /**
     * Export CSV layout: 2-column like Run History, 3 filter sections (Player Filter, Player Count, Runs).
     */
    public record ExportCsvBounds(
            int leftPanelLeft, int leftPanelWidth, int leftPanelTop, int leftPanelBottom,
            int rightPanelLeft, int rightPanelWidth, int rightPanelTop, int rightPanelBottom,
            int[] sectionTops, int sectionHeight, int barHeight, int barWidth,
            int overlayLeft, int overlayWidth,
            int selectionListBottom, int makeSelectionContainerTop,
            int iconTop, int iconHeight, int iconLeft, int iconWidth,
            int footerLeftX, int footerRightX, int footerY, int buttonWidth, int buttonHeight) {

        public static final int EXPORT_SECTION_COUNT = 3;

        public static ExportCsvBounds compute(int screenWidth, int screenHeight) {
            Layout layout = Layout.compute(screenWidth);
            int contentWidth = layout.contentWidth();
            int leftPanelWidth = (int) (contentWidth * GSRRunHistoryParameters.LEFT_PANEL_WIDTH_FRACTION);
            int gapWidth = (int) (contentWidth * GSRRunHistoryParameters.PANEL_GAP_FRACTION);
            int rightPanelWidth = (int) (contentWidth * GSRRunHistoryParameters.RIGHT_PANEL_WIDTH_FRACTION);
            int leftPanelLeft = layout.contentLeft();
            int rightPanelLeft = leftPanelLeft + leftPanelWidth + gapWidth;

            int topY = GSRRunHistoryParameters.RUN_HISTORY_TITLE_Y
                    + GSRRunHistoryParameters.RUN_HISTORY_TITLE_HEIGHT
                    + GSRRunHistoryParameters.RUN_HISTORY_CONTENT_MARGIN;
            int footerTop = screenHeight - GSRRunHistoryParameters.RUN_HISTORY_FOOTER_Y_OFFSET;
            int bottomY = footerTop - GSRRunHistoryParameters.RUN_HISTORY_CONTENT_MARGIN;
            int leftPanelTop = topY;
            int leftPanelBottom = bottomY;
            int rightPanelTop = topY;
            int rightPanelBottom = bottomY;

            int leftPanelHeight = leftPanelBottom - leftPanelTop;
            int filtersHeight = leftPanelHeight / 2;
            int filtersTop = leftPanelBottom - filtersHeight;
            int iconTop = leftPanelTop + GSRRunHistoryParameters.LEFT_COLUMN_INTERIOR_MARGIN;
            int iconHeight = filtersTop - iconTop - GSRRunHistoryParameters.EXPORT_CSV_ICON_FILTER_GAP;
            int iconLeft = leftPanelLeft + GSRRunHistoryParameters.LEFT_COLUMN_INTERIOR_MARGIN;
            int iconWidth = leftPanelWidth - 2 * GSRRunHistoryParameters.LEFT_COLUMN_INTERIOR_MARGIN;

            /* Use same bar height as Run History; 3 sections for Export CSV filters (bottom 50%). */
            int sectionHeight = Math.max(GSRRunHistoryParameters.SELECTION_SECTION_MIN_HEIGHT,
                    filtersHeight / EXPORT_SECTION_COUNT);
            int barHeight = Math.max(GSRRunHistoryParameters.FILTER_BAR_HEIGHT / 2,
                    sectionHeight - GSRRunHistoryParameters.CONTAINER_HEADER_GAP);
            int[] sectionTops = new int[EXPORT_SECTION_COUNT];
            for (int i = 0; i < EXPORT_SECTION_COUNT; i++) {
                sectionTops[i] = filtersTop + i * sectionHeight;
            }

            int overlayWidth = leftPanelWidth - 2 * GSRRunHistoryParameters.LEFT_COLUMN_INTERIOR_MARGIN;
            int overlayLeft = leftPanelLeft + GSRRunHistoryParameters.LEFT_COLUMN_INTERIOR_MARGIN;
            int selectionListBottom = leftPanelBottom - GSRRunHistoryParameters.SELECTION_CONTAINER_MARGIN
                    - GSRRunHistoryParameters.MAKE_SELECTION_CONTAINER_HEIGHT;
            int makeSelectionContainerTop = selectionListBottom + GSRRunHistoryParameters.SELECTION_SCROLL_BEHIND_HEIGHT
                    + GSRRunHistoryParameters.SELECTION_DELIMITER_BAR_HEIGHT + GSRRunHistoryParameters.SELECTION_DELIMITER_BUTTON_GAP;

            var footer = net.berkle.groupspeedrun.gui.components.GSRMenuComponents.footerLayout(screenWidth, screenHeight, GSRRunHistoryParameters.RUN_HISTORY_FOOTER_Y_OFFSET);
            return new ExportCsvBounds(
                    leftPanelLeft, leftPanelWidth, leftPanelTop, leftPanelBottom,
                    rightPanelLeft, rightPanelWidth, rightPanelTop, rightPanelBottom,
                    sectionTops, sectionHeight, barHeight, leftPanelWidth,
                    overlayLeft, overlayWidth,
                    selectionListBottom, makeSelectionContainerTop,
                    iconTop, iconHeight, iconLeft, iconWidth,
                    footer.leftX(), footer.rightX(), footer.footerY(), footer.buttonWidth(), footer.buttonHeight());
        }

        public int barTop(int sectionIndex) {
            int headerGap = Math.min(GSRRunHistoryParameters.CONTAINER_HEADER_GAP, sectionHeight - barHeight);
            return sectionTops[sectionIndex] + headerGap;
        }
    }
}
