package net.berkle.groupspeedrun.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * S2C: Server notifies client that the run was victorious. Client plays victory sounds
 * and spawns firework particles around players for the celebration duration.
 */
public record GSRVictoryCelebrationPayload() implements CustomPacketPayload {

    public static final Type<GSRVictoryCelebrationPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "victory_celebration"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRVictoryCelebrationPayload> CODEC = StreamCodec.unit(new GSRVictoryCelebrationPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
