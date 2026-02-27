package net.berkle.groupspeedrun.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** S2C: Server tells client to send their run IDs. */
public record GSRRunIdsRequestPayload() implements CustomPayload {

    public static final Id<GSRRunIdsRequestPayload> ID = new Id<>(Identifier.of("gsr", "run_ids_request"));

    public static final PacketCodec<PacketByteBuf, GSRRunIdsRequestPayload> CODEC = PacketCodec.unit(new GSRRunIdsRequestPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
