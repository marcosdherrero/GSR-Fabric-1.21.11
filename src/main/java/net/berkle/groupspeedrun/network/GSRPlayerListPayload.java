package net.berkle.groupspeedrun.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** S2C: Server sends list of online players (UUID + name) for Run Manager screens. */
public record GSRPlayerListPayload(net.minecraft.nbt.CompoundTag nbt) implements CustomPacketPayload {

    public static final Type<GSRPlayerListPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "player_list"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRPlayerListPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG_COMPOUND, GSRPlayerListPayload::nbt,
            GSRPlayerListPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
