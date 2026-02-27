package net.berkle.groupspeedrun.parameter;

import net.minecraft.util.Identifier;

/**
 * Parameters for the Run History screen: layout, colors, and chart styling.
 * Used by {@link net.berkle.groupspeedrun.gui.GSRRunHistoryScreen}.
 *
 * <p><b>Text shadow standard:</b> Button-like text (on vanilla button texture) must use
 * {@code drawTextWithShadow} or {@code drawCenteredTextWithShadow} to match main menu styling.
 * Use {@link #TEXT_COLOR} for button text. Section labels and chart text may use
 * {@code drawText} without shadow.
 */
public final class GSRRunHistoryParameters {

    private GSRRunHistoryParameters() {}

    // --- Tab indices ---
    /** Run Info tab (single run display). */
    public static final int TAB_RUN_INFO = 0;
    /** Run Graphs tab (single run comparison charts). */
    public static final int TAB_RUN_GRAPHS = 1;
    /** Player Graphs tab (multi-run aggregation). */
    public static final int TAB_PLAYER_GRAPHS = 2;

    // --- Layout (percentage-based panel widths) ---
    /** Title Y position (pixels from top). 50% of shared TITLE_Y for tighter spacing. */
    public static final int RUN_HISTORY_TITLE_Y = 10;
    /** Approximate title row height (pixels). Used to compute content top. */
    public static final int RUN_HISTORY_TITLE_HEIGHT = 14;
    /** Margin between content area and title/footer (pixels). Content fits between title and footer with this gap. */
    public static final int RUN_HISTORY_CONTENT_MARGIN = 4;
    /** Y offset from bottom for Run History footer buttons (Back, Export CSV). Smaller than main menu for more graph space. */
    public static final int RUN_HISTORY_FOOTER_Y_OFFSET = 28;
    /** Horizontal margin for footer buttons (Back/Close, Export CSV) in pixels. */
    public static final int RUN_HISTORY_EXPORT_BUTTON_MARGIN = 10;
    /** Left panel (filters) width as fraction of content area (0.25 = 25%). */
    public static final float LEFT_PANEL_WIDTH_FRACTION = 0.25f;
    /** Right panel (detail with tabs) width as fraction of content area (0.73 = 73%). */
    public static final float RIGHT_PANEL_WIDTH_FRACTION = 0.73f;
    /** Gap between panels as fraction of content area (0.02 = 2%). */
    public static final float PANEL_GAP_FRACTION = 0.02f;
    /** Horizontal margin (pixels) on each side of the content area. */
    public static final int CONTENT_HORIZONTAL_MARGIN = 4;
    /** Unified height for tabs and dropdown selector bars (pixels). Tabs and left-column bars match. */
    public static final int SELECTOR_BAR_HEIGHT = 22;
    /** Height of tab row in pixels (space reserved for tab bar). */
    public static final int TAB_HEIGHT = SELECTOR_BAR_HEIGHT + 4;
    /** Height of each row in run list and category list in pixels. */
    public static final int ROW_HEIGHT = 20;
    /** Height of bar chart bars in pixels. */
    public static final int BAR_HEIGHT = 20;
    /** Gap between bar chart slots in pixels. */
    public static final int BAR_GAP = 2;
    /** Number of compare bars for Recent/Best views (Selected, Avg, 5 most recent). */
    public static final int NUM_COMPARE_BARS = 7;
    /** Minimum bar width in pixels for All time view (bars scale to fit container). */
    public static final int OVER_TIME_MIN_BAR_WIDTH = 4;
    /** Run count above which All time view enables horizontal scroll (narrow bars). */
    public static final int OVER_TIME_SCROLL_THRESHOLD = 25;
    /** Run count above which bar labels are hidden (rely on hover tooltip). */
    public static final int OVER_TIME_LABEL_HIDE_THRESHOLD = 10;
    /** Fixed bar width in pixels when All time view overflows and horizontal scroll is enabled. */
    public static final int OVER_TIME_SCROLLED_BAR_WIDTH = 8;
    /** Horizontal scroll amount per wheel tick for All time chart when overflow. */
    public static final int OVER_TIME_HORIZONTAL_SCROLL_AMOUNT = 24;
    /** Minimum thumb width in pixels for horizontal chart scrollbar. */
    public static final int CHART_HORIZONTAL_SCROLLBAR_MIN_THUMB = 24;
    /** Maximum bar width as fraction of chart container (0.25 = 25%). */
    public static final float BAR_MAX_WIDTH_FRACTION = 0.25f;
    /** Height of filter bar (dropdown trigger button) in pixels. Matches SELECTOR_BAR_HEIGHT. */
    public static final int FILTER_BAR_HEIGHT = SELECTOR_BAR_HEIGHT;
    /** Max height of filter dropdown list in pixels (scrollable if more items). */
    public static final int FILTER_DROPDOWN_MAX_HEIGHT = 180;
    /** Height of Filter "Make selection" button in pixels. */
    public static final int FILTER_MAKE_SELECTION_BUTTON_HEIGHT = 22;
    /** Height of horizontal delimiter bar between selection list and Make selection button (pixels). List scrolls behind it. */
    public static final int SELECTION_DELIMITER_BAR_HEIGHT = 4;
    /** Gap between delimiter bar and Make selection button (pixels). */
    public static final int SELECTION_DELIMITER_BUTTON_GAP = 4;
    /** Height of area above delimiter where list scrolls behind (pixels). */
    public static final int SELECTION_SCROLL_BEHIND_HEIGHT = 4;
    /** Interior margin for all selection containers (top, left, right, bottom) in pixels. */
    public static final int SELECTION_CONTAINER_MARGIN = 4;
    /** Color for selection delimiter bar (ARGB). Separates scrollable list from Make selection button. */
    public static final int SELECTION_DELIMITER_BAR_COLOR = 0xFF505050;
    /** Total height of Make selection area: scroll-behind + delimiter + gap + button. */
    public static final int MAKE_SELECTION_CONTAINER_HEIGHT = SELECTION_SCROLL_BEHIND_HEIGHT
            + SELECTION_DELIMITER_BAR_HEIGHT + SELECTION_DELIMITER_BUTTON_GAP + FILTER_MAKE_SELECTION_BUTTON_HEIGHT;
    /** Max label length before truncation (e.g. "..." suffix). */
    public static final int LABEL_MAX_LEN = 22;
    /** Gap between tabs in pixels. */
    public static final int TAB_GAP = 2;
    /** Inner padding for tab/detail content in pixels. */
    public static final int CONTENT_PADDING = 2;
    /** Bottom padding of right column (detail) container (pixels). */
    public static final int DETAIL_PANEL_BOTTOM_PADDING = 2;
    /** Chart title row height in pixels (Run Time, Split Times, etc.). */
    public static final int CHART_TITLE_HEIGHT = 12;
    /** Chart description row height in pixels (below title). */
    public static final int CHART_DESCRIPTION_HEIGHT = 10;
    /** Chart axis label/value text height in pixels. */
    public static final int CHART_AXIS_TEXT_HEIGHT = 10;
    /** Max width for chart labels when truncation is disabled (effectively unlimited). */
    public static final int CHART_LABEL_MAX_WIDTH_UNLIMITED = 999;
    /** Minimum slot width in pixels for player chart bars. */
    public static final int PLAYER_CHART_MIN_SLOT_WIDTH = 20;
    /** Size of player face/head icon in pixels for player chart bars. */
    public static final int PLAYER_CHART_FACE_SIZE = 12;
    /** Gap between player face and name label in pixels. */
    public static final int PLAYER_CHART_FACE_NAME_GAP = 2;
    /** Approximate maximum bar height in pixels for content height calculation. */
    public static final int CHART_APPROX_MAX_BAR_HEIGHT = 80;
    /** Make selection button glow border width in pixels. */
    public static final int MAKE_SELECTION_GLOW_BORDER = 1;
    /** Chart bar Y offset from content top in pixels. */
    public static final int CHART_BAR_Y_OFFSET = 24;
    /** Container height below which compact chart layout is used (pixels). */
    public static final int CHART_COMPACT_THRESHOLD = 140;
    /** Compact chart title height in pixels (used when container is small). */
    public static final int CHART_TITLE_HEIGHT_COMPACT = 8;
    /** Compact chart description height in pixels (used when container is small). */
    public static final int CHART_DESCRIPTION_HEIGHT_COMPACT = 6;
    /** Compact chart bar Y offset in pixels (used when container is small). */
    public static final int CHART_BAR_Y_OFFSET_COMPACT = 8;
    /** Compact chart axis text height in pixels (used when container is small). */
    public static final int CHART_AXIS_TEXT_HEIGHT_COMPACT = 8;
    /** Chart label Y offset below bar in pixels. */
    public static final int CHART_LABEL_Y_OFFSET = 2;
    /** Chart value Y offset below label in pixels. */
    public static final int CHART_VALUE_Y_OFFSET = 12;
    /** Gap between run selection and stat selection containers when graph tab is selected (pixels). */
    public static final int LEFT_CONTAINER_GAP = 4;
    /** Extra scroll padding so the last row is fully visible when scrolled to bottom (pixels). */
    public static final int BOTTOM_SCROLL_PADDING = 4;
    /** Offset to hide category list off-screen when Run Info tab is selected (pixels). */
    public static final int CATEGORY_LIST_OFFSCREEN_OFFSET = 20;
    /** Inset from container edge for list items (pixels). */
    public static final int CONTAINER_INSET = 1;
    /** Interior margin for left column (icon, filters) in Export CSV and Run History (pixels). */
    public static final int LEFT_COLUMN_INTERIOR_MARGIN = 4;
    /** Horizontal inset for list item text (pixels). */
    public static final int LIST_TEXT_INSET = 4;
    /** Size of optional item icon in dropdown list rows (pixels). */
    public static final int DROPDOWN_ITEM_ICON_SIZE = 16;
    /** Margin inside button around dropdown list item icons (pixels). */
    public static final int DROPDOWN_ITEM_ICON_MARGIN = 2;
    /** Scale factor for dropdown list item icons (0.75 = 75% of DROPDOWN_ITEM_ICON_SIZE). */
    public static final float DROPDOWN_ITEM_ICON_SCALE = 0.75f;
    /** Gap between item icon and text in dropdown list rows (pixels). */
    public static final int DROPDOWN_ITEM_ICON_TEXT_GAP = 4;
    /** Size of icon in trigger button (pixels). */
    public static final int TRIGGER_ICON_SIZE = 16;
    /** Margin inside trigger button around icon (pixels). */
    public static final int TRIGGER_ICON_MARGIN = 2;
    /** Alpha for default color icon tint overlay (0x60 ≈ 38%). ARGB format. */
    public static final int DEFAULT_COLOR_ICON_TINT_ALPHA = 0x60;
    /** Vertical gap between container header and list (pixels). */
    public static final int LIST_VERTICAL_GAP = 2;
    /** Text scale for left column section and trigger labels (0.85 = 85%) at full size. */
    public static final float LEFT_COLUMN_LABEL_SCALE = 0.85f;
    /** Minimum fit scale for left column when container is small (0.5 = 50%). */
    public static final float LEFT_COLUMN_MIN_FIT_SCALE = 0.5f;
    /** Reference bar width for scale calculation; narrower bars use reduced scale. */
    public static final int LEFT_COLUMN_REFERENCE_BAR_WIDTH = 140;
    /** Gap from container top to filter bar (pixels). Section height = this + bar height (no padding beneath bar). */
    public static final int CONTAINER_HEADER_GAP = 10;
    /** Height of each selection section: header gap + bar height. Sections contain content with no extra padding. */
    public static final int SELECTION_SECTION_HEIGHT = CONTAINER_HEADER_GAP + FILTER_BAR_HEIGHT;
    /** Minimum section height when scaling to fit (pixels). Ensures bars remain usable. */
    public static final int SELECTION_SECTION_MIN_HEIGHT = 24;
    /** Gap from stat container top to category list (pixels). */
    public static final int CATEGORY_LIST_TOP_GAP = 4;
    /** Tab bar top offset (pixels). */
    public static final int TAB_TOP_OFFSET = 2;
    /** Scroll amount per wheel tick for filter dropdown list. */
    public static final int FILTER_DROPDOWN_SCROLL_AMOUNT = 20;
    /** Scroll amount per wheel tick for detail panel (Run Info, Stats). */
    public static final int DETAIL_SCROLL_AMOUNT = 24;
    /** Margin between content bottom and footer (pixels). */
    public static final int CONTENT_BOTTOM_MARGIN = 2;
    /** Display index for Select All when placed at top of multi-select dropdown. */
    public static final int MULTISELECT_SELECT_ALL_INDEX = 0;
    /** Display index for Deselect All when placed at top of multi-select dropdown. */
    public static final int MULTISELECT_DESELECT_ALL_INDEX = 1;
    /** Display index where data items start (Select All and Deselect All occupy 0 and 1). */
    public static final int MULTISELECT_DATA_START_INDEX = 2;
    /** Label for Select All action at top of multi-select dropdown list. */
    public static final String SELECT_ALL_LABEL = "§7— Select All —";
    /** Label for Deselect All action at top of multi-select dropdown list. */
    public static final String DESELECT_ALL_LABEL = "§7— Deselect All —";
    /** Runs preset: select last 5 most recent runs. */
    public static final String RUNS_PRESET_LAST_5_LABEL = "§7— Last 5 —";
    /** Runs preset: select last 10 most recent runs. */
    public static final String RUNS_PRESET_LAST_10_LABEL = "§7— Last 10 —";
    /** Runs preset: select last 20 most recent runs. */
    public static final String RUNS_PRESET_LAST_20_LABEL = "§7— Last 20 —";
    /** Filter bar: total height in pixels (2 rows of bars + gap). */
    public static final int FILTER_BAR_HEIGHT_TOTAL = 48;
    /** Filter bar: vertical gap between rows in pixels. */
    public static final int FILTER_BAR_ROW_GAP = 4;
    /** Filter bar: horizontal gap between columns in pixels. */
    public static final int FILTER_BAR_COLUMN_GAP = 4;
    /** Filter bar: minimum overlay width for dropdown list when bar is narrow (pixels). */
    public static final int FILTER_BAR_DROPDOWN_MIN_WIDTH = 280;
    /** Gap between Run Info text and Delete Run button (pixels). */
    public static final int DELETE_RUN_BUTTON_GAP = 8;
    /** Height of Delete Run button (pixels). */
    public static final int DELETE_RUN_BUTTON_HEIGHT = 20;
    /** Width of Delete Run button (pixels). */
    public static final int DELETE_RUN_BUTTON_WIDTH = 90;

