package net.berkle.groupspeedrun.config;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * S2C payload carrying world run state (and later player HUD config) as NBT.
 */
public record GSRConfigPayload(NbtCompound nbt) implements CustomPayload {

    public static final Id<GSRConfigPayload> ID = new Id<>(Identifier.of("gsr", "config_sync"));

    public static final PacketCodec<PacketByteBuf, GSRConfigPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.NBT_COMPOUND, GSRConfigPayload::nbt,
            GSRConfigPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
