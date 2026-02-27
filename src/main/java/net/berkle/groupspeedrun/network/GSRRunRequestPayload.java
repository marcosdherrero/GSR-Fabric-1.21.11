package net.berkle.groupspeedrun.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** C2S: Client requests run data for run IDs they don't have. */
public record GSRRunRequestPayload(net.minecraft.nbt.NbtCompound nbt) implements CustomPayload {

    public static final Id<GSRRunRequestPayload> ID = new Id<>(Identifier.of("gsr", "run_request"));

    public static final PacketCodec<PacketByteBuf, GSRRunRequestPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.NBT_COMPOUND, GSRRunRequestPayload::nbt,
            GSRRunRequestPayload::new
    );

    public static final String KEY_RUN_IDS = "runIds";

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
