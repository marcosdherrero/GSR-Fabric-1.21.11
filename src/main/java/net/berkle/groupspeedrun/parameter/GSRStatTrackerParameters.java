package net.berkle.groupspeedrun.parameter;

/**
 * Consolidated metadata for all tracked stats: display names (quips), icons, descriptions, and units.
 * Single source of truth for {@link net.berkle.groupspeedrun.GSRStats} leaderboard,
 * {@link net.berkle.groupspeedrun.managers.GSRBroadcastManager}, and any future UI.
 * Edit this file to change tracker text across the mod.
 */
public final class GSRStatTrackerParameters {

    private GSRStatTrackerParameters() {}

    // --- Pearl Jam (Ender Pearls) ---
    /** Display name / quip. */
    public static final String PEARL_JAM_NAME = "Pearl Jam";
    /** Unicode icon for broadcast. */
    public static final String PEARL_JAM_ICON = "🔮";
    /** Minecraft § color code for broadcast. */
    public static final String PEARL_JAM_COLOR = "§3";
    /** Description (e.g. for tooltips). */
    public static final String PEARL_JAM_DESCRIPTION = "Most ender pearls collected";
    /** Unit suffix for value display. */
    public static final String PEARL_JAM_UNIT = " pearls";

    // --- Pog Champ (Blaze Rods) ---
    public static final String POG_CHAMP_NAME = "Pog Champ";
    public static final String POG_CHAMP_ICON = "🔥";
    public static final String POG_CHAMP_COLOR = "§e";
    public static final String POG_CHAMP_DESCRIPTION = "Most blaze rods collected";
    public static final String POG_CHAMP_UNIT = " blaze rods";

    // --- ADC (Damage Dealt) ---
    public static final String ADC_NAME = "ADC";
    public static final String ADC_ICON = "🏹";
    public static final String ADC_COLOR = "§6";
    public static final String ADC_DESCRIPTION = "Most damage dealt";
    public static final String ADC_UNIT = " damage done";

    // --- Tank (Damage Taken) ---
    public static final String TANK_NAME = "Tank";
    public static final String TANK_ICON = "❈";
    public static final String TANK_COLOR = "§4";
    public static final String TANK_DESCRIPTION = "Most damage taken";
    public static final String TANK_UNIT = " hearts";

    // --- Sightseer (Distance) ---
    public static final String SIGHTSEER_NAME = "Sightseer";
    public static final String SIGHTSEER_ICON = "👣";
    public static final String SIGHTSEER_COLOR = "§f";
    public static final String SIGHTSEER_DESCRIPTION = "Most blocks traveled";
    public static final String SIGHTSEER_UNIT = " blocks";

    // --- Builder (placed) ---
    public static final String BUILDER_PLACED_NAME = "Builder (placed)";
    public static final String BUILDER_PLACED_ICON = "🔨";
    public static final String BUILDER_PLACED_COLOR = "§2";
    public static final String BUILDER_PLACED_DESCRIPTION = "Most blocks placed";
    public static final String BUILDER_PLACED_UNIT = " blocks";

    // --- Builder (broken) ---
    public static final String BUILDER_BROKEN_NAME = "Builder (broken)";
    public static final String BUILDER_BROKEN_ICON = "⛏";
    public static final String BUILDER_BROKEN_COLOR = "§2";
    public static final String BUILDER_BROKEN_DESCRIPTION = "Most blocks broken";
    public static final String BUILDER_BROKEN_UNIT = " blocks";

    // --- Dragon Warrior ---
    public static final String DRAGON_WARRIOR_NAME = "Dragon Warrior";
    public static final String DRAGON_WARRIOR_ICON = "🐉";
    public static final String DRAGON_WARRIOR_COLOR = "§5";
    public static final String DRAGON_WARRIOR_DESCRIPTION = "Most damage dealt to the Ender Dragon";
    public static final String DRAGON_WARRIOR_UNIT = " damage";

    // --- Healer ---
    public static final String HEALER_NAME = "Healer";
    public static final String HEALER_ICON = "❤";
    public static final String HEALER_COLOR = "§d";
    public static final String HEALER_DESCRIPTION = "Most HP healed";
    public static final String HEALER_UNIT = " HP healed";

    // --- Brew Master ---
    public static final String BREW_MASTER_NAME = "Brew Master";
    public static final String BREW_MASTER_ICON = "🧪";
    public static final String BREW_MASTER_COLOR = "§b";
    public static final String BREW_MASTER_DESCRIPTION = "Most potions consumed";
    public static final String BREW_MASTER_UNIT = " potions";

    // --- Defender ---
    public static final String DEFENDER_NAME = "Defender";
    public static final String DEFENDER_ICON = "🛡";
    public static final String DEFENDER_COLOR = "§b";
    public static final String DEFENDER_DESCRIPTION = "Highest armor rating";
    public static final String DEFENDER_UNIT = " armor";

    // --- Screen Addict ---
    public static final String SCREEN_ADDICT_NAME = "Screen Addict";
    public static final String SCREEN_ADDICT_ICON = "🗃";
    public static final String SCREEN_ADDICT_COLOR = "§3";
    public static final String SCREEN_ADDICT_DESCRIPTION = "Most time in inventories";
    public static final String SCREEN_ADDICT_UNIT = " ticks";

    // --- Killer (Entity Kills) ---
    public static final String KILLER_NAME = "Killer";
    public static final String KILLER_ICON = "⚔";
    public static final String KILLER_COLOR = "§c";
    public static final String KILLER_DESCRIPTION = "Most entities killed";
    public static final String KILLER_UNIT = " kills";

    // --- Food Eater ---
    public static final String FOOD_EATER_NAME = "Food Eater";
    public static final String FOOD_EATER_ICON = "🍖";
    public static final String FOOD_EATER_COLOR = "§6";
    public static final String FOOD_EATER_DESCRIPTION = "Most food eaten";
    public static final String FOOD_EATER_UNIT = " eaten";

    // --- Fall Damage ---
    public static final String FALL_DAMAGE_NAME = "Fall Damage";
    public static final String FALL_DAMAGE_ICON = "⬇";
    public static final String FALL_DAMAGE_COLOR = "§8";
    public static final String FALL_DAMAGE_DESCRIPTION = "Most fall damage taken";
    public static final String FALL_DAMAGE_UNIT = " hearts";

    /** Build broadcast label: color + icon + name. */
    public static String broadcastLabel(String color, String icon, String name) {
        return color + " " + icon + " " + name;
    }
}
