package net.berkle.groupspeedrun.config;

import net.berkle.groupspeedrun.parameter.GSRStorageParameters;
import net.berkle.groupspeedrun.parameter.GSRWorldConfigParameters;
import net.berkle.groupspeedrun.util.GSRJsonUtil;
import net.berkle.groupspeedrun.util.GSRNbtUtil;
import net.berkle.groupspeedrun.util.GSRStoragePaths;
import net.minecraft.nbt.CompoundTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Title-screen / new-world default for seed validation. World config also stores the flag;
 * this file is the source of truth when no world is loaded (create-world).
 */
public final class GSRSeedFilterSettings {

    private static final Logger LOGGER = LoggerFactory.getLogger("GSR-SeedFilter");

    public static final boolean DEFAULT_ENABLED = true;

    private static volatile boolean enabled = DEFAULT_ENABLED;
    private static volatile boolean loaded = false;

    private GSRSeedFilterSettings() {}

    public static boolean isEnabled() {
        ensureLoaded();
        return enabled;
    }

    public static void setEnabled(boolean value) {
        enabled = value;
        loaded = true;
        save();
    }

    public static void ensureLoaded() {
        if (!loaded) load();
    }

    public static void load() {
        Path path = file();
        enabled = DEFAULT_ENABLED;
        if (Files.exists(path)) {
            try {
                CompoundTag nbt = GSRJsonUtil.readNbtFromFile(path);
                GSRNbtUtil.getBoolean(nbt, GSRWorldConfigParameters.K_SEED_FILTER_ENABLED).ifPresent(v -> enabled = v);
            } catch (Exception e) {
                LOGGER.warn("GSR: Could not read seed filter setting from {}", path, e);
            }
        }
        loaded = true;
    }

    private static void save() {
        Path path = file();
        try {
            Files.createDirectories(path.getParent());
            CompoundTag nbt = new CompoundTag();
            nbt.putBoolean(GSRWorldConfigParameters.K_SEED_FILTER_ENABLED, enabled);
            GSRJsonUtil.writeNbtAsJsonAtomic(path, nbt);
        } catch (Exception e) {
            LOGGER.warn("GSR: Could not write seed filter setting to {}", path, e);
        }
    }

    private static Path file() {
        return GSRStoragePaths.getGsrRoot().resolve(GSRStorageParameters.SEED_FILTER_FILE);
    }
}
