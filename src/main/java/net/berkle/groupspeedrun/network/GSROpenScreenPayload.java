package net.berkle.groupspeedrun.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * S2C: Tells the client to open a GSR screen (config or controls).
 */
@SuppressWarnings("null")
public record GSROpenScreenPayload(byte screenType) implements CustomPacketPayload {

    public static final Type<GSROpenScreenPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "open_screen"));

    public static final byte TYPE_CONFIG = 0;
    public static final byte TYPE_CONTROLS = 1;

    public static final StreamCodec<RegistryFriendlyByteBuf, GSROpenScreenPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, GSROpenScreenPayload::screenType,
            GSROpenScreenPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