    /** Ticker: full cycle duration in ms (scroll through full text). 25% slower than original 10000. */
    public static final int TICKER_CYCLE_MS = 12500;
    /** Horizontal margin (pixels) inside button so scrolling text stays within edges. */
    public static final int BUTTON_TICKER_MARGIN = 4;
    /** Ticker: show rotating text for this many ms after a selection is made. */
    public static final int TICKER_ACTIVE_AFTER_SELECTION_MS = 10000;
    /** Ticker: pause at start/end in ms. */
    public static final int TICKER_PAUSE_MS = 800;
    /** Ticker button text color when active (ARGB). */
    public static final int TICKER_ACTIVE_COLOR = 0xFFFFFFFF;
    /** Ticker button text color when inactive (ARGB). */
    public static final int TICKER_INACTIVE_COLOR = 0xFFA0A0A0;
    /** Tooltip: max width/height as fraction of screen (0.35 = 35%). Used by chart bars and global tooltip mixin. */
    public static final float TOOLTIP_MAX_SCREEN_FRACTION = 0.35f;
    /** Chart bar tooltip: vertical scroll cycle duration in ms (cyclical loop). 50% slower than original. */
    public static final int TOOLTIP_SCROLL_CYCLE_MS = 16000;
    /** Chart bar tooltip: pause at top (ms) before starting scroll, and again each time content returns to top. */
    public static final int TOOLTIP_SCROLL_START_DELAY_MS = 2500;
    /** Run Info auto-scroll: hover delay (ms) before starting scroll. */
    public static final int RUN_INFO_AUTO_SCROLL_HOVER_DELAY_MS = 2500;
    /** Run Info auto-scroll: pause (ms) at top and bottom before reversing direction. */
    public static final int RUN_INFO_AUTO_SCROLL_PAUSE_MS = 2500;
    /** Run Info auto-scroll: pixels per second (5% of original 15 px/s for very slow scroll). */
    public static final float RUN_INFO_AUTO_SCROLL_SPEED = 0.75f;
    /** Chart bar tooltip: inner padding in pixels. */
    public static final int TOOLTIP_PADDING = 8;
    /** Make selection button breathing: cycle period in ms (~2 s). */
    public static final int MAKE_SELECTION_BREATHE_PERIOD_MS = 2000;
    /** Make selection button breathing: min alpha (0–1). */
    public static final float MAKE_SELECTION_BREATHE_ALPHA_MIN = 0.25f;
    /** Make selection button breathing: max alpha (0–1). */
    public static final float MAKE_SELECTION_BREATHE_ALPHA_MAX = 0.65f;
    /** Make selection button breathing: glow color (RGB, alpha applied per-frame). */
    public static final int MAKE_SELECTION_BREATHE_GLOW_COLOR = 0xFF55AAFF;
    /** Scrollbar width in pixels for lists and dropdowns. */
    public static final int SCROLLBAR_WIDTH = 3;
    /** Minimum scrollbar thumb height in pixels so it stays visible. */
    public static final int SCROLLBAR_MIN_THUMB_HEIGHT = 8;
    /** Number of split segments in Split Times bar (N, B, F, E, D). */
    public static final int SPLIT_SEGMENT_COUNT = 5;
    /** Gap between split segments in pixels. */
    public static final int SPLIT_SEGMENT_GAP = 1;

