package net.berkle.groupspeedrun.managers;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.mixin.accessors.GSRMinecraftServerAccessor;
import net.berkle.groupspeedrun.parameter.GSRStorageParameters;
import net.berkle.groupspeedrun.util.GSRJsonUtil;
import net.berkle.groupspeedrun.util.GSRStoragePaths;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Full world snapshot for GSR reset: copy region/entities/poi/level.dat when the run is still
 * primed, then restore over the live save on the next world load (after disconnect).
 */
public final class GSRWorldSnapshotManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("GSR-Snapshot");
    private static final String SESSION_LOCK = "session.lock";
    private static final String LEVEL_DAT = "level.dat";

    private GSRWorldSnapshotManager() {}

    /** True when a usable original-world backup exists (must include {@code level.dat}). */
    public static boolean hasValidSnapshot(MinecraftServer server) {
        Path snapshotDir = GSRStoragePaths.getSnapshotDir(server);
        return Files.isDirectory(snapshotDir) && Files.isRegularFile(snapshotDir.resolve(LEVEL_DAT));
    }

    /** Canonical save folder id for {@code WorldOpenFlows.openWorld}. */
    public static String getLevelId(MinecraftServer server) {
        try {
            return ((GSRMinecraftServerAccessor) server).gsr$getStorageSource().getLevelId();
        } catch (Exception e) {
            Path root = server.getWorldPath(LevelResource.ROOT).toAbsolutePath().normalize();
            String name = root.getFileName().toString();
            if (name.isEmpty() || ".".equals(name)) {
                Path parent = root.getParent();
                name = parent != null ? parent.getFileName().toString() : "world";
            }
            return name;
        }
    }

    /**
     * Call at the very start of SERVER_STARTING: if restore flag exists, copy snapshot over world root and clear flag.
     */
    public static void checkAndRestoreIfNeeded(MinecraftServer server) {
        Path worldRoot = server.getWorldPath(LevelResource.ROOT);
        Path worldDir = GSRStoragePaths.getWorldDir(server);
        Path flagFile = worldDir.resolve(GSRStorageParameters.RESTORE_FLAG_FILE);
        if (!Files.exists(flagFile)) return;

        Path snapshotDir = GSRStoragePaths.getSnapshotDir(server);
        if (!hasValidSnapshot(server)) {
            LOGGER.error("[GSR] Restore flag set but snapshot missing or incomplete at {}", snapshotDir);
            try { Files.deleteIfExists(flagFile); } catch (IOException e) { LOGGER.warn("[GSR] Could not remove flag", e); }
            return;
        }

        try {
            clearDirectory(worldRoot);
            copyDirectoryContents(snapshotDir, worldRoot);
            Files.deleteIfExists(flagFile);
            GSRConfigWorld config = GSRConfigWorld.load(server);
            if (config.isRunNotStarted()) {
                config.startTime = -1;
                config.save(server);
            }
            GSRMain.CONFIG = config;
            LOGGER.info("[GSR] World restored from snapshot.");
        } catch (IOException e) {
            LOGGER.error("[GSR] Failed to restore world from snapshot", e);
        }
    }

    private static void clearDirectory(Path dir) throws IOException {
        if (!Files.isDirectory(dir)) return;
        try (var stream = Files.list(dir)) {
            for (Path entry : stream.toList()) {
                if (shouldSkip(entry)) continue;
                if (Files.isDirectory(entry)) {
                    deleteRecursively(entry);
                } else {
                    try {
                        Files.delete(entry);
                    } catch (IOException e) {
                        LOGGER.warn("[GSR] Could not delete {} during restore", entry, e);
                    }
                }
            }
        }
    }

    private static void deleteRecursively(Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult postVisitDirectory(Path d, IOException e) throws IOException {
                if (e != null) throw e;
                Files.delete(d);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path f, BasicFileAttributes attrs) throws IOException {
                if (!shouldSkip(f)) {
                    Files.delete(f);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Take a full copy of the world directory to gsr_folder/snapshots/&lt;world&gt;/.
     * Call while the run is still primed (startTime &lt;= 0). Skips {@code session.lock}.
     */
    public static void takeSnapshotIfNeeded(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.isVictorious || config.isFailed) return;
        if (config.startTime > 0) return;
        if (hasValidSnapshot(server)) return;

        Path worldRoot = server.getWorldPath(LevelResource.ROOT);
        Path snapshotDir = GSRStoragePaths.getSnapshotDir(server);
        try {
            if (Files.exists(snapshotDir)) {
                deleteRecursively(snapshotDir);
            }
            server.saveAllChunks(true, false, false);
            copyDirectoryContents(worldRoot, snapshotDir);
            if (!hasValidSnapshot(server)) {
                LOGGER.error("[GSR] Snapshot incomplete at {} (missing level.dat)", snapshotDir);
                if (Files.exists(snapshotDir)) deleteRecursively(snapshotDir);
                return;
            }
            LOGGER.info("[GSR] World snapshot saved to {}", snapshotDir);
        } catch (IOException e) {
            LOGGER.error("[GSR] Failed to take world snapshot", e);
            try {
                if (Files.exists(snapshotDir) && !hasValidSnapshot(server)) deleteRecursively(snapshotDir);
            } catch (IOException ignored) {}
        }
    }

    /**
     * Set flag so that on next world load the world is restored from snapshot. Call from reset after validating snapshot.
     */
    public static void setRestoreFromSnapshotOnNextLoad(MinecraftServer server) {
        Path worldDir = GSRStoragePaths.getWorldDir(server);
        Path flagFile = worldDir.resolve(GSRStorageParameters.RESTORE_FLAG_FILE);
        try {
            Files.createDirectories(worldDir);
            GSRJsonUtil.writeNbtAsJson(flagFile, new net.minecraft.nbt.CompoundTag());
        } catch (IOException e) {
            LOGGER.error("[GSR] Failed to set restore flag", e);
        }
    }

    private static boolean shouldSkip(Path path) {
        Path name = path.getFileName();
        return name != null && SESSION_LOCK.equals(name.toString());
    }

    private static void copyDirectoryContents(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path rel = source.relativize(dir);
                if (rel.getNameCount() == 0) {
                    Files.createDirectories(target);
                    return FileVisitResult.CONTINUE;
                }
                Files.createDirectories(target.resolve(rel));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (shouldSkip(file)) return FileVisitResult.CONTINUE;
                Path rel = source.relativize(file);
                Path dest = target.resolve(rel);
                try {
                    Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    LOGGER.warn("[GSR] Skipping locked or unreadable file {}", file, e);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
