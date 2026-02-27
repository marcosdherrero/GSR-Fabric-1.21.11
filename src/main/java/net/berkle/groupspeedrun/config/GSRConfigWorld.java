package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRWorldConfigParameters;
import net.berkle.groupspeedrun.util.GSRJsonUtil;
import net.berkle.groupspeedrun.util.GSRStoragePaths;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Central data structure for Group Speedrun world state: run state and split times.
 * Persists to world/data/gsr/ (world-colocated); NBT keys and file name in {@link GSRWorldConfigParameters}.
 */
public class GSRConfigWorld {

    private static final Logger LOGGER = LoggerFactory.getLogger("GSR-Config");

    // --- Run state ---
    public long startTime = -1;
    public boolean isTimerFrozen = false;
    public long frozenTime = 0;
    /** True when freeze was caused by server stop or last player disconnect; enables auto-resume on next join. */
    public boolean frozenByServerStop = false;
    /** True when admin manually paused; GSR Options button shows Resume. False when running or frozen by server/menu. */
    public boolean manualPause = false;
    public boolean isVictorious = false;
    public boolean isFailed = false;
    public boolean groupDeathEnabled = true;
    /** Shared health (default off). */
    public boolean sharedHealthEnabled = false;
    /** UUIDs excluded from group death (their death does not fail run, they do not trigger auto-start). Empty = no exclusions (all in). */
    public final Set<UUID> groupDeathParticipants = new HashSet<>();
    /** UUIDs in shared health pool. Empty = all players when sharedHealthEnabled. */
    public final Set<UUID> sharedHealthParticipants = new HashSet<>();
    /** UUIDs excluded from run: no stats, no triggers, no group death/health. */
    public final Set<UUID> excludedFromRun = new HashSet<>();

    /** True if player is in group death pool (can trigger auto-start, death fails run). Selection list = excluded; empty = all in. */
    public boolean isInGroupDeath(UUID playerUuid) {
        if (playerUuid == null) return false;
        if (excludedFromRun.contains(playerUuid)) return false;
        return !groupDeathParticipants.contains(playerUuid);
    }

    /** True if player is in shared health pool. Empty participants = all non-excluded when sharedHealthEnabled. */
    public boolean isInSharedHealth(UUID playerUuid) {
        if (playerUuid == null || !sharedHealthEnabled) return false;
        if (excludedFromRun.contains(playerUuid)) return false;
        return sharedHealthParticipants.isEmpty() || sharedHealthParticipants.contains(playerUuid);
    }

    /** True when run has not started yet; movement by group death participants should trigger auto-start. */
    public boolean isRunNotStarted() {
        return !isVictorious && !isFailed && startTime <= 0;
    }
    public String failedByPlayerName = "";
    public String failedByDeathMessage = "";
    public int runParticipantCount = 0;
    /** Set on victory from best dragon-damage snapshot (for status/leaderboard). */
    public String dragonWarriorName = "";
    public float dragonWarriorDamage = 0f;
    /** Lowest difficulty ordinal during run (0=Peaceful, 1=Easy, 2=Normal, 3=Hard). -1 = not yet set. */
    public int lowestDifficultyOrdinal = -1;

    // --- Split times (ms) ---
    public long timeNether = 0;
    public long timeBastion = 0;
    public long timeFortress = 0;
    public long timeEnd = 0;
    public long timeDragon = 0;
    public long lastSplitTime = 0;
    /** First time (ms elapsed) any player used an ender eye this run; 0 = not yet. Used for stronghold locator 30 min gate. */
    public long timeFirstEnderEye = 0;
    /** First elapsed ms when player reentered overworld after both fortress and bastion; 0 = not yet. For stronghold locator gate. */
    public long timeFirstOverworldReturnAfterNether = 0;
    /** True if any locator was toggled (via menu or command); run is invalid for ranking when antiCheatEnabled. */
    public boolean locatorDeranked = false;
    /** Host setting: when true, locator use invalidates runs. When false, runs are never invalidated. Default true. */
    public boolean antiCheatEnabled = true;
    /** When true, run auto-starts on first movement or block break. When false, admin must press Start. Default true. */
    public boolean autoStartEnabled = true;
    /** When non-admins can use locators: 0=Never, 1=Always, 2=30 min post previous split. Default 2. */
    public int locatorNonAdminMode = 2;
    /** Effective allowNewWorldBeforeRunEnd from designated admin (highest-level admin who joined first). Sync-only; default when none. */
    public boolean effectiveAllowNewWorldBeforeRunEnd = false;

