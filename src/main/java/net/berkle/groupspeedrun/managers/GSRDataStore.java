package net.berkle.groupspeedrun.managers;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.GSRStats;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.data.GSRRunParticipant;
import net.berkle.groupspeedrun.data.GSRRunRecord;
import net.berkle.groupspeedrun.data.GSRRunPlayerSnapshot;
import net.berkle.groupspeedrun.data.GSRRunSaveState;
import net.berkle.groupspeedrun.data.GSRRunSaveStateNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.berkle.groupspeedrun.parameter.GSRStorageParameters;
import net.berkle.groupspeedrun.util.GSRJsonUtil;
import net.berkle.groupspeedrun.util.GSRNbtUtil;
import net.berkle.groupspeedrun.util.GSRStoragePaths;

/**
 * Database-style store for run history: runs, participants, and per-run player snapshots.
 * Persists to gsr_folder/worlds/&lt;world&gt;/db/ as NBT. File names in {@link GSRStorageParameters}.
 */
public final class GSRDataStore {

    private static final Logger LOGGER = LoggerFactory.getLogger("GSR-DataStore");

    private GSRDataStore() {}

    public static Path getDbDir(MinecraftServer server) {
        return GSRStoragePaths.getWorldDir(server).resolve(GSRStorageParameters.DB_DIR);
    }

    /**
     * Writes NBT to path as JSON atomically: write to .tmp file, then rename to replace.
     * Avoids corrupting the target if the process crashes during write.
     */
    private static void writeAtomic(Path path, CompoundTag root) throws IOException {
        Path tmp = path.resolveSibling(path.getFileName() + GSRStorageParameters.ATOMIC_WRITE_SUFFIX);
        GSRJsonUtil.writeNbtAsJson(tmp, root);
        Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
    }

    /** Append a run record to the runs table. Uses atomic write to avoid corruption on crash. */
    public static void appendRun(MinecraftServer server, GSRRunRecord run) {
        Path dir = getDbDir(server);
        Path path = dir.resolve(GSRStorageParameters.RUNS_FILE);
        try {
            Files.createDirectories(dir);
            ListTag list = loadList(path);
            list.add(toNbt(run));
            CompoundTag root = new CompoundTag();
            root.put("runs", list);
            writeAtomic(path, root);
        } catch (Exception e) {
            LOGGER.error("[GSR] Failed to append run", e);
        }
    }

    /** Append a participant row. Uses atomic write to avoid corruption on crash. */
    public static void appendRunParticipant(MinecraftServer server, GSRRunParticipant p) {
        Path dir = getDbDir(server);
        Path path = dir.resolve(GSRStorageParameters.PARTICIPANTS_FILE);
        try {
            Files.createDirectories(dir);
            ListTag list = loadList(path, "participants");
            list.add(participantToNbt(p));
            CompoundTag root = new CompoundTag();
            root.put("participants", list);
            writeAtomic(path, root);
        } catch (Exception e) {
            LOGGER.error("[GSR] Failed to append run participant", e);
        }
    }

    /** Append a run player snapshot (one per participant per run). Uses atomic write to avoid corruption on crash. */
    public static void appendRunPlayerSnapshot(MinecraftServer server, GSRRunPlayerSnapshot s) {
        Path dir = getDbDir(server);
        Path path = dir.resolve(GSRStorageParameters.SNAPSHOTS_FILE);
        try {
            Files.createDirectories(dir);
            ListTag list = loadList(path, "snapshots");
            list.add(snapshotToNbt(s));
            CompoundTag root = new CompoundTag();
            root.put("snapshots", list);
            writeAtomic(path, root);
        } catch (Exception e) {
            LOGGER.error("[GSR] Failed to append run snapshot", e);
        }
    }

