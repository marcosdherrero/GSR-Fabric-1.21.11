package net.berkle.groupspeedrun.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** S2C: Server sends completed run data so clients can save to shared folder. */
public record GSRRunCompletePayload(net.minecraft.nbt.CompoundTag nbt) implements CustomPacketPayload {

    public static final Type<GSRRunCompletePayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "run_complete"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRRunCompletePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG_COMPOUND, GSRRunCompletePayload::nbt,
            GSRRunCompletePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
