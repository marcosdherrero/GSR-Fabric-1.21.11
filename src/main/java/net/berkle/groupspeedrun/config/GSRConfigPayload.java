package net.berkle.groupspeedrun.config;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * S2C payload carrying world run state (and later player HUD config) as NBT.
 */
public record GSRConfigPayload(CompoundTag nbt) implements CustomPacketPayload {

    public static final Type<GSRConfigPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "config_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRConfigPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, GSRConfigPayload::nbt,
            GSRConfigPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
