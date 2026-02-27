package net.berkle.groupspeedrun.parameter;

import net.minecraft.text.Text;

/**
 * Button labels and screen titles for GSR UI. Centralizes all button text for consistency
 * and easier localization. Used by GSR screens (Controls, Run History, Locators, etc.).
 */
public final class GSRButtonParameters {

    private GSRButtonParameters() {}

    // --- Main menu (title screen) ---
    /** Run History button on main menu. Opens run history screen. */
    public static final String TITLE_RUN_HISTORY = "GSR Run History";
    /** GSR Controls button on main menu. Opens GSR controls screen (Run Manager, Locators, etc.). */
    public static final String TITLE_GSR_CONTROLS = "GSR Controls";
    /** New World button on main menu. Opens create world screen. */
    public static final String TITLE_NEW_WORLD = "New World";
    /** GSR Options button on main menu. Opens GSR controls screen. */
    public static final String TITLE_GSR_OPTIONS = "GSR Options";
    /** GSR Config button on main menu. Opens GSR preferences (config) screen. */
    public static final String TITLE_GSR_CONFIG = "GSR Config";

    // --- Pause menu (ESC) ---
    /** Save and quit button. Short label for pause menu exit row. */
    public static final String GAME_MENU_SAVE_QUIT = "Save and Quit";
    /** Horizontal padding inside title screen buttons (pixels). */
    public static final int TITLE_BUTTON_PADDING_H = 4;
    /** Vertical padding inside title screen buttons (pixels). */
    public static final int TITLE_BUTTON_PADDING_V = 2;
    /** Text scale for title screen buttons to fit label with padding (0.82 = 82%). */
    public static final float TITLE_BUTTON_TEXT_SCALE = 0.82f;
    /** Title button text color when active (ARGB). */
    public static final int TITLE_BUTTON_TEXT_ACTIVE = 0xFFFFFFFF;
    /** Title button text color when inactive (ARGB). */
    public static final int TITLE_BUTTON_TEXT_INACTIVE = 0xFFA0A0A0;
    /** Realms button width threshold: scale text when width is below this (pixels). */
    public static final int REALMS_BUTTON_SCALE_THRESHOLD = 120;

    // --- Shared footer (Back / Close) ---
    /** Back button. Returns to parent screen. */
    public static final String FOOTER_BACK = "Back";
    /** Back button. Closes screen and returns to game. */
    public static final String FOOTER_CLOSE = "Back";

    // --- GSR Controls screen ---
    /** Start run (admin). Shown when run has not started yet. */
    public static final String CONTROLS_START = "Start Run";
    /** Pause run (admin). Shown when run is active and running. */
    public static final String CONTROLS_PAUSE = "Pause Run";
    /** Resume run (admin). Shown when run is active and paused. */
    public static final String CONTROLS_RESUME = "Resume Run";
    /** Reset run (admin). */
    public static final String CONTROLS_RESET = "Reset Run";
    /** This run manager. Opens run manager screen. */
    public static final String CONTROLS_RUN_MANAGER = "This Run Mngr";
    /** Locators. Opens locators screen (requires active world). */
    public static final String CONTROLS_LOCATORS = "Locators";
    /** GSR Run History. Opens run history screen (same as main menu). */
    public static final String CONTROLS_RUN_HISTORY = "GSR Run History";
    /** GSR Config. Opens mod config screen. */
    public static final String CONTROLS_PREFERENCES = "GSR Config";

    // --- Run Manager screen ---
    /** Manage group death settings. */
    public static final String RUN_MANAGER_DEATH_SETTINGS = "Manage Group Death Settings";
    /** Manage group health settings. */
    public static final String RUN_MANAGER_HEALTH_SETTINGS = "Manage Group Health Settings";
    /** Run Manager derank confirm: apply participant change anyway (invalidates run for ranking). */
    public static final String RUN_MANAGER_DERANK_CONFIRM_APPLY = "Apply Anyway";
    /** Run Manager derank confirm: cancel and keep previous participant settings. */
    public static final String RUN_MANAGER_DERANK_CONFIRM_CANCEL = "Cancel";

    // --- Participant screens (Group/Exclude) ---
    /** Group all participants. */
    public static final String PARTICIPANT_GROUP_ALL = "Group all";
    /** Exclude all participants. */
    public static final String PARTICIPANT_EXCLUDE_ALL = "Exclude all";
    /** Move selected right. */
    public static final String PARTICIPANT_MOVE_RIGHT = "->";
    /** Move selected left. */
    public static final String PARTICIPANT_MOVE_LEFT = "<-";
    /** Save and close. */
    public static final String PARTICIPANT_SAVE = "Save";

