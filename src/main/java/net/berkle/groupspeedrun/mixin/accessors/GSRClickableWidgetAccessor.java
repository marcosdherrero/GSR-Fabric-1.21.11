package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.gui.widget.ClickableWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Exposes the active state for disabling buttons (e.g. permission-gated admin buttons). */
@Mixin(ClickableWidget.class)
public interface GSRClickableWidgetAccessor {
    @Accessor("active")
    void gsr$setActive(boolean active);

    /** Invokes vanilla playDownSound to match Minecraft menu button click exactly. */
    @Invoker("playDownSound")
    void gsr$playDownSound(SoundManager soundManager);
}
