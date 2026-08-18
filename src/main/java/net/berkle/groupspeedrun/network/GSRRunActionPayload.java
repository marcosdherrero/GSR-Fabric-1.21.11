package net.berkle.groupspeedrun.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * C2S: Client requests a run action (start, pause, resume, reset). Server runs same logic as /gsr commands.
 */
@SuppressWarnings("null")
public record GSRRunActionPayload(byte action) implements CustomPacketPayload {

    public static final Type<GSRRunActionPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "run_action"));

    public static final byte ACTION_START = 0;
    public static final byte ACTION_PAUSE = 1;
    public static final byte ACTION_RESUME = 2;
    public static final byte ACTION_RESET = 3;
    /** C2S: Single-player pause menu opened; server freezes timer. No admin check. */
    public static final byte ACTION_CLIENT_PAUSE = 4;
    /** C2S: Single-player pause menu closed; server unfreezes only if frozen by client pause. */
    public static final byte ACTION_CLIENT_RESUME = 5;

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRRunActionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, GSRRunActionPayload::action,
            GSRRunActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
