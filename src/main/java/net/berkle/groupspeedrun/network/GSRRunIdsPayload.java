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

/** C2S: Client sends their run IDs. S2C: Server sends other players' run IDs to a joiner. */
public record GSRRunIdsPayload(net.minecraft.nbt.NbtCompound nbt) implements CustomPayload {

    public static final Id<GSRRunIdsPayload> ID = new Id<>(Identifier.of("gsr", "run_ids"));

    public static final PacketCodec<PacketByteBuf, GSRRunIdsPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.NBT_COMPOUND, GSRRunIdsPayload::nbt,
            GSRRunIdsPayload::new
    );

    public static final String KEY_RUN_IDS = "runIds";

    public static NbtCompound toNbt(List<String> runIds) {
        NbtCompound nbt = new NbtCompound();
        NbtList list = new NbtList();
        for (String id : runIds) list.add(NbtString.of(id));
        nbt.put(KEY_RUN_IDS, list);
        return nbt;
    }

    public static List<String> fromNbt(NbtCompound nbt) {
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
