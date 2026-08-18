package net.berkle.groupspeedrun.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** C2S: Client requests run data for run IDs they don't have. */
public record GSRRunRequestPayload(net.minecraft.nbt.CompoundTag nbt) implements CustomPacketPayload {

    public static final Type<GSRRunRequestPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "run_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRRunRequestPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, GSRRunRequestPayload::nbt,
            GSRRunRequestPayload::new
    );

    public static final String KEY_RUN_IDS = "runIds";

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
