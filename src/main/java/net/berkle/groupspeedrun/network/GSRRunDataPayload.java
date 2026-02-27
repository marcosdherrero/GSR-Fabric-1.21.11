package net.berkle.groupspeedrun.network;

import net.berkle.groupspeedrun.parameter.GSRNetworkParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * C2S: Client sends run data. S2C: Server forwards run data to requester.
 *
 * <p>Expected NBT structure: runId (String), runNbt (Compound containing GSRRunSaveState).
 */
public record GSRRunDataPayload(net.minecraft.nbt.NbtCompound nbt) implements CustomPayload {

    public static final Id<GSRRunDataPayload> ID = new Id<>(Identifier.of("gsr", "run_data"));

    public static final PacketCodec<PacketByteBuf, GSRRunDataPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.NBT_COMPOUND, GSRRunDataPayload::nbt,
            GSRRunDataPayload::new
    );

    public static final String KEY_RUN_ID = "runId";
    public static final String KEY_RUN_NBT = "runNbt";

    /**
     * Validates payload: required keys present. runNbt compound size is bounded by
     * {@link GSRNetworkParameters#RUN_DATA_MAX_NBT_BYTES} via PacketByteBuf limits.
     * Returns true if valid; false if invalid (reject).
     */
    public static boolean isValid(NbtCompound nbt) {
        if (nbt == null) return false;
        return nbt.contains(KEY_RUN_ID) && nbt.contains(KEY_RUN_NBT);
    }

    public static NbtCompound toNbt(String runId, NbtCompound runNbt) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString(KEY_RUN_ID, runId);
        nbt.put(KEY_RUN_NBT, runNbt);
        return nbt;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
