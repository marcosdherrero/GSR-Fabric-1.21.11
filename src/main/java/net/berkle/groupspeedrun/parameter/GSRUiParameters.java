package net.berkle.groupspeedrun.parameter;

/**
 * Parameters for GSR UI screens: layout (padding, button size, colors) and message strings.
 * Used by {@link net.berkle.groupspeedrun.gui.GSRControlsScreen}, {@link net.berkle.groupspeedrun.util.GSRStatusText},
 * and other GSR screens.
 */
public final class GSRUiParameters {

    private GSRUiParameters() {}

    // --- Status screen ---
    /** Padding around content (pixels). */
    public static final int STATUS_PADDING = 10;
    /** Line height for status text (pixels). */
    public static final int STATUS_LINE_HEIGHT = 12;
    /** Button width (pixels). */
    public static final int STATUS_BUTTON_WIDTH = 200;
    /** Button height (pixels). */
    public static final int STATUS_BUTTON_HEIGHT = 20;
    /** Opaque background color (ARGB). */
    public static final int STATUS_BG_COLOR = 0xFF1C1C1C;
    /** Top of content area (pixels from top). */
    public static final int STATUS_CONTENT_TOP = 36;
    /** Bottom margin below content for buttons (pixels). */
    public static final int STATUS_CONTENT_BOTTOM_MARGIN = 44;
    /** Shown when no run data is available. */
    public static final String STATUS_FALLBACK_MESSAGE = "No run data available.";

    // --- Controls screen (GSR options in-world) ---
    /** Button width (pixels). Matches main menu TITLE_FULL_BUTTON_WIDTH. */
    public static final int CONTROLS_BUTTON_WIDTH = 200;
    /** Half-width for two-column buttons. Matches main menu Options/Quit (98 = (200-4)/2). */
    public static final int CONTROLS_HALF_BUTTON_WIDTH = 98;
    /** Button height (pixels). Matches main menu TITLE_BUTTON_HEIGHT. */
    public static final int CONTROLS_BUTTON_HEIGHT = 20;
    /** Vertical gap between button rows. Matches main menu TITLE_BUTTON_ROW_GAP. */
    public static final int CONTROLS_ROW_GAP = 4;
    /** Horizontal gap between two columns. Matches main menu TITLE_BUTTON_GAP. */
    public static final int CONTROLS_COL_GAP = 4;
    /** Y offset from bottom for footer row (Back | GSR Config). Matches main menu TITLE_OPTIONS_QUIT_Y_OFFSET. */
    public static final int CONTROLS_FOOTER_Y_OFFSET = 44;
    /** Y offset from center for first content row. Matches main menu TITLE_SCREEN_BUTTON_Y_OFFSET. */
    public static final int CONTROLS_INITIAL_Y_OFFSET = -40;
    /** Timer alpha when shown behind options (0–1). De-emphasizes timer so buttons are focus. */
    public static final float CONTROLS_TIMER_ALPHA = 0.45f;
    /** Initial Y for single-column screens (e.g. Run Manager). Pixels from top. */
    public static final int CONTROLS_INITIAL_Y = 40;
    /** Padding between buttons for single-column layouts (pixels). */
    public static final int CONTROLS_PADDING = 10;

    // --- Shared Back / Close footer (all GSR menus) ---
    /** Width of each Back/Close button (pixels). */
    public static final int FOOTER_BUTTON_WIDTH = 100;
    /** Height of Back/Close buttons (pixels). */
    public static final int FOOTER_BUTTON_HEIGHT = 20;
    /** Gap between Back and Close (pixels). */
    public static final int FOOTER_BUTTON_GAP = 10;
    /** Y offset from bottom of screen to footer row (pixels). */
    public static final int FOOTER_Y_OFFSET = 30;
    /** Title screen: Y offset from center for first button row (pixels). Negative = above center. */
    public static final int TITLE_SCREEN_BUTTON_Y_OFFSET = -40;
    /** Title screen: vertical gap between button rows (pixels). Same value used for horizontal gap between half-width columns. */
    public static final int TITLE_BUTTON_ROW_GAP = 4;

