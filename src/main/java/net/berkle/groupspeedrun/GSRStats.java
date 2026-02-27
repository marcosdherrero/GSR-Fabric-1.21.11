package net.berkle.groupspeedrun;

import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.berkle.groupspeedrun.parameter.GSRBroadcastParameters;
import net.berkle.groupspeedrun.parameter.GSRStatTrackerParameters;
import net.berkle.groupspeedrun.parameter.GSRStorageParameters;
import net.berkle.groupspeedrun.util.GSRFormatUtil;
import net.berkle.groupspeedrun.util.GSRJsonUtil;
import net.berkle.groupspeedrun.util.GSRStoragePaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * GSRStats: Per-player statistics. Phase 2: distance, damage, blocks, dragon damage. Persist to NBT.
 */
public final class GSRStats {

    private static final Logger LOGGER = LoggerFactory.getLogger("GSR-Stats");
    private static final String STATS_FILE = GSRStorageParameters.STATS_FILE;

    public static final Map<UUID, Float> DISTANCE_MOVED = new ConcurrentHashMap<>();
    /** Per-player per-travel-type distance. Key: walk, sprint, swim, fly, climb, fall. */
    public static final Map<UUID, Map<String, Float>> DISTANCE_MOVED_BY_TYPE = new ConcurrentHashMap<>();
    public static final Map<UUID, Float> TOTAL_DAMAGE_DEALT = new ConcurrentHashMap<>();
    public static final Map<UUID, Float> TOTAL_DAMAGE_TAKEN = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> BLOCKS_PLACED = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> BLOCKS_BROKEN = new ConcurrentHashMap<>();
    /** Per-player per-block-type counts for most-placed/most-broken. Key: block id (e.g. minecraft:stone). */
    public static final Map<UUID, Map<String, Integer>> BLOCKS_PLACED_BY_TYPE = new ConcurrentHashMap<>();
    public static final Map<UUID, Map<String, Integer>> BLOCKS_BROKEN_BY_TYPE = new ConcurrentHashMap<>();
    public static final Map<UUID, Float> DRAGON_DAMAGE_MAP = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> ENDER_PEARLS_COLLECTED = new ConcurrentHashMap<>();
    public static final Map<UUID, Integer> BLAZE_RODS_COLLECTED = new ConcurrentHashMap<>();
    /** Total entities killed per player. */
    public static final Map<UUID, Integer> ENTITY_KILLS = new ConcurrentHashMap<>();
    /** Per-player per-entity-type kill counts. Key: entity type id (e.g. minecraft:zombie). */
    public static final Map<UUID, Map<String, Integer>> ENTITY_KILLS_BY_TYPE = new ConcurrentHashMap<>();
    /** Total food items eaten per player. */
    public static final Map<UUID, Integer> FOOD_EATEN = new ConcurrentHashMap<>();
    /** Per-player per-item-type food eaten. Key: item id (e.g. minecraft:cooked_beef). */
    public static final Map<UUID, Map<String, Integer>> FOOD_EATEN_BY_TYPE = new ConcurrentHashMap<>();
    /** Screen time in ticks (20 ticks = 1 second). Includes all screens (chest, furnace, player inventory via client report, etc.). */
    public static final Map<UUID, Long> SCREEN_TIME_TICKS = new ConcurrentHashMap<>();
    /** Per-player per-screen-type time. Key: screen handler type id (e.g. minecraft:generic_9x3). */
    public static final Map<UUID, Map<String, Long>> SCREEN_TIME_BY_TYPE = new ConcurrentHashMap<>();
    /** Fall damage taken per player. */
    public static final Map<UUID, Float> FALL_DAMAGE_TAKEN = new ConcurrentHashMap<>();
    /** Per-player per-damage-type dealt. Key: damage type id (e.g. minecraft:player_attack). */
    public static final Map<UUID, Map<String, Float>> DAMAGE_DEALT_BY_TYPE = new ConcurrentHashMap<>();
    /** Per-player per-damage-type taken. Key: damage type id (e.g. minecraft:player_attack). */
    public static final Map<UUID, Map<String, Float>> DAMAGE_TAKEN_BY_TYPE = new ConcurrentHashMap<>();

    private GSRStats() {}

    /** True if this player should be counted for stats (not excluded from run). */
    public static boolean shouldRecordForPlayer(UUID uuid) {
        if (uuid == null) return false;
        GSRConfigWorld config = GSRMain.CONFIG;
        return config == null || !config.excludedFromRun.contains(uuid);
    }

    @SuppressWarnings("null")
    public static void addFloat(Map<UUID, Float> map, UUID uuid, float amount) {
        if (uuid == null || amount == 0 || !shouldRecordForPlayer(uuid)) return;
        map.merge(uuid, amount, Float::sum);
    }

    @SuppressWarnings("null")
    public static void addInt(Map<UUID, Integer> map, UUID uuid, int amount) {
        if (uuid == null || amount == 0 || !shouldRecordForPlayer(uuid)) return;
        map.merge(uuid, amount, Integer::sum);
    }

