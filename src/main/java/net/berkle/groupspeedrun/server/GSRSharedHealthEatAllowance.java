package net.berkle.groupspeedrun.server;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;
import net.berkle.groupspeedrun.parameter.GSRSharedHealthParameters;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks per-player "eat allowance" for shared health: players earn allowance by accumulating
 * exhaustion (sprint, mine, jump, attack). Eating costs allowance. Passive players (hiding)
 * cannot eat without activity. Players who recently took damage are exempt.
 */
public final class GSRSharedHealthEatAllowance {

    /** Per-player eat allowance (exhaustion units). Increases with activity, decreases when eating. */
    private static final Map<UUID, Float> EAT_ALLOWANCE = new ConcurrentHashMap<>();
    /** Per-player tick when they last took damage. Exempt from limit for DAMAGE_EXEMPT_TICKS. */
    private static final Map<UUID, Long> LAST_DAMAGE_TICK = new ConcurrentHashMap<>();

    private GSRSharedHealthEatAllowance() {}

    /**
     * Records exhaustion added for a shared health player. Call from addExhaustion mixin.
     *
     * @param player Server player
     * @param exhaustion Amount added (from sprint, mine, jump, etc.)
     */
    public static void recordExhaustion(ServerPlayerEntity player, float exhaustion) {
        if (player == null || exhaustion <= 0) return;
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || !config.sharedHealthEnabled || !config.isInSharedHealth(player.getUuid())) return;

        EAT_ALLOWANCE.merge(player.getUuid(), exhaustion, (a, b) -> Math.min(GSRSharedHealthParameters.MAX_EAT_ALLOWANCE, a + b));
    }

    /**
     * Records that a shared health player took damage. They are exempt from eat limit for a window.
     *
     * @param player Server player
     * @param worldTick Current server tick
     */
    public static void recordDamage(ServerPlayerEntity player, long worldTick) {
        if (player == null) return;
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || !config.sharedHealthEnabled || !config.isInSharedHealth(player.getUuid())) return;

        LAST_DAMAGE_TICK.put(player.getUuid(), worldTick);
    }

    /**
     * Returns true if the player can eat the given nutrition. If false, eating should be blocked.
     *
     * @param player Server player
     * @param nutrition Nutrition points of the food
     * @param worldTick Current server tick
     * @return true if eating is allowed
     */
    public static boolean canEat(ServerPlayerEntity player, int nutrition, long worldTick) {
        if (player == null || nutrition <= 0) return true;
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || !config.sharedHealthEnabled || !config.isInSharedHealth(player.getUuid())) return true;

        Long lastDamage = LAST_DAMAGE_TICK.get(player.getUuid());
        if (lastDamage != null && (worldTick - lastDamage) < GSRSharedHealthParameters.DAMAGE_EXEMPT_TICKS) {
            return true;
        }

        float cost = nutrition * GSRSharedHealthParameters.EXHAUSTION_PER_NUTRITION;
        Float allowance = EAT_ALLOWANCE.get(player.getUuid());
        float current = allowance != null ? allowance : 0f;
        return current >= cost;
    }

    /**
     * Deducts allowance when a shared health player eats. Call after consumeItem succeeds.
     *
     * @param player Server player
     * @param nutrition Nutrition points consumed
     */
    public static void deductAllowance(ServerPlayerEntity player, int nutrition) {
        if (player == null || nutrition <= 0) return;
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || !config.sharedHealthEnabled || !config.isInSharedHealth(player.getUuid())) return;

        float cost = nutrition * GSRSharedHealthParameters.EXHAUSTION_PER_NUTRITION;
        EAT_ALLOWANCE.compute(player.getUuid(), (uuid, a) -> {
            float current = a != null ? a : 0f;
            float next = Math.max(0f, current - cost);
            return next <= 0 ? null : next;
        });
    }

    /**
     * Sends the "need activity" message to the player. Call when eating is blocked.
     */
    public static void sendNeedActivityMessage(ServerPlayerEntity player) {
        if (player != null) {
            player.sendMessage(net.minecraft.text.Text.literal(GSRUiParameters.MSG_PREFIX + GSRSharedHealthParameters.MSG_NEED_ACTIVITY), false);
        }
    }

    /** Cleans up when player disconnects. Call from disconnect handler. */
    public static void onPlayerDisconnect(UUID uuid) {
        EAT_ALLOWANCE.remove(uuid);
        LAST_DAMAGE_TICK.remove(uuid);
    }
}
