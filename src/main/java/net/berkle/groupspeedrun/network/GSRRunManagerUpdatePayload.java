package net.berkle.groupspeedrun.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** C2S: Client sends updated participant sets (group death, shared health, excluded) from Run Manager. */
public record GSRRunManagerUpdatePayload(net.minecraft.nbt.CompoundTag nbt) implements CustomPacketPayload {

    public static final Type<GSRRunManagerUpdatePayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "run_manager_update"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRRunManagerUpdatePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, GSRRunManagerUpdatePayload::nbt,
            GSRRunManagerUpdatePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
