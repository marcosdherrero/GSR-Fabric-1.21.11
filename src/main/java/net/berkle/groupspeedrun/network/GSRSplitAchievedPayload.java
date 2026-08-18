package net.berkle.groupspeedrun.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * S2C: Server notifies client that a split was achieved. Client plays level-up chime.
 *
 * @param splitName Display name (e.g. "Nether", "Bastion")
 * @param timeMs   Split time in milliseconds
 */
public record GSRSplitAchievedPayload(String splitName, long timeMs) implements CustomPacketPayload {

    public static final Type<GSRSplitAchievedPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "split_achieved"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRSplitAchievedPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, GSRSplitAchievedPayload::splitName,
            ByteBufCodecs.VAR_LONG, GSRSplitAchievedPayload::timeMs,
            (splitName, timeMs) -> new GSRSplitAchievedPayload(splitName, timeMs != null ? timeMs : 0L)
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
