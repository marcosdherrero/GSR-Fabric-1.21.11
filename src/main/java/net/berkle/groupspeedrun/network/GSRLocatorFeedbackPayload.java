package net.berkle.groupspeedrun.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * S2C: Locator could not find its target. Client shows an advancement-style toast (not a hover tooltip).
 *
 * @param structureType fortress, bastion, stronghold, or ship
 * @param missReason    dimension_unloaded, wrong_dimension, or not_found
 */
public record GSRLocatorFeedbackPayload(String structureType, String missReason) implements CustomPacketPayload {

    public static final Type<GSRLocatorFeedbackPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "locator_feedback"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRLocatorFeedbackPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, GSRLocatorFeedbackPayload::structureType,
            ByteBufCodecs.STRING_UTF8, GSRLocatorFeedbackPayload::missReason,
            GSRLocatorFeedbackPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
