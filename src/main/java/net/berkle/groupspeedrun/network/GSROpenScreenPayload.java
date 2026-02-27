package net.berkle.groupspeedrun.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * S2C: Tells the client to open a GSR screen (config or controls).
 */
@SuppressWarnings("null")
public record GSROpenScreenPayload(byte screenType) implements CustomPayload {

    public static final Id<GSROpenScreenPayload> ID = new Id<>(Identifier.of("gsr", "open_screen"));

    public static final byte TYPE_CONFIG = 0;
    public static final byte TYPE_CONTROLS = 1;

    public static final PacketCodec<PacketByteBuf, GSROpenScreenPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BYTE, GSROpenScreenPayload::screenType,
            GSROpenScreenPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
