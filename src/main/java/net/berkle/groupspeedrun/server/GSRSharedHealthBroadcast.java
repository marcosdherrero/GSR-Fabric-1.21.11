package net.berkle.groupspeedrun.server;

import net.berkle.groupspeedrun.GSRMain;
import net.berkle.groupspeedrun.config.GSRConfigWorld;
import net.berkle.groupspeedrun.parameter.GSRUiParameters;
import net.berkle.groupspeedrun.util.GSRFormatUtil;
import net.berkle.groupspeedrun.server.GSRSharedHealthEatAllowance;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

/**
 * Broadcasts damage and eating events to shared health participants when shared health is enabled.
 * Players in the shared health pool receive chat messages about who took damage (and from what)
 * and who ate food (how much hunger and what item).
 */
public final class GSRSharedHealthBroadcast {

    private GSRSharedHealthBroadcast() {}

    /**
     * Sends a chat message to all players in the shared health pool.
     *
     * @param server Minecraft server
     * @param message Chat message (supports § color codes)
     */
    public static void broadcastToSharedHealthParticipants(MinecraftServer server, Text message) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || !config.sharedHealthEnabled) return;
        for (ServerPlayerEntity p : server.getPlayerManager().getPlayerList()) {
            if (config.isInSharedHealth(p.getUuid())) {
                p.sendMessage(message, false);
            }
        }
    }

    /**
     * Called after a player in shared health takes damage. Broadcasts who took damage, how much, and from what.
     *
     * @param player Player who took damage
     * @param source Damage source
     * @param damageTaken Actual damage taken (after shields, before armor)
     */
    public static void onSharedHealthPlayerDamaged(ServerPlayerEntity player, DamageSource source, float damageTaken) {
        if (damageTaken <= 0) return;
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || !config.sharedHealthEnabled || !config.isInSharedHealth(player.getUuid())) return;
        MinecraftServer server = player.getEntityWorld() instanceof ServerWorld sw ? sw.getServer() : null;
        if (server == null) return;

        GSRSharedHealthEatAllowance.recordDamage(player, server.getTicks());

        String playerName = player.getDisplayName().getString();
        String sourceName = getDamageSourceDisplayName(source);
        String msg = GSRUiParameters.MSG_PREFIX + "§c" + playerName + " §7took §f" + formatDamage(damageTaken)
                + " §7damage from §f" + sourceName;
        broadcastToSharedHealthParticipants(server, Text.literal(msg));
    }

    /**
     * Called when a player in shared health eats food. Broadcasts who ate, how many hunger points, and what item.
     *
     * @param player Player who ate
     * @param stack Item stack consumed (before consumption)
     * @param foodComponent Food component with nutrition
     */
    public static void onSharedHealthPlayerAte(ServerPlayerEntity player, ItemStack stack, FoodComponent foodComponent) {
        GSRConfigWorld config = GSRMain.CONFIG;
        if (config == null || !config.sharedHealthEnabled || !config.isInSharedHealth(player.getUuid())) return;
        MinecraftServer server = player.getEntityWorld() instanceof ServerWorld sw ? sw.getServer() : null;
        if (server == null) return;

        int nutrition = foodComponent.nutrition();
        String playerName = player.getDisplayName().getString();
        String itemName = stack.isEmpty() ? "unknown" : stack.getItem().getName(stack).getString();
        int haunches = (nutrition + 1) / 2;
        String msg = GSRUiParameters.MSG_PREFIX + "§a" + playerName + " §7filled §f" + haunches
                + " §7haunch" + (haunches != 1 ? "es" : "") + " §7with §f" + itemName;
        broadcastToSharedHealthParticipants(server, Text.literal(msg));
    }

    private static String getDamageSourceDisplayName(DamageSource source) {
        Entity attacker = source.getAttacker();
        if (attacker != null) {
            return attacker.getDisplayName().getString();
        }
        String name = source.getName();
        if (name != null && name.contains(".")) {
            name = name.substring(name.lastIndexOf('.') + 1);
        }
        return name != null ? name.replace('_', ' ') : "unknown";
    }

    private static String formatDamage(float damage) {
        return GSRFormatUtil.formatNumber(damage);
    }
}
