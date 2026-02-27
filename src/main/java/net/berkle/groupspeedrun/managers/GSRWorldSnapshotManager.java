package net.berkle.groupspeedrun.managers;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.parameter.GSRStorageParameters;
import net.berkle.groupspeedrun.util.GSRJsonUtil;
import net.berkle.groupspeedrun.util.GSRStoragePaths;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
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
 * Manages full world snapshot for GSR reset: take snapshot when armed (startTime == -1),
 * restore from snapshot on next world load when flag is set.
 */
public final class GSRWorldSnapshotManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("GSR-Snapshot");

    private GSRWorldSnapshotManager() {}

    /**
     * Call at the very start of SERVER_STARTING: if restore flag exists, copy snapshot over world root and clear flag.
     */
    public static void checkAndRestoreIfNeeded(MinecraftServer server) {
        Path worldRoot = server.getSavePath(WorldSavePath.ROOT);
        Path worldDir = GSRStoragePaths.getWorldDir(server);
        Path flagFile = worldDir.resolve(GSRStorageParameters.RESTORE_FLAG_FILE);
        if (!Files.exists(flagFile)) return;

        Path snapshotDir = GSRStoragePaths.getSnapshotDir(server);
        if (!Files.isDirectory(snapshotDir)) {
            LOGGER.warn("[GSR] Restore flag set but snapshot not found at {}", snapshotDir);
            try { Files.deleteIfExists(flagFile); } catch (IOException e) { LOGGER.warn("[GSR] Could not remove flag", e); }
            return;
        }

        try {
            clearDirectory(worldRoot);
            copyDirectoryContents(snapshotDir, worldRoot);
            Files.deleteIfExists(flagFile);
            // Ensure armed state (startTime = -1) is written so auto-start works after restore
            GSRConfigWorld config = GSRConfigWorld.load(server);
            if (!config.isVictorious && !config.isFailed && config.startTime <= 0) {
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
                if (Files.isDirectory(entry)) {
                    deleteRecursively(entry);
                } else {
                    Files.delete(entry);
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
                Files.delete(f);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Take a full copy of the world directory to gsr_folder/snapshots/&lt;world&gt;/. Call when startTime == -1 and snapshot not yet taken.
     */
    public static void takeSnapshotIfNeeded(MinecraftServer server) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || config.startTime > 0 || config.isVictorious || config.isFailed) return;

        Path worldRoot = server.getSavePath(WorldSavePath.ROOT);
        Path snapshotDir = GSRStoragePaths.getSnapshotDir(server);
        if (Files.exists(snapshotDir)) return;

        try {
            server.save(true, false, false);
            copyDirectoryContents(worldRoot, snapshotDir);
            LOGGER.info("[GSR] World snapshot saved to {}", snapshotDir);
        } catch (IOException e) {
            LOGGER.error("[GSR] Failed to take world snapshot", e);
        }
    }

    /**
     * Set flag so that on next world load the world is restored from snapshot. Call from /gsr reset.
     */
    public static void setRestoreFromSnapshotOnNextLoad(MinecraftServer server) {
        Path worldDir = GSRStoragePaths.getWorldDir(server);
        Path flagFile = worldDir.resolve(GSRStorageParameters.RESTORE_FLAG_FILE);
        try {
            Files.createDirectories(worldDir);
            GSRJsonUtil.writeNbtAsJson(flagFile, new net.minecraft.nbt.NbtCompound());
        } catch (IOException e) {
            LOGGER.error("[GSR] Failed to set restore flag", e);
        }
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
                Path destDir = target.resolve(rel);
                Files.createDirectories(destDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path rel = source.relativize(file);
                Path dest = target.resolve(rel);
                Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