    // --- Main menu (Title screen) - standardized layout ---
    /** Width of full-width buttons (Singleplayer, Multiplayer, Mods). */
    public static final int TITLE_FULL_BUTTON_WIDTH = 200;
    /** Width of each half-width button. Derived so 2*half + gap = full (total width matches single-column). */
    public static final int TITLE_HALF_BUTTON_WIDTH = (TITLE_FULL_BUTTON_WIDTH - TITLE_BUTTON_ROW_GAP) / 2;
    /** Height of all main menu buttons. */
    public static final int TITLE_BUTTON_HEIGHT = 20;
    /** Horizontal gap between half-width buttons. Matches vertical row gap for consistent padding. */
    public static final int TITLE_BUTTON_GAP = TITLE_BUTTON_ROW_GAP;
    /** Y offset from bottom for Options/Quit row when preserving vanilla position (fallback if button not found). */
    public static final int TITLE_OPTIONS_QUIT_Y_OFFSET = 44;

    // --- Shared message prefix (chat / status text) ---
    /** Prefix for GSR messages (e.g. "§6[GSR] "). */
    public static final String MSG_PREFIX = "§6[GSR] ";

    // --- Status screen ---
    /** Request new status about every 2 seconds while open (frames at ~40 fps). */
    public static final int STATUS_REFRESH_INTERVAL_FRAMES = 80;
    /** Title Y position (pixels from top). */
    public static final int TITLE_Y = 20;
    /** Content area inner padding (pixels). */
    public static final int CONTENT_INNER_PADDING = 4;
    /** Content box background (ARGB). */
    public static final int CONTENT_BOX_BG = 0xFF282828;
    /** Height of alpha fade at top/bottom of scrollable content (pixels). Softens cutoff edges. */
    public static final int SCROLL_FADE_HEIGHT = 14;
    /** Content top offset from content area (pixels). */
    public static final int CONTENT_TOP_OFFSET = 6;

    // --- Controls / Locators / KeyBind screen backgrounds ---
    /** Dark background for Controls and Locators screens (ARGB). */
    public static final int SCREEN_BG_DARK = 0xE0101010;

    // --- Locators screen ---
    public static final int LOCATORS_PREVIEW_BAR_WIDTH = 200;
    public static final int LOCATORS_PREVIEW_BAR_HEIGHT = 8;
    public static final int LOCATORS_PREVIEW_ICON_RADIUS = 9;
    public static final int LOCATORS_PREVIEW_Y = 50;
    public static final int LOCATORS_BUTTON_WIDTH = 180;
    public static final int LOCATORS_ROW_PAD = 4;
    public static final int LOCATORS_COL_GAP = 8;
    public static final int LOCATORS_PADDING_ABOVE_FOOTER = 24;
    public static final int LOCATORS_TWO_COLUMN_THRESHOLD = 320;
    /** Deranked message color (ARGB). Bronze #CD7F32. */
    public static final int LOCATORS_DERANKED_COLOR = 0xFFCD7F32;
    /** Inactive locator label color (ARGB). */
    public static final int LOCATORS_INACTIVE_LABEL = 0xFF888888;
    /** Chat message when structure locate fails. %s = structure type (fortress, bastion, etc.). */
    public static final String MSG_LOCATOR_NOT_FOUND = "Could not find %s nearby. Locator turned off.";
    /** Chat message when run is deranked due to admin command use. */
    public static final String MSG_ADMIN_COMMAND_DERANKED = "Run deranked: admin command used.";

    // --- New World Confirm screen ---
    public static final int NEW_WORLD_BUTTON_WIDTH = 160;
    public static final int NEW_WORLD_BUTTON_GAP = 5;
    public static final int NEW_WORLD_TITLE_OFFSET = 40;
    public static final int NEW_WORLD_LINE1_OFFSET = 20;
    public static final int NEW_WORLD_LINE2_OFFSET = 8;
    /** Title color (ARGB). */
    public static final int NEW_WORLD_TITLE_COLOR = 0xFFFFFFFF;
    /** Secondary text color (ARGB). */
    public static final int NEW_WORLD_LINE1_COLOR = 0xFFE0E0E0;
    public static final int NEW_WORLD_LINE2_COLOR = 0xFFB0B0B0;
    /** Minimum button gap (pixels). */
    public static final int NEW_WORLD_MIN_BUTTON_GAP = 10;
    /** Fallback world name when none suggested. */
    public static final String NEW_WORLD_DEFAULT_NAME = "gsr_run_1_host_Host";

