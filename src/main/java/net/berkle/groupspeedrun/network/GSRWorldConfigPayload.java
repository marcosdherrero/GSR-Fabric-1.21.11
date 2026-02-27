package net.berkle.groupspeedrun.network;

import net.berkle.groupspeedrun.GSRClient;
import net.berkle.groupspeedrun.parameter.GSRWorldConfigParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/** C2S: Host sends world config (antiCheatEnabled, locatorNonAdminMode). Server accepts only from host. */
public record GSRWorldConfigPayload(NbtCompound nbt) implements CustomPayload {

    public static final Id<GSRWorldConfigPayload> ID = new Id<>(Identifier.of("gsr", "world_config"));

    public static final PacketCodec<PacketByteBuf, GSRWorldConfigPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.NBT_COMPOUND, GSRWorldConfigPayload::nbt,
            GSRWorldConfigPayload::new
    );

    public static final String KEY_ANTI_CHEAT_ENABLED = "antiCheatEnabled";

    /** Builds NBT from client world config for host-editable fields. */
    public static NbtCompound fromConfig() {
        NbtCompound nbt = new NbtCompound();
        nbt.putBoolean(KEY_ANTI_CHEAT_ENABLED, GSRClient.clientWorldConfig.antiCheatEnabled);
        nbt.putBoolean(GSRWorldConfigParameters.K_AUTO_START_ENABLED, GSRClient.clientWorldConfig.autoStartEnabled);
        nbt.putInt(GSRWorldConfigParameters.K_LOCATOR_NON_ADMIN_MODE, GSRClient.clientWorldConfig.locatorNonAdminMode);
        return nbt;
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