    // --- Locator HUD: structure tracking (coordinates; toggles are per-player in GSRConfigPlayer) ---
    public boolean fortressLocated = false;
    public boolean bastionLocated = false;
    public boolean strongholdLocated = false;
    public boolean shipLocated = false;
    public int fortressX, fortressZ;
    public int bastionX, bastionZ;
    public int strongholdX, strongholdZ;
    public int shipX, shipY, shipZ;

    /** When player enters a tracked structure and it was the only locator: fade-out over 3 seconds. */
    public long locatorFadeStartTime = 0;
    public String locatorFadeType = "";

    /** Icon items and theme colors are in {@link GSRConfigPlayer} (per-player locator HUD config). */

    /**
     * Elapsed ms. If not started returns 0; if frozen/victorious/failed returns frozenTime;
     * otherwise current time minus startTime.
     * When the run completes (victory or fail), the timer stops and locks at that time until reset.
     */
    public long getElapsedTime() {
        if (startTime <= 0) return 0;
        if (isVictorious || isFailed) return frozenTime;
        if (isTimerFrozen) return frozenTime > 0 ? frozenTime : System.currentTimeMillis() - startTime;
        return System.currentTimeMillis() - startTime;
    }

    /** Clears only completion state (fail/victory). Used on client disconnect so run time persists until server sync. */
    public void clearCompletionStateOnly() {
        isFailed = false;
        isVictorious = false;
        failedByPlayerName = "";
        failedByDeathMessage = "";
    }

    /** Clears only split times (for /gsr start). */
    public void clearSplitsOnly() {
        timeNether = 0;
        timeBastion = 0;
        timeFortress = 0;
        timeEnd = 0;
        timeDragon = 0;
        lastSplitTime = 0;
        timeFirstEnderEye = 0;
        timeFirstOverworldReturnAfterNether = 0;
    }

    /** Resets all run data to defaults. When run was invalidated/deranked, preserves participant selections from Run Manager. */
    public void resetRunData() {
        boolean preserveParticipants = locatorDeranked;
        Set<UUID> savedGroupDeath = preserveParticipants ? new HashSet<>(groupDeathParticipants) : null;
        Set<UUID> savedSharedHealth = preserveParticipants ? new HashSet<>(sharedHealthParticipants) : null;
        Set<UUID> savedExcluded = preserveParticipants ? new HashSet<>(excludedFromRun) : null;

        startTime = -1;
        isTimerFrozen = false;
        frozenTime = 0;
        frozenByServerStop = false;
        manualPause = false;
        isVictorious = false;
        isFailed = false;
        groupDeathEnabled = true;
        failedByPlayerName = "";
        failedByDeathMessage = "";
        runParticipantCount = 0;
        fortressLocated = false;
        bastionLocated = false;
        strongholdLocated = false;
        shipLocated = false;
        locatorDeranked = false;
        locatorFadeStartTime = 0;
        locatorFadeType = "";
        frozenByServerStop = false;
        lowestDifficultyOrdinal = -1;
        groupDeathParticipants.clear();
        sharedHealthParticipants.clear();
        excludedFromRun.clear();
        clearSplitsOnly();

        if (preserveParticipants && savedGroupDeath != null && savedSharedHealth != null && savedExcluded != null) {
            groupDeathParticipants.addAll(savedGroupDeath);
            sharedHealthParticipants.addAll(savedSharedHealth);
            excludedFromRun.addAll(savedExcluded);
        }
    }

