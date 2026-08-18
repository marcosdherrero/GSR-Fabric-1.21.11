package net.berkle.groupspeedrun.network;

import net.berkle.groupspeedrun.GSRClient;
import net.berkle.groupspeedrun.parameter.GSRWorldConfigParameters;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/** C2S: Host sends world config (antiCheatEnabled, locatorNonAdminMode). Server accepts only from host. */
public record GSRWorldConfigPayload(CompoundTag nbt) implements CustomPacketPayload {

    public static final Type<GSRWorldConfigPayload> ID = new Type<>(Identifier.fromNamespaceAndPath("gsr", "world_config"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GSRWorldConfigPayload> CODEC = StreamCodec.composite(
            ByteBufCodecs.COMPOUND_TAG, GSRWorldConfigPayload::nbt,
            GSRWorldConfigPayload::new
    );

    public static final String KEY_ANTI_CHEAT_ENABLED = "antiCheatEnabled";

    /** Builds NBT from client world config for host-editable fields. */
    public static CompoundTag fromConfig() {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean(KEY_ANTI_CHEAT_ENABLED, GSRClient.clientWorldConfig.antiCheatEnabled);
        nbt.putBoolean(GSRWorldConfigParameters.K_AUTO_START_ENABLED, GSRClient.clientWorldConfig.autoStartEnabled);
        nbt.putBoolean(GSRWorldConfigParameters.K_SEED_FILTER_ENABLED, GSRClient.clientWorldConfig.seedFilterEnabled);
        nbt.putInt(GSRWorldConfigParameters.K_LOCATOR_NON_ADMIN_MODE, GSRClient.clientWorldConfig.locatorNonAdminMode);
        return nbt;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
