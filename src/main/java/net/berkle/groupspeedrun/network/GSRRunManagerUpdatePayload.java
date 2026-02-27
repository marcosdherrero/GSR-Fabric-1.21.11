package net.berkle.groupspeedrun.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** C2S: Client sends updated participant sets (group death, shared health, excluded) from Run Manager. */
public record GSRRunManagerUpdatePayload(net.minecraft.nbt.NbtCompound nbt) implements CustomPayload {

    public static final Id<GSRRunManagerUpdatePayload> ID = new Id<>(Identifier.of("gsr", "run_manager_update"));

    public static final PacketCodec<PacketByteBuf, GSRRunManagerUpdatePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.NBT_COMPOUND, GSRRunManagerUpdatePayload::nbt,
            GSRRunManagerUpdatePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
