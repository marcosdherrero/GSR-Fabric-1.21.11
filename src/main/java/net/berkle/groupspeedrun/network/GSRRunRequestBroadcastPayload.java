package net.berkle.groupspeedrun.network;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** S2C: Server tells clients (except requester) that a player needs these run IDs. Send run data if you have it. */
public record GSRRunRequestBroadcastPayload(net.minecraft.nbt.CompoundTag nbt) implements CustomPacketPayload {

    public static final Type<GSRRunRequestBroadcastPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "run_request_broadcast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRRunRequestBroadcastPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, GSRRunRequestBroadcastPayload::nbt,
            GSRRunRequestBroadcastPayload::new
    );

    public static final String KEY_REQUESTER_UUID = "requesterUuid";
    public static final String KEY_RUN_IDS = "runIds";

    public static CompoundTag toNbt(UUID requesterUuid, List<String> runIds) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString(KEY_REQUESTER_UUID, requesterUuid.toString());
        ListTag list = new ListTag();
        for (String id : runIds) list.add(StringTag.valueOf(id));
        nbt.put(KEY_RUN_IDS, list);
        return nbt;
    }

    public static UUID getRequesterUuid(CompoundTag nbt) {
        return UUID.fromString(nbt.getString(KEY_REQUESTER_UUID).orElse("00000000-0000-0000-0000-000000000000"));
    }

    public static List<String> getRunIds(CompoundTag nbt) {
        List<String> out = new ArrayList<>();
        ListTag list = nbt.getList(KEY_RUN_IDS).orElse(new ListTag());
        for (int i = 0; i < list.size(); i++) {
            Tag el = list.get(i);
            if (el instanceof StringTag nbtStr) {
                nbtStr.asString().ifPresent(out::add);
            }
        }
        return out;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