    // --- Colors (all ARGB with 0xFF prefix) ---
    /** Label color (Runs, Filter, Compare). */
    public static final int LABEL_COLOR = 0xFFAAAAAA;
    /** Default text color (white). Use with drawTextWithShadow/drawCenteredTextWithShadow for button-like text to match main menu. */
    public static final int TEXT_COLOR = 0xFFFFFFFF;
    /** Empty state message ("Select a run"). */
    public static final int EMPTY_MESSAGE_COLOR = 0xFF888888;
    /** Symbol for passed/victorious runs (dragon). */
    public static final String STATUS_ICON_VICTORY = "🐉";
    /** Symbol for failed runs (skull). */
    public static final String STATUS_ICON_FAIL = "💀";
    /** Symbol for active (in-progress) runs (stopwatch). */
    public static final String STATUS_ICON_ACTIVE = "⏱";
    /** Symbol for active runs that are deranked (white flag). Shown when active and locators/admin invalidated. */
    public static final String STATUS_ICON_DERANKED = "\uD83C\uDFF3";
    /** Symbol for frozen/paused runs (snowflake). Used when timer was frozen at save time. */
    public static final String STATUS_ICON_FROZEN = "\u2745";
    /** Tab background when selected. */
    public static final int TAB_SELECTED_BG = 0xFF505070;
    /** Tab background when hovered. */
    public static final int TAB_HOVER_BG = 0xFF4A4A4A;
    /** Tab background when idle. */
    public static final int TAB_BG = 0xFF404040;
    /** Chart category label color (pure white). */
    public static final int CHART_LABEL_COLOR = 0xFFFFFFFF;
    /** Chart bar slot background. */
    public static final int BAR_SLOT_BG = 0xFF282828;
    /** Chart bar label color (Sel, Avg, #1, etc.). Pure white. */
    public static final int BAR_LABEL_COLOR = 0xFFFFFFFF;
    /** Chart bar value color (pure white). */
    public static final int BAR_VALUE_COLOR = 0xFFFFFFFF;
    /** Status icon color when drawn on bar (high contrast over bar fill). */
    public static final int STATUS_ICON_ON_BAR_COLOR = 0xFFFFFFFF;
    /** Selected run bar color. */
    public static final int BAR_COLOR_SELECTED = 0xFF4080FF;
    /** Average bar color. */
    public static final int BAR_COLOR_AVG = 0xFFFFFF00;
    /** Recent run bar color. */
    public static final int BAR_COLOR_RECENT = 0xFF888888;
    /** Best run bar color (Player Graphs best/worst split). */
    public static final int BAR_COLOR_BEST = 0xFF00AA00;
    /** Worst run bar color (Player Graphs best/worst split). */
    public static final int BAR_COLOR_WORST = 0xFFAA0000;
    /** Best Fail segment (Player Graphs 4-segment: Best Fail, Best Success, Worst Fail, Worst Success). */
    public static final int PLAYER_CHART_BEST_FAIL_COLOR = 0xFF558855;
    /** Best Success segment. */
    public static final int PLAYER_CHART_BEST_SUCCESS_COLOR = 0xFF00AA00;
    /** Worst Fail segment. */
    public static final int PLAYER_CHART_WORST_FAIL_COLOR = 0xFFAA0000;
    /** Worst Success segment. */
    public static final int PLAYER_CHART_WORST_SUCCESS_COLOR = 0xFFAA5050;
    /** Player chart gradient: best rank (green). */
    public static final int PLAYER_CHART_COLOR_BEST = 0xFF00AA00;
    /** Player chart gradient: worst rank (red). */
    public static final int PLAYER_CHART_COLOR_WORST = 0xFFAA0000;
    /** Split segment colors (N, B, F, E, D) for Split Times chart. */
    public static final int[] SPLIT_SEGMENT_COLORS = {
            0xFF5599FF, 0xFF55AA55, 0xFFAA8855, 0xFF9955AA, 0xFFAA5555
    };
    /** Split key height in pixels (drawn at top of bar area, behind bars). */
    public static final int SPLIT_KEY_HEIGHT = 12;
    /** Split key text color (ARGB). White for contrast on colored segments. */
    public static final int SPLIT_KEY_TEXT_COLOR = 0xFFFFFFFF;
    /** Scrollbar track color for lists and dropdowns. */
    public static final int SCROLLBAR_TRACK_COLOR = 0x40202020;
    /** Scrollbar thumb color for lists and dropdowns. */
    public static final int SCROLLBAR_THUMB_COLOR = 0xFFAAAAAA;
    /** Chart bar hover tooltip background (ARGB). */
    public static final int TOOLTIP_BG = 0xF0101010;
    /** Chart bar hover tooltip border (ARGB). */
    public static final int TOOLTIP_BORDER = 0xFF505050;
    /** Chart bar hover tooltip text color. */
    public static final int TOOLTIP_TEXT_COLOR = 0xFFFFFFFF;
    /** Minecraft § code for tooltip Overall line (orange/gold). */
    public static final String TOOLTIP_OVERALL_COLOR = "§6";
    /** Minecraft § code for tooltip Success line (green). */
    public static final String TOOLTIP_SUCCESS_COLOR = "§a";
    /** Minecraft § code for tooltip Fail line (red). */
    public static final String TOOLTIP_FAIL_COLOR = "§c";
    /** Minecraft § code for tooltip colons and counts (white). */
    public static final String TOOLTIP_WHITE = "§f";
    /** Minecraft § code for Avg value (yellow, matches BAR_COLOR_AVG). */
    public static final String TOOLTIP_AVG_VALUE_COLOR = "§e";

