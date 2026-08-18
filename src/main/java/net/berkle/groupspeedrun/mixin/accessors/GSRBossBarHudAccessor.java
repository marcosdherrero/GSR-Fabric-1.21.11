package net.berkle.groupspeedrun.mixin.accessors;

import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

/** Exposes the boss bar map so the locator HUD can offset when boss bars are visible. */
@Mixin(BossHealthOverlay.class)
public interface GSRBossBarHudAccessor {
    @Accessor("bossBars")
    Map<UUID, LerpingBossEvent> getBossBars();
}
