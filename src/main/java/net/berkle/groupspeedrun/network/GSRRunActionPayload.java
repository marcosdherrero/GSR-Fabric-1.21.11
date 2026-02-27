package net.berkle.groupspeedrun.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * C2S: Client requests a run action (start, pause, resume, reset). Server runs same logic as /gsr commands.
 */
@SuppressWarnings("null")
public record GSRRunActionPayload(byte action) implements CustomPayload {

    public static final Id<GSRRunActionPayload> ID = new Id<>(Identifier.of("gsr", "run_action"));

    public static final byte ACTION_START = 0;
    public static final byte ACTION_PAUSE = 1;
    public static final byte ACTION_RESUME = 2;
    public static final byte ACTION_RESET = 3;
    /** C2S: Single-player pause menu opened; server freezes timer. No admin check. */
    public static final byte ACTION_CLIENT_PAUSE = 4;
    /** C2S: Single-player pause menu closed; server unfreezes only if frozen by client pause. */
    public static final byte ACTION_CLIENT_RESUME = 5;

    public static final PacketCodec<PacketByteBuf, GSRRunActionPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BYTE, GSRRunActionPayload::action,
            GSRRunActionPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
