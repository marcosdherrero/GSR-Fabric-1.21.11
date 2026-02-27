package net.berkle.groupspeedrun.mixin.trackers;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.GSRStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Tracks damage dealt (by type) and dragon damage. Damage taken is tracked via Fabric AFTER_DAMAGE
 * for accuracy (actual post-armor amount, all damage types).
 */
@Mixin(LivingEntity.class)
public abstract class GSRLivingEntityTracker {

    /** Injects at end of applyDamage to record damage dealt (by type) and dragon damage for stats. */
    @Inject(method = "applyDamage", at = @At("TAIL"))
    private void groupspeedrun$trackDamage(ServerWorld world, DamageSource source, float amount, CallbackInfo ci) {
        if (GSRMain.CONFIG == null || GSRMain.CONFIG.startTime <= 0 || GSRMain.CONFIG.isTimerFrozen) return;
        LivingEntity target = (LivingEntity) (Object) this;
        String typeId = GSRStats.getDamageTypeId(world, source);
        if (source.getAttacker() instanceof ServerPlayerEntity attacker) {
            GSRStats.addDamageDealtByType(attacker.getUuid(), typeId, amount);
            if (target instanceof EnderDragonEntity) {
                GSRStats.addFloat(GSRStats.DRAGON_DAMAGE_MAP, attacker.getUuid(), amount);
            }
        }
    }
}