    /** Records one block placed; updates both total and per-type count for most-placed. */
    public static void addBlockPlaced(UUID uuid, String blockId) {
        if (uuid == null || blockId == null || blockId.isEmpty() || !shouldRecordForPlayer(uuid)) return;
        addInt(BLOCKS_PLACED, uuid, 1);
        BLOCKS_PLACED_BY_TYPE.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).merge(blockId, 1, (a, b) -> (a != null ? a : 0) + (b != null ? b : 0));
    }

    /** Records distance moved by travel type. typeKey: walk, sprint, swim, fly, climb, fall. */
    public static void addDistanceMoved(UUID uuid, String typeKey, float amount) {
        if (uuid == null || amount <= 0 || !shouldRecordForPlayer(uuid)) return;
        addFloat(DISTANCE_MOVED, uuid, amount);
        String key = (typeKey != null && !typeKey.isEmpty()) ? typeKey : "walk";
        DISTANCE_MOVED_BY_TYPE.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).merge(key, amount, Float::sum);
    }

    /** Returns the travel type id with most distance for a player; null if none. */
    public static String getMostTraveledTypeId(UUID uuid) {
        Map<String, Float> byType = DISTANCE_MOVED_BY_TYPE.get(uuid);
        if (byType == null || byType.isEmpty()) return null;
        var best = byType.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        return best != null ? best.getKey() : null;
    }

    /** Returns the distance of the most-traveled type for a player; 0 if none. */
    public static float getMostTraveledTypeAmount(UUID uuid) {
        Map<String, Float> byType = DISTANCE_MOVED_BY_TYPE.get(uuid);
        if (byType == null || byType.isEmpty()) return 0f;
        var best = byType.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        return best != null ? best.getValue() : 0f;
    }

    /** Records one block broken; updates both total and per-type count for most-broken. */
    public static void addBlockBroken(UUID uuid, String blockId) {
        if (uuid == null || blockId == null || blockId.isEmpty() || !shouldRecordForPlayer(uuid)) return;
        addInt(BLOCKS_BROKEN, uuid, 1);
        BLOCKS_BROKEN_BY_TYPE.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).merge(blockId, 1, (a, b) -> (a != null ? a : 0) + (b != null ? b : 0));
    }

    /** Returns the most-placed block id for a player; null if none. */
    public static String getMostPlacedBlockId(UUID uuid) {
        Map<String, Integer> byType = BLOCKS_PLACED_BY_TYPE.get(uuid);
        if (byType == null || byType.isEmpty()) return null;
        var best = byType.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        return best != null ? best.getKey() : null;
    }

    /** Returns the count of the most-placed block for a player; 0 if none. */
    public static int getMostPlacedBlockCount(UUID uuid) {
        Map<String, Integer> byType = BLOCKS_PLACED_BY_TYPE.get(uuid);
        if (byType == null || byType.isEmpty()) return 0;
        var best = byType.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        return best != null ? best.getValue() : 0;
    }

    /** Returns the most-broken block id for a player; null if none. */
    public static String getMostBrokenBlockId(UUID uuid) {
        Map<String, Integer> byType = BLOCKS_BROKEN_BY_TYPE.get(uuid);
        if (byType == null || byType.isEmpty()) return null;
        var best = byType.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        return best != null ? best.getKey() : null;
    }

    /** Returns the count of the most-broken block for a player; 0 if none. */
    public static int getMostBrokenBlockCount(UUID uuid) {
        Map<String, Integer> byType = BLOCKS_BROKEN_BY_TYPE.get(uuid);
        if (byType == null || byType.isEmpty()) return 0;
        var best = byType.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        return best != null ? best.getValue() : 0;
    }

    /** Records one entity kill; updates both total and per-type count. */
    public static void addEntityKill(UUID uuid, String entityTypeId) {
        if (uuid == null || entityTypeId == null || entityTypeId.isEmpty() || !shouldRecordForPlayer(uuid)) return;
        addInt(ENTITY_KILLS, uuid, 1);
        ENTITY_KILLS_BY_TYPE.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).merge(entityTypeId, 1, (a, b) -> (a != null ? a : 0) + (b != null ? b : 0));
    }

    /** Returns the most-killed entity type id; null if none. */
    public static String getMostKilledEntityId(UUID uuid) {
        Map<String, Integer> byType = ENTITY_KILLS_BY_TYPE.get(uuid);
        if (byType == null || byType.isEmpty()) return null;
        var best = byType.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        return best != null ? best.getKey() : null;
    }

    /** Returns the count of the most-killed entity; 0 if none. */
    public static int getMostKilledEntityCount(UUID uuid) {
        Map<String, Integer> byType = ENTITY_KILLS_BY_TYPE.get(uuid);
        if (byType == null || byType.isEmpty()) return 0;
        var best = byType.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        return best != null ? best.getValue() : 0;
    }

    /** Records one food item eaten; updates both total and per-type count. */
    public static void addFoodEaten(UUID uuid, String itemId) {
        if (uuid == null || itemId == null || itemId.isEmpty() || !shouldRecordForPlayer(uuid)) return;
        addInt(FOOD_EATEN, uuid, 1);
        FOOD_EATEN_BY_TYPE.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).merge(itemId, 1, (a, b) -> (a != null ? a : 0) + (b != null ? b : 0));
    }

    /** Returns the most-eaten food item id; null if none. */
    public static String getMostEatenFoodId(UUID uuid) {
        Map<String, Integer> byType = FOOD_EATEN_BY_TYPE.get(uuid);
        if (byType == null || byType.isEmpty()) return null;
        var best = byType.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        return best != null ? best.getKey() : null;
    }

    /** Returns the count of the most-eaten food; 0 if none. */
    public static int getMostEatenFoodCount(UUID uuid) {
        Map<String, Integer> byType = FOOD_EATEN_BY_TYPE.get(uuid);
        if (byType == null || byType.isEmpty()) return 0;
        var best = byType.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        return best != null ? best.getValue() : 0;
    }

    /** Records screen time (1 tick). Call each tick when player has non-player screen open. */
    public static void addScreenTime(UUID uuid, String screenTypeId) {
        if (uuid == null || screenTypeId == null || screenTypeId.isEmpty() || !shouldRecordForPlayer(uuid)) return;
        SCREEN_TIME_TICKS.merge(uuid, 1L, Long::sum);
        SCREEN_TIME_BY_TYPE.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).merge(screenTypeId, 1L, (a, b) -> (a != null ? a : 0L) + (b != null ? b : 0L));
    }

    /** Returns the most-used screen type id; null if none. */
    public static String getMostUsedScreenId(UUID uuid) {
        Map<String, Long> byType = SCREEN_TIME_BY_TYPE.get(uuid);
        if (byType == null || byType.isEmpty()) return null;
        var best = byType.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        return best != null ? best.getKey() : null;
    }

    /** Returns the ticks spent in the most-used screen; 0 if none. */
    public static long getMostUsedScreenTicks(UUID uuid) {
        Map<String, Long> byType = SCREEN_TIME_BY_TYPE.get(uuid);
        if (byType == null || byType.isEmpty()) return 0;
        var best = byType.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        return best != null ? best.getValue() : 0;
    }

    /** Records damage dealt by type; call from damage tracker. typeId from DamageSource.getType().getKey().map(k -> k.getValue().toString()).orElse("unknown"). */
    public static void addDamageDealtByType(UUID uuid, String typeId, float amount) {
        if (uuid == null || amount <= 0 || !shouldRecordForPlayer(uuid)) return;
        addFloat(TOTAL_DAMAGE_DEALT, uuid, amount);
        String id = (typeId != null && !typeId.isEmpty()) ? typeId : "unknown";
        DAMAGE_DEALT_BY_TYPE.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).merge(id, amount, Float::sum);
    }

    /**
     * Returns damage type id from source for per-type tracking (e.g. minecraft:player_attack).
     * Used by damage tracker mixin and Fabric AFTER_DAMAGE handler.
     */
    public static String getDamageTypeId(ServerWorld world, DamageSource source) {
        if (world == null || source == null) return "unknown";
        DamageType type = source.getType();
        return world.getRegistryManager().getOrThrow(RegistryKeys.DAMAGE_TYPE).getKey(type).map(k -> k.getValue().toString()).orElse("unknown");
    }

    /** Records damage taken by type; call from damage tracker or Fabric AFTER_DAMAGE. */
    public static void addDamageTakenByType(UUID uuid, String typeId, float amount) {
        if (uuid == null || amount <= 0 || !shouldRecordForPlayer(uuid)) return;
        addFloat(TOTAL_DAMAGE_TAKEN, uuid, amount);
        String id = (typeId != null && !typeId.isEmpty()) ? typeId : "unknown";
        DAMAGE_TAKEN_BY_TYPE.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).merge(id, amount, Float::sum);
    }

    /** Returns the damage type id that dealt the most damage for a player; null if none. */
    public static String getMostDamageDealtTypeId(UUID uuid) {
        Map<String, Float> byType = DAMAGE_DEALT_BY_TYPE.get(uuid);
        if (byType == null || byType.isEmpty()) return null;
        var best = byType.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        return best != null ? best.getKey() : null;
    }

    /** Returns the amount of the most-dealt damage type; 0 if none. */
    public static float getMostDamageDealtTypeAmount(UUID uuid) {
        Map<String, Float> byType = DAMAGE_DEALT_BY_TYPE.get(uuid);
        if (byType == null || byType.isEmpty()) return 0f;
        var best = byType.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        return best != null ? best.getValue() : 0f;
    }

    /** Returns the damage type id that dealt the most damage taken by a player; null if none. */
    public static String getMostDamageTakenTypeId(UUID uuid) {
        Map<String, Float> byType = DAMAGE_TAKEN_BY_TYPE.get(uuid);
        if (byType == null || byType.isEmpty()) return null;
        var best = byType.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        return best != null ? best.getKey() : null;
    }

    /** Returns the amount of the most-taken damage type; 0 if none. */
    public static float getMostDamageTakenTypeAmount(UUID uuid) {
        Map<String, Float> byType = DAMAGE_TAKEN_BY_TYPE.get(uuid);
        if (byType == null || byType.isEmpty()) return 0f;
        var best = byType.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
        return best != null ? best.getValue() : 0f;
    }

    private static final double STAT_EPSILON = GSRBroadcastParameters.STAT_EPSILON;

    /**
     * Current run leaderboard lines (best player per category). Only includes online players.
     * Used for status screen when server builds the message.
     */
    public static List<String> getLeaderboardLines(MinecraftServer server) {
        List<String> out = new ArrayList<>();
        if (server == null) return out;

        addLineInt(out, server, GSRStatTrackerParameters.PEARL_JAM_NAME, ENDER_PEARLS_COLLECTED, GSRStatTrackerParameters.PEARL_JAM_UNIT);
        addLineInt(out, server, GSRStatTrackerParameters.POG_CHAMP_NAME, BLAZE_RODS_COLLECTED, GSRStatTrackerParameters.POG_CHAMP_UNIT);
        Map<UUID, String> mostDealtTypeId = new java.util.HashMap<>();
        Map<UUID, Float> mostDealtTypeAmount = new java.util.HashMap<>();
        for (UUID u : TOTAL_DAMAGE_DEALT.keySet()) {
            String id = getMostDamageDealtTypeId(u);
            if (id != null) {
                mostDealtTypeId.put(u, id);
                mostDealtTypeAmount.put(u, getMostDamageDealtTypeAmount(u));
            }
        }
        addLineFloatWithMost(out, server, GSRStatTrackerParameters.ADC_NAME, TOTAL_DAMAGE_DEALT, GSRStatTrackerParameters.ADC_UNIT, mostDealtTypeId, mostDealtTypeAmount);
        Map<UUID, String> mostTakenTypeId = new java.util.HashMap<>();
        Map<UUID, Float> mostTakenTypeAmount = new java.util.HashMap<>();
        for (UUID u : TOTAL_DAMAGE_TAKEN.keySet()) {
            String id = getMostDamageTakenTypeId(u);
            if (id != null) {
                mostTakenTypeId.put(u, id);
                mostTakenTypeAmount.put(u, getMostDamageTakenTypeAmount(u));
            }
        }
        addLineFloatWithMost(out, server, GSRStatTrackerParameters.TANK_NAME, TOTAL_DAMAGE_TAKEN, GSRStatTrackerParameters.TANK_UNIT, mostTakenTypeId, mostTakenTypeAmount);
        Map<UUID, String> mostTraveledTypeId = new java.util.HashMap<>();
        Map<UUID, Float> mostTraveledTypeAmount = new java.util.HashMap<>();
        for (UUID u : DISTANCE_MOVED.keySet()) {
            String id = getMostTraveledTypeId(u);
            if (id != null) {
                mostTraveledTypeId.put(u, id);
                mostTraveledTypeAmount.put(u, getMostTraveledTypeAmount(u));
            }
        }
        addLineFloatWithMost(out, server, GSRStatTrackerParameters.SIGHTSEER_NAME, DISTANCE_MOVED, GSRStatTrackerParameters.SIGHTSEER_UNIT, true, mostTraveledTypeId, mostTraveledTypeAmount, false);
        Map<UUID, String> mostPlacedId = new java.util.HashMap<>();
        Map<UUID, Integer> mostPlacedCount = new java.util.HashMap<>();
        Map<UUID, String> mostBrokenId = new java.util.HashMap<>();
        Map<UUID, Integer> mostBrokenCount = new java.util.HashMap<>();
        for (UUID u : BLOCKS_PLACED.keySet()) {
            String id = getMostPlacedBlockId(u);
            if (id != null) {
                mostPlacedId.put(u, id);
                mostPlacedCount.put(u, getMostPlacedBlockCount(u));
            }
        }
        for (UUID u : BLOCKS_BROKEN.keySet()) {
            String id = getMostBrokenBlockId(u);
            if (id != null) {
                mostBrokenId.put(u, id);
                mostBrokenCount.put(u, getMostBrokenBlockCount(u));
            }
        }
        addLineIntWithMost(out, server, GSRStatTrackerParameters.BUILDER_PLACED_NAME, BLOCKS_PLACED, GSRStatTrackerParameters.BUILDER_PLACED_UNIT, mostPlacedId, mostPlacedCount);
        addLineIntWithMost(out, server, GSRStatTrackerParameters.BUILDER_BROKEN_NAME, BLOCKS_BROKEN, GSRStatTrackerParameters.BUILDER_BROKEN_UNIT, mostBrokenId, mostBrokenCount);
        addLineFloat(out, server, GSRStatTrackerParameters.DRAGON_WARRIOR_NAME, DRAGON_DAMAGE_MAP, GSRStatTrackerParameters.DRAGON_WARRIOR_UNIT, true);
        Map<UUID, String> mostKilledId = new java.util.HashMap<>();
        Map<UUID, Integer> mostKilledCount = new java.util.HashMap<>();
        for (UUID u : ENTITY_KILLS.keySet()) {
            String id = getMostKilledEntityId(u);
            if (id != null) {
                mostKilledId.put(u, id);
                mostKilledCount.put(u, getMostKilledEntityCount(u));
            }
        }
        addLineIntWithMost(out, server, GSRStatTrackerParameters.KILLER_NAME, ENTITY_KILLS, GSRStatTrackerParameters.KILLER_UNIT, mostKilledId, mostKilledCount);
        Map<UUID, String> mostEatenId = new java.util.HashMap<>();
        Map<UUID, Integer> mostEatenCount = new java.util.HashMap<>();
        for (UUID u : FOOD_EATEN.keySet()) {
            String id = getMostEatenFoodId(u);
            if (id != null) {
                mostEatenId.put(u, id);
                mostEatenCount.put(u, getMostEatenFoodCount(u));
            }
        }
        addLineIntWithMost(out, server, GSRStatTrackerParameters.FOOD_EATER_NAME, FOOD_EATEN, GSRStatTrackerParameters.FOOD_EATER_UNIT, mostEatenId, mostEatenCount);
        Map<UUID, String> mostScreenId = new java.util.HashMap<>();
        Map<UUID, Long> mostScreenTicks = new java.util.HashMap<>();
        for (UUID u : SCREEN_TIME_TICKS.keySet()) {
            String id = getMostUsedScreenId(u);
            if (id != null) {
                mostScreenId.put(u, id);
                mostScreenTicks.put(u, getMostUsedScreenTicks(u));
            }
        }
        addLineLongWithMost(out, server, GSRStatTrackerParameters.SCREEN_ADDICT_NAME, SCREEN_TIME_TICKS, GSRStatTrackerParameters.SCREEN_ADDICT_UNIT, mostScreenId, mostScreenTicks);
        addLineFloat(out, server, GSRStatTrackerParameters.FALL_DAMAGE_NAME, FALL_DAMAGE_TAKEN, GSRStatTrackerParameters.FALL_DAMAGE_UNIT, true);
        return out;
    }

    private static String nameFor(MinecraftServer server, UUID uuid) {
        if (server == null || uuid == null) return null;
        var player = server.getPlayerManager().getPlayer(uuid);
        return player != null ? player.getName().getString() : null;
    }

    private static void addLineFloat(List<String> out, MinecraftServer server, String label, Map<UUID, Float> map, String unit, boolean allowZero) {
        addLineFloatWithMost(out, server, label, map, unit, allowZero, null, null);
    }

    private static void addLineFloatWithMost(List<String> out, MinecraftServer server, String label, Map<UUID, Float> map, String unit, Map<UUID, String> mostIdMap, Map<UUID, Float> mostAmountMap) {
        addLineFloatWithMost(out, server, label, map, unit, false, mostIdMap, mostAmountMap, true);
    }

    private static void addLineFloatWithMost(List<String> out, MinecraftServer server, String label, Map<UUID, Float> map, String unit, boolean allowZero, Map<UUID, String> mostIdMap, Map<UUID, Float> mostAmountMap) {
        addLineFloatWithMost(out, server, label, map, unit, allowZero, mostIdMap, mostAmountMap, true);
    }

    private static void addLineFloatWithMost(List<String> out, MinecraftServer server, String label, Map<UUID, Float> map, String unit, boolean allowZero, Map<UUID, String> mostIdMap, Map<UUID, Float> mostAmountMap, boolean showMostAmountInParens) {
        UUID best = null;
        float bestV = allowZero ? -1 : 0;
        for (Map.Entry<UUID, Float> e : map.entrySet()) {
            if (!shouldRecordForPlayer(e.getKey())) continue;
            float v = e.getValue();
            if (v > bestV && (allowZero || v >= STAT_EPSILON)) {
                bestV = v;
                best = e.getKey();
            }
        }
        if (best == null) return;
        String name = nameFor(server, best);
        if (name == null) return;
        boolean asHearts = GSRStatTrackerParameters.TANK_UNIT.equals(unit) || GSRStatTrackerParameters.FALL_DAMAGE_UNIT.equals(unit);
        String suffix = asHearts ? GSRFormatUtil.formatDamageAsHearts(bestV) : GSRFormatUtil.formatNumber(bestV) + unit;
        if (mostIdMap != null && mostAmountMap != null) {
            String mid = mostIdMap.get(best);
            Float mamt = mostAmountMap.get(best);
            if (mid != null && mamt != null && mamt > STAT_EPSILON) {
                String typeDisplay = GSRFormatUtil.formatDamageTypeForDisplay(mid);
                String mamtStr = asHearts ? GSRFormatUtil.formatNumber(mamt / 2.0) + " " : GSRFormatUtil.formatNumber(mamt) + " ";
                suffix = suffix + " §8(" + (showMostAmountInParens ? mamtStr : "") + typeDisplay + ")";
            }
        }
        out.add("§7  " + label + ": §b" + name + " §7(" + suffix + ")");
    }

    private static void addLineInt(List<String> out, MinecraftServer server, String label, Map<UUID, Integer> map, String unit) {
        addLineIntWithMost(out, server, label, map, unit, null, null);
    }

    private static void addLineIntWithMost(List<String> out, MinecraftServer server, String label, Map<UUID, Integer> map, String unit, Map<UUID, String> mostIdMap, Map<UUID, Integer> mostCountMap) {
        UUID best = null;
        int bestV = 0;
        for (Map.Entry<UUID, Integer> e : map.entrySet()) {
            if (!shouldRecordForPlayer(e.getKey())) continue;
            int v = e.getValue();
            if (v > bestV) {
                bestV = v;
                best = e.getKey();
            }
        }
        if (best == null || bestV <= 0) return;
        String name = nameFor(server, best);
        if (name == null) return;
        String suffix = GSRFormatUtil.formatNumber(bestV) + unit;
        if (mostIdMap != null && mostCountMap != null) {
            String mid = mostIdMap.get(best);
            Integer mcount = mostCountMap.get(best);
            if (mid != null && mcount != null && mcount > 0) {
                String blockDisplay = GSRFormatUtil.formatDamageTypeForDisplay(mid);
                suffix = GSRFormatUtil.formatNumber(bestV) + unit + " §8(" + GSRFormatUtil.formatNumber(mcount) + " " + blockDisplay + ")";
            }
        }
        out.add("§7  " + label + ": §b" + name + " §7(" + suffix + ")");
    }

    private static void addLineLongWithMost(List<String> out, MinecraftServer server, String label, Map<UUID, Long> map, String unit, Map<UUID, String> mostIdMap, Map<UUID, Long> mostTicksMap) {
        UUID best = null;
        long bestV = 0;
        for (Map.Entry<UUID, Long> e : map.entrySet()) {
            if (!shouldRecordForPlayer(e.getKey())) continue;
            long v = e.getValue();
            if (v > bestV) {
                bestV = v;
                best = e.getKey();
            }
        }
        if (best == null || bestV <= 0) return;
        String name = nameFor(server, best);
        if (name == null) return;
        String suffix = GSRFormatUtil.formatNumber(bestV) + unit;
        if (mostIdMap != null && mostTicksMap != null) {
            String mid = mostIdMap.get(best);
            Long mticks = mostTicksMap.get(best);
            if (mid != null && mticks != null && mticks > 0) {
                String screenDisplay = GSRFormatUtil.formatDamageTypeForDisplay(mid);
                suffix = GSRFormatUtil.formatNumber(bestV) + unit + " §8(" + GSRFormatUtil.formatNumber(mticks) + " " + screenDisplay + ")";
            }
        }
        out.add("§7  " + label + ": §b" + name + " §7(" + suffix + ")");
    }

    public static void reset() {
        DISTANCE_MOVED.clear();
        DISTANCE_MOVED_BY_TYPE.clear();
        TOTAL_DAMAGE_DEALT.clear();
        TOTAL_DAMAGE_TAKEN.clear();
        BLOCKS_PLACED.clear();
        BLOCKS_BROKEN.clear();
        BLOCKS_PLACED_BY_TYPE.clear();
        BLOCKS_BROKEN_BY_TYPE.clear();
        DRAGON_DAMAGE_MAP.clear();
        ENDER_PEARLS_COLLECTED.clear();
        BLAZE_RODS_COLLECTED.clear();
        ENTITY_KILLS.clear();
        ENTITY_KILLS_BY_TYPE.clear();
        FOOD_EATEN.clear();
        FOOD_EATEN_BY_TYPE.clear();
        SCREEN_TIME_TICKS.clear();
        SCREEN_TIME_BY_TYPE.clear();
        FALL_DAMAGE_TAKEN.clear();
        DAMAGE_DEALT_BY_TYPE.clear();
        DAMAGE_TAKEN_BY_TYPE.clear();
        LOGGER.info("[GSR] Statistics cleared.");
    }

    public static void save(MinecraftServer server) {
        if (server == null) return;
        Path worldDir = GSRStoragePaths.getWorldDir(server);
        Path path = worldDir.resolve(STATS_FILE);
        try {
            Files.createDirectories(worldDir);
            NbtCompound root = new NbtCompound();
            writeMapFloat(root, "distance", DISTANCE_MOVED);
            writeMapMapFloat(root, "distanceByType", DISTANCE_MOVED_BY_TYPE);
            writeMapFloat(root, "damageDealt", TOTAL_DAMAGE_DEALT);
            writeMapFloat(root, "damageTaken", TOTAL_DAMAGE_TAKEN);
            writeMapInt(root, "blocksPlaced", BLOCKS_PLACED);
            writeMapInt(root, "blocksBroken", BLOCKS_BROKEN);
            writeMapMapInt(root, "blocksPlacedByType", BLOCKS_PLACED_BY_TYPE);
            writeMapMapInt(root, "blocksBrokenByType", BLOCKS_BROKEN_BY_TYPE);
            writeMapFloat(root, "dragonDamage", DRAGON_DAMAGE_MAP);
            writeMapInt(root, "enderPearls", ENDER_PEARLS_COLLECTED);
            writeMapInt(root, "blazeRods", BLAZE_RODS_COLLECTED);
            writeMapInt(root, "entityKills", ENTITY_KILLS);
            writeMapMapInt(root, "entityKillsByType", ENTITY_KILLS_BY_TYPE);
            writeMapInt(root, "foodEaten", FOOD_EATEN);
            writeMapMapInt(root, "foodEatenByType", FOOD_EATEN_BY_TYPE);
            writeMapLong(root, "screenTimeTicks", SCREEN_TIME_TICKS);
            writeMapMapLong(root, "screenTimeByType", SCREEN_TIME_BY_TYPE);
            writeMapFloat(root, "fallDamageTaken", FALL_DAMAGE_TAKEN);
            writeMapMapFloat(root, "damageDealtByType", DAMAGE_DEALT_BY_TYPE);
            writeMapMapFloat(root, "damageTakenByType", DAMAGE_TAKEN_BY_TYPE);
            GSRJsonUtil.writeNbtAsJson(path, root);
        } catch (IOException e) {
            LOGGER.error("[GSR] Failed to save stats", e);
        }
    }

    public static void load(MinecraftServer server) {
        if (server == null) return;
        reset();
        Path worldDir = GSRStoragePaths.getWorldDir(server);
        Path path = worldDir.resolve(STATS_FILE);
        if (!Files.exists(path)) return;
        try {
            NbtCompound root = GSRJsonUtil.readNbtFromFile(path);
            readMapFloat(root, "distance", DISTANCE_MOVED);
            readMapMapFloat(root, "distanceByType", DISTANCE_MOVED_BY_TYPE);
            readMapFloat(root, "damageDealt", TOTAL_DAMAGE_DEALT);
            readMapFloat(root, "damageTaken", TOTAL_DAMAGE_TAKEN);
            readMapInt(root, "blocksPlaced", BLOCKS_PLACED);
            readMapInt(root, "blocksBroken", BLOCKS_BROKEN);
            readMapMapInt(root, "blocksPlacedByType", BLOCKS_PLACED_BY_TYPE);
            readMapMapInt(root, "blocksBrokenByType", BLOCKS_BROKEN_BY_TYPE);
            readMapFloat(root, "dragonDamage", DRAGON_DAMAGE_MAP);
            readMapInt(root, "enderPearls", ENDER_PEARLS_COLLECTED);
            readMapInt(root, "blazeRods", BLAZE_RODS_COLLECTED);
            readMapInt(root, "entityKills", ENTITY_KILLS);
            readMapMapInt(root, "entityKillsByType", ENTITY_KILLS_BY_TYPE);
            readMapInt(root, "foodEaten", FOOD_EATEN);
            readMapMapInt(root, "foodEatenByType", FOOD_EATEN_BY_TYPE);
            readMapLong(root, "screenTimeTicks", SCREEN_TIME_TICKS);
            readMapMapLong(root, "screenTimeByType", SCREEN_TIME_BY_TYPE);
            readMapFloat(root, "fallDamageTaken", FALL_DAMAGE_TAKEN);
            readMapMapFloat(root, "damageDealtByType", DAMAGE_DEALT_BY_TYPE);
            readMapMapFloat(root, "damageTakenByType", DAMAGE_TAKEN_BY_TYPE);
        } catch (Exception e) {
            LOGGER.warn("[GSR] Failed to load stats", e);
        }
    }

    private static void writeMapFloat(NbtCompound root, String key, Map<UUID, Float> map) {
        NbtCompound sub = new NbtCompound();
        for (Map.Entry<UUID, Float> e : map.entrySet()) {
            sub.putFloat(e.getKey().toString(), e.getValue());
        }
        root.put(key, sub);
    }

    private static void writeMapInt(NbtCompound root, String key, Map<UUID, Integer> map) {
        NbtCompound sub = new NbtCompound();
        for (Map.Entry<UUID, Integer> e : map.entrySet()) {
            sub.putInt(e.getKey().toString(), e.getValue());
        }
        root.put(key, sub);
    }

    private static void readMapFloat(NbtCompound root, String key, Map<UUID, Float> out) {
        root.getCompound(key).ifPresent(sub -> {
            for (String k : sub.getKeys()) {
                try {
                    sub.getFloat(k).ifPresent(v -> out.put(UUID.fromString(k), v));
                } catch (Exception ignored) {}
            }
        });
    }

    private static void readMapInt(NbtCompound root, String key, Map<UUID, Integer> out) {
        root.getCompound(key).ifPresent(sub -> {
            for (String k : sub.getKeys()) {
                try {
                    sub.getInt(k).ifPresent(v -> out.put(UUID.fromString(k), v));
                } catch (Exception ignored) {}
                }
        });
    }

    private static void writeMapMapInt(NbtCompound root, String key, Map<UUID, Map<String, Integer>> map) {
        NbtCompound sub = new NbtCompound();
        for (Map.Entry<UUID, Map<String, Integer>> e : map.entrySet()) {
            if (e.getValue() == null || e.getValue().isEmpty()) continue;
            NbtCompound inner = new NbtCompound();
            for (Map.Entry<String, Integer> ie : e.getValue().entrySet()) {
                inner.putInt(ie.getKey(), ie.getValue());
            }
            sub.put(e.getKey().toString(), inner);
        }
        if (!sub.getKeys().isEmpty()) root.put(key, sub);
    }

    private static void readMapMapInt(NbtCompound root, String key, Map<UUID, Map<String, Integer>> out) {
        root.getCompound(key).ifPresent(sub -> {
            for (String k : sub.getKeys()) {
                try {
                    UUID uuid = UUID.fromString(k);
                    Map<String, Integer> inner = new ConcurrentHashMap<>();
                    sub.getCompound(k).ifPresent(innerSub -> {
                        for (String ik : innerSub.getKeys()) {
                            innerSub.getInt(ik).ifPresent(v -> inner.put(ik, v));
                        }
                    });
                    if (!inner.isEmpty()) out.put(uuid, inner);
                } catch (Exception ignored) {}
            }
        });
    }

    private static void writeMapMapFloat(NbtCompound root, String key, Map<UUID, Map<String, Float>> map) {
        NbtCompound sub = new NbtCompound();
        for (Map.Entry<UUID, Map<String, Float>> e : map.entrySet()) {
            if (e.getValue() == null || e.getValue().isEmpty()) continue;
            NbtCompound inner = new NbtCompound();
            for (Map.Entry<String, Float> ie : e.getValue().entrySet()) {
                inner.putFloat(ie.getKey(), ie.getValue());
            }
            sub.put(e.getKey().toString(), inner);
        }
        if (!sub.getKeys().isEmpty()) root.put(key, sub);
    }

    private static void readMapMapFloat(NbtCompound root, String key, Map<UUID, Map<String, Float>> out) {
        root.getCompound(key).ifPresent(sub -> {
            for (String k : sub.getKeys()) {
                try {
                    UUID uuid = UUID.fromString(k);
                    Map<String, Float> inner = new ConcurrentHashMap<>();
                    sub.getCompound(k).ifPresent(innerSub -> {
                        for (String ik : innerSub.getKeys()) {
                            innerSub.getFloat(ik).ifPresent(v -> inner.put(ik, v));
                        }
                    });
                    if (!inner.isEmpty()) out.put(uuid, inner);
                } catch (Exception ignored) {}
            }
        });
    }

    private static void writeMapLong(NbtCompound root, String key, Map<UUID, Long> map) {
        NbtCompound sub = new NbtCompound();
        for (Map.Entry<UUID, Long> e : map.entrySet()) {
            sub.putLong(e.getKey().toString(), e.getValue());
        }
        root.put(key, sub);
    }

    private static void readMapLong(NbtCompound root, String key, Map<UUID, Long> out) {
        root.getCompound(key).ifPresent(sub -> {
            for (String k : sub.getKeys()) {
                try {
                    sub.getLong(k).ifPresent(v -> out.put(UUID.fromString(k), v));
                } catch (Exception ignored) {}
            }
        });
    }

    private static void writeMapMapLong(NbtCompound root, String key, Map<UUID, Map<String, Long>> map) {
        NbtCompound sub = new NbtCompound();
        for (Map.Entry<UUID, Map<String, Long>> e : map.entrySet()) {
            if (e.getValue() == null || e.getValue().isEmpty()) continue;
            NbtCompound inner = new NbtCompound();
            for (Map.Entry<String, Long> ie : e.getValue().entrySet()) {
                inner.putLong(ie.getKey(), ie.getValue());
            }
            sub.put(e.getKey().toString(), inner);
        }
        if (!sub.getKeys().isEmpty()) root.put(key, sub);
    }

    private static void readMapMapLong(NbtCompound root, String key, Map<UUID, Map<String, Long>> out) {
        root.getCompound(key).ifPresent(sub -> {
            for (String k : sub.getKeys()) {
                try {
                    UUID uuid = UUID.fromString(k);
                    Map<String, Long> inner = new ConcurrentHashMap<>();
                    sub.getCompound(k).ifPresent(innerSub -> {
                        for (String ik : innerSub.getKeys()) {
                            innerSub.getLong(ik).ifPresent(v -> inner.put(ik, v));
                        }
                    });
                    if (!inner.isEmpty()) out.put(uuid, inner);
                } catch (Exception ignored) {}
            }
        });
    }
}
