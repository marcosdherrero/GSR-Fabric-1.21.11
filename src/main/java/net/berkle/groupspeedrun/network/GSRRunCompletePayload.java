package net.berkle.groupspeedrun.network;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** S2C: Server sends completed run data so clients can save to shared folder. */
public record GSRRunCompletePayload(net.minecraft.nbt.NbtCompound nbt) implements CustomPayload {

    public static final Id<GSRRunCompletePayload> ID = new Id<>(Identifier.of("gsr", "run_complete"));

    public static final PacketCodec<PacketByteBuf, GSRRunCompletePayload> CODEC = PacketCodec.tuple(
            PacketCodecs.NBT_COMPOUND, GSRRunCompletePayload::nbt,
            GSRRunCompletePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