    public void writeNbt(NbtCompound nbt) {
        nbt.putLong(GSRWorldConfigParameters.K_START_TIME, startTime);
        nbt.putBoolean(GSRWorldConfigParameters.K_IS_FROZEN, isTimerFrozen);
        nbt.putLong(GSRWorldConfigParameters.K_FROZEN_TIME, frozenTime);
        nbt.putBoolean(GSRWorldConfigParameters.K_FROZEN_BY_SERVER_STOP, frozenByServerStop);
        nbt.putBoolean(GSRWorldConfigParameters.K_MANUAL_PAUSE, manualPause);
        nbt.putBoolean(GSRWorldConfigParameters.K_IS_VICTORIOUS, isVictorious);
        nbt.putBoolean(GSRWorldConfigParameters.K_IS_FAILED, isFailed);
        nbt.putBoolean(GSRWorldConfigParameters.K_GROUP_DEATH, groupDeathEnabled);
        nbt.putBoolean(GSRWorldConfigParameters.K_SHARED_HEALTH, sharedHealthEnabled);
        nbt.putString(GSRWorldConfigParameters.K_FAILED_BY_NAME, failedByPlayerName != null ? failedByPlayerName : "");
        nbt.putString(GSRWorldConfigParameters.K_FAILED_BY_MSG, failedByDeathMessage != null ? failedByDeathMessage : "");
        nbt.putInt(GSRWorldConfigParameters.K_RUN_PARTICIPANT_COUNT, runParticipantCount);
        nbt.putString(GSRWorldConfigParameters.K_DRAGON_WARRIOR_NAME, dragonWarriorName != null ? dragonWarriorName : "");
        nbt.putFloat(GSRWorldConfigParameters.K_DRAGON_WARRIOR_DAMAGE, dragonWarriorDamage);
        nbt.putInt(GSRWorldConfigParameters.K_LOWEST_DIFFICULTY_ORDINAL, lowestDifficultyOrdinal);
        nbt.putLong(GSRWorldConfigParameters.K_T_NETHER, timeNether);
        nbt.putLong(GSRWorldConfigParameters.K_T_BASTION, timeBastion);
        nbt.putLong(GSRWorldConfigParameters.K_T_FORTRESS, timeFortress);
        nbt.putLong(GSRWorldConfigParameters.K_T_END, timeEnd);
        nbt.putLong(GSRWorldConfigParameters.K_T_DRAGON, timeDragon);
        nbt.putLong(GSRWorldConfigParameters.K_L_SPLIT, lastSplitTime);
        nbt.putLong(GSRWorldConfigParameters.K_T_FIRST_ENDER_EYE, timeFirstEnderEye);
        nbt.putBoolean(GSRWorldConfigParameters.K_LOCATOR_DERANKED, locatorDeranked);
        nbt.putBoolean(GSRWorldConfigParameters.K_ANTI_CHEAT_ENABLED, antiCheatEnabled);
        nbt.putBoolean(GSRWorldConfigParameters.K_AUTO_START_ENABLED, autoStartEnabled);
        nbt.putInt(GSRWorldConfigParameters.K_LOCATOR_NON_ADMIN_MODE, locatorNonAdminMode);
        nbt.putLong(GSRWorldConfigParameters.K_T_FIRST_OVERWORLD_RETURN, timeFirstOverworldReturnAfterNether);
        nbt.putBoolean(GSRWorldConfigParameters.K_FORT_LOCATED, fortressLocated);
        nbt.putBoolean(GSRWorldConfigParameters.K_BAST_LOCATED, bastionLocated);
        nbt.putBoolean(GSRWorldConfigParameters.K_STRONGHOLD_LOCATED, strongholdLocated);
        nbt.putBoolean(GSRWorldConfigParameters.K_SHIP_LOCATED, shipLocated);
        nbt.putInt(GSRWorldConfigParameters.K_FORT_X, fortressX);
        nbt.putInt(GSRWorldConfigParameters.K_FORT_Z, fortressZ);
        nbt.putInt(GSRWorldConfigParameters.K_BAST_X, bastionX);
        nbt.putInt(GSRWorldConfigParameters.K_BAST_Z, bastionZ);
        nbt.putInt(GSRWorldConfigParameters.K_STRONGHOLD_X, strongholdX);
        nbt.putInt(GSRWorldConfigParameters.K_STRONGHOLD_Z, strongholdZ);
        nbt.putInt(GSRWorldConfigParameters.K_SHIP_X, shipX);
        nbt.putInt(GSRWorldConfigParameters.K_SHIP_Y, shipY);
        nbt.putInt(GSRWorldConfigParameters.K_SHIP_Z, shipZ);
        nbt.putLong(GSRWorldConfigParameters.K_LOCATOR_FADE_START, locatorFadeStartTime);
        nbt.putString(GSRWorldConfigParameters.K_LOCATOR_FADE_TYPE, locatorFadeType != null ? locatorFadeType : "");
        writeUuidSet(nbt, GSRWorldConfigParameters.K_GROUP_DEATH_PARTICIPANTS, groupDeathParticipants);
        writeUuidSet(nbt, GSRWorldConfigParameters.K_SHARED_HEALTH_PARTICIPANTS, sharedHealthParticipants);
        writeUuidSet(nbt, GSRWorldConfigParameters.K_EXCLUDED_FROM_RUN, excludedFromRun);
    }

