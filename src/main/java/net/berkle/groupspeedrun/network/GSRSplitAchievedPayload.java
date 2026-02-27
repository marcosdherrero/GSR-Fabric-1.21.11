package net.berkle.groupspeedrun.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * S2C: Server notifies client that a split was achieved. Client plays level-up chime.
 *
 * @param splitName Display name (e.g. "Nether", "Bastion")
 * @param timeMs   Split time in milliseconds
 */
public record GSRSplitAchievedPayload(String splitName, long timeMs) implements CustomPayload {

    public static final Id<GSRSplitAchievedPayload> ID = new Id<>(Identifier.of("gsr", "split_achieved"));

    public static final PacketCodec<PacketByteBuf, GSRSplitAchievedPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, GSRSplitAchievedPayload::splitName,
            PacketCodecs.VAR_LONG, GSRSplitAchievedPayload::timeMs,
            (splitName, timeMs) -> new GSRSplitAchievedPayload(splitName, timeMs != null ? timeMs : 0L)
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
