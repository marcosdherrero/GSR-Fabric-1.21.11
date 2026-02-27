package net.berkle.groupspeedrun.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * C2S: Client reports screen time for a screen type the server cannot detect (e.g. player inventory).
 * Server validates screenTypeId against allowed list before adding to stats.
 */
@SuppressWarnings("null")
public record GSRScreenTimePayload(String screenTypeId) implements CustomPayload {

    public static final Id<GSRScreenTimePayload> ID = new Id<>(Identifier.of("gsr", "screen_time"));

    /** Screen type id for survival player inventory (E key). Server cannot distinguish from game view. */
    public static final String PLAYER_INVENTORY = "minecraft:player_inventory";
    /** Screen type id for creative mode inventory. */
    public static final String CREATIVE_INVENTORY = "minecraft:creative_inventory";

    public static final PacketCodec<PacketByteBuf, GSRScreenTimePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING, GSRScreenTimePayload::screenTypeId,
            GSRScreenTimePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