    private static void writeUuidSet(NbtCompound nbt, String key, Set<UUID> set) {
        NbtList list = new NbtList();
        for (UUID u : set) {
            if (u != null) list.add(NbtString.of(u.toString()));
        }
        nbt.put(key, list);
    }

    public void readNbt(NbtCompound nbt) {
        if (nbt == null) return;
        readLong(nbt, GSRWorldConfigParameters.K_START_TIME, v -> this.startTime = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_IS_FROZEN).ifPresent(v -> this.isTimerFrozen = v);
        readLong(nbt, GSRWorldConfigParameters.K_FROZEN_TIME, v -> this.frozenTime = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_FROZEN_BY_SERVER_STOP).ifPresent(v -> this.frozenByServerStop = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_MANUAL_PAUSE).ifPresent(v -> this.manualPause = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_IS_VICTORIOUS).ifPresent(v -> this.isVictorious = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_IS_FAILED).ifPresent(v -> this.isFailed = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_GROUP_DEATH).ifPresent(v -> this.groupDeathEnabled = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_SHARED_HEALTH).ifPresent(v -> this.sharedHealthEnabled = v);
        nbt.getString(GSRWorldConfigParameters.K_FAILED_BY_NAME).ifPresent(v -> this.failedByPlayerName = v);
        nbt.getString(GSRWorldConfigParameters.K_FAILED_BY_MSG).ifPresent(v -> this.failedByDeathMessage = v);
        nbt.getInt(GSRWorldConfigParameters.K_RUN_PARTICIPANT_COUNT).ifPresent(v -> this.runParticipantCount = v);
        nbt.getString(GSRWorldConfigParameters.K_DRAGON_WARRIOR_NAME).ifPresent(v -> this.dragonWarriorName = v);
        nbt.getFloat(GSRWorldConfigParameters.K_DRAGON_WARRIOR_DAMAGE).ifPresent(v -> this.dragonWarriorDamage = v);
        nbt.getInt(GSRWorldConfigParameters.K_LOWEST_DIFFICULTY_ORDINAL).ifPresent(v -> this.lowestDifficultyOrdinal = v);
        readLong(nbt, GSRWorldConfigParameters.K_T_NETHER, v -> this.timeNether = v);
        readLong(nbt, GSRWorldConfigParameters.K_T_BASTION, v -> this.timeBastion = v);
        readLong(nbt, GSRWorldConfigParameters.K_T_FORTRESS, v -> this.timeFortress = v);
        readLong(nbt, GSRWorldConfigParameters.K_T_END, v -> this.timeEnd = v);
        readLong(nbt, GSRWorldConfigParameters.K_T_DRAGON, v -> this.timeDragon = v);
        readLong(nbt, GSRWorldConfigParameters.K_L_SPLIT, v -> this.lastSplitTime = v);
        readLong(nbt, GSRWorldConfigParameters.K_T_FIRST_ENDER_EYE, v -> this.timeFirstEnderEye = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_LOCATOR_DERANKED).ifPresent(v -> this.locatorDeranked = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_ANTI_CHEAT_ENABLED).ifPresent(v -> this.antiCheatEnabled = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_AUTO_START_ENABLED).ifPresent(v -> this.autoStartEnabled = v);
        nbt.getInt(GSRWorldConfigParameters.K_LOCATOR_NON_ADMIN_MODE).ifPresent(v -> this.locatorNonAdminMode = Math.max(0, Math.min(2, v)));
        nbt.getBoolean(GSRWorldConfigParameters.K_EFFECTIVE_ALLOW_NEW_WORLD_BEFORE_RUN_END).ifPresent(v -> this.effectiveAllowNewWorldBeforeRunEnd = v);
        readLong(nbt, GSRWorldConfigParameters.K_T_FIRST_OVERWORLD_RETURN, v -> this.timeFirstOverworldReturnAfterNether = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_FORT_LOCATED).ifPresent(v -> this.fortressLocated = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_BAST_LOCATED).ifPresent(v -> this.bastionLocated = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_STRONGHOLD_LOCATED).ifPresent(v -> this.strongholdLocated = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_SHIP_LOCATED).ifPresent(v -> this.shipLocated = v);
        // Backward compat: legacy K_FORT_ACTIVE etc. map to fortressLocated
        nbt.getBoolean(GSRWorldConfigParameters.K_FORT_ACTIVE).ifPresent(v -> this.fortressLocated = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_BAST_ACTIVE).ifPresent(v -> this.bastionLocated = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_STRONGHOLD_ACTIVE).ifPresent(v -> this.strongholdLocated = v);
        nbt.getBoolean(GSRWorldConfigParameters.K_SHIP_ACTIVE).ifPresent(v -> this.shipLocated = v);
        nbt.getInt(GSRWorldConfigParameters.K_FORT_X).ifPresent(v -> this.fortressX = v);
        nbt.getInt(GSRWorldConfigParameters.K_FORT_Z).ifPresent(v -> this.fortressZ = v);
        nbt.getInt(GSRWorldConfigParameters.K_BAST_X).ifPresent(v -> this.bastionX = v);
        nbt.getInt(GSRWorldConfigParameters.K_BAST_Z).ifPresent(v -> this.bastionZ = v);
        nbt.getInt(GSRWorldConfigParameters.K_STRONGHOLD_X).ifPresent(v -> this.strongholdX = v);
        nbt.getInt(GSRWorldConfigParameters.K_STRONGHOLD_Z).ifPresent(v -> this.strongholdZ = v);
        nbt.getInt(GSRWorldConfigParameters.K_SHIP_X).ifPresent(v -> this.shipX = v);
        nbt.getInt(GSRWorldConfigParameters.K_SHIP_Y).ifPresent(v -> this.shipY = v);
        nbt.getInt(GSRWorldConfigParameters.K_SHIP_Z).ifPresent(v -> this.shipZ = v);
        readLong(nbt, GSRWorldConfigParameters.K_LOCATOR_FADE_START, v -> this.locatorFadeStartTime = v);
        nbt.getString(GSRWorldConfigParameters.K_LOCATOR_FADE_TYPE).ifPresent(v -> this.locatorFadeType = v != null ? v : "");
        readUuidSet(nbt, GSRWorldConfigParameters.K_GROUP_DEATH_PARTICIPANTS, groupDeathParticipants);
        readUuidSet(nbt, GSRWorldConfigParameters.K_SHARED_HEALTH_PARTICIPANTS, sharedHealthParticipants);
        readUuidSet(nbt, GSRWorldConfigParameters.K_EXCLUDED_FROM_RUN, excludedFromRun);
    }