    // --- Reset Run Confirm screen ---
    /** Reset confirm: title Y offset from center (pixels). */
    public static final int RESET_CONFIRM_TITLE_OFFSET = 40;
    /** Reset confirm: message line Y offset from center (pixels). */
    public static final int RESET_CONFIRM_MESSAGE_OFFSET = 12;
    /** Reset confirm: max width for wrapped message text (pixels). */
    public static final int RESET_CONFIRM_MESSAGE_MAX_WIDTH = 280;
    /** Locator invalidate confirm: title Y offset from center (pixels). */
    public static final int LOCATOR_CONFIRM_TITLE_OFFSET = 40;
    /** Locator invalidate confirm: message line Y offset from center (pixels). */
    public static final int LOCATOR_CONFIRM_MESSAGE_OFFSET = 12;

    // --- Run Manager screens ---
    public static final int RUN_MANAGER_ROW_HEIGHT = 22;
    public static final int RUN_MANAGER_PANEL_PADDING = 8;
    /** Horizontal margin from screen edges for participant panels (pixels). */
    public static final int PARTICIPANT_PANEL_MARGIN = 16;
    /** Total horizontal inset (both sides) for panel content area (pixels). */
    public static final int PARTICIPANT_TOTAL_HORIZONTAL_INSET = 32;
    /** Vertical space between panel bottom and footer (pixels). */
    public static final int PARTICIPANT_PANEL_BOTTOM_OFFSET = 60;
    /** Y offset from footer row to action buttons (Group all, ->, Exclude all) (pixels). */
    public static final int PARTICIPANT_ACTION_BUTTON_Y_OFFSET = 35;
    /** Width of Group all / Exclude all / -> buttons (pixels). */
    public static final int PARTICIPANT_ACTION_BUTTON_WIDTH = 80;
    /** Height of action buttons (pixels). */
    public static final int PARTICIPANT_ACTION_BUTTON_HEIGHT = 20;
    /** Gap between action buttons (pixels). */
    public static final int PARTICIPANT_ACTION_BUTTON_GAP = 6;
    /** Padding inside row for clickable area (pixels). */
    public static final int PARTICIPANT_ROW_BUTTON_PAD = 2;
    /** Y offset above panel top for "Grouped" / "Excluded" labels (pixels). */
    public static final int PARTICIPANT_LABEL_OFFSET = 14;
    /** Text color when row is selected (ARGB). */
    public static final int PARTICIPANT_SELECTED_TEXT_COLOR = 0xFF4080FF;
    /** Title and label text color (ARGB). */
    public static final int PARTICIPANT_TITLE_COLOR = 0xFFFFFFFF;
    /** Player row background (button-like, ARGB). */
    public static final int RUN_MANAGER_ROW_BG = 0xFF404040;
    /** Player row background when selected (ARGB). */
    public static final int RUN_MANAGER_ROW_BG_SELECTED = 0xFF505070;
    /** Player row background when hovered (ARGB). */
    public static final int RUN_MANAGER_ROW_BG_HOVER = 0xFF4A4A4A;
    public static final int RUN_MANAGER_PANEL_GAP = 12;
    public static final int RUN_MANAGER_CONTENT_TOP = 36;
    public static final int RUN_MANAGER_CONTENT_BOTTOM = 44;
    /** Run Manager dropdown layout: list width (pixels). Centered via width/2 - RUN_MANAGER_LIST_HALF_WIDTH. */
    public static final int RUN_MANAGER_LIST_WIDTH = 240;
    /** Half of list width for centering (pixels). */
    public static final int RUN_MANAGER_LIST_HALF_WIDTH = RUN_MANAGER_LIST_WIDTH / 2;
    /** Y position of container top (pixels from screen top). */
    public static final int RUN_MANAGER_CONTAINER_TOP = 40;
    /** Height of each dropdown section (label + bar) in collapsed state (pixels). Tight fit, no extra gap. */
    public static final int RUN_MANAGER_SECTION_HEIGHT = 44;
    /** Vertical gap between the two dropdown sections (pixels). Minimal. */
    public static final int RUN_MANAGER_SECTION_GAP = 4;
    /** Y offset from section top to bar (label area height, pixels). */
    public static final int RUN_MANAGER_LABEL_TOP_OFFSET = 20;
    /** Max visible list rows when dropdown open. List scrolls if more items. */
    public static final int RUN_MANAGER_MAX_LIST_ROWS = 8;

