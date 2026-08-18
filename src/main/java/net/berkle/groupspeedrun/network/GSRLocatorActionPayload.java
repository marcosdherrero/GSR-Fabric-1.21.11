package net.berkle.groupspeedrun.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * C2S: Client requests a locator action from the GSR Locators menu (clear or toggle one structure).
 */
@SuppressWarnings("null")
public record GSRLocatorActionPayload(byte action) implements CustomPacketPayload {

    public static final Type<GSRLocatorActionPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "locator_action"));

    public static final byte ACTION_CLEAR = 0;
    public static final byte ACTION_TOGGLE_FORTRESS = 1;
    public static final byte ACTION_TOGGLE_BASTION = 2;
    public static final byte ACTION_TOGGLE_STRONGHOLD = 3;
    public static final byte ACTION_TOGGLE_SHIP = 4;

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRLocatorActionPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.BYTE, GSRLocatorActionPayload::action,
            GSRLocatorActionPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