    /** Reads long from NBT; falls back to double for JSON round-trip (numbers stored as double). */
    private static void readLong(NbtCompound nbt, String key, java.util.function.LongConsumer setter) {
        var opt = nbt.getLong(key);
        if (opt.isPresent()) {
            setter.accept(opt.get());
            return;
        }
        nbt.getDouble(key).ifPresent(v -> setter.accept(v.longValue()));
    }

    private static void readUuidSet(NbtCompound nbt, String key, Set<UUID> out) {
        out.clear();
        NbtList list = nbt.getList(key).orElse(new NbtList());
        for (int i = 0; i < list.size(); i++) {
            try {
                NbtElement el = list.get(i);
                if (el instanceof net.minecraft.nbt.NbtString nbtStr) {
                    String s = nbtStr.asString().orElse("");
                    if (!s.isEmpty()) out.add(UUID.fromString(s));
                }
            } catch (Exception ignored) {}
        }
    }

    public void save(MinecraftServer server) {
        Path worldDir = GSRStoragePaths.getWorldDir(server);
        Path nbtPath = worldDir.resolve(GSRWorldConfigParameters.NBT_FILE);
        try {
            Files.createDirectories(worldDir);
            NbtCompound nbt = new NbtCompound();
            writeNbt(nbt);
            GSRJsonUtil.writeNbtAsJson(nbtPath, nbt);
            LOGGER.debug("GSR: Saved world config to {} (startTime={})", nbtPath.toAbsolutePath(), startTime);
        } catch (IOException e) {
            LOGGER.error("GSR: Failed to save world config to {}", nbtPath.toAbsolutePath(), e);
        }
    }

