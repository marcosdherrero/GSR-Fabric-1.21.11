package net.berkle.groupspeedrun.client;

import net.berkle.groupspeedrun.data.GSRRunParticipant;
import net.berkle.groupspeedrun.data.GSRRunPlayerSnapshot;
import net.berkle.groupspeedrun.data.GSRRunSaveState;
import net.berkle.groupspeedrun.data.GSRRunSaveStateNbt;
import net.berkle.groupspeedrun.parameter.GSRStorageParameters;
import net.berkle.groupspeedrun.util.GSRJsonUtil;
import net.berkle.groupspeedrun.util.GSRStoragePaths;
import net.minecraft.nbt.NbtCompound;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client-side loader for run history. Reads from personal (runs you completed) and shared
 * (runs synced from other players) folders. Merges by runId when the same run appears in both.
 */
public final class GSRSharedRunLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger("GSR-SharedRunLoader");

    private GSRSharedRunLoader() {}

    public static Path getPersonalRunsDir() {
        return GSRStoragePaths.getGsrRoot().resolve(GSRStorageParameters.PERSONAL_RUNS_DIR);
    }

    public static Path getSharedRunsDir() {
        return GSRStoragePaths.getGsrRoot().resolve(GSRStorageParameters.SHARED_RUNS_DIR);
    }

    /** Get run IDs from both personal and shared folders (for sync with other players). */
    public static List<String> getRunIds() {
        java.util.Set<String> ids = new java.util.LinkedHashSet<>();
        collectRunIds(getPersonalRunsDir(), ids);
        collectRunIds(getSharedRunsDir(), ids);
        return new ArrayList<>(ids);
    }

    private static void collectRunIds(Path dir, java.util.Set<String> out) {
        if (!Files.isDirectory(dir)) return;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                String name = path.getFileName().toString();
                if (name.startsWith("run_") && (name.endsWith(".json") || name.endsWith(".nbt"))) {
                    out.add(name.substring(4, name.length() - (name.endsWith(".json") ? 5 : 4)));
                }
            }
        } catch (IOException e) {
            LOGGER.warn("[GSR] Failed to list runs in {}: {}", dir.getFileName(), e.getMessage());
        }
    }

    /** Load a single run by ID. Checks personal then shared; merges if both have it. */
    public static GSRRunSaveState loadByRunId(String runId) {
        if (runId == null || runId.isEmpty()) return null;
        GSRRunSaveState personal = loadOne(getPersonalRunsDir().resolve("run_" + runId + ".json"));
        GSRRunSaveState shared = loadOne(getSharedRunsDir().resolve("run_" + runId + ".json"));
        if (personal != null && shared != null) return merge(personal, shared);
        return personal != null ? personal : shared;
    }

    /** Load all runs from personal and shared folders, merged by runId (same run = one entry). */
    public static List<GSRRunSaveState> loadAll() {
        Map<String, GSRRunSaveState> byRunId = new HashMap<>();
        loadDirIntoMap(getPersonalRunsDir(), byRunId);
        loadDirIntoMap(getSharedRunsDir(), byRunId);
        List<GSRRunSaveState> out = new ArrayList<>(byRunId.values());
        out.sort((a, b) -> Long.compare(b.record().endMs(), a.record().endMs()));
        return out;
    }

    private static void loadDirIntoMap(Path dir, Map<String, GSRRunSaveState> byRunId) {
        if (!Files.isDirectory(dir)) return;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                try {
                    String name = path.getFileName().toString();
                    if (!name.startsWith("run_") || (!name.endsWith(".json") && !name.endsWith(".nbt"))) continue;
                    GSRRunSaveState state = loadOne(path);
                    if (state != null) {
                        String id = state.record().runId();
                        byRunId.merge(id, state, GSRSharedRunLoader::merge);
                    }
                } catch (Exception e) {
                    LOGGER.warn("[GSR] Failed to load run file {}: {}", path.getFileName(), e.getMessage());
                }
            }
        } catch (IOException e) {
            LOGGER.warn("[GSR] Failed to list runs in {}: {}", dir.getFileName(), e.getMessage());
        }
    }

    /** Save a run to personal folder (runs you completed). Merges if runId already exists. */
    public static void saveToPersonal(GSRRunSaveState state) {
        saveToDir(state, getPersonalRunsDir());
    }

    /** Save a run to shared folder (runs synced from others). Merges if runId already exists. */
    public static void saveToShared(GSRRunSaveState state) {
        saveToDir(state, getSharedRunsDir());
    }

    /** Save to shared folder. Kept for backward compatibility (e.g. existing callers). */
    public static void save(GSRRunSaveState state) {
        saveToShared(state);
    }

    /**
     * Deletes a run from both personal and shared folders. Call after user confirms.
     * Does not delete the "current" (active) run.
     *
     * @param runId Run ID to delete.
     * @return True if at least one file was deleted.
     */
    public static boolean deleteRun(String runId) {
        if (runId == null || runId.isEmpty() || "current".equals(runId)) return false;
        boolean deleted = false;
        Path personalPath = getPersonalRunsDir().resolve("run_" + runId + ".json");
        Path sharedPath = getSharedRunsDir().resolve("run_" + runId + ".json");
        try {
            if (Files.exists(personalPath)) {
                Files.delete(personalPath);
                deleted = true;
            }
        } catch (IOException e) {
            LOGGER.warn("[GSR] Failed to delete run from personal: {}", e.getMessage());
        }
        try {
            if (Files.exists(sharedPath)) {
                Files.delete(sharedPath);
                deleted = true;
            }
        } catch (IOException e) {
            LOGGER.warn("[GSR] Failed to delete run from shared: {}", e.getMessage());
        }
        return deleted;
    }

    private static void saveToDir(GSRRunSaveState state, Path dir) {
        if (state == null || state.record() == null) return;
        Path path = dir.resolve("run_" + state.record().runId() + ".json");
        try {
            Files.createDirectories(dir);
            GSRRunSaveState existing = loadOne(path);
            GSRRunSaveState toSave = existing != null ? merge(existing, state) : state;
            NbtCompound root = GSRRunSaveStateNbt.toNbt(toSave);
            GSRJsonUtil.writeNbtAsJson(path, root);
        } catch (Exception e) {
            LOGGER.warn("[GSR] Failed to save run to {}: {}", dir.getFileName(), e.getMessage());
        }
    }

    private static GSRRunSaveState loadOne(Path path) {
        Path toRead = path;
        if (!Files.exists(path)) {
            String name = path.getFileName().toString();
            Path alt = name.endsWith(".json") ? path.getParent().resolve(name.replace(".json", ".nbt")) : path.getParent().resolve(name.replace(".nbt", ".json"));
            if (Files.exists(alt)) toRead = alt;
            else return null;
        }
        try {
            NbtCompound root = GSRJsonUtil.readNbtFromFile(toRead);
            return GSRRunSaveStateNbt.fromNbt(root);
        } catch (Exception e) {
            return null;
        }
    }

    private static GSRRunSaveState merge(GSRRunSaveState a, GSRRunSaveState b) {
        Map<String, GSRRunPlayerSnapshot> snapMap = new HashMap<>();
        for (GSRRunPlayerSnapshot s : a.snapshots()) snapMap.put(s.playerUuid(), s);
        for (GSRRunPlayerSnapshot s : b.snapshots()) snapMap.put(s.playerUuid(), s);
        Map<String, GSRRunParticipant> partMap = new HashMap<>();
        for (GSRRunParticipant p : a.participants()) partMap.put(p.playerUuid(), p);
        for (GSRRunParticipant p : b.participants()) partMap.put(p.playerUuid(), p);
        return new GSRRunSaveState(
            a.record(),
            new ArrayList<>(partMap.values()),
            new ArrayList<>(snapMap.values())
        );
    }

}
