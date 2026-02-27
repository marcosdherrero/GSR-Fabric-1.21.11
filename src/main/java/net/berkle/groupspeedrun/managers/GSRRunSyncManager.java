package net.berkle.groupspeedrun.managers;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.berkle.groupspeedrun.data.GSRRunSaveState;
import net.berkle.groupspeedrun.data.GSRRunSaveStateNbt;
import net.berkle.groupspeedrun.network.GSRRunDataPayload;
import net.berkle.groupspeedrun.network.GSRRunIdsPayload;
import net.berkle.groupspeedrun.network.GSRRunIdsRequestPayload;
import net.berkle.groupspeedrun.network.GSRRunRequestBroadcastPayload;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Server-side run sync: when players join, exchange run IDs and relay run data.
 */
@SuppressWarnings("null")
public final class GSRRunSyncManager {

    private static final Map<UUID, Set<String>> playerRunIds = new HashMap<>();
    private static UUID runRequestRequester = null;
    private static Set<String> runRequestIds = new HashSet<>();
    /** Cached server run IDs; refreshed on first use and when runs are recorded. */
    private static Set<String> serverRunIdsCache = new HashSet<>();
    private static boolean serverRunIdsCacheValid = false;

    private GSRRunSyncManager() {}

    /** Call when a new run is recorded to keep the run IDs cache up to date. */
    public static void addServerRunId(String runId) {
        if (runId != null && !runId.isEmpty()) {
            serverRunIdsCache.add(runId);
        }
    }

    private static Set<String> getServerRunIds(MinecraftServer server) {
        if (!serverRunIdsCacheValid) {
            serverRunIdsCache.clear();
            for (var r : GSRDataStore.loadRuns(server)) {
                serverRunIdsCache.add(r.runId());
            }
            serverRunIdsCacheValid = true;
        }
        return serverRunIdsCache;
    }

    /** Called when a player joins: request run IDs from all clients. */
    public static void onPlayerJoin(MinecraftServer server, ServerPlayerEntity joiner) {
        for (ServerPlayerEntity p : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(p, new GSRRunIdsRequestPayload());
        }
    }

    /** Called when a player disconnects: clear their run IDs and requester if they were requesting. */
    public static void onPlayerDisconnect(UUID playerUuid) {
        playerRunIds.remove(playerUuid);
        if (playerUuid.equals(runRequestRequester)) {
            runRequestRequester = null;
            runRequestIds.clear();
        }
    }

    /** Handle C2S run IDs from client. */
    public static void handleRunIds(MinecraftServer server, ServerPlayerEntity sender, List<String> runIds) {
        playerRunIds.put(sender.getUuid(), new HashSet<>(runIds));

        // Union of others' run IDs plus this world's run IDs from server storage.
        // Ensures joiners get runs from both other clients and world data (e.g. if all participants disconnected).
        Set<String> othersRunIds = new HashSet<>();
        for (Map.Entry<UUID, Set<String>> e : playerRunIds.entrySet()) {
            if (!e.getKey().equals(sender.getUuid())) {
                othersRunIds.addAll(e.getValue());
            }
        }
        othersRunIds.addAll(getServerRunIds(server));
        if (!othersRunIds.isEmpty()) {
            NbtCompound nbt = GSRRunIdsPayload.toNbt(new ArrayList<>(othersRunIds));
            ServerPlayNetworking.send(sender, new GSRRunIdsPayload(nbt));
        }
    }

    /** Handle C2S run request from client. */
    public static void handleRunRequest(MinecraftServer server, ServerPlayerEntity requester, List<String> runIds) {
        if (runIds.isEmpty()) return;
        runRequestRequester = requester.getUuid();
        runRequestIds = new HashSet<>(runIds);

        NbtCompound nbt = GSRRunRequestBroadcastPayload.toNbt(requester.getUuid(), runIds);
        for (ServerPlayerEntity p : PlayerLookup.all(server)) {
            if (!p.getUuid().equals(requester.getUuid())) {
                ServerPlayNetworking.send(p, new GSRRunRequestBroadcastPayload(nbt));
            }
        }

        // Provision runs from server world storage so requester gets them even if no other client has them.
        for (String runId : runIds) {
            GSRRunSaveState state = GSRDataStore.loadRunSaveStateByRunId(server, runId);
            if (state != null) {
                NbtCompound runNbt = GSRRunSaveStateNbt.toNbt(state);
                ServerPlayNetworking.send(requester, new GSRRunDataPayload(GSRRunDataPayload.toNbt(runId, runNbt)));
            }
        }
    }

    /** Handle C2S run data from client; forward to requester if applicable. */
    public static void handleRunData(MinecraftServer server, ServerPlayerEntity sender, NbtCompound payloadNbt) {
        String runId = payloadNbt.getString(GSRRunDataPayload.KEY_RUN_ID).orElse(null);
        NbtCompound runNbt = payloadNbt.getCompound(GSRRunDataPayload.KEY_RUN_NBT).orElse(null);
        if (runId == null || runNbt == null || runRequestRequester == null || !runRequestIds.contains(runId)) {
            return;
        }
        ServerPlayerEntity requester = server.getPlayerManager().getPlayer(runRequestRequester);
        if (requester != null) {
            ServerPlayNetworking.send(requester, new GSRRunDataPayload(GSRRunDataPayload.toNbt(runId, runNbt)));
        }
    }
}
