package net.berkle.groupspeedrun.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * S2C: Server notifies client that the run was victorious. Client plays victory sounds
 * and spawns firework particles around players for the celebration duration.
 */
public record GSRVictoryCelebrationPayload() implements CustomPayload {

    public static final Id<GSRVictoryCelebrationPayload> ID = new Id<>(Identifier.of("gsr", "victory_celebration"));

    public static final PacketCodec<PacketByteBuf, GSRVictoryCelebrationPayload> CODEC = PacketCodec.unit(new GSRVictoryCelebrationPayload());

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