    public static GSRConfigWorld load(MinecraftServer server) {
        GSRConfigWorld config = new GSRConfigWorld();
        Path canonicalDir = GSRStoragePaths.getWorldDir(server);
        Path canonicalPath = canonicalDir.resolve(GSRWorldConfigParameters.NBT_FILE);
        Path absoluteCanonical = canonicalPath.toAbsolutePath();
        boolean loaded = false;
        LOGGER.info("GSR: Loading world config from {} (exists={})", absoluteCanonical, Files.exists(canonicalPath));
        if (Files.exists(canonicalPath)) {
            try {
                NbtCompound nbt = GSRJsonUtil.readNbtFromFile(canonicalPath);
                config.readNbt(nbt);
                loaded = true;
                LOGGER.info("GSR: Loaded world config from {} (startTime={}, frozen={})", canonicalPath, config.startTime, config.isTimerFrozen);
            } catch (Exception e) {
                LOGGER.warn("GSR: Failed to load world config from {}", canonicalPath, e);
            }
        }
        if (!loaded) {
            Path[] legacyCandidates = GSRStoragePaths.getLegacyWorldDirCandidates(server);
            LOGGER.info("GSR: Trying {} legacy paths", legacyCandidates.length);
            for (Path worldDir : legacyCandidates) {
                Path path = worldDir.resolve(GSRWorldConfigParameters.NBT_FILE);
                if (Files.exists(path)) {
                    try {
                        NbtCompound nbt = GSRJsonUtil.readNbtFromFile(path);
                        config.readNbt(nbt);
                        loaded = true;
                        migrateConfigToCanonicalPath(path, canonicalPath);
                        LOGGER.info("GSR: Loaded world config from legacy {} (startTime={}, frozen={})", path, config.startTime, config.isTimerFrozen);
                        break;
                    } catch (Exception e) {
                        LOGGER.warn("GSR: Failed to load world config from {}", path, e);
                    }
                }
            }
        }
        if (!loaded) {
            LOGGER.warn("GSR: No world config found (primary: {}, tried legacy paths)", canonicalPath);
        }
        if (!config.isVictorious && !config.isFailed && config.startTime <= 0) {
            config.startTime = -1;
        }
        return config;
    }

    /** Copies config from legacy path to canonical path so future loads use the same location. */
    private static void migrateConfigToCanonicalPath(Path from, Path to) {
        try {
            Files.createDirectories(to.getParent());
            Files.copy(from, to, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            LOGGER.info("GSR: Migrated world config from {} to {}", from, to);
        } catch (IOException e) {
            LOGGER.warn("GSR: Failed to migrate config to canonical path", e);
        }
    }
}
