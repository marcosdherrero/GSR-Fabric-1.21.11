package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Invokes vanilla Create World after a filtered seed is written into the UI state. */
@Mixin(CreateWorldScreen.class)
public interface GSRCreateWorldScreenAccessor {
    @Invoker("onCreate")
    void gsr$onCreate();
}
