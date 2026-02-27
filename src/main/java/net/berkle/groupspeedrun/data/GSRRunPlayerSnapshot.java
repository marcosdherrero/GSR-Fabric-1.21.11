package net.berkle.groupspeedrun.data;

/**
 * Per-run per-player aggregated stats. One row per (runId, playerUuid).
 * Used for leaderboards and run-end broadcast "best in category".
 */
public record GSRRunPlayerSnapshot(
    String runId,
    String playerUuid,
    String playerName,
    float damageDealt,
    float damageTaken,
    /** Most damage dealt type id (e.g. minecraft:player_attack); null if none. */
    String mostDamageDealtTypeId,
    /** Amount from most damage dealt type; 0 if none. */
    float mostDamageDealtTypeAmount,
    /** Most damage taken type id; null if none. */
    String mostDamageTakenTypeId,
    /** Amount from most damage taken type; 0 if none. */
    float mostDamageTakenTypeAmount,
    float dragonDamage,
    int enderPearls,
    int blazeRods,
    int blocksPlaced,
    int blocksBroken,
    /** Most placed block id (e.g. minecraft:oak_log); null if none. */
    String mostPlacedBlockId,
    /** Count of most placed block; 0 if none. */
    int mostPlacedBlockCount,
    /** Most broken block id (e.g. minecraft:stone); null if none. */
    String mostBrokenBlockId,
    /** Count of most broken block; 0 if none. */
    int mostBrokenBlockCount,
    float distanceMoved,
    /** Most traveled type id (walk, sprint, swim, fly, climb, fall); null if none. */
    String mostTraveledTypeId,
    /** Distance of most traveled type; 0 if none. */
    float mostTraveledTypeAmount,
    /** Total entities killed; 0 if none. */
    int entityKills,
    /** Most killed entity type id (e.g. minecraft:zombie); null if none. */
    String mostKilledEntityId,
    /** Count of most killed entity; 0 if none. */
    int mostKilledEntityCount,
    /** Total food items eaten; 0 if none. */
    int foodEaten,
    /** Most eaten food item id (e.g. minecraft:cooked_beef); null if none. */
    String mostEatenFoodId,
    /** Count of most eaten food; 0 if none. */
    int mostEatenFoodCount,
    /** Screen time in ticks (20 ticks = 1 second); 0 if none. */
    long screenTimeTicks,
    /** Most used screen type id (e.g. minecraft:generic_9x3); null if none. */
    String mostUsedScreenId,
    /** Ticks in most used screen; 0 if none. */
    long mostUsedScreenTicks,
    /** Fall damage taken; 0 if none. */
    float fallDamageTaken
) {}
