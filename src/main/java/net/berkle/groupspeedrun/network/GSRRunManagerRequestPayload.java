package net.berkle.groupspeedrun.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** C2S: Client requests player list and current participant config for Run Manager. */
public record GSRRunManagerRequestPayload() implements CustomPayload {

    public static final Id<GSRRunManagerRequestPayload> ID = new Id<>(Identifier.of("gsr", "run_manager_request"));

    public static final PacketCodec<PacketByteBuf, GSRRunManagerRequestPayload> CODEC =
            PacketCodec.unit(new GSRRunManagerRequestPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
