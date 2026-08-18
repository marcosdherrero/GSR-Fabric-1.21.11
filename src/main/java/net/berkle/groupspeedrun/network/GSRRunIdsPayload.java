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

/** C2S: Client sends their run IDs. S2C: Server sends other players' run IDs to a joiner. */
public record GSRRunIdsPayload(net.minecraft.nbt.CompoundTag nbt) implements CustomPacketPayload {

    public static final Type<GSRRunIdsPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "run_ids"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRRunIdsPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, GSRRunIdsPayload::nbt,
            GSRRunIdsPayload::new
    );

    public static final String KEY_RUN_IDS = "runIds";

    public static CompoundTag toNbt(List<String> runIds) {
        CompoundTag nbt = new CompoundTag();
        ListTag list = new ListTag();
        for (String id : runIds) list.add(StringTag.valueOf(id));
        nbt.put(KEY_RUN_IDS, list);
        return nbt;
    }

    public static List<String> fromNbt(CompoundTag nbt) {
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
