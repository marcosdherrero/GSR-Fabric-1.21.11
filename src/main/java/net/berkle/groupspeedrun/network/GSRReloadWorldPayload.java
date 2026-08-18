package net.berkle.groupspeedrun.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * S2C: Host should disconnect and reopen this save so the snapshot restore on next load can run.
 *
 * @param levelId vanilla save folder id ({@code LevelStorageAccess.getLevelId()})
 */
public record GSRReloadWorldPayload(String levelId) implements CustomPacketPayload {

    public static final Type<GSRReloadWorldPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "reload_world"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRReloadWorldPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, GSRReloadWorldPayload::levelId,
            GSRReloadWorldPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
