package net.berkle.groupspeedrun.mixin.trackers;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.GSRStats;
import net.berkle.groupspeedrun.server.GSRSharedHealthBroadcast;
import net.berkle.groupspeedrun.server.GSRSharedHealthEatAllowance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects at consumeItem to enforce eat allowance for shared health and broadcast eating.
 */
@Mixin(LivingEntity.class)
public abstract class GSRPlayerEatTracker {

    @Unique
    private static final ThreadLocal<Integer> gsr$consumedNutrition = new ThreadLocal<>();

    /** Injects at head of consumeItem: block eating if insufficient allowance, else broadcast. */
    @Inject(method = "completeUsingItem", at = @At("HEAD"), cancellable = true)
    private void groupspeedrun$onConsumeItemHead(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer player)) return;

        ItemStack active = self.getActiveItem();
        if (active.isEmpty()) return;

        FoodProperties food = active.get(DataComponents.FOOD);
        if (food == null) return;

        int nutrition = food.nutrition();
        gsr$consumedNutrition.set(nutrition);

        if (player.level() instanceof ServerLevel sw) {
            long tick = sw.getServer() != null ? sw.getServer().getTicks() : 0;
            if (!GSRSharedHealthEatAllowance.canEat(player, nutrition, tick)) {
                gsr$consumedNutrition.remove();
                GSRSharedHealthEatAllowance.sendNeedActivityMessage(player);
                ci.cancel();
                return;
            }
        }

        GSRSharedHealthBroadcast.onSharedHealthPlayerAte(player, active, food);
        if (GSRMain.CONFIG != null && GSRMain.CONFIG.startTime > 0 && !GSRMain.CONFIG.isTimerFrozen) {
            String itemId = active.getRegistryEntry().getKey().map(k -> k.getValue().toString()).orElse(null);
            if (itemId != null) GSRStats.addFoodEaten(player.getUuid(), itemId);
        }
    }

    /** Injects at tail of consumeItem to deduct eat allowance after successful consumption. */
    @Inject(method = "completeUsingItem", at = @At("TAIL"))
    private void groupspeedrun$onConsumeItemTail(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayer player)) return;

        Integer nutrition = gsr$consumedNutrition.get();
        gsr$consumedNutrition.remove();
        if (nutrition != null && nutrition > 0) {
            GSRSharedHealthEatAllowance.deductAllowance(player, nutrition);
        }
    }
}