    // --- KeyBind screen ---
    public static final int KEYBIND_ROW_HEIGHT = 22;
    public static final int KEYBIND_COLUMN_OFFSET = 20;
    public static final int KEYBIND_COLUMN_WIDTH = 160;
    public static final int KEYBIND_BUTTON_Y_OFFSET = 28;
    /** Selected row highlight color (ARGB). */
    public static final int KEYBIND_SELECTED_COLOR = 0xFFFFFF00;
    /** Y position where keybind list starts (pixels). */
    public static final int KEYBIND_LIST_TOP = 50;
    /** Default/unselected text color (ARGB). */
    public static final int KEYBIND_DEFAULT_COLOR = 0xFFFFFFFF;

    // --- Click protection (anti-spam, prevents wrong-button activation) ---
    /** Minimum ms between handled clicks on dropdowns/buttons. Ignores rapid repeated clicks. */
    public static final int CLICK_COOLDOWN_MS = 300;
    /** Minimum ms mouse must hover over element before click is accepted. Prevents accidental activation of recently hovered element. */
    public static final int CLICK_HOVER_STABILITY_MS = 80;

    // --- Status screen text ---
    /** Default text color for status content (ARGB). */
    public static final int STATUS_DEFAULT_TEXT_COLOR = 0xFFE0E0E0;
    /** Title color (ARGB). */
    public static final int TITLE_COLOR = 0xFFFFFFFF;

    // --- Run Manager loading ---
    /** "Loading..." text color (ARGB). */
    public static final int RUN_MANAGER_LOADING_COLOR = 0xFFAAAAAA;
    /** Y offset for loading text (pixels from center). */
    public static final int RUN_MANAGER_LOADING_Y_OFFSET = 4;

    // --- Locators screen ---
    /** Vertical offset from preview to content buttons (pixels). */
    public static final int LOCATORS_CONTENT_OFFSET = 24;
    /** Side margin for two-column layout (pixels). */
    public static final int LOCATORS_PANEL_MARGIN = 16;
    /** Y offset above preview bar for deranked label (pixels). */
    public static final int LOCATORS_DERANKED_LABEL_OFFSET = 14;
    /** Deranked message text. */
    public static final String LOCATORS_DERANKED_MESSAGE = "Run deranked (locators used)";
    /** Width of dropdown overlay list (pixels). Bar height from GSRRunHistoryParameters.SELECTOR_BAR_HEIGHT. */
    public static final int LOCATORS_DROPDOWN_LIST_WIDTH = 200;
    /** Height of each toggle row (label + bar) on Locators screen (pixels). */
    public static final int LOCATORS_TOGGLE_ROW_HEIGHT = 36;

