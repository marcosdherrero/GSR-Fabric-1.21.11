package net.berkle.groupspeedrun.network;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** S2C: Server tells clients (except requester) that a player needs these run IDs. Send run data if you have it. */
public record GSRRunRequestBroadcastPayload(net.minecraft.nbt.NbtCompound nbt) implements CustomPayload {

    public static final Id<GSRRunRequestBroadcastPayload> ID = new Id<>(Identifier.of("gsr", "run_request_broadcast"));

    public static final PacketCodec<PacketByteBuf, GSRRunRequestBroadcastPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.NBT_COMPOUND, GSRRunRequestBroadcastPayload::nbt,
            GSRRunRequestBroadcastPayload::new
    );

    public static final String KEY_REQUESTER_UUID = "requesterUuid";
    public static final String KEY_RUN_IDS = "runIds";

    public static NbtCompound toNbt(UUID requesterUuid, List<String> runIds) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString(KEY_REQUESTER_UUID, requesterUuid.toString());
        NbtList list = new NbtList();
        for (String id : runIds) list.add(NbtString.of(id));
        nbt.put(KEY_RUN_IDS, list);
        return nbt;
    }

    public static UUID getRequesterUuid(NbtCompound nbt) {
        return UUID.fromString(nbt.getString(KEY_REQUESTER_UUID).orElse("00000000-0000-0000-0000-000000000000"));
    }

    public static List<String> getRunIds(NbtCompound nbt) {
        List<String> out = new ArrayList<>();
        NbtList list = nbt.getList(KEY_RUN_IDS).orElse(new NbtList());
        for (int i = 0; i < list.size(); i++) {
            NbtElement el = list.get(i);
            if (el instanceof NbtString nbtStr) {
                nbtStr.asString().ifPresent(out::add);
            }
        }
        return out;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