    /**
     * Load all runs from the runs table. Used for server-side leaderboards, export, or analytics.
     * Client run history uses {@link net.berkle.groupspeedrun.client.GSRSharedRunLoader} instead.
     */
    public static List<GSRRunRecord> loadRuns(MinecraftServer server) {
        Path path = getDbDir(server).resolve(GSRStorageParameters.RUNS_FILE);
        ListTag list = loadList(path);
        List<GSRRunRecord> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Tag el = list.get(i);
            if (el instanceof CompoundTag c) out.add(fromNbt(c));
        }
        return out;
    }

    /**
     * If the world HUD looks primed but run history has a completed run newer than the last Reset,
     * restore those times/flags so a jar swap or crash cannot auto-start over a finished PB.
     *
     * @return true if the HUD was restored from history
     */
    public static boolean restoreHudFromLatestCompletedRun(MinecraftServer server, GSRConfigWorld config) {
        if (server == null || config == null) return false;
        if (config.isVictorious || config.isFailed) return false;
        if (config.startTime > 0) return false;
        GSRRunRecord latest = loadRuns(server).stream()
                .filter(r -> GSRRunRecord.STATUS_VICTORY.equals(r.status()) || GSRRunRecord.STATUS_FAIL.equals(r.status()))
                .max(Comparator.comparingLong(GSRRunRecord::endMs))
                .orElse(null);
        if (latest == null || latest.endMs() <= config.lastResetTimeMs) return false;
        config.startTime = latest.startMs() > 0 ? latest.startMs() : config.startTime;
        config.frozenTime = Math.max(0L, latest.endMs() - Math.max(0L, latest.startMs()));
        config.isTimerFrozen = true;
        config.frozenByServerStop = false;
        config.manualPause = false;
        config.isVictorious = GSRRunRecord.STATUS_VICTORY.equals(latest.status());
        config.isFailed = GSRRunRecord.STATUS_FAIL.equals(latest.status());
        config.failedByPlayerName = latest.failedByPlayerName() != null ? latest.failedByPlayerName() : "";
        config.failedByDeathMessage = latest.failedByDeathMessage() != null ? latest.failedByDeathMessage() : "";
        config.runParticipantCount = latest.participantCount();
        config.timeNether = latest.timeNether();
        config.timeBastion = latest.timeBastion();
        config.timeFortress = latest.timeFortress();
        config.timeEnd = latest.timeEnd();
        config.timeDragon = latest.timeDragon();
        config.locatorDeranked = latest.deranked();
        config.save(server);
        LOGGER.info("[GSR] Restored HUD from completed run {} ({})", latest.runId(), latest.status());
        return true;
    }

    /**
     * Load all run player snapshots from the snapshots table. Used for server-side analytics or export.
     * Joins with runs by runId when needed.
     */
    public static List<GSRRunPlayerSnapshot> loadSnapshots(MinecraftServer server) {
        Path path = getDbDir(server).resolve(GSRStorageParameters.SNAPSHOTS_FILE);
        ListTag list = loadList(path, "snapshots");
        List<GSRRunPlayerSnapshot> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Tag el = list.get(i);
            if (el instanceof CompoundTag c) out.add(snapshotFromNbt(c));
        }
        return out;
    }

    /**
     * Load a full run save state by runId from server storage.
     * Tries per-run file (db/runs/&lt;runId&gt;.json) first for O(1) lookup; falls back to table scan for legacy data.
     *
     * @param server Server instance.
     * @param runId  Run ID to load.
     * @return Full save state, or null if run not found.
     */
    public static GSRRunSaveState loadRunSaveStateByRunId(MinecraftServer server, String runId) {
        if (server == null || runId == null || runId.isEmpty()) return null;
        Path perRunPath = getDbDir(server).resolve(GSRStorageParameters.DB_RUNS_DIR).resolve(runId + ".json");
        if (Files.exists(perRunPath)) {
            try {
                CompoundTag root = GSRJsonUtil.readNbtFromJson(perRunPath);
                return GSRRunSaveStateNbt.fromNbt(root);
            } catch (Exception e) {
                LOGGER.warn("[GSR] Failed to load per-run file {}, falling back to tables", perRunPath, e);
            }
        }
        List<GSRRunRecord> runs = loadRuns(server);
        GSRRunRecord run = runs.stream().filter(r -> runId.equals(r.runId())).findFirst().orElse(null);
        if (run == null) return null;
        List<GSRRunParticipant> participants = loadParticipants(server).stream()
                .filter(p -> runId.equals(p.runId())).toList();
        List<GSRRunPlayerSnapshot> snapshots = loadSnapshots(server).stream()
                .filter(s -> runId.equals(s.runId())).toList();
        return new GSRRunSaveState(run, participants, snapshots);
    }

    /**
     * Load all run participants from the participants table. Used for server-side analytics or export.
     * Joins with runs by runId when needed.
     */
    public static List<GSRRunParticipant> loadParticipants(MinecraftServer server) {
        Path path = getDbDir(server).resolve(GSRStorageParameters.PARTICIPANTS_FILE);
        ListTag list = loadList(path, "participants");
        List<GSRRunParticipant> out = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            Tag el = list.get(i);
            if (el instanceof CompoundTag c) out.add(participantFromNbt(c));
        }
        return out;
    }

    private static GSRRunParticipant participantFromNbt(CompoundTag c) {
        return new GSRRunParticipant(
            c.getString("runId").orElse(""),
            c.getString("playerUuid").orElse(""),
            c.getString("playerName").orElse("")
        );
    }

    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ISO_INSTANT;

    /**
     * Builds a run player snapshot from GSRStats for the given player.
     * Shared by recordCurrentRun and saveCompletedRunToPlayerFolders.
     */
    private static GSRRunPlayerSnapshot buildSnapshotForPlayer(UUID uuid, String runId, String name) {
        float damageDealt = GSRStats.TOTAL_DAMAGE_DEALT.getOrDefault(uuid, 0f);
        float damageTaken = GSRStats.TOTAL_DAMAGE_TAKEN.getOrDefault(uuid, 0f);
        float dragonDamage = GSRStats.DRAGON_DAMAGE_MAP.getOrDefault(uuid, 0f);
        int pearls = GSRStats.ENDER_PEARLS_COLLECTED.getOrDefault(uuid, 0);
        int blaze = GSRStats.BLAZE_RODS_COLLECTED.getOrDefault(uuid, 0);
        int placed = GSRStats.BLOCKS_PLACED.getOrDefault(uuid, 0);
        int broken = GSRStats.BLOCKS_BROKEN.getOrDefault(uuid, 0);
        String mostPlacedId = GSRStats.getMostPlacedBlockId(uuid);
        int mostPlacedCount = GSRStats.getMostPlacedBlockCount(uuid);
        String mostBrokenId = GSRStats.getMostBrokenBlockId(uuid);
        int mostBrokenCount = GSRStats.getMostBrokenBlockCount(uuid);
        float dist = GSRStats.DISTANCE_MOVED.getOrDefault(uuid, 0f);
        String mostTraveledTypeId = GSRStats.getMostTraveledTypeId(uuid);
        float mostTraveledTypeAmount = GSRStats.getMostTraveledTypeAmount(uuid);
        int entityKills = GSRStats.ENTITY_KILLS.getOrDefault(uuid, 0);
        String mostKilledId = GSRStats.getMostKilledEntityId(uuid);
        int mostKilledCount = GSRStats.getMostKilledEntityCount(uuid);
        int foodEaten = GSRStats.FOOD_EATEN.getOrDefault(uuid, 0);
        String mostEatenId = GSRStats.getMostEatenFoodId(uuid);
        int mostEatenCount = GSRStats.getMostEatenFoodCount(uuid);
        long screenTimeTicks = GSRStats.SCREEN_TIME_TICKS.getOrDefault(uuid, 0L);
        String mostScreenId = GSRStats.getMostUsedScreenId(uuid);
        long mostScreenTicks = GSRStats.getMostUsedScreenTicks(uuid);
        float fallDamage = GSRStats.FALL_DAMAGE_TAKEN.getOrDefault(uuid, 0f);
        String mostDealtTypeId = GSRStats.getMostDamageDealtTypeId(uuid);
        float mostDealtTypeAmt = GSRStats.getMostDamageDealtTypeAmount(uuid);
        String mostTakenTypeId = GSRStats.getMostDamageTakenTypeId(uuid);
        float mostTakenTypeAmt = GSRStats.getMostDamageTakenTypeAmount(uuid);
        return new GSRRunPlayerSnapshot(
            runId, uuid.toString(), name,
            damageDealt, damageTaken, mostDealtTypeId, mostDealtTypeAmt, mostTakenTypeId, mostTakenTypeAmt,
            dragonDamage, pearls, blaze, placed, broken,
            mostPlacedId, mostPlacedCount, mostBrokenId, mostBrokenCount, dist,
            mostTraveledTypeId, mostTraveledTypeAmount,
            entityKills, mostKilledId, mostKilledCount, foodEaten, mostEatenId, mostEatenCount,
            screenTimeTicks, mostScreenId, mostScreenTicks, fallDamage
        );
    }

    private static Set<UUID> collectParticipantUuids() {
        Set<UUID> uuids = new HashSet<>();
        uuids.addAll(GSRStats.DISTANCE_MOVED.keySet());
        uuids.addAll(GSRStats.TOTAL_DAMAGE_DEALT.keySet());
        uuids.addAll(GSRStats.TOTAL_DAMAGE_TAKEN.keySet());
        uuids.addAll(GSRStats.BLOCKS_PLACED.keySet());
        uuids.addAll(GSRStats.BLOCKS_BROKEN.keySet());
        uuids.addAll(GSRStats.DRAGON_DAMAGE_MAP.keySet());
        uuids.addAll(GSRStats.ENDER_PEARLS_COLLECTED.keySet());
        uuids.addAll(GSRStats.BLAZE_RODS_COLLECTED.keySet());
        uuids.addAll(GSRStats.ENTITY_KILLS.keySet());
        uuids.addAll(GSRStats.FOOD_EATEN.keySet());
        uuids.addAll(GSRStats.SCREEN_TIME_TICKS.keySet());
        uuids.addAll(GSRStats.FALL_DAMAGE_TAKEN.keySet());
        return uuids;
    }

    /**
     * Save the completed run to per-player folders (gsr_folder/worlds/&lt;world&gt;/completed_runs/&lt;uuid&gt;/run_&lt;endMs&gt;.nbt).
     * Call before reset when the run is completed (victory or fail). Each participant gets one file with run summary and their stats.
     */
    public static void saveCompletedRunToPlayerFolders(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || !config.isVictorious && !config.isFailed) return;

        Set<UUID> participantUuids = collectParticipantUuids();
        if (participantUuids.isEmpty()) return;

        long startMs = config.startTime > 0 ? config.startTime : System.currentTimeMillis();
        long endMs = startMs + config.frozenTime;
        String runId = UUID.randomUUID().toString();
        String worldName = server.getWorldData().getLevelName();
        String startDateIso = ISO_FORMAT.format(Instant.ofEpochMilli(startMs));
        String endDateIso = ISO_FORMAT.format(Instant.ofEpochMilli(endMs));
        String status = config.isVictorious ? GSRRunRecord.STATUS_VICTORY : GSRRunRecord.STATUS_FAIL;

        boolean deranked = config.antiCheatEnabled && config.locatorDeranked;
        String runDifficulty = GSRRunRecord.computeRunDifficulty(config.runParticipantCount, config.lowestDifficultyOrdinal);
        GSRRunRecord run = new GSRRunRecord(
            runId, worldName, startMs, endMs, startDateIso, endDateIso, status,
            config.failedByPlayerName != null ? config.failedByPlayerName : "",
            config.failedByDeathMessage != null ? config.failedByDeathMessage : "",
            config.runParticipantCount,
            config.timeNether, config.timeBastion, config.timeFortress, config.timeEnd, config.timeDragon,
            deranked,
            runDifficulty
        );
        CompoundTag runNbt = toNbt(run);

        Path baseDir = GSRStoragePaths.getWorldDir(server).resolve(GSRStorageParameters.COMPLETED_RUNS_DIR);
        for (UUID uuid : participantUuids) {
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            String name = (player != null) ? player.getName().getString() : uuid.toString();
            if (name == null || name.isEmpty()) name = uuid.toString();

            GSRRunPlayerSnapshot snap = buildSnapshotForPlayer(uuid, runId, name);
            Path playerDir = baseDir.resolve(uuid.toString());
            Path file = playerDir.resolve("run_" + endMs + ".json");
            try {
                Files.createDirectories(playerDir);
                CompoundTag root = new CompoundTag();
                root.put("run", runNbt);
                root.put("snapshot", snapshotToNbt(snap));
                GSRJsonUtil.writeNbtAsJson(file, root);
            } catch (IOException e) {
                LOGGER.warn("[GSR] Failed to save completed run for player {}: {}", uuid, e.getMessage());
            }
        }
        LOGGER.info("[GSR] Saved completed run to {} player folder(s)", participantUuids.size());
    }

    /**
     * Record the current run to the database: one run row, participant rows, and snapshot rows.
     * Each write uses atomic temp-then-rename to avoid corruption on crash.
     * Returns the save state (record + participants + snapshots) for broadcast and later reference.
     */
    public static GSRRunSaveState recordCurrentRun(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null) return null;

        String runId = UUID.randomUUID().toString();
        String worldName = server.getWorldData().getLevelName();
        long startMs = config.startTime > 0 ? config.startTime : System.currentTimeMillis();
        long endMs = config.isTimerFrozen ? startMs + config.frozenTime : System.currentTimeMillis();
        String startDateIso = ISO_FORMAT.format(Instant.ofEpochMilli(startMs));
        String endDateIso = ISO_FORMAT.format(Instant.ofEpochMilli(endMs));
        String status = config.isVictorious ? GSRRunRecord.STATUS_VICTORY : GSRRunRecord.STATUS_FAIL;

        boolean deranked = config.antiCheatEnabled && config.locatorDeranked;
        String runDifficulty = GSRRunRecord.computeRunDifficulty(config.runParticipantCount, config.lowestDifficultyOrdinal);
        GSRRunRecord run = new GSRRunRecord(
            runId,
            worldName,
            startMs,
            endMs,
            startDateIso,
            endDateIso,
            status,
            config.failedByPlayerName != null ? config.failedByPlayerName : "",
            config.failedByDeathMessage != null ? config.failedByDeathMessage : "",
            config.runParticipantCount,
            config.timeNether,
            config.timeBastion,
            config.timeFortress,
            config.timeEnd,
            config.timeDragon,
            deranked,
            runDifficulty
        );
        appendRun(server, run);

        List<GSRRunParticipant> participants = new ArrayList<>();
        List<GSRRunPlayerSnapshot> snapshots = new ArrayList<>();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID uuid = player.getUUID();
            String name = player.getName().getString();
            GSRRunParticipant p = new GSRRunParticipant(runId, uuid.toString(), name);
            appendRunParticipant(server, p);
            participants.add(p);

            GSRRunPlayerSnapshot snap = buildSnapshotForPlayer(uuid, runId, name);
            appendRunPlayerSnapshot(server, snap);
            snapshots.add(snap);
        }
        GSRRunSaveState state = new GSRRunSaveState(run, participants, snapshots);
        Path perRunDir = getDbDir(server).resolve(GSRStorageParameters.DB_RUNS_DIR);
        Path perRunPath = perRunDir.resolve(runId + ".json");
        try {
            Files.createDirectories(perRunDir);
            CompoundTag root = GSRRunSaveStateNbt.toNbt(state);
            Path tmp = perRunPath.resolveSibling(perRunPath.getFileName() + GSRStorageParameters.ATOMIC_WRITE_SUFFIX);
            GSRJsonUtil.writeNbtAsJson(tmp, root);
            Files.move(tmp, perRunPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            LOGGER.warn("[GSR] Failed to write per-run file {}: {}", perRunPath, e.getMessage());
        }
        LOGGER.info("[GSR] Recorded run {} ({} participants, ended {})", runId, config.runParticipantCount, endDateIso);
        return state;
    }

    private static ListTag loadList(Path path) {
        return loadList(path, "runs");
    }

    private static ListTag loadList(Path path, String key) {
        if (!Files.exists(path)) return new ListTag();
        try {
            CompoundTag root = GSRJsonUtil.readNbtFromJson(path);
            return root.getList(key).orElse(new ListTag());
        } catch (Exception e) {
            LOGGER.warn("[GSR] Failed to load list from {}", path, e);
        }
        return new ListTag();
    }

    private static CompoundTag toNbt(GSRRunRecord r) {
        CompoundTag c = new CompoundTag();
        c.putString("runId", r.runId());
        c.putString("worldName", r.worldName());
        c.putLong("startMs", r.startMs());
        c.putLong("endMs", r.endMs());
        c.putString("startDateIso", r.startDateIso());
        c.putString("endDateIso", r.endDateIso());
        c.putString("status", r.status());
        c.putString("failedByPlayerName", r.failedByPlayerName());
        c.putString("failedByDeathMessage", r.failedByDeathMessage());
        c.putInt("participantCount", r.participantCount());
        c.putLong("timeNether", r.timeNether());
        c.putLong("timeBastion", r.timeBastion());
        c.putLong("timeFortress", r.timeFortress());
        c.putLong("timeEnd", r.timeEnd());
        c.putLong("timeDragon", r.timeDragon());
        c.putBoolean("deranked", r.deranked());
        c.putString("runDifficulty", r.runDifficulty() != null ? r.runDifficulty() : "");
        return c;
    }

    private static GSRRunRecord fromNbt(CompoundTag c) {
        return new GSRRunRecord(
            c.getString("runId").orElse(""),
            c.getString("worldName").orElse(""),
            GSRNbtUtil.getLong(c, "startMs").orElse(0L),
            GSRNbtUtil.getLong(c, "endMs").orElse(0L),
            c.getString("startDateIso").orElse(""),
            c.getString("endDateIso").orElse(""),
            c.getString("status").orElse(""),
            c.getString("failedByPlayerName").orElse(""),
            c.getString("failedByDeathMessage").orElse(""),
            GSRNbtUtil.getInt(c, "participantCount").orElse(0),
            GSRNbtUtil.getLong(c, "timeNether").orElse(0L),
            GSRNbtUtil.getLong(c, "timeBastion").orElse(0L),
            GSRNbtUtil.getLong(c, "timeFortress").orElse(0L),
            GSRNbtUtil.getLong(c, "timeEnd").orElse(0L),
            GSRNbtUtil.getLong(c, "timeDragon").orElse(0L),
            GSRNbtUtil.getBoolean(c, "deranked").orElse(false),
            c.getString("runDifficulty").filter(s -> s != null && !s.isEmpty()).orElse("—")
        );
    }

    private static CompoundTag participantToNbt(GSRRunParticipant p) {
        CompoundTag c = new CompoundTag();
        c.putString("runId", p.runId());
        c.putString("playerUuid", p.playerUuid());
        c.putString("playerName", p.playerName());
        return c;
    }

    private static CompoundTag snapshotToNbt(GSRRunPlayerSnapshot s) {
        CompoundTag c = new CompoundTag();
        c.putString("runId", s.runId());
        c.putString("playerUuid", s.playerUuid());
        c.putString("playerName", s.playerName());
        c.putFloat("damageDealt", s.damageDealt());
        c.putFloat("damageTaken", s.damageTaken());
        if (s.mostDamageDealtTypeId() != null) c.putString("mostDamageDealtTypeId", s.mostDamageDealtTypeId());
        c.putFloat("mostDamageDealtTypeAmount", s.mostDamageDealtTypeAmount());
        if (s.mostDamageTakenTypeId() != null) c.putString("mostDamageTakenTypeId", s.mostDamageTakenTypeId());
        c.putFloat("mostDamageTakenTypeAmount", s.mostDamageTakenTypeAmount());
        c.putFloat("dragonDamage", s.dragonDamage());
        c.putInt("enderPearls", s.enderPearls());
        c.putInt("blazeRods", s.blazeRods());
        c.putInt("blocksPlaced", s.blocksPlaced());
        c.putInt("blocksBroken", s.blocksBroken());
        if (s.mostPlacedBlockId() != null) c.putString("mostPlacedBlockId", s.mostPlacedBlockId());
        c.putInt("mostPlacedBlockCount", s.mostPlacedBlockCount());
        if (s.mostBrokenBlockId() != null) c.putString("mostBrokenBlockId", s.mostBrokenBlockId());
        c.putInt("mostBrokenBlockCount", s.mostBrokenBlockCount());
        c.putFloat("distanceMoved", s.distanceMoved());
        if (s.mostTraveledTypeId() != null) c.putString("mostTraveledTypeId", s.mostTraveledTypeId());
        c.putFloat("mostTraveledTypeAmount", s.mostTraveledTypeAmount());
        c.putInt("entityKills", s.entityKills());
        if (s.mostKilledEntityId() != null) c.putString("mostKilledEntityId", s.mostKilledEntityId());
        c.putInt("mostKilledEntityCount", s.mostKilledEntityCount());
        c.putInt("foodEaten", s.foodEaten());
        if (s.mostEatenFoodId() != null) c.putString("mostEatenFoodId", s.mostEatenFoodId());
        c.putInt("mostEatenFoodCount", s.mostEatenFoodCount());
        c.putLong("screenTimeTicks", s.screenTimeTicks());
        if (s.mostUsedScreenId() != null) c.putString("mostUsedScreenId", s.mostUsedScreenId());
        c.putLong("mostUsedScreenTicks", s.mostUsedScreenTicks());
        c.putFloat("fallDamageTaken", s.fallDamageTaken());
        return c;
    }

    private static GSRRunPlayerSnapshot snapshotFromNbt(CompoundTag c) {
        return new GSRRunPlayerSnapshot(
            c.getString("runId").orElse(""),
            c.getString("playerUuid").orElse(""),
            c.getString("playerName").orElse(""),
            c.getFloat("damageDealt").orElse(0f),
            c.getFloat("damageTaken").orElse(0f),
            c.getString("mostDamageDealtTypeId").orElse(null),
            c.getFloat("mostDamageDealtTypeAmount").orElse(0f),
            c.getString("mostDamageTakenTypeId").orElse(null),
            c.getFloat("mostDamageTakenTypeAmount").orElse(0f),
            c.getFloat("dragonDamage").orElse(0f),
            c.getInt("enderPearls").orElse(0),
            c.getInt("blazeRods").orElse(0),
            c.getInt("blocksPlaced").orElse(0),
            c.getInt("blocksBroken").orElse(0),
            c.getString("mostPlacedBlockId").orElse(null),
            c.getInt("mostPlacedBlockCount").orElse(0),
            c.getString("mostBrokenBlockId").orElse(null),
            c.getInt("mostBrokenBlockCount").orElse(0),
            c.getFloat("distanceMoved").orElse(0f),
            c.getString("mostTraveledTypeId").orElse(null),
            c.getFloat("mostTraveledTypeAmount").orElse(0f),
            c.getInt("entityKills").orElse(0),
            c.getString("mostKilledEntityId").orElse(null),
            c.getInt("mostKilledEntityCount").orElse(0),
            c.getInt("foodEaten").orElse(0),
            c.getString("mostEatenFoodId").orElse(null),
            c.getInt("mostEatenFoodCount").orElse(0),
            c.getLong("screenTimeTicks").orElse(0L),
            c.getString("mostUsedScreenId").orElse(null),
            c.getLong("mostUsedScreenTicks").orElse(0L),
            c.getFloat("fallDamageTaken").orElse(0f)
        );
    }
}
