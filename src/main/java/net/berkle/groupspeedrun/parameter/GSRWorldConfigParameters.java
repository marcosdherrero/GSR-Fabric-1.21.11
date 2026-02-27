package net.berkle.groupspeedrun.parameter;

/**
 * Parameters for world run config persistence: NBT keys and save file name.
 * Used by {@link net.berkle.groupspeedrun.config.GSRConfigWorld}.
 */
public final class GSRWorldConfigParameters {

    private GSRWorldConfigParameters() {}

    /** Filename under gsr_folder/worlds/&lt;world&gt;/ for the run state. */
    public static final String NBT_FILE = "groupspeedrun_world.json";

    // --- NBT keys (run state) ---
    public static final String K_START_TIME = "startTime";
    public static final String K_IS_FROZEN = "isTimerFrozen";
    public static final String K_FROZEN_TIME = "frozenTime";
    /** True when freeze was caused by server stop or last player disconnect; enables auto-resume on next join. */
    public static final String K_FROZEN_BY_SERVER_STOP = "frozenByServerStop";
    /** True when admin manually paused; button shows Resume. False when running or frozen by server/menu. */
    public static final String K_MANUAL_PAUSE = "manualPause";
    public static final String K_IS_VICTORIOUS = "isVictorious";
    public static final String K_IS_FAILED = "isFailed";
    public static final String K_GROUP_DEATH = "groupDeathEnabled";
    public static final String K_SHARED_HEALTH = "sharedHealthEnabled";
    public static final String K_FAILED_BY_NAME = "failedByPlayerName";
    public static final String K_FAILED_BY_MSG = "failedByDeathMessage";
    public static final String K_RUN_PARTICIPANT_COUNT = "runParticipantCount";
    public static final String K_DRAGON_WARRIOR_NAME = "dragonWarriorName";
    public static final String K_DRAGON_WARRIOR_DAMAGE = "dragonWarriorDamage";
    /** Lowest difficulty ordinal during run (0–3). -1 = not set. */
    public static final String K_LOWEST_DIFFICULTY_ORDINAL = "lowestDifficultyOrdinal";

    // --- NBT keys (split times, ms) ---
    public static final String K_T_NETHER = "timeNether";
    public static final String K_T_BASTION = "timeBastion";
    public static final String K_T_FORTRESS = "timeFortress";
    public static final String K_T_END = "timeEnd";
    public static final String K_T_DRAGON = "timeDragon";
    public static final String K_L_SPLIT = "lastSplitTime";
    /** First time any player used an ender eye (thrown) this run; for stronghold locator 30 min gate. */
    public static final String K_T_FIRST_ENDER_EYE = "timeFirstEnderEye";

    /** Run is deranked (invalid for ranking) because locators were used (menu or admin). */
    public static final String K_LOCATOR_DERANKED = "locatorDeranked";

    /** Host setting: when true, locator use invalidates runs. When false, runs are never invalidated for locators. */
    public static final String K_ANTI_CHEAT_ENABLED = "antiCheatEnabled";
    /** When true, run auto-starts on first movement/block break. When false, admin must press Start. Default true. */
    public static final String K_AUTO_START_ENABLED = "autoStartEnabled";
    /** When non-admins can use locators: 0=Never, 1=Always, 2=30 min post previous split. */
    public static final String K_LOCATOR_NON_ADMIN_MODE = "locatorNonAdminMode";
    /** Effective allowNewWorldBeforeRunEnd from designated admin; sync-only, not persisted. */
    public static final String K_EFFECTIVE_ALLOW_NEW_WORLD_BEFORE_RUN_END = "effectiveAllowNewWorldBeforeRunEnd";
    /** First elapsed ms when player reentered overworld after both fortress and bastion; for stronghold locator gate. */
    public static final String K_T_FIRST_OVERWORLD_RETURN = "timeFirstOverworldReturnAfterNether";

    // --- NBT keys (locator HUD: structure tracking) ---
    /** World has structure located; toggles are per-player. */
    public static final String K_FORT_LOCATED = "fortressLocated";
    public static final String K_BAST_LOCATED = "bastionLocated";
    public static final String K_STRONGHOLD_LOCATED = "strongholdLocated";
    public static final String K_SHIP_LOCATED = "shipLocated";
    /** Legacy keys (backward compat). */
    public static final String K_FORT_ACTIVE = "fortressActive";
    public static final String K_BAST_ACTIVE = "bastionActive";
    public static final String K_STRONGHOLD_ACTIVE = "strongholdActive";
    public static final String K_SHIP_ACTIVE = "shipActive";
    public static final String K_FORT_X = "fortressX";
    public static final String K_FORT_Z = "fortressZ";
    public static final String K_BAST_X = "bastionX";
    public static final String K_BAST_Z = "bastionZ";
    public static final String K_STRONGHOLD_X = "strongholdX";
    public static final String K_STRONGHOLD_Z = "strongholdZ";
    public static final String K_SHIP_X = "shipX";
    public static final String K_SHIP_Y = "shipY";
    public static final String K_SHIP_Z = "shipZ";

    /** Locator fade-out when player enters structure and it was the only one. */
    public static final String K_LOCATOR_FADE_START = "locatorFadeStartTime";
    public static final String K_LOCATOR_FADE_TYPE = "locatorFadeType";

    // --- Run Manager: per-player participant lists ---
    /** UUIDs in group death pool (their death fails run). Empty = all players (legacy). */
    public static final String K_GROUP_DEATH_PARTICIPANTS = "groupDeathParticipants";
    /** UUIDs in shared health pool. Empty = all players when sharedHealthEnabled. */
    public static final String K_SHARED_HEALTH_PARTICIPANTS = "sharedHealthParticipants";
    /** UUIDs excluded from run: no stats, no triggers, no group death/health. */
    public static final String K_EXCLUDED_FROM_RUN = "excludedFromRun";
}
