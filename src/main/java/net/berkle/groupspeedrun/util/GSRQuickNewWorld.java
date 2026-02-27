package net.berkle.groupspeedrun.util;

import net.berkle.groupspeedrun.parameter.GSRStorageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

/**
 * Run counter and suggested world name for "New GSR World" (gsr_run_X_host_Name).
 */
public final class GSRQuickNewWorld {

    private static final Logger LOGGER = LoggerFactory.getLogger("GSR-QuickNewWorld");
    private static final Pattern SANITIZE = Pattern.compile("[^a-zA-Z0-9_-]");

    private GSRQuickNewWorld() {}

    public static Path getGsrConfigDir() {
        return GSRStoragePaths.getGsrRoot();
    }

    public static int getAndIncrementRunCount() {
        Path dir = getGsrConfigDir();
        Path file = dir.resolve(GSRStorageParameters.RUN_COUNT_FILE);
        int next = 1;
        try {
            Files.createDirectories(dir);
            if (Files.exists(file)) {
                net.minecraft.nbt.NbtCompound nbt = GSRJsonUtil.readNbtFromFile(file);
                int count = nbt.getInt("count").orElse(0);
                if (count > 0) next = count + 1;
            }
            Path legacyFile = dir.resolve("run_count.txt");
            if (Files.exists(legacyFile)) {
                try {
                    String s = Files.readString(legacyFile).trim();
                    if (!s.isEmpty()) next = Math.max(next, Integer.parseInt(s) + 1);
                    Files.deleteIfExists(legacyFile);
                } catch (NumberFormatException ignored) {}
            }
            net.minecraft.nbt.NbtCompound nbt = new net.minecraft.nbt.NbtCompound();
            nbt.putInt("count", next);
            GSRJsonUtil.writeNbtAsJson(file, nbt);
        } catch (IOException | NumberFormatException e) {
            LOGGER.warn("[GSR] Could not read/write run count", e);
        }
        return next;
    }

    /** Sanitize for use in folder name (replace invalid chars with underscore). */
    public static String sanitizeHostName(String name) {
        if (name == null || name.isEmpty()) return "Host";
        return SANITIZE.matcher(name).replaceAll("_");
    }

    public static String suggestedWorldName(int runNumber, String hostName) {
        return "gsr_run_" + runNumber + "_host_" + sanitizeHostName(hostName);
    }
}