    // --- Locators screen ---
    /** Clear all locators. */
    public static final String LOCATORS_CLEAR = "Clear Locators";
    /** Fortress locator toggle. */
    public static final String LOCATORS_FORTRESS = "Fortress";
    /** Bastion locator toggle. */
    public static final String LOCATORS_BASTION = "Bastion";
    /** Stronghold locator toggle. */
    public static final String LOCATORS_STRONGHOLD = "Stronghold";
    /** End ship (wings) locator toggle. */
    public static final String LOCATORS_WINGS = "Wings (End Ship)";
    /** Locator invalidate confirm: turn on anyway (invalidates run for ranking). */
    public static final String LOCATOR_CONFIRM_TURN_ON = "Turn On Anyway";
    /** Locator invalidate confirm: cancel and keep locator off. */
    public static final String LOCATOR_CONFIRM_CANCEL = "Cancel";

    // --- Run History screen ---
    /** Export run history to CSV. */
    public static final String RUN_HISTORY_EXPORT_CSV = "Export CSV";
    /** Delete selected run from personal and shared lists. */
    public static final String RUN_HISTORY_DELETE_RUN = "Delete Run";
    /** Delete only the run currently being displayed (single delete). */
    public static final String RUN_HISTORY_DELETE_ONE = "Delete";
    /** Delete all runs currently selected in the Runs dropdown. */
    public static final String RUN_HISTORY_DELETE_ALL_SELECTED = "Delete All Selected";
    /** Export CSV popup: confirm download button. */
    public static final String EXPORT_CSV_EXPORT = "Export";

    // --- New World Confirm screen ---
    /** Save world and open Create World screen. */
    public static final String NEW_WORLD_SAVE_CREATE = "Save & Create New World";
    /** Cancel and return to parent. */
    public static final String NEW_WORLD_CANCEL = "Cancel";

    // --- Reset Run Confirm screen ---
    /** Confirm reset: wipe run data, teleport players, restore from snapshot. */
    public static final String RESET_CONFIRM = "Confirm Reset";
    /** Cancel and return to parent. */
    public static final String RESET_CANCEL = "Cancel";
    /** Reset confirm message: describes what reset does. */
    public static final String RESET_CONFIRM_MESSAGE = "Wipes run data, teleports players to spawn, restores world from snapshot.";

    // --- GSR Preferences screen ---
    /** Keybinds button. Opens vanilla Controls (keybinds) screen. */
    public static final String PREFERENCES_KEYBINDS = "Keybinds";
    /** Reset Mod Settings to defaults button. */
    public static final String PREFERENCES_RESET_MOD_SETTINGS = "Reset All to Default";
    /** Reset Mod Settings confirm: apply defaults. */
    public static final String PREFERENCES_RESET_CONFIRM = "Reset to Default";
    /** Reset Mod Settings confirm: cancel. */
    public static final String PREFERENCES_RESET_CANCEL = "Cancel";
    /** Reset Mod Settings confirm message. */
    public static final String PREFERENCES_RESET_CONFIRM_MESSAGE = "Resets HUD Scale, Visibility, Anti-Cheat, Auto Start, Locator Non-Admin, and New World Before Run Ends to defaults.";

    // --- Screen titles ---
    /** GSR Preferences screen title. */
    public static final String SCREEN_PREFERENCES = "GSR Preferences";
    /** Run History screen title. */
    public static final String SCREEN_RUN_HISTORY = "Run History";
    /** Run Manager screen title. */
    public static final String SCREEN_RUN_MANAGER = "Run Manager";
    /** GSR Controls screen title. */
    public static final String SCREEN_CONTROLS = "GSR Controls";
    /** GSR Locators screen title. */
    public static final String SCREEN_LOCATORS = "GSR Locators";

    // --- Helpers ---
    /** Returns Text.literal(s) for button labels. */
    public static Text literal(String label) {
        return Text.literal(label);
    }

    /** Returns Text for locator toggle button: "name: ON" or "name: OFF". */
    public static Text locatorToggle(String name, boolean active) {
        return Text.literal(name + ": " + (active ? "ON" : "OFF"));
    }
}
