package net.berkle.groupspeedrun.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** C2S: Client requests player list and current participant config for Run Manager. */
public record GSRRunManagerRequestPayload() implements CustomPacketPayload {

    public static final Type<GSRRunManagerRequestPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "run_manager_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRRunManagerRequestPayload> CODEC =
            StreamCodec.unit(new GSRRunManagerRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
