package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes the save's level id so GSR can reload the same world after a snapshot restore. */
@Mixin(MinecraftServer.class)
public interface GSRMinecraftServerAccessor {
    @Accessor("storageSource")
    LevelStorageSource.LevelStorageAccess gsr$getStorageSource();
}
