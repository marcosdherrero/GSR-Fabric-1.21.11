package net.berkle.groupspeedrun.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * C2S: Client requests a locator action from the GSR Locators menu (clear or toggle one structure).
 */
@SuppressWarnings("null")
public record GSRLocatorActionPayload(byte action) implements CustomPayload {

    public static final Id<GSRLocatorActionPayload> ID = new Id<>(Identifier.of("gsr", "locator_action"));

    public static final byte ACTION_CLEAR = 0;
    public static final byte ACTION_TOGGLE_FORTRESS = 1;
    public static final byte ACTION_TOGGLE_BASTION = 2;
    public static final byte ACTION_TOGGLE_STRONGHOLD = 3;
    public static final byte ACTION_TOGGLE_SHIP = 4;

    public static final PacketCodec<PacketByteBuf, GSRLocatorActionPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.BYTE, GSRLocatorActionPayload::action,
            GSRLocatorActionPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
