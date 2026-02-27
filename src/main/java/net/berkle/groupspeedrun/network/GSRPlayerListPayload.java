package net.berkle.groupspeedrun.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** S2C: Server sends list of online players (UUID + name) for Run Manager screens. */
public record GSRPlayerListPayload(net.minecraft.nbt.NbtCompound nbt) implements CustomPayload {

    public static final Id<GSRPlayerListPayload> ID = new Id<>(Identifier.of("gsr", "player_list"));

    public static final PacketCodec<PacketByteBuf, GSRPlayerListPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.NBT_COMPOUND, GSRPlayerListPayload::nbt,
            GSRPlayerListPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
