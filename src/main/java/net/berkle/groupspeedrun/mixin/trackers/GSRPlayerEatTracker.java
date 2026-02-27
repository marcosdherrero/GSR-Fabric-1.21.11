package net.berkle.groupspeedrun.mixin.trackers;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.GSRStats;
import net.berkle.groupspeedrun.server.GSRSharedHealthBroadcast;
import net.berkle.groupspeedrun.server.GSRSharedHealthEatAllowance;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
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
    @Inject(method = "consumeItem", at = @At("HEAD"), cancellable = true)
    private void groupspeedrun$onConsumeItemHead(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayerEntity player)) return;

        ItemStack active = self.getActiveItem();
        if (active.isEmpty()) return;

        FoodComponent food = active.get(DataComponentTypes.FOOD);
        if (food == null) return;

        int nutrition = food.nutrition();
        gsr$consumedNutrition.set(nutrition);

        if (player.getEntityWorld() instanceof ServerWorld sw) {
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
    @Inject(method = "consumeItem", at = @At("TAIL"))
    private void groupspeedrun$onConsumeItemTail(CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof ServerPlayerEntity player)) return;

        Integer nutrition = gsr$consumedNutrition.get();
        gsr$consumedNutrition.remove();
        if (nutrition != null && nutrition > 0) {
            GSRSharedHealthEatAllowance.deductAllowance(player, nutrition);
        }
    }
}
