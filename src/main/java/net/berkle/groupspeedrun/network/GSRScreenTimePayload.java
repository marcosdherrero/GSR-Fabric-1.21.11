package net.berkle.groupspeedrun.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * C2S: Client reports screen time for a screen type the server cannot detect (e.g. player inventory).
 * Server validates screenTypeId against allowed list before adding to stats.
 */
@SuppressWarnings("null")
public record GSRScreenTimePayload(String screenTypeId) implements CustomPacketPayload {

    public static final Type<GSRScreenTimePayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "screen_time"));

    /** Screen type id for survival player inventory (E key). Server cannot distinguish from game view. */
    public static final String PLAYER_INVENTORY = "minecraft:player_inventory";
    /** Screen type id for creative mode inventory. */
    public static final String CREATIVE_INVENTORY = "minecraft:creative_inventory";

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRScreenTimePayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, GSRScreenTimePayload::screenTypeId,
            GSRScreenTimePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