    // --- GSR Preferences screen (custom, uses GSRMultiSelectDropdown) ---
    /** Content top (pixels from screen top). */
    public static final int PREFERENCES_CONTENT_TOP = 40;
    /** Content horizontal margin (pixels). */
    public static final int PREFERENCES_CONTENT_MARGIN = 24;
    /** Width of dropdown overlay list (pixels). Matches Locators/Run History. */
    public static final int PREFERENCES_DROPDOWN_LIST_WIDTH = 220;
    /** Height of label area above each bar (pixels). Label is left-aligned above the button. */
    public static final int PREFERENCES_LABEL_AREA_HEIGHT = 10;
    /** Row height for each option (label area + bar) in pixels. */
    public static final int PREFERENCES_ROW_HEIGHT = 34;
    /** Gap between the two columns (pixels). */
    public static final int PREFERENCES_COL_GAP = 12;
    /** Gap between category header and first option (pixels). */
    public static final int PREFERENCES_CATEGORY_GAP = 2;
    /** Margin after each category, before next category title (pixels). */
    public static final int PREFERENCES_CATEGORY_MARGIN = 12;
    /** Height of category header row (pixels). */
    public static final int PREFERENCES_CATEGORY_HEADER_HEIGHT = 18;
    /** Minimum Y for dropdown overlay top (pixels). Ensures overlay stays below title. */
    public static final int PREFERENCES_DROPDOWN_MIN_TOP = 40;
    /** Dropdown overlay: half-height for vertical centering (pixels). Overlay is 2× this value tall. */
    public static final int PREFERENCES_DROPDOWN_OVERLAY_HALF_HEIGHT = 120;
    /** Dropdown overlay: full height in pixels. */
    public static final int PREFERENCES_DROPDOWN_OVERLAY_HEIGHT = 240;
    /** Gap between content bottom and footer row (pixels). */
    public static final int FOOTER_CONTENT_GAP = 16;
    /** Scroll step multiplier for content area (pixels per wheel tick). */
    public static final int CONTENT_SCROLL_STEP = 20;
    /** Horizontal divider between categories: color (ARGB), height (pixels). */
    public static final int PREFERENCES_CATEGORY_DIVIDER_COLOR = 0xFF404040;
    public static final int PREFERENCES_CATEGORY_DIVIDER_HEIGHT = 1;
    /** Gap between divider line and next category label (pixels). Prevents overlap. */
    public static final int PREFERENCES_CATEGORY_DIVIDER_GAP = 6;
    /** Locator HUD preview next to category label: bar width, bar height, icon radius (pixels). */
    public static final int PREFERENCES_LOCATOR_PREVIEW_WIDTH = 72;
    public static final int PREFERENCES_LOCATOR_PREVIEW_BAR_HEIGHT = 4;
    public static final int PREFERENCES_LOCATOR_PREVIEW_ICON_RADIUS = 5;
    /** Gap between category label and locator preview (pixels). */
    public static final int PREFERENCES_LOCATOR_PREVIEW_LABEL_GAP = 8;
    /** Full-screen blur overlay when dropdown open (ARGB). Very light dim so page stays visible; blocks background interaction. Used by Preferences, Locators, Run History, Export CSV, Run Manager. */
    public static final int PREFERENCES_DROPDOWN_OVERLAY_DIM = 0x20000000;
    /** Alias for shared dropdown overlay. Same as PREFERENCES_DROPDOWN_OVERLAY_DIM. */
    public static final int DROPDOWN_OVERLAY_DIM = PREFERENCES_DROPDOWN_OVERLAY_DIM;
    /** Toggle ON color for Anti-Cheat (ARGB). Green. */
    public static final int PREFERENCES_TOGGLE_ANTICHEAT_ON = 0xFF4CAF50;
    /** Toggle OFF color for Anti-Cheat (ARGB). Gray. */
    public static final int PREFERENCES_TOGGLE_ANTICHEAT_OFF = 0xFF9E9E9E;
    /** Toggle ON color for New World Before Run Ends (ARGB). Green. */
    public static final int PREFERENCES_TOGGLE_NEWWORLD_ON = 0xFF4CAF50;
    /** Toggle OFF color for New World Before Run Ends (ARGB). Gray. */
    public static final int PREFERENCES_TOGGLE_NEWWORLD_OFF = 0xFF9E9E9E;
    /** Toggle ON color for Timer Display Side, Compass Display Height (ARGB). */
    public static final int PREFERENCES_TOGGLE_NEUTRAL_ON = 0xFF4CAF50;
    /** Toggle OFF color for neutral options (ARGB). Gray. */
    public static final int PREFERENCES_TOGGLE_NEUTRAL_OFF = 0xFF9E9E9E;
    /** Size of toggle button icon (pixels). */
    public static final int PREFERENCES_TOGGLE_ICON_SIZE = 16;
    /** Margin inside toggle bar around icon (pixels). */
    public static final int PREFERENCES_TOGGLE_ICON_MARGIN = 2;

    // --- Pause menu (Save and Quit to Title | GSR Controls row) ---
    /** Total row width (pixels). Matches vanilla grid: 2*98 + 4 = 200. */
    public static final int GAME_MENU_ROW_TOTAL_WIDTH = 200;
    /** Gap between the three buttons (pixels). Matches vanilla GRID_MARGIN. */
    public static final int GAME_MENU_ROW_GAP = 4;
    /** Button height for Options / Open to LAN / GSR Options (pixels). Matches vanilla. */
    public static final int GAME_MENU_ROW_BUTTON_HEIGHT = 20;

    // --- Controls Options screen (GSR Key Binds button) ---
    /** Button width (pixels). */
    public static final int CONTROLS_OPTIONS_BUTTON_WIDTH = 200;
    /** Button height (pixels). */
    public static final int CONTROLS_OPTIONS_BUTTON_HEIGHT = 20;
    /** Y offset from bottom for button (pixels). */
    public static final int CONTROLS_OPTIONS_BUTTON_Y_OFFSET = 52;
}
