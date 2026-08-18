package net.berkle.groupspeedrun.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** S2C: Server tells client to send their run IDs. */
public record GSRRunIdsRequestPayload() implements CustomPacketPayload {

    public static final Type<GSRRunIdsRequestPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "run_ids_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRRunIdsRequestPayload> CODEC = StreamCodec.unit(new GSRRunIdsRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
