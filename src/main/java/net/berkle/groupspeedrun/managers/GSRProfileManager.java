package net.berkle.groupspeedrun.managers;

import net.berkle.groupspeedrun.config.GSRConfigPlayer;
import net.berkle.groupspeedrun.util.GSRJsonUtil;
import net.berkle.groupspeedrun.util.GSRStoragePaths;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Phase 5: Per-player HUD config storage. Load/save from world/data/gsr_players.nbt.
 */
public final class GSRProfileManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("GSR-Profile");
    private static final String PLAYERS_FILE = "players.json";
    private static final Map<UUID, GSRConfigPlayer> PLAYER_CONFIGS = new ConcurrentHashMap<>();

    private GSRProfileManager() {}

    public static GSRConfigPlayer getPlayerConfig(ServerPlayerEntity player) {
        if (player == null) return new GSRConfigPlayer();
        return PLAYER_CONFIGS.computeIfAbsent(player.getUuid(), u -> new GSRConfigPlayer());
    }

    public static void updatePlayerSettings(ServerPlayerEntity player, GSRConfigPlayer config) {
        if (player != null && config != null) {
            PLAYER_CONFIGS.put(player.getUuid(), config);
        }
    }

    public static void save(MinecraftServer server) {
        if (server == null) return;
        Path worldDir = GSRStoragePaths.getWorldDir(server);
        Path path = worldDir.resolve(PLAYERS_FILE);
        try {
            Files.createDirectories(worldDir);
            NbtCompound root = new NbtCompound();
            for (Map.Entry<UUID, GSRConfigPlayer> e : PLAYER_CONFIGS.entrySet()) {
                NbtCompound sub = new NbtCompound();
                e.getValue().writeNbt(sub);
                root.put(e.getKey().toString(), sub);
            }
            GSRJsonUtil.writeNbtAsJson(path, root);
        } catch (IOException e) {
            LOGGER.error("[GSR] Failed to save player configs", e);
        }
    }

    public static void load(MinecraftServer server) {
        if (server == null) return;
        PLAYER_CONFIGS.clear();
        Path worldDir = GSRStoragePaths.getWorldDir(server);
        Path path = worldDir.resolve(PLAYERS_FILE);
        if (!Files.exists(path)) return;
        try {
            NbtCompound root = GSRJsonUtil.readNbtFromFile(path);
            root.getKeys().forEach(k -> {
                try {
                    UUID uuid = UUID.fromString(k);
                    root.getCompound(k).ifPresent(sub -> {
                        GSRConfigPlayer config = new GSRConfigPlayer();
                        config.readNbt(sub);
                        PLAYER_CONFIGS.put(uuid, config);
                    });
                } catch (Exception ignored) {}
            });
        } catch (Exception e) {
            LOGGER.warn("[GSR] Failed to load player configs", e);
        }
    }
}