    // --- Export CSV popup (vertical stack, content-fitting) ---
    /** Export CSV left column: GSR icon texture size (pixels). Source texture is square; used for UV mapping. */
    /** Source texture size for gsr_icon.png (must be power-of-2, e.g. 512x512). */
    public static final int EXPORT_CSV_ICON_TEXTURE_SIZE = 512;
    /** Export CSV left column: vertical gap between icon area and filter section (pixels). */
    public static final int EXPORT_CSV_ICON_FILTER_GAP = 4;
    /** Export CSV icon placeholder color when texture fails to load (ARGB). */
    public static final int EXPORT_CSV_ICON_PLACEHOLDER_COLOR = 0xFF202020;
    /** Export CSV left column: GUI atlas sprite identifier for gsr_icon (texture in minecraft:textures/gsr/). */
    public static final Identifier EXPORT_CSV_ICON_SPRITE = Identifier.of("minecraft", "gsr/gsr_icon");
    /** Export CSV popup: margin around content (pixels). */
    public static final int EXPORT_POPUP_MARGIN = 4;
    /** Export CSV popup: title Y offset from panel top (pixels). */
    public static final int EXPORT_POPUP_TITLE_Y = 12;
    /** Export CSV popup: vertical gap between filter rows (pixels). */
    public static final int EXPORT_POPUP_ROW_GAP = 8;
    /** Export CSV popup: dropdown section height (label + bar) per filter (pixels). */
    public static final int EXPORT_POPUP_SECTION_HEIGHT = 36;
    /** Export CSV popup: minimum panel width (pixels). Ensures dropdown overlay fits. */
    public static final int EXPORT_POPUP_MIN_WIDTH = 260;
    /** Export CSV popup: vertical gap between container bottom and footer buttons (pixels). */
    public static final int EXPORT_POPUP_CONTAINER_BUTTON_GAP = 8;
}
