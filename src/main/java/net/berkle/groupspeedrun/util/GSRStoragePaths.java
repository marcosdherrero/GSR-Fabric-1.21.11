package net.berkle.groupspeedrun.util;

import net.berkle.groupspeedrun.parameter.GSRStorageParameters;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;

import java.nio.file.Path;

/**
 * Resolves GSR storage paths. World-specific data lives in world/data/gsr/ (world-colocated).
 * Legacy paths in gsr_folder/worlds/ are used only for migration.
 */
public final class GSRStoragePaths {

    private GSRStoragePaths() {}

    /** Root folder for all GSR data (instance main folder / gsr_folder). */
    public static Path getGsrRoot() {
        return FabricLoader.getInstance().getGameDir().resolve(GSRStorageParameters.GSR_ROOT);
    }

    /**
     * World-colocated GSR data dir (world_root/data/gsr/). Guarantees save and load use the same path.
     */
    public static Path getWorldDataDir(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("data").resolve("gsr");
    }

    /**
     * World-specific GSR data dir. Uses world-colocated path for guaranteed consistency.
     */
    public static Path getWorldDir(MinecraftServer server) {
        return getWorldDataDir(server);
    }

    /** Snapshot folder for a world (gsr_folder/snapshots/&lt;worldName&gt;/). */
    public static Path getSnapshotDir(MinecraftServer server) {
        String worldName = getWorldName(server);
        return getSnapshotDir(worldName);
    }

    /** Snapshot folder keyed by save id or display name (client restore uses the level id). */
    public static Path getSnapshotDir(String worldKey) {
        return getGsrRoot().resolve("snapshots").resolve(sanitizeForPath(worldKey));
    }

    /** Canonical world name for storage paths. Prefers level name; falls back to save path folder name. */
    private static String getWorldName(MinecraftServer server) {
        try {
            String levelName = server.getWorldData().getLevelName();
            if (levelName != null && !levelName.isEmpty()) {
                return sanitizeForPath(levelName);
            }
        } catch (Exception ignored) {}
        try {
            return sanitizeForPath(server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT).getFileName().toString());
        } catch (Exception e) {
            return "world";
        }
    }

    /** Replaces path-invalid characters and spaces with underscore for consistent paths. */
    public static String sanitizeForPath(String name) {
        if (name == null) return "world";
        return name.replaceAll("[\\\\/:*?\"<>|\\s]", "_");
    }

    /**
     * Returns legacy candidate paths (gsr_folder/worlds/&lt;name&gt;/) for migration from old storage.
     */
    public static Path[] getLegacyWorldDirCandidates(MinecraftServer server) {
        java.util.List<Path> candidates = new java.util.ArrayList<>();
        try {
            String levelName = server.getWorldData().getLevelName();
            String folderName = server.getWorldPath(LevelResource.ROOT).getFileName().toString();
            Path worldsBase = getGsrRoot().resolve("worlds");
            if (levelName != null && !levelName.isEmpty()) {
                Path byLevel = worldsBase.resolve(levelName);
                if (!candidates.contains(byLevel)) candidates.add(byLevel);
                String levelSanitized = sanitizeForPath(levelName);
                if (!levelSanitized.equals(levelName)) {
                    Path byLevelSanitized = worldsBase.resolve(levelSanitized);
                    if (!candidates.contains(byLevelSanitized)) candidates.add(byLevelSanitized);
                }
            }
            if (folderName != null && !folderName.isEmpty()) {
                Path byFolder = worldsBase.resolve(folderName);
                if (!candidates.contains(byFolder)) candidates.add(byFolder);
                String folderSanitized = sanitizeForPath(folderName);
                if (!folderSanitized.equals(folderName)) {
                    Path byFolderSanitized = worldsBase.resolve(folderSanitized);
                    if (!candidates.contains(byFolderSanitized)) candidates.add(byFolderSanitized);
                }
            }
        } catch (Exception ignored) {}
        return candidates.toArray(new Path[0]);
    }
}
