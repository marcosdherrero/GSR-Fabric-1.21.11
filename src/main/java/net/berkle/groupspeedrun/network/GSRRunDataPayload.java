package net.berkle.groupspeedrun.network;

import net.berkle.groupspeedrun.parameter.GSRNetworkParameters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * C2S: Client sends run data. S2C: Server forwards run data to requester.
 *
 * <p>Expected NBT structure: runId (String), runNbt (Compound containing GSRRunSaveState).
 */
public record GSRRunDataPayload(net.minecraft.nbt.CompoundTag nbt) implements CustomPacketPayload {

    public static final Type<GSRRunDataPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "run_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRRunDataPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG_COMPOUND, GSRRunDataPayload::nbt,
            GSRRunDataPayload::new
    );

    public static final String KEY_RUN_ID = "runId";
    public static final String KEY_RUN_NBT = "runNbt";

    /**
     * Validates payload: required keys present. runNbt compound size is bounded by
     * {@link GSRNetworkParameters#RUN_DATA_MAX_NBT_BYTES} via RegistryFriendlyByteBuf limits.
     * Returns true if valid; false if invalid (reject).
     */
    public static boolean isValid(CompoundTag nbt) {
        if (nbt == null) return false;
        return nbt.contains(KEY_RUN_ID) && nbt.contains(KEY_RUN_NBT);
    }

    public static CompoundTag toNbt(String runId, CompoundTag runNbt) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString(KEY_RUN_ID, runId);
        nbt.put(KEY_RUN_NBT, runNbt);
        return nbt;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
